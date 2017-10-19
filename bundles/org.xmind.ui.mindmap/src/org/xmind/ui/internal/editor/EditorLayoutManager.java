package org.xmind.ui.internal.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Jason Wong
 */
public class EditorLayoutManager implements IEditorLayoutManager {

    private Composite pageContainer;

    private IEditorLayout currentEditorLayout;

    public EditorLayoutManager(Composite pageContainer) {
        this.pageContainer = pageContainer;
    }

    @Override
    public void setActiveLayout(IEditorLayout editorLayout) {
        pageContainer.setRedraw(false);
        if (currentEditorLayout != null)
            currentEditorLayout.deactivate(pageContainer);

        currentEditorLayout = editorLayout;
        currentEditorLayout.activate(pageContainer);

        pageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        pageContainer.layout();
        pageContainer.setRedraw(true);
    }

    @Override
    public IEditorLayout getActiveLayout() {
        return currentEditorLayout;
    }

    @Override
    public void restoreDefault() {
        setActiveLayout(new DefaultEditorLayout());
    }

}
