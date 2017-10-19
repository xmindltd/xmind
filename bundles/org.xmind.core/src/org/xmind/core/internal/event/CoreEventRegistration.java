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

import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;

public class CoreEventRegistration implements ICoreEventRegistration {

    private CoreEventSupport dispatcher;

    private ICoreEventSource source;

    private String eventType;

    private ICoreEventListener listener;

    private boolean once;

    private boolean valid = true;

    public CoreEventRegistration(CoreEventSupport dispatcher,
            ICoreEventSource source, String eventType,
            ICoreEventListener listener) {
        this(dispatcher, source, eventType, listener, false);
    }

    public CoreEventRegistration(CoreEventSupport dispatcher,
            ICoreEventSource source, String eventType,
            ICoreEventListener listener, boolean once) {
        this.dispatcher = dispatcher;
        this.source = source;
        this.eventType = eventType;
        this.listener = listener;
        this.once = once;
    }

    public ICoreEventSource getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public ICoreEventListener getListener() {
        return listener;
    }

    /**
     * @return the once
     */
    public boolean isOnce() {
        return once;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.event.ICoreEventRegistration#isValid()
     */
    public boolean isValid() {
        return valid;
    }

    public void unregister() {
        dispatcher.unregister(this);
        valid = false;
    }

}