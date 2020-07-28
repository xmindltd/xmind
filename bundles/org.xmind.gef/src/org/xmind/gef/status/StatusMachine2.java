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
package org.xmind.gef.status;

/**
 * @author Brian Sun
 * @version 2005
 */
public class StatusMachine2 extends AbstractStatusMachine {

    private int status = 0;

    public StatusMachine2() {
        super();
    }

    public StatusMachine2(Object source) {
        super(source);
    }

    /**
     * @see cn.brainy.gef.status.IStatusMachine#setStatus(int, boolean)
     */
    public final void setStatus(int key, boolean value) {
        if (!isStatus(key) == !value)
            return;

        if (value)
            status = status | key;
        else
            status = status & ~key;
        fireStatusChanged(key, value);
    }

    /**
     * @see cn.brainy.gef.status.IStatusMachine#isStatus(int)
     */
    public final boolean isStatus(int key) {
        return (status & key) != 0;
    }

    public int getStatus() {
        return status;
    }

}