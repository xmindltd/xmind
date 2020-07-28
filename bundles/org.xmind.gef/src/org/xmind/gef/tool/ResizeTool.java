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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.GEFUtils;

/**
 * @author Frank Shaka
 */
public abstract class ResizeTool extends DraggingTool {

    protected static final Dimension TEMP_SIZE = new Dimension();

    private Rectangle initArea = new Rectangle();

    private Rectangle resultArea = null;

    private int orientation = -1;

    private boolean keepRatio = false;

    private Cursor cursor = null;

    public Rectangle getInitArea() {
        return initArea;
    }

    public void setInitArea(Rectangle rect) {
        this.initArea.setBounds(rect);
        this.resultArea = getInitArea().getCopy();
    }

    public Rectangle getResultArea() {
        return resultArea;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        this.cursor = GEFUtils.getPositionCursor(orientation);
    }

    public boolean isKeepRatio() {
        return keepRatio;
    }

    public void setKeepRatio(boolean keepRatio) {
        this.keepRatio = keepRatio;
    }

    protected void onDragging(Point cursorPosition, MouseDragEvent me) {
        ensureDragPositionVisible(cursorPosition, me);
        if (resultArea == null) {
            resultArea = getInitArea().getCopy();
        }
        updateArea(resultArea, cursorPosition, me);
    }

    protected abstract void updateArea(Rectangle area, Point cursorPosition,
            MouseDragEvent me);

    protected void updateAreaBounds(Rectangle area, Point cursorPosition) {
        Dimension size = TEMP_SIZE;
        if (orientation == PositionConstants.EAST) {
            if (isKeepRatio()) {
                keepRatio(size, cursorPosition.x - initArea.x, -1,
                        initArea.width, initArea.height);
                area.y = initArea.y - (size.height - initArea.height) / 2;
                area.setSize(size);
            } else {
                area.width = constrainWidth(cursorPosition.x - initArea.x);
            }
        } else if (orientation == PositionConstants.WEST) {
            if (isKeepRatio()) {
                keepRatio(size, initArea.right() - cursorPosition.x, -1,
                        initArea.width, initArea.height);
                area.y = initArea.y - (size.height - initArea.height) / 2;
                area.setSize(size);
            } else {
                area.width = constrainWidth(initArea.right() - cursorPosition.x);
            }
            area.x = initArea.right() - area.width;
        } else if (orientation == PositionConstants.SOUTH) {
            if (isKeepRatio()) {
                keepRatio(size, -1, cursorPosition.y - initArea.y,
                        initArea.width, initArea.height);
                area.x = initArea.x - (size.width - initArea.width) / 2;
                area.setSize(size);
            } else {
                area.height = constrainHeight(cursorPosition.y - initArea.y);
            }
        } else if (orientation == PositionConstants.NORTH) {
            if (isKeepRatio()) {
                keepRatio(size, -1, initArea.bottom() - cursorPosition.y,
                        initArea.width, initArea.height);
                area.x = initArea.x - (size.width - initArea.width) / 2;
                area.setSize(size);
            } else {
                area.height = constrainHeight(initArea.bottom()
                        - cursorPosition.y);
            }
            area.y = initArea.bottom() - area.height;
        } else if (orientation == PositionConstants.SOUTH_EAST) {
            if (isKeepRatio()) {
                keepRatio(size, cursorPosition.x - initArea.x, cursorPosition.y
                        - initArea.y, initArea.width, initArea.height);
                area.setSize(size);
            } else {
                area.width = constrainWidth(cursorPosition.x - initArea.x);
                area.height = constrainHeight(cursorPosition.y - initArea.y);
            }
        } else if (orientation == PositionConstants.SOUTH_WEST) {
            if (isKeepRatio()) {
                keepRatio(size, initArea.right() - cursorPosition.x,
                        cursorPosition.y - initArea.y, initArea.width,
                        initArea.height);
                area.setSize(size);
            } else {
                area.width = constrainWidth(initArea.right() - cursorPosition.x);
                area.height = constrainHeight(cursorPosition.y - initArea.y);
            }
            area.x = initArea.right() - area.width;
        } else if (orientation == PositionConstants.NORTH_WEST) {
            if (isKeepRatio()) {
                keepRatio(size, initArea.right() - cursorPosition.x, initArea
                        .bottom()
                        - cursorPosition.y, initArea.width, initArea.height);
                area.setSize(size);
            } else {
                area.width = constrainWidth(initArea.right() - cursorPosition.x);
                area.height = constrainHeight(initArea.bottom()
                        - cursorPosition.y);
            }
            area.x = initArea.right() - area.width;
            area.y = initArea.bottom() - area.height;
        } else if (orientation == PositionConstants.NORTH_EAST) {
            if (isKeepRatio()) {
                keepRatio(size, cursorPosition.x - initArea.x, initArea
                        .bottom()
                        - cursorPosition.y, initArea.width, initArea.height);
                area.setSize(size);
            } else {
                area.width = constrainWidth(cursorPosition.x - initArea.x);
                area.height = constrainHeight(initArea.bottom()
                        - cursorPosition.y);
            }
            area.y = initArea.bottom() - area.height;
        }
    }

    protected void keepRatio(Dimension result, int w, int h, int initW,
            int initH) {
        result.width = w;
        result.height = h;
    }

    protected int constrainWidth(int w) {
        return w;
    }

    protected int constrainHeight(int h) {
        return h;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return cursor;
    }

}