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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.xmind.gef.GEF;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.event.MouseWheelEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 */
public class SelectTool extends GraphicalTool {

    private static final List<IPart> EMPTY_PARTS = Collections.emptyList();

    private IPart sequenceStart = null;

    private boolean ignoreResetSeqStart = false;

    private IPart toSelectOnMouseUp = null;

    protected void setToSelectOnMouseUp(IPart part) {
        toSelectOnMouseUp = part;
    }

    public void mouseDoubleClick(MouseEvent me, IViewer viewer) {
        setToSelectOnMouseUp(null);
        super.mouseDoubleClick(me, viewer);
    }

    protected boolean handleMouseDoubleClick(MouseEvent me) {
        if (me.leftOrRight) {
            IPart source = (IPart) me.target;
            if (source.hasRole(GEF.ROLE_EDITABLE)) {
                resetSeqSelectStart();
                handleEditRequest(createEditRequestOnDoubleClick(source, me));
                me.consume();
                return true;
            }
        }
        return super.handleMouseDoubleClick(me);
    }

    protected Request createEditRequestOnDoubleClick(IPart source, MouseEvent me) {
        return new Request(GEF.REQ_EDIT).setPrimaryTarget(me.target)
                .setDomain(getDomain()).setViewer(getTargetViewer());
    }

    public void mouseDown(MouseEvent me, IViewer viewer) {
        setToSelectOnMouseUp(null);
        super.mouseDown(me, viewer);
    }

    protected boolean handleMouseDown(MouseEvent me) {
        getStatus().setStatus(GEF.ST_MOUSE_PRESSED, true);
        getStatus().setStatus(GEF.ST_MOUSE_RIGHT, !me.leftOrRight);
        if (isToSelectOnMouseUp(me.target, getTargetViewer())) {
            setToSelectOnMouseUp(me.target);
        } else {
            mouseSelect(me.target);
        }
        return true;
    }

    /**
     * @param target
     */
    protected void mouseSelect(IPart target) {
        if (target.hasRole(GEF.ROLE_SELECTABLE)) {
            if (getStatus().isStatus(GEF.ST_CONTROL_PRESSED)) {
                getTargetViewer().setFocused(target);
            } else if (!target.getStatus().isSelected()) {
                if (getStatus().isStatus(GEF.ST_SHIFT_PRESSED)
                        && getTargetViewer().getFocused() != null) {
                    sequenceSelect(target);
                } else {
                    selectSingle(target);
                }
            }
        } else if (!getStatus().isStatus(GEF.ST_CONTROL_PRESSED)) {
            handleSelectNone(getTargetViewer());
        }
    }

    protected boolean isToSelectOnMouseUp(IPart part, IViewer viewer) {
        return part == viewer.getRootPart();
    }

    public void mouseDrag(MouseDragEvent me, IViewer viewer) {
        setToSelectOnMouseUp(null);
        super.mouseDrag(me, viewer);
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        resetSeqSelectStart();
        if (getStatus().isStatus(GEF.ST_NO_DRAGGING)) {
            me.consume();
            return true;
        }

        IPart dragSource = me.source;
        String toolType = null;
        if (canStartBrowsing(dragSource, me)) {
            toolType = getBrowseToolId();
        } else {
            if (canMove(dragSource, me)) {
                toolType = getMoveTool(dragSource, me);
            } else if (dragSource == getTargetViewer().getRootPart()) {
                toolType = getAreaSelectToolId();
            }
        }
        ITool tool = getTool(toolType);
        if (tool != null && tool != this) {
            changeToMoveTool(toolType, tool, dragSource, me);
            me.consume();
            return true;
        }
        return super.handleMouseDrag(me);
    }

    protected String getBrowseToolId() {
        return GEF.TOOL_BROWSE;
    }

    protected String getAreaSelectToolId() {
        return GEF.TOOL_AREASELECT;
    }

    protected void changeToMoveTool(String moveToolType, ITool moveTool,
            IPart dragSource, MouseDragEvent me) {
        if (moveTool instanceof ISourceTool
                && dragSource instanceof IGraphicalEditPart) {
            ((ISourceTool) moveTool).setSource((IGraphicalEditPart) dragSource);
        }
        if (moveTool instanceof IGraphicalTool) {
            ((IGraphicalTool) moveTool).setCursorPosition(getCursorPosition());
        }
        if (moveTool instanceof IDraggingTool) {
            ((IDraggingTool) moveTool).setStartingPosition(me.startingLocation);
        }
        changeActiveTool(moveToolType);
        if (moveTool == getDomain().getActiveTool()) {
            moveTool.mouseDrag(me, getTargetViewer());
        }
    }

    protected boolean canStartBrowsing(IPart host, MouseDragEvent me) {
        return getStatus().isStatus(GEF.ST_MOUSE_RIGHT)
                || (getStatus().isStatus(GEF.ST_MOUSE_PRESSED) && host
                        .hasRole(GEF.ROLE_MAP_MOVABLE));
    }

    protected boolean canMove(IPart host, MouseDragEvent me) {
        return host.hasRole(GEF.ROLE_MOVABLE);
    }

    protected String getMoveTool(IPart source, MouseDragEvent me) {
        return GEF.TOOL_MOVE;
    }

    protected boolean handleMouseEntered(MouseEvent me) {
        IPart target = me.target;
        if (target != null && target.hasRole(GEF.ROLE_SELECTABLE)) {
            setPreSelected(target);
        } else {
            setPreSelected(null);
        }
        return super.handleMouseEntered(me);
    }

    protected void setPreSelected(IPart target) {
        getTargetViewer().setPreselected(target);
    }

    protected boolean handleMouseHover(MouseEvent me) {
        getStatus().setStatus(GEF.ST_MOUSE_HOVER, true);
        return super.handleMouseHover(me);
    }

    public void mouseLongPressed(MouseEvent me, IViewer viewer) {
        setToSelectOnMouseUp(null);
        super.mouseLongPressed(me, viewer);
    }

    protected boolean handleLongPressed(MouseEvent me) {
        if (me.leftOrRight && !getStatus().isStatus(GEF.ST_SHIFT_PRESSED)
                && !getStatus().isStatus(GEF.ST_CONTROL_PRESSED)
                && !getStatus().isStatus(GEF.ST_ALT_PRESSED)) {
            String browseToolId = getBrowseToolId();
            if (browseToolId != null) {
                ITool tool = getTool(browseToolId);
                if (tool != null) {
                    if (tool instanceof IDraggingTool) {
                        ((IDraggingTool) tool)
                                .setStartingPosition(getCursorPosition());
                    }
                    if (tool instanceof IGraphicalTool) {
                        ((IGraphicalTool) tool)
                                .setCursorPosition(getCursorPosition());
                    }
                    changeActiveTool(browseToolId);
                    if (tool == getDomain().getActiveTool()) {
                        me.consume();
                    }
                    return true;
                }
            }
        }
        return super.handleLongPressed(me);
    }

    public void mouseMove(MouseEvent me, IViewer viewer) {
        setToSelectOnMouseUp(null);
        super.mouseMove(me, viewer);
    }

    protected boolean handleMouseMove(MouseEvent me) {
        getStatus().setStatus(GEF.ST_MOUSE_HOVER, false);
        return super.handleMouseMove(me);
    }

    /**
     * @see org.xmind.gef.tool.AbstractTool#mouseUp(org.xmind.gef.event.MouseEvent)
     */
    @Override
    public boolean handleMouseUp(MouseEvent me) {
        getStatus().setStatus(GEF.ST_NO_DRAGGING, false);
        getStatus().setStatus(GEF.ST_MOUSE_PRESSED, false);
        IPart host = me.target;
        if (getStatus().isStatus(GEF.ST_CONTROL_PRESSED)) {
            if (host.hasRole(GEF.ROLE_SELECTABLE)) {
                multiSelect(host);
            }
        }
        if (toSelectOnMouseUp != null) {
            mouseSelect(toSelectOnMouseUp);
            setToSelectOnMouseUp(null);
        }
        if (!me.leftOrRight) {
            getStatus().setStatus(GEF.ST_MOUSE_RIGHT, false);
            if (!getStatus().isStatus(GEF.ST_HIDE_CMENU)) {
                IGraphicalViewer viewer = getTargetViewer();
                if (viewer instanceof GraphicalViewer) {
                    ((GraphicalViewer) viewer).hideToolTip();
                }
            }
        }
        return true;
    }

    protected boolean handleKeyDown(KeyEvent ke) {
        int state = ke.getState();
        int key = ke.keyCode;
        IGraphicalViewer viewer = getTargetViewer();
        if (viewer != null) {
            if (isNavigationKey(state, key)) {
                return handleNavigationKeyDown(viewer, state, key);
            }
        }
        return super.handleKeyDown(ke);
    }

    protected boolean handleNavigationKeyDown(IGraphicalViewer viewer,
            int state, int key) {
        String type = getNavigationRequestType(key);
        if (type != null) {
            Request request = createTargetedRequest(type, viewer, false);
            request.setPrimaryTarget(viewer.getFocusedPart());
            if (request.hasTargets()) {
                boolean sequential = isSequentialNavigation(state);
                if (sequential) {
                    request.setParameter(GEF.PARAM_NAV_SEQUENTIAL, Boolean.TRUE);
                    if (getSequenceStart() == null) {
                        IPart newStart = request.getPrimaryTarget();
                        setSequenceStart(newStart);
                    }
                    request.setParameter(GEF.PARAM_NAV_SEQUENCE_START,
                            getSequenceStart());
                }
                return handleNavRequest(request, sequential);
            }
        }
        return true;
    }

    protected boolean handleNavRequest(Request request, boolean sequential) {
        getDomain().handleRequest(request);
        Object result = request.getResult(GEF.RESULT_NAVIGATION);
        if (result != null && result instanceof IPart[]) {
            IPart[] toSelect = (IPart[]) result;
            IPart toFocus = (IPart) request.getResult(GEF.RESULT_NEW_FOCUS);
            navigateTo(Arrays.asList(toSelect), toFocus, sequential);
            return true;
        }
        return false;
    }

    protected void navigateTo(List<IPart> toSelect, boolean sequential) {
        navigateTo(toSelect, null, sequential);
    }

    protected void navigateTo(List<IPart> toSelect, IPart toFocus,
            boolean sequential) {
        if (sequential) {
            ignoreResetSeqStart = true;
        }
        select(toSelect,
                toFocus == null || !toSelect.contains(toFocus) ? (toSelect
                        .isEmpty() ? null : toSelect.get(0)) : toFocus);
        if (sequential)
            ignoreResetSeqStart = false;
    }

    protected boolean isNavigationKey(int state, int key) {
        if (state == 0 || state == SWT.MOD2) {
            return key == SWT.ARROW_UP || key == SWT.ARROW_DOWN
                    || key == SWT.ARROW_LEFT || key == SWT.ARROW_RIGHT
                    || key == SWT.HOME || key == SWT.END;
        }
        return false;
    }

    protected boolean isSequentialNavigation(int state) {
        return state == SWT.MOD2;
    }

    protected String getNavigationRequestType(int key) {
        switch (key) {
        case SWT.ARROW_UP:
            return GEF.REQ_NAV_UP;
        case SWT.ARROW_DOWN:
            return GEF.REQ_NAV_DOWN;
        case SWT.ARROW_LEFT:
            return GEF.REQ_NAV_LEFT;
        case SWT.ARROW_RIGHT:
            return GEF.REQ_NAV_RIGHT;
        case SWT.HOME:
            return GEF.REQ_NAV_BEGINNING;
        case SWT.END:
            return GEF.REQ_NAV_END;
        }
        return null;
    }

    protected void handleNonTargetedRequest(Request request) {
        String requestType = request.getType();
        if (GEF.REQ_UNDO.equals(requestType)) {
            ICommandStack cs = getDomain().getCommandStack();
            if (cs.canUndo())
                cs.undo();
            return;
        } else if (GEF.REQ_REDO.equals(requestType)) {
            ICommandStack cs = getDomain().getCommandStack();
            if (cs.canRedo())
                cs.redo();
            return;
        } else if (request.getTargetViewer() != null) {
            if (GEF.REQ_SELECT_NONE.equals(requestType)) {
                handleSelectNone(request.getTargetViewer());
                return;
            } else if (GEF.REQ_SELECT_ALL.equals(requestType)) {
                handleSelectAll(request.getTargetViewer());
                return;
            } else if (allowsFillTargets(request)) {
                fillTargets(request, request.getTargetViewer(), false);
                if (request.hasTargets()) {
                    handleTargetedRequest(request);
                    return;
                }
            }
        }
        super.handleNonTargetedRequest(request);
    }

    protected boolean allowsFillTargets(Request request) {
        return true;
    }

    protected void handleTargetedRequest(Request request) {
        String requestType = request.getType();
        if (GEF.REQ_SELECT.equals(requestType)) {
            select(request.getTargets(), request.getPrimaryTarget());
        } else if (GEF.REQ_SELECT_SINGLE.equals(requestType)) {
            selectSingle(request.getPrimaryTarget());
        } else if (GEF.REQ_EDIT.equals(requestType)) {
            handleEditRequest(request);
        } else {
            super.handleTargetedRequest(request);
        }
    }

    protected void handleSelectAll(IViewer viewer) {
        List<IPart> allSelectable = collectAllSelectable(viewer.getRootPart(),
                new ArrayList<IPart>());
        IPart toFocus = getTargetViewer().getFocusedPart();
        select(allSelectable, toFocus);
    }

    protected List<IPart> collectAllSelectable(IPart parent, List<IPart> results) {
        for (IPart child : parent.getChildren()) {
            if (isSelectableOnSelectAll(child)) {
                results.add(child);
            }
            collectAllSelectable(child, results);
        }
        return results;
    }

    protected boolean isSelectableOnSelectAll(IPart child) {
        return getTargetViewer().getSelectionSupport().isSelectable(child);
    }

    /**
     * @return the lastSeqStart
     */
    public IPart getSequenceStart() {
        return sequenceStart;
    }

    protected List<IPart> getSequenceParts(IPart start, IPart end) {
        return Arrays.asList(start, end);
    }

    protected void select(List<? extends IPart> toSelect, IPart toFocus) {
        IGraphicalViewer viewer = getTargetViewer();
        if (viewer != null) {
            IPart lastFocused = viewer.getFocusedPart();
            viewer.setSelection(new StructuredSelection(toSelect), true);
            resetSeqSelectStart();
            viewer.setFocused(getToFocus(toFocus, lastFocused));
        }
    }

    protected IPart getToFocus(IPart toFocus, IPart lastFocused) {
        if (isFocusable(toFocus, lastFocused)) {
            return toFocus;
        }
        return null;
    }

    protected boolean isFocusable(IPart toFocus, IPart lastFocused) {
        if (lastFocused != null && lastFocused.getStatus().isSelected())
            return true;
        return toFocus != null && toFocus.getStatus().isSelected();
    }

    protected void multiSelect(IPart host) {
        if (!host.getStatus().isSelected()) {
            getTargetViewer().getSelectionSupport().appendSelection(host);
        } else {
            getTargetViewer().getSelectionSupport().deselect(host);
        }
    }

    protected void handleSelectNone(IViewer viewer) {
        select(EMPTY_PARTS, null);
    }

    protected void selectSingle(IPart target) {
        select(Collections.singletonList(target), target);
    }

    protected void sequenceSelect(IPart target) {
        if (getSequenceStart() == null) {
            setSequenceStart(getTargetViewer().getFocusedPart());
        }
        List<IPart> seqParts = getSequenceParts(getSequenceStart(), target);
        if (seqParts != null && !seqParts.isEmpty()) {
            ignoreResetSeqStart = true;
            select(seqParts, target);
            ignoreResetSeqStart = false;
        }
    }

    protected void setSequenceStart(IPart start) {
        this.sequenceStart = start;
    }

    public void resetSeqSelectStart() {
        if (ignoreResetSeqStart)
            return;
        setSequenceStart(null);
    }

    protected void handleEditRequest(Request request) {
        startEditing(request.getPrimaryTarget(), request);
    }

    protected void startEditing(IPart source, Request request) {
        //selectSingle(source);
        String editToolType = getEditTool(source, request);
        ITool et = getTool(editToolType);
        if (et != null && et != this) {
            changeActiveTool(editToolType);
            if (et == getDomain().getActiveTool()) {
                et.handleRequest(request);
            }
        }
    }

    protected String getEditTool(IPart source, Request request) {
        return GEF.TOOL_EDIT;
    }

    @Override
    protected boolean handleWheelScrolled(MouseWheelEvent me) {
        if (getStatus().isStatus(GEF.ST_CONTROL_PRESSED)) {
            if (handleZoomByScroll(me))
                return true;
        }
        return super.handleWheelScrolled(me);
    }

    protected boolean handleZoomByScroll(MouseWheelEvent me) {
        if (me.upOrDown) {
            getDomain().handleRequest(GEF.REQ_ZOOMIN, getTargetViewer());
        } else {
            getDomain().handleRequest(GEF.REQ_ZOOMOUT, getTargetViewer());
        }
        me.doIt = false;
        return true;
    }

}