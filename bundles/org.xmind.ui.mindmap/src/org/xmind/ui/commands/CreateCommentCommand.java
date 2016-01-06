package org.xmind.ui.commands;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.comment.IComment;
import org.xmind.core.internal.dom.SheetImpl;
import org.xmind.core.internal.dom.TopicImpl;
import org.xmind.gef.command.Command;

/**
 * The undo and redo function of this command is merged with
 * ModifyCommentCommand, so it's not provided in here.
 * 
 */
public class CreateCommentCommand extends Command {

    private Object target;

    public CreateCommentCommand(Object target) {
        this.target = target;
    }

    public void execute() {
        if (target instanceof ITopic) {
            TopicImpl topic = (TopicImpl) target;
            IWorkbook workbook = topic.getOwnedWorkbook();
            IComment comment = workbook.getCommentManager().createComment();
            workbook.getCommentManager().addComment(comment, topic);
        } else if (target instanceof ISheet) {
            SheetImpl sheet = (SheetImpl) target;
            IWorkbook workbook = sheet.getOwnedWorkbook();
            IComment comment = workbook.getCommentManager().createComment();
            workbook.getCommentManager().addComment(comment, sheet);
        }
        super.execute();
    }

}
