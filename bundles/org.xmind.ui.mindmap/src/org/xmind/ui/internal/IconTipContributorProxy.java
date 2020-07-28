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
package org.xmind.ui.internal;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IIconTipContributor;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.Logger;

public class IconTipContributorProxy implements IIconTipContributor {

    private static class NullIconTipContributor implements IIconTipContributor {

        private NullIconTipContributor() {
        }

        public IAction createAction(ITopicPart topicPart, ITopic topic) {
            return null;
        }

        public void fillContextMenu(IIconTipPart part) {
        }

        public void topicActivated(ITopicPart topicPart) {
        }

        public void topicDeactivated(ITopicPart topicPart) {
        }
    }

    private static final IIconTipContributor NULL_CONTRIBUTOR = new NullIconTipContributor();

    private IConfigurationElement element;

    private String id;

    private ImageDescriptor icon;

    private String label;

    private String tooltip;

    private IIconTipContributor implementation;

    public IconTipContributorProxy(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(RegistryConstants.ATT_ID);
        this.label = element.getAttribute(RegistryConstants.ATT_LABEL);
        this.tooltip = element.getAttribute(RegistryConstants.ATT_TOOLTIP);
        if (RegistryReader.getClassValue(element,
                RegistryConstants.ATT_CONTRIBUTOR_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR,
                    element.getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
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

    private ImageDescriptor createIcon() {
        String iconName = element.getAttribute(ATT_ICON);
        if (iconName != null) {
            String plugId = element.getNamespaceIdentifier();
            return AbstractUIPlugin.imageDescriptorFromPlugin(plugId, iconName);
        }
        return null;
    }

    public IIconTipContributor getImplementation() {
        if (implementation == null) {
            try {
                implementation = (IIconTipContributor) element
                        .createExecutableExtension(RegistryConstants.ATT_CONTRIBUTOR_CLASS);
            } catch (CoreException e) {
                Logger.log(
                        e,
                        "Failed to create icon tip contributor from class: " //$NON-NLS-1$
                                + RegistryReader
                                        .getClassValue(
                                                element,
                                                RegistryConstants.ATT_CONTRIBUTOR_CLASS));
                implementation = NULL_CONTRIBUTOR;
            }
        }
        return implementation;
    }

    public void fillContextMenu(IIconTipPart part) {
        getImplementation().fillContextMenu(part);
    }

    public void topicActivated(ITopicPart topicPart) {
        getImplementation().topicActivated(topicPart);
    }

    public void topicDeactivated(ITopicPart topicPart) {
        getImplementation().topicDeactivated(topicPart);
    }

}