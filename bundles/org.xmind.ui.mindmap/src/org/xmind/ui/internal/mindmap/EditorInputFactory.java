package org.xmind.ui.internal.mindmap;

import java.io.File;
import java.net.URI;

import org.eclipse.ui.IEditorInput;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.editor.CreatedWorkbookRef;
import org.xmind.ui.internal.editor.MindMapEditorInput;
import org.xmind.ui.internal.editor.PreLoadedWorkbookRef;
import org.xmind.ui.internal.editor.URLWorkbookRef;
import org.xmind.ui.mindmap.IEditorInputFactory;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.WorkbookInitializer;

public class EditorInputFactory implements IEditorInputFactory {

    @Override
    public IEditorInput createDefaultEditorInput() {
        return new MindMapEditorInput(
                CreatedWorkbookRef.createFromWorkbookInitializer(
                        WorkbookInitializer.getDefault(), null));
    }

    @Override
    public IEditorInput createEditorInput(URI uri) {
        return new MindMapEditorInput(uri);
    }

    @Override
    public IEditorInput createEditorInput(URI uri, String name) {
        return new MindMapEditorInput(URLWorkbookRef.create(uri, name));
    }

    @Override
    public IEditorInput createEditorInput(IWorkbookRef workbookRef) {
        return new MindMapEditorInput(workbookRef);
    }

    @Override
    public IEditorInput createEditorInputForFile(File file) {
        return new MindMapEditorInput(file.toURI());
    }

    @Override
    public IEditorInput createEditorInputForPreLoadedWorkbook(
            IWorkbook workbook, String name) {
        return new MindMapEditorInput(
                PreLoadedWorkbookRef.createFromLoadedWorkbook(workbook, name));
    }

    @Override
    public IEditorInput createEditorInputForWorkbookInitializer(
            WorkbookInitializer initializer, String name) {
        return new MindMapEditorInput(CreatedWorkbookRef
                .createFromWorkbookInitializer(initializer, name));
    }

}
