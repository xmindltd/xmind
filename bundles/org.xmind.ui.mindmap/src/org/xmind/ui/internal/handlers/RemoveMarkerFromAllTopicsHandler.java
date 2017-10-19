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
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteMarkerCommand;

public class RemoveMarkerFromAllTopicsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        removeMarkerFromSheet(HandlerUtil.getCurrentSelectionChecked(event),
                MindMapHandlerUtil.findContributingEditor(event));
        return null;
    }

    private void removeMarkerFromSheet(ISelection selection,
            IEditorPart editor) {
        if (!(selection instanceof IStructuredSelection))
            return;

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj == null || !(obj instanceof IMarkerRef))
            return;

        IMarkerRef markerRef = (IMarkerRef) obj;
        String markerId = markerRef.getMarkerId();
        if (markerId == null)
            return;

        ISheet sheet = markerRef.getOwnedSheet();
        if (sheet == null)
            return;

        List<Command> commands = new ArrayList<Command>();
        collectRemoveMarkerCommands(markerId, sheet.getRootTopic(), commands);
        if (commands.isEmpty())
            return;

        Command command = new CompoundCommand(
                CommandMessages.Command_RemoveMarkerFromAllTopics, commands);

        ICommandStack commandStack = editor == null ? null
                : editor.getAdapter(ICommandStack.class);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    private void collectRemoveMarkerCommands(String markerId, ITopic topic,
            List<Command> commands) {
        for (IMarkerRef mr : topic.getMarkerRefs()) {
            if (markerId.equals(mr.getMarkerId())) {
                commands.add(new DeleteMarkerCommand(mr));
            }
        }

        Iterator<ITopic> childrenIt = topic.getAllChildrenIterator();
        while (childrenIt.hasNext()) {
            ITopic child = childrenIt.next();
            collectRemoveMarkerCommands(markerId, child, commands);
        }
    }

}
