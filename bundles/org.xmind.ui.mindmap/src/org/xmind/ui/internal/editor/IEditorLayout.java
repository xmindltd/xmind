package org.xmind.ui.internal.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * @author Jason Wong
 */
public interface IEditorLayout {

    void activate(Composite parent);

    void deactivate(Composite parent);

    Layout getSWTLayout();

}
