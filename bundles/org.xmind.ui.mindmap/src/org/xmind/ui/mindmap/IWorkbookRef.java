/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.mindmap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.xmind.core.IWorkbook;
import org.xmind.gef.ui.editor.IEditable;
import org.xmind.ui.internal.editor.MindMapPreviewOptions;

/**
 * This interface represents an editable document whose content is a workbook.
 *
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IWorkbookRef extends IEditable {

    String PROP_WORKBOOK = "workbook"; //$NON-NLS-1$

    /**
     * Returns the workbook loaded by {@link #open(IProgressMonitor)}.
     * 
     * @return the loaded workbook {@link IWorkbook}, or <code>null</code> if
     *         none (e.g. when not loaded yet or already closed)
     */
    IWorkbook getWorkbook();

    /**
     * Returns the id of the save wizard who creates this workbook ref,
     * typically used to show the default save option when saving this workbook
     * ref as another name, or <code>null</code> indicating no default option
     * should be considered.
     *
     * @return a string identifying the creator wizard or <code>null</code>
     */
    String getSaveWizardId();

    /**
     * Opens an input stream to read preview image data for a specific sheet. If
     * the desired preview image is not available, <code>null</code> is returned
     * (instead of throwing FileNotFoundException). This method is typically
     * used when exporting contents and previews to another workbook ref.
     *
     * @param sheetId
     * @param options
     * @return {@link InputStream} for the preview image data for a sheet, or
     *         <code>null</code> if not available
     * @throws IOException
     * @throws IllegalArgumentException
     *             if <code>sheetId</code> is null
     */
    InputStream getPreviewImageData(String sheetId,
            MindMapPreviewOptions options) throws IOException;

    /**
     *
     * @return
     */
    boolean canImportFrom(IWorkbookRef source);

    /**
     *
     * @param source
     * @param callback
     */
    void importFrom(IProgressMonitor monitor, IWorkbookRef source)
            throws InterruptedException, InvocationTargetException;

    void addWorkbookRefListener(IWorkbookRefListener workbookRefListener);

    void removeWorkbookRefListener(IWorkbookRefListener workbookRefListener);

    boolean activateNotifier();

}
