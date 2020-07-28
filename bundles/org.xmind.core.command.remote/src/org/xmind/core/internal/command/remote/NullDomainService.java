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
package org.xmind.core.internal.command.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.remote.CommandServiceInfo;
import org.xmind.core.command.remote.ICommandServer;
import org.xmind.core.command.remote.ICommandServerAdvertiser;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceDomainDirector;
import org.xmind.core.command.remote.ICommandServiceInfo;
import org.xmind.core.command.remote.IDomainServiceFactory;
import org.xmind.core.command.remote.IRemoteCommandService;
import org.xmind.core.command.remote.IRemoteCommandServiceDiscoverer;
import org.xmind.core.command.remote.IRemoteCommandServiceListener;

/**
 * @author Frank Shaka
 */
public class NullDomainService implements IDomainServiceFactory,
        ICommandServiceDomainDirector, ICommandServer,
        ICommandServerAdvertiser, IRemoteCommandServiceDiscoverer {

    private static final IRemoteCommandService[] NO_SERVICES = new IRemoteCommandService[0];

    private static NullDomainService singleton = null;

    private ICommandServiceInfo info = new CommandServiceInfo();

    /**
     * 
     */
    private NullDomainService() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.command.remote.IDomainService#init(org.xmind.core.command
     * .remote.ICommandServiceDomain)
     */
    public void init(ICommandServiceDomain domain) {
    }

    public IStatus activate(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IStatus deactivate(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IRemoteCommandService[] getRemoteCommandServices() {
        return NO_SERVICES;
    }

    public IRemoteCommandService findRemoteCommandService(String serviceName) {
        return null;
    }

    public void addRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
    }

    public void removeRemoteCommandServiceListener(
            IRemoteCommandServiceListener listener) {
    }

    public IStatus refresh(IProgressMonitor monitor) {
        monitor.done();
        return Status.OK_STATUS;
    }

    public void setRegisteringInfo(ICommandServiceInfo info) {
        this.info = info;
    }

    public IStatus register(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IStatus unregister(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IStatus deploy(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IStatus undeploy(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public ICommandServiceInfo getRegisteringInfo() {
        return info;
    }

    public ICommandServiceInfo getRegisteredInfo() {
        return info;
    }

    public IStatus connect(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public IStatus disconnect(IProgressMonitor monitor) {
        return Status.CANCEL_STATUS;
    }

    public int getStatus() {
        return INACTIVE;
    }

    public Object createDomainService(ICommandServiceDomain domain) {
        return this;
    }

    public static synchronized NullDomainService getDefault() {
        if (singleton == null) {
            singleton = new NullDomainService();
        }
        return singleton;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(Class adapter) {
        return null;
    }

}
