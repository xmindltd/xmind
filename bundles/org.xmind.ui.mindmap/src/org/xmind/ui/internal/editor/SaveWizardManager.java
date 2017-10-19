package org.xmind.ui.internal.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.ISaveWizard;

public class SaveWizardManager implements IRegistryEventListener {

    public static class SaveWizardDescriptor {

        private String id;

        private IConfigurationElement element;

        private ISaveWizard wizard = null;

        private SaveWizardDescriptor(String id, IConfigurationElement element) {
            this.id = id;
            this.element = element;
        }

        public String getId() {
            return id;
        }

        public ISaveWizard getWizard() {
            if (wizard == null && element != null) {
                try {
                    wizard = (ISaveWizard) element.createExecutableExtension(
                            IWorkbenchRegistryConstants.ATT_CLASS);
                } catch (CoreException e) {
                    MindMapUIPlugin.getDefault().getLog()
                            .log(new Status(IStatus.ERROR,
                                    MindMapUIPlugin.PLUGIN_ID,
                                    "Failed to create save wizard from class: " //$NON-NLS-1$
                                            + element.getDeclaringExtension()
                                                    .getNamespaceIdentifier()
                                            + "/" //$NON-NLS-1$
                                            + element.getAttribute(
                                                    IWorkbenchRegistryConstants.ATT_CLASS),
                            e));
                    return null;
                }
            }
            return wizard;
        }

        private void dispose() {
            this.element = null;
            this.wizard = null;
        }

        private boolean isFromExtension(IExtension ext) {
            return element != null && ext != null
                    && ext.equals(element.getDeclaringExtension());
        }

        public String getName() {
            String name = element
                    .getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
            return name == null ? "" : name; //$NON-NLS-1$
        }

    }

    private List<SaveWizardDescriptor> wizards = null;

    private IExtensionRegistry registry = null;

    public SaveWizardManager() {
    }

    public List<SaveWizardDescriptor> getWizards() {
        ensureLoaded();
        return Collections.unmodifiableList(wizards);
    }

    private synchronized void ensureLoaded() {
        if (wizards != null)
            return;

        wizards = new ArrayList<SaveWizardDescriptor>();

        registry = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = registry.getExtensionPoint(
                MindMapUI.PLUGIN_ID, RegistryConstants.EXT_SAVE_WIZARDS);
        if (extPoint == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Extension point '" + MindMapUI.PLUGIN_ID //$NON-NLS-1$
                                    + "." + RegistryConstants.EXT_SAVE_WIZARDS //$NON-NLS-1$
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
                if (RegistryConstants.TAG_SAVE_WIZARD
                        .equals(element.getName())) {
                    String id = element
                            .getAttribute(IWorkbenchRegistryConstants.ATT_ID);
                    String clazz = element.getAttribute(
                            IWorkbenchRegistryConstants.ATT_CLASS);
                    if (id == null || id.length() <= 0 || clazz == null
                            || clazz.length() <= 0) {
                        MindMapUIPlugin.getDefault().getLog()
                                .log(new Status(IStatus.WARNING,
                                        MindMapUIPlugin.PLUGIN_ID,
                                        "Invalid save wizard extension in " //$NON-NLS-1$
                                                + ext.getUniqueIdentifier()));
                        continue;
                    }
                    wizards.add(new SaveWizardDescriptor(id, element));
                }
            }
        }
    }

    public synchronized void dispose() {
        if (wizards != null) {
            SaveWizardDescriptor[] ws = wizards
                    .toArray(new SaveWizardDescriptor[wizards.size()]);
            wizards = null;
            for (SaveWizardDescriptor w : ws) {
                w.dispose();
            }
        }
    }

    @Override
    public void added(IExtension[] extensions) {
        readExtensions(extensions);
    }

    @Override
    public void removed(IExtension[] extensions) {
        if (wizards != null) {
            for (IExtension ext : extensions) {
                SaveWizardDescriptor[] ws = wizards
                        .toArray(new SaveWizardDescriptor[wizards.size()]);
                for (SaveWizardDescriptor w : ws) {
                    if (w.isFromExtension(ext)) {
                        wizards.remove(w);
                        w.dispose();
                    }
                }
            }
        }
    }

    @Override
    public void added(IExtensionPoint[] extensionPoints) {
        // do not care about other extension points
    }

    @Override
    public void removed(IExtensionPoint[] extensionPoints) {
        // do not care about other extension points
    }

}
