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
package org.xmind.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.wizards.WizardMessages;

public abstract class AbstractExportWizard extends Wizard implements
        IExportWizard {

    protected class ExportSucceedDialog extends Dialog {

        private IProgressMonitor monitor;

        protected ExportSucceedDialog(Shell parentShell) {
            super(parentShell);
        }

        public ExportSucceedDialog(Shell parentShell, IProgressMonitor monitor) {
            super(parentShell);
            this.monitor = monitor;
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(WizardMessages.ExportPage_OpenFileConfirm_title);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Control area = super.createDialogArea(parent);

            Label message = new Label((Composite) area, SWT.NONE);
            message.setText(WizardMessages.ExportPage_OpenFileConfirm_message);

            return area;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OPEN_ID,
                    WizardMessages.ExportPage_OpenFileConfirm_folder, false);
            createButton(parent, IDialogConstants.OK_ID,
                    WizardMessages.ExportPage_OpenFileConfirm_open, true);
            createButton(parent, IDialogConstants.CANCEL_ID,
                    WizardMessages.ExportPage_OpenFileConfirm_close, false);

            getButton(IDialogConstants.OK_ID).forceFocus();
        }

        @Override
        protected void buttonPressed(int buttonId) {
            super.buttonPressed(buttonId);
            if (IDialogConstants.OPEN_ID == buttonId) {
                openPressed();
            }
        }

        @Override
        protected void okPressed() {
            openFile(getTargetPath(), monitor);
            super.okPressed();
        }

        private void openPressed() {
            showFile(getTargetPath(), monitor);
            close();
        }

    }

    protected static final String KEY_PATH_HISTORY = "PATH_HISTORY"; //$NON-NLS-1$

    protected static final String FILTER_ALL_FILES = "*.*"; //$NON-NLS-1$

    private String targetPath;

    private boolean overwriteWithoutPrompt = false;

    private List<String> pathHistory = new ArrayList<String>();

    private List<String> temporaryPaths = null;

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        loadDialogSettings();
    }

    protected void loadDialogSettings() {
        if (getDialogSettings() != null) {
            loadDialogSettings(getDialogSettings());
        }
    }

    protected void loadDialogSettings(IDialogSettings settings) {
        String history = settings.get(KEY_PATH_HISTORY);
        if (history != null && !"".equals(history)) { //$NON-NLS-1$
            String[] paths = history.split("\\|"); //$NON-NLS-1$
            for (String path : paths) {
                if (!"".equals(path)) //$NON-NLS-1$
                    pathHistory.add(path);
            }
        }

        if (!pathHistory.isEmpty()) {
            String lastPath = pathHistory.get(pathHistory.size() - 1);
            setTargetPath(exchangePath(lastPath, getSuggestedFileName()));
        }
    }

    private String exchangePath(String path, String suggestedFileName) {
        StringBuffer sb = new StringBuffer();
        int lastSeparatorIndex = path.lastIndexOf(File.separatorChar);
        if (lastSeparatorIndex >= 0) {
            sb.append(path, 0, lastSeparatorIndex + 1);
        } else {
            sb.append(File.separator);
        }
        sb.append(suggestedFileName);

        return sb.toString();
    }

    protected abstract String getSuggestedFileName();

    public void dispose() {
        saveDialogSettings();
        super.dispose();
        deleteTemporaryPaths();
    }

    protected void saveDialogSettings() {
        if (getDialogSettings() != null) {
            saveDialogSettings(getDialogSettings());
        }
    }

    protected void saveDialogSettings(IDialogSettings settings) {
        if (targetPath != null) {
            if (!pathHistory.contains(targetPath)) {
                pathHistory.add(targetPath);
            } else {
                pathHistory.remove(targetPath);
                pathHistory.add(targetPath);
            }
            StringBuilder sb = new StringBuilder(pathHistory.size() * 20);
            for (String path : pathHistory) {
                if (sb.length() > 0)
                    sb.append('|');
                sb.append(path);
            }
            settings.put(KEY_PATH_HISTORY, sb.toString());
            targetPath = null;
        }
    }

    public boolean canFinish() {
        return super.canFinish() && hasTargetPath();
    }

    protected void launchTargetFile(boolean fileOrDirectory,
            final IProgressMonitor monitor, Display display,
            final Shell parentShell) {
        display.syncExec(new Runnable() {
            public void run() {
                new ExportSucceedDialog(parentShell, monitor).open();
            }
        });
    }

    protected String requestTemporaryPath(String applicationName,
            String fileNameExtension, boolean fileOrDirectory) {
        String tempDir = Core.getWorkspace().getTempDir();
        File mainDir = FileUtils.ensureDirectory(new File(tempDir, "export")); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder(26
                + (applicationName == null ? 0 : applicationName.length() + 1)
                + (fileNameExtension == null ? 4
                        : fileNameExtension.length() + 1));
        if (applicationName != null) {
            sb.append(applicationName);
            sb.append('_');
        }
        sb.append(Core.getIdFactory().createId());
        if (fileNameExtension == null) {
            sb.append(".tmp"); //$NON-NLS-1$
        } else {
            if (fileNameExtension.charAt(0) != '.')
                sb.append('.');
            sb.append(fileNameExtension);
        }
        String name = sb.toString();
        File file = new File(mainDir, name);
        if (!fileOrDirectory)
            FileUtils.ensureDirectory(file);
        String result = file.getAbsolutePath();
        if (temporaryPaths == null)
            temporaryPaths = new ArrayList<String>(3);
        temporaryPaths.add(result);
        return result;
    }

    protected void deleteTemporaryPath(String path) {
        if (FileUtils.delete(new File(path))) {
            if (temporaryPaths != null) {
                temporaryPaths.remove(path);
                if (temporaryPaths.isEmpty())
                    temporaryPaths = null;
            }
        }
    }

    protected void deleteTemporaryPaths() {
        if (temporaryPaths != null) {
            for (Object o : temporaryPaths.toArray()) {
                deleteTemporaryPath((String) o);
            }
        }
    }

    protected String cleanFileName(String name) {
        if (name == null)
            return ""; //$NON-NLS-1$
        return name.replaceAll("\\r\\n|\\r|\\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void openFile(String path, IProgressMonitor monitor) {
        if (new File(path).exists()) {
            monitor.subTask(WizardMessages.ExportPage_Launching);
            Program.launch(path);
        }
    }

    public void showFile(String path, IProgressMonitor monitor) {
        if (new File(path).exists()) {
            boolean show = org.xmind.ui.viewers.FileUtils.show(new File(path));
            if (!show)
                Program.launch(new File(path).getParent());
        }
    }

    public void setTargetPath(String path) {
        this.targetPath = path;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean hasTargetPath() {
        return this.targetPath != null && !"".equals(this.targetPath); //$NON-NLS-1$
    }

    public List<String> getPathHistory() {
        return pathHistory;
    }

    public void setOverwriteWithoutPrompt(boolean overwriteWithoutPrompt) {
        this.overwriteWithoutPrompt = overwriteWithoutPrompt;
    }

    public boolean isOverwriteWithoutPrompt() {
        return overwriteWithoutPrompt;
    }

}