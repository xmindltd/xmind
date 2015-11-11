package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.comment.IComment;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.DeleteCommentAction;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.TextFormatter;

public class CommentsInfoItemContributor extends AbstractInfoItemContributor {

    private static class ShowCommentsAction extends Action {

        private ITopicPart topicPart;

        public ShowCommentsAction(ITopicPart topicPart) {
            super(MindMapMessages.EditComments_text,
                    MindMapUI.getImages().get("menu_modify_comment.png", true)); //$NON-NLS-1$
            setId("org.xmind.ui.editComments"); //$NON-NLS-1$
            this.topicPart = topicPart;
        }

        public void run() {
            if (!topicPart.getStatus().isActive())
                return;

            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            if (window == null)
                return;

            CommentsPopup popup = new CommentsPopup(window, topicPart, false);
            popup.open();
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        List<IComment> comments = topic.getOwnedWorkbook().getCommentManager()
                .getComments(topic);
        if (comments.size() == 0) {
            return null;
        } else if (comments.size() == 1) {
            String content = comments.get(0).getContent();
            if (content == null || content.equals("")) { //$NON-NLS-1$
                return null;
            }
        }

        IAction action = null;
        IActionRegistry actionRegistry = (IActionRegistry) topicPart
                .getAdapter(IActionRegistry.class);
        if (actionRegistry != null) {
            action = actionRegistry.getAction("org.xmind.ui.editComments"); //$NON-NLS-1$
            if (action != null)
                action = new DelegatingAction(action);
        }

        if (action == null)
            action = new ShowCommentsAction(topicPart);

        String text = ""; //$NON-NLS-1$

        for (int i = 0; i < comments.size(); i++) {
            IComment comment = comments.get(i);
            String userName = comment.getAuthor();
            String content = comment.getContent();

            String timeMillisString = comment.getTime();
            String dateString = TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.DATE_FORMAT_PATTERN);
            String timeString = TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.TIME_FORMAT_PATTERN);
            String timeText = dateString + " at " + timeString; //$NON-NLS-1$

            text += userName + " : " + content + "\n" + timeText; //$NON-NLS-1$ //$NON-NLS-2$
            if (i != comments.size() - 1) {
                text += "\n\n"; //$NON-NLS-1$
            }
        }

        if (text.length() > 500)
            text = text.substring(0, 500) + "...\n..."; //$NON-NLS-1$
        action.setToolTipText(text);

        return action;
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.TopicComments);
    }

    public void removeComments(ITopic topic) {
        if (topic == null)
            return;
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null)
            return;

        IGraphicalEditor editor = (IGraphicalEditor) window.getActivePage()
                .getActiveEditor();
        if (editor == null)
            return;

        DeleteCommentAction removeAction = new DeleteCommentAction(editor);
        List<IComment> comments = topic.getOwnedWorkbook().getCommentManager()
                .getComments(topic);

        if (removeAction == null || comments == null)
            return;

        for (IComment comment : comments) {
            removeAction.selectedCommentChanged(comment);
            removeAction.run();
        }
    }

    @Override
    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            final ITopic topic) {
        List<IAction> actions = new ArrayList<IAction>();

        IAction editCommentsAction = createAction(topicPart, topic);
        editCommentsAction.setText(MindMapMessages.ModifyMenu);
        IAction deleteCommentsAction = new Action(
                MindMapMessages.Comment_Delete_label) {
            @Override
            public void run() {
                removeComments(topic);
            };
        };
        deleteCommentsAction.setId("org.xmind.ui.removeComments"); //$NON-NLS-1$
        deleteCommentsAction.setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.DELETE, true));

        actions.add(editCommentsAction);
        actions.add(deleteCommentsAction);
        return actions;
    }

    @Override
    protected void handleTopicEvent(IInfoPart infoPart, CoreEvent event) {
    }

    @Override
    protected void handleTopicEvent(ITopicPart topicPart, CoreEvent event) {
        topicPart.refresh();
    }

}