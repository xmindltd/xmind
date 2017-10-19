package org.xmind.ui.internal.editor;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;

/**
 * 
 * @author Frank Shaka
 * @since 3.0
 * @deprecated
 */
@Deprecated
public interface IWorkbookReferrer {

//    void setSelection(ISelection selection, boolean reveal, boolean forceFocus);

    /**
     * @deprecated
     */
    @Deprecated
    void savePreivew(IWorkbook workbook, IProgressMonitor monitor)
            throws IOException, CoreException;

    /**
     * @deprecated
     */
    @Deprecated
    void postSave(IProgressMonitor monitor);

    /**
     * @deprecated
     */
    @Deprecated
    void postSaveAs(Object newKey, IProgressMonitor monitor);

}
