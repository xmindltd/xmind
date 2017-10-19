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
package org.xmind.core.internal.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;

/**
 * 
 * @author frankshaka
 * @deprecated Use CoreEventRegistrationList
 */
public class CoreEventListenerList {

    private List<ICoreEventListener> listeners;

    public CoreEventListenerList(ICoreEventListener listener) {
        this.listeners = new ArrayList<ICoreEventListener>(4);
        this.listeners.add(listener);
    }

    public CoreEventListenerList(ICoreEventListener[] listeners) {
        this.listeners = new ArrayList<ICoreEventListener>(listeners.length);
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public void add(ICoreEventListener listener) {
        if (listener == null)
            return;

        if (listeners == null) {
            listeners = new ArrayList<ICoreEventListener>(4);
        }
        listeners.add(listener);
    }

    public void remove(ICoreEventListener listener) {
        if (listener == null || this.listeners == null)
            return;

        listeners.remove(listener);
    }

    public boolean isEmpty() {
        return listeners == null || listeners.isEmpty();
    }

    public void fireCoreEvent(CoreEvent e) {
        if (listeners == null)
            return;

        Object[] list = listeners.toArray();
        for (Object listener : list) {
            ((ICoreEventListener) listener).handleCoreEvent(e);
        }
    }

    public boolean hasListener(ICoreEventListener listener) {
        return listeners != null && listeners.contains(listener);
    }

}