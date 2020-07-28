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

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.xmind.gef.IViewer;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public class BrowsingTool extends DraggingTool {

    private static final Point SCROLL_POSITION = new Point();

    private Point startScrollPosition = null;

    private boolean hidePopupMenu = false;

//    private Menu preservedMenu = null;
//
//    private Control menuOwner = null;

    protected void start() {
        startScrollPosition = new Point(getTargetViewer().getScrollPosition());
    }

    protected void end() {
    }

    /**
     * @see org.xmind.gef.tool.GraphicalTool#getCurrentCursor(org.eclipse.draw2d.geometry.Point,
     *      IPart)
     */
    @Override
    public Cursor getCurrentCursor(Point pos, IPart host) {
        return Cursors.HAND;
    }

    public void mouseDrag(MouseDragEvent me, IViewer viewer) {
        super.mouseDrag(me, viewer);
        if (!me.leftOrRight) {
            if (!hidePopupMenu)
                hidePopupMenu = true;
        }
    }

    public void mouseUp(MouseEvent me, IViewer viewer) {
        super.mouseUp(me, viewer);
        if (hidePopupMenu) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                Menu menu = control.getMenu();
                if (menu != null && !menu.isDisposed()) {
                    menu.setVisible(false);
                }
            }
        }
        hidePopupMenu = false;
//        if (preservedMenu != null && !preservedMenu.isDisposed()
//                && menuOwner != null && !menuOwner.isDisposed()) {
//            menuOwner.getMenu().setVisible(false);
//            menuOwner.setMenu(preservedMenu);
//        }
//        menuOwner = null;
//        preservedMenu = null;
//        if (!me.leftOrRight && preservedMenu != null && menuOwner != null) {
//            Display.getCurrent().asyncExec(new Runnable() {
//                public void run() {
//                    if (preservedMenu != null && !preservedMenu.isDisposed()
//                            && menuOwner != null && !menuOwner.isDisposed()) {
//                        menuOwner.setMenu(preservedMenu);
//                    }
//                    menuOwner = null;
//                    preservedMenu = null;
//                }
//            });
//        } else {
//            preservedMenu = null;
//            menuOwner = null;
//        }
    }

//    protected boolean handleMouseUp(MouseEvent me) {
//        boolean ret = super.handleMouseUp(me);
////        ITool currentTool = getDomain().getActiveTool();
////        if (!me.leftOrRight && currentTool instanceof AbstractTool) {
////            ((AbstractTool) currentTool).getStatus().setStatus(
////                    GEF.ST_HIDE_CMENU, true);
////        }
//        return ret;
//    }

    protected void onDragging(Point cursorPosition, MouseDragEvent me) {
        if (startScrollPosition != null) {
            SCROLL_POSITION.setLocation(startScrollPosition);
            SCROLL_POSITION.translate(me.getSWTDisplacement().negate());
            getTargetViewer().scrollTo(SCROLL_POSITION);
        }
    }

}