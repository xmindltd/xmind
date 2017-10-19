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

package org.xmind.gef.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;

/**
 * @author Frank Shaka
 * 
 */
public class EventListenerSupport {

    private Map<Object, List<Object>> map = new HashMap<Object, List<Object>>();

    public void addListener(Object type, Object listener) {
        List<Object> list = map.get(type);
        if (list == null) {
            list = new ArrayList<Object>();
            map.put(type, list);
        }
        list.add(listener);
    }

    public void removeListener(Object type, Object listener) {
        List<Object> list = map.get(type);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                map.remove(type);
            }
        }
    }

    public void clear(Object type) {
        map.remove(type);
    }

    public void clearAll() {
        map.clear();
    }

    public boolean isEmpty(Object type) {
        List<Object> list = map.get(type);
        return list == null || list.isEmpty();
    }

    public void fireEvent(Object type, final IEventDispatcher dispatcher) {
        List<Object> list = map.get(type);
        if (list != null && !list.isEmpty()) {
            Object[] listeners = list.toArray();
            for (int i = 0; i < listeners.length; i++) {
                final Object listener = listeners[i];
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        dispatcher.dispatch(listener);
                    }
                });
            }
        }
    }

}
