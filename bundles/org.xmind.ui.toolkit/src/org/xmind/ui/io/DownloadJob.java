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
package org.xmind.ui.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.ui.internal.ToolkitPlugin;
import org.xmind.ui.io.IDownloadTarget.IDownloadTarget2;

/**
 * A <code>DownloadJob</code> performs data downloading from a URL to a target
 * location.
 * 
 * @author Frank Shaka
 */
public class DownloadJob extends Job {

    private String pluginId;

    private String sourceURL;

    private IDownloadTarget target;

    private URLConnection connection = null;

    public DownloadJob(String jobName, String sourceURL, String targetPath) {
        this(jobName, sourceURL, new FileDownloadTarget(targetPath, true),
                ToolkitPlugin.PLUGIN_ID);
    }

    public DownloadJob(String jobName, String sourceURL, String targetPath,
            String pluginId) {
        this(jobName, sourceURL, new FileDownloadTarget(targetPath, true),
                pluginId);
    }

    public DownloadJob(String jobName, String sourceURL,
            IDownloadTarget target) {
        this(jobName, sourceURL, target, ToolkitPlugin.PLUGIN_ID);
    }

    public DownloadJob(String jobName, String sourceURL, IDownloadTarget target,
            String pluginId) {
        super(jobName);
        Assert.isNotNull(sourceURL);
        Assert.isNotNull(target);
        this.sourceURL = sourceURL;
        this.target = target;
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public String getTargetPath() {
        return target.getPath();
    }

    /**
     * Sets up the URL connection. This method is called after the connection is
     * created, and before it is actually connected.
     * <p>
     * Clients may extend this method to add options (e.g. timeout) or HTTP
     * headers to this connection. The default implementation does nothing so
     * that all default values are used.
     * 
     * @param connection
     *            the connection created from the source URL
     */
    protected void setupConnection(URLConnection connection) {
        // To be extended.
    }

    /**
     * Validates the connected URL connection object. This method is called
     * after the connection is connected, and before the data transfer starts.
     * <p>
     * Clients may extend this method to check for response code or other
     * validating factors of this connection. The default implementation does
     * nothing and simply returns <code>null</code> to accept all connections.
     * 
     * @param connection
     *            the connection object connected to the source URL
     * @return a non-<code>null</code> status to reject this connection from
     *         being downloaded, or <code>null</code> to accept it
     */
    protected IStatus validateConnection(URLConnection connection) {
        return null;
    }

    /**
     * Executes this download job.
     * <p>
     * This implementation delegates the actual download job to
     * {@link #runSafely(IProgressMonitor)} and interprets its exceptions to
     * error status.
     * 
     * @param monitor
     *            the monitor to be used for reporting progress and responding
     *            to cancelation. The monitor is never <code>null</code>
     * @return resulting status of the run. The result must not be
     *         <code>null</code>
     */
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status;
        try {
            status = runSafely(monitor);
        } catch (Throwable e) {
            if (e instanceof InterruptedIOException) {
                status = cancelStatus();
            } else {
                status = errorStatus(e);
            }
        }
        if (target instanceof IDownloadTarget2) {
            try {
                ((IDownloadTarget2) target).afterDownload(status);
            } catch (Throwable ignore) {
            }
        }
        return status;
    }

    /**
     * Executes the download job in a safe context. It's safe to throw
     * exceptions in this method to interrupt the download process.
     * 
     * @param monitor
     *            the progress monitor
     * @return resulting status. Must not be <code>null</code>
     * @throws Exception
     *             any type of exception
     */
    private IStatus runSafely(IProgressMonitor monitor) throws Exception {
        monitor.beginTask(null, 100);

        monitor.subTask(NLS.bind(Messages.ConnectingSource, getSourceURL()));

        URL url = new URL(sourceURL);
        URLConnection connection = url.openConnection();

        while (true) {
            setURLConnection(connection);
            if (monitor.isCanceled())
                return cancelStatus();

            setupConnection(connection);
            if (monitor.isCanceled())
                return cancelStatus();

            connection.connect();
            if (monitor.isCanceled())
                return cancelStatus();

            /// auto redirect
            int responseCode = ((HttpURLConnection) connection)
                    .getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String newLocation = connection.getHeaderField("Location"); //$NON-NLS-1$
                connection = new URL(newLocation).openConnection();

                if (monitor.isCanceled())
                    return cancelStatus();
            } else {
                break;
            }
        }

        IStatus consumed = validateConnection(connection);
        if (consumed != null)
            return consumed;
        if (monitor.isCanceled())
            return cancelStatus();

        int length = connection.getContentLength();
        if (monitor.isCanceled())
            return cancelStatus();

        InputStream sourceStream = connection.getInputStream();

        try {
            if (monitor.isCanceled())
                return cancelStatus();

            monitor.subTask(
                    NLS.bind(Messages.InitializingTarget, getTargetPath()));
            OutputStream targetStream = target.openOutputStream();
            if (monitor.isCanceled())
                return cancelStatus();

            try {
                sourceStream = new MonitoredInputStream(sourceStream, monitor);
                targetStream = new MonitoredOutputStream(targetStream, monitor);

                monitor.subTask(Messages.TransferingData);

                transfer(sourceStream, targetStream,
                        new SubProgressMonitor(monitor, 100), length);

                setURLConnection(null);

                monitor.done();
                return new Status(IStatus.OK, pluginId,
                        NLS.bind(Messages.DownloadFinished, getSourceURL(),
                                getTargetPath()));
            } finally {
                try {
                    targetStream.close();
                } catch (IOException ignore) {
                }
            }
        } finally {
            try {
                sourceStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Transfers all content from the source input stream to the target output
     * stream.
     * 
     * @param sourceStream
     *            the source input stream
     * @param targetStream
     *            the target output stream
     * @param monitor
     *            the progress monitor
     * @param length
     *            the total length of data to read, or an integer value less
     *            than <code>0</code> indicating that the length is unknown
     * @throws IOException
     */
    private void transfer(InputStream sourceStream, OutputStream targetStream,
            IProgressMonitor monitor, int length) throws IOException {
        monitor.beginTask(null, length < 0 ? 100 : Math.max(1, length / 1024));
        String total = length < 0 ? null : String.format("%.1fK", //$NON-NLS-1$
                length / 1024.0d);
        byte[] buffer = new byte[1024];
        int downloaded = 0;
        int num;
        int worked = 0, newWorked = 0;
        while ((num = sourceStream.read(buffer)) > 0) {
            targetStream.write(buffer, 0, num);
            downloaded += num;
            String taskName = (total == null
                    ? String.format("(%.1fK)", //$NON-NLS-1$ 
                            (downloaded / 1024.0))
                    : String.format("(%.1fK/%s)", //$NON-NLS-1$ 
                            (downloaded / 1024.0), total));
            monitor.subTask(Messages.TransferingData + " " + taskName); //$NON-NLS-1$

            if (length < 0) {
                newWorked = Math.min(worked + 1, 99);
            } else {
                newWorked = downloaded / 1024;
            }
            if (newWorked > worked) {
                monitor.worked(newWorked - worked);
                worked = newWorked;
            }
        }
    }

    /**
     * Creates a status indicating the job is canceled.
     * 
     * @return a status with <code>CANCEL</code> severity
     */
    protected Status cancelStatus() {
        return new Status(IStatus.CANCEL, pluginId, NLS.bind(
                Messages.DownloadCanceled, getSourceURL(), getTargetPath()));
    }

    /**
     * Creates an error status that wraps the given exception.
     * 
     * @param e
     *            the exception to wrap
     * @return a status with <code>ERROR</code> severity that wraps the given
     *         exception
     */
    protected IStatus errorStatus(Throwable e) {
        return new Status(IStatus.ERROR, pluginId, NLS.bind(
                Messages.DownloadFailed, getSourceURL(), getTargetPath()), e);
    }

    private void setURLConnection(URLConnection connection) {
        this.connection = connection;
    }

    protected void canceling() {
        super.canceling();
        URLConnection currentConnection = this.connection;
        if (currentConnection != null) {
            // TODO destroy current URL connection
        }
    }

}
