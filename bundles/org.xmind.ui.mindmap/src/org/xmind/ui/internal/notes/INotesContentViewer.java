package org.xmind.ui.internal.notes;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface INotesContentViewer {

    Control createControl(Composite parent);

    Control getControl();

    void setInput(Object input);

    void dispose();

}
