package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

public class RoundedRectBalloonCalloutBranchConnection
        extends AbstractCalloutBranchConnection implements ICorneredDecoration {

    public static final int OFFSET = 10;

    private int cornerSize = 0;

    public RoundedRectBalloonCalloutBranchConnection() {
        super();
    }

    public RoundedRectBalloonCalloutBranchConnection(String id) {
        super(id);
    }

    @Override
    protected void route(IFigure figure, Path shape) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);

        int corner = getCornerSize();

        Rectangle r = getTargetAnchor().getOwner().getBounds();
        r = getOutlineBox(r);

        if (getSourceAnchor() != null) {
            PrecisionPoint rp = getSourceAnchor().getReferencePoint();
            p2 = Geometry.getChopBoxLocation(rp.x, rp.y, r, 0);
        }

        int side = Geometry.getSide(p2.x, p2.y, r);
        shape.moveTo(p2);

        if (side == Geometry.SIDE_ONE) {
            shape.lineTo(p1);
            double delta = p2.x - r.x - OFFSET - corner;
            if (delta >= 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x - OFFSET, r.y);
                shape.lineTo(p3);
                shape.lineTo(r.x + corner, r.y);
                shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 90, 90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(r.x, r.y - delta);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x, r.y + r.height - corner);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, 180, 90);
            shape.lineTo(r.x + r.width - corner, r.y + r.height);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, -90,
                    90);
            shape.lineTo(r.x + r.width, r.y + corner);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 0, 90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_TWO) {
            shape.lineTo(p1);
            double delta = r.getTopRight().x - p2.x - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x + OFFSET, r.y);
                shape.lineTo(p3);
                shape.lineTo(r.x + r.width - corner, r.y);
                shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                        2 * corner, 90, -90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(r.getTopRight().x,
                        p2.y - delta);
                shape.lineTo(p3);
            }
            shape.lineTo(r.getTopRight().x, r.y + r.height - corner);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, 0,
                    -90);
            shape.lineTo(r.x + corner, r.y + r.height);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, -90, -90);
            shape.lineTo(r.x, r.y + corner);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 180, -90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_THREE) {
            double delta = p2.y - r.getTopRight().y - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(r.getTopRight().x,
                        p2.y - OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(r.x + r.width, r.y + corner);
                shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                        2 * corner, 0, 90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        r.getTopRight().x + delta, r.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x + corner, r.y);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 90, 90);
            shape.lineTo(r.x, r.y + r.height - corner);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, 180, 90);
            shape.lineTo(r.x + r.width - corner, r.y + r.height);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, -90,
                    90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_FOUR) {
            double delta = r.getBottomRight().y - p2.y - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(r.getTopRight().x,
                        p2.y + OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(r.x + r.width, r.y + r.height - corner);
                shape.addArc(r.x + r.width - 2 * corner,
                        r.y + r.height - 2 * corner, 2 * corner, 2 * corner, 0,
                        -90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        r.getBottomRight().x + delta, r.getBottomRight().y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x + corner, r.y + r.height);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, -90, -90);
            shape.lineTo(r.x, r.y + corner);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 180, -90);
            shape.lineTo(r.x + r.width - corner, r.y);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 90, -90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_FIVE) {
            double delta = r.getBottomRight().x - p2.x - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x + OFFSET, p2.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(r.x + r.width - corner, r.y + r.height);
                shape.addArc(r.x + r.width - 2 * corner,
                        r.y + r.height - 2 * corner, 2 * corner, 2 * corner,
                        -90, 90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(r.getBottomRight().x,
                        r.getBottomRight().y + delta);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x + r.width, r.y + corner);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 0, 90);
            shape.lineTo(r.x + corner, r.y);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 90, 90);
            shape.lineTo(r.x, r.y + r.height - corner);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, 180, 90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_SIX) {
            double delta = p2.x - r.getBottomLeft().x - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x - OFFSET, p2.y);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(r.x + corner, r.y + r.height);
                shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                        2 * corner, -90, -90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(r.getBottomLeft().x,
                        r.getBottomLeft().y + delta);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x, r.y + corner);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 180, -90);
            shape.lineTo(r.x + r.width - corner, r.y);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 90, -90);
            shape.lineTo(r.getTopRight().x, r.y + r.height - corner);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, 0,
                    -90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_SEVEN) {
            double delta = r.getBottomLeft().y - p2.y - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x, p2.y + OFFSET);
                shape.lineTo(p1);
                shape.lineTo(p3);
                shape.lineTo(r.x, r.y + r.height - corner);
                shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                        2 * corner, 180, 90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(
                        r.getBottomLeft().x - delta, r.getBottomLeft().y);
                shape.lineTo(p1);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x + r.width - corner, r.y + r.height);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, -90,
                    90);
            shape.lineTo(r.x + r.width, r.y + corner);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 0, 90);
            shape.lineTo(r.x + corner, r.y);
            shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 90, 90);
            shape.lineTo(p2);
        } else if (side == Geometry.SIDE_EIGHT) {
            shape.lineTo(p1);
            double delta = p2.y - r.y - OFFSET - corner;
            if (delta > 0) {
                PrecisionPoint p3 = new PrecisionPoint(p2.x, p2.y - OFFSET);
                shape.lineTo(p3);
                shape.lineTo(r.x, r.y + corner);
                shape.addArc(r.x, r.y, 2 * corner, 2 * corner, 180, -90);
            } else {
                PrecisionPoint p3 = new PrecisionPoint(r.x - delta, r.y);
                shape.lineTo(p3);
            }
            shape.lineTo(r.x + r.width - corner, r.y);
            shape.addArc(r.x + r.width - 2 * corner, r.y, 2 * corner,
                    2 * corner, 90, -90);

            shape.lineTo(r.getTopRight().x, r.y + r.height - corner);
            shape.addArc(r.x + r.width - 2 * corner,
                    r.y + r.height - 2 * corner, 2 * corner, 2 * corner, 0,
                    -90);
            shape.lineTo(r.x + corner, r.y + r.height);
            shape.addArc(r.x, r.y + r.height - 2 * corner, 2 * corner,
                    2 * corner, -90, -90);
            shape.lineTo(p2);
        }
        shape.close();

    }

    public int getCornerSize() {
        return cornerSize;
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

}
