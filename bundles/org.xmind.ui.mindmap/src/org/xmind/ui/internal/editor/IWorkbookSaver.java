package org.xmind.ui.internal.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.IWorkbook;

/**
 * 
 * @author Frank Shaka
 * @deprecated
 */
@Deprecated
public interface IWorkbookSaver {

    /**
     * Determines whether the target exists and the save operation will
     * overwrite it.
     * 
     * @deprecated
     * @return <code>true</code> if the target exists and the save operationg
     *         will overwrite it, or <code>false</code> otherwise
     */
    @Deprecated
    boolean willOverwriteTarget();

    /**
     * Save the workbook.
     * 
     * @deprecated
     * @param monitor
     * @param workbook
     * @throws CoreException
     */
    @Deprecated
    void save(IProgressMonitor monitor, IWorkbook workbook)
            throws CoreException;

}
