package org.xmind.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Shawn Liu
 * @since 3.6.50
 */
public class ShareOption {

    public static final String ATT_DISABLED_SITE = "disabledSite"; //$NON-NLS-1$

    private IConfigurationElement element;

    private String id;

    private String label;

    private String tooltip;

    private String commandId;

    private ImageDescriptor icon;

    private String category;

    private String disabledSite;

    public ShareOption(IConfigurationElement element) throws CoreException {
        this.element = element;
        this.id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        this.label = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
        this.tooltip = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
        this.category = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_CATEGORY);
        this.commandId = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
        this.disabledSite = element.getAttribute(ATT_DISABLED_SITE);
        this.icon = null;

        if (id == null || commandId == null)
            throw new CoreException(
                    new Status(IStatus.ERROR, element.getNamespaceIdentifier(),
                            "Invalid extension element")); //$NON-NLS-1$
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltip() {
        return tooltip;
    }

    /**
     * @return the commandId
     */
    public String getCommandId() {
        return commandId;
    }

    public ImageDescriptor getImage() {
        if (icon == null) {
            icon = createImage();
        }
        return icon;
    }

    private ImageDescriptor createImage() {
        String imageName = element
                .getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
        if (imageName != null) {
            String plugId = element.getNamespaceIdentifier();
            return AbstractUIPlugin.imageDescriptorFromPlugin(plugId,
                    imageName);
        }
        return null;
    }

    public String getCategory() {
        return category;
    }

    public String getDisabledSite() {
        return disabledSite;
    }

}
