package org.xmind.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.style.IStyled;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.internal.MindMapUIPlugin;

public class ResetStyleHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        resetStyle(HandlerUtil.getCurrentSelection(event),
                MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void resetStyle(ISelection selection, IEditorPart editor) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        List<Command> commands = new ArrayList<Command>();
        for (Object obj : ((IStructuredSelection) selection).toList()) {
            if (obj instanceof IStyled) {
                commands.add(
                        new ModifyStyleCommand((IStyled) obj, (String) null));
            }
        }
        if (commands.isEmpty())
            return;

        Command command = new CompoundCommand(
                CommandMessages.Command_ModifyStyle, commands);

        ICommandStack commandStack = editor == null ? null
                : MindMapUIPlugin.getAdapter(editor, ICommandStack.class);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

}
