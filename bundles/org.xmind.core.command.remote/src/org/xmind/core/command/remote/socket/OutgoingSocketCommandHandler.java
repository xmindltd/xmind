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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.IReturnValueConsumer;
import org.xmind.core.command.transfer.OutgoingCommandHandler;
import org.xmind.core.internal.command.remote.Messages;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * A client that handles outgoing commands sent to remote sockets.
 * 
 * @author Frank Shaka
 */
public class OutgoingSocketCommandHandler extends OutgoingCommandHandler {

    private static final String DEBUG_OPTION = "/debug/outgoingSocketCommandHandler"; //$NON-NLS-1$

    private static boolean DEBUGGING = RemoteCommandPlugin.getDefault()
            .isDebugging(DEBUG_OPTION);

    private ISocketAddress remoteAddress;

    private SocketPool socketPool = null;

    /**
     * 
     */
    public OutgoingSocketCommandHandler(ISocketAddress remoteAddress) {
        Assert.isNotNull(remoteAddress);
        this.remoteAddress = remoteAddress;
        setRemoteLocation(remoteAddress);
    }

    public void setSocketPool(SocketPool socketPool) {
        this.socketPool = socketPool;
    }

    public IStatus handleOutgoingCommand(IProgressMonitor monitor,
            ICommand command, IReturnValueConsumer returnValueConsumer,
            int timeout) {
        monitor.beginTask(null, 100);

        monitor.subTask(NLS.bind(
                Messages.OutgoingSocketCommandHandler_ConnectionRemoteCommand_Message,
                remoteAddress));
        Socket socket = new Socket();
        try {
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            try {
                InetSocketAddress address = new InetSocketAddress(
                        remoteAddress.getHostName(), remoteAddress.getPort());
                socket.connect(address, timeout);
                socket.setSoTimeout(timeout);
                if (socketPool != null)
                    socketPool.addSocket(socket);
            } catch (IOException e) {
                if (DEBUGGING)
                    return new Status(IStatus.ERROR,
                            RemoteCommandPlugin.PLUGIN_ID,
                            NLS.bind(
                                    Messages.OutgoingSocketCommandHandler_ConnectionFailed_Message,
                                    remoteAddress),
                            e);
                return new Status(IStatus.CANCEL, RemoteCommandPlugin.PLUGIN_ID,
                        NLS.bind(
                                Messages.OutgoingSocketCommandHandler_ConnectionFailed_Message,
                                remoteAddress),
                        e);
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            InputStream input;
            try {
                input = socket.getInputStream();
            } catch (IOException e) {
                return new Status(IStatus.ERROR, RemoteCommandPlugin.PLUGIN_ID,
                        NLS.bind(
                                Messages.OutgoingSocketCommandHandler_FailedOpenInputStream,
                                remoteAddress),
                        e);
            }
            try {
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                OutputStream output;
                try {
                    output = socket.getOutputStream();
                } catch (IOException e) {
                    return new Status(IStatus.ERROR,
                            RemoteCommandPlugin.PLUGIN_ID,
                            NLS.bind(
                                    Messages.OutgoingSocketCommandHandler_FailedOpenOutputStream,
                                    remoteAddress),
                            e);
                }
                try {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;

                    monitor.worked(10);

                    IProgressMonitor handleMonitor = new SubProgressMonitor(
                            monitor, 90);
                    IStatus handled = super.handleOutgoingCommand(handleMonitor,
                            command, returnValueConsumer, input, output);
                    if (!handleMonitor.isCanceled())
                        handleMonitor.done();
                    if (!monitor.isCanceled())
                        monitor.done();
                    return handled;
                } finally {
                    try {
                        output.close();
                    } catch (IOException e) {
                    }
                }
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            if (socketPool != null)
                socketPool.removeSocket(socket);
        }
    }

    protected IStatus createSendingErrorStatus(IOException e) {
        if (e instanceof SocketTimeoutException) {
            return new Status(IStatus.WARNING, getPluginId(),
                    NLS.bind(
                            Messages.OutgoingSocketCommandHandler_ConnectionTimeOut,
                            remoteAddress),
                    e);
        }
        return super.createSendingErrorStatus(e);
    }

    protected IStatus createReceivingErrorStatus(IOException e) {
        if (e instanceof SocketTimeoutException) {
            return new Status(IStatus.WARNING, getPluginId(),
                    NLS.bind(
                            Messages.OutgoingSocketCommandHandler_ConnectionTimeOut,
                            remoteAddress),
                    e);
        }
        return super.createReceivingErrorStatus(e);
    }

}
