/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

/**
 * @author frankshaka
 * 
 */
public class CancelHyperlinkAction extends PageAction implements
        ISelectionAction {

    /**
     * 
     */
    public CancelHyperlinkAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.CANCEL_HYPERLINK.getId(), page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (isDisposed())
            return;

        sendRequest(new Request(MindMapUI.REQ_MODIFY_HYPERLINK).setParameter(
                GEF.PARAM_TEXT, null));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.ui.actions.ISelectionAction#setSelection(org.eclipse.jface
     * .viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection)
                && hasModifiableHyperlink(selection));
    }

    /**
     * @param selection
     * @return
     */
    private boolean hasModifiableHyperlink(ISelection selection) {
        Object topic = ((IStructuredSelection) selection).getFirstElement();
        if (topic == null || !(topic instanceof ITopic))
            return false;
        String hyperlink = ((ITopic) topic).getHyperlink();
        if (hyperlink == null)
            return false;
        return MindMapUI.getProtocolManager().isHyperlinkModifiable(topic,
                hyperlink);
    }

}
