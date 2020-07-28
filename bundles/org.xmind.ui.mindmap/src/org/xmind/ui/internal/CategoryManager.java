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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.internal.expressions.Expressions;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.MindMapUI;

public class CategoryManager implements ICategoryManager {

    protected static class Category {

        static final String DEFAULT_TYPE = "DEFAULTELEMENTTYPE"; //$NON-NLS-1$

        private String id;

        private String objectClass;

        private String elementType;

        private String name;

//        ElementType(IConfigurationElement element) throws CoreException {
//            id = element.getAttribute(ATT_ID);
//            objectClass = getClassValue(element, ATT_OBJECTCLASS);
//            if (objectClass == null)
//                throw new CoreException(new Status(IStatus.ERROR, element
//                        .getNamespaceIdentifier(), 0,
//                        "Invalid extension (missing class name): " + id, //$NON-NLS-1$
//                        null));
//            name = element.getAttribute(ATT_NAME);
//            if (name == null)
//                name = ""; //$NON-NLS-1$
//        }

        Category(String id, String objectClass, String elementType,
                String name) {
            this.id = id;
            this.objectClass = objectClass;
            this.name = name;
            this.elementType = elementType;
        }

        public String getElementType() {
            return elementType;
        }

        public String getId() {
            return id;
        }

        public String getObjectClazz() {
            return objectClass;
        }

        public boolean belongsToThisCategory(Object o) {
            boolean belongs = Expressions.isInstanceOf(o, objectClass);
            if (belongs && o instanceof ITopic) {
                belongs = belongs && (((ITopic) o).getType() != null)
                        && (((ITopic) o).getType().equals(elementType)
                                || DEFAULT_TYPE.equals(elementType));
            }
            return belongs;
        }

        public String getName() {
            return name;
        }

    }

    private List<Category> categories = null;

    private String[] categoryIds = null;

    /* package */ CategoryManager() {
    }

//    public String getElementType(Object element) {
//        ensureLoaded();
//        for (ElementType type : elementTypes) {
//            if (type.isThisType(element))
//                return type.getId();
//        }
//        return null;
//    }

    public ICategoryAnalyzation analyze(Object[] elements) {
        return new CategoryAnalyzation(elements, this);
    }

    public String[] getAllCategories() {
        if (categoryIds == null) {
            ensureLoaded();
            int size = categories.size();
            categoryIds = new String[size];
            for (int i = 0; i < size; i++) {
                categoryIds[i] = categories.get(i).getId();
            }
        }
        return categoryIds;
    }

    private void ensureLoaded() {
        if (categories != null)
            return;
        lazyLoad();
        if (categories == null)
            categories = Collections.emptyList();
    }

    /**
     */
    private void lazyLoad() {
        register(new Category(MindMapUI.CATEGORY_CALLOUT,
                "org.xmind.core.ITopic", ITopic.CALLOUT, //$NON-NLS-1$
                MindMapMessages.Category_Callout));
        register(new Category(MindMapUI.CATEGORY_SUMMARY,
                "org.xmind.core.ITopic", ITopic.SUMMARY, //$NON-NLS-1$
                MindMapMessages.Category_Summary));
        register(new Category(MindMapUI.CATEGORY_TOPIC, "org.xmind.core.ITopic", //$NON-NLS-1$
                Category.DEFAULT_TYPE,
                MindMapMessages.Category_Topic));
        register(new Category(MindMapUI.CATEGORY_SHEET, "org.xmind.core.ISheet", //$NON-NLS-1$
                Category.DEFAULT_TYPE,
                MindMapMessages.Category_Sheet));
        register(new Category(MindMapUI.CATEGORY_BOUNDARY,
                "org.xmind.core.IBoundary", Category.DEFAULT_TYPE, //$NON-NLS-1$
                MindMapMessages.Category_Boundary));
        register(new Category(MindMapUI.CATEGORY_RELATIONSHIP,
                "org.xmind.core.IRelationship", Category.DEFAULT_TYPE,//$NON-NLS-1$
                MindMapMessages.Category_Relationship));
        register(new Category(MindMapUI.CATEGORY_MARKER,
                "org.xmind.core.marker.IMarkerRef", Category.DEFAULT_TYPE, //$NON-NLS-1$ 
                MindMapMessages.Category_Marker));
        register(new Category(MindMapUI.CATEGORY_IMAGE, "org.xmind.core.IImage", //$NON-NLS-1$
                Category.DEFAULT_TYPE,
                MindMapMessages.Category_Image));
//        if (Platform.isRunning()) {
//            readRegistry(Platform.getExtensionRegistry(), MindMapUI.PLUGIN_ID,
//                    RegistryConstants.EXT_ELEMENT_TYPES);
//        }
    }

//    protected boolean readElement(IConfigurationElement element) {
//        if (RegistryConstants.TAG_ELEMENT_TYPE.equals(element.getName())) {
//            readElementType(element);
//            return true;
//        }
//        return false;
//    }
//
//    private void readElementType(IConfigurationElement element) {
//        try {
//            ElementType elementType = new ElementType(element);
//            register(elementType);
//        } catch (CoreException e) {
//            Logger.log(e, "Failed to load ElementType: " + element.toString()); //$NON-NLS-1$
//        }
//    }

    private void register(Category category) {
        if (categories == null)
            categories = new ArrayList<Category>();
        categories.add(category);
    }

    public String getCategoryName(String categoryId) {
        ensureLoaded();
        Category category = getCategory(categoryId);
        if (category != null)
            return category.getName();
        if (MULTIPLE_CATEGORIES.equals(categoryId))
            return MindMapMessages.MultipleObjects;
        if (NO_CATEGORY.equals(categoryId))
            return MindMapMessages.NoObject;
        if (UNKNOWN_CATEGORY.equals(categoryId))
            return MindMapMessages.UnknownObjects;
        return ""; //$NON-NLS-1$
    }

    public boolean belongsToCategory(Object element, String categoryId) {
        Category category = getCategory(categoryId);
        if (category != null) {
            return category.belongsToThisCategory(element);
        }
        return false;
    }

    private Category getCategory(String categoryId) {
        if (categoryId == null)
            return null;

        ensureLoaded();
        for (Category type : categories) {
            if (categoryId.equals(type.getId()))
                return type;
        }
        return null;
    }

}
