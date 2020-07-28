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
package org.xmind.ui.internal.dialogs;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.ui.dialogs.IHyperlinkPage;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class HyperlinkPageDescriptor {

    private IConfigurationElement element;

    private String id;

    private String name;

    private String protocolId;

    private ImageDescriptor icon;

    public HyperlinkPageDescriptor(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
        this.name = element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
        this.protocolId = element.getAttribute("protocolId"); //$NON-NLS-1$

        if (element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) == null) {
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                    null));
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IHyperlinkPage createPage() throws CoreException {
        return (IHyperlinkPage) element
                .createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
    }

    public String getProtocolId() {
        return protocolId;
    }

    public ImageDescriptor getIcon() {
        if (icon == null) {
            icon = createIcon();
        }
        return icon;
    }

    private ImageDescriptor createIcon() {
        String iconName = element.getAttribute(ATT_ICON);
        if (iconName != null) {
            String plugId = element.getNamespaceIdentifier();
            return AbstractUIPlugin.imageDescriptorFromPlugin(plugId, iconName);
        }
        return null;
    }

}
