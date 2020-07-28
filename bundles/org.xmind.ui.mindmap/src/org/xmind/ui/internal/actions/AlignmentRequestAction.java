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
package org.xmind.ui.internal.actions;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class AlignmentRequestAction extends RequestAction implements
        ISelectionAction {

    private int alignment;

    public AlignmentRequestAction(IGraphicalEditorPage page, int alignment) {
        super(page, GEF.REQ_ALIGN);
        this.alignment = alignment;
        switch (alignment) {
        case PositionConstants.LEFT:
            setId(ActionConstants.ALIGNMENT_LEFT_ID);
            break;
        case PositionConstants.CENTER:
            setId(ActionConstants.ALIGNMENT_CENTER_ID);
            break;
        case PositionConstants.RIGHT:
            setId(ActionConstants.ALIGNMENT_RIGHT_ID);
            break;
        case PositionConstants.TOP:
            setId(ActionConstants.ALIGNMENT_TOP_ID);
            break;
        case PositionConstants.MIDDLE:
            setId(ActionConstants.ALIGNMENT_MIDDLE_ID);
            break;
        case PositionConstants.BOTTOM:
            setId(ActionConstants.ALIGNMENT_BOTTOM_ID);
            break;
        }
    }

    public int getAlignment() {
        return alignment;
    }

    public void run() {
        if (isDisposed())
            return;

        Request request = new Request(getRequestType());
        request.setDomain(getEditDomain());
        request.setViewer(getViewer());
        request.setParameter(GEF.PARAM_ALIGNMENT, getAlignment());
        sendRequest(request);
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.hasSuchElements(selection,
                MindMapUI.CATEGORY_TOPIC)
                && !MindMapUtils.isSingleTopic(selection));
    }

}