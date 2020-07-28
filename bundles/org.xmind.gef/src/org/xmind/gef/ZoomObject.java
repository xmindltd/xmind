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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;

/**
 * @author Brian Sun
 */
public class ZoomObject {

    private List<IZoomListener> listeners = null;

    public ZoomObject() {
        super();
    }

    public void addZoomListener(IZoomListener l) {
        if (listeners == null)
            listeners = new ArrayList<IZoomListener>();
        listeners.add(l);
    }

    public void removeZoomListener(IZoomListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    public void fireScaleChanged(final double oldValue, final double newValue) {
        if (listeners == null || oldValue == newValue)
            return;
        for (final Object l : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IZoomListener) l).scaleChanged(ZoomObject.this, oldValue,
                            newValue);
                }
            });
        }
    }

}