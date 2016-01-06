package org.xmind.ui.commands;

import org.xmind.core.IIdentifiable;
import org.xmind.core.comment.IComment;
import org.xmind.gef.command.Command;

public class AddCommentCommand extends Command {

    private IComment comment;

    private int index = -1;

    public AddCommentCommand(IIdentifiable target, IComment comment) {
        comment.setTarget(target);
        this.comment = comment;
    }

    public void redo() {
        index = comment.getOwnedWorkbook().getCommentManager().getAllComments()
                .indexOf(comment);
        comment.getOwnedWorkbook().getCommentManager().addComment(comment,
                index);
        super.undo();
    }

    public void undo() {
        comment.getOwnedWorkbook().getCommentManager().removeComment(comment);
        super.redo();
    }

}
