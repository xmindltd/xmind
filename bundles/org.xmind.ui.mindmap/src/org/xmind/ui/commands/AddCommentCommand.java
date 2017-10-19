package org.xmind.ui.commands;

import org.xmind.core.IComment;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.SourceCommand;

public class AddCommentCommand extends SourceCommand {

    private String author;

    private long time;

    private String objectId;

    private String content;

    private IWorkbook workbook;

    private IComment comment;

    public AddCommentCommand(String author, long time, String objectId,
            String content, IWorkbook workbook) {
        super(workbook.getElementById(objectId));

        this.author = author;
        this.time = time;
        this.objectId = objectId;
        this.content = content;
        this.workbook = workbook;
    }

    public AddCommentCommand(String author, long time, String objectId,
            String content, IWorkbook workbook, IComment comment) {
        this(author, time, objectId, content, workbook);
        this.comment = comment;
    }

    public void redo() {
        if (comment == null) {
            comment = workbook.getCommentManager().createComment(author, time,
                    objectId);
        }
        if (!content.equals(comment.getContent())) {
            comment.setContent(content);
        }

        workbook.getCommentManager().addComment(comment);
        super.redo();
    }

    public void undo() {
        workbook.getCommentManager().removeComment(comment);
        super.undo();
    }

}
