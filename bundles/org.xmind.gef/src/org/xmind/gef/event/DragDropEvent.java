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
package org.xmind.gef.event;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.part.IPart;

/**
 * @author Frank Shaka
 */
public class DragDropEvent {

    /**
     * Part under cursor
     */
    public IPart target;

    /**
     * Location of cursor relative to current viewer's content layer
     */
    public Point location;

    /**
     * A list of acceptable drop data, <code>null</code> meaning that there's no
     * external drag source.
     */
    public DndData dndData;

    /**
     * The drag-drop operation being performed while -1 means that there's no
     * external drag source. Tool implementations may modify this field to the
     * one they accept.
     * 
     * @see DND#DROP_NONE
     * @see DND#DROP_MOVE
     * @see DND#DROP_COPY
     * @see DND#DROP_LINK
     */
    public int detail;

    /**
     * A bitwise OR'ing of the operations that the DragSource can support (e.g.
     * DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK). The detail value must be
     * a member of this list or DND.DROP_NONE.
     * 
     * @see DND#DROP_NONE
     * @see DND#DROP_MOVE
     * @see DND#DROP_COPY
     * @see DND#DROP_LINK
     */
    public int operations;

    protected DropTargetEvent sourceSWTEvent;

    private boolean consumed = false;

    public DragDropEvent(DropTargetEvent swtEvent, IPart host, Point location,
            int detail, int operations) {
        this.sourceSWTEvent = swtEvent;
        this.target = host;
        this.location = location;
//        this.detail = Util.isMac() ? DND.DROP_COPY : detail;
        this.detail = detail;
        this.operations = operations;
    }

    public static DragDropEvent createFrom(DropTargetEvent swtEvent,
            IPart host, Point location) {
        return new DragDropEvent(swtEvent, host, location, swtEvent.detail,
                swtEvent.operations);
    }

    public DropTargetEvent getSourceSWTEvent() {
        return sourceSWTEvent;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    @Override
    public String toString() {
        return "{host=" + target //$NON-NLS-1$
                + ",location=" + location //$NON-NLS-1$
                + ",data=" + dndData //$NON-NLS-1$
                + ",detail=" + detail //$NON-NLS-1$
                + ",operations=" + operations //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
    }

}