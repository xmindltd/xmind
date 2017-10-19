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
package org.xmind.ui.internal.mindmap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class ToolExtensionRegistry extends RegistryReader {

    private static final ToolExtensionRegistry instance = new ToolExtensionRegistry();

    private static class ToolFactory {

        private IConfigurationElement element;

        private String id;

        private boolean failed = false;

        public ToolFactory(IConfigurationElement element) throws CoreException {
            this.element = element;
            this.id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
            if (getClassValue(element, IWorkbenchRegistryConstants.ATT_CLASS) == null) {
                throw new CoreException(new Status(IStatus.ERROR, element
                        .getNamespaceIdentifier(),
                        "Invalid extension (missing class value)")); //$NON-NLS-1$
            }
        }

        public String getId() {
            return id;
        }

        public ITool createInstance() {
            if (failed)
                return null;

            try {
                return (ITool) element
                        .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
            } catch (CoreException e) {
                failed = true;
                Logger.log(e, "Failed to create tool instance (id=" + id + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }
        }
    }

    private ToolExtensionRegistry() {
    }

    private Map<String, ToolFactory> factories = null;

    public ITool createTool(String id) {
        ensureLoaded();
        ToolFactory factory = factories.get(id);
        if (factory == null)
            return null;
        return factory.createInstance();
    }

    private void ensureLoaded() {
        if (factories != null)
            return;

        lazyLoad();
        if (factories == null)
            factories = Collections.emptyMap();
    }

    private void lazyLoad() {
        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    "tools"); //$NON-NLS-1$
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if ("tool".equals(name)) { //$NON-NLS-1$
            readToolFactory(element);
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readToolFactory(IConfigurationElement element) {
        try {
            register(new ToolFactory(element));
        } catch (CoreException e) {
            Logger.log(e, "Failed to create tool factory"); //$NON-NLS-1$
        }
    }

    private void register(ToolFactory factory) {
        if (factories == null)
            factories = new HashMap<String, ToolFactory>();
        factories.put(factory.getId(), factory);
    }

    public static ToolExtensionRegistry getInstance() {
        return instance;
    }

}