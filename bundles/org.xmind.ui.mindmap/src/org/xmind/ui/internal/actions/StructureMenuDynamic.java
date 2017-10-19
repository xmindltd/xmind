package org.xmind.ui.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.ITopic;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.branch.IBranchPolicyDescriptor;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTopicStructureCommand;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class StructureMenuDynamic extends ContributionItem implements
        IWorkbenchContribution {

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

    private boolean dirty = true;

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overridden to always return true and force dynamic menu building.
     */
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void fill(Menu menu, int index) {
        if (page == null)
            return;

        if (getParent() instanceof MenuManager) {
            ((MenuManager) getParent()).addMenuListener(menuListener);
        }

        if (!dirty) {
            return;
        }

        MenuManager manager = new MenuManager();
        fillMenu(manager);
        IContributionItem items[] = manager.getItems();
        if (items.length > 0) {
            for (int i = 0; i < items.length; i++) {
                items[i].fill(menu, index++);
            }
        }
        dirty = false;
    }

    private void fillMenu(IMenuManager menu) {
        List<IBranchPolicyDescriptor> descriptors = getCurrentStructures();
        if (descriptors == null)
            return;

        for (IBranchPolicyDescriptor descriptor : descriptors) {
            addStructureAction(descriptor, menu);
        }
    }

    private void addStructureAction(IBranchPolicyDescriptor descriptor,
            IMenuManager menu) {
        menu.add(new StructureAction(descriptor));
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

    public void initialize(IServiceLocator serviceLocator) {
        IWorkbenchWindow window = (IWorkbenchWindow) serviceLocator
                .getService(IWorkbenchWindow.class);
        if (window == null)
            return;

        IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null)
            return;

        IEditorPart editor = activePage.getActiveEditor();
        if (editor instanceof IGraphicalEditor) {
            page = ((IGraphicalEditor) editor).getActivePageInstance();
        }

    }

}
