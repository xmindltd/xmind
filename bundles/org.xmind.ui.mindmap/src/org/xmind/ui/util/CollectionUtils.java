/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.IFilter;

/**
 * @author Frank Shaka
 */
public class CollectionUtils {

    private CollectionUtils() {
    }

    public static final class FilteredCollection<E> extends
            AbstractCollection<E> implements java.util.Collection<E>,
            Serializable {

        private static final long serialVersionUID = 359553173125286125L;

        private IFilter filter;
        private Collection<E> c;
        private Collection<E> remainders;

        public FilteredCollection(Collection<E> c, IFilter filter) {
            this.c = c;
            this.filter = filter;
            refilter();
        }

        @SuppressWarnings("unchecked")
        private void refilter() {
            remainders = null;
            try {
                Class collectionClass = c.getClass();
                remainders = (Collection<E>) collectionClass.newInstance();
            } catch (Throwable e) {
            }
            if (remainders == null)
                remainders = new ArrayList<E>();
            for (E e : c) {
                if (!isFilteredOut(e))
                    remainders.add(e);
            }
        }

        private boolean isFilteredOut(Object e) {
            return filter != null && !filter.select(e);
        }

        @Override
        public Iterator<E> iterator() {
            return remainders.iterator();
        }

        @Override
        public int size() {
            return remainders.size();
        }

        @Override
        public boolean add(E e) {
            boolean added = c.add(e);
            if (added)
                refilter();
            return added;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = c.remove(o);
            if (removed)
                refilter();
            return removed;
        }

        @Override
        public int hashCode() {
            return remainders.hashCode();
        }

        @Override
        public String toString() {
            return remainders.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return remainders.equals(obj);
        }

    }

    public static final <T> Collection<T> filtered(Collection<T> c,
            IFilter filter) {
        return new FilteredCollection<T>(c, filter);
    }

}