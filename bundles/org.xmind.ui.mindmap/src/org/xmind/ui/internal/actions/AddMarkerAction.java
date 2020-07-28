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

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.EditDomain;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use command and handlers
 */
public class AddMarkerAction extends PageAction implements ISelectionAction {

    private String markerId;

    public AddMarkerAction(String id, IGraphicalEditorPage page) {
        this(id, page, null);
    }

    public AddMarkerAction(String id, IGraphicalEditorPage page,
            String markerId) {
        super(id, page);
        this.markerId = markerId;
    }

    public void runWithMarkerId(String markerId) {
        if (getPage() == null)
            return;

        EditDomain domain = getEditDomain();
        if (domain == null)
            return;

        Request request = new Request(MindMapUI.REQ_ADD_MARKER);
        request.setViewer(getViewer());
        request.setDomain(domain);
        request.setParameter(MindMapUI.PARAM_MARKER_ID, markerId);
        domain.handleRequest(request);
    }

    public void run() {
        if (markerId == null)
            return;

        runWithMarkerId(markerId);
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.hasSuchElements(selection,
                MindMapUI.CATEGORY_TOPIC));
    }

}