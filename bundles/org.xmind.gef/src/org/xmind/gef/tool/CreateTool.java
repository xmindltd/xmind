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

import static org.xmind.gef.GEF.ST_HIDE_CMENU;
import static org.xmind.gef.GEF.TOOL_DEFAULT;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class CreateTool extends GraphicalTool {

    protected boolean handleMouseDown(MouseEvent me) {
        if (me.leftOrRight) {
            if (canFinishOnLeftMouseDown(me)) {
                finish();
                return true;
            }
        } else {
            if (canCancelOnRightMouseDown(me)) {
                cancel();
                ((AbstractTool) getDomain().getActiveTool()).getStatus()
                        .setStatus(ST_HIDE_CMENU, true);
                return true;
            }
        }
        return super.handleMouseDown(me);
    }

    protected abstract boolean canFinishOnLeftMouseDown(MouseEvent me);

    protected abstract boolean canCancelOnRightMouseDown(MouseEvent me);

    protected boolean handleKeyDown(KeyEvent ke) {
        if (shouldCancel(ke)) {
            cancel();
            return true;
        }
        return super.handleKeyDown(ke);
    }

    protected abstract boolean shouldCancel(KeyEvent ke);

    protected void finish() {
        changeToNextTool();
    }

    protected void cancel() {
        changeToNextTool();
    }

    protected void changeToNextTool() {
        changeActiveTool(getNextTool());
    }

    protected String getNextTool() {
        return TOOL_DEFAULT;
    }

    protected abstract boolean canFinish(String requestType);

    protected void internalHandleRequest(Request request) {
        if (request.getTargetViewer() == null
                || request.getTargetViewer() != getTargetViewer()) {
            super.internalHandleRequest(request);
            return;
        }
        String requestType = request.getType();
        if (GEF.REQ_FINISH.equals(requestType)
                && getStatus().isStatus(GEF.ST_ACTIVE)) {
            if (canFinish(requestType)) {
                finish();
            }
        } else if (GEF.REQ_CANCEL.equals(requestType)
                && getStatus().isStatus(GEF.ST_ACTIVE)) {
            cancel();
        } else if (isViewRequest(requestType)) {
            getDomain().getDefaultTool().handleRequest(request);
        } else {
            if (getStatus().isStatus(GEF.ST_ACTIVE))
                cancel();
            getDomain().handleRequest(request);
        }
    }

    protected boolean isViewRequest(String reqType) {
        return isViewRole(getDomain().getPartRoles().getRole(reqType));
    }

    protected boolean isViewRole(String role) {
        return GEF.ROLE_SELECTABLE.equals(role)
                || GEF.ROLE_SCALABLE.equals(role);
    }

//    protected void internalHandleRequest(Request request) {
//        super.handleSingleRequest(request);
//    }

//    protected void internalHandleRequest(String requestType, IViewer viewer) {
//        super.handleRequest(requestType, viewer);
//    }

    /**
     * @see org.xmind.gef.tool.GraphicalTool#copyStatus(org.xmind.gef.tool.ITool)
     */
    @Override
    protected ITool copyStatus(ITool next) {
        next = super.copyStatus(next);
        if (next instanceof IDraggingTool) {
            ((IDraggingTool) next).setStartingPosition(getCursorPosition());
        }
        if (next instanceof AbstractTool) {
            ((AbstractTool) next).getStatus().setStatus(GEF.ST_NO_DRAGGING,
                    true);
        }
        return next;
    }

}