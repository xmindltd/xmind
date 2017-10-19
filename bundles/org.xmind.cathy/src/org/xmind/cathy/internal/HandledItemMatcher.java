package org.xmind.cathy.internal;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.workbench.Selector;

public class HandledItemMatcher implements Selector {

    private String commandId;

    public HandledItemMatcher(String commandId) {
        this.commandId = commandId;
    }

    public boolean select(MApplicationElement element) {
        if (!(element instanceof MHandledItem))
            return false;

        MHandledItem handledItem = (MHandledItem) element;
        MCommand command = handledItem.getCommand();
        if (command == null)
            return false;

        return this.commandId.equals(command.getElementId());
    }
}