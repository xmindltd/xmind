/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.signin.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.internal.XMindNetRequest;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.FileUtils;

public class XMindUpdater {

    public static final String SKIPPABLE_YES = "yes"; //$NON-NLS-1$
    public static final String SKIPPABLE_NO = "no"; //$NON-NLS-1$

    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    private static final IStatus OK = new Status(IStatus.OK, PLUGIN_ID,
            Status.OK_STATUS.getMessage());

    private static final IStatus CANCELED = new Status(IStatus.CANCEL,
            PLUGIN_ID, Status.CANCEL_STATUS.getMessage());

    private static final int DOWNLOAD_ID = IDialogConstants.CLIENT_ID + 1;

    private static final int SKIP_ID = IDialogConstants.CLIENT_ID + 2;

    private static final int REMIND_ME_ID = IDialogConstants.CLIENT_ID + 3;

    private static interface IRunnable {
        IStatus run(IProgressMonitor monitor) throws Exception;
    }

    private class NewUpdateDialog extends Dialog {

        private String whatsNew;

        private boolean hasWhatsNew;

        public NewUpdateDialog(Shell parentShell) {
            super(parentShell);
            this.whatsNew = data.getWhatsNew();
            this.hasWhatsNew = whatsNew != null && !"".equals(whatsNew); //$NON-NLS-1$
        }

        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Messages.XMindUpdater_NewDialog_Title);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createIconAndMessages(composite);
            return composite;
        }

        private void createIconAndMessages(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            composite.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 20;
            composite.setLayout(gridLayout);

            createIcon(composite);
            createMessages(composite);
        }

        private void createIcon(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            ImageDescriptor imgDesc = AbstractUIPlugin
                    .imageDescriptorFromPlugin("org.xmind.cathy", //$NON-NLS-1$
                            "icons/xmind.48.png"); //$NON-NLS-1$
            if (imgDesc != null) {
                final Image img = imgDesc.createImage();
                label.setImage(img);
                label.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        img.dispose();
                    }
                });
            } else {
                label.setImage(parent.getDisplay()
                        .getSystemImage(SWT.ICON_INFORMATION));
            }
        }

        private void createMessages(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = 400;
            gridData.heightHint = hasWhatsNew ? 200 : SWT.DEFAULT;
            composite.setLayoutData(gridData);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 10;
            gridLayout.horizontalSpacing = 0;
            composite.setLayout(gridLayout);

            createMessageArea(composite);
            createSpecArea(composite);
            createWhatsNewArea(composite);
        }

        private void createMessageArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(
                    new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            label.setText(Messages.XMindUpdater_Dialog_NewVersionAvailable);
            label.setFont(
                    FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT, 1));
        }

        private void createSpecArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(
                    new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            label.setText(
                    NLS.bind(Messages.XMindUpdater_NewDialog_NewVersionLabel,
                            data.getBuildId()));
        }

        private void createWhatsNewArea(Composite parent) {
            if (!hasWhatsNew)
                return;
            Text text = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER
                    | SWT.V_SCROLL | SWT.H_SCROLL);
            text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            text.setText(whatsNew);
            text.setEditable(false);
        }

        @Override
        protected Control createButtonBar(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2; // this is incremented by createButton
            layout.makeColumnsEqualWidth = false;
            layout.marginWidth = convertHorizontalDLUsToPixels(
                    IDialogConstants.HORIZONTAL_MARGIN);
            layout.marginHeight = convertVerticalDLUsToPixels(
                    IDialogConstants.VERTICAL_MARGIN);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                    IDialogConstants.HORIZONTAL_SPACING);
            layout.verticalSpacing = convertVerticalDLUsToPixels(
                    IDialogConstants.VERTICAL_SPACING);

            // Align buttons starting from the right side of logo.
            layout.marginLeft = 60;

            composite.setLayout(layout);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
            composite.setLayoutData(data);
            composite.setFont(parent.getFont());

            createLeftButtonBar(composite);

            return super.createButtonBar(composite);
        }

        private Composite createLeftButtonBar(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            // create a layout with spacing and margins appropriate for the font
            // size.
            GridLayout layout = new GridLayout();
            layout.numColumns = 0; // this is incremented by createButton
            layout.makeColumnsEqualWidth = true;
            layout.marginWidth = convertHorizontalDLUsToPixels(
                    IDialogConstants.HORIZONTAL_MARGIN);
            layout.marginHeight = convertVerticalDLUsToPixels(
                    IDialogConstants.VERTICAL_MARGIN);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                    IDialogConstants.HORIZONTAL_SPACING);
            layout.verticalSpacing = convertVerticalDLUsToPixels(
                    IDialogConstants.VERTICAL_SPACING);
            composite.setLayout(layout);
            GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            composite.setLayoutData(data);
            composite.setFont(parent.getFont());

            createLeftButtons(composite);

            return composite;
        }

        private void createLeftButtons(Composite parent) {
            createButton(parent, SKIP_ID,
                    Messages.XMindUpdater_NewDialog_SkipLabel, false);
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, REMIND_ME_ID,
                    Messages.XMindUpdater_NewDialog_RemindLaterLabel, false);
            createButton(parent, DOWNLOAD_ID,
                    Messages.XMindUpdater_NewDialog_UpdateLabel, true)
                            .setFocus();
        }

        @Override
        protected void buttonPressed(int buttonId) {
            setReturnCode(buttonId);
            close();
        }

    }

    private final Display display;

    private final IWorkbench workbench;

    private UpdateData data;

    private final boolean installPermitted;

    private final boolean downloadPermitted;

    private final String skippable;

    private XMindNetRequest checkVersionRequest = null;

    private XMindNetRequest downloadRequest = null;

    public XMindUpdater(IWorkbench workbench) {
        this(workbench, Display.getCurrent(), null, SKIPPABLE_NO, false, false);
    }

    public XMindUpdater(IWorkbench workbench, String skippable) {
        this(workbench, Display.getCurrent(), null, skippable, false, false);
    }

    public XMindUpdater(IWorkbench workbench, UpdateData data, String skippable,
            boolean installPermitted) {
        this(workbench, Display.getCurrent(), data, skippable, installPermitted,
                false);
    }

    public XMindUpdater(IWorkbench workbench, Display display, UpdateData data,
            String skippable, boolean installPermitted,
            boolean downloadPermitted) {
        this.workbench = workbench;
        if (display == null && workbench != null)
            display = workbench.getDisplay();
        this.display = display;

        this.data = data;
        this.installPermitted = installPermitted;
        this.downloadPermitted = downloadPermitted;
        this.skippable = skippable;
    }

    public void abort() {
        if (checkVersionRequest != null) {
            checkVersionRequest.abort();
        }
        if (downloadRequest != null) {
            downloadRequest.abort();
        }
    }

    public IStatus run(final IProgressMonitor monitor) {
        Display display = Display.getCurrent();
        if (display == null)
            return runWithoutUI(monitor);

        final IStatus[] result = new IStatus[1];
        result[0] = null;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                result[0] = runWithoutUI(monitor);
            }
        });
        thread.setName("Check For Software Updates (Forked)"); //$NON-NLS-1$
        thread.start();

        while (result[0] == null) {
            if (!display.isDisposed())
                display.readAndDispatch();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                monitor.setCanceled(true);
                thread.interrupt();
                return CANCELED;
            }
        }

        return result[0];
    }

    private IStatus runWithoutUI(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        if (this.data == null) {
            IStatus checked = check(new SubProgressMonitor(monitor, 1));
            if (!checked.isOK())
                return checked;
        }

        if (this.data == null)
            return CANCELED;

        if (this.data.getInstallerFile() == null) {
            IStatus downloadConfirmed = confirmDownload(monitor);
            if (!downloadConfirmed.isOK())
                return downloadConfirmed;

            IStatus downloaded = download(new SubProgressMonitor(monitor, 79));
            if (!downloaded.isOK())
                return downloaded;
        }

        IStatus installConfirmed = confirmInstall(monitor);
        if (!installConfirmed.isOK())
            return installConfirmed;

        IStatus installed = install(new SubProgressMonitor(monitor, 19));
        if (!installed.isOK())
            return installed;

        return OK;
    }

    private IStatus check(IProgressMonitor monitor) {
        monitor.beginTask(null, 1);

        monitor.subTask(Messages.XMindUpdater_Task_CheckForUpdates);

        IProduct product = Platform.getProduct();
        if (product == null
                || !"org.xmind.cathy.product".equals(product.getId())) //$NON-NLS-1$
            throw new IllegalStateException(
                    Messages.XMindUpdater_Error_NoXMindProductFound);

        String currentVersion = System.getProperty("org.xmind.product.version"); //$NON-NLS-1$
        if (currentVersion == null) {
            throw new IllegalStateException(
                    Messages.XMindUpdater_Error_NoXMindProductVersionFound);
        }
        String currentBuildId = System.getProperty("org.xmind.product.buildid"); //$NON-NLS-1$
        String distribId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            // Compatible with older XMind distributions:
            distribId = "cathy_portable"; //$NON-NLS-1$
        }

        XMindNetRequest request = new XMindNetRequest();
        this.checkVersionRequest = request;
        request.path("/_api/checkVersion/%s", currentVersion); //$NON-NLS-1$
        request.addParameter("distrib", distribId); //$NON-NLS-1$
        if (currentBuildId != null) {
            request.addParameter("buildid", currentBuildId); //$NON-NLS-1$
        }
        try {
            request.get();
        } catch (OperationCanceledException e) {
            return Status.CANCEL_STATUS;
        }

        if (monitor.isCanceled() || request.isAborted())
            return CANCELED;

        if (request.getError() != null)
            return error(request.getError(),
                    request.getError().getLocalizedMessage());

        monitor.done();

        int code = request.getStatusCode();
        IDataStore data = request.getData();
        if (code == XMindNetRequest.HTTP_NOT_FOUND) {
            // No updates found:
            return runInUI(monitor, new IRunnable() {
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    MessageDialog.openInformation(getParentShell(),
                            Messages.XMindUpdater_DialogTitle,
                            Messages.XMindUpdater_Dialog_NoUpdatesFound);
                    return CANCELED;
                }
            });
        } else if (code == XMindNetRequest.HTTP_OK) {
            if (data != null) {
                String downloadURL = data.getString("download"); //$NON-NLS-1$
                if (downloadURL != null) {
                    this.data = UpdateData.createNewData();
                    this.data.setDownloadURL(downloadURL);
                    String allDownloadsURL = data.getString("allDownloads"); //$NON-NLS-1$
                    if (allDownloadsURL == null) {
                        allDownloadsURL = "http://www.xmind.net/xmind/downloads/"; //$NON-NLS-1$
                    }
                    this.data.setAllDownloadsURL(allDownloadsURL);
                    this.data.setSize(data.getLong("size")); //$NON-NLS-1$
                    this.data.setVersion(data.getString("version")); //$NON-NLS-1$
                    this.data.setBuildId(data.getString("buildId")); //$NON-NLS-1$
                    this.data.setWhatsNew(data.getString("whatsNew")); //$NON-NLS-1$
                    this.data.setCanInstall(!data.getBoolean("canInstall")); //$NON-NLS-1$
                    return OK;
                }
            }
            return error(null,
                    NLS.bind(
                            Messages.XMindUpdater_Error_FailedToCheck_with_responseText,
                            request.getResponseText()));
        } else {
            return error(null,
                    NLS.bind(
                            Messages.XMindUpdater_Error_FailedToCheck_with_responseCode,
                            code));
        }
    }

    private IStatus confirmDownload(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return CANCELED;

        if (downloadPermitted)
            return OK;

        monitor.subTask(Messages.XMindUpdater_Task_ConfirmDownloading);
        return runInUI(monitor, new IRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
                ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(
                        InstanceScope.INSTANCE, "org.xmind.cathy"); //$NON-NLS-1$
                String version = data.getBuildId();
                if (SKIPPABLE_YES.equalsIgnoreCase(skippable)
                        && preferenceStore.getString("needSkipVersion").equals( //$NON-NLS-1$
                                version))
                    return CANCELED;
                int code = new NewUpdateDialog(getParentShell()).open();
                if (code == DOWNLOAD_ID) {
                    return OK;
                } else if (code == SKIP_ID) {
//                    openAllDownloadsUrl();
                    preferenceStore.setValue("needSkipVersion", //$NON-NLS-1$
                            version == null
                                    ? IPreferenceStore.STRING_DEFAULT_DEFAULT
                                    : version);
                } else if (code == REMIND_ME_ID) {
                    preferenceStore.setValue("needSkipVersion", //$NON-NLS-1$
                            IPreferenceStore.STRING_DEFAULT_DEFAULT);
                }
                return CANCELED;
            }
        });
    }

//
//    private void openAllDownloadsUrl() {
//        XMindNet.gotoURL(true, data.getAllDownloadsURL());
//    }

    private IStatus download(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return CANCELED;

        final String downloadURL = data.getDownloadURL();
        if (downloadURL == null)
            return CANCELED;

        monitor.beginTask(null, 100);

        if (!data.canInstall()) {
            if (FileUtils.launch(downloadURL))
                return OK;

            monitor.subTask(Messages.XMindUpdater_Task_ChooseSaveLocation);
            IStatus fileChosen = runInUI(monitor, new IRunnable() {
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    FileDialog dialog = new FileDialog(getParentShell(),
                            SWT.SAVE);
                    dialog.setText(Messages.XMindUpdater_SaveDialogTitle);
                    dialog.setFileName(getFileName(downloadURL));
                    String path = dialog.open();
                    if (path == null)
                        return CANCELED;
                    data.setInstallerFile(new File(path));
                    return OK;
                }
            });
            if (!fileChosen.isOK())
                return fileChosen;
        }

        File installerFile = data.getInstallerFile();
        if (installerFile == null) {
            installerFile = createTempInstallerFile(downloadURL);
            data.setInstallerFile(installerFile);
        }

        if (monitor.isCanceled())
            return CANCELED;

        monitor.subTask(Messages.XMindUpdater_Task_Download);
        final XMindNetRequest request = new XMindNetRequest();
        this.downloadRequest = request;
        request.uri(downloadURL);
        request.setTargetFile(data.getInstallerFile());

        final boolean[] finished = new boolean[1];
        finished[0] = false;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                request.get();
                finished[0] = true;

            }
        });
        thread.setDaemon(true);
        thread.setName("Download XMind Software Updates"); //$NON-NLS-1$
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        int oldStatus = -1;
        int requestProgress = 0;
        int receivingProgress = 0;
        while (!finished[0]) {
            if (monitor.isCanceled()) {
                request.abort();
                return CANCELED;
            }
            if (request.isAborted()) {
                monitor.setCanceled(true);
                return CANCELED;
            }
            int status = request.getStatusCode();
            Throwable error = request.getError();
            if (error != null) {
                return error(error,
                        NLS.bind(
                                Messages.XMindUpdater_Error_FailedToDownload_with_errorDescription,
                                error.getLocalizedMessage()));
            }
            if (status != oldStatus) {
                int newProgress = requestProgress;
                if (status == XMindNetRequest.HTTP_PREPARING) {
                    newProgress = 0;
                } else if (status == XMindNetRequest.HTTP_CONNECTING) {
                    newProgress = 5;
                } else if (status == XMindNetRequest.HTTP_SENDING) {
                    newProgress = 10;
                } else if (status == XMindNetRequest.HTTP_WAITING) {
                    newProgress = 15;
                } else if (status == XMindNetRequest.HTTP_RECEIVING) {
                    newProgress = 20;
                } else if (status == XMindNetRequest.HTTP_ERROR) {
                    return error(request.getError(),
                            Messages.XMindUpdater_Error_FailedToDownloadUnknownError);
                } else if (status == XMindNetRequest.HTTP_OK) {
                    break;
                } else {
                    return error(request.getError(),
                            NLS.bind(
                                    Messages.XMindUpdater_Error_FailedToDownloadUnknownError_with_responseCode,
                                    status));
                }
                monitor.worked(newProgress - requestProgress);
                requestProgress = newProgress;
                oldStatus = status;
            }

            if (status == XMindNetRequest.HTTP_RECEIVING) {
                long total = request.getTotalBytes();
                if (total > 0) {
                    long bytes = request.getTransferedBytes();
                    int newProgress = (int) (bytes * 80.0 / total);
                    if (newProgress > receivingProgress) {
                        monitor.worked(newProgress - receivingProgress);
                        monitor.subTask(NLS.bind(
                                Messages.XMindUpdater_Task_DownloadProgress_with_percentage,
                                String.format("%.2f", bytes * 100.0 / total))); //$NON-NLS-1$
                        receivingProgress = newProgress;
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                monitor.setCanceled(true);
                return CANCELED;
            }

        }

        if (monitor.isCanceled())
            return CANCELED;

        int status = request.getStatusCode();
        Throwable error = request.getError();
        if (error != null) {
            return error(error,
                    NLS.bind(
                            Messages.XMindUpdater_Error_FailedToDownload_with_errorDescription,
                            error.getLocalizedMessage()));
        } else if (status == XMindNetRequest.HTTP_ERROR) {
            return error(request.getError(),
                    Messages.XMindUpdater_Error_FailedToDownloadUnknownError);
        } else if (status != XMindNetRequest.HTTP_OK) {
            return error(request.getError(),
                    NLS.bind(
                            Messages.XMindUpdater_Error_FailedToDownloadUnknownError_with_responseCode,
                            status));
        }

        data.save();

        monitor.done();

        return OK;
    }

    private IStatus confirmInstall(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return CANCELED;

        if (installPermitted)
            return OK;

        monitor.subTask(Messages.XMindUpdater_Task_ConfirmInstalling);
        return runInUI(monitor, new IRunnable() {
            public IStatus run(IProgressMonitor monitor) throws Exception {
                int choice = new MessageDialog(getParentShell(),
                        Messages.XMindUpdater_DialogTitle, null,
                        Messages.XMindUpdater_Dialog_ConfirmInstalling,
                        SWT.ICON_INFORMATION,
                        new String[] {
                                Messages.XMindUpdater_Action_QuitAndInstall_text,
                                Messages.XMindUpdater_Action_Later_text },
                        0).open();
                if (choice == 0) {
                    return OK;
                }
                return CANCELED;
            }
        });
    }

    private IStatus install(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return CANCELED;

        monitor.subTask(Messages.XMindUpdater_Task_LaunchInstaller);

        File installerFile = data.getInstallerFile();
        if (installerFile == null || !installerFile.exists())
            return CANCELED;

        IStatus workbenchClosed = closeWorkbench(monitor);
        if (!workbenchClosed.isOK())
            return workbenchClosed;

        if (!data.canInstall()) {
            return launchInstallerFile(monitor, installerFile);
        }

        IStatus launched = launchUpdater(monitor, installerFile);
        if (!launched.isOK())
            return launched;
        return OK;
    }

    private IStatus launchInstallerFile(IProgressMonitor monitor,
            final File installerFile) {
        if (monitor.isCanceled())
            return CANCELED;

        if (display == null || display.isDisposed()) {
            return launchFile(monitor, installerFile);
        } else {
            return runInUI(monitor, new IRunnable() {
                public IStatus run(IProgressMonitor monitor) throws Exception {
                    return launchFile(monitor, installerFile);
                }
            });
        }
    }

    private IStatus launchFile(IProgressMonitor monitor, File file) {
        if (file == null)
            return CANCELED;
        if (!file.exists())
            return error(null,
                    NLS.bind(
                            Messages.XMindUpdater_Error_InstallerExecutableNotFound_with_executablePath,
                            file.getAbsolutePath()));
        if (file.canExecute()) {
            try {
                Runtime.getRuntime()
                        .exec(new String[] { file.getAbsolutePath() });
                return OK;
            } catch (IOException e) {
                return error(e,
                        NLS.bind(
                                Messages.XMindUpdater_Error_FailedToExecuteInstaller_with_errorDescription,
                                e.getLocalizedMessage()));
            }
        } else {
            if (!FileUtils.launch(file.getAbsolutePath()))
                FileUtils.show(file);
            return OK;
        }
    }

    private IStatus launchUpdater(IProgressMonitor monitor,
            final File installerFile) {
        if (monitor.isCanceled())
            return CANCELED;

        String os = Platform.getOS();
        if (Platform.OS_WIN32.equals(os)) {
            return updateOnWindows(monitor, installerFile);
        } else if (Platform.OS_MACOSX.equals(os)) {
            return updateOnMacOSX(monitor, installerFile);
        } else if (Platform.OS_LINUX.equals(os)) {
            return updateOnLinux(monitor, installerFile);
        } else {
            return launchInstallerFile(monitor, installerFile);
        }
    }

    private IStatus updateOnWindows(IProgressMonitor monitor,
            final File installerFile) {
        return launchShell(monitor, installerFile.getAbsolutePath(), "/SILENT", //$NON-NLS-1$
                "/mode=update"); //$NON-NLS-1$
//        String installPath = getInstallPath();
//        try {
//            File batch = File.createTempFile("xmind-update", ".vbs"); //$NON-NLS-1$//$NON-NLS-2$
//            PrintStream ps = new PrintStream(new FileOutputStream(batch));
//            try {
//                ps.println("WScript.sleep(1000)"); //$NON-NLS-1$
//                ps.println("Set objShell = WScript.CreateObject(\"WScript.Shell\")"); //$NON-NLS-1$
//                ps.println("objShell.run \"\"\"" //$NON-NLS-1$
//                        + installerFile.getAbsolutePath()
//                        + "\"\" /SILENT\", , True"); //$NON-NLS-1$
//                if (installPath != null) {
//                    ps.println("objShell.run \"\"\"" + installPath //$NON-NLS-1$
//                            + "\\XMind.exe\"\"\""); //$NON-NLS-1$
//                }
//                ps.println("Set objShell = nothing"); //$NON-NLS-1$
//            } finally {
//                ps.close();
//            }
//            return launchShell(monitor, "wscript.exe", batch.getAbsolutePath()); //$NON-NLS-1$
//        } catch (IOException e) {
//            return error(e, NLS.bind("Failed to execute installer file: {0}",
//                    installerFile.getAbsolutePath()));
//        }
//        return launchShell(monitor, "explorer.exe", "/select,\""
//                + installerFile.getAbsolutePath() + "\"");
    }

    private static class ScriptWriter {

        private PrintStream ps;

        public ScriptWriter(File file) throws FileNotFoundException {
            this.ps = new PrintStream(file);
        }

        public void line(String line, Object... args) throws IOException {
            if (args.length == 0) {
                this.ps.println(line);
            } else {
                this.ps.println(String.format(line, args));
            }
        }

        public void close() throws IOException {
            this.ps.close();
        }
    }

    private IStatus updateOnMacOSX(IProgressMonitor monitor,
            final File installerFile) {
        if (installerFile.getName().endsWith(".dmg")) { //$NON-NLS-1$
            String appPath = findAppPath();
            if (appPath != null) {
                String scriptPath = generateUpdateScriptOnMacOSX(installerFile,
                        appPath);
                if (scriptPath != null) {
                    try {
                        Runtime.getRuntime()
                                .exec(new String[] { "open", scriptPath }); //$NON-NLS-1$
                        return OK;
                    } catch (IOException e) {
                    }
                }
            }
        }
        FileUtils.launch(installerFile.getAbsolutePath());
        return OK;
    }

    @SuppressWarnings("nls")
    private String generateUpdateScriptOnMacOSX(final File installerFile,
            String appPath) {
        String tempAppPath = appPath.substring(0, appPath.length() - 4)
                + " (previous version).app";
        String quotedAppPath = quote(appPath);
        String quotedTempAppPath = quote(tempAppPath);
        String quotedDMGPath = quote(installerFile.getAbsolutePath());
        String workspacePath = getWorkspacePath();
        try {
            String quotedLogPath = workspacePath == null
                    ? quote(File.createTempFile("updatexmind", ".log")
                            .getAbsolutePath())
                    : quote(workspacePath + "/updatexmind.log");
            File script = File.createTempFile("xmind-update", ".sh");
            ScriptWriter writer = new ScriptWriter(script);
            try {
                writer.line("#!/bin/bash");
                writer.line("mv %s %s", quotedAppPath, quotedTempAppPath);
                writer.line("hdiutil mount -nobrowse -quiet %s", quotedDMGPath);
                writer.line(
                        "cp -R \"/Volumes/XMind/XMind.app\" /Applications >> %s",
                        quotedLogPath);
                String ascript = "tell app \"Finder\" to delete POSIX file "
                        + quotedTempAppPath;
                writer.line("osascript -e %s >> %s", quote(ascript),
                        quotedLogPath);
                writer.line("hdiutil unmount -quiet \"/Volumes/XMind/\"");
                writer.line("rm -rf %s", quotedDMGPath);
                writer.line("open \"/Applications/XMind.app\"");
            } finally {
                writer.close();
            }
            return script.getAbsolutePath();
        } catch (IOException e) {
        }
        return null;
    }

    private IStatus updateOnLinux(IProgressMonitor monitor,
            final File installerFile) {
        FileUtils.show(installerFile);
        return OK;
    }

    private Shell getParentShell() {
        if (workbench != null) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if (window != null)
                return window.getShell();
        }
        return Display.getCurrent().getActiveShell();
    }

    private static String findAppPath() {
        String installPath = getInstallPath();
        if (installPath != null) {
            File appLocation = findAppLocation(new File(installPath));
            if (appLocation != null) {
                return appLocation.getAbsolutePath();
            }
        }
        return null;
    }

    private static String getInstallPath() {
        Location location = Platform.getInstallLocation();
        if (location == null)
            return null;
        URL url = location.getURL();
        if (url == null)
            return null;
        try {
            return FileLocator.toFileURL(url).getFile();
        } catch (IOException e) {
            return null;
        }
    }

    private static String getWorkspacePath() {
        Location location = Platform.getInstanceLocation();
        if (location == null)
            return null;
        URL url = location.getURL();
        if (url == null)
            return null;
        try {
            return FileLocator.toFileURL(url).getFile();
        } catch (IOException e) {
            return null;
        }
    }

    private static File findAppLocation(File location) {
        if (location == null)
            return null;
        String name = location.getName();
        if (name.endsWith(".app")) //$NON-NLS-1$
            return location;
        if ("Resources".equals(name) || "Contents".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
            return findAppLocation(location.getParentFile());
        }
        return null;
    }

    private static String quote(String path) {
        return "\"" //$NON-NLS-1$
                + path.replaceAll("\\", "\\\\") //$NON-NLS-1$ //$NON-NLS-2$
                        .replaceAll("\"", "\\\"") //$NON-NLS-1$ //$NON-NLS-2$
                + "\""; //$NON-NLS-1$
    }

    private IStatus launchShell(IProgressMonitor monitor, String... commands) {
        try {
            Runtime.getRuntime().exec(commands);
            return OK;
        } catch (IOException e) {
            return error(e,
                    NLS.bind(
                            Messages.XMindUpdater_Error_FailedToExecuteCommand_with_commandLine,
                            getCommandLine(commands)));
        }
    }

    private static String getCommandLine(String... args) {
        StringBuffer buffer = new StringBuffer(args.length * 15);
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                buffer.append(args[i]);
            } else {
                buffer.append(' ');
                buffer.append('"');
                buffer.append(args[i]);
                buffer.append('"');
            }
        }
        return buffer.toString();
    }

    private IStatus closeWorkbench(IProgressMonitor monitor) {
        return workbench == null || display == null || display.isDisposed() ? OK
                : runInUI(monitor, new IRunnable() {
                    public IStatus run(IProgressMonitor monitor)
                            throws Exception {
                        if (workbench != null && !workbench.close())
                            return CANCELED;
                        return OK;
                    }
                });
    }

    private IStatus runInUI(final IProgressMonitor monitor,
            final IRunnable runnable) {
        if (display == null || display.isDisposed())
            return CANCELED;
        final IStatus[] result = new IStatus[1];
        result[0] = null;
        display.syncExec(new Runnable() {
            public void run() {
                try {
                    result[0] = runnable.run(monitor);
                } catch (Throwable e) {
                    result[0] = error(e, e.getLocalizedMessage());
                }
            }
        });
        if (monitor.isCanceled())
            return CANCELED;
        if (result[0] != null)
            return result[0];
        return OK;
    }

    private static File createTempInstallerFile(String downloadURL) {
        File dir = UpdateData.getUpdateFolder();
        String fileName = getFileName(downloadURL);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, fileName);
    }

    private static String getFileName(String downloadURL) {
        String path;
        try {
            path = new URI(downloadURL).getPath();
        } catch (URISyntaxException e) {
            int j = downloadURL.lastIndexOf('?');
            if (j < 0)
                path = downloadURL;
            else
                path = downloadURL.substring(0, j);
        }
        int i = path.lastIndexOf('/');
        if (i < 0)
            return path;
        return path.substring(i + 1);
    }

    private static IStatus error(Throwable exception, String message) {
        return new Status(IStatus.WARNING, PLUGIN_ID, message, exception);
    }

    public static boolean checkSoftwareUpdateOnStart() {
        String currentVersion = System.getProperty("org.xmind.product.version"); //$NON-NLS-1$
        if (currentVersion == null)
            return false;

        UpdateData data = UpdateData.loadData();
        if (data == null)
            // No previously downloaded software updates.
            return false;

        String newVersion = data.getVersion();
        if (newVersion == null
                || compareVersion(currentVersion, newVersion) >= 0) {
            // Outdated software updates.
            UpdateData.clear();
            return false;
        }

        File installerFile = data.getInstallerFile();
        if (installerFile == null || !installerFile.exists()) {
            // Unfinished software updates.
            UpdateData.clear();
            return false;
        }

        int choice = new MessageDialog(null, Messages.XMindUpdater_DialogTitle,
                null,
                Messages.XMindUpdater_Dialog_ConfirmInstallingOnStartupCheck,
                SWT.ICON_INFORMATION,
                new String[] { Messages.XMindUpdater_Action_Install_text,
                        Messages.XMindUpdater_Action_Clear_text,
                        Messages.XMindUpdater_Action_Later_text },
                0).open();
        if (choice == 1) {
            if (MessageDialog.openQuestion(null,
                    Messages.XMindUpdater_DialogTitle,
                    Messages.XMindUpdater_Dialog_ConfirmClearDownload))
                UpdateData.clear();
        }
        if (choice != 0)
            return false;

        return new XMindUpdater(null, data, SKIPPABLE_NO, true)
                .run(new NullProgressMonitor()).isOK();
    }

    private static int compareVersion(String version1, String version2) {
        Pattern sep = Pattern.compile("\\."); //$NON-NLS-1$
        String[] v1 = sep.split(version1);
        String[] v2 = sep.split(version2);
        int m = Math.max(v1.length, v2.length);
        StringBuffer s1 = new StringBuffer(32);
        StringBuffer s2 = new StringBuffer(32);
        String e = ""; //$NON-NLS-1$
        String p1, p2;
        int j, t;
        for (int i = 0; i < m; i++) {
            p1 = i < v1.length ? v1[i] : e;
            p2 = i < v2.length ? v2[i] : e;
            t = Math.max(p1.length(), p2.length());
            for (j = 0; j < t; j++) {
                if (j < t - p1.length())
                    s1.append('0');
                if (j < t - p2.length())
                    s2.append('0');
            }
            s1.append(p1);
            s2.append(p2);
        }
        return s1.toString().compareTo(s2.toString());
    }
}
