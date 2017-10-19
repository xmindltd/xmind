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
package org.xmind.core.event;

public interface ICoreEventSupport {

    ICoreEventRegistration registerCoreEventListener(ICoreEventSource source,
            String eventType, ICoreEventListener listener);

    ICoreEventRegistration registerGlobalListener(String eventType,
            ICoreEventListener listener);

    ICoreEventRegistration registerOnceCoreEventListener(
            ICoreEventSource source, String eventType,
            ICoreEventListener listener);

    ICoreEventRegistration registerOnceGlobalListener(String eventType,
            ICoreEventListener listener);

    void dispatchValueChange(ICoreEventSource source, String eventType,
            Object oldValue, Object newValue);

    void dispatchIndexedValueChange(ICoreEventSource source, String eventType,
            Object oldValue, Object newValue, int index);

    void dispatchTargetChange(ICoreEventSource source, String eventType,
            Object target);

    void dispatchIndexedTargetChange(ICoreEventSource source, String eventType,
            Object target, int index);

    void dispatchTargetValueChange(ICoreEventSource source, String eventType,
            Object target, Object oldValue, Object newValue);

    void dispatch(ICoreEventSource source, CoreEvent event);

}