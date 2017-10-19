package org.xmind.ui.commands;

import org.xmind.core.IComment;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.MindMapMessages;

public class ModifyCommentCommand extends ModifyCommand {

    private IComment comment;

    public ModifyCommentCommand(Object target, IComment comment,
            String newValue) {
        super(target, newValue);
        this.comment = comment;
        super.setLabel(MindMapMessages.ModifyComment_label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.command.ModifyCommand#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object source) {
        return comment.getContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.command.ModifyCommand#setValue(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    protected void setValue(Object source, Object value) {
        comment.setContent((String) value);
    }

}
