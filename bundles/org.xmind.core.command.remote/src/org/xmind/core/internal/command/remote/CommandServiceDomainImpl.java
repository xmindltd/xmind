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

import org.eclipse.core.runtime.IConfigurationElement;
import org.xmind.core.command.remote.ICommandServer;
import org.xmind.core.command.remote.ICommandServerAdvertiser;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceDomainDirector;
import org.xmind.core.command.remote.IDomainServiceFactory;
import org.xmind.core.command.remote.IRemoteCommandServiceDiscoverer;

/**
 * @author Frank Shaka
 */
public class CommandServiceDomainImpl implements ICommandServiceDomain,
        IDomainsExtensionConstants {

    private final CommandServiceDomainManagerImpl manager;

    private final String id;

    private final IConfigurationElement element;

    private ICommandServiceDomainDirector director = null;

    private ICommandServer server = null;

    private ICommandServerAdvertiser advertiser = null;

    private IRemoteCommandServiceDiscoverer discoverer = null;

    /**
     * 
     */
    public CommandServiceDomainImpl(CommandServiceDomainManagerImpl manager,
            String id, IConfigurationElement element) {
        this.manager = manager;
        this.id = id;
        this.element = element;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return element.getAttribute(ATT_NAME);
    }

    private <T> T createDomainService(String attrName, Class<T> serviceClass) {
        String factoryId = element.getAttribute(attrName);
        if (factoryId != null) {
            IDomainServiceFactory factory = manager
                    .getServiceFactory(factoryId);
            if (factory != null) {
                Object service = factory.createDomainService(this);
                if (service != null
                        && serviceClass.isAssignableFrom(service.getClass())) {
                    return serviceClass.cast(service);
                }
            }
        }
        if (serviceClass == ICommandServiceDomainDirector.class) {
            return serviceClass.cast(new DefaultCommandServiceDomainDirector());
        }
        return serviceClass.cast(NullDomainService.getDefault());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.ICommandServiceDomain#getDirector()
     */
    public synchronized ICommandServiceDomainDirector getDirector() {
        if (director == null) {
            director = createDomainService(ATT_DIRECTOR,
                    ICommandServiceDomainDirector.class);
            director.init(this);
        }
        return director;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandServiceDomain#
     * getLocalCommandServerDeployer()
     */
    public synchronized ICommandServer getCommandServer() {
        if (server == null) {
            server = createDomainService(ATT_SERVER, ICommandServer.class);
            server.init(this);
        }
        return server;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.ICommandServiceDomain#
     * getLocalCommandServerAdvertiser()
     */
    public synchronized ICommandServerAdvertiser getCommandServerAdvertiser() {
        if (advertiser == null) {
            advertiser = createDomainService(ATT_ADVERTISER,
                    ICommandServerAdvertiser.class);
            advertiser.init(this);
        }
        return advertiser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandServiceDomain#
     * getRemoteCommandServiceDiscoverer()
     */
    public synchronized IRemoteCommandServiceDiscoverer getRemoteCommandServiceDiscoverer() {
        if (discoverer == null) {
            discoverer = createDomainService(ATT_DISCOVERER,
                    IRemoteCommandServiceDiscoverer.class);
            discoverer.init(this);
        }
        return discoverer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof CommandServiceDomainImpl))
            return false;
        CommandServiceDomainImpl that = (CommandServiceDomainImpl) obj;
        return this.id.equals(that.id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CommandServiceDomain#" + this.id; //$NON-NLS-1$
    }
}
