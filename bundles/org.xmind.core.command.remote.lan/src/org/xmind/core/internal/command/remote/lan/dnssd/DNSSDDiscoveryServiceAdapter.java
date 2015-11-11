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
/**
 * 
 */
package org.xmind.core.internal.command.remote.lan.dnssd;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.remote.AbstractRemoteCommandServiceDiscoverer;
import org.xmind.core.command.remote.ICommandServerAdvertiser;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.IRemoteCommandServiceDiscoverer;
import org.xmind.core.command.remote.Identifier;
import org.xmind.core.command.remote.socket.ISocketAddress;
import org.xmind.core.command.remote.socket.SocketPool;
import org.xmind.core.internal.command.remote.lan.LANRemoteCommandPlugin;
import org.xmind.core.internal.command.remote.lan.Messages;
import org.xmind.core.internal.command.remote.lan.Util;

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.dnssd.TXTRecord;

/**
 * @author Frank Shaka
 */
public class DNSSDDiscoveryServiceAdapter extends
        AbstractRemoteCommandServiceDiscoverer implements
        ICommandServerAdvertiser, IRemoteCommandServiceDiscoverer,
        BrowseListener {

    private static final String OPTION = "/debug/dnssd"; //$NON-NLS-1$

    private static boolean DEBUGGING = LANRemoteCommandPlugin
            .isDebugging(OPTION);

    private static final String SERVICE_TYPE = "_xmind._tcp"; //$NON-NLS-1$

    protected static final String NAME = "name"; //$NON-NLS-1$

    private DNSSDService browser = null;

    private Object lock = new Object();

    private boolean active = false;

    private boolean deactivating = false;

    private boolean unregistering = false;

    private ICommandServiceInfo registeringInfo = null;

    private DNSSDCommandServiceInfo registeredInfo = null;

    private AsyncQueuedExecutor asyncQueue = null;

    private Object queueLock = new Object();

    private RegisterListener registerListener = new RegisterListener() {

        public void operationFailed(DNSSDService service, int errorCode) {
            LANRemoteCommandPlugin.log("DNSSD registration failed: ErrorCode=" //$NON-NLS-1$
                    + errorCode, null);
        }

        public void serviceRegistered(final DNSSDRegistration registration,
                final int flags, final String serviceName,
                final String regType, final String domain) {
            if (DEBUGGING)
                System.out.println("Local DNSSD service registered: " //$NON-NLS-1$
                        + serviceName + " regType=" + regType //$NON-NLS-1$
                        + " domain=" + domain); //$NON-NLS-1$

            enqueue(new Runnable() {
                public void run() {
                    long start, end;

                    if (active && !deactivating && registeredInfo != null) {
                        // We have to explicitly refresh all remote command services
                        // as their identifiers may have been changed since we
                        // registered ours.
                        if (browser != null) {
                            start = System.currentTimeMillis();
                            browser.stop();
                            end = System.currentTimeMillis();
                            if (DEBUGGING)
                                System.out.println("DNSSD browser stopped: (" //$NON-NLS-1$
                                        + (end - start) + " ms)"); //$NON-NLS-1$
                            browser = null;
                        }

                        start = System.currentTimeMillis();
                        IRemoteCommandService[] services = getRemoteCommandServices();
                        for (int i = 0; i < services.length; i++) {
                            remoteCommandServiceDropped(services[i]);
                        }
                        end = System.currentTimeMillis();
                        if (DEBUGGING)
                            System.out
                                    .println("All DNSSD remote command services removed: " //$NON-NLS-1$
                                            + services.length + " (" //$NON-NLS-1$
                                            + (end - start) + " ms)"); //$NON-NLS-1$

                        if (browser == null) {
                            DNSSDService service;
                            try {
                                start = System.currentTimeMillis();
                                service = DNSSD.browse(SERVICE_TYPE,
                                        DNSSDDiscoveryServiceAdapter.this);
                                end = System.currentTimeMillis();
                                if (DEBUGGING)
                                    System.out
                                            .println("DNSSD browse listener installed: (" //$NON-NLS-1$
                                                    + (end - start) + " ms)"); //$NON-NLS-1$
                                browser = service;
                            } catch (DNSSDException e) {
                                LANRemoteCommandPlugin.log(
                                        "Failed to restart DNSSD browser.", e); //$NON-NLS-1$
                            }
                        }
                    }

                    // Remove any remote DNSSD service that has conflicting service name:
                    IRemoteCommandService conflictingRemoteCommandService = findRemoteCommandService(serviceName);
                    if (conflictingRemoteCommandService != null) {
                        if (DEBUGGING)
                            System.out
                                    .println("Removing conflicting remote command service: " //$NON-NLS-1$
                                            + serviceName);
                        remoteCommandServiceDropped(conflictingRemoteCommandService);
                    }

                    ICommandServiceInfo source = registeredInfo != null ? registeredInfo
                            : registeringInfo;
                    DNSSDCommandServiceInfo info = new DNSSDCommandServiceInfo(
                            source, registration, flags, serviceName, regType,
                            domain);
                    info.setId(new Identifier(getDomain().getId(), serviceName));
                    registeredInfo = info;
                }
            });
        }
    };

    public DNSSDDiscoveryServiceAdapter() {
    }

    public IStatus activate(IProgressMonitor monitor) {
        long start, end;
        if (DEBUGGING)
            System.out.println("Activating DNSSD discovery service."); //$NON-NLS-1$

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_OperationLock);
        deactivating = false;

        synchronized (lock) {
            monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_ActivateRemoteCommand);

            synchronized (queueLock) {
                if (asyncQueue == null) {
                    asyncQueue = new AsyncQueuedExecutor(
                            "DNSSDEventHandlingQueue"); //$NON-NLS-1$
                }
            }

            if (browser == null) {
                DNSSDService service;
                try {
                    start = System.currentTimeMillis();
                    service = DNSSD.browse(SERVICE_TYPE, this);
                    end = System.currentTimeMillis();
                    if (DEBUGGING)
                        System.out.println("DNSSD browse listener installed: (" //$NON-NLS-1$
                                + (end - start) + " ms)"); //$NON-NLS-1$
                    browser = service;
                } catch (DNSSDException e) {
                    return new Status(IStatus.ERROR, LANRemoteCommandPlugin.ID,
                            null, e);
                } catch (Throwable e) {
                    return new Status(
                            IStatus.ERROR,
                            LANRemoteCommandPlugin.ID,
                            23333,
                            Messages.DNSSDDiscoveryServiceAdapter_RemoteCommandServiceDiscoverer,
                            e);
                }
            }
            monitor.worked(90);

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            active = true;
            monitor.done();
            if (DEBUGGING)
                System.out.println("Activated DNSSD discovery service."); //$NON-NLS-1$
            return Status.OK_STATUS;
        }
    }

    public IStatus deactivate(IProgressMonitor monitor) {
        long start, end;
        if (DEBUGGING)
            System.out.println("Deactivating DNSSD discovery service."); //$NON-NLS-1$

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_OperationLock);
        deactivating = true;

        synchronized (lock) {
            monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_DeactivateRemoteCommand);

            start = System.currentTimeMillis();
            IRemoteCommandService[] services = getRemoteCommandServices();
            for (int i = 0; i < services.length; i++) {
                remoteCommandServiceDropped(services[i]);
            }
            end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out
                        .println("All DNSSD remote command services removed: " //$NON-NLS-1$
                                + services.length + " (" + (end - start) //$NON-NLS-1$
                                + " ms)"); //$NON-NLS-1$

            if (browser != null) {
                start = System.currentTimeMillis();
                browser.stop();
                end = System.currentTimeMillis();
                if (DEBUGGING)
                    System.out.println("DNSSD browser stopped: (" //$NON-NLS-1$
                            + (end - start) + " ms)"); //$NON-NLS-1$
                browser = null;
            }

            synchronized (queueLock) {
                if (asyncQueue != null) {
                    asyncQueue.dispose();
                    asyncQueue = null;
                }
            }

            active = false;
            monitor.done();
            if (DEBUGGING)
                System.out.println("Deactivated DNSSD discovery service."); //$NON-NLS-1$
            return Status.OK_STATUS;
        }
    }

    public IStatus refresh(IProgressMonitor monitor) {
        long start, end;

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_OperationLock);

        synchronized (lock) {
            monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_RefreshRemoteCommand);

            if (browser != null) {
                start = System.currentTimeMillis();
                browser.stop();
                end = System.currentTimeMillis();
                if (DEBUGGING)
                    System.out.println("DNSSD browser stopped: (" //$NON-NLS-1$
                            + (end - start) + " ms)"); //$NON-NLS-1$
                browser = null;
            }
            monitor.worked(30);

            start = System.currentTimeMillis();
            IRemoteCommandService[] services = getRemoteCommandServices();
            for (int i = 0; i < services.length; i++) {
                remoteCommandServiceDropped(services[i]);
            }
            end = System.currentTimeMillis();
            if (DEBUGGING)
                System.out
                        .println("All DNSSD remote command services removed: " //$NON-NLS-1$ 
                                + services.length + " (" //$NON-NLS-1$
                                + (end - start) + " ms)"); //$NON-NLS-1$
            monitor.worked(40);

            if (browser == null) {
                DNSSDService service;
                try {
                    start = System.currentTimeMillis();
                    service = DNSSD.browse(SERVICE_TYPE, this);
                    end = System.currentTimeMillis();
                    if (DEBUGGING)
                        System.out.println("DNSSD browse listener installed: (" //$NON-NLS-1$
                                + (end - start) + " ms)"); //$NON-NLS-1$
                    browser = service;
                } catch (DNSSDException e) {
                    return new Status(IStatus.ERROR, LANRemoteCommandPlugin.ID,
                            null, e);
                }
            }
            monitor.worked(30);

            monitor.done();
            return Status.OK_STATUS;
        }
    }

    public void operationFailed(DNSSDService service, int errorCode) {
        if (DEBUGGING) {
            System.out.println("DNSSD operation failed: (ErrorCode=" //$NON-NLS-1$
                    + errorCode + ") " + service); //$NON-NLS-1$
        } else {
            LANRemoteCommandPlugin.log("DNSSD operation failed: (ErrorCode=" //$NON-NLS-1$
                    + errorCode + ") " + service, null); //$NON-NLS-1$
        }
    }

    public void serviceFound(final DNSSDService browser, final int flags,
            final int ifIndex, final String serviceName, final String regType,
            final String domain) {
        if (DEBUGGING)
            System.out.println("DNSSD remote service found: " + serviceName //$NON-NLS-1$
                    + " regType=" + regType //$NON-NLS-1$
                    + " domain=" + domain); //$NON-NLS-1$
        enqueue(new Runnable() {
            public void run() {
                if (!active || deactivating) {
                    if (DEBUGGING)
                        System.out
                                .println("DNSSD service found but discoverer is not active."); //$NON-NLS-1$
                    return;
                }
                if (registeredInfo != null
                        && registeredInfo.serviceName.equals(serviceName)) {
                    if (DEBUGGING)
                        System.out
                                .println("Local DNSSD service found: " + serviceName); //$NON-NLS-1$
                    return;
                }

                if (DEBUGGING)
                    System.out
                            .println("Resolving and adding remote DNSSD service: " //$NON-NLS-1$
                                    + serviceName);
                new RemoteCommandServiceAdder(flags, ifIndex, serviceName,
                        regType, domain);
            }
        });
    }

    private class RemoteCommandServiceAdder extends ResolveAdapter {

        private DNSSDService resolver;

        /**
         * @param flags
         * @param ifIndex
         * @param serviceName
         * @param regType
         * @param domain
         */
        public RemoteCommandServiceAdder(int flags, int ifIndex,
                String serviceName, String regType, String domain) {
            super(flags, ifIndex, serviceName, regType, domain);
            try {
                this.resolver = DNSSD.resolve(flags, ifIndex, serviceName,
                        regType, domain, this);
            } catch (DNSSDException e) {
                this.resolver = null;
                LANRemoteCommandPlugin.log(null, e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.apple.dnssd.ResolveListener#serviceResolved(com.apple.dnssd.
         * DNSSDService, int, int, java.lang.String, java.lang.String, int,
         * com.apple.dnssd.TXTRecord)
         */
        public void serviceResolved(final DNSSDService resolver,
                final int flags, final int ifIndex, final String fullName,
                final String hostName, final int port, final TXTRecord txtRecord) {
            try {
                enqueue(new Runnable() {
                    public void run() {
                        if (!active || deactivating)
                            return;
                        if (DEBUGGING)
                            System.out
                                    .println("DNSSD remote service resolved and about to be added: " //$NON-NLS-1$
                                            + serviceName);

                        try {
                            DNSSDRemoteCommandService remoteCommandService = new DNSSDRemoteCommandService(
                                    getFlags(), getIfIndex(), getServiceName(),
                                    getRegType(), getDomainName(), hostName,
                                    port, txtRecord, getDomain().getId());

                            remoteCommandService
                                    .setSocketPool((SocketPool) getDomain()
                                            .getCommandServer().getAdapter(
                                                    SocketPool.class));
                            remoteCommandServiceDiscovered(remoteCommandService);
                            if (DEBUGGING)
                                System.out
                                        .println("DNSSD remote service resolved and added: " //$NON-NLS-1$
                                                + serviceName);
                        } catch (Throwable e) {
                            LANRemoteCommandPlugin.log(null, e);
                        }
                    }
                });
            } finally {
                stopResolver();
            }
        }

        private void stopResolver() {
            DNSSDService theResolver = this.resolver;
            this.resolver = null;
            if (theResolver != null) {
                theResolver.stop();
            }
        }

    }

    public void serviceLost(final DNSSDService browser, final int flags,
            final int ifIndex, final String serviceName, final String regType,
            final String domain) {
        if (DEBUGGING)
            System.out.println("DNSSD remote service lost: " + serviceName //$NON-NLS-1$
                    + " regType=" + regType //$NON-NLS-1$
                    + " domain=" + domain); //$NON-NLS-1$
        enqueue(new Runnable() {
            public void run() {
                if (!active || deactivating) {
                    if (DEBUGGING)
                        System.out
                                .println("DNSSD remote service lost but discoverer is not active."); //$NON-NLS-1$
                    return;
                }
                IRemoteCommandService service = findRemoteCommandService(serviceName);
                if (service != null) {
                    remoteCommandServiceDropped(service);
                }
                if (DEBUGGING)
                    System.out.println("DNSSD remote service removed: " //$NON-NLS-1$
                            + serviceName);
            }
        });
    }

    public void setRegisteringInfo(ICommandServiceInfo info) {
        this.registeringInfo = info;
    }

    public ICommandServiceInfo getRegisteredInfo() {
        return registeredInfo;
    }

    public IStatus register(IProgressMonitor monitor) {
        long start, end;

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_OperationLock);
        unregistering = false;
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        synchronized (lock) {
            monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_RegisterLocalCommandServer);
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            if (registeredInfo == null) {
                ISocketAddress address = (ISocketAddress) registeringInfo
                        .getAdapter(ISocketAddress.class);
                if (address == null)
                    return new Status(
                            IStatus.ERROR,
                            LANRemoteCommandPlugin.ID,
                            Messages.DNSSDDiscoveryServiceAdapter_RegisterSocketFailed);
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                int port = address.getPort();
                TXTRecord txtRecord = new TXTRecord();
                txtRecord.set(NAME, registeringInfo.getName());
                extractMetadata(registeringInfo, txtRecord,
                        ICommandServiceInfo.VERSION);
                extractMetadata(registeringInfo, txtRecord,
                        ICommandServiceInfo.CLIENT_NAME);
                extractMetadata(registeringInfo, txtRecord,
                        ICommandServiceInfo.CLIENT_SYMBOLIC_NAME);
                extractMetadata(registeringInfo, txtRecord,
                        ICommandServiceInfo.CLIENT_VERSION);
                extractMetadata(registeringInfo, txtRecord,
                        ICommandServiceInfo.CLIENT_BUILD_ID);
                if (DEBUGGING)
                    System.out
                            .println("DNSSD local service registration about to start: port=" + port //$NON-NLS-1$
                                    + " regType=" + SERVICE_TYPE //$NON-NLS-1$
                                    + " txtRecord=" + txtRecord); //$NON-NLS-1$
                if (monitor.isCanceled())
                    return Status.CANCEL_STATUS;

                start = System.currentTimeMillis();
                try {
                    DNSSD.register(0, 0, null, SERVICE_TYPE, null, null, port,
                            txtRecord, registerListener);
                } catch (DNSSDException e) {
                    return new Status(IStatus.ERROR, LANRemoteCommandPlugin.ID,
                            null, e);
                }
                try {
                    while (registeredInfo == null && !unregistering) {
                        if (monitor.isCanceled())
                            return Status.CANCEL_STATUS;
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                }
                if (unregistering)
                    return Status.CANCEL_STATUS;
                end = System.currentTimeMillis();
                if (DEBUGGING)
                    System.out.println("DNSSD local service registered: (" //$NON-NLS-1$
                            + (end - start) + " ms)"); //$NON-NLS-1$
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            monitor.done();
            return Status.OK_STATUS;
        }
    }

    public IStatus unregister(IProgressMonitor monitor) {
        long start, end;

        monitor.beginTask(null, 100);
        monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_OperationLock);
        unregistering = true;

        synchronized (lock) {
            monitor.subTask(Messages.DNSSDDiscoveryServiceAdapter_UnregisteredLocalCommandServer);
            if (registeredInfo != null) {
                start = System.currentTimeMillis();
                registeredInfo.registration.stop();
                end = System.currentTimeMillis();
                if (DEBUGGING)
                    System.out
                            .println("DNSSD local service registration stopped: (" //$NON-NLS-1$
                                    + (end - start) + " ms)"); //$NON-NLS-1$
                registeredInfo = null;
            }
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;

            monitor.done();
            return Status.OK_STATUS;
        }
    }

    private static void extractMetadata(ICommandServiceInfo info,
            TXTRecord txtRecord, String key) {
        String value = info.getMetadata(key);
        if (value != null) {
            txtRecord.set(key, Util.encode(value));
        }
    }

    private void enqueue(Runnable task) {
        synchronized (queueLock) {
            if (asyncQueue != null) {
                asyncQueue.execute(task);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        return null;
    }
}
