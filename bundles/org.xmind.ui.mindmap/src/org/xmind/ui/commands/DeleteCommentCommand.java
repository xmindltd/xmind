package org.xmind.ui.commands;

import org.xmind.core.comment.IComment;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.MindMapMessages;

public class DeleteCommentCommand extends SourceCommand {

    private IComment comment;

    private int index = -1;

    public DeleteCommentCommand(IComment comment) {
        super(comment.getTarget());
        this.comment = comment;

        setLabel(MindMapMessages.DeleteComment_label);
    }

    public void redo() {
        index = comment.getOwnedWorkbook().getCommentManager().getAllComments()
                .indexOf(comment);
        comment.getOwnedWorkbook().getCommentManager().removeComment(comment);
        super.redo();
    }

    public void undo() {
        comment.getOwnedWorkbook().getCommentManager().addComment(comment,
                index);
        super.undo();
    }

}
