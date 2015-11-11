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
package org.xmind.cathy.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.internal.XMindNetRequest;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.viewers.FileUtils;

import net.xmind.signin.XMindNet;

/**
 * 
 * @author Frank Shaka
 * @deprecated See net.xmind.signin.internal.update.CheckForUpdatesJob in
 *             net.xmind.signin plugin.
 */
public class CheckUpdatesJob extends Job {

    private static final String PLUGIN_ID = CathyPlugin.PLUGIN_ID;

    private class NewUpdateDialog extends Dialog {

        public NewUpdateDialog(Shell parentShell) {
            super(parentShell);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createImageAndText(composite);
            return composite;
        }

        private void createImageAndText(Composite parent) {
            Composite imageAndText = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = SWT.DEFAULT;
            gridData.heightHint = SWT.DEFAULT;
            imageAndText.setLayoutData(gridData);

            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 20;
            imageAndText.setLayout(gridLayout);

            createImage(imageAndText);
            createText(imageAndText);
        }

        private void createImage(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.TOP, false, false));
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

        private void createText(Composite parent) {
            Composite textArea = new Composite(parent, SWT.NONE);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.widthHint = 420;
            gridData.heightHint = SWT.DEFAULT;
            textArea.setLayoutData(gridData);
            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            gridLayout.verticalSpacing = 10;
            gridLayout.horizontalSpacing = 0;
            textArea.setLayout(gridLayout);

            createMessageArea(textArea);
            createInfoArea(textArea);
            createMoreDownloadsArea(textArea);
        }

        private void createMessageArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            label.setText(WorkbenchMessages.CheckUpdatesJob_NewUpdate_message);
            label.setFont(
                    FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT, 1));
        }

        private void createInfoArea(Composite parent) {
            Label label = new Label(parent, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            label.setText(NLS.bind(
                    WorkbenchMessages.CheckUpdatesJob_NewUpdate_info_message,
                    version, FileUtils.fileLengthToString(size)));
        }

        private void createMoreDownloadsArea(Composite parent) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(
                    new GridData(SWT.FILL, SWT.CENTER, true, false));
            label.setText(
                    WorkbenchMessages.CheckUpdatesJob_NewUpdate_moreDownloads_text);
            label.setForeground(
                    parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
            label.setCursor(
                    parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            label.addListener(SWT.MouseUp, new Listener() {
                public void handleEvent(Event event) {
                    openAllDownloadsUrl();
                }
            });
        }

    }

    private final IWorkbench workbench;

    private boolean showFailureResult;

    private XMindNetRequest checkVersionRequest = null;

    private String downloadUrl = null;

    private String allDownloadsUrl = null;

    private String version = null;

    private long size = 0;

    public CheckUpdatesJob(IWorkbench workbench, boolean showFailureResult) {
        super(WorkbenchMessages.CheckUpdatesJob_jobName);
        this.workbench = workbench;
        this.showFailureResult = showFailureResult;
    }

    protected IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        IStatus checked = check(new SubProgressMonitor(monitor, 1));
        if (!checked.isOK())
            return checked;

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return Status.OK_STATUS;
    }

    private IStatus check(IProgressMonitor monitor) {
        monitor.beginTask(null, 1);

        monitor.subTask("Checking for new updates"); //$NON-NLS-1$

        String currentVersion = System.getProperty("org.xmind.product.version"); //$NON-NLS-1$
        if (currentVersion == null) {
            throw new IllegalStateException(
                    "XMind application not running. Failed to fetch product version."); //$NON-NLS-1$
        }
        String distribId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            distribId = "cathy_portable"; //$NON-NLS-1$
        }

        downloadUrl = null;
        allDownloadsUrl = null;
        version = null;
        size = 0;

        XMindNetRequest request = new XMindNetRequest();
        this.checkVersionRequest = request;
        request.path("/_api/checkVersion/%s", currentVersion); //$NON-NLS-1$
        request.addParameter("distrib", distribId); //$NON-NLS-1$
        request.get();

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        if (request.isAborted())
            return Status.CANCEL_STATUS;

        if (request.getError() != null)
            return errorStatus(request.getError(),
                    request.getError().getLocalizedMessage());

        monitor.done();

        int code = request.getStatusCode();
        IDataStore data = request.getData();
        if (code == XMindNetRequest.HTTP_OK) {
            if (data != null) {
                downloadUrl = data.getString("download"); //$NON-NLS-1$
                if (downloadUrl != null) {
                    allDownloadsUrl = data.getString("allDownloads"); //$NON-NLS-1$
                    size = data.getLong("size"); //$NON-NLS-1$
                    version = data.getString("version"); //$NON-NLS-1$
                    if (allDownloadsUrl == null) {
                        allDownloadsUrl = "http://www.xmind.net/xmind/downloads/"; //$NON-NLS-1$
                    }
                    return showNewUpdate(monitor);
                }
            }
            return errorStatus(null, NLS.bind(
                    "Invalid response from checking for new updates: {0}", //$NON-NLS-1$
                    request.getResponseText()));
        } else if (code == XMindNetRequest.HTTP_NOT_FOUND) {
            return showNoUpdate(monitor);
        } else {
            return errorStatus(null, NLS.bind(
                    "Failed to check for new updates due to unexpected error ({0}).", //$NON-NLS-1$
                    code));
        }
    }

    @Override
    protected void canceling() {
        if (checkVersionRequest != null) {
            checkVersionRequest.abort();
        }
        super.canceling();
    }

    private IStatus runInUI(final IProgressMonitor monitor,
            final Runnable runnable) {
        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed())
            return Status.CANCEL_STATUS;
        final Throwable[] error = new Throwable[1];
        error[0] = null;
        display.syncExec(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    error[0] = e;
                }
            }
        });
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        if (error[0] != null) {
            return errorStatus(error[0], null);
        }
        return Status.OK_STATUS;
    }

    private IStatus showNewUpdate(IProgressMonitor monitor) {
        final boolean[] canceled = new boolean[1];
        canceled[0] = false;
        IStatus shown = runInUI(monitor, new Runnable() {
            public void run() {
                int ret = new NewUpdateDialog(null).open();
                if (ret != NewUpdateDialog.OK) {
                    canceled[0] = true;
                }
            }
        });
        if (!shown.isOK())
            return shown;
        if (canceled[0])
            return Status.CANCEL_STATUS;
        return Status.OK_STATUS;
    }

    private IStatus showNoUpdate(IProgressMonitor monitor) {
        return runInUI(monitor, new Runnable() {
            public void run() {
                MessageDialog.openInformation(null,
                        WorkbenchMessages.AppWindowTitle,
                        WorkbenchMessages.CheckUpdatesJob_NoUpdate_message);
            }
        });
    }

    private IStatus errorStatus(Throwable exception, String message) {
        if (showFailureResult)
            return new Status(IStatus.ERROR, PLUGIN_ID, message, exception);
        return new Status(IStatus.WARNING, PLUGIN_ID, message, exception);
    }

    private void openAllDownloadsUrl() {
        XMindNet.gotoURL(true, allDownloadsUrl);
    }

}
