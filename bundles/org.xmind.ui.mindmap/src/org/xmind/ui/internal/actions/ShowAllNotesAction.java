package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.core.ISheet;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.NotesPart;
import org.xmind.ui.mindmap.MindMapUI;

public class ShowAllNotesAction extends Action {

    private NotesPart notesPart;

    public ShowAllNotesAction(NotesPart notesPart) {
        this.notesPart = notesPart;

        setId("org.xmind.ui.action.showAllNotes"); //$NON-NLS-1$
        setText(MindMapMessages.ShowAllNotes_text);
        setImageDescriptor(
                MindMapUI.getImages().get("menu_window_notes.png", true)); //$NON-NLS-1$
        setToolTipText(MindMapMessages.ShowAllNotes_tooltip);
    }

    public void run() {
        reveal();
    }

    private void reveal() {
        if (notesPart == null) {
            return;
        }
        IGraphicalEditor editor = (IGraphicalEditor) notesPart
                .getContributingPart();
        if (editor == null) {
            return;
        }
        editor.getSite().getPage().activate(editor);

        ISheet sheet = (ISheet) editor.getActivePageInstance()
                .getAdapter(ISheet.class);
        if (sheet != null) {
            ISelectionProvider selectionProvider = editor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.setSelection(new StructuredSelection(sheet));
            }
        }
    }

}
