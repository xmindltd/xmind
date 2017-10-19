/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
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
import org.xmind.ui.internal.spreadsheet.structures.Chart;
import org.xmind.ui.internal.spreadsheet.structures.Column;
import org.xmind.ui.internal.spreadsheet.structures.ColumnHead;
import org.xmind.ui.internal.spreadsheet.structures.ColumnInsertion;
import org.xmind.ui.internal.spreadsheet.structures.ColumnOrder;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetStructure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.util.MindMapUtils;

public class ColumnMoveTool extends DummyMoveTool {

    private IBranchPart branch = null;

    private ColumnHead sourceColHead = null;

    protected void start() {
        branch = (IBranchPart) super.getSource();
        sourceColHead = (ColumnHead) MindMapUtils.getCache(getSource(),
                Spreadsheet.CACHE_MOVE_SOURCE_COLUMN_HEAD);
        Assert.isNotNull(sourceColHead);
        super.start();
    }

    protected void end() {
        IInsertion insertion = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_COLUMN_INSERTION);
        if (insertion != null) {
            insertion.pullOut();
            MindMapUtils.flushCache(branch, Spreadsheet.CACHE_COLUMN_INSERTION);
        }
        MindMapUtils.flushCache(branch,
                Spreadsheet.CACHE_MOVE_SOURCE_COLUMN_HEAD);
        super.end();
    }

    protected IFigure createDummy() {
        Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
        if (layer != null) {
            RotatableWrapLabel fig = new RotatableWrapLabel(sourceColHead
                    .toString(), RotatableWrapLabel.NORMAL);
            layer.add(fig);
            fig.setFont(sourceColHead.getFont());
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
        if (structure instanceof SpreadsheetStructure) {
            int index = ((SpreadsheetStructure) structure)
                    .calcColumnInsertionIndex(branch, currentPos);
            installInsertion(index);
        }
    }

    private void installInsertion(int index) {
        IInsertion oldInsertion = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_COLUMN_INSERTION);
        if (oldInsertion == null || oldInsertion.getIndex() != index) {
            if (oldInsertion != null) {
                oldInsertion.pullOut();
            }
            IInsertion newInsertion = new ColumnInsertion(branch, index,
                    sourceColHead.getPrefSize());
            newInsertion.pushIn();
        }
    }

    protected Request createRequest() {
        // TODO refactor this 'move column' process into request and policy 
        IInsertion ins = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_COLUMN_INSERTION);
        if (ins != null) {
            int insIndex = ins.getIndex();
            IStructure structure = branch.getBranchPolicy()
                    .getStructure(branch);
            if (structure instanceof SpreadsheetStructure) {
                Chart chart = ((SpreadsheetStructure) structure)
                        .getChart(branch);
                List<Column> columns = chart.getColumns();
                ColumnOrder newOrder = new ColumnOrder();
                for (int i = 0; i < columns.size(); i++) {
                    if (i == insIndex) {
                        newOrder.addColumnHead(sourceColHead);
                    }
                    Column col = columns.get(i);
                    if (!sourceColHead.equals(col.getHead())) {
                        newOrder.addColumnHead(col.getHead());
                    }
                }

                ModifyColumnOrderCommand command = new ModifyColumnOrderCommand(
                        chart.getTitle().getTopic(), newOrder);
                ICommandStack cs = getDomain().getCommandStack();
                if (cs != null) {
                    command.setLabel(Messages.Command_MoveColumn);
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