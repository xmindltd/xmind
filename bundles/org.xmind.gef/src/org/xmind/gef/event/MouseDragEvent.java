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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class MouseDragEvent extends MouseEvent {

    /**
     * The SWT mouse down event that initially triggered this drag event.
     */
    protected org.eclipse.swt.events.MouseEvent startingSWTEvent;

    /**
     * The location of the mouse cursor where this drag event got initiated by a
     * mouse down event.
     */
    public Point startingLocation;

    /**
     * The part which was under the mouse cursor when this drag event got
     * initiated.
     */
    public IPart source;

    public MouseDragEvent(org.eclipse.swt.events.MouseEvent startEvent,
            org.eclipse.swt.events.MouseEvent currentEvent, IPart target,
            IPart source, Point startLoc, Point currentLoc, boolean leftOrRight) {
        this(startEvent, currentEvent, target, source, startLoc, currentLoc,
                leftOrRight, 0);
    }

    public MouseDragEvent(org.eclipse.swt.events.MouseEvent startEvent,
            org.eclipse.swt.events.MouseEvent currentEvent, IPart target,
            IPart source, Point startLoc, Point currentLoc,
            boolean leftOrRight, int state) {
        super(currentEvent, target, leftOrRight, currentLoc, state);
        this.startingSWTEvent = startEvent;
        this.startingLocation = startLoc;
        this.source = source;
    }

    public static MouseDragEvent createEvent(
            org.eclipse.swt.events.MouseEvent current, MouseDragEvent prev,
            Point currentLoc, IPart newTarget) {
        return new MouseDragEvent(prev.startingSWTEvent, current, newTarget,
                prev.source, prev.startingLocation, currentLoc,
                prev.leftOrRight, current.stateMask);
    }

//    public MouseDragEvent( MouseDragEvent prev, Point current, IPart hover ) {
//        this(prev.host, prev.leftOrRight, prev.startPoint, current, hover, current.getDifference(prev.location) );
//    }

//    public static MouseDragEvent createEvent( IPart host, boolean leftOrRight, int dx, int dy, Point startPoint, Point location ) {
//        return new MouseDragEvent( host, leftOrRight, new Dimension(dx, dy), startPoint, location );
//    }

    public static MouseDragEvent createEvent(
            org.eclipse.swt.events.MouseEvent start, IPart source,
            Point startLoc) {
        return new MouseDragEvent(start, start, source, source, startLoc,
                startLoc, getButtonState(start.button), start.stateMask);
    }

    public Dimension getDisplacement() {
        return cursorLocation.getDifference(startingLocation);
    }

    public Dimension getSWTDisplacement() {
        return new Dimension(currentSWTEvent.x - startingSWTEvent.x,
                currentSWTEvent.y - startingSWTEvent.y);
    }

    public org.eclipse.swt.events.MouseEvent getStartingSWTEvent() {
        return startingSWTEvent;
    }

//    public static MouseDragEvent createEvent( MouseDragEvent event, Point location, IPart hover ) {
//        return new MouseDragEvent( event, location, hover );
//    }
}