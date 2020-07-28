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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class DialogUtils {

    private static final String OLD_FILE_EXT = ".xmap"; //$NON-NLS-1$

    private static final String OPEN_DIALOG_SETTINGS_ID = "org.xmind.ui.openDialog"; //$NON-NLS-1$

    private static final String FILTER_INDEX = "filterIndex"; //$NON-NLS-1$

    private static final String FILTER_PATH = "filterPath"; //$NON-NLS-1$

    private DialogUtils() {
    }

    public static void makeDefaultImageSelectorDialog(FileDialog dialog,
            boolean withAllFileFilter) {
        makeImageSelectorDialog(dialog, withAllFileFilter,
                ImageFormat.values());
    }

    public static void makeImageSelectorDialog(FileDialog dialog,
            boolean withAllFileFilter, ImageFormat... imageFormats) {
        Collection<String> extensions = new ArrayList<String>();
        Collection<String> names = new ArrayList<String>();
        if (withAllFileFilter) {
            extensions.add("*.*"); //$NON-NLS-1$
            names.add(NLS.bind("{0} (*.*)", //$NON-NLS-1$
                    DialogMessages.AllFilesFilterName));
        }
        for (ImageFormat format : imageFormats) {
            List<String> exts = format.getExtensions();
            if (!exts.isEmpty()) {
                StringBuilder extBuilder = new StringBuilder(exts.size() * 5);
                StringBuilder extDescBuilder = new StringBuilder(
                        exts.size() * 5);
                for (String ext : exts) {
                    String pattern = "*" + ext; //$NON-NLS-1$
                    if (extBuilder.length() > 0)
                        extBuilder.append(";"); //$NON-NLS-1$
                    extBuilder.append(pattern);
                    if (extDescBuilder.length() > 0)
                        extDescBuilder.append(", "); //$NON-NLS-1$
                    extDescBuilder.append(pattern);
                }
                extensions.add(extBuilder.toString());
                names.add(NLS.bind("{0} ({1})", //$NON-NLS-1$ 
                        format.getDescription(), extDescBuilder.toString()));
            }
        }
        dialog.setFilterExtensions(
                extensions.toArray(new String[extensions.size()]));
        dialog.setFilterNames(names.toArray(new String[names.size()]));
    }

    public static boolean confirmOverwrite(Shell shell, String filePath) {
        return MessageDialog.openConfirm(shell,
                DialogMessages.ConfirmOverwrite_title,
                NLS.bind(DialogMessages.ConfirmOverwrite_message, filePath));
    }

    public static boolean confirmRestart(Shell shell) {
        return new MessageDialog(null, DialogMessages.ConfirmRestart_title,
                null, DialogMessages.ConfirmRestart_message,
                MessageDialog.QUESTION,
                new String[] { DialogMessages.ConfirmRestart_Restart,
                        DialogMessages.ConfirmRestart_Continue },
                1).open() == MessageDialog.OK;
    }

    public static String save(Shell shell, String title, String proposalName,
            String[] filterExtensions, String[] filterNames, int filterIndex,
            String dirPath) {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setOverwrite(false);
        dialog.setText(title == null ? DialogMessages.Save_title : title);
        if (proposalName != null)
            dialog.setFileName(proposalName);
        if (dirPath != null)
            dialog.setFilterPath(dirPath);
        if (filterExtensions != null)
            dialog.setFilterExtensions(filterExtensions);
        if (filterNames != null)
            dialog.setFilterNames(filterNames);
        if (filterIndex >= 0)
            dialog.setFilterIndex(filterIndex);
        return save(shell, dialog);
    }

    public static String save(Shell shell, String proposalName,
            String[] filterExtensions, String[] filterNames, int filterIndex,
            String dirPath) {
        return save(shell, null, proposalName, filterExtensions, filterNames,
                filterIndex, dirPath);
    }

    public static String save(Shell shell, FileDialog dialog) {
        String fileName = dialog.open();
        if (fileName != null) {
            int filterIndex = dialog.getFilterIndex();
            if (filterIndex >= 0) {
                String extension = dialog.getFilterExtensions()[filterIndex];
                fileName = adaptFileName(fileName, extension);
                if (new File(fileName).exists()
                        && !DialogUtils.confirmOverwrite(shell, fileName))
                    return save(shell, dialog);
            }
        }
        return fileName;
    }

    private static String adaptFileName(String fileName, String extension) {
        if (extension != null && !"".equals(extension)) { //$NON-NLS-1$
            String defaultExt = null;
            for (String ext : extension.split(";")) { //$NON-NLS-1$
                ext = ext.trim();
                if (ext.startsWith("*")) //$NON-NLS-1$
                    ext = ext.substring(1);
                if (defaultExt == null)
                    defaultExt = ext;
                if (fileName.endsWith(ext))
                    return fileName;
            }
            if (defaultExt != null)
                return fileName + defaultExt;
        }
        return fileName;
    }

    /**
     * 
     * @param parentShell
     * @param style
     *            SWT.NONE, SWT.MULTI, SWT.SHEET
     * @return
     */
    public static List<File> openXMindFiles(Shell parentShell, int style) {
        FileDialog dialog = new FileDialog(parentShell, SWT.OPEN | style);

        String xmindExt = "*" + MindMapUI.FILE_EXT_XMIND; //$NON-NLS-1$
        String xmtExt = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
        String oldExt = "*" + OLD_FILE_EXT; //$NON-NLS-1$
        String allSupportedFileExt = String.format("%s;%s;%s", //$NON-NLS-1$
                xmindExt, xmtExt, oldExt);
        String allExt = "*.*"; //$NON-NLS-1$

        List<String> filterExtensions = new ArrayList<String>(
                Arrays.asList(dialog.getFilterExtensions()));
        filterExtensions.add(xmindExt);
        filterExtensions.add(oldExt);
        filterExtensions.add(allSupportedFileExt);
        filterExtensions.add(allExt);
        dialog.setFilterExtensions(
                filterExtensions.toArray(new String[filterExtensions.size()]));

        List<String> filterNames = new ArrayList<String>(
                Arrays.asList(dialog.getFilterNames()));
        filterNames.add(NLS.bind("{0} ({1})", //$NON-NLS-1$
                DialogMessages.WorkbookFilterName, xmindExt));
        filterNames.add(NLS.bind("{0} ({1})", //$NON-NLS-1$
                DialogMessages.OldWorkbookFilterName, oldExt));
        filterNames
                .add(NLS.bind("{0} ({1}, {2}, {3})", //$NON-NLS-1$
                        new Object[] {
                                DialogMessages.AllSupportedFilesFilterName,
                                xmindExt, xmtExt, oldExt }));
        filterNames.add(NLS.bind("{0} ({1})", //$NON-NLS-1$
                DialogMessages.AllFilesFilterName, allExt));
        dialog.setFilterNames(
                filterNames.toArray(new String[filterNames.size()]));

        IDialogSettings globalSettings = MindMapUIPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings settings = globalSettings
                .getSection(OPEN_DIALOG_SETTINGS_ID);
        if (settings == null) {
            settings = globalSettings.addNewSection(OPEN_DIALOG_SETTINGS_ID);
        }

        int filterIndex = 0;
        try {
            filterIndex = settings.getInt(FILTER_INDEX);
            if (filterIndex < 0 || filterIndex > 2)
                filterIndex = 0;
        } catch (NumberFormatException ignore) {
        }
        dialog.setFilterIndex(filterIndex);

        String filterPath = settings.get(FILTER_PATH);
        if (filterPath != null && !"".equals(filterPath)) { //$NON-NLS-1$
            dialog.setFilterPath(filterPath);
        }

        String selection = dialog.open();
        if (selection == null)
            return Collections.emptyList();

        filterIndex = dialog.getFilterIndex();
        settings.put(FILTER_INDEX, filterIndex);

        filterPath = dialog.getFilterPath();
        settings.put(FILTER_PATH, filterPath);

        String[] fileNames = dialog.getFileNames();
        List<File> files = new ArrayList<File>(fileNames.length);
        for (int i = 0; i < fileNames.length; i++) {
            files.add(new File(filterPath, fileNames[i]));
        }
        return files;
    }

}