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
package org.xmind.ui.internal.tools;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.ILabelPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.texteditor.ContentProposalAdapter;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.FloatingTextEditorContentAssistAdapter;
import org.xmind.ui.texteditor.FloatingTextEditorHelper;
import org.xmind.ui.tools.MindMapEditToolBase;
import org.xmind.ui.util.MindMapUtils;

public class LabelEditTool extends MindMapEditToolBase {

    private class LabelEditorHelper extends FloatingTextEditorHelper {

        private int prefHeight = -1;

        public LabelEditorHelper() {
            super(true);
            setMinWidth(160);
        }

        protected Font getPreferredFont(IFigure figure) {
            if (label != null && figure == label.getFigure())
                return super.getPreferredFont(figure);
            return JFaceResources.getDefaultFont();
        }

        protected Rectangle calcPreferredBounds(IFigure figure,
                Rectangle bounds) {
            if (label == null || figure != label.getFigure()) {
                bounds = bounds.getTranslated(0, bounds.height);
                if (prefHeight < 0) {
                    prefHeight = getPreferredFont(figure).getFontData()[0]
                            .getHeight();
                }
                bounds.height = prefHeight;
            }
            return super.calcPreferredBounds(figure, bounds);
        }

    }

    private LabelEditorHelper helper = null;

    private ILabelPart label = null;

    protected ContentProposalAdapter contentProposalAdapter = null;

    public LabelEditTool() {
        addEditRequestType(MindMapUI.REQ_EDIT_LABEL);
    }

    public ILabelPart getLabelPart() {
        return label;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.tool.EditTool#canEdit(org.xmind.gef.part.IGraphicalEditPart
     * )
     */
    @Override
    protected boolean canEdit(IGraphicalEditPart target) {
        return super.canEdit(target) && target instanceof ITopicPart;
    }

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof ITopicPart);
        label = ((ITopicPart) source).getOwnerBranch().getLabel();
        super.setSource(source);
    }

    protected String getInitialText(IPart source) {
        if (label != null)
            return label.getLabelText();
        return MindMapUtils
                .getLabelText(((ITopicPart) source).getTopic().getLabels());
    }

    protected Request createTextRequest(IPart source, IDocument document) {
        Request request = createTargetedRequest(MindMapUI.REQ_MODIFY_LABEL,
                getTargetViewer(), false);
        request.setParameter(GEF.PARAM_TEXT, document.get());
        return request;
    }

    protected void hookEditor(FloatingTextEditor editor) {
        super.hookEditor(editor);
        if (helper == null) {
            helper = new LabelEditorHelper();
        }
        helper.setEditor(editor);
        helper.setViewer(getTargetViewer());
        helper.setFigure(getHookFigure());
        helper.activate();
    }

    private IFigure getHookFigure() {
        if (label != null)
            return label.getFigure();
        return getSource().getFigure();
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
        return null;
    }

}
