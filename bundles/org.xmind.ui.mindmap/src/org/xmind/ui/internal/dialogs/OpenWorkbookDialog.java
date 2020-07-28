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
package org.xmind.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

@Deprecated
public class OpenWorkbookDialog {

    private static final String OPEN_DIALOG_SETTINGS_ID = "org.xmind.ui.openDialog"; //$NON-NLS-1$

    private static final String OLD_FILE_EXT = ".xmap"; //$NON-NLS-1$

    private static final String FILTER_INDEX = "filterIndex"; //$NON-NLS-1$

    private static final String FILTER_PATH = "filterPath"; //$NON-NLS-1$

    private IWorkbenchWindow window;

    private IDialogSettings ds;

    public OpenWorkbookDialog(IWorkbenchWindow window) {
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
    }

    public IEditorPart[] open() {
        if (window == null)
            return null;

        FileDialog fd = new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
        String xmindExt = "*" + MindMapUI.FILE_EXT_XMIND; //$NON-NLS-1$
        String xmtExt = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
        String oldExt = "*" + OLD_FILE_EXT; //$NON-NLS-1$
        String allSupportedFileExt = String.format("%s;%s;%s", //$NON-NLS-1$
                xmindExt, xmtExt, oldExt);
        String allExt = "*.*"; //$NON-NLS-1$
        fd.setFilterExtensions(
                new String[] { xmindExt, oldExt, allSupportedFileExt, allExt });
        fd.setFilterNames(new String[] {
                NLS.bind("{0} ({1})", DialogMessages.WorkbookFilterName, //$NON-NLS-1$
                        xmindExt),
                NLS.bind("{0} ({1})", DialogMessages.OldWorkbookFilterName, //$NON-NLS-1$
                        oldExt),
                NLS.bind("{0} ({1}, {2}, {3})", //$NON-NLS-1$
                        new Object[] {
                                DialogMessages.AllSupportedFilesFilterName,
                                xmindExt, xmtExt, oldExt }),
                NLS.bind("{0} ({1})", DialogMessages.AllFilesFilterName, //$NON-NLS-1$
                        allExt) });
        fd.setFilterIndex(getFilterIndex());
        String path = getFilterPath();
        if (path != null && !"".equals(path)) { //$NON-NLS-1$
            fd.setFilterPath(path);
        }
        if (fd.open() == null)
            return null;

        path = fd.getFilterPath();
        String[] fileNames = fd.getFileNames();
        setFilterIndex(fd.getFilterIndex());
        setFilterPath(path);
        List<IEditorPart> editors = new ArrayList<IEditorPart>(
                fileNames.length);
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(path, fileNames[i]);
            if (file.exists()) {
                IEditorPart editor = open(file);
                if (editor != null) {
                    editors.add(editor);
                }
            }
        }
        if (editors.isEmpty())
            return null;
        return editors.toArray(new IEditorPart[editors.size()]);
    }

    private void setFilterIndex(int index) {
        getDialogSettings().put(FILTER_INDEX, index);
    }

    private int getFilterIndex() {
        try {
            int filterIndex = getDialogSettings().getInt(FILTER_INDEX);
            if (filterIndex >= 0 && filterIndex <= 2)
                return filterIndex;
        } catch (NumberFormatException ignore) {
        }
        return 0;
    }

    private void setFilterPath(String path) {
        getDialogSettings().put(FILTER_PATH, path);
    }

    private String getFilterPath() {
        return getDialogSettings().get(FILTER_PATH);
    }

    private IDialogSettings getDialogSettings() {
        if (ds == null) {
            IDialogSettings global = MindMapUIPlugin.getDefault()
                    .getDialogSettings();
            ds = global.getSection(OPEN_DIALOG_SETTINGS_ID);
            if (ds == null) {
                ds = global.addNewSection(OPEN_DIALOG_SETTINGS_ID);
            }
        }
        return ds;
    }

    protected IEditorPart open(final File file) {
        final IEditorPart[] editor = new IEditorPart[1];
        String errMessage = NLS.bind(
                DialogMessages.FailedToLoadWorkbook_message,
                file.getAbsolutePath());
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                IEditorInput input = MME.createFileEditorInput(file);
                editor[0] = window.getActivePage().openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
        return editor[0];
    }

    public void dispose() {
        window = null;
    }

}
