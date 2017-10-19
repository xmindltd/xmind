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
import java.util.List;

import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegistration;

/**
 * @author frankshaka
 * 
 */
public class CoreEventRegistrationList {

    public static final int ONLY_ONCE = 1;

    public static final int ONLY_NORMAL = 1 << 1;

    public static final int ALL = ONLY_NORMAL | ONLY_ONCE;

    private List<CoreEventRegistration> regs;

    private int numOnceRegs = 0;

    public CoreEventRegistrationList() {
    }

    public void add(CoreEventRegistration reg) {
        if (reg == null)
            return;

        if (regs == null) {
            regs = new ArrayList<CoreEventRegistration>(4);
        }
        regs.add(reg);
        if (reg.isOnce()) {
            numOnceRegs++;
        }
    }

    public void remove(CoreEventRegistration reg) {
        if (reg == null || this.regs == null)
            return;

        boolean isRemoved = regs.remove(reg);
        if (reg.isOnce() && isRemoved) {
            numOnceRegs--;
        }
        if (regs.isEmpty()) {
            regs = null;
        }
    }

    public boolean isEmpty() {
        return regs == null || regs.isEmpty();
    }

    public void fireCoreEvent(CoreEvent e, int group) {
        if (regs == null)
            return;

        Object[] array = regs.toArray();
        for (Object obj : array) {
            CoreEventRegistration reg = (CoreEventRegistration) obj;
            if (group == ALL
                    || ((group == ONLY_ONCE && reg.isOnce()) || (group == ONLY_NORMAL && !reg
                            .isOnce()))) {
                reg.getListener().handleCoreEvent(e);
                if (reg.isOnce()) {
                    reg.unregister();
                }
            }
        }
    }

    public boolean hasRegistration(ICoreEventRegistration reg) {
        return regs != null && regs.contains(reg);
    }

    public boolean hasOnceRegistration() {
        return numOnceRegs > 0;
    }

}
