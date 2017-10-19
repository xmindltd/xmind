package org.xmind.ui.internal;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IInfoItemContributor;
import org.xmind.ui.mindmap.IInfoItemPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.Logger;

public class InfoItemContributorProxy implements IInfoItemContributor {

    private static class NullInfoItemContributor
            implements IInfoItemContributor {

        private NullInfoItemContributor() {
        }

        public IAction createAction(ITopicPart topicPart, ITopic topic) {
            return null;
        }

        public String getContent(ITopic topic) {
            return null;
        }

        public void fillContextMenu(IInfoItemPart part) {
        }

        public void topicActivated(ITopicPart topicPart) {
        }

        public void topicDeactivated(ITopicPart topicPart) {
        }

        public void topicActivated(IInfoPart infoPart) {
        }

        public void topicDeactivated(IInfoPart infoPart) {
        }

        public String getId() {
            return null;
        }

        public String getDefaultMode() {
            return null;
        }

        public String getAvailableModes() {
            return null;
        }

        public String getCardLabel() {
            return null;
        }

        public String getSVGFilePath(ITopic topic, IAction action) {
            return null;
        }

        public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
            return false;
        }

        public List<IAction> getPopupMenuActions(ITopicPart topicPart,
                ITopic topic) {
            return Collections.emptyList();
        }

        @Override
        public boolean isModified(ITopicPart topicPart, ITopic topic,
                IAction action) {
            return true;
        }
    }

    private static final IInfoItemContributor NULL_CONTRIBUTOR = new NullInfoItemContributor();

    private IConfigurationElement element;

    private String id;

    private ImageDescriptor icon;

    private String label;

    private String tooltip;

    private String defaultMode;

    private String availableModes;

    private String cardLabel;

    private IInfoItemContributor implementation;

    public InfoItemContributorProxy(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(RegistryConstants.ATT_ID);
        this.label = element.getAttribute(RegistryConstants.ATT_LABEL);
        this.tooltip = element.getAttribute(RegistryConstants.ATT_TOOLTIP);
        this.defaultMode = element.getAttribute(RegistryConstants.ATT_MODE);
        this.availableModes = element
                .getAttribute(RegistryConstants.ATT_AVAILABLEMODES);
        this.cardLabel = element.getAttribute(RegistryConstants.ATT_CARD_LABEL);
        if (RegistryReader.getClassValue(element,
                RegistryConstants.ATT_CONTRIBUTOR_CLASS) == null) {
            throw new CoreException(
                    new Status(IStatus.ERROR, element.getNamespaceIdentifier(),
                            0, "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                            null));
        }
    }

    public IAction createAction(ITopicPart topicPart, ITopic topic) {
        IAction action = getImplementation().createAction(topicPart, topic);
        if (action != null) {
            if (action.getImageDescriptor() == null) {
                action.setImageDescriptor(getIcon());
            }
            if (action.getText() == null) {
                action.setText(getLabel());
            }
            if (action.getToolTipText() == null) {
                action.setToolTipText(getTooltip());
            }
        }
        return action;
    }

    public String getContent(ITopic topic) {
        return getImplementation().getContent(topic);
    }

    public ImageDescriptor getIcon() {
        if (icon == null) {
            icon = createIcon();
        }
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String getId() {
        return id;
    }

    public String getDefaultMode() {
        return defaultMode;
    }

    public String getAvailableModes() {
        return availableModes;
    }

    public String getCardLabel() {
        return cardLabel;
    }

    public String getSVGFilePath(ITopic topic, IAction action) {
        /// TODO write in extension
        return getImplementation().getSVGFilePath(topic, action);
    }

    public boolean isCardModeAvailable(ITopic topic, ITopicPart topicPart) {
        return getImplementation().isCardModeAvailable(topic, topicPart);
    }

    private ImageDescriptor createIcon() {
        String iconName = element.getAttribute(ATT_ICON);
        if (iconName != null) {
            String plugId = element.getNamespaceIdentifier();
            return AbstractUIPlugin.imageDescriptorFromPlugin(plugId, iconName);
        }
        return null;
    }

    public IInfoItemContributor getImplementation() {
        if (implementation == null) {
            try {
                implementation = (IInfoItemContributor) element
                        .createExecutableExtension(
                                RegistryConstants.ATT_CONTRIBUTOR_CLASS);
            } catch (CoreException e) {
                Logger.log(e,
                        "Failed to create icon tip contributor from class: " //$NON-NLS-1$
                                + RegistryReader.getClassValue(element,
                                        RegistryConstants.ATT_CONTRIBUTOR_CLASS));
                implementation = NULL_CONTRIBUTOR;
            }
        }
        return implementation;
    }

    public void fillContextMenu(IInfoItemPart part) {
        getImplementation().fillContextMenu(part);
    }

    public void topicActivated(IInfoPart infoPart) {
        getImplementation().topicActivated(infoPart);
    }

    public void topicDeactivated(IInfoPart infoPart) {
        getImplementation().topicDeactivated(infoPart);
    }

    public void topicActivated(ITopicPart topicPart) {
        getImplementation().topicActivated(topicPart);
    }

    public void topicDeactivated(ITopicPart topicPart) {
        getImplementation().topicDeactivated(topicPart);
    }

    public List<IAction> getPopupMenuActions(ITopicPart topicPart,
            ITopic topic) {
        return getImplementation().getPopupMenuActions(topicPart, topic);
    }

    @Override
    public boolean isModified(ITopicPart topicPart, ITopic topic,
            IAction action) {
        return getImplementation().isModified(topicPart, topic, action);
    }

}
