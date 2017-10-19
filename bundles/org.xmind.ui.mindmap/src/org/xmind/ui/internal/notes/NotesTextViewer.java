package org.xmind.ui.internal.notes;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

public class NotesTextViewer extends TextViewer implements ISelection {

    public NotesTextViewer(Composite parent, int styles) {
        super(parent, styles);
    }

    protected int getEmptySelectionChangedEventDelay() {
        return 100;
    }

    public boolean isEmpty() {
        return false;
    }
}
