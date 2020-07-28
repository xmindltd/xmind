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
package org.xmind.core.internal.dom;

import java.util.ArrayList;
import java.util.Collection;

import org.xmind.core.Core;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.AbstractRefCounter;
import org.xmind.core.util.ILabelRefCounter;

public class SheetLabelRefCounter extends AbstractRefCounter implements
        ILabelRefCounter, ICoreEventSource {

    private SheetImpl ownedSheet;

    private Collection<String> oldValue = null;

    public SheetLabelRefCounter(SheetImpl ownedSheet) {
        this.ownedSheet = ownedSheet;
    }

    protected Object findResource(String resourceId) {
        return this;
    }

    public void increaseRef(String resourceId) {
        oldValue = new ArrayList<String>(getCountedRefs());
        super.increaseRef(resourceId);
    }

    protected void postIncreaseRef(String resourceId, Object resource) {
        if (this.oldValue != null) {
            Collection<String> oldValue = this.oldValue;
            Collection<String> newValue = new ArrayList<String>(
                    getCountedRefs());
            this.oldValue = null;
            fireValueChange(Core.ResourceRefs, oldValue, newValue);
        }
    }

    public void decreaseRef(String resourceId) {
        oldValue = new ArrayList<String>(getCountedRefs());
        super.decreaseRef(resourceId);
    }

    protected void postDecreaseRef(String resourceId, Object resource) {
        if (this.oldValue != null) {
            Collection<String> oldValue = this.oldValue;
            Collection<String> newValue = new ArrayList<String>(
                    getCountedRefs());
            this.oldValue = null;
            fireValueChange(Core.ResourceRefs, oldValue, newValue);
        }
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedSheet.getCoreEventSupport();
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    private void fireValueChange(String eventType, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, eventType, oldValue,
                newValue);
    }

}