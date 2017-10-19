package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

public class RectangleBalloonCalloutBranchConnection
        extends AbstractCalloutBranchConnection {

    public static final int OFFSET = 10;

    public RectangleBalloonCalloutBranchConnection() {
        super();
    }

    public RectangleBalloonCalloutBranchConnection(String id) {
        super(id);
    }

    @Override
    protected void route(IFigure figure, Path shape) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);

        Rectangle targetRect = getTargetAnchor().getOwner().getBounds();
        targetRect = getOutlineBox(targetRect);

        if (getSourceAnchor() != null) {
            PrecisionPoint rp = getSourceAnchor().getReferencePoint();
            p2 = Geometry.getChopBoxLocation(rp.x, rp.y, targetRect, 0);
        }

        int side = Geometry.getSide(p2.x, p2.y, targetRect);
        shape.moveTo(p2);

        if (side == Geometry.SIDE_ONE) {
            shape.lineTo(p1);
            double delta = p2.x - targetRect.x - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x - OFFSET, p2.y);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getTopLeft());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(targetRect.x,
                        p2.y - delta);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_TWO) {
            double delta = targetRect.getTopRight().x - p2.x - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x + OFFSET, p2.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getTopRight());
            } else {
                shape.lineTo(p1);
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getTopRight().x, p2.y - delta);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_THREE) {
            double delta = p2.y - targetRect.getTopRight().y - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getTopRight().x, p2.y - OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getTopRight());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getTopRight().x + delta,
                        targetRect.getTopRight().y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_FOUR) {
            double delta = targetRect.getBottomRight().y - p2.y - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getTopRight().x, p2.y + OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getBottomRight());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getBottomRight().x + delta,
                        targetRect.getBottomRight().y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_FIVE) {
            double delta = targetRect.getBottomRight().x - p2.x - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x + OFFSET, p2.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getBottomRight());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getBottomRight().x,
                        targetRect.getBottomRight().y + delta);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_SIX) {
            double delta = p2.x - targetRect.getBottomLeft().x - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x - OFFSET, p2.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getBottomLeft());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getBottomLeft().x,
                        targetRect.getBottomLeft().y + delta);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_SEVEN) {
            double delta = targetRect.getBottomLeft().y - p2.y - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x, p2.y + OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getBottomLeft());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        targetRect.getBottomLeft().x - delta,
                        targetRect.getBottomLeft().y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(targetRect.getTopLeft());
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_EIGHT) {
            shape.lineTo(p1);
            double delta = p2.y - targetRect.y - OFFSET;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x, p2.y - OFFSET);
                shape.lineTo(p3);
                shape.lineTo(targetRect.getTopLeft());
            } else {
                PrecisionPoint p3 = new PrecisionPoint(targetRect.x - delta,
                        targetRect.y);
                shape.lineTo(p3);
            }
            shape.lineTo(targetRect.getTopRight());
            shape.lineTo(targetRect.getBottomRight());
            shape.lineTo(targetRect.getBottomLeft());
            shape.lineTo(p2);
        }

        shape.close();

    }
}
