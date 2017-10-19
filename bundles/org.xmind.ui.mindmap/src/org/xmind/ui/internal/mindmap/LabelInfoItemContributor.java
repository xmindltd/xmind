package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.CommandStack;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.ModifyLabelCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class LabelInfoItemContributor extends AbstractInfoItemContributor {

    private static class EditLabelAction extends Action {
        private IGraphicalViewer viewer;

        public EditLabelAction(IGraphicalViewer viewer) {
            setId(MindMapActionFactory.EDIT_LABEL.getId());
            setText(MindMapMessages.EditLabel_text);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.LABEL, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.LABEL, false));
            setActionDefinitionId("org.xmind.ui.command.editLabel"); //$NON-NLS-1$
            this.viewer = viewer;
        }

        @Override
        public void run() {
            if (viewer == null)
                return;
            EditDomain editDomain = viewer.getEditDomain();
            if (editDomain == null)
                return;
            editDomain.handleRequest(MindMapUI.REQ_EDIT_LABEL, viewer);
        }

    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        if (getContent(topic) == null)
            return null;

        if (!isLabelVisible(topicPart))
            return null;

        IAction action = null;
        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry != null) {
            action = actionRegistry
                    .getAction(MindMapActionFactory.EDIT_LABEL.getId());
            if (action != null)
                action = new DelegatingAction(action);
        }

        if (action == null || action.getImageDescriptor() == null) {
            IViewer viewer = topicPart.getSite().getViewer();
            if (viewer != null && viewer instanceof IGraphicalViewer)
                action = new EditLabelAction((IGraphicalViewer) viewer);
        }

        if (action != null)
            action.setToolTipText(getContent(topic));

        action.setEnabled(true);

        return action;
    }

    private boolean isLabelVisible(ITopicPart part) {
        IBranchPart branch = MindMapUtils.findBranch(part);
        if (branch != null) {
            IBranchPart parent = branch.getParentBranch();
            if (parent != null) {
                IStyleSelector ss = StyleUtils.getStyleSelector(parent);
                String value = ss.getStyleValue(parent,
                        Styles.HideChildrenLabels);
                if (Boolean.TRUE.toString().equals(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getContent(ITopic topic) {
        Set<String> labels = topic.getLabels();
        if (labels.isEmpty())
            return null;
        return MindMapUtils.getLabelText(labels);
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.mindmap/icons/label.svg"; //$NON-NLS-1$
    }

    @Override
    public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
        return !isIconTipOnly(topicPart);
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.Labels);
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
        infoPart.refresh();
        infoPart.getTopicPart().refresh();
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        topicPart.refresh();
    }

    public void removeLabel(ITopic topic) {
        IGraphicalEditor editor = getEditor();
        if (editor == null)
            return;

        ModifyLabelCommand command = new ModifyLabelCommand(topic,
                new HashSet<String>());
        CommandStack cs = (CommandStack) editor.getCommandStack();
        if (cs != null) {
            cs.execute(command);
        }

    }

    private IGraphicalEditor getEditor() {

        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return null;

        IGraphicalEditor editor = (IGraphicalEditor) window.getActivePage()
                .getActiveEditor();
        return editor;
    }

    @Override
    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            final ITopic topic) {

        IViewer viewer = topicPart.getSite().getViewer();
        if (viewer == null)
            return Collections.emptyList();
        IAction modifyLabelAction = new EditLabelAction(
                (IGraphicalViewer) viewer);

        modifyLabelAction.setText(MindMapMessages.ModifyMenu);
        modifyLabelAction.setImageDescriptor(null);

        IAction deleteLabelAction = new Action(
                MindMapMessages.InfoItem_Delete_text) {
            @Override
            public void run() {
                removeLabel(topic);
            }
        };
        deleteLabelAction.setId("org.xmind.ui.removeLabel"); //$NON-NLS-1$
        deleteLabelAction.setImageDescriptor(null);

        List<IAction> actions = new ArrayList<IAction>();
        actions.add(modifyLabelAction);
        actions.add(deleteLabelAction);
        return actions;
    }

}
