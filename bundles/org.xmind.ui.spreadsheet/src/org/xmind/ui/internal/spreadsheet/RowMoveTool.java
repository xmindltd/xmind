/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.spreadsheet;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.spreadsheet.structures.Chart2;
import org.xmind.ui.internal.spreadsheet.structures.Row2;
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.internal.spreadsheet.structures.RowInsertion;
import org.xmind.ui.internal.spreadsheet.structures.RowOrder;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetColumnStructure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.util.MindMapUtils;

public class RowMoveTool extends DummyMoveTool {

    private IBranchPart branch = null;

    private RowHead sourceRowHead = null;

    protected void start() {
        branch = (IBranchPart) super.getSource();
        sourceRowHead = (RowHead) MindMapUtils.getCache(getSource(),
                Spreadsheet.CACHE_MOVE_SOURCE_ROW_HEAD);
        Assert.isNotNull(sourceRowHead);
        super.start();
    }

    protected void end() {
        IInsertion insertion = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_ROW_INSERTION);
        if (insertion != null) {
            insertion.pullOut();
            MindMapUtils.flushCache(branch, Spreadsheet.CACHE_ROW_INSERTION);
        }
        MindMapUtils.flushCache(branch, Spreadsheet.CACHE_MOVE_SOURCE_ROW_HEAD);
        super.end();
    }

    protected IFigure createDummy() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            RotatableWrapLabel fig = new RotatableWrapLabel(
                    sourceRowHead.toString(), RotatableWrapLabel.NORMAL);
            layer.add(fig);
            fig.setFont(sourceRowHead.getFont());
            fig.setSize(fig.getPreferredSize());
            fig.setLocation(getStartingPosition().getTranslated(
                    fig.getSize().scale(0.5).negate()));
            return fig;
        }
        return null;
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        super.onMoving(currentPos, me);
        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        if (structure instanceof SpreadsheetColumnStructure) {
            int index = ((SpreadsheetColumnStructure) structure)
                    .calcRowInsertionIndex(branch, currentPos);
            installInsertion(index);
        }
    }

    private void installInsertion(int index) {
        IInsertion oldInsertion = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_ROW_INSERTION);
        if (oldInsertion == null || oldInsertion.getIndex() != index) {
            if (oldInsertion != null) {
                oldInsertion.pullOut();
            }
            IInsertion newInsertion = new RowInsertion(branch, index,
                    sourceRowHead.getPrefSize());
            newInsertion.pushIn();
        }
    }

    protected Request createRequest() {
        IInsertion ins = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_ROW_INSERTION);
        if (ins != null) {
            int insIndex = ins.getIndex();
            IStructure structure = branch.getBranchPolicy()
                    .getStructure(branch);
            if (structure instanceof SpreadsheetColumnStructure) {
                Chart2 chart = ((SpreadsheetColumnStructure) structure)
                        .getChart(branch);
                List<Row2> rows = chart.getRows();
                RowOrder newOrder = new RowOrder();
                for (int i = 0; i < rows.size(); i++) {
                    if (i == insIndex) {
                        newOrder.addRowHead(sourceRowHead);
                    }
                    Row2 row = rows.get(i);
                    if (!sourceRowHead.equals(row.getHead())) {
                        newOrder.addRowHead(row.getHead());
                    }
                }

                ModifyRowOrderCommand command = new ModifyRowOrderCommand(chart
                        .getTitle().getTopic(), newOrder);
                ICommandStack cs = getDomain().getCommandStack();
                if (cs != null) {
                    command.setLabel(Messages.Command_MoveRow);
                    cs.execute(command);
                }
            }
        }
        return null;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return Cursors.HAND;
    }
}