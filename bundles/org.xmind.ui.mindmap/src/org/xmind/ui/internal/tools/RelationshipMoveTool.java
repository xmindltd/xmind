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
package org.xmind.ui.internal.tools;

import static org.xmind.ui.mindmap.MindMapUI.SOURCE_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.SOURCE_CONTROL_POINT;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_ANCHOR;
import static org.xmind.ui.mindmap.MindMapUI.TARGET_CONTROL_POINT;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;
import org.xmind.core.IRelationshipEnd;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.SelectionFigure;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.internal.figures.RelationshipFigure;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IRelationshipPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.DummyMoveTool;
import org.xmind.ui.util.MindMapUtils;

public class RelationshipMoveTool extends DummyMoveTool {

//    private static final Double DEFAULT_AMOUNT = Double
//            .valueOf(Styles.DEF_CONTROL_POINT_AMOUNT);

    private int pointId = -1;

    private RelationshipDummy relDummy = null;

//    private Double newAngle = null;
//
//    private Double newAmount = null;

    private Point newPosition = null;

    private IPart newNode = null;

    private IFeedbackService feedbackService = null;

    public void setSource(IGraphicalEditPart source) {
        Assert.isTrue(source instanceof IRelationshipPart);
        super.setSource(source);
    }

    protected IRelationshipPart getSourceRelationship() {
        return (IRelationshipPart) super.getSource();
    }

    protected IFigure createDummy() {
        IRelationshipPart sourceRel = getSourceRelationship();
        if (sourceRel == null)
            return null;

        Point pos = getStartingPosition();
        if (pos != null) {
            pointId = sourceRel.getPointId(pos);
        }

        if (pointId == -1)
            return null;

        if (relDummy == null) {
            Layer layer = getTargetViewer().getLayer(GEF.LAYER_PRESENTATION);
            if (layer != null) {
                relDummy = createRelationshipDummy(layer, sourceRel);
            }
        }
        if (relDummy != null)
            return relDummy.getRelDummy().getFigure();
        return null;
    }

    protected RelationshipDummy createRelationshipDummy(IFigure layer,
            IRelationshipPart sourceRel) {
        return new RelationshipDummy(layer, sourceRel, pointId,
                getTargetViewer());
    }

    protected void destroyDummy() {
        pointId = -1;
        newPosition = null;
//        newAngle = null;
//        newAmount = null;
        setNewNode(null);
        feedbackService = null;
        if (relDummy != null) {
            relDummy.dispose();
            relDummy = null;
        }
        super.destroyDummy();
    }

    private void setNewNode(IPart node) {
        IPart oldNode = this.newNode;
        if (node == oldNode)
            return;

        if (oldNode != null) {
            removeNodeFeedback(oldNode);
        }
        this.newNode = node;
        if (node != null) {
            addNodeFeedback(node);
        }
    }

    private void addNodeFeedback(IPart node) {
        if (node instanceof IGraphicalPart) {
            IFigure nodeFigure = ((IGraphicalPart) node).getFigure();
            if (nodeFigure != null) {
                if (feedbackService == null) {
                    feedbackService = (IFeedbackService) getTargetViewer()
                            .getService(IFeedbackService.class);
                }
                if (feedbackService != null) {
                    SelectionFigure figure = feedbackService
                            .addSelection(nodeFigure);
                    figure.setPreselected(true);
                }
            }
        }
    }

    private void removeNodeFeedback(IPart node) {
        if (feedbackService != null) {
            if (node instanceof IGraphicalPart) {
                IFigure nodeFigure = ((IGraphicalPart) node).getFigure();
                if (nodeFigure != null) {
                    feedbackService.removeSelection(nodeFigure);
                }
            }
        }
    }

    private IPart findRelationshipEndPart(IPart part) {
        if (part != null) {
            IConnectionPart rel = getSourceRelationship();
            if (part == rel.getSourceNode() || part == rel.getTargetNode())
                return null;

            Object m = MindMapUtils.getRealModel(part);
            if (m instanceof IRelationshipEnd) {
                return (IPart) part;
            }
        }
        return null;
    }

    protected void onMoving(Point currentPos, MouseDragEvent me) {
        super.onMoving(currentPos, me);
        if (pointId == SOURCE_ANCHOR || pointId == TARGET_ANCHOR) {
            setNewNode(findRelationshipEndPart(me.target));
        }
    }

    protected void updateDummyPosition(Point pos) {
        if (relDummy == null)
            return;

        if (pointId == SOURCE_ANCHOR || pointId == TARGET_ANCHOR) {
            INodePart node;
            if (pointId == SOURCE_ANCHOR) {
                node = relDummy.getSourceNodeDummy();
            } else {
                node = relDummy.getTargetNodeDummy();
            }
            if (node != null) {
                IFigure fig = node.getFigure();
                if (fig instanceof IReferencedFigure) {
                    ((IReferencedFigure) fig).setReference(pos);
                } else {
                    fig.setLocation(pos);
                }
            }
        } else if (pointId == SOURCE_CONTROL_POINT
                || pointId == TARGET_CONTROL_POINT) {
            IFigure fig = relDummy.getRelDummy().getFigure();
            if (fig != null && fig instanceof RelationshipFigure) {
                RelationshipFigure rf = (RelationshipFigure) fig;
                IRelationshipDecoration dec = rf.getDecoration();
                if (dec != null) {
                    boolean sourceOrTarget = pointId == SOURCE_CONTROL_POINT;
                    updateControlPointPosition(pos, rf, dec, sourceOrTarget);
                }
            }
        }

        relDummy.refreshFeedback();
    }

    private void updateControlPointPosition(Point pos,
            RelationshipFigure figure, IRelationshipDecoration decoration,
            boolean sourceOrTarget) {
        PrecisionPoint ref;
        if (sourceOrTarget)
            ref = figure.getSourceAnchor().getReferencePoint();
        else
            ref = figure.getTargetAnchor().getReferencePoint();
        newPosition = new Point((int) (pos.x - ref.x), (int) (pos.y - ref.y));
        if (sourceOrTarget) {
            decoration.setRelativeSourceControlPoint(figure, newPosition);
        } else {
            decoration.setRelativeTargetControlPoint(figure, newPosition);
        }
        relDummy.getRelDummy().getRelationship()
                .getControlPoint(sourceOrTarget ? 0 : 1)
                .setPosition(newPosition.x, newPosition.y);
//
//        PrecisionPoint ref1 = figure.getSourceAnchor().getReferencePoint();
//        PrecisionPoint ref2 = figure.getTargetAnchor().getReferencePoint();
//        newAngle = Double
//                .valueOf(calcNewAngle(pos, ref1, ref2, sourceOrTarget));
//
//        if (sourceOrTarget) {
//            decoration.setSourceControlPointHint(figure, newAngle,
//                    DEFAULT_AMOUNT);
//        } else {
//            decoration.setTargetControlPointHint(figure, newAngle,
//                    DEFAULT_AMOUNT);
//        }
//
//        PrecisionPoint ancPonit1 = decoration.getSourcePosition(figure);
//        PrecisionPoint ancPoint2 = decoration.getTargetPosition(figure);
//        newAmount = Double.valueOf(calcNewAmount(pos, ancPonit1, ancPoint2,
//                sourceOrTarget));
//
//        relDummy.getRelDummy().getRelationship().setControlPoint(
//                sourceOrTarget ? 0 : 1, newAngle, newAmount);
    }

//    private double calcNewAmount(Point cursorPos,
//            PrecisionPoint sourceAnchorPoint, PrecisionPoint targetAnchorPoint,
//            boolean sourceOrTarget) {
//        if (sourceOrTarget) {
//            return Geometry.getAmount(cursorPos.x, cursorPos.y,
//                    sourceAnchorPoint.x, sourceAnchorPoint.y,
//                    targetAnchorPoint.x, targetAnchorPoint.y);
//        }
//        return Geometry.getAmount(cursorPos.x, cursorPos.y,
//                targetAnchorPoint.x, targetAnchorPoint.y, sourceAnchorPoint.x,
//                sourceAnchorPoint.y);
//    }
//
//    private double calcNewAngle(Point cursorPos,
//            PrecisionPoint sourceReference, PrecisionPoint targetReference,
//            boolean sourceOrTarget) {
//        if (sourceOrTarget) {
//            return Geometry.getDeltaAngle(cursorPos.x, cursorPos.y,
//                    sourceReference.x, sourceReference.y, targetReference.x,
//                    targetReference.y);
//        }
//        return Geometry.getDeltaAngle(cursorPos.x, cursorPos.y,
//                targetReference.x, targetReference.y, sourceReference.x,
//                sourceReference.y);
//    }

    public Cursor getCurrentCursor(Point pos, IPart host) {
        if (pointId == SOURCE_ANCHOR || pointId == TARGET_ANCHOR) {
            return Cursors.HAND;
        } else if (pointId == SOURCE_CONTROL_POINT
                || pointId == TARGET_CONTROL_POINT) {
            return Cursors.CROSS;
        }
        return super.getCurrentCursor(pos, host);
    }

    protected Request createRequest() {
        Request request = null;
        if (pointId == SOURCE_ANCHOR || pointId == TARGET_ANCHOR) {
            if (newNode != null) {
                request = new Request(MindMapUI.REQ_RETARGET_REL);
                request.setParameter(MindMapUI.PARAM_MOVE_REL_NEW_NODE, newNode);
//                MoveRelationshipRequest req = new MoveRelationshipRequest(
//                        MindMapUI.REQ_RETARGET_REL, getSourceRelationship(),
//                        pointId);
//                req.setNewNode(newNode);
//                return req;
            }
        } else if (pointId == SOURCE_CONTROL_POINT
                || pointId == TARGET_CONTROL_POINT) {
            if (newPosition != null) {
                request = new Request(MindMapUI.REQ_MOVE_CONTROL_POINT);
                request.setParameter(GEF.PARAM_POSITION, newPosition);
            }
//            if (newAngle != null && newAmount != null) {
//                request = new Request(MindMapUI.REQ_MOVE_CONTROL_POINT);
//                request.setParameter(MindMapUI.PARAM_MOVE_REL_NEW_ANGLE,
//                        newAngle);
//                request.setParameter(MindMapUI.PARAM_MOVE_REL_NEW_AMOUNT,
//                        newAmount);
////                MoveRelationshipRequest req = new MoveRelationshipRequest(
////                        MindMapUI.REQ_MOVE_CONTROL_POINT,
////                        getSourceRelationship(), pointId);
////                req.setNewAngle(newAngle);
////                req.setNewAmount(newAmount);
////                return req;
//            }
        }
        if (request != null) {
            request.setPrimaryTarget(getSourceRelationship());
            request.setDomain(getDomain());
            request.setViewer(getTargetViewer());
            request.setParameter(MindMapUI.PARAM_MOVE_REL_POINT_ID,
                    Integer.valueOf(pointId));
        }
        return request;
    }

}