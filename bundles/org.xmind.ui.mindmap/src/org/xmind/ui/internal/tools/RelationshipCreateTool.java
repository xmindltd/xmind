/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.tools;

import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.IRelationshipEnd;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IOriginBased;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.KeyEvent;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.event.MouseEvent;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyCreateTool;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.SWTUtils;

public class RelationshipCreateTool extends DummyCreateTool {

    private IPart sourceNode = null;

    private IPart targetNode = null;

    private Point targetPosition = null;

    private RelationshipDummy relDummy = null;

    protected boolean shouldCancel(KeyEvent ke) {
        return SWTUtils.matchKey(ke.getState(), ke.keyCode, 0, SWT.ESC);
    }

    protected boolean canCancelOnRightMouseDown(MouseEvent me) {
        return true;
    }

    protected boolean canFinishOnLeftMouseDown(MouseEvent me) {
        IPart host = me.target;
        if (sourceNode == null) {
            sourceNode = getRelationshipEndPart(host);
            if (sourceNode != null) {
                createDummy();
            }
        } else if (targetNode == null || targetPosition == null) {
            if (host == null || host instanceof IRootPart
                    || host instanceof ISheetPart) {
                targetPosition = me.cursorLocation;
                ISheetPart sheet = (ISheetPart) getTargetViewer().getAdapter(
                        ISheetPart.class);
                if (sheet != null) {
                    IFigure figure = sheet.getFigure();
                    if (figure instanceof IOriginBased) {
                        Point origin = ((IOriginBased) figure).getOrigin();
                        if (origin != null) {
                            targetPosition = new Point(targetPosition.x
                                    - origin.x, targetPosition.y - origin.y);
                        }
                    }
                }
            } else {
                IPart node = getRelationshipEndPart(host);
                if (node != null && node != sourceNode) {
                    targetNode = node;
                }
            }
        }
        return canFinish();
    }

    protected IFigure doCreateDummy() {
        if (relDummy == null) {
            if (sourceNode instanceof IGraphicalPart) {
                Layer layer = getTargetViewer()
                        .getLayer(GEF.LAYER_PRESENTATION);
                if (layer != null) {
                    relDummy = new RelationshipDummy(layer,
                            (IGraphicalPart) sourceNode, getCursorPosition(),
                            getTargetViewer());
                }
            }
        }
        if (relDummy != null)
            return relDummy.getRelDummy().getFigure();
        return null;
    }

    protected void destroyDummy() {
        if (relDummy != null) {
            relDummy.dispose();
            relDummy = null;
        }
        super.destroyDummy();
    }

    private IPart getRelationshipEndPart(IPart part) {
        if (part != null && part instanceof IPart) {
            Object m = MindMapUtils.getRealModel(part);
            if (m instanceof IRelationshipEnd) {
                return (IPart) part;
            }
        }
        return null;
    }

    protected boolean canFinish(String requestType) {
        return canFinish();
    }

    private boolean canFinish() {
        return sourceNode != null
                && (targetNode != null || targetPosition != null);
    }

    protected void onActivated(ITool prevTool) {
        super.onActivated(prevTool);
        List<IPart> selectedParts = getSelectedParts(getTargetViewer());
        IPart sourceNode = null;
        for (IPart p : selectedParts) {
            sourceNode = getRelationshipEndPart(p);
            if (sourceNode != null)
                break;
        }
        getTargetViewer().setSelection(StructuredSelection.EMPTY);
        if (sourceNode != null) {
            this.sourceNode = sourceNode;
            createDummy();
        }
    }

    protected boolean handleMouseEntered(MouseEvent me) {
        boolean ret = super.handleMouseEntered(me);
        IPart node = getRelationshipEndPart(me.target);
        if (node != null) {
            getTargetViewer().setPreselected(node);
        }
        return ret;
    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (getRelationshipEndPart(host) != null)
            return Cursors.HAND;
        return MindMapUI.getImages().getCursor(
                IMindMapImages.CURSOR_RELATIONSHIP);
    }

    protected void recover() {
        super.recover();
        sourceNode = null;
        targetNode = null;
        targetPosition = null;
    }

    protected Request createRequest() {
        if (sourceNode != null) {
            ISheetPart sheet = (ISheetPart) getTargetViewer().getAdapter(
                    ISheetPart.class);
            if (sheet != null) {
                Request request = new Request(MindMapUI.REQ_CREATE_RELATIONSHIP);
                request.setDomain(getDomain());
                request.setViewer(getTargetViewer());
                request.setPrimaryTarget(sheet);
                request.setParameter(MindMapUI.PARAM_SOURCE_NODE, sourceNode);
                if (targetNode != null) {
                    request.setParameter(MindMapUI.PARAM_TARGET_NODE,
                            targetNode);
//                    return new RelationshipRequest(
//                            MindMapUI.REQ_CREATE_RELATIONSHIP, sheet,
//                            sourceNode, targetNode);
                } else if (targetPosition != null) {
                    request.setParameter(GEF.PARAM_POSITION, targetPosition);
//                    return new RelationshipRequest(
//                            MindMapUI.REQ_CREATE_RELATIONSHIP, sheet,
//                            sourceNode, targetPosition);
                }
                return request;
            }
        }
        return null;
    }

    protected boolean handleMouseDrag(MouseDragEvent me) {
        boolean ret = super.handleMouseDrag(me);
        if (getDummy() != null) {
            updateDummyPosition(getDummy(), getCursorPosition());
        }
        return ret;
    }

    protected void updateDummyPosition(IFigure dummy, Point pos) {
        if (relDummy != null) {
            IFigure fig = relDummy.getTargetNodeDummy().getFigure();
            if (fig instanceof IReferencedFigure) {
                ((IReferencedFigure) fig).setReference(pos);
            } else {
                fig.setLocation(pos);
            }
        }
    }

    protected boolean handleMouseUp(MouseEvent me) {
        boolean ret = super.handleMouseUp(me);
        if (sourceNode != null && targetNode == null && targetPosition == null) {
            IPart node = getRelationshipEndPart(me.target);
            if (node != null && node != sourceNode) {
                targetNode = node;
                if (canFinish())
                    finish();
            }
        }
        return ret;
    }

}