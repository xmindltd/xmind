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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;

public class CoreEventSupport implements ICoreEventSupport {

    private static class CoreEventManager {

        private Map<String, CoreEventRegistrationList> regs;

        public void add(CoreEventRegistration reg) {
            regs = addListener(reg.getEventType(), reg, regs);
        }

        private Map<String, CoreEventRegistrationList> addListener(String type,
                CoreEventRegistration reg,
                Map<String, CoreEventRegistrationList> map) {
            if (map == null)
                map = new HashMap<String, CoreEventRegistrationList>();
            CoreEventRegistrationList list = map.get(type);
            if (list == null) {
                list = new CoreEventRegistrationList();
                map.put(type, list);
            }
            list.add(reg);
            return map;
        }

        public void remove(CoreEventRegistration reg) {
            regs = removeListener(reg.getEventType(), reg, regs);
        }

        private Map<String, CoreEventRegistrationList> removeListener(
                String type, CoreEventRegistration reg,
                Map<String, CoreEventRegistrationList> map) {
            if (map != null) {
                CoreEventRegistrationList list = map.get(type);
                if (list != null) {
                    list.remove(reg);
                    if (list.isEmpty()) {
                        map.remove(type);
                    }
                }
                if (map.isEmpty()) {
                    map = null;
                }
            }
            return map;
        }

        public boolean isEmpty() {
            return regs == null || regs.isEmpty();
        }

        public void dispatchCoreEvent(String type, CoreEvent event) {
            if (regs != null) {
                CoreEventRegistrationList list = regs.get(type);
                if (list != null) {
                    if (list.hasOnceRegistration()) {
                        list.fireCoreEvent(event,
                                CoreEventRegistrationList.ONLY_ONCE);
                    } else {
                        list
                                .fireCoreEvent(event,
                                        CoreEventRegistrationList.ALL);
                    }
                }
            }
        }

        public void dispatchCoreEvent(String type, CoreEvent event,
                int eventGroup) {
            dispatchCoreEvent(type, event, eventGroup, regs);
        }

        private Map<String, CoreEventRegistrationList> dispatchCoreEvent(
                String type, CoreEvent event, int eventGroup,
                Map<String, CoreEventRegistrationList> map) {
            if (map != null) {
                CoreEventRegistrationList list = map.get(type);
                if (list != null) {
                    list.fireCoreEvent(event, eventGroup);
                }
            }
            return map;
        }

        public boolean hasListeners(String type) {
            return regs != null && regs.containsKey(type);
        }

        public boolean hasOnceListeners(String type) {
            if (regs == null)
                return false;
            CoreEventRegistrationList list = regs.get(type);
            return list != null && list.hasOnceRegistration();
        }

    }

    private ICoreEventSupport parent;

    private Map<ICoreEventSource, CoreEventManager> managers;

    private CoreEventManager globalManager;

    public ICoreEventSupport getParent() {
        return parent;
    }

    public void setParent(ICoreEventSupport parent) {
        this.parent = parent;
    }

    public ICoreEventRegistration registerCoreEventListener(
            ICoreEventSource source, String eventType,
            ICoreEventListener listener) {
        return registerListener(source, eventType, listener, false);
    }

    private ICoreEventRegistration registerListener(ICoreEventSource source,
            String eventType, ICoreEventListener listener, boolean once) {
        CoreEventRegistration reg = new CoreEventRegistration(this, source,
                eventType, listener, once);
        if (managers == null)
            managers = new HashMap<ICoreEventSource, CoreEventManager>();
        CoreEventManager manager = managers.get(source);
        if (manager == null) {
            manager = new CoreEventManager();
            managers.put(source, manager);
        }
        manager.add(reg);
        return reg;
    }

    public ICoreEventRegistration registerGlobalListener(String eventType,
            ICoreEventListener listener) {
        return registerGlobalListener(eventType, listener, false);
    }

    private ICoreEventRegistration registerGlobalListener(String eventType,
            ICoreEventListener listener, boolean once) {
        CoreEventRegistration reg = new CoreEventRegistration(this, null,
                eventType, listener, once);
        if (globalManager == null)
            globalManager = new CoreEventManager();
        globalManager.add(reg);
        return reg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSupport#registerOnceCoreEventListener(
     * org.xmind.core.event.ICoreEventSource, java.lang.String,
     * org.xmind.core.event.ICoreEventListener)
     */
    public ICoreEventRegistration registerOnceCoreEventListener(
            ICoreEventSource source, String eventType,
            ICoreEventListener listener) {
        ICoreEventRegistration reg = registerListener(source, eventType,
                listener, true);
        dispatch(source, eventType, new CoreEvent(source, eventType, null,
                listener), false);
        return reg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSupport#registerOnceGlobalListener(java
     * .lang.String, org.xmind.core.event.ICoreEventListener)
     */
    public ICoreEventRegistration registerOnceGlobalListener(String eventType,
            ICoreEventListener listener) {
        ICoreEventRegistration reg = registerGlobalListener(eventType,
                listener, true);
        if (managers != null) {
            for (Entry<ICoreEventSource, CoreEventManager> entry : managers
                    .entrySet()) {
                if (entry.getValue().hasListeners(eventType)) {
                    ICoreEventSource source = entry.getKey();
                    dispatch(source, eventType, new CoreEvent(source,
                            eventType, listener), false);
                }
            }
        }
        return reg;
    }

    protected void unregister(CoreEventRegistration reg) {
        ICoreEventSource source = reg.getSource();
        if (source == null) {
            unregisterGlobalRegistration(reg);
            return;
        }

        if (managers == null || managers.isEmpty())
            return;

        CoreEventManager manager = managers.get(source);
        if (manager == null)
            return;

        manager.remove(reg);
        if (manager.isEmpty()) {
            managers.remove(source);
        }
        if (managers.isEmpty()) {
            managers = null;
        }

        if (reg.isOnce()) {
            String eventType = reg.getEventType();
            ICoreEventListener listener = reg.getListener();
            dispatch(source, eventType, new CoreEvent(source, eventType,
                    listener, null), false);
        }
    }

    private void unregisterGlobalRegistration(CoreEventRegistration reg) {
        if (globalManager != null) {
            globalManager.remove(reg);
        }
        if (globalManager.isEmpty()) {
            globalManager = null;
        }
    }

    public void dispatchValueChange(ICoreEventSource source, String eventType,
            Object oldValue, Object newValue) {
        if (oldValue == newValue
                || (oldValue != null && oldValue.equals(newValue)))
            return;

        dispatch(source, eventType, new CoreEvent(source, eventType, oldValue,
                newValue), true);
    }

    public void dispatchIndexedValueChange(ICoreEventSource source,
            String eventType, Object oldValue, Object newValue, int index) {
        if (oldValue == newValue
                || (oldValue != null && oldValue.equals(newValue)))
            return;

        dispatch(source, eventType, new CoreEvent(source, eventType, oldValue,
                newValue, index), true);
    }

    public void dispatchIndexedTargetChange(ICoreEventSource source,
            String eventType, Object target, int index) {
        dispatch(source, eventType, new CoreEvent(source, eventType, target,
                index), true);
    }

    public void dispatchTargetChange(ICoreEventSource source, String eventType,
            Object target) {
        dispatch(source, eventType, new CoreEvent(source, eventType, target),
                true);
    }

    public void dispatchTargetValueChange(ICoreEventSource source,
            String eventType, Object target, Object oldValue, Object newValue) {
        dispatch(source, eventType, new CoreEvent(source, eventType, target,
                oldValue, newValue), true);
    }

    public void dispatch(ICoreEventSource source, CoreEvent event) {
        String eventType = event.getType();
        dispatch(source, eventType, event, true);
    }

    private void dispatch(ICoreEventSource source, String eventType,
            CoreEvent event, boolean notifyOnceListeners) {
        dispatchBySource(source, eventType, event, notifyOnceListeners);
        dispatchGlobal(eventType, event, notifyOnceListeners);
    }

    private void dispatchBySource(ICoreEventSource source, String eventType,
            CoreEvent event, boolean notifyOnceListeners) {
        if (managers == null || managers.isEmpty())
            return;

        CoreEventManager manager = managers.get(source);
        if (manager == null || manager.isEmpty())
            return;

        if (notifyOnceListeners) {
            manager.dispatchCoreEvent(eventType, event);
        } else {
            manager.dispatchCoreEvent(eventType, event,
                    CoreEventRegistrationList.ONLY_NORMAL);
        }
    }

    private void dispatchGlobal(String eventType, CoreEvent event,
            boolean notifyOnceListeners) {
        if (globalManager != null) {
            if (notifyOnceListeners) {
                globalManager.dispatchCoreEvent(eventType, event);
            } else {
                globalManager.dispatchCoreEvent(eventType, event,
                        CoreEventRegistrationList.ONLY_NORMAL);
            }
        }
        if (getParent() != null && getParent() instanceof CoreEventSupport) {
            ((CoreEventSupport) getParent()).dispatchGlobal(eventType, event,
                    notifyOnceListeners);
        }
    }

    public void dispose() {
        if (managers != null) {
            managers.clear();
            managers = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(ICoreEventSource source, String eventType) {
        if (managers == null)
            return false;
        CoreEventManager manager = managers.get(source);
        return manager != null && manager.hasListeners(eventType);
    }

    public boolean hasOnceListeners(ICoreEventSource source, String eventType) {
        if (managers == null)
            return false;
        CoreEventManager manager = managers.get(source);
        return manager != null && manager.hasOnceListeners(eventType);
    }

}