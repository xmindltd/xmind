package org.xmind.ui.commands;

import org.xmind.core.comment.IComment;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.MindMapMessages;

public class ModifyCommentCommand extends SourceCommand {

    private IComment comment;

    private String newValue;

    private String oldValue;

    private int index = -1;

    public ModifyCommentCommand(IComment comment, String newValue) {
        super(comment.getTarget());
        this.comment = comment;
        this.oldValue = comment.getContent();
        this.newValue = newValue;
        super.setLabel(MindMapMessages.ModifyComment_label);
    }

    @Override
    public void redo() {
        if (newValue == null || newValue.equals("")) { //$NON-NLS-1$
            index = comment.getOwnedWorkbook().getCommentManager()
                    .getAllComments().indexOf(comment);
            comment.getOwnedWorkbook().getCommentManager()
                    .removeComment(comment);
        } else if (index != -1 && (oldValue == null || oldValue.equals(""))) { //$NON-NLS-1$
            comment.getOwnedWorkbook().getCommentManager().addComment(comment,
                    index);
        } else {
            comment.setContent(newValue);
        }
        super.redo();
    }

    @Override
    public void undo() {
        if (newValue == null || newValue.equals("")) { //$NON-NLS-1$
            comment.getOwnedWorkbook().getCommentManager().addComment(comment,
                    index);
        } else if (oldValue == null || oldValue.equals("")) { //$NON-NLS-1$
            index = comment.getOwnedWorkbook().getCommentManager()
                    .getAllComments().indexOf(comment);
            comment.getOwnedWorkbook().getCommentManager()
                    .removeComment(comment);
        } else {
            comment.setContent(oldValue);
        }
        super.undo();
    }

}
