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

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_CATEGORY;
import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_CLASS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class DecorationManager extends RegistryReader implements
        IDecorationManager, IDecorationFactory {

    private static final List<IDecorationDescriptor> NO_DESCRIPTORS = Collections
            .emptyList();

    private Map<String, DecorationDescriptor> decorationDescriptors = null;

    private List<DecorationCategory> categories = null;

    private Map<String, List<IDecorationDescriptor>> categoriedDescriptors = null;

    public IDecoration createDecoration(String decorationId, IGraphicalPart part) {
        if (part == null || decorationId == null)
            return null;
        ensureLoaded();
        DecorationDescriptor factory = decorationDescriptors.get(decorationId);
        return factory == null ? null : factory.createDecoration(decorationId,
                part);
    }

    public IDecorationDescriptor getDecorationDescriptor(String decorationId) {
        ensureLoaded();
        return decorationDescriptors.get(decorationId);
    }

    public List<IDecorationDescriptor> getDescriptors(String categoryId) {
        ensureLoaded();
        List<IDecorationDescriptor> list = categoriedDescriptors
                .get(categoryId);
        return list == null ? NO_DESCRIPTORS : list;
    }

    public Collection<DecorationCategory> getCategories() {
        ensureLoaded();
        return categories;
    }

    private void ensureLoaded() {
        if (categories != null && categoriedDescriptors != null
                && decorationDescriptors != null)
            return;
        lazyLoad();
        if (categoriedDescriptors == null)
            categoriedDescriptors = Collections.emptyMap();
        if (decorationDescriptors == null)
            decorationDescriptors = Collections.emptyMap();
        if (categories == null)
            categories = Collections.emptyList();
    }

    private void lazyLoad() {
        if (Platform.isRunning()) {
            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                    RegistryConstants.EXT_DECORATIONS);
        }
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (RegistryConstants.TAG_DECORATION.equals(name)) {
            readDecoration(element);
            readElementChildren(element);
            return true;
        } else if (TAG_CATEGORY.equals(name)) {
            readCategory(element);
            readElementChildren(element);
            return true;
        } else if (TAG_CLASS.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_FACTORY.equals(name)) {
            readElementChildren(element);
            return true;
        } else if (RegistryConstants.TAG_DEFAULT_VALUE.equals(name)) {
            readElementChildren(element);
            return true;
        }
        return false;
    }

    private void readCategory(IConfigurationElement element) {
        DecorationCategory category = new DecorationCategory(element);
        registerCategory(category);
    }

    private void readDecoration(IConfigurationElement element) {
        try {
            DecorationDescriptor desc = new DecorationDescriptor(element);
            registerDecoration(desc);
        } catch (CoreException e) {
            Logger.log(e, "Failed to load decoration: " + element); //$NON-NLS-1$
        }
    }

    private void registerDecoration(DecorationDescriptor desc) {
        String id = desc.getId();
        if (decorationDescriptors == null)
            decorationDescriptors = new HashMap<String, DecorationDescriptor>();
        decorationDescriptors.put(id, desc);

        String categoryId = desc.getCategoryId();
        if (categoryId != null && !"".equals(categoryId)) { //$NON-NLS-1$
            if (categoriedDescriptors == null)
                categoriedDescriptors = new HashMap<String, List<IDecorationDescriptor>>();
            List<IDecorationDescriptor> list = categoriedDescriptors
                    .get(categoryId);
            if (list == null) {
                list = new ArrayList<IDecorationDescriptor>();
                categoriedDescriptors.put(categoryId, list);
            }
            list.add(desc);
        }
    }

    private void registerCategory(DecorationCategory category) {
        if (categories == null)
            categories = new ArrayList<DecorationCategory>();
        categories.add(category);
    }

}