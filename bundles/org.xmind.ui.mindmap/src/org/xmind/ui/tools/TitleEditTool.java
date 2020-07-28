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
package org.xmind.ui.tools;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.text.IDocument;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.ITitledFigure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.texteditor.FloatingTextEditor;
import org.xmind.ui.texteditor.FloatingTextEditorHelper;
import org.xmind.ui.util.MindMapUtils;

public class TitleEditTool extends MindMapEditToolBase {

    private FloatingTextEditorHelper helper = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.EditTool#canEdit(org.xmind.gef.part.IGraphicalEditPart
     * )
     */
    @Override
    protected boolean canEdit(IGraphicalEditPart target) {
        return super.canEdit(target) && !(target instanceof ITopicPart);
    }

    protected String getInitialText(IPart source) {
        Object m = MindMapUtils.getRealModel(source);
        if (m instanceof ITitled) {
            return ((ITitled) m).getTitleText();
        }
        return null;
    }

    protected Request createTextRequest(IPart source, IDocument document) {
        Request request = createTargetedRequest(GEF.REQ_MODIFY,
                getTargetViewer(), false);
        request.setParameter(GEF.PARAM_TEXT, document.get());
        return request;
    }

    protected boolean isMultilineAllowed() {
        return true;
    }

    protected boolean isWrapAllowed() {
        return true;
    }

    protected void hookEditor(FloatingTextEditor editor) {
        super.hookEditor(editor);
        if (helper == null) {
            helper = createHelper();
        }
        helper.setEditor(editor);
        helper.setViewer(getTargetViewer());
        helper.setFigure(getTitleFigure());
        helper.activate();
    }

    protected FloatingTextEditorHelper createHelper() {
        return new FloatingTextEditorHelper(true);
    }

    protected FloatingTextEditorHelper getHelper() {
        return helper;
    }

    protected void unhookEditor(FloatingTextEditor editor) {
        if (helper != null) {
            helper.deactivate();
        }
        super.unhookEditor(editor);
    }

    protected IFigure getTitleFigure() {
        IFigure figure = getSource().getFigure();
        if (figure instanceof ITitledFigure) {
            ITextFigure t = ((ITitledFigure) figure).getTitle();
            if (t != null)
                return t;
        }
        return figure;
    }

}