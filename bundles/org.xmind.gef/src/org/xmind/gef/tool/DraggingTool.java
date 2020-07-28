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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public abstract class DraggingTool extends GraphicalTool implements
        IDraggingTool {

    private Point startPos = null;

    private boolean scrolling = false;

    private boolean ended = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.tool.IDraggingTool#getStartingPosition()
     */
    public Point getStartingPosition() {
        return startPos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.IDraggingTool#setStartingPosition(org.eclipse.draw2d
     * .geometry.Point)
     */
    public void setStartingPosition(Point pos) {
        this.startPos = pos;
    }

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        if (startPos != null) {
            ended = false;
            start();
        }
    }

    protected void onDeactivated(ITool nextTool) {
        if (!ended) {
            ended = true;
            end();
            startPos = null;
        }
        super.onDeactivated(nextTool);
    }

    protected abstract void start();

    protected abstract void end();

    protected abstract void onDragging(Point cursorPosition, MouseDragEvent me);

    protected boolean handleMouseDown(MouseEvent me) {
        if (startPos == null) {
            startPos = me.cursorLocation;
            ended = false;
            start();
            return true;
        }
        return super.handleMouseDown(me);
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        onDragging(getCursorPosition(), me);
        return true;
    }

    public IFigure getToolTip(IPart source, Point position) {
        return null;
    }

    protected IFigure superFindToolTip(IPart source, Point position) {
        return super.getToolTip(source, position);
    }

    protected void ensureDragPositionVisible(Point currentPos,
            final MouseDragEvent me) {
        Rectangle r = getTargetViewer().getZoomManager().getAntiScaled(
                getTargetViewer().getClientArea());
        int deltaX = 0;
        int deltaY = 0;
        if (currentPos.x < r.x) {
            deltaX = currentPos.x - r.x;
        } else if (currentPos.x > r.right()) {
            deltaX = currentPos.x - r.right();
        }
        if (currentPos.y < r.y) {
            deltaY = currentPos.y - r.y;
        } else if (currentPos.y > r.bottom()) {
            deltaY = currentPos.y - r.bottom();
        }
        if (Math.abs(deltaX) < getScrollingDetection()
                && Math.abs(deltaY) < getScrollingDetection()) {
            if (deltaX != 0 || deltaY != 0) {
                getTargetViewer().scrollDelta(deltaX, deltaY);
                setCursorPosition(currentPos.getTranslated(deltaX, deltaY));
                scrolling = true;
                getTargetViewer().getControl().getDisplay()
                        .timerExec(200, new Runnable() {
                            public void run() {
                                if (scrolling)
                                    onDragging(getCursorPosition(), me);
                            }
                        });
            } else {
                scrolling = false;
            }
        } else {
            scrolling = false;
        }
    }

    protected int getScrollingDetection() {
        return SCROLLING_DETECTION;
    }

    protected boolean handleMouseUp(MouseEvent me) {
        scrolling = false;
        finish();
        return true;
    }

    protected boolean handleKeyDown(KeyEvent ke) {
        if (ke.keyCode == SWT.ESC) {
            cancel();
            return true;
        }
        return super.handleKeyDown(ke);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.tool.IDraggingTool#finish()
     */
    public void finish() {
        if (!ended) {
            ended = true;
            end();
            startPos = null;
        }
        if (getDomain().getActiveTool() == this) {
            changeToNextTool();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.tool.IDraggingTool#cancel()
     */
    public void cancel() {
        if (!ended) {
            ended = true;
            end();
            startPos = null;
        }
        if (getDomain().getActiveTool() == this) {
            changeToNextTool();
        }
    }

    protected void changeToNextTool() {
        changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected void internalHandleRequest(Request request) {
        if (request.getTargetViewer() == null
                || request.getTargetViewer() != getTargetViewer()) {
            super.internalHandleRequest(request);
            return;
        }
        String requestType = request.getType();
        if (GEF.REQ_FINISH.equals(requestType)) {
            finish();
        } else if (GEF.REQ_CANCEL.equals(requestType)) {
            cancel();
        } else {
            cancel();
            if (!getStatus().isStatus(GEF.ST_ACTIVE)) {
                getDomain().handleRequest(request);
            } else {
                getDomain().getDefaultTool().handleRequest(request);
            }
        }
    }

//    protected void internalHandleRequest(Request request) {
//        super.handleSingleRequest(request);
//    }

}