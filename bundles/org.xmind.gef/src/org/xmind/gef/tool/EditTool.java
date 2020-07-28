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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class EditTool extends GraphicalTool implements ISourceTool {

    private IGraphicalEditPart source;

    private boolean remainActive = false;

    private Set<String> editRequestTypes = new HashSet<String>();

    private Request lastForwardedRequest = null;

    public IGraphicalEditPart getSource() {
        return source;
    }

    public void setSource(IGraphicalEditPart source) {
        this.source = source;
    }

    /**
     * @see org.xmind.gef.tool.AbstractTool#onActivated(ITool)
     */
    @Override
    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        refreshStatus();
    }

    @Override
    protected void onDeactivated(ITool nextTool) {
        finishEditing();
        super.onDeactivated(nextTool);
    }

    /**
     *
     */
    protected void refreshStatus() {
        getStatus().setStatus(GEF.ST_CONTROL_PRESSED, false);
        getStatus().setStatus(GEF.ST_SHIFT_PRESSED, false);
        getStatus().setStatus(GEF.ST_ALT_PRESSED, false);
    }

    protected void handleEditRequest(Request request) {
        IPart target = request.getPrimaryTarget();
        if (target instanceof IGraphicalEditPart) {
            IGraphicalEditPart newSource = (IGraphicalEditPart) target;
            if (canEdit(newSource)) {
                if (newSource != getSource()) {
                    remainActive = true;
                    finishEditing();
                    remainActive = false;
                }
                setSource(newSource);
                if (acceptEditRequest(request)) {
                    if (shouldUpdateSelectionOnEdit(newSource, request)) {
                        getTargetViewer().setSelection(
                                new StructuredSelection(newSource), true);
                    }
                    if (!startEditing(getSource())) {
                        changeActiveTool(GEF.TOOL_DEFAULT);
                    }
                } else {
                    finishEditing();
                }
            } else {
                finishEditing();
                if (request != lastForwardedRequest) {
                    lastForwardedRequest = request;
                    getDomain().handleRequest(request);
                }
            }
        }
    }

    /**
     * @param newSource
     * @param request
     * @return
     */
    protected boolean shouldUpdateSelectionOnEdit(IGraphicalEditPart newSource,
            Request request) {
        return true;
    }

    protected boolean canEdit(IGraphicalEditPart target) {
        return true;
    }

    protected boolean acceptEditRequest(Request request) {
        return true;
    }

    /**
     * @param source
     * @return
     */
    protected boolean startEditing(IGraphicalEditPart source) {
        return false;
    }

    protected void cancelEditing() {
        if (!remainActive)
            changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected void finishEditing() {
        if (!remainActive)
            changeActiveTool(GEF.TOOL_DEFAULT);
    }

    protected boolean isViewRequest(String reqType) {
        String role = getDomain().getPartRoles().getRole(reqType);
        return isViewRole(role);
    }

    protected boolean isViewRole(String role) {
        return GEF.ROLE_SELECTABLE.equals(role)
                || GEF.ROLE_SCALABLE.equals(role)
                || GEF.ROLE_MODIFIABLE.equals(role);
    }

    protected Collection<String> getEditRequestTypes() {
        return editRequestTypes;
    }

    protected void addEditRequestType(String reqType) {
        editRequestTypes.add(reqType);
    }

    protected void removeEditRequestType(String reqType) {
        editRequestTypes.remove(reqType);
    }

    protected void internalHandleRequest(Request request) {
        if (request.getTargetViewer() == null
                || request.getTargetViewer() != getTargetViewer()) {
            super.internalHandleRequest(request);
            return;
        }
        String requestType = request.getType();
        if (GEF.REQ_EDIT.equals(requestType)
                || getEditRequestTypes().contains(requestType)) {
            handleEditRequest(request);
        } else if (GEF.REQ_SELECT_ALL.equals(requestType)) {
            selectAll();
        } else if (GEF.REQ_COPY.equals(requestType)) {
            copy();
        } else if (GEF.REQ_CUT.equals(requestType)) {
            cut();
        } else if (GEF.REQ_PASTE.equals(requestType)) {
            paste();
        } else if (GEF.REQ_DELETE.equals(requestType)) {
            delete();
        } else if (GEF.REQ_CANCEL.equals(requestType)) {
            cancelEditing();
        } else if (GEF.REQ_FINISH.equals(requestType)) {
            finishEditing();
        } else if (GEF.REQ_REDO.equals(requestType)) {
            if (canRedo()) {
                redo();
            }
        } else if (GEF.REQ_UNDO.equals(requestType)) {
            if (canUndo()) {
                undo();
            }
        } else if (isViewRequest(requestType)) {
            getDomain().getDefaultTool().handleRequest(request);
        } else {
            finishEditing();
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

    protected void selectAll() {
    }

    protected void copy() {
    }

    protected void cut() {
    }

    protected void paste() {
    }

    protected void delete() {
    }

    protected void undo() {
    }

    protected void redo() {
    }

    public boolean canUndo() {
        return false;
    }

    public boolean canRedo() {
        return false;
    }

    protected boolean handleMouseDown(MouseEvent me) {
        if (shouldFinishOnMouseDown(me)) {
            finishEditing();
            ITool activeTool = getDomain().getActiveTool();
            if (activeTool != this)
                activeTool.mouseDown(me, getTargetViewer());
            return true;
        } else {
            return super.handleMouseDown(me);
        }
    }

    protected boolean shouldFinishOnMouseDown(MouseEvent me) {
        return me.target != getSource();
    }

    protected boolean handleKeyUp(KeyEvent ke) {
        if (shouldFinish(ke)) {
            finishEditing();
            return true;
        } else if (shouldCancel(ke)) {
            cancelEditing();
            return true;
        }
        return super.handleKeyUp(ke);
    }

    protected abstract boolean shouldCancel(KeyEvent ke);

    protected abstract boolean shouldFinish(KeyEvent ke);

    protected boolean handleMouseEntered(MouseEvent me) {
        if (me.target.hasRole(GEF.ROLE_SELECTABLE)) {
            getTargetViewer().setPreselected(me.target);
        }
        return super.handleMouseEntered(me);
    }

}
