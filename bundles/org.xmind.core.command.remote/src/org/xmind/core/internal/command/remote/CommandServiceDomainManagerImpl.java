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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.remote.ICommandServiceDomain;
import org.xmind.core.command.remote.ICommandServiceDomainManager;
import org.xmind.core.command.remote.IDomainServiceFactory;

/**
 * @author Frank Shaka
 */
public class CommandServiceDomainManagerImpl implements
        ICommandServiceDomainManager, IDomainsExtensionConstants {

    private static final CommandServiceDomainManagerImpl singleton = new CommandServiceDomainManagerImpl();

    private static class DomainServiceFactoryDescriptor {

        private IConfigurationElement element;

        private IDomainServiceFactory factory = null;

        public DomainServiceFactoryDescriptor(IConfigurationElement element) {
            this.element = element;
        }

        public IDomainServiceFactory getFactory() {
            if (factory == null) {
                try {
                    factory = (IDomainServiceFactory) element
                            .createExecutableExtension(ATT_CLASS);
                } catch (CoreException e) {
                    RemoteCommandPlugin.log(null, e);
                    factory = NullDomainService.getDefault();
                }
            }
            return factory == NullDomainService.getDefault() ? null : factory;
        }
    }

    private Map<String, CommandServiceDomainImpl> domains = null;

    private Map<String, DomainServiceFactoryDescriptor> factories = null;

    /**
     * 
     */
    private CommandServiceDomainManagerImpl() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandServiceFramework#
     * getRemoteCommandServiceDomains()
     */
    public synchronized ICommandServiceDomain[] getCommandServiceDomains() {
        ensureLoaded();
        return domains.values().toArray(
                new ICommandServiceDomain[domains.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.command.remote.IRemoteCommandServiceFramework#
     * getRemoteCommandServiceDomain(java.lang.String)
     */
    public synchronized ICommandServiceDomain getCommandServiceDomain(
            String domainId) {
        ensureLoaded();
        return domains.get(domainId);
    }

    public IDomainServiceFactory getServiceFactory(String factoryId) {
        DomainServiceFactoryDescriptor factory = factories.get(factoryId);
        return factory == null ? null : factory.getFactory();
    }

    private void ensureLoaded() {
        if (domains == null) {
            IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
                    .getExtensionPoint(EXT_POINT);
            if (extensionPoint != null) {
                loadFromExtensions(extensionPoint.getExtensions());
            }
            if (domains == null)
                domains = new HashMap<String, CommandServiceDomainImpl>(1);
        }
    }

    private void loadFromExtensions(IExtension[] extensions) {
        for (int i = 0; i < extensions.length; i++) {
            loadFromExtensionElements(extensions[i].getConfigurationElements());
        }
    }

    private void loadFromExtensionElements(IConfigurationElement[] elements) {
        for (int i = 0; i < elements.length; i++) {
            loadFromExtensionElement(elements[i]);
        }
    }

    private void loadFromExtensionElement(IConfigurationElement element) {
        if (TAG_DOMAIN.equals(element.getName())) {
            loadRemoteCommandServiceDomain(element);
        } else if (TAG_SERVICE_FACTORY.equals(element.getName())) {
            loadServiceFactory(element);
        }
        loadFromExtensionElements(element.getChildren());
    }

    private void loadRemoteCommandServiceDomain(IConfigurationElement element) {
        String id = element.getAttribute(ATT_ID);
        if (id == null || "".equals(id)) {//$NON-NLS-1$
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid domain extension: (missing id)")); //$NON-NLS-1$
            return;
        }
        if (element.getAttribute(ATT_SERVER) == null) {
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid domain extension: (missing server) " + id)); //$NON-NLS-1$
            return;
        }
        if (element.getAttribute(ATT_ADVERTISER) == null) {
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid domain extension: (missing advertiser) " + id)); //$NON-NLS-1$
            return;
        }
        if (element.getAttribute(ATT_DISCOVERER) == null) {
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid domain extension: (missing discoverer) " + id)); //$NON-NLS-1$
            return;
        }

        CommandServiceDomainImpl domain = new CommandServiceDomainImpl(this,
                id, element);
        if (domains == null) {
            domains = new HashMap<String, CommandServiceDomainImpl>(3);
        }
        domains.put(id, domain);
    }

    private void loadServiceFactory(IConfigurationElement element) {
        String id = element.getAttribute(ATT_ID);
        if (id == null || "".equals(id)) { //$NON-NLS-1$
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid serviceFactory extension: (missing id)")); //$NON-NLS-1$
            return;
        }

        if (element.getAttribute(ATT_CLASS) == null) {
            RemoteCommandPlugin.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid serviceFactory extension: (missing class) " + id)); //$NON-NLS-1$
            return;
        }

        DomainServiceFactoryDescriptor factory = new DomainServiceFactoryDescriptor(
                element);
        if (factories == null) {
            factories = new HashMap<String, DomainServiceFactoryDescriptor>(3);
        }
        factories.put(id, factory);
    }

    public static CommandServiceDomainManagerImpl getDefault() {
        return CommandServiceDomainManagerImpl.singleton;
    }

}
