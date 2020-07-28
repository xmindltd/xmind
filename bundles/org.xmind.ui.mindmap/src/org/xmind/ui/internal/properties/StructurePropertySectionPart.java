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
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.IViewer;
import org.xmind.ui.branch.IBranchPolicyDescriptor;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTopicStructureCommand;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.MindMapPropertySectionPartBase;
import org.xmind.ui.resources.ImageDescriptorProvider;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.ImageCachedLabelProvider;
import org.xmind.ui.viewers.MComboViewer;

public class StructurePropertySectionPart
        extends MindMapPropertySectionPartBase {

    private static final List<IBranchPolicyDescriptor> NO_BRANCH_POLICY = Collections
            .emptyList();

    private static final String FOLLOW = "org.xmind.ui.properties.structure.follow"; //$NON-NLS-1$

    private static class BranchPolicyLabelProvider extends
            ImageCachedLabelProvider implements ImageDescriptorProvider {

        protected Image createImage(Object element) {
            if (element instanceof IBranchPolicyDescriptor) {
                IBranchPolicyDescriptor desc = (IBranchPolicyDescriptor) element;
                ImageDescriptor icon = desc.getIcon();
                if (icon != null)
                    return icon.createImage(false);
            }
            return null;
        }

        @Override
        public ImageDescriptor getImageDescriptor(Object element) {
            if (element instanceof IBranchPolicyDescriptor) {
                IBranchPolicyDescriptor desc = (IBranchPolicyDescriptor) element;
                return desc.getIcon();
            }
            return null;
        }

        public String getText(Object element) {
            if (element instanceof IBranchPolicyDescriptor) {
                IBranchPolicyDescriptor desc = (IBranchPolicyDescriptor) element;
                return desc.getName();
            }
            if (element instanceof String) {
                return PropertyMessages.FollowParentStructure_text;
            }

            return super.getText(element);
        }
    }

    private class BranchPolicySelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;
            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof IBranchPolicyDescriptor) {
                changeStructure((IBranchPolicyDescriptor) o);
            } else {
                doFollowParentStructure();
            }
        }

    }

    private MComboViewer structureViewer;

//    private Button followParentStructureCheck;

    protected GridLayout createLayout(Composite parent) {
        GridLayout layout = super.createLayout(parent);
        layout.verticalSpacing = 7;
        return layout;
    }

    protected void createContent(Composite parent) {
        structureViewer = new MComboViewer(parent, SWT.NONE);
        structureViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        structureViewer.getControl()
                .setToolTipText(PropertyMessages.Structure_toolTip);
        structureViewer.setContentProvider(new ArrayContentProvider());
        structureViewer.setLabelProvider(new BranchPolicyLabelProvider());
        structureViewer.addSelectionChangedListener(
                new BranchPolicySelectionChangedListener());
    }

    public void setFocus() {
        if (structureViewer != null
                && !structureViewer.getControl().isDisposed())
            structureViewer.getControl().setFocus();
    }

    protected void doRefresh() {
        if (structureViewer != null
                && !structureViewer.getControl().isDisposed()) {
            List<IBranchPolicyDescriptor> enableds = getCurrentStructures();
            if (getCurrentFollowParentStructure())
                structureViewer.setInput(enableds);
            else {
                List<Object> list = new ArrayList<Object>(enableds);
                Object separator = new Object();
                list.add(separator);
                list.add(FOLLOW);
                structureViewer.setSeparatorImitation(separator);
                structureViewer.setInput(list);
            }
            structureViewer.setSelection(getCurrentStructure());
            structureViewer.getControl().setEnabled(!enableds.isEmpty());
        }
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        if (source instanceof ITopic) {
            register.register(Core.StructureClass);
        }
    }

    private void changeStructure(IBranchPolicyDescriptor descriptor) {
        changeStructureClass(descriptor.getId());
    }

    private void changeStructureClass(String newStructureClass) {
        List<ITopic> topics = getTopics();
        if (!topics.isEmpty()) {
            ModifyTopicStructureCommand cmd = new ModifyTopicStructureCommand(
                    topics, newStructureClass);
            cmd.setLabel(CommandMessages.Command_ModifyTopicStructure);
            saveAndRun(cmd);
        }
    }

    private List<ITopic> getTopics() {
        Object[] elements = getSelectedElements();
        ArrayList<ITopic> list = new ArrayList<ITopic>(elements.length);
        for (Object o : elements) {
            if (o instanceof ITopic && !list.contains(o))
                list.add((ITopic) o);
        }
        return list;
    }

    private List<IBranchPolicyDescriptor> getCurrentStructures() {
        List<IBranchPolicyDescriptor> list = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            for (Object o : getSelectedElements()) {
                if (o instanceof ITopic) {
                    IBranchPart branch = MindMapUtils
                            .findBranch(getGraphicalPart(o, viewer));
                    if (branch != null) {
                        List<IBranchPolicyDescriptor> applicables = MindMapUI
                                .getBranchPolicyManager()
                                .getApplicableBranchPolicyDescriptors(branch);
                        if (applicables.isEmpty())
                            return NO_BRANCH_POLICY;
                        if (list == null) {
                            list = new ArrayList<IBranchPolicyDescriptor>(
                                    applicables);
                        } else {
                            list.retainAll(applicables);
                        }
                        if (list.isEmpty())
                            return NO_BRANCH_POLICY;
                    }
                }
            }
        }
        if (list == null || list.isEmpty())
            return NO_BRANCH_POLICY;
        return list;
    }

    private ISelection getCurrentStructure() {
        String branchPolicyId = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            for (Object o : getSelectedElements()) {
                if (o instanceof ITopic) {
                    IBranchPart branch = MindMapUtils
                            .findBranch(getGraphicalPart(o, viewer));
                    if (branch != null) {
                        String thisId = branch.getBranchPolicyId();
                        if (branchPolicyId == null) {
                            branchPolicyId = thisId;
                        } else if (!branchPolicyId.equals(thisId)) {
                            branchPolicyId = null;
                            break;
                        }
                    }
                }
            }
        }
        if (branchPolicyId != null) {
            IBranchPolicyDescriptor descriptor = MindMapUI
                    .getBranchPolicyManager()
                    .getBranchPolicyDescriptor(branchPolicyId);
            if (descriptor != null) {
                return new StructuredSelection(descriptor);
            }
        }
        return StructuredSelection.EMPTY;
    }

    private boolean getCurrentFollowParentStructure() {
        IViewer viewer = getActiveViewer();
        if (viewer == null)
            return false;
        for (Object o : getSelectedElements()) {
            if (!(o instanceof ITopic))
                return false;
            IBranchPart branch = MindMapUtils
                    .findBranch(getGraphicalPart(o, viewer));
            if (branch == null || !isFollowParentStructure(branch))
                return false;
        }
        return true;
    }

    private boolean isFollowParentStructure(IBranchPart branch) {
        return branch.isCentral()
                || branch.getTopic().getStructureClass() == null;
    }

    private void doFollowParentStructure() {
        changeStructureClass(null);
    }

    public void dispose() {
        super.dispose();
        structureViewer = null;
    }

}
