package org.xmind.ui.internal.editor;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * @author Jason Wong
 */
public class DefaultEditorLayout implements IEditorLayout {

    private Layout layout;

    public DefaultEditorLayout() {
    }

    @Override
    public void activate(Composite parent) {
        layout = new FillLayout();
        parent.setLayout(layout);

        Control[] cs = parent.getChildren();
        if (cs.length > 0) {
            Control editorContainer = cs[0];
            editorContainer.setVisible(true);
            editorContainer.setLayoutData(null);
        }
    }

    @Override
    public void deactivate(Composite parent) {
    }

    @Override
    public Layout getSWTLayout() {
        return layout;
    }

}
