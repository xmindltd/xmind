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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ITopic;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.branch.IBranchPolicyDescriptor;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTopicStructureCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class StructureMenu extends MenuManager {

    private class StructureAction extends Action {

        private IBranchPolicyDescriptor descriptor;

        public StructureAction(IBranchPolicyDescriptor descriptor) {
            this.descriptor = descriptor;
            setId("#" + descriptor.getId()); //$NON-NLS-1$
            setText(descriptor.getName());
            setImageDescriptor(descriptor.getIcon());
        }

        public void run() {
            if (page == null)
                return;

            changeStructureClass(descriptor.getId());
        }

        private void changeStructureClass(String newStructureClass) {
            EditDomain domain = page.getEditDomain();
            if (domain == null)
                return;

            ICommandStack commandStack = domain.getCommandStack();
            if (commandStack == null)
                return;

            List<ITopic> topics = getTopics();
            if (topics != null && !topics.isEmpty()) {
                ModifyTopicStructureCommand command = new ModifyTopicStructureCommand(
                        topics, newStructureClass);
                command.setLabel(CommandMessages.Command_ModifyTopicStructure);
                commandStack.execute(command);
            }
        }

        private List<ITopic> getTopics() {
            ISelection selection = page.getViewer().getSelection();
            if (selection.isEmpty()
                    || !(selection instanceof IStructuredSelection))
                return null;
            Object[] elements = ((IStructuredSelection) selection).toArray();
            ArrayList<ITopic> list = new ArrayList<ITopic>(elements.length);
            for (Object o : elements) {
                if (o instanceof ITopic && !list.contains(o))
                    list.add((ITopic) o);
            }
            return list;
        }

    }

    private IGraphicalEditorPage page;

    public StructureMenu() {
        super(MindMapMessages.Structure_text, ActionConstants.STRUCTURE_MENU);
        setRemoveAllWhenShown(true);
        addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillMenu();
            }
        });
    }

    public void setActivePage(IGraphicalEditorPage page) {
        this.page = page;
    }

    private void fillMenu() {
        List<IBranchPolicyDescriptor> descriptors = getCurrentStructures();
        if (descriptors == null)
            return;

        for (IBranchPolicyDescriptor descriptor : descriptors) {
            addStructureAction(descriptor);
        }
    }

    private void addStructureAction(IBranchPolicyDescriptor descriptor) {
        add(new StructureAction(descriptor));
    }

    private List<IBranchPolicyDescriptor> getCurrentStructures() {
        if (page == null)
            return null;
        List<IBranchPolicyDescriptor> list = null;
        IViewer viewer = page.getViewer();
        if (viewer != null) {
            ISelection selection = viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                for (Object o : ((IStructuredSelection) selection).toArray()) {
                    if (o instanceof ITopic) {
                        IBranchPart branch = MindMapUtils.findBranch(viewer
                                .findPart(o));
                        if (branch != null) {
                            List<IBranchPolicyDescriptor> applicables = MindMapUI
                                    .getBranchPolicyManager()
                                    .getApplicableBranchPolicyDescriptors(
                                            branch);
                            if (applicables.isEmpty())
                                return null;
                            if (list == null) {
                                list = new ArrayList<IBranchPolicyDescriptor>(
                                        applicables);
                            } else {
                                list.retainAll(applicables);
                            }
                            if (list.isEmpty())
                                return null;
                        }
                    }
                }
            }
        }
        return list;
    }

}