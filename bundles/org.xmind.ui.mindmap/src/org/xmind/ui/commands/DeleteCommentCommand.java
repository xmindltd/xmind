package org.xmind.ui.commands;

import org.xmind.core.IComment;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.MindMapMessages;

public class DeleteCommentCommand extends SourceCommand {

    private IComment comment;

    public DeleteCommentCommand(Object target, IComment comment) {
        super(target);
        this.comment = comment;
        setLabel(MindMapMessages.DeleteComment_label);
    }

    public void redo() {
        comment.getOwnedWorkbook().getCommentManager().removeComment(comment);
        super.redo();
    }

    public void undo() {
        comment.getOwnedWorkbook().getCommentManager().addComment(comment);
        super.undo();
    }

}
