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
package org.xmind.ui.internal.decorations;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CATEGORY_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_CLASS;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ICON;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_ID;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.ATT_NAME;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.util.Logger;

public class DecorationDescriptor implements IDecorationDescriptor,
        IDecorationFactory, IStyleValueProvider {

    private IConfigurationElement element;

    private IConfigurationElement classElement;

    private IConfigurationElement factoryElement;

    private IDecorationFactory contributedFactory;

    private boolean triedLoadingContributedFacotry;

    private boolean failedCreatingDecoration;

    private String id;

    private String name;

    private String categoryId;

    private ImageDescriptor icon;

    private Map<String, String> defaultValues;

    DecorationDescriptor(IConfigurationElement element) throws CoreException {
        this.element = element;
        id = element.getAttribute(ATT_ID);
        categoryId = element.getAttribute(ATT_CATEGORY_ID);
        name = element.getAttribute(ATT_NAME);
        loadClassElement();
        loadFactoryElement();
        if ((classElement == null && factoryElement == null)
                || !hasClassAttribute())
            throw new CoreException(new Status(IStatus.ERROR, element
                    .getNamespaceIdentifier(), 0,
                    "Invalid extension (missing class name): " + id, //$NON-NLS-1$
                    null));
        loadDefaultValues();
    }

    private boolean hasClassAttribute() {
        if (classElement != null) {
            if (classElement.getAttribute(ATT_CLASS) != null)
                return true;
        }
        if (factoryElement != null) {
            if (factoryElement.getAttribute(ATT_CLASS) != null)
                return true;
        }
        return false;
    }

    private void loadClassElement() {
        IConfigurationElement[] children = element
                .getChildren(IWorkbenchRegistryConstants.TAG_CLASS);
        if (children.length == 0)
            return;
        classElement = children[0];
    }

    private void loadFactoryElement() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_FACTORY);
        if (children.length == 0)
            return;
        factoryElement = children[0];
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getId() {
        return id;
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

    public String getName() {
        return name;
    }

    private IDecorationFactory getContributedFactory() {
        if (factoryElement == null)
            return null;
        if (contributedFactory == null && !triedLoadingContributedFacotry) {
            try {
                contributedFactory = (IDecorationFactory) factoryElement
                        .createExecutableExtension(ATT_CLASS);
            } catch (CoreException e) {
                Logger.log(e, "Failed to create decoration factory: " + id); //$NON-NLS-1$
            }
            triedLoadingContributedFacotry = true;
        }
        return contributedFactory;
    }

    public IDecoration createDecoration(String id, IGraphicalPart part) {
        IDecoration decoration = null;
        IDecorationFactory factory = getContributedFactory();
        if (factory != null) {
            decoration = factory.createDecoration(id, part);
        }
        if (decoration == null) {
            decoration = createDecoration();
        }
        if (decoration != null) {
            decoration.setId(id);
        }
        return decoration;
    }

    private IDecoration createDecoration() {
        if (failedCreatingDecoration || classElement == null)
            return null;
        try {
            return (IDecoration) classElement
                    .createExecutableExtension(ATT_CLASS);
        } catch (CoreException e) {
            failedCreatingDecoration = true;
            Logger.log(e, "Failed to create decoration: " + id); //$NON-NLS-1$
            return null;
        }
    }

    private void loadDefaultValues() {
        IConfigurationElement[] children = element
                .getChildren(RegistryConstants.TAG_DEFAULT_VALUE);
        if (children.length == 0)
            return;
        for (IConfigurationElement child : children) {
            String key = child.getAttribute(RegistryConstants.ATT_KEY);
            if (key != null) {
                String value = child.getAttribute(RegistryConstants.ATT_VALUE);
                if (defaultValues == null)
                    defaultValues = new HashMap<String, String>();
                defaultValues.put(key, value);
            }
        }
    }

    public IStyleValueProvider getDefaultValueProvider(String key) {
        if (defaultValues != null && defaultValues.containsKey(key))
            return this;
        return null;
    }

    public String getValue(IGraphicalPart part, String key) {
        if (defaultValues != null)
            return defaultValues.get(key);
        return null;
    }

    public boolean isKeyInteresting(IGraphicalPart part, String key) {
        if (defaultValues != null)
            return defaultValues.containsKey(key);
        return false;
    }

}