package org.xmind.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IRevision;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.DeleteRevisionCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;

public class DeleteRevisionHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        deleteRevisions(HandlerUtil.getCurrentSelection(event),
                HandlerUtil.getActiveEditor(event));
        return null;
    }

    private static void deleteRevisions(ISelection selection,
            IEditorPart editor) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        List<Command> commands = new ArrayList<Command>(
                ((IStructuredSelection) selection).size());
        Iterator iterator = ((IStructuredSelection) selection).iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof IRevision) {
                commands.add(new DeleteRevisionCommand((IRevision) obj));
            }
        }
        if (commands.isEmpty())
            return;

        String label;
        if (commands.size() > 1) {
            label = MindMapMessages.DeleteMultipleRevisionsCommand_label;
        } else {
            label = MindMapMessages.DeleteSingleRevisionCommand_label;
        }
        Command command = new CompoundCommand(label, commands);
        ICommandStack commandStack = editor == null ? null
                : MindMapUIPlugin.getAdapter(editor, ICommandStack.class);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

}
