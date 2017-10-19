package org.xmind.ui.mindmap;

import java.io.File;
import java.net.URI;

import org.eclipse.ui.IEditorInput;
import org.xmind.core.IWorkbook;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IEditorInputFactory {

    IEditorInput createDefaultEditorInput();

    IEditorInput createEditorInput(URI uri);

    IEditorInput createEditorInput(URI uri, String name);

    IEditorInput createEditorInput(IWorkbookRef workbookRef);

    IEditorInput createEditorInputForFile(File file);

    IEditorInput createEditorInputForPreLoadedWorkbook(IWorkbook workbook,
            String name);

    IEditorInput createEditorInputForWorkbookInitializer(
            WorkbookInitializer initializer, String name);

}
