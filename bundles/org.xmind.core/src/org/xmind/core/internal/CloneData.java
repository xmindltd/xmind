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
package org.xmind.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.ICloneData;

public class CloneData implements ICloneData {

    private static class CategorizedString {
        private String category;
        private String source;

        public CategorizedString(String category, String source) {
            this.category = category;
            this.source = source;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof CategorizedString))
                return false;
            CategorizedString that = (CategorizedString) obj;
            return (this.category == that.category || (this.category != null
                    && this.category.equals(that.category)))
                    && (this.source == that.source || (this.source != null
                            && this.source.equals(that.source)));
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            if (category == null || source == null) {
                return super.hashCode();
            }
            return this.category.hashCode() ^ this.source.hashCode();
        }
    }

    private Collection<Object> sources;

    private ICloneData parent;

    private Map<Object, Object> clonedElements = new HashMap<Object, Object>();

    private Map<Object, List<ICloneDataListener>> listeners = new HashMap<Object, List<ICloneDataListener>>();

    private Map<Object, Object> caches = new HashMap<Object, Object>();

    public CloneData(Collection<? extends Object> sources, ICloneData parent) {
        this.sources = new ArrayList<Object>(sources);
        this.parent = parent;
    }

    public String getString(String category, String source) {
        return (String) get(new CategorizedString(category, source));
    }

    public void putString(String category, String source, String cloned) {
        CategorizedString sourceObj = new CategorizedString(category, source);
        doPut(sourceObj, cloned);
        fireStringCloned(sourceObj, category, source, cloned);
    }

    public Object get(Object source) {
        Object cloned = clonedElements.isEmpty() ? null
                : clonedElements.get(source);
        if (cloned == null && parent != null)
            cloned = parent.get(source);
        return cloned;
    }

    public boolean hasCloned() {
        if (!clonedElements.isEmpty()) {
            for (Object cloned : clonedElements.values())
                if (cloned != null)
                    return true;
        }
        if (parent != null)
            return parent.hasCloned();
        return false;
    }

    public Collection<Object> getCloneds() {
        ArrayList<Object> list = new ArrayList<Object>(sources.size());
        for (Object source : sources) {
            Object cloned = get(source);
            if (cloned != null)
                list.add(cloned);
        }
        return list;
    }

    public Collection<Object> getSources() {
        return sources;
    }

    public ICloneData getParent() {
        return parent;
    }

    public void put(Object source, Object cloned) {
        doPut(source, cloned);
        fireObjectCloned(source, cloned);
    }

    protected void doPut(Object source, Object cloned) {
        clonedElements.put(source, cloned);
        if (parent != null) {
            parent.put(source, cloned);
        }
    }

    public void cache(Object key, Object value) {
        caches.put(key, value);
    }

    public Object getCache(Object key) {
        return caches.isEmpty() ? null : caches.get(key);
    }

    private void fireObjectCloned(Object source, Object cloned) {
        if (listeners.isEmpty())
            return;
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null || list.isEmpty()) {
            listeners.remove(source);
            return;
        }
        for (Object o : list.toArray()) {
            ((ICloneDataListener) o).objectCloned(source, cloned);
        }
    }

    private void fireStringCloned(Object sourceObj, String category,
            String source, String cloned) {
        if (listeners.isEmpty())
            return;
        List<ICloneDataListener> list = listeners.get(sourceObj);
        if (list == null || list.isEmpty()) {
            listeners.remove(sourceObj);
            return;
        }
        for (Object o : list.toArray()) {
            ((ICloneDataListener) o).stringCloned(category, source, cloned);
        }
    }

    public void addCloneDataListener(Object source,
            ICloneDataListener listener) {
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null) {
            list = new ArrayList<ICloneDataListener>();
            listeners.put(source, list);
        }
        list.add(listener);
    }

    public void removeCloneDataListener(Object source,
            ICloneDataListener listener) {
        if (listeners.isEmpty())
            return;
        List<ICloneDataListener> list = listeners.get(source);
        if (list == null)
            return;
        list.remove(listener);
        if (list.isEmpty())
            listeners.remove(source);
    }

    public void addCloneDataListener(String category, String source,
            ICloneDataListener listener) {
        addCloneDataListener(new CategorizedString(category, source), listener);
    }

    public void removeCloneDataListener(String category, String source,
            ICloneDataListener listener) {
        removeCloneDataListener(new CategorizedString(category, source),
                listener);
    }

    public boolean isCloned(Object source) {
        if (clonedElements != null && clonedElements.containsKey(source))
            return true;
        return parent != null && parent.isCloned(source);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICloneData#isCloned(java.lang.String,
     * java.lang.String)
     */
    public boolean isCloned(String category, String source) {
        return isCloned(new CategorizedString(category, source));
    }

//    public String getString(String sourceString) {
//        Object cloned = clonedElements == null ? null : clonedElements
//                .get(sourceString);
//        if ((cloned == null || !(cloned instanceof String)) && parent != null) {
//            cloned = parent.get(sourceString);
//        }
//        return cloned instanceof String ? (String) cloned : null;
//    }

}
