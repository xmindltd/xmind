package org.xmind.ui.internal.e4handlers;

import java.io.File;
import java.net.URI;

import javax.inject.Named;

import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.LocalFileWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;
import org.xmind.ui.mindmap.MindMapUI;

public class OpenLocalFileHandler {

    private static final String KEY_COMMAND_PARAMTER_URI = "org.xmind.ui.mindmap.commandparameter.openLocalFile.uri"; //$NON-NLS-1$

    @Execute
    public void run(final IWorkbenchWindow window,
            final @Named(KEY_COMMAND_PARAMTER_URI) String uri) {
        if (uri == null || "".equals(uri)) //$NON-NLS-1$
            return;

        @SuppressWarnings("restriction")
        IWorkbookRefFactory factory = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory();
        IWorkbookRef workbookRef = factory.createWorkbookRef(URI.create(uri),
                null);
        Assert.isTrue(workbookRef instanceof LocalFileWorkbookRef);

        LocalFileWorkbookRef localWorkbookRef = (LocalFileWorkbookRef) workbookRef;
        String filePath = localWorkbookRef.getURI().getPath();
        if (!new File(filePath).exists()) {
            showMessageDialog(
                    MindMapMessages.WorkbookHistoryItem_FileMissingMessage);
            return;
        }
        IWorkbenchPage page = window.getActivePage();
        Assert.isTrue(page != null);

        final IEditorInput editorInput = MindMapUI.getEditorInputFactory()
                .createEditorInput(workbookRef);
        Assert.isTrue(editorInput != null);
        try {
            page.openEditor(editorInput, MindMapUI.MINDMAP_EDITOR_ID);
        } catch (PartInitException e) {
            MindMapUIPlugin.log(e, this.getClass().getName() + "--> openEdior"); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    private void showMessageDialog(String message) {
        String[] buttonLabels = new String[] { IDialogConstants.CLOSE_LABEL };

        MessageDialog dialog = new MessageDialog(null,
                MindMapMessages.OpenLocalFileHandler_MessageDialog_title, null,
                message, MessageDialog.WARNING, buttonLabels, 0);
        dialog.open();
    }
}
