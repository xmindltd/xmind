package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.ui.mindmap.INumberSeparator;
import org.xmind.ui.mindmap.INumberSeparatorDescriptor;
import org.xmind.ui.mindmap.INumberSeparatorManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class NumberSeparatorExtensionManager extends RegistryReader implements
        INumberSeparatorManager {

    private static class NumberSeparatorProxy implements INumberSeparator,
            INumberSeparatorDescriptor {

        private IConfigurationElement element;

        private String id;

        private String name;

        private String description;

        private INumberSeparator implementation;

        private boolean failedInitimplementation = false;

        public NumberSeparatorProxy(IConfigurationElement element)
                throws CoreException {
            this.element = element;
            this.id = element.getAttribute(RegistryConstants.ATT_ID);
            this.name = element.getAttribute(RegistryConstants.ATT_NAME);
            this.description = element
                    .getAttribute(RegistryConstants.ATT_DESCRIPTION);
            if (getClassValue(element, RegistryConstants.ATT_CLASS) == null)
                throw new CoreException(new Status(IStatus.ERROR,
                        element.getNamespaceIdentifier(), 0,
                        "Invalid extension (missing class name): " + id, null)); //$NON-NLS-1$

        }

        private INumberSeparator getImplementation() {
            if (implementation == null && !failedInitimplementation) {
                try {
                    implementation = (INumberSeparator) element
                            .createExecutableExtension(RegistryConstants.ATT_CLASS);
                } catch (CoreException e) {
                    Logger.log(
                            e,
                            "Failed to create number separator from class: " //$NON-NLS-1$
                                    + getClassValue(element,
                                            RegistryConstants.ATT_CLASS));
                    failedInitimplementation = true;
                }
            }
            return implementation;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getText() {
            INumberSeparator impl = getImplementation();
            if (impl != null)
                return impl.getText();
            return null;
        }

    }

    private Map<String, NumberSeparatorProxy> separators = null;

    private List<INumberSeparatorDescriptor> list = null;

    public List<INumberSeparatorDescriptor> getDescriptors() {
        ensureLoaded();
        return list;
    }

    public INumberSeparatorDescriptor getDescriptor(String separatorId) {
        ensureLoaded();
        return separators.get(separatorId);
    }

    public INumberSeparator getSeparator(String separatorId) {
        ensureLoaded();
        return separators.get(separatorId);
    }

    public String getSeparatorText(String separatorId) {
        INumberSeparator separator = getSeparator(separatorId);
        if (separator != null)
            return separator.getText();
        return null;
    }

    @Override
    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_SEPARATOR.equals(name)) {
            readFormat(element);
            return true;
        }
        return false;
    }

    private void readFormat(IConfigurationElement element) {
        NumberSeparatorProxy proxy;
        try {
            proxy = new NumberSeparatorProxy(element);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load number separator: " + element); //$NON-NLS-1$
            return;
        }

        if (separators == null)
            separators = new HashMap<String, NumberSeparatorProxy>();
        separators.put(proxy.getId(), proxy);

        if (list == null)
            list = new ArrayList<INumberSeparatorDescriptor>();
        list.add(proxy);
    }

    private void ensureLoaded() {
        if (separators != null && list != null)
            return;

        lazyLoad();

        if (separators == null)
            separators = Collections.emptyMap();
        if (list == null)
            list = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                RegistryConstants.EXT_NUMBER_SEPARATORS);
    }
}
