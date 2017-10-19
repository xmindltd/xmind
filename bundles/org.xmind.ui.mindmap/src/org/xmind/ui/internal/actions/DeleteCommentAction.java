package org.xmind.ui.internal.actions;

import org.xmind.core.IComment;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.commands.DeleteCommentCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.comments.CommentAction;
import org.xmind.ui.mindmap.MindMapUI;

public class DeleteCommentAction extends CommentAction {

    private IComment comment;

    public DeleteCommentAction(IGraphicalEditor editor) {
        super(editor);

        setId("org.xmind.ui.action.deleteComment"); //$NON-NLS-1$
        setText(MindMapMessages.DeleteComment_text);
        setImageDescriptor(
                MindMapUI.getImages().get("delete-comment.png", true)); //$NON-NLS-1$
        setDisabledImageDescriptor(
                MindMapUI.getImages().get("delete-comment.png", false)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.DeleteComment_tooltip);
    }

    public void run() {
        if (comment == null)
            return;

        Object target = comment.getOwnedWorkbook()
                .getElementById(comment.getObjectId());
        if (target == null)
            return;

        DeleteCommentCommand cmd = new DeleteCommentCommand(target, comment);
        ICommandStack commandStack = getCommandStack();
        if (commandStack != null) {
            commandStack.execute(cmd);
        } else {
            cmd.execute();
        }
    }

    @Override
    public void selectedCommentChanged(IComment comment) {
        this.comment = comment;
        setEnabled(comment != null);
    }

}
