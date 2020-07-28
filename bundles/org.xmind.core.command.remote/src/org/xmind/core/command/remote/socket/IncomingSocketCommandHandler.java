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
/**
 * 
 */
package org.xmind.core.command.remote.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.transfer.IncomingCommandHandler;
import org.xmind.core.internal.command.remote.Messages;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * A job that handles incoming commands sent through sockets.
 * 
 * @author Frank Shaka
 */
public class IncomingSocketCommandHandler extends Job {

    private class InternalIncomingSocketCommandHandler extends
            IncomingCommandHandler {

        public InternalIncomingSocketCommandHandler() {
            super();
        }

        protected IStatus createReadingErrorStatus(Throwable e) {
            IStatus status = IncomingSocketCommandHandler.this
                    .createReadingErrorStatus(this, e);
            if (status != null)
                return status;
            return super.createReadingErrorStatus(e);
        }

        protected IStatus createWritingErrorStatus(Throwable e) {
            IStatus status = IncomingSocketCommandHandler.this
                    .createWritingErrorStatus(this, e);
            if (status != null)
                return status;
            return super.createWritingErrorStatus(e);
        }
    }

    private final Socket socket;

    private SocketPool socketPool = null;

    private String pluginId = RemoteCommandPlugin.PLUGIN_ID;

    /**
     * 
     */
    public IncomingSocketCommandHandler(Socket socket) {
        super("Handle Command Request Coming From Socket Connection"); //$NON-NLS-1$
        this.socket = socket;
        setUser(false);
        setSystem(true);
    }

    public void setSocketPool(SocketPool socketPool) {
        this.socketPool = socketPool;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            InputStream input = socket.getInputStream();
            try {
                OutputStream output = socket.getOutputStream();
                try {
                    IncomingCommandHandler handler = new InternalIncomingSocketCommandHandler();
                    handler.setPluginId(getPluginId());
                    handler.setRemoteLocation(socket.getRemoteSocketAddress());
                    return handler
                            .handleIncomingCommand(monitor, input, output);
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, RemoteCommandPlugin.PLUGIN_ID,
                    null, e);
        } finally {
            if (socketPool != null)
                socketPool.removeSocket(socket);
        }
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        if (pluginId == null)
            pluginId = RemoteCommandPlugin.PLUGIN_ID;
        this.pluginId = pluginId;
    }

    protected IStatus createReadingErrorStatus(IncomingCommandHandler handler,
            Throwable e) {
        if (e instanceof SocketException || e instanceof SocketTimeoutException) {
            return new Status(
                    IStatus.WARNING,
                    getPluginId(),
                    NLS.bind(
                            Messages.IncomingSocketCommandHandler_ConnectionFailed_Message,
                            handler.getRemoteLocation()), e);
        }
        return null;
    }

    protected IStatus createWritingErrorStatus(IncomingCommandHandler handler,
            Throwable e) {
        if (e instanceof SocketException || e instanceof SocketTimeoutException) {
            return new Status(
                    IStatus.WARNING,
                    getPluginId(),
                    NLS.bind(
                            Messages.IncomingSocketCommandHandler_ConnectionFailed_Message,
                            handler.getRemoteLocation()), e);
        }
        return null;
    }
}
