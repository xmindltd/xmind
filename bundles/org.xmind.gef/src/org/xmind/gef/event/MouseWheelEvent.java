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
package org.xmind.gef.event;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Event;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class MouseWheelEvent extends MouseEvent {

    public boolean upOrDown;

    public boolean doIt = true;

    public MouseWheelEvent(IPart host, Point location, boolean upOrDown) {
        this(host, location, upOrDown, 0);
    }

    /**
     * @param host
     * @param location
     */
    public MouseWheelEvent(IPart host, Point location, boolean upOrDown,
            int state) {
        super(null, host, true, location, state);
        this.upOrDown = upOrDown;
    }

    public static MouseWheelEvent createEvent(IPart host, Event wheelEvent) {
        return new MouseWheelEvent(host, new Point(wheelEvent.x, wheelEvent.y),
                wheelEvent.count > 0, wheelEvent.stateMask);
    }
}