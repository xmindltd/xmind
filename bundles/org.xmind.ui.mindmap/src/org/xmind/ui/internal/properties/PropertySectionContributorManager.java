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
package org.xmind.ui.internal.properties;

import static org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants.TAG_ENABLEMENT;
import static org.xmind.ui.internal.RegistryConstants.TAG_SECTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.xmind.gef.ui.properties.IPropertySectionPart;
import org.xmind.ui.internal.RegistryConstants;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;

public class PropertySectionContributorManager extends RegistryReader {

    private static PropertySectionContributorManager instance = null;

    private static final List<String> NO_SECTION_IDS = Collections.emptyList();

    private Map<String, PropertySectionFactory> sectionFactories = null;

    private List<String> sectionIds = null;

    private PropertySectionContributorManager() {
    }

    protected boolean readElement(IConfigurationElement element) {
        String name = element.getName();
        if (TAG_SECTION.equals(name)) {
            readSection(element);
            return true;
        } else if (TAG_ENABLEMENT.equals(name)) {
            return true;
        }
        return false;
    }

    private void readSection(IConfigurationElement element) {
        PropertySectionFactory sectionFactory;
        try {
            sectionFactory = new PropertySectionFactory(element);
        } catch (CoreException e) {
            Logger.log(e,
                    "Failed to create PropertySection: " + element.toString()); //$NON-NLS-1$
            return;
        }

        String sectionId = sectionFactory.getId();
        if (sectionFactories == null)
            sectionFactories = new HashMap<String, PropertySectionFactory>();
        sectionFactories.put(sectionId, sectionFactory);
        if (sectionIds == null)
            sectionIds = new ArrayList<String>();
        sectionIds.add(sectionId);
    }

    private void ensureLoaded() {
        if (sectionFactories != null)
            return;

        lazyLoad();
        if (sectionFactories == null)
            sectionFactories = Collections.emptyMap();
        if (sectionIds == null)
            sectionIds = Collections.emptyList();
    }

    private void lazyLoad() {
        readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
                RegistryConstants.EXT_PROPERTY_SECTIONS);
    }

    public List<String> getApplicableSectionIds(Object[] objects) {
        if (objects == null || objects.length == 0)
            return NO_SECTION_IDS;

        ensureLoaded();
        List<String> ids = new ArrayList<String>(sectionIds);
        Iterator<String> it = ids.iterator();
        while (it.hasNext()) {
            PropertySectionFactory factory = sectionFactories.get(it.next());
            if (factory == null || !factory.isEnabledOn(objects)) {
                it.remove();
            }
        }
        return ids;
    }

    public IPropertySectionPart createSection(String id) {
        if (id == null || "".equals(id)) //$NON-NLS-1$
            return null;

        ensureLoaded();
        PropertySectionFactory factory = sectionFactories.get(id);
        return factory == null ? null : factory.createSection();
    }

//    /**
//     * 
//     * @param objects
//     * @return
//     */
//    public String calcPageId(Object[] objects) {
//        if (objects == null || objects.length == 0)
//            return null;
//
//        ensureLoaded();
//        StringBuilder sb = new StringBuilder(sectionIds.size() * 15);
//        Iterator<String> it = sectionIds.iterator();
//        while (it.hasNext()) {
//            PropertySectionFactory sectionFactory = sectionFactories.get(it
//                    .next());
//            if (sectionFactory != null) {
//                if (sectionFactory.isEnabledOn(objects)) {
//                    if (sb.length() > 0) {
//                        sb.append(",");
//                    }
//                    sb.append(sectionFactory.getId());
//                }
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 
//     * @param pageId
//     * @return
//     */
//    public IPropertyPagePart createPageById(String pageId) {
//        if (pageId == null || "".equals(pageId)) //$NON-NLS-1$
//            return null;
//
//        ensureLoaded();
//        String[] sectionIds = pageId.split(","); //$NON-NLS-1$
//        List<PropertySectionFactory> factories = new ArrayList<PropertySectionFactory>(
//                sectionIds.length);
//        for (String sectionId : sectionIds) {
//            PropertySectionFactory factory = sectionFactories.get(sectionId);
//            if (factory != null) {
//                factories.add(factory);
//            }
//        }
//        if (factories != null) {
//            return new MindMapPropertyPagePart(pageId, factories);
//        }
//        return null;
//    }

    public static PropertySectionContributorManager getInstance() {
        if (instance == null)
            instance = new PropertySectionContributorManager();
        return instance;
    }

}