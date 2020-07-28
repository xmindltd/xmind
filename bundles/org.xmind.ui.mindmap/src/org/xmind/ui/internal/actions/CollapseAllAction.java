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

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class CollapseAllAction extends RequestAction
        implements ISelectionAction {

    public CollapseAllAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.COLLAPSE_ALL.getId(), page,
                GEF.REQ_COLLAPSE_ALL);
    }

    public void setSelection(ISelection selection) {
        //1.select sheet (which root topic has attached children).
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (structuredSelection.size() == 1) {
                Object obj = structuredSelection.getFirstElement();
                if (obj instanceof ISheet) {
                    ISheet sheet = (ISheet) obj;
                    setEnabled(sheet.getRootTopic().getChildren(ITopic.ATTACHED)
                            .size() != 0);
                    return;
                }
            }
        }

        //2.select topics (at least one topic has attached children).
        if (MindMapUtils.isAllSuchElements(selection,
                MindMapUI.CATEGORY_TOPIC)) {
            boolean enabled = false;
            List<Object> topics = MindMapUtils.getAllSuchElements(selection,
                    MindMapUI.CATEGORY_TOPIC);
            for (Object topic : topics) {
                if (((ITopic) topic).getChildren(ITopic.ATTACHED).size() != 0) {
                    enabled = true;
                    break;
                }
            }
            setEnabled(enabled);
            return;
        }

        setEnabled(false);
    }

}
