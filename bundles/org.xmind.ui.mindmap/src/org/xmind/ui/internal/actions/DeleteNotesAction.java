package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ITopic;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.ui.commands.DeleteNotesCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.notes.SheetNotesViewer;
import org.xmind.ui.mindmap.MindMapUI;

public class DeleteNotesAction extends Action implements ISelectionAction {

    private SheetNotesViewer viewer;

    private ITopic topic;

    public DeleteNotesAction(SheetNotesViewer viewer) {
        this.viewer = viewer;

        setId("org.xmind.ui.action.deleteNotes"); //$NON-NLS-1$
        setText(MindMapMessages.DeleteNotes_text);
        setImageDescriptor(MindMapUI.getImages().get("del_notes.gif", true)); //$NON-NLS-1$
        setDisabledImageDescriptor(
                MindMapUI.getImages().get("del_notes.gif", false)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.DeleteNotes_tooltip);
    }

    public void run() {
        if (topic == null || viewer == null) {
            return;
        }
        DeleteNotesCommand cmd = new DeleteNotesCommand(topic);
        ICommandStack cs = viewer.getEditor().getCommandStack();
        cs.execute(cmd);
    }

    public void setSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof ITopic) {
                this.topic = (ITopic) obj;
                setEnabled(true);
                return;
            }
        }
        this.topic = null;
        setEnabled(false);
    }

}
