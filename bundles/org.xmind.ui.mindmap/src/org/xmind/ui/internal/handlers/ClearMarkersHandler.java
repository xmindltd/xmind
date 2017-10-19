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
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;

public class ClearMarkersHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        clearMarkers(HandlerUtil.getCurrentSelectionChecked(event),
                MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void clearMarkers(ISelection selection, IEditorPart editor) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        List<Command> commands = new ArrayList<Command>();
        for (Object obj : ((IStructuredSelection) selection).toList()) {
            if (obj instanceof ITopic) {
                for (IMarkerRef mr : ((ITopic) obj).getMarkerRefs()) {
                    commands.add(new DeleteMarkerCommand(mr));
                }
            }
        }
        if (commands.isEmpty())
            return;

        Command command = new CompoundCommand(
                CommandMessages.Command_ClearMarkers, commands);

        ICommandStack commandStack = editor == null ? null
                : editor.getAdapter(ICommandStack.class);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

}
