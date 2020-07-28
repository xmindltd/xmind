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
package org.xmind.ui.internal.mindmap;

import java.util.HashMap;
import java.util.Map;

import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.mindmap.ICacheValueProvider;

public class CacheManager implements ICacheManager {

    private Map<String, Object> caches = null;

    private Map<String, ICacheValueProvider> valueProviders = null;

    private IPart part;

    public CacheManager(IPart part) {
        this.part = part;
    }

    public void flush(String key) {
        if (caches == null)
            return;
        caches.remove(key);
    }

    public Object getCache(String key) {
        if (caches == null || key == null)
            return null;
        Object cache = caches.get(key);
        if (cache == null) {
            ICacheValueProvider valueProvider = getValueProvider(key);
            if (valueProvider != null) {
                cache = valueProvider.getValue(part, key);
                setCache(key, cache);
            }
        }
        return cache;
    }

    public void setCache(String key, Object cache) {
        if (key == null)
            return;
        if (caches == null)
            caches = new HashMap<String, Object>();
        if (cache == null)
            caches.remove(key);
        else
            caches.put(key, cache);
    }

    public ICacheValueProvider getValueProvider(String key) {
        if (valueProviders == null)
            return null;
        return valueProviders.get(key);
    }

    public void setValueProvider(String key, ICacheValueProvider valueProvider) {
        if (key == null || valueProvider == null)
            return;
        if (valueProviders == null)
            valueProviders = new HashMap<String, ICacheValueProvider>();
        valueProviders.put(key, valueProvider);
    }

    public void removeValueProvider(String key) {
        if (key == null || valueProviders == null)
            return;
        valueProviders.remove(key);
    }

}