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
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IGraphicalEditPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class MoveTool extends DraggingTool implements ISourceTool {

    private IGraphicalEditPart source = null;

    public IGraphicalEditPart getSource() {
        return source;
    }

    public void setSource(IGraphicalEditPart source) {
        this.source = source;
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        if (source == null && me.source instanceof IGraphicalEditPart) {
            source = (IGraphicalEditPart) me.source;
        }
        return super.handleMouseDrag(me);
    }

    protected void end() {
        source = null;
    }

    /**
     * @see org.xmind.gef.tool.DraggingTool#onDragging(org.eclipse.draw2d.geometry.Point,
     *      MouseDragEvent)
     */
    @Override
    protected void onDragging(Point cursorPosition, MouseDragEvent me) {
        ensureDragPositionVisible(cursorPosition, me);
        onMoving(cursorPosition, me);
    }

    protected abstract void onMoving(Point currentPos, MouseDragEvent me);

}