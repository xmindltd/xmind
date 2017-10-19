package org.xmind.ui.internal.editor;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;
import static org.xmind.ui.internal.RegistryConstants.ATT_SCHEME;
import static org.xmind.ui.internal.RegistryConstants.EXT_WORKBOOK_REF_FACTORIES;
import static org.xmind.ui.internal.RegistryConstants.TAG_AVAILABLE_FOR_URI_SCHEME;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;
import org.xmind.ui.mindmap.MindMapUI;

public class WorkbookRefFactoryManager
        implements IWorkbookRefFactory, IRegistryEventListener {

    private static class WorkbookRefFactoryDescriptor {
        private IConfigurationElement element;
        private final Set<String> schemes;
        private volatile IWorkbookRefFactory factoryInstance;

        private WorkbookRefFactoryDescriptor(IConfigurationElement element) {
            if (element.getAttribute(ATT_CLASS) == null) {
                throw new IllegalArgumentException(
                        "No 'class' attribute on factory element (contributed by " //$NON-NLS-1$
                                + element.getContributor().getName() + ")"); //$NON-NLS-1$
            }
            this.element = element;
            this.schemes = new HashSet<String>();
            for (IConfigurationElement schemeTag : element
                    .getChildren(TAG_AVAILABLE_FOR_URI_SCHEME)) {
                String scheme = schemeTag.getAttribute(ATT_SCHEME);
                if (scheme != null) {
                    this.schemes.add(scheme);
                }
            }
        }

        public synchronized IWorkbookRefFactory getInstance() {
            if (this.factoryInstance == null && this.element != null) {
                Object ins = null;
                try {
                    ins = this.element.createExecutableExtension(ATT_CLASS);
                } catch (CoreException e) {
                    MindMapUIPlugin.getDefault().getLog().log(e.getStatus());
                }
                if (ins != null && ins instanceof IWorkbookRefFactory) {
                    this.factoryInstance = (IWorkbookRefFactory) ins;
                }
            }
            return this.factoryInstance;
        }

        public synchronized void dispose() {
            this.schemes.clear();
            this.element = null;
            this.factoryInstance = null;
        }

        public boolean isAvailableForURIScheme(String scheme) {
            return this.schemes.contains(scheme);
        }

        public boolean isFromExtension(IExtension ext) {
            return element != null && ext != null
                    && ext.equals(element.getDeclaringExtension());
        }

    }

    private List<WorkbookRefFactoryDescriptor> factories = null;

    private IExtensionRegistry registry = null;

    public WorkbookRefFactoryManager() {
        super();
    }

    public synchronized IWorkbookRef createWorkbookRef(URI uri,
            IMemento state) {
        IWorkbookRefFactory factory = getWorkbookRefFactoryForURI(uri);
        if (factory != null)
            return factory.createWorkbookRef(uri, state);
        return URLWorkbookRef.create(uri, state);
    }

    private synchronized IWorkbookRefFactory getWorkbookRefFactoryForURI(
            URI uri) {
        ensureLoaded();
        String scheme = uri.getScheme();
        for (WorkbookRefFactoryDescriptor factory : factories) {
            if (factory.isAvailableForURIScheme(scheme)) {
                return factory.getInstance();
            }
        }
        return null;
    }

    private synchronized void ensureLoaded() {
        if (factories != null)
            return;

        factories = new ArrayList<WorkbookRefFactoryDescriptor>();

        registry = Platform.getExtensionRegistry();
        if (registry == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                            "Extension registry is not available")); //$NON-NLS-1$
            return;
        }

        IExtensionPoint extPoint = registry.getExtensionPoint(
                MindMapUI.PLUGIN_ID, EXT_WORKBOOK_REF_FACTORIES);
        if (extPoint == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                            "Extension point '" + MindMapUI.PLUGIN_ID //$NON-NLS-1$
                                    + "." + EXT_WORKBOOK_REF_FACTORIES //$NON-NLS-1$
                                    + "' not found.")); //$NON-NLS-1$
            return;
        }

        registry.addListener(this, extPoint.getUniqueIdentifier());

        readExtensions(extPoint.getExtensions());
    }

    private void readExtensions(IExtension[] extensions) {
        for (IExtension ext : extensions) {
            for (IConfigurationElement element : ext
                    .getConfigurationElements()) {
                if (RegistryConstants.TAG_FACTORY.equals(element.getName())) {
                    WorkbookRefFactoryDescriptor desc = new WorkbookRefFactoryDescriptor(
                            element);
                    factories.add(desc);
                }
            }
        }
    }

    public synchronized void dispose() {
        if (factories != null) {
            for (Object o : factories.toArray()) {
                ((WorkbookRefFactoryDescriptor) o).dispose();
            }
            factories.clear();
            factories = null;
        }
        if (registry != null) {
            registry.removeListener(this);
            registry = null;
        }
    }

    public void added(IExtension[] extensions) {
        readExtensions(extensions);
    }

    public void removed(IExtension[] extensions) {
        for (IExtension ext : extensions) {
            for (Object o : factories.toArray()) {
                if (((WorkbookRefFactoryDescriptor) o).isFromExtension(ext)) {
                    factories.remove(o);
                    ((WorkbookRefFactoryDescriptor) o).dispose();
                }
            }
        }
    }

    public void added(IExtensionPoint[] extensionPoints) {
        // do not care about other extension points
    }

    public void removed(IExtensionPoint[] extensionPoints) {
        // do not care about other extension points
    }

}
