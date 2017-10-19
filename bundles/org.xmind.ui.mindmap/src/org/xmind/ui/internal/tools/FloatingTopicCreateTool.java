/**
 * 
 */
package org.xmind.ui.internal.tools;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.tool.IGraphicalTool;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyCreateTool;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author frankshaka
 * 
 */
public class FloatingTopicCreateTool extends DummyCreateTool {

    private Request request;

    private Point position;

    private BranchDummy branchDummy = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.AbstractTool#onActivated(org.xmind.gef.tool.ITool)
     */
    protected void onActivated(ITool prevTool) {
        request = null;
        if (prevTool instanceof IGraphicalTool) {
            setCursorPosition(((IGraphicalTool) prevTool).getCursorPosition());
        }
        super.onActivated(prevTool);
        createDummy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.AbstractTool#onDeactivated(org.xmind.gef.tool.ITool)
     */
    protected void onDeactivated(ITool nextTool) {
        super.onDeactivated(nextTool);
        request = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.CreateTool#internalHandleRequest(org.xmind.gef.Request
     * )
     */
    @Override
    protected void internalHandleRequest(Request request) {
        if (!getStatus().isStatus(GEF.ST_ACTIVE))
            return;

        if (this.request == null
                && MindMapUI.REQ_CREATE_FLOAT.equals(request.getType())) {
            this.request = request;
            pack();
        } else {
            super.internalHandleRequest(request);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tools.DummyCreateTool#createRequest()
     */
    protected Request createRequest() {
        if (position == null)
            return null;

        Request req;
        if (request != null) {
            req = request;
        } else {
            req = new Request(MindMapUI.REQ_CREATE_FLOAT);
        }
        req.setParameter(GEF.PARAM_POSITION, position);
        ISheetPart sheet = (ISheetPart) getTargetViewer().getAdapter(
                ISheetPart.class);
        if (sheet != null) {
            req.setPrimaryTarget(sheet);
        }
        return req;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.tools.DummyCreateTool#doCreateDummy()
     */
    protected IFigure doCreateDummy() {
        if (branchDummy == null) {
            branchDummy = new BranchDummy(getTargetViewer(), true);
            branchDummy.getTopic().setTitleText(
                    MindMapMessages.TitleText_FloatingTopic);
            pack();
        }
        IFigure figure = branchDummy.getBranch().getFigure();
        if (getCursorPosition() != null) {
            if (figure instanceof IReferencedFigure) {
                ((IReferencedFigure) figure).setReference(getCursorPosition());
            } else {
                Dimension size = figure.getSize();
                figure.setLocation(getCursorPosition().getTranslated(
                        -size.width / 2, -size.height / 2));
            }
        }
        return figure;
    }

    private void pack() {
        if (branchDummy == null)
            return;

        if (request != null) {
            Object param = request.getParameter(MindMapUI.PARAM_PROPERTY_PREFIX
                    + Core.StructureClass);
            if (param instanceof String) {
                branchDummy.getTopic().setStructureClass((String) param);
            }
        }
        branchDummy.pack();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tools.DummyCreateTool#destroyDummy(org.eclipse.draw2d.IFigure
     * )
     */
    protected void destroyDummy(IFigure dummy) {
        if (branchDummy != null) {
            branchDummy.dispose();
            branchDummy = null;
        }
        super.destroyDummy(dummy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tools.DummyCreateTool#updateDummyPosition(org.eclipse.draw2d
     * .IFigure, org.eclipse.draw2d.geometry.Point)
     */
    protected void updateDummyPosition(IFigure dummy, Point pos) {
        this.position = pos;
        ((IReferencedFigure) dummy).setReference(pos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.CreateTool#canCancelOnRightMouseDown(org.xmind.gef
     * .event.MouseEvent)
     */
    protected boolean canCancelOnRightMouseDown(MouseEvent me) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.tool.CreateTool#canFinish(java.lang.String)
     */
    protected boolean canFinish(String requestType) {
        return position != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.CreateTool#canFinishOnLeftMouseDown(org.xmind.gef.
     * event.MouseEvent)
     */
    protected boolean canFinishOnLeftMouseDown(MouseEvent me) {
        return position != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.tool.CreateTool#shouldCancel(org.xmind.gef.event.KeyEvent)
     */
    protected boolean shouldCancel(KeyEvent ke) {
        return SWTUtils.matchKey(ke.getState(), ke.keyCode, 0, SWT.ESC);
    }

}
