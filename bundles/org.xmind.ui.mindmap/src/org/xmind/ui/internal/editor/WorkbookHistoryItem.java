package org.xmind.ui.internal.editor;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.xmind.ui.IEditorHistory;
import org.xmind.ui.dialogs.IDialogConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * 
 * @author Frank Shaka
 * @deprecated Use {@link IEditorHistory} instead
 */
public class WorkbookHistoryItem {

    static final String FILE_PROTOCOL = "file:"; //$NON-NLS-1$

    private IEditorInput input;

    private String uri;

    private long time;

    public WorkbookHistoryItem(IEditorInput input, String uri, long time) {
        this.input = input;
        this.uri = uri;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public String getURI() {
        return this.uri;
    }

    public String getPath() {
        if (FilePathParser.isFileURI(uri)) {
            return FilePathParser.toPath(uri);
        }
        return uri;
    }

    void setInput(IEditorInput input) {
        this.input = input;
    }

    public IEditorInput getExistingEditorInput() {
        return this.input;
    }

    public IEditorInput createNewEditorInput() throws CoreException {
        return MME.createFileEditorInput(getPath());
    }

    public void reopen(IWorkbenchPage page) throws CoreException {
        if (!new File(getPath()).exists()) {
            MessageDialog.openWarning(new Shell(),
                    IDialogConstants.COMMON_TITLE,
                    MindMapMessages.WorkbookHistoryItem_FileMissingMessage);
            return;
        }
        page.openEditor(createNewEditorInput(), MindMapUI.MINDMAP_EDITOR_ID,
                true);
    }

    @Override
    public String toString() {
        return "{time=" + time + ",uri=" + uri + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    static String toURI(String filePath) {
        return FilePathParser.toURI(filePath, false);
    }

}
