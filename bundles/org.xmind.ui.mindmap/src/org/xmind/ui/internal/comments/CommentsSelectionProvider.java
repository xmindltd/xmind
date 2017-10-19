package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class CommentsSelectionProvider implements ISelectionProvider {

    private List<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

    private ISelection selection;

    //it to be used in commentsView -- findReplaceAction.
    private ISelection oldSelection;

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        if (this.selection == selection
                || (this.selection != null && this.selection.equals(selection)))
            return;

        this.oldSelection = this.selection;
        this.selection = selection;
        fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
    }

    public ISelection getOldSelection() {
        return oldSelection == null ? StructuredSelection.EMPTY : oldSelection;
    }

    public ISelection getSelection() {
        return selection == null ? StructuredSelection.EMPTY : selection;
    }

    private void fireSelectionChanged(SelectionChangedEvent event) {
        for (Object o : selectionChangedListeners.toArray()) {
            ((ISelectionChangedListener) o).selectionChanged(event);
        }
    }

}
