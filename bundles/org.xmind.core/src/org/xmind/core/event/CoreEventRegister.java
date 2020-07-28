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
package org.xmind.core.event;

import org.xmind.core.IAdaptable;
import org.xmind.core.internal.event.NullCoreEventRegistration;

public class CoreEventRegister extends CoreEventRegisterBase {

    private ICoreEventSource source;

    private ICoreEventSupport support;

    private ICoreEventListener listener;

    public CoreEventRegister() {
    }

    public CoreEventRegister(ICoreEventSource source) {
        this.source = source;
        this.support = null;
        this.listener = null;
    }

    public CoreEventRegister(ICoreEventSupport support) {
        this.support = support;
        this.source = null;
        this.listener = null;
    }

    public CoreEventRegister(ICoreEventListener listener) {
        this.source = null;
        this.support = null;
        this.listener = listener;
    }

    public CoreEventRegister(ICoreEventSource source,
            ICoreEventListener listener) {
        this.source = source;
        this.support = null;
        this.listener = listener;
    }

    public CoreEventRegister(ICoreEventSupport support,
            ICoreEventListener listener) {
        this.source = null;
        this.support = support;
        this.listener = listener;
    }

    public CoreEventRegister(Object source, ICoreEventListener listener) {
        this.source = (ICoreEventSource) getAdapter(source,
                ICoreEventSource.class);
        if (this.source == null) {
            this.support = (ICoreEventSupport) getAdapter(source,
                    ICoreEventSupport.class);
        } else {
            this.support = null;
        }
        this.listener = listener;
    }

    public ICoreEventRegistration register(String eventType) {
        if (listener == null)
            return NullCoreEventRegistration.getInstance();

        ICoreEventRegistration reg;
        if (source != null) {
            reg = source.registerCoreEventListener(eventType, listener);
            addRegistration(reg);
        } else if (support != null) {
            reg = support.registerGlobalListener(eventType, listener);
            addRegistration(reg);
        } else {
            reg = NullCoreEventRegistration.getInstance();
        }
        return reg;
    }

    public void setNextListener(ICoreEventListener listener) {
        this.listener = listener;
    }

    public void setNextSource(ICoreEventSource source) {
        this.source = source;
        this.support = null;
    }

    public void setNextSupport(ICoreEventSupport support) {
        this.source = null;
        this.support = support;
    }

    public void setNextSourceFrom(Object source) {
        this.source = (ICoreEventSource) getAdapter(source,
                ICoreEventSource.class);
        if (this.source == null) {
            this.support = (ICoreEventSupport) getAdapter(source,
                    ICoreEventSupport.class);
        } else {
            this.support = null;
        }
    }

    private static Object getAdapter(Object adaptable, Class<?> adapter) {
        if (adapter.isInstance(adaptable))
            return adaptable;
        if (IAdaptable.class.isInstance(adaptable)) {
            return ((IAdaptable) adaptable).getAdapter(adapter);
        }
        return null;
    }

}