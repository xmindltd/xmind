package org.xmind.ui.internal.actions;

import org.xmind.core.comment.IComment;
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
        DeleteCommentCommand cmd = new DeleteCommentCommand(comment);
        String content = comment.getContent();
        if (content == null || content.equals("")) { //$NON-NLS-1$
            cmd.execute();
        } else {
            ICommandStack cs = getCommandStack();
            cs.execute(cmd);
        }
    }

    @Override
    public void selectedCommentChanged(IComment comment) {
        if (comment != null) {
            this.comment = comment;
        }
        setEnabled(comment != null);
    }

}
