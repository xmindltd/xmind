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
import java.util.HashMap;
import java.util.Map;

import org.xmind.core.IIdentifiable;

public class ElementRegistry {

    private Map<Object, Object> map = new HashMap<Object, Object>();

    public ElementRegistry() {
    }

    public Object getElement(Object key) {
        return map.get(key);
    }

    public Collection<Object> getRegisteredElements() {
        return map.values();
    }

    public void registerByKey(Object key, Object element) {
        map.put(key, element);
    }

    public void unregisterByKey(Object key) {
        map.remove(key);
    }

    public void register(IIdentifiable element) {
        if (element != null) {
            registerByKey(element.getId(), element);
        }
    }

    public void unregister(IIdentifiable element) {
        if (element != null) {
            unregisterByKey(element.getId());
        }
    }

}