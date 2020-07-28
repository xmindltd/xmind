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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;

public class CategoryAnalyzation implements ICategoryAnalyzation {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private Object[] elements;

    private CategoryManager manager;

    private Map<String, List<Object>> map = null;

    private String mainCategory = null;

    /* package */CategoryAnalyzation(Object[] elements, CategoryManager manager) {
        this.elements = elements;
        this.manager = manager;
    }

    public Object[] getElements() {
        return elements;
    }

    public boolean isEmpty() {
        return ICategoryManager.NO_CATEGORY.equals(getMainCategory());
    }

    public String getMainCategory() {
        checkAnalyze();
        return mainCategory;
    }

    public int size() {
        return elements.length;
    }

    private void checkAnalyze() {
        if (mainCategory == null || map == null)
            analyze();
    }

    private void analyze() {
        String[] categories = manager.getAllCategories();
        for (Object o : elements) {
            String t = getCategory(o, categories);
            if (t == null)
                t = ICategoryManager.UNKNOWN_CATEGORY;
            if (mainCategory == null) {
                mainCategory = t;
            } else if (!ICategoryManager.MULTIPLE_CATEGORIES
                    .equals(mainCategory)
                    && !mainCategory.equals(t)) {
                mainCategory = ICategoryManager.MULTIPLE_CATEGORIES;
            }
            if (map == null) {
                map = new HashMap<String, List<Object>>();
            }
            List<Object> list = map.get(t);
            if (list == null) {
                list = new ArrayList<Object>();
                map.put(t, list);
            }
            list.add(o);
        }
        if (mainCategory == null) {
            mainCategory = ICategoryManager.NO_CATEGORY;
        }
        if (map == null) {
            map = Collections.emptyMap();
        }
    }

    private String getCategory(Object o, String[] categories) {
        for (String category : categories) {
            if (manager.belongsToCategory(o, category))
                return category;
        }
        return null;
    }

    public String[] getMultipleCategories() {
        checkAnalyze();
        Set<String> categories = map.keySet();
        return categories.toArray(new String[categories.size()]);
    }

    public Object[] getElementsByCategory(String type) {
        checkAnalyze();
        List<Object> list = map.get(type);
        if (list == null)
            return EMPTY_ARRAY;
        return list.toArray();
    }

    public int getNumElementsByCategory(String type) {
        checkAnalyze();
        List<Object> list = map.get(type);
        return list == null ? 0 : list.size();
    }

    public boolean isMultiple() {
        return ICategoryManager.MULTIPLE_CATEGORIES.equals(getMainCategory());
    }

}