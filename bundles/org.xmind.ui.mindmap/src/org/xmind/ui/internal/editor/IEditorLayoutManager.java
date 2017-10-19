package org.xmind.ui.internal.editor;

/**
 * @author Jason Wong
 */
public interface IEditorLayoutManager {

    void setActiveLayout(IEditorLayout editorLayout);

    IEditorLayout getActiveLayout();

    void restoreDefault();

}
