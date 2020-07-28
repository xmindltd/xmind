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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.xmind.core.util.IRefCounter;

public abstract class AbstractRefCounter implements IRefCounter {

    private static final Set<String> NO_RESOURCE_IDS = Collections.emptySet();

    private Map<String, Integer> counts = null;

    public Collection<String> getRefs() {
        if (counts == null)
            return NO_RESOURCE_IDS;
        return counts.keySet();
    }

    public Collection<String> getCountedRefs() {
        if (counts == null)
            return NO_RESOURCE_IDS;
        Set<String> list = new HashSet<String>();
        for (Entry<String, Integer> en : counts.entrySet()) {
            if (en.getValue() != null && en.getValue().intValue() > 0) {
                list.add(en.getKey());
            }
        }
        return list;
    }

    public void increaseRef(String resourceId) {
        Object resource = findResource(resourceId);
        if (resource == null)
            return;

        if (counts == null)
            counts = new HashMap<String, Integer>();
        Integer c = counts.get(resourceId);
        if (c == null) {
            c = Integer.valueOf(1);
        } else {
            c = Integer.valueOf(c.intValue() + 1);
        }
        counts.put(resourceId, c);
        postIncreaseRef(resourceId, resource);
    }

    public void decreaseRef(String resourceId) {
        if (counts == null)
            return;

        Object resource = findResource(resourceId);
        if (resource == null)
            return;

        Integer c = counts.get(resourceId);
        if (c == null || c.intValue() <= 0)
            return;

        counts.put(resourceId, Integer.valueOf(c.intValue() - 1));
        postDecreaseRef(resourceId, resource);
    }

    public int getRefCount(String resourceId) {
        if (counts == null)
            return 0;
        Integer c = counts.get(resourceId);
        if (c == null)
            return 0;
        return c.intValue();
    }

    protected abstract Object findResource(String resourceId);

    protected abstract void postIncreaseRef(String resourceId, Object resource);

    protected abstract void postDecreaseRef(String resourceId, Object resource);

}