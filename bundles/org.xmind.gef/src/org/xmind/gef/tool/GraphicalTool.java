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
import org.eclipse.swt.graphics.Cursor;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.event.DragDropEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public abstract class GraphicalTool extends AbstractTool implements
        IGraphicalTool {

    private Point cursorPosition = null;

    /**
     * @return the cursorPos
     */
    public Point getCursorPosition() {
        return cursorPosition;
    }

    /**
     * @param pos
     *            the mousePos to set
     */
    public void setCursorPosition(Point pos) {
        this.cursorPosition = pos;
    }

    public IGraphicalViewer getTargetViewer() {
        return (IGraphicalViewer) super.getTargetViewer();
    }

    /**
     * @see org.xmind.gef.tool.AbstractTool#copyStatus(org.xmind.gef.tool.ITool)
     */
    @Override
    protected ITool copyStatus(ITool next) {
        next = super.copyStatus(next);
        if (next instanceof IGraphicalTool) {
            ((IGraphicalTool) next).setCursorPosition(getCursorPosition());
        }
        return next;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        return null;
    }

    public IFigure getToolTip(IPart source, Point position) {
//        if (source instanceof IGraphicalEditPart
//                && source.getStatus().isActive()) {
//            IGraphicalEditPart gp = (IGraphicalEditPart) source;
//            return gp.findTooltipAt(position);
//        }
        return null;
    }

    protected boolean handleMouseExited(MouseEvent me) {
        IGraphicalViewer viewer = getTargetViewer();
        if (viewer != null) {
            IPart preselectedPart = viewer.getPreselectedPart();
            if (me.target == preselectedPart) {
                viewer.setPreselected(null);
            }
        }
        return super.handleMouseExited(me);
    }

    protected boolean handleDragExited(DragDropEvent de) {
        IGraphicalViewer viewer = getTargetViewer();
        if (viewer != null) {
            IPart preselectedPart = viewer.getPreselectedPart();
            if (de.target == preselectedPart) {
                viewer.setPreselected(null);
            }
        }
        return super.handleDragExited(de);
    }

}