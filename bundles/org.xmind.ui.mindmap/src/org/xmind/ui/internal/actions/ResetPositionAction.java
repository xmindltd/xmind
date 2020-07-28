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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.IRelationship;
import org.xmind.core.ITopic;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;

public class ResetPositionAction extends RequestAction
        implements ISelectionAction {

    public ResetPositionAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.RESET_POSITION.getId(), page,
                MindMapUI.REQ_RESET_POSITION);
    }

    public void setSelection(ISelection selection) {
        // TODO Auto-generated method stub
        if (selection instanceof IStructuredSelection) {
            int num = ((IStructuredSelection) selection).size();
            for (Object o : ((IStructuredSelection) selection).toArray()) {
                if (!isResettable(o)) {
                    num--;
                }
            }
            setEnabled(num > 0);
        }
    }

    private boolean isResettable(Object o) {
        // TODO Auto-generated method stub
        if (o instanceof ITopic) {
            ITopic t = (ITopic) o;
            ITopic central = (ITopic) getViewer().getAdapter(ITopic.class);
            if (central != null && central.equals(t.getParent())
                    && t.isAttached()) {
                if (t.getPosition() != null)
                    return true;
            }
        } else if (o instanceof IRelationship) {
            IRelationship r = (IRelationship) o;
            if (r.getControlPoint(0).getPosition() != null
                    || r.getControlPoint(1).getPosition() != null)
                return true;
        }
        return false;
    }

    @Override
    public void run() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_MODIFY,
                UserDataConstants.RESET_POSITION);
        super.run();
    }

}
