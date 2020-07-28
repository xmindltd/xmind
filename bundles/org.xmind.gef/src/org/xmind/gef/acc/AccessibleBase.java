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
package org.xmind.gef.acc;

public abstract class AccessibleBase implements IAccessible {

    private int id = -1;

    public int getAccessibleId() {
        if (id < 0)
            return internalGetId();
        return id;
    }

    private int internalGetId() {
        int code = super.hashCode();
        if (code < 0) {
            code -= 4;
        }
        return code;
    }

    public void setAccessibleId(int id) {
        if (id < 0 || this.id >= 0)
            return;
        this.id = id;
    }

}