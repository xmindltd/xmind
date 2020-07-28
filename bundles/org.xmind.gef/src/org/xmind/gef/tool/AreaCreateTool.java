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
package org.xmind.gef.tool;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.event.MouseDragEvent;

/**
 * @author Frank Shaka
 */
public abstract class AreaCreateTool extends DraggingTool {

    private Rectangle area = null;

    public void setStartingPosition(Point pos) {
        super.setStartingPosition(pos);
        area = null;
    }

    public Rectangle getResult() {
        return area;
    }

    protected void start() {
        if (getStartingPosition() != null) {
            if (getCursorPosition() != null) {
                area = new Rectangle(getStartingPosition(), getCursorPosition());
            } else {
                area = new Rectangle(getStartingPosition(),
                        getStartingPosition());
            }
        } else {
            area = new Rectangle();
        }
    }

    protected void onDragging(Point cursorPosition, MouseDragEvent me) {
        ensureDragPositionVisible(cursorPosition, me);
        if (area != null) {
            updateArea(area, cursorPosition);
        }
    }

    protected void updateArea(Rectangle area, Point currentPos) {
        setBounds(area, getStartingPosition(), currentPos);
    }

    protected static void setBounds(Rectangle r, Point p1, Point p2) {
        r.x = Math.min(p1.x, p2.x);
        r.y = Math.min(p1.y, p2.y);
        r.width = Math.abs(p2.x - p1.x) + 1;
        r.height = Math.abs(p2.y - p1.y) + 1;
    }

}