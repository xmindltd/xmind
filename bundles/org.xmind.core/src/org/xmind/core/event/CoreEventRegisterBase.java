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

import java.util.ArrayList;
import java.util.List;


public abstract class CoreEventRegisterBase implements ICoreEventRegister {

    private List<ICoreEventRegistration> regs = null;

    protected void addRegistration(ICoreEventRegistration reg) {
        if (reg == null)
            return;
        if (regs == null)
            regs = new ArrayList<ICoreEventRegistration>();
        regs.add(reg);
    }

    public void unregisterAll() {
        if (regs != null) {
            for (ICoreEventRegistration reg : regs) {
                reg.unregister();
            }
            regs = null;
        }
    }

    public boolean hasRegistration() {
        return regs != null && !regs.isEmpty();
    }

}