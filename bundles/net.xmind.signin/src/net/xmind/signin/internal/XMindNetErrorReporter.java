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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.net.internal.IRequestStatusChangeListener;
import org.xmind.core.net.internal.XMindNetRequest;
import org.xmind.ui.internal.statushandlers.IErrorReporter;
import org.xmind.ui.internal.statushandlers.StatusDetails;

public class XMindNetErrorReporter implements IErrorReporter {

    private static final String API_PATH = "/_api/error-report"; //$NON-NLS-1$

    private static final String USER_NAME_REPLACEMENT = "USERNAME"; //$NON-NLS-1$

    private static final String PREF_REPORTER_EMAIL = "net.xmind.signin.errorReporter.reporterEmail"; //$NON-NLS-1$

    private static class ReportErrorJob extends Job
            implements IRequestStatusChangeListener {

        private final StatusDetails error;

        private final String reporterEmail;

        private XMindNetRequest req = null;

        private IProgressMonitor monitor;

        public ReportErrorJob(StatusDetails error, String reporterEmail) {
            super(Messages.ReportErrorJob_jobName);
            this.error = error;
            this.reporterEmail = reporterEmail;
        }

        protected IStatus run(IProgressMonitor monitor) {
            File tempLogFile = null;
            try {
                this.monitor = monitor;
                monitor.beginTask(null, 100);

                req = new XMindNetRequest(true);
                req.addStatusChangeListener(this);
                req.path(API_PATH);
                req.multipart();
                if (reporterEmail != null) {
                    req.addParameter("reporter", reporterEmail); //$NON-NLS-1$
                }
                req.addParameter("details", error.getFullText()); //$NON-NLS-1$
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                tempLogFile = copyTempLogFile();
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                if (tempLogFile != null) {
                    req.addParameter("log", tempLogFile); //$NON-NLS-1$
                }
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                monitor.worked(10);
                monitor.subTask(
                        Messages.ReportErrorJob_SendingErrorReport_taskTitle);

                req.post();
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                int code = req.getStatusCode();
                if (code != XMindNetRequest.HTTP_OK) {
                    return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                            code, req.getResponseText(), req.getError());
                }

                monitor.done();
                return Status.OK_STATUS;

            } catch (Throwable e) {
                return new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                        "Failed to send error report to xmind.net", e); //$NON-NLS-1$
            } finally {
                if (tempLogFile != null)
                    tempLogFile.delete();
            }
        }

        protected void canceling() {
            super.canceling();
            if (req != null) {
                req.abort();
            }
        }

        public void requestStatusChanged(XMindNetRequest request, int oldStatus,
                int newStatus) {
            IProgressMonitor monitor = this.monitor;
            if (monitor == null || monitor.isCanceled())
                return;

            if (newStatus == XMindNetRequest.HTTP_CONNECTING) {
                monitor.worked(10);
            } else if (newStatus == XMindNetRequest.HTTP_SENDING) {
                monitor.worked(10);
            } else if (newStatus == XMindNetRequest.HTTP_WAITING) {
                monitor.worked(10);
            } else if (newStatus == XMindNetRequest.HTTP_RECEIVING) {
                monitor.worked(30);
            }
        }

    }

    public XMindNetErrorReporter() {
    }

    public boolean report(final StatusDetails error)
            throws InterruptedException {
        final Display display = Display.getCurrent();

        // Retrieve reporter email:
        String reporterEmail = Activator.getDefault().getPreferenceStore()
                .getString(PREF_REPORTER_EMAIL);
        if (display != null) {
            InputDialog inputDialog = new InputDialog(display.getActiveShell(),
                    Messages.XMindNetErrorReporter_ReporterEmailInputDialog_windowTitle,
                    Messages.XMindNetErrorReporter_ReporterEmailInputDialog_message,
                    reporterEmail, null);
            if (inputDialog.open() != InputDialog.OK)
                throw new InterruptedException();
            reporterEmail = inputDialog.getValue();
            Activator.getDefault().getPreferenceStore()
                    .putValue(PREF_REPORTER_EMAIL, reporterEmail);
        }

        // Start report job:
        ReportErrorJob job = new ReportErrorJob(error, reporterEmail);
        job.setUser(true);
        job.schedule(0);

        // Block the current thread for job to finish:
        if (display != null) {
            job.addJobChangeListener(new JobChangeAdapter() {
                public void done(IJobChangeEvent event) {
                    super.done(event);
                    display.wake();
                }
            });
            while (job.getResult() == null) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } else {
            job.join();
        }

        IStatus result = job.getResult();
        if (result.matches(IStatus.CANCEL))
            throw new InterruptedException();
        return result.isOK();
    }

    private static File copyTempLogFile() {
        IPath logFilePath = Platform.getLogFileLocation();
        if (logFilePath == null)
            return null;

        File logFile = logFilePath.toFile();
        if (logFile == null)
            return null;

        IPath tempDirPath = Platform
                .getStateLocation(Activator.getDefault().getBundle());
        if (tempDirPath == null)
            return null;

        File tempDir = tempDirPath.toFile();
        if (tempDir == null)
            return null;

        File tempFile = new File(new File(tempDir, "temp-log"), //$NON-NLS-1$
                "" + System.currentTimeMillis() + ".log"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!tempFile.getParentFile().isDirectory()) {
            tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.getParentFile().isDirectory())
            return null;

        String userName = System.getProperty("user.name"); //$NON-NLS-1$
        if ("".equals(userName.trim())) //$NON-NLS-1$
            userName = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            try {
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(tempFile));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (userName != null)
                            line = line.replace(userName,
                                    USER_NAME_REPLACEMENT);
                        writer.write(line);
                        writer.newLine();
                    }
                } finally {
                    writer.close();
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            tempFile.delete();
            return null;
        }

        return tempFile;
    }

}
