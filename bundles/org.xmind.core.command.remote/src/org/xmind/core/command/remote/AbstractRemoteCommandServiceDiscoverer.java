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
package org.xmind.core.command.remote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.xmind.core.internal.command.remote.RemoteCommandPlugin;

/**
 * A base implementation of remote command service discoverer.
 * 
 * @author Frank Shaka
 */
public abstract class AbstractRemoteCommandServiceDiscoverer implements
        IRemoteCommandServiceDiscoverer {

    private ICommandServiceDomain domain;

    private Map<String, IRemoteCommandService> cache = new HashMap<String, IRemoteCommandService>();

    private Object cacheLock = new Object();

    private ListenerList listeners = new ListenerList();

    /**
     * 
     */
    public AbstractRemoteCommandServiceDiscoverer() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.command.remote.IDomainService#init(org.xmind.core.command
     * .remote.ICommandServiceDomain)
     */
    public void init(ICommandServiceDomain domain) {
        this.domain = domain;
    }

    /**
     * @return the domain
     */
    public ICommandServiceDomain getDomain() {
        return domain;
    }

    public IRemoteCommandService[] getRemoteCommandServices() {
        synchronized (cacheLock) {
            return cache.values().toArray(
                    new IRemoteCommandService[cache.size()]);
        }
    }

    public IRemoteCommandService findRemoteCommandService(String serviceName) {
        synchronized (cacheLock) {
            return cache.get(serviceName);
        }
    }

    public void addRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
        listeners.add(listener);
    }

    public void removeRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
        listeners.remove(listener);
    }

    protected void remoteCommandServiceDiscovered(IRemoteCommandService service) {
        IRemoteCommandService serviceRemovedById = null;
        synchronized (cacheLock) {
            ICommandServiceInfo info = service.getInfo();
            String serviceName = info.getId().getName();
            serviceRemovedById = cache.remove(serviceName);
            cache.put(serviceName, service);
        }
        synchronized (listeners) {
            if (serviceRemovedById != null) {
                fireRemoteCommandServiceDropped(serviceRemovedById);
            }
            fireRemoteCommandServiceDiscovered(service);
        }
    }

    protected void remoteCommandServiceDropped(IRemoteCommandService service) {
        IRemoteCommandService serviceRemovedById = null;
        synchronized (cacheLock) {
            ICommandServiceInfo info = service.getInfo();
            String serviceName = info.getId().getName();
            serviceRemovedById = cache.remove(serviceName);
        }
        synchronized (listeners) {
            if (serviceRemovedById != null) {
                fireRemoteCommandServiceDropped(serviceRemovedById);
            }
        }
    }

    protected void fireRemoteCommandServiceDiscovered(
            final IRemoteCommandService service) {
        Object[] theListeners = listeners.getListeners();
        for (int i = 0; i < theListeners.length; i++) {
            final IRemoteCommandServiceListener listener = (IRemoteCommandServiceListener) theListeners[i];
            SafeRunner.run(new ISafeRunnable() {
                public void run() throws Exception {
                    listener.remoteCommandServiceDiscovered(service);
                }

                public void handleException(Throwable exception) {
                    RemoteCommandPlugin.log(null, exception);
                }
            });
        }
    }

    protected void fireRemoteCommandServiceDropped(
            final IRemoteCommandService service) {
        Object[] theListeners = listeners.getListeners();
        for (int i = 0; i < theListeners.length; i++) {
            final IRemoteCommandServiceListener listener = (IRemoteCommandServiceListener) theListeners[i];
            SafeRunner.run(new ISafeRunnable() {
                public void run() throws Exception {
                    listener.remoteCommandServiceDropped(service);
                }

                public void handleException(Throwable exception) {
                    RemoteCommandPlugin.log(null, exception);
                }
            });
        }
    }

}
