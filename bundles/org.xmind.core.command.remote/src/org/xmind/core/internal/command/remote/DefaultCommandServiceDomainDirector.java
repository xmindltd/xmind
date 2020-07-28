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
package org.xmind.core.internal.command.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceDomainDirector;

/**
 * @author Frank Shaka
 */
public class DefaultCommandServiceDomainDirector implements
        ICommandServiceDomainDirector {

    private ICommandServiceDomain domain;

    private int connectionCount = 0;

    private int status = INACTIVE;

    private Object statusLock = new Object();

    public DefaultCommandServiceDomainDirector() {
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

    public IStatus connect(IProgressMonitor monitor) {
        boolean shouldWait = false;
        synchronized (statusLock) {
            if (status == ACTIVE) {
                connectionCount++;
                monitor.done();
                return Status.OK_STATUS;
            } else if (status == INACTIVE) {
                status = ACTIVATING;
            } else if (status == ACTIVATING) {
                shouldWait = true;
            } else {
                return Status.CANCEL_STATUS;
            }
        }

        monitor.beginTask(NLS.bind(
                Messages.DefaultCommandServiceDomainDirector_ConnectionRemoteCommand,
                getDomain().getName()), 1);

        if (shouldWait) {
            try {
                while (true) {
                    synchronized (statusLock) {
                        if (status == ACTIVE) {
                            connectionCount++;
                            monitor.done();
                            return Status.OK_STATUS;
                        } else if (status == INACTIVE || status == DEACTIVATING) {
                            return Status.CANCEL_STATUS;
                        }
                    }
                    Thread.sleep(5);
                }
            } catch (InterruptedException e) {
                return Status.CANCEL_STATUS;
            }
        } else {
            IStatus activated;
            try {
                activated = activate(new SubProgressMonitor(monitor, 1));
            } catch (Throwable e) {
                activated = new Status(IStatus.ERROR,
                        RemoteCommandPlugin.PLUGIN_ID, null, e);
            }
            if (activated != null && !activated.isOK()) {
                synchronized (statusLock) {
                    status = DEACTIVATING;
                }
                try {
                    deactivate(new NullProgressMonitor());
                } catch (Throwable e) {
                    RemoteCommandPlugin.log(
                            "Failed to deactivate command service domain.", e); //$NON-NLS-1$
                }
                synchronized (statusLock) {
                    status = INACTIVE;
                    return activated;
                }
            } else {
                synchronized (statusLock) {
                    status = ACTIVE;
                    connectionCount++;
                    monitor.done();
                    return Status.OK_STATUS;
                }
            }
        }
    }

    public IStatus disconnect(IProgressMonitor monitor) {
        synchronized (statusLock) {
            if (status == INACTIVE || status == DEACTIVATING) {
                monitor.done();
                return Status.OK_STATUS;
            } else if (status == ACTIVATING) {
                return Status.CANCEL_STATUS;
            } else {
                connectionCount--;
                if (connectionCount > 0)
                    return Status.OK_STATUS;
                status = DEACTIVATING;
            }
        }

        monitor.beginTask(NLS.bind(
                Messages.DefaultCommandServiceDomainDirector_DisconnectionRemoteCommand,
                getDomain().getName()), 1);

        IStatus deactivated;
        try {
            deactivated = deactivate(new SubProgressMonitor(monitor, 1));
        } catch (Throwable e) {
            deactivated = new Status(IStatus.ERROR,
                    RemoteCommandPlugin.PLUGIN_ID, null, e);
        }
        synchronized (statusLock) {
            status = INACTIVE;
        }
        return deactivated;
    }

    /**
     * @param monitor
     * @return
     */
    private IStatus activate(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        IProgressMonitor subMonitor;

        // Activate remote command service discoverer:
        subMonitor = new SubProgressMonitor(monitor, 40);
        IStatus activated = getDomain().getRemoteCommandServiceDiscoverer()
                .activate(subMonitor);
        if (activated != null && !activated.isOK())
            return activated;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        // Deploy local command server:
        subMonitor = new SubProgressMonitor(monitor, 30);
        IStatus deployed = getDomain().getCommandServer().deploy(subMonitor);
        if (deployed != null && !deployed.isOK())
            return deployed;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        // Register local command server as a command service:
        getDomain().getCommandServerAdvertiser().setRegisteringInfo(
                getDomain().getCommandServer().getRegisteringInfo());
        subMonitor = new SubProgressMonitor(monitor, 30);
        IStatus registered = getDomain().getCommandServerAdvertiser().register(
                subMonitor);
        if (registered != null && !registered.isOK())
            return registered;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return Status.OK_STATUS;
    }

    /**
     * @param nullProgressMonitor
     * @return
     */
    private IStatus deactivate(IProgressMonitor monitor) {
        monitor.beginTask(null, 100);

        IProgressMonitor subMonitor;

        // Unregister local command server as a command service:
        subMonitor = new SubProgressMonitor(monitor, 30);
        IStatus unregistered = getDomain().getCommandServerAdvertiser()
                .unregister(subMonitor);
        if (unregistered != null && !unregistered.isOK())
            return unregistered;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        // Undeploy local command server:
        subMonitor = new SubProgressMonitor(monitor, 30);
        IStatus undeployed = getDomain().getCommandServer()
                .undeploy(subMonitor);
        if (undeployed != null && !undeployed.isOK())
            return undeployed;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        // Deactivate remote command service discoverer:
        subMonitor = new SubProgressMonitor(monitor, 40);
        IStatus deactivated = getDomain().getRemoteCommandServiceDiscoverer()
                .deactivate(subMonitor);
        if (deactivated != null && !deactivated.isOK())
            return deactivated;
        if (subMonitor.isCanceled())
            return Status.CANCEL_STATUS;
        subMonitor.done();

        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        monitor.done();
        return Status.OK_STATUS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.command.remote.ICommandServiceDomainDirector#getStatus()
     */
    public int getStatus() {
        synchronized (statusLock) {
            return status;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        return null;
    }

}
