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
package org.xmind.core.command.remote.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.xmind.core.command.remote.ICommandServer;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.internal.command.remote.Messages;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * This class provides basic implementations of a command server that relies on
 * a server socket.
 * 
 * @author Frank Shaka
 */
public class SocketCommandServer implements ICommandServer {

    private ICommandServiceDomain domain;

    private int defaultPort;

    private int defaultBacklog;

    private boolean triesEphemeralPort;

    private ServerSocket server = null;

    private Thread thread = null;

    private SocketCommandServiceInfo info = null;

    private Object lock = new Object();

    private Runnable serverRunner = new Runnable() {

        /**
         * Runs the server handling loop. Accepts all socket connections and
         * fork them into separate handling job.
         */
        public void run() {
            ServerSocket theServer = server;
            if (theServer == null)
                return;

            try {
                while (true) {
                    Socket socket = theServer.accept();
                    socketPool.addSocket(socket);
                    runIncomingCommandHandler(socket);
                }
            } catch (IOException e) {
                if (thread != null || server != null) {
                    RemoteCommandPlugin
                            .log("Error occurred while handling local command server.", //$NON-NLS-1$
                                    e);
                }
            } finally {
                try {
                    theServer.close();
                } catch (IOException e) {
                }
            }
        }

    };

    private SocketPool socketPool = new SocketPool();

    /**
     * Constructs a new instance using default configurations, i.e. an ephemeral
     * port as the default port, a backlog valued <code>50</code>.
     */
    public SocketCommandServer() {
        this(0, 50, false);
    }

    /**
     * Constructs a new instance with specified configurations.
     * 
     * @param defaultPort
     *            the default port number
     * @param defaultBacklog
     *            the default connection waiting queue size
     * @param triesEphemeralPort
     *            <code>true</code> to indicate that an ephemeral port will be
     *            tried if the default port fails to be bound to, which makes
     *            the best effort to ensure the command server started up, or
     *            <code>false</code> if no other port number than the default
     *            one should be tried to make the command server either fix on
     *            the given port number or just fail
     */
    public SocketCommandServer(int defaultPort, int defaultBacklog,
            boolean triesEphemeralPort) {
        if (defaultPort < 0)
            defaultPort = 0;
        this.defaultPort = defaultPort;
        this.defaultBacklog = defaultBacklog;
        this.triesEphemeralPort = triesEphemeralPort;
    }

    public void init(ICommandServiceDomain domain) {
        this.domain = domain;
    }

    /**
     * @return the domain
     */
    public ICommandServiceDomain getDomain() {
        return domain;
    }

    /**
     * @return the defaultBacklog
     */
    public int getDefaultBacklog() {
        return defaultBacklog;
    }

    /**
     * @return the defaultPort
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * @return the server
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * Returns the thread that handles the server socket's events. It is
     * referred to as <i>'the loop thread'</i> in this class.
     * 
     * @return the loop thread, or <code>null</code> if the command server is
     *         not deployed or has been undeployed
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Deploys this command server using a server socket.
     * 
     * <p>
     * This method will first try to bind the server socket to the default port.
     * If that fails, it will use an ephemeral port provided by the system.
     * </p>
     * 
     * <p>
     * A daemon thread will be started once the server socket is ready. The
     * thread will start a loop of accepting new socket connections on the
     * server socket and starting an {@link org.eclipse.core.runtime.jobs.Job}
     * to handle the command coming in through the accepted socket connection.
     * </p>
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the deployment
     */
    public IStatus deploy(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);
        monitor.subTask(Messages.SocketCommandServer_OperationLock);

        synchronized (lock) {
            monitor.subTask(Messages.SocketCommandServer_OpenCommandServerSocket);

            if (server == null || server.isClosed()) {
                try {
                    server = new ServerSocket();
                } catch (IOException e) {
                    return new Status(IStatus.ERROR,
                            RemoteCommandPlugin.PLUGIN_ID, null, e);
                }
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                monitor.worked(20);

                InetAddress localAddress = null;
                int port = defaultPort;
                try {
                    server.bind(new InetSocketAddress(localAddress, port),
                            defaultBacklog);
                } catch (IOException e) {
                    if (monitor.isCanceled())
                        return Status.CANCEL_STATUS;
                    if (port != 0 && triesEphemeralPort) {
                        try {
                            server.bind(new InetSocketAddress(localAddress, 0),
                                    defaultBacklog);
                        } catch (IOException e1) {
                            if (monitor.isCanceled())
                                return Status.CANCEL_STATUS;
                            return new Status(IStatus.ERROR,
                                    RemoteCommandPlugin.PLUGIN_ID, null, e);
                        }
                    } else {
                        return new Status(IStatus.ERROR,
                                RemoteCommandPlugin.PLUGIN_ID, null, e);
                    }
                }

                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;
                monitor.worked(60);

            } else {
                monitor.worked(70);
            }

            if (info == null) {
                info = new SocketCommandServiceInfo();
                info.setAddress(new SocketAddress(server.getInetAddress()
                        .getHostName(), server.getLocalPort()));
                info.setName(System.getProperty("user.name")); //$NON-NLS-1$
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            monitor.worked(10);

            if (thread == null) {
                thread = new Thread(serverRunner, getLoopThreadName());
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            monitor.worked(10);

            monitor.done();
            return Status.OK_STATUS;
        }
    }

    /**
     * Undeploys this command server. First, the daemon looping thread will be
     * interrupted. Then the server socket opened by
     * {@link #deploy(IProgressMonitor)} will be closed.
     * 
     * @param monitor
     *            the progress monitor
     * @return the status of the undeployment
     */
    public IStatus undeploy(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);
        monitor.subTask(Messages.SocketCommandServer_OperationLock);

        synchronized (lock) {
            monitor.subTask(Messages.SocketCommandServer_CloseCommandServerSocket);

            Thread oldThread = thread;
            ServerSocket oldServer = server;
            thread = null;
            server = null;
            info = null;
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            monitor.worked(10);

            if (oldThread != null) {
                oldThread.interrupt();
            }
            if (oldServer != null) {
                try {
                    oldServer.close();
                } catch (IOException e) {
                    RemoteCommandPlugin.log(
                            "Could not stop local command server socket.", e); //$NON-NLS-1$
                }
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            monitor.worked(90);

            socketPool.clear();

            monitor.done();
            return Status.OK_STATUS;
        }
    }

    public ICommandServiceInfo getRegisteringInfo() {
        return info;
    }

    /**
     * Returns the socket pool owned by this command server. A socket pool
     * maintains a list of all active sockets and provides a method to close all
     * active sockets.
     * 
     * @return the socket pool
     */
    public SocketPool getSocketPool() {
        return socketPool;
    }

    /**
     * Handles the incoming accepted socket. The default implementation simply
     * delegates the handling process to an {@link IncomingSocketCommandHandler}
     * . Subclasses may extend this method to provide their own implementation.
     * 
     * @param socket
     *            the accepted socket to handle, never <code>null</code>
     */
    protected void runIncomingCommandHandler(Socket socket) {
        createIncomingCommandHandler(socket).schedule();
    }

    /**
     * Creates a socket command handler for the given accepted socket. The
     * default implementation returns an {@link IncomingSocketCommandHandler}.
     * Subclasses may extend this method to provide their own implementation.
     * 
     * @param socket
     *            the accepted socket to handle, never <code>null</code>
     * @return a socket command handler, never <code>null</code>
     */
    protected Job createIncomingCommandHandler(Socket socket) {
        IncomingSocketCommandHandler handler = new IncomingSocketCommandHandler(
                socket);
        handler.setSocketPool(getSocketPool());
        return handler;
    }

    /**
     * Returns the name of the command server loop thread. Subclasses may extend
     * this method to provide their own name for the loop thread.
     * 
     * @return a name, never <code>null</code>
     */
    protected String getLoopThreadName() {
        return "SocketCommandServerLoop"; //$NON-NLS-1$
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        if (adapter == SocketPool.class)
            return getSocketPool();
        return null;
    }

}
