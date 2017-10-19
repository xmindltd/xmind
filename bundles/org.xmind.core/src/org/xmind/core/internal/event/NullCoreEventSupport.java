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

import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;

public class NullCoreEventSupport implements ICoreEventSupport {

    private static final NullCoreEventSupport instance = new NullCoreEventSupport();

    private NullCoreEventSupport() {
    }

    public void dispatch(ICoreEventSource source, CoreEvent event) {
    }

    public void dispatchIndexedTargetChange(ICoreEventSource source,
            String eventType, Object target, int index) {
    }

    public void dispatchIndexedValueChange(ICoreEventSource source,
            String eventType, Object oldValue, Object newValue, int index) {
    }

    public void dispatchTargetChange(ICoreEventSource source, String eventType,
            Object target) {
    }

    public void dispatchTargetValueChange(ICoreEventSource source,
            String eventType, Object target, Object oldValue, Object newValue) {
    }

    public void dispatchValueChange(ICoreEventSource source, String eventType,
            Object oldValue, Object newValue) {
    }

    public ICoreEventRegistration registerCoreEventListener(
            ICoreEventSource source, String eventType,
            ICoreEventListener listener) {
        return NullCoreEventRegistration.getInstance();
    }

    public ICoreEventRegistration registerGlobalListener(String eventType,
            ICoreEventListener listener) {
        return NullCoreEventRegistration.getInstance();
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
        return NullCoreEventRegistration.getInstance();
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
        return NullCoreEventRegistration.getInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSupport#hasListeners(org.xmind.core.event
     * .ICoreEventSource, java.lang.String)
     */
    public boolean hasListeners(ICoreEventSource source, String eventType) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSupport#hasOnceListeners(org.xmind.core
     * .event.ICoreEventSource, java.lang.String)
     */
    public boolean hasOnceListeners(ICoreEventSource source, String eventType) {
        return false;
    }

    public static NullCoreEventSupport getInstance() {
        return instance;
    }

}