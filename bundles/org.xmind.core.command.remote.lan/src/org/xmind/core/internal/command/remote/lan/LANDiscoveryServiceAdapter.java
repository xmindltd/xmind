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
package org.xmind.core.internal.command.remote.lan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.remote.ICommandServerAdvertiser;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.IRemoteCommandServiceDiscoverer;
import org.xmind.core.command.remote.IRemoteCommandServiceListener;

/**
 * 
 * @author Frank Shaka
 */
public class LANDiscoveryServiceAdapter implements ICommandServerAdvertiser,
        IRemoteCommandServiceDiscoverer {

    private static final IRemoteCommandService[] NO_SERVICES = new IRemoteCommandService[0];

    private ICommandServerAdvertiser advertiserImpl;

    private IRemoteCommandServiceDiscoverer discovererImpl;

    private IStatus fatalStatus = null;

    public LANDiscoveryServiceAdapter() {
        try {
            Class.forName("com.apple.dnssd.DNSSD"); //$NON-NLS-1$
        } catch (UnsatisfiedLinkError e) {
            // Most possibly thrown by System.loadLibrary('jdns_sd').
            fatalStatus = new Status(IStatus.ERROR, LANRemoteCommandPlugin.ID,
                    23333, "Bonjour installation is not found or damaged.", //$NON-NLS-1$
                    e);
        } catch (ClassNotFoundException e) {
            fatalStatus = new Status(
                    IStatus.ERROR,
                    LANRemoteCommandPlugin.ID,
                    23331,
                    "DNSSD Java Client Library (com.apple.dnssd) is not found.", //$NON-NLS-1$
                    e);
        } catch (Throwable e) {
            fatalStatus = new Status(
                    IStatus.ERROR,
                    LANRemoteCommandPlugin.ID,
                    23332,
                    "DNSSD Java Client Library (com.apple.dnssd) fails to be loaded: " + e.getMessage(), //$NON-NLS-1$
                    e);
        }

        Object impl = null;
        if (fatalStatus == null) {
            try {
                Class<?> implClass = Class
                        .forName("org.xmind.core.internal.command.remote.lan.dnssd.DNSSDDiscoveryServiceAdapter"); //$NON-NLS-1$
                impl = implClass.newInstance();
            } catch (Throwable e) {
                LANRemoteCommandPlugin
                        .log("LANDiscoveryService: Failed to load implementation class: org.xmind.core.internal.command.remote.lan.dnssd.DNSSDDiscoveryServiceAdapter", //$NON-NLS-1$
                                e);
            }
        }

        this.advertiserImpl = (ICommandServerAdvertiser) impl;
        this.discovererImpl = (IRemoteCommandServiceDiscoverer) impl;
    }

    public void init(ICommandServiceDomain domain) {
        if (advertiserImpl != null)
            advertiserImpl.init(domain);
    }

    public IStatus activate(IProgressMonitor monitor) {
        if (fatalStatus != null)
            return fatalStatus;
        if (discovererImpl != null)
            return discovererImpl.activate(monitor);
        return Status.OK_STATUS;
    }

    public IStatus deactivate(IProgressMonitor monitor) {
        if (discovererImpl != null)
            return discovererImpl.deactivate(monitor);
        return Status.OK_STATUS;
    }

    public IRemoteCommandService[] getRemoteCommandServices() {
        if (discovererImpl != null)
            return discovererImpl.getRemoteCommandServices();
        return NO_SERVICES;
    }

    public IRemoteCommandService findRemoteCommandService(String serviceName) {
        if (discovererImpl != null)
            return discovererImpl.findRemoteCommandService(serviceName);
        return null;
    }

    public void addRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
        if (discovererImpl != null)
            discovererImpl.addRemoteCommandServiceListener(listener);
    }

    public void removeRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
        if (discovererImpl != null)
            discovererImpl.removeRemoteCommandServiceListener(listener);
    }

    public IStatus refresh(IProgressMonitor monitor) {
        if (fatalStatus != null)
            return fatalStatus;
        if (discovererImpl != null)
            return discovererImpl.refresh(monitor);
        return Status.OK_STATUS;
    }

    public ICommandServiceInfo getRegisteredInfo() {
        if (advertiserImpl != null)
            return advertiserImpl.getRegisteredInfo();
        return null;
    }

    public IStatus register(IProgressMonitor monitor) {
        if (fatalStatus != null)
            return fatalStatus;
        if (advertiserImpl != null)
            return advertiserImpl.register(monitor);
        return Status.OK_STATUS;
    }

    public void setRegisteringInfo(ICommandServiceInfo info) {
        if (advertiserImpl != null)
            advertiserImpl.setRegisteringInfo(info);
    }

    public IStatus unregister(IProgressMonitor monitor) {
        if (advertiserImpl != null)
            return advertiserImpl.unregister(monitor);
        return Status.OK_STATUS;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        Object adapted = null;
        if (advertiserImpl != null) {
            adapted = advertiserImpl.getAdapter(adapter);
        }
        if (adapted != null)
            return adapted;
        if (discovererImpl != null) {
            adapted = discovererImpl.getAdapter(adapter);
        }
        return adapted;
    }

//    private static Class<?> loadClass(String className) {
//        if (className == null || "".equals(className)) //$NON-NLS-1$
//            return null;
//
//        int bundleSeparatorPosition = className.indexOf('/');
//        if (bundleSeparatorPosition < 0) {
//            try {
//                return Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                LANRemoteCommandPlugin.log(
//                        "Failed to load class: " + className, e); //$NON-NLS-1$
//            }
//        } else {
//            String bundleId = className.substring(0, bundleSeparatorPosition);
//            String classNameInBundle = className
//                    .substring(bundleSeparatorPosition + 1);
//            Bundle bundle = Platform.getBundle(bundleId);
//            if (bundle != null) {
//                try {
//                    return bundle.loadClass(classNameInBundle);
//                } catch (ClassNotFoundException e) {
//                    LANRemoteCommandPlugin.log("Failed to load class: " //$NON-NLS-1$
//                            + className, e);
//                }
//            }
//        }
//        return null;
//    }

}
