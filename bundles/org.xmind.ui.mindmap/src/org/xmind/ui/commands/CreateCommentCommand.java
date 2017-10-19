package org.xmind.ui.commands;

import org.xmind.gef.command.Command;

/**
 * The undo and redo function of this command is merged with
 * ModifyCommentCommand, so it's not provided in here.
 * 
 * @deprecated
 */
@Deprecated
public class CreateCommentCommand extends Command {

    public CreateCommentCommand(Object target) {
    }

    public void execute() {
        super.execute();
    }

}
