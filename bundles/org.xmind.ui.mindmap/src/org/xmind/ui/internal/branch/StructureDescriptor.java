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
package org.xmind.ui.internal.branch;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.ui.branch.IBranchStructure;
import org.xmind.ui.util.Logger;

class StructureDescriptor implements IStructureDescriptor {

    private IConfigurationElement element;

    private String id;

    private String name;

    private ImageDescriptor icon;

    private IBranchStructure structure;

    public StructureDescriptor(IConfigurationElement element)
            throws CoreException {
        this.element = element;
        this.id = element.getAttribute(ATT_ID);
        this.name = element.getAttribute(ATT_NAME);
        if (RegistryReader.getClassValue(element, ATT_CLASS) == null) {
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

    public IBranchStructure getAlgorithm() {
        if (structure == null) {
            try {
                structure = (IBranchStructure) element
                        .createExecutableExtension(ATT_CLASS);
            } catch (CoreException e) {
                Logger.log(e, "Failed to create structure: " + id); //$NON-NLS-1$
                structure = DefaultStructureDescriptor.getInstance()
                        .getAlgorithm();
            }
        }
        return structure;
    }

}