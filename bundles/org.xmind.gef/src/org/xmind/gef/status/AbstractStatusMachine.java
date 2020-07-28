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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class AbstractStatusMachine implements IStatusMachine {

    private Object source = null;
    private List<IStatusListener> listeners = null;

    public AbstractStatusMachine() {
    }

    public AbstractStatusMachine(Object source) {
        this.source = source;
    }

    protected void fireStatusChanged(int key, boolean newValue) {
        if (listeners != null) {
            StatusEvent event = new StatusEvent(source == null ? this : source,
                    key, newValue);
            for (Object l : listeners.toArray()) {
                ((IStatusListener) l).statusChanged(event);
            }
        }
    }

    /**
     * @see org.xmind.gef.status.IStatusMachine#addStatusListener(org.xmind.gef.status.IStatusListener)
     */
    public void addStatusListener(IStatusListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<IStatusListener>();
        }
        listeners.add(listener);
    }

    /**
     * @see org.xmind.gef.status.IStatusMachine#removeStatusListener(org.xmind.gef.status.IStatusListener)
     */
    public void removeStatusListener(IStatusListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

//    /**
//     * @see org.xmind.framework.Disposable#dispose()
//     */
//    @Override
//    public void dispose() {
//        if (listeners != null) {
//            listeners.clear();
//            listeners = null;
//        }
//        super.dispose();
//    }
}