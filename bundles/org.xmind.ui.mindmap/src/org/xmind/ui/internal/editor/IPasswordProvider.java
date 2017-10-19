package org.xmind.ui.internal.editor;

import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * This interface is used for retrieving password from the end user.
 * 
 * @author Ren Siu
 * @since 3.6.50
 */
public interface IPasswordProvider {

    /**
     * Prompt the end user to enter the password to open the specified workbook.
     * 
     * @param workbookRef
     *            the {@link IWorkbookRef} that requesting its password
     * @param message
     *            a {@link String} stating the reason of this password request,
     *            or <code>null</code> to show the default message
     * @return a {@link String} of password, or <code>null</code> to indicate
     *         that the user has canceled the whole open operation
     */
    String askForPassword(IWorkbookRef workbookRef, String message);

}
