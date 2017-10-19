package org.xmind.ui.commands;

import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.ITopic;
import org.xmind.gef.command.SourceCommand;
import org.xmind.ui.internal.MindMapMessages;

public class DeleteNotesCommand extends SourceCommand {

    private ITopic topic;

    private INotesContent htmlContent;

    private INotesContent plainContent;

    public DeleteNotesCommand(ITopic topic) {
        super(topic);
        this.topic = topic;
        setLabel(MindMapMessages.DeleteNotes_label);
    }

    public void redo() {
        if (topic == null) {
            return;
        }
        INotes notes = topic.getNotes();
        if (htmlContent == null && plainContent == null) {
            htmlContent = notes.getContent(INotes.HTML);
            plainContent = notes.getContent(INotes.PLAIN);
        }
        notes.setContent(INotes.HTML, null);
        notes.setContent(INotes.PLAIN, null);

        super.redo();
    }

    public void undo() {
        INotes notes = topic.getNotes();
        notes.setContent(INotes.PLAIN, plainContent);
        notes.setContent(INotes.HTML, htmlContent);

        super.undo();
    }

}
