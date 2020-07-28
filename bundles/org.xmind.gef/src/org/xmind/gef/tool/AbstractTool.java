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

import static org.xmind.gef.GEF.ST_ACTIVE;
import static org.xmind.gef.GEF.ST_ALT_PRESSED;
import static org.xmind.gef.GEF.ST_CONTROL_PRESSED;
import static org.xmind.gef.GEF.ST_SHIFT_PRESSED;

import java.util.List;

import org.eclipse.swt.SWT;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.event.DragDropEvent;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.event.MouseWheelEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.status.IStatusMachine;
import org.xmind.gef.status.StatusMachine2;

/**
 * @author Brian Sun
 */
public abstract class AbstractTool implements ITool, IDragDropHandler {

//    private String type;

    private EditDomain domain = null;

    private IStatusMachine statusMachine = null;

    private String contextId = null;

//    private IViewer targetViewer = null;

//    private List<Request> requestQueue = new ArrayList<Request>(5);
//
//    private boolean handlingQueue = false;

//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getType() {
//        return type;
//    }

    /**
     * @return the contextId
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * @param contextId
     *            the contextId to set
     */
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public EditDomain getDomain() {
        return domain;
    }

    public void setDomain(EditDomain domain) {
        this.domain = domain;
    }

    public IViewer getTargetViewer() {
//        if (targetViewer == null) {
//            targetViewer = getDomain().getViewer();
//        }
        return getDomain().getTargetViewer();
    }

    public void setTargetViewer(IViewer viewer) {
        getDomain().setTargetViewer(viewer);
    }

    /**
     * <b>IMPORTANT: </b> this method is not intended to be subclassed and
     * should only be called by EditDomain. Clients may extend the protected
     * method {@link #onActivated(ITool)} instead.
     * 
     * @see org.xmind.gef.tool.ITool#activate(ITool)
     */
    public final void activate(ITool prevTool) {
        if (getStatus().isStatus(ST_ACTIVE))
            return;

        getStatus().setStatus(ST_ACTIVE, true);
        onActivated(prevTool);
    }

    /**
     * <b>IMPORTANT: </b> this method is not intended to be subclassed and
     * should only be called by EditDomain. Clients may extend the protected
     * method {@link #onDeactivated(ITool)} instead.
     * 
     * @see org.xmind.gef.tool.ITool#deactivate(ITool)
     */
    public final void deactivate(ITool nextTool) {
        if (!getStatus().isStatus(ST_ACTIVE))
            return;

        getStatus().setStatus(ST_ACTIVE, false);
        onDeactivated(nextTool);
    }

    /**
     * This method is called after the activation of this tool and may by
     * extended clients.
     * 
     * @param prevTool
     */
    protected void onActivated(ITool prevTool) {
    }

    /**
     * This method is called after the deactivation of this tool and may by
     * extended by clients.
     * 
     * @param nextTool
     */
    protected void onDeactivated(ITool nextTool) {
        if (nextTool != null) {
            copyStatus(nextTool);
        }
    }

    /**
     * @param next
     */
    protected ITool copyStatus(ITool next) {
        if (next instanceof AbstractTool) {
            IStatusMachine nextStatus = ((AbstractTool) next).getStatus();
            IStatusMachine status = getStatus();
            copyStatus(ST_CONTROL_PRESSED, status, nextStatus);
            copyStatus(ST_SHIFT_PRESSED, status, nextStatus);
            copyStatus(ST_ALT_PRESSED, status, nextStatus);
            ((AbstractTool) next).setTargetViewer(getTargetViewer());
        }
        return next;
    }

    /**
     * @return the statusMachine
     */
    public IStatusMachine getStatus() {
        if (statusMachine == null) {
            statusMachine = new StatusMachine2();
        }
        return statusMachine;
    }

    /**
     * @param key
     * @param fromStatus
     * @param toStatus
     */
    private void copyStatus(int key, IStatusMachine fromStatus,
            IStatusMachine toStatus) {
        toStatus.setStatus(key, fromStatus.isStatus(key));
    }

    public ITool changeActiveTool(String toolType) {
        domain.setActiveTool(toolType);
        return domain.getActiveTool();
    }

    protected ITool getTool(String toolType) {
        return domain.getTool(toolType);
    }

    /**
     * @see org.xmind.gef.tool.ITool#focusGained()
     */
    public void focusGained(IViewer viewer) {
        setTargetViewer(viewer);
        handleFocusGained();
    }

    protected boolean handleFocusGained() {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#focusLost()
     */
    public void focusLost(IViewer viewer) {
        setTargetViewer(viewer);
        handleFocusLost();
    }

    protected boolean handleFocusLost() {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#keyDown(org.xmind.gef.event.KeyEvent)
     */
    public void keyDown(KeyEvent ke, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(ke, true);
        handleKeyDown(ke);
    }

    protected boolean handleKeyDown(KeyEvent ke) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#keyUp(org.xmind.gef.event.KeyEvent)
     */
    public void keyUp(KeyEvent ke, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(ke, false);
        handleKeyUp(ke);
    }

    protected boolean handleKeyUp(KeyEvent ke) {
        return false;
    }

    protected void captureModifier(KeyEvent ke, boolean pressed) {
        if ((ke.keyCode & SWT.MOD1) != 0)
            getStatus().setStatus(ST_CONTROL_PRESSED, pressed);
        if ((ke.keyCode & SWT.MOD2) != 0)
            getStatus().setStatus(ST_SHIFT_PRESSED, pressed);
        if ((ke.keyCode & SWT.MOD3) != 0)
            getStatus().setStatus(ST_ALT_PRESSED, pressed);
    }

    protected void captureModifier(MouseEvent me) {
        getStatus().setStatus(ST_CONTROL_PRESSED, me.isState(SWT.MOD1));
        getStatus().setStatus(ST_SHIFT_PRESSED, me.isState(SWT.MOD2));
        getStatus().setStatus(ST_ALT_PRESSED, me.isState(SWT.MOD3));
    }

    /**
     * @see org.xmind.gef.tool.ITool#keyTraversed(org.xmind.gef.event.KeyEvent)
     */
    public void keyTraversed(KeyEvent ke, IViewer viewer) {
        setTargetViewer(viewer);
        handleKeyTraversed(ke);
    }

    protected boolean handleKeyTraversed(KeyEvent ke) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseDoubleClick(org.xmind.gef.event.MouseEvent)
     */
    public void mouseDoubleClick(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseDoubleClick(me);
    }

    protected boolean handleMouseDoubleClick(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseDown(org.xmind.gef.event.MouseEvent)
     */
    public void mouseDown(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseDown(me);
    }

    protected boolean handleMouseDown(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseLongPressed(org.xmind.gef.event.MouseEvent)
     */
    public void mouseLongPressed(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleLongPressed(me);
    }

    protected boolean handleLongPressed(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseDrag(org.xmind.gef.event.MouseEvent)
     */
    public void mouseDrag(MouseDragEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseDrag(me);
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseHover(org.xmind.gef.event.MouseEvent)
     */
    public void mouseHover(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseHover(me);
    }

    protected boolean handleMouseHover(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseMove(org.xmind.gef.event.MouseEvent)
     */
    public void mouseMove(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseMove(me);
    }

    protected boolean handleMouseMove(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseUp(org.xmind.gef.event.MouseEvent)
     */
    public void mouseUp(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseUp(me);
    }

    protected boolean handleMouseUp(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseEntered(org.xmind.gef.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseEntered(me);
    }

    protected boolean handleMouseEntered(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseExited(org.xmind.gef.event.MouseEvent)
     */
    public void mouseExited(MouseEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleMouseExited(me);
    }

    protected boolean handleMouseExited(MouseEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.ITool#mouseWheelScrolled(org.xmind.gef.event.MouseWheelEvent)
     */
    public void mouseWheelScrolled(MouseWheelEvent me, IViewer viewer) {
        setTargetViewer(viewer);
        captureModifier(me);
        handleWheelScrolled(me);
    }

    protected boolean handleWheelScrolled(MouseWheelEvent me) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#dragEntered(org.xmind.gef.event.DragDropEvent)
     */
    public void dragEntered(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragEntered(de);
    }

    protected boolean handleDragEntered(DragDropEvent de) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#dragExited(org.xmind.gef.event.DragDropEvent)
     */
    public void dragExited(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragExited(de);
    }

    protected boolean handleDragExited(DragDropEvent de) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#dragOver(org.xmind.gef.event.DragDropEvent)
     */
    public void dragOver(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragOver(de);
    }

    protected boolean handleDragOver(DragDropEvent de) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#dragOperationChanged(org.xmind.gef.event.DragDropEvent)
     */
    public void dragOperationChanged(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragOperationChanged(de);
    }

    protected boolean handleDragOperationChanged(DragDropEvent de) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#drop(org.xmind.gef.event.DragDropEvent)
     */
    public void drop(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDrop(de);
    }

    protected boolean handleDrop(DragDropEvent de) {
        return false;
    }

    /**
     * @see org.xmind.gef.tool.IDragDropHandler#dropAccept(org.xmind.gef.event.DragDropEvent)
     */
    public void dropAccept(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDropAccept(de);
    }

    protected boolean handleDropAccept(DragDropEvent de) {
        return false;
    }

    public void dragDismissed(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragDismissed(de);
    }

    protected boolean handleDragDismissed(DragDropEvent de) {
        return false;
    }

    public void dragStarted(DragDropEvent de, IViewer viewer) {
        setTargetViewer(viewer);
        handleDragStarted(de);
    }

    protected boolean handleDragStarted(DragDropEvent de) {
        return false;
    }

    /**
     * @deprecated see {@link ITool#handleRequest(String, IViewer)}
     */
    public final void handleRequest(String requestType, IViewer targetViewer) {
//        setTargetViewer(targetViewer);
//        Request request = new Request(requestType);
//        request.setViewer(targetViewer);
//        handleRequest(request);
        getDomain().handleRequest(requestType, targetViewer);
    }

    /**
     * @see org.xmind.gef.tool.ITool#handleRequest(org.xmind.gef.Request)
     */
    public final void handleRequest(Request request) {
        setTargetViewer(request.getTargetViewer());
        internalHandleRequest(request);
    }

    protected void internalHandleRequest(Request request) {
        if (request.hasTargets()) {
            handleTargetedRequest(request);
        } else {
            handleNonTargetedRequest(request);
        }
    }

//    protected final void handleSingleRequest(Request request) {
//        if (request.getTargetViewer() == null) {
//            request.setViewer(getTargetViewer());
//        }
//        if (request.getTargetDomain() == null) {
//            request.setDomain(getDomain());
//        }
//    }

    protected void handleTargetedRequest(Request request) {
        IPart target = request.getPrimaryTarget();
        String role = getDomain().getPartRoles().getRole(request.getType());
        if (target == null || target instanceof IRootPart) {
            if (role != null) {
                target = findPartByRole(role, target == null ? request
                        .getTargetViewer().getRootPart() : target);
            }
        }
        if (target != null) {
            target.handleRequest(request, role);
        }
    }

    protected void handleNonTargetedRequest(Request request) {
        String type = request.getType();
        String role = getDomain().getPartRoles().getRole(type);
        if (role != null) {
            IPart part = findPartByRole(role, request.getTargetViewer()
                    .getRootPart());
            if (part != null && part.hasRole(role)) {
                part.handleRequest(request, role);
            }
        }
    }

    protected IPart findPartByRole(String role, IPart parent) {
        for (IPart p : parent.getChildren()) {
            if (p.hasRole(role))
                return p;
            IPart child = findPartByRole(role, p);
            if (child != null)
                return child;
        }
        return null;
    }

    protected Request createTargetedRequest(String type, IViewer viewer,
            boolean includeRootPartIfEmpty) {
        return fillTargets(
                new Request(type).setViewer(viewer).setDomain(getDomain()),
                viewer, includeRootPartIfEmpty);
    }

    protected Request fillTargets(Request request, IViewer viewer,
            boolean includeRootPartIfEmpty) {
        List<IPart> parts = getSelectedParts(viewer);
        if (parts.isEmpty()) {
            if (includeRootPartIfEmpty)
                request.setPrimaryTarget(viewer.getRootPart());
        } else {
            request.setTargets(parts);
//            IPart focusedPart = viewer.getFocusedPart();
//            if (parts.contains(focusedPart))
//                request.setPrimaryTarget(focusedPart);
        }
        return request;
    }

    protected List<IPart> getSelectedParts(IViewer viewer) {
        return viewer.getSelectionSupport().getPartSelection();
    }

}