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
package org.xmind.core.internal.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.xmind.core.command.ICommand;
import org.xmind.core.command.ICommandHandler;

public class XMindCommandHandlerRegistry implements IRegistryEventListener {

    private static final String EXT_POINT_ID = "handlers"; //$NON-NLS-1$

    private static final String ATT_COMMAND = "command"; //$NON-NLS-1$

    private static final String ATT_CLASS = "class"; //$NON-NLS-1$

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    public static class CommandHandlerDescriptor {

        private String id;

        private Pattern pattern;

        private IConfigurationElement element;

        private ICommandHandler handler;

        public CommandHandlerDescriptor(String id, IConfigurationElement element) {
            String namePattern = element.getAttribute(ATT_COMMAND);
            if (namePattern == null)
                namePattern = ""; //$NON-NLS-1$
            this.id = id;
            this.pattern = Pattern.compile(namePattern);
            this.element = element;
            this.handler = null;
        }

        public String getId() {
            return id;
        }

        public Matcher match(String commandName) {
            return pattern.matcher(commandName);
        }

        public ICommandHandler getHandler() {
            if (handler == null) {
                try {
                    handler = (ICommandHandler) element
                            .createExecutableExtension(ATT_CLASS);
                } catch (CoreException e) {
                    Logger.log(null, e);
                }
            }
            return handler;
        }

        @Override
        public String toString() {
            return "CommandHandler{" + getId() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private static final XMindCommandHandlerRegistry instance = new XMindCommandHandlerRegistry();

    private List<CommandHandlerDescriptor> descriptors = null;

    private Map<String, List<CommandHandlerDescriptor>> cache = null;

    private XMindCommandHandlerRegistry() {
    }

    private synchronized List<CommandHandlerDescriptor> getHandlers() {
        ensureLoaded();
        return descriptors;
    }

    private void ensureLoaded() {
        if (descriptors != null)
            return;
        loadFromExtensions();
        if (descriptors == null)
            descriptors = Collections.emptyList();
    }

    private void loadFromExtensions() {
        IExtensionPoint point = Platform.getExtensionRegistry()
                .getExtensionPoint(XMindCommandPlugin.PLUGIN_ID, EXT_POINT_ID);
        if (point == null)
            return;
        IExtension[] extensions = point.getExtensions();
        loadFromExtensions(extensions);
    }

    private void loadFromExtensions(IExtension[] extensions) {
        for (int i = 0; i < extensions.length; i++) {
            loadFromConfigurationElements(extensions[i]
                    .getConfigurationElements());
        }
    }

    private void loadFromConfigurationElements(IConfigurationElement[] elements) {
        for (int i = 0; i < elements.length; i++) {
            loadFromConfigurationElement(elements[i]);
        }
    }

    private void loadFromConfigurationElement(IConfigurationElement element) {
        String name = element.getName();
        if ("handler".equals(name)) { //$NON-NLS-1$
            loadFromHandlerConfigurationElement(element);
            loadFromConfigurationElements(element.getChildren());
        }
    }

    private void loadFromHandlerConfigurationElement(
            IConfigurationElement element) {
        String id = element.getAttribute(ATT_ID);
        if (id == null) {
            Logger.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid extension: (id missing)")); //$NON-NLS-1$
            return;
        }
        if (element.getAttribute(ATT_CLASS) == null) {
            Logger.log(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(),
                    "Invalid extension: (class missing)")); //$NON-NLS-1$
            return;
        }

        registerContribution(new CommandHandlerDescriptor(id, element));
    }

    private void registerContribution(CommandHandlerDescriptor contribution) {
        if (descriptors == null)
            descriptors = new ArrayList<CommandHandlerDescriptor>();
        descriptors.add(contribution);
    }

    synchronized List<CommandHandlerDescriptor> findMatchedHandlerDescriptors(
            ICommand command) {
        if (cache == null) {
            cache = new HashMap<String, List<CommandHandlerDescriptor>>();
        }
        List<CommandHandlerDescriptor> list = cache.get(command
                .getCommandName());
        if (list == null) {
            list = collectHandlers(command.getCommandName());
            cache.put(command.getCommandName(), list);
        }
        return list;
    }

    private List<CommandHandlerDescriptor> collectHandlers(String commandName) {
        List<CommandHandlerDescriptor> list = new ArrayList<CommandHandlerDescriptor>();
        for (CommandHandlerDescriptor desc : getHandlers()) {
            if (desc.match(commandName).matches()) {
                list.add((CommandHandlerDescriptor) desc);
            }
        }
        return list;
    }

    private void invalidate() {
        descriptors = null;
        cache = null;
    }

    public void added(IExtension[] extensions) {
        invalidate();
    }

    public void removed(IExtension[] extensions) {
        invalidate();
    }

    public void added(IExtensionPoint[] extensionPoints) {
        // do nothing
    }

    public void removed(IExtensionPoint[] extensionPoints) {
        // do nothing
    }

    public void installRegistryEventListener(IExtensionRegistry registry) {
        if (registry == null)
            return;
        registry.addListener(this, XMindCommandPlugin.PLUGIN_ID + "." //$NON-NLS-1$
                + EXT_POINT_ID);
    }

    public void uninstallRegistryEventListener(IExtensionRegistry registry) {
        if (registry == null)
            return;
        registry.removeListener(this);
    }

    public static XMindCommandHandlerRegistry getInstance() {
        return instance;
    }

}
