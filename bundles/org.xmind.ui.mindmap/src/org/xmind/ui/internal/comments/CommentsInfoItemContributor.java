package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.EditDomain;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.ui.actions.DelegatingAction;
import org.xmind.ui.commands.DeleteCommentCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.AbstractInfoItemContributor;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.TextFormatter;

public class CommentsInfoItemContributor extends AbstractInfoItemContributor {

    private static final String PRESENTATION_VIERWER_CLASS_NAME = "PresentationViewer"; //$NON-NLS-1$

    private static class ShowCommentsAction extends Action {

        private ITopicPart topicPart;

        public ShowCommentsAction(ITopicPart topicPart) {
            super(MindMapMessages.EditComments_text,
                    MindMapUI.getImages().get("menu_modify_comment.png", true)); //$NON-NLS-1$
            setId("org.xmind.ui.editComments"); //$NON-NLS-1$
            this.topicPart = topicPart;
        }

        public void run() {
            if (topicPart == null || topicPart.getSite() == null
                    || topicPart.getSite().getViewer() == null
                    || topicPart.getSite().getViewer().getClass()
                            .getSimpleName()
                            .equals(PRESENTATION_VIERWER_CLASS_NAME))
                return;

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
        Set<IComment> comments = new TreeSet<IComment>(topic.getOwnedWorkbook()
                .getCommentManager().getComments(topic.getId()));
        if (comments.isEmpty())
            return null;

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

        StringBuilder text = new StringBuilder();

        Iterator<IComment> commentIt = comments.iterator();
        while (commentIt.hasNext()) {
            IComment comment = commentIt.next();
            String author = comment.getAuthor();
            String content = comment.getContent();

            long timeMillisString = comment.getTime();

            text.append(author);
            text.append(" : "); //$NON-NLS-1$
            text.append(content);
            text.append('\n');
            text.append(TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.DATE_FORMAT_PATTERN));
            text.append(' ');
            text.append(TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.TIME_FORMAT_PATTERN));
            if (commentIt.hasNext()) {
                text.append("\n\n"); //$NON-NLS-1$
            }

            if (text.length() > 500)
                break;
        }

        if (text.length() > 500) {
            text.delete(501, text.length());
            text.append("...\n..."); //$NON-NLS-1$
        }
        action.setToolTipText(text.toString());

        return action;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        return "platform:/plugin/org.xmind.ui.mindmap/icons/comments.svg"; //$NON-NLS-1$
    }

    @Override
    protected void registerTopicEvent(ITopicPart topicPart, ITopic topic,
            ICoreEventRegister register) {
        register.register(Core.CommentAdd);
        register.register(Core.CommentRemove);

        register.setNextSupport(
                topic.getOwnedWorkbook().getAdapter(ICoreEventSupport.class));
        register.register(Core.CommentContent);
    }

    private void removeComments(ITopicPart topicPart) {
        ITopic topic = topicPart.getTopic();
        Set<IComment> comments = topic.getOwnedWorkbook().getCommentManager()
                .getComments(topic.getId());
        if (comments.isEmpty())
            return;

        List<Command> commands = new ArrayList<Command>(comments.size());
        for (IComment comment : comments) {
            commands.add(new DeleteCommentCommand(topic, comment));
        }
        Command command = new CompoundCommand(
                MindMapMessages.Comment_Delete_label, commands);

        EditDomain domain = topicPart.getSite().getDomain();
        ICommandStack commandStack = domain == null ? null
                : domain.getCommandStack();
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    @Override
    public List<IAction> getPopupMenuActions(final ITopicPart topicPart,
            final ITopic topic) {
        List<IAction> actions = new ArrayList<IAction>();

        IAction editCommentsAction = createAction(topicPart, topic);
        editCommentsAction.setText(MindMapMessages.ModifyMenu);
        editCommentsAction.setImageDescriptor(null);
        actions.add(editCommentsAction);

        IAction deleteCommentsAction = new Action(
                MindMapMessages.Comment_Delete_label) {
            @Override
            public void run() {
                removeComments(topicPart);
            };
        };
        deleteCommentsAction.setId("org.xmind.ui.removeComments"); //$NON-NLS-1$
        deleteCommentsAction.setImageDescriptor(null);
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
