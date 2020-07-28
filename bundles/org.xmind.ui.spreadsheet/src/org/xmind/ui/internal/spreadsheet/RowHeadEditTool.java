/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.spreadsheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.spreadsheet.structures.Cell2;
import org.xmind.ui.internal.spreadsheet.structures.Chart2;
import org.xmind.ui.internal.spreadsheet.structures.Column2;
import org.xmind.ui.internal.spreadsheet.structures.Item2;
import org.xmind.ui.internal.spreadsheet.structures.Row2;
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.internal.tools.LabelProposalProvider;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.ContentProposalAdapter;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.FloatingTextEditorContentAssistAdapter;
import org.xmind.ui.tools.MindMapEditToolBase;

public class RowHeadEditTool extends MindMapEditToolBase {

    private Chart2 chart;

    private RowHead rowHead;

    private Row2 row;

    private RowHeadEditorHelper helper;

    protected ContentProposalAdapter contentProposalAdapter = null;

    protected boolean acceptEditRequest(Request request) {
        setChart((Chart2) request.getParameter(Spreadsheet.PARAM_CHART));
        setRow((Row2) request.getParameter(Spreadsheet.PARAM_ROW));
        setRowHead((RowHead) request.getParameter(Spreadsheet.PARAM_ROW_HEAD));
        return super.acceptEditRequest(request);
    }

    public void setChart(Chart2 chart) {
        Assert.isNotNull(chart);
        this.chart = chart;
    }

    public void setRowHead(RowHead rowHead) {
        Assert.isNotNull(rowHead);
        this.rowHead = rowHead;
    }

    public void setRow(Row2 row) {
        Assert.isNotNull(row);
        this.row = row;
    }

    protected String getInitialText(IPart source) {
        Assert.isNotNull(rowHead);
        Assert.isNotNull(row);
        return rowHead.toString();
    }

    @Override
    protected void handleTextModified(IPart source, IDocument document) {
        if (finishedOnMouseDown) {
            finishedOnMouseDown = false;
            if (shouldIgnoreTextChange(source, document, oldText))
                return;
        }
        Request request = createTextRequest(source, document);
        if (request != null)
            source.handleRequest(request,
                    Spreadsheet.ROLE_SHEET_HEAD_MODIFIABLE);
    }

    protected Request createTextRequest(IPart source, IDocument document) {
        if (row == null)
            return null;

        if (isRowEmpty()) {
            Request request = new Request(MindMapUI.REQ_CREATE_CHILD)
                    .setViewer(getTargetViewer());
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    document.get());
            for (Cell2 cell : row.getCells()) {
                Column2 col = cell.getOwnedColumn();
                if (col != null) {
                    request.setPrimaryTarget(col.getHead());
                    return request;
                }
            }
            return null;
        } else {
            Request request = new Request(Spreadsheet.REQ_MODIFY_SHEET_HEAD);
            request.setParameter(GEF.PARAM_TEXT, document.get());
            List<IPart> targets = new ArrayList<IPart>();
            for (Cell2 cell : row.getCells()) {
                for (Item2 item : cell.getItems()) {
                    targets.add(item.getBranch().getTopicPart());
                }
            }
            request.setTargets(targets);
            return request;
        }
    }

    private boolean isRowEmpty() {
        if (!row.getHead().isEmpty())
            return false;
        for (Cell2 cell : row.getCells())
            if (!cell.getItems().isEmpty())
                return false;
        return true;
    }

    protected void hookEditor(FloatingTextEditor editor) {
        super.hookEditor(editor);
        if (helper == null) {
            helper = new RowHeadEditorHelper(true);
        }
        helper.setEditor(editor);
        helper.setViewer(getTargetViewer());
        helper.setChart(chart);
        helper.setRow(row);
        helper.setRowHead(rowHead);
        helper.activate();
    }

    protected void unhookEditor(FloatingTextEditor editor) {
        if (helper != null) {
            helper.deactivate();
        }
        super.unhookEditor(editor);
    }

    protected void hookEditorControl(FloatingTextEditor editor,
            ITextViewer textViewer) {
        super.hookEditorControl(editor, textViewer);
        LabelProposalProvider proposalProvider = new LabelProposalProvider(
                getSource());
        if (contentProposalAdapter == null) {
            contentProposalAdapter = new FloatingTextEditorContentAssistAdapter(
                    editor, proposalProvider);
            contentProposalAdapter.setProposalAcceptanceStyle(
                    ContentProposalAdapter.PROPOSAL_REPLACE);
            contentProposalAdapter.setPopupSize(new Point(180, 80));
            final Image labelImage = createLabelProposalImage();
            if (labelImage != null) {
                contentProposalAdapter.setLabelProvider(new LabelProvider() {
                    public Image getImage(Object element) {
                        return labelImage;
                    }
                });
            }
            editor.getControl().addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    contentProposalAdapter.setLabelProvider(null);
                    contentProposalAdapter = null;
                    if (labelImage != null)
                        labelImage.dispose();
                }
            });
        } else {
            contentProposalAdapter.setContentProposalProvider(proposalProvider);
        }
    }

    private Image createLabelProposalImage() {
        return MindMapUI.getImages().get(IMindMapImages.LABEL, true)
                .createImage(false, Display.getCurrent());
    }

}