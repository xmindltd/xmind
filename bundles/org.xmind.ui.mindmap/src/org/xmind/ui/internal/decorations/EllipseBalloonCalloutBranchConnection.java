package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

public class EllipseBalloonCalloutBranchConnection
        extends AbstractCalloutBranchConnection {

    private static final float ARCANGLE = 340;

    public EllipseBalloonCalloutBranchConnection() {
        super();
    }

    public EllipseBalloonCalloutBranchConnection(String id) {
        super(id);
    }

    protected void route(IFigure figure, Path shape) {
        PrecisionPoint p1 = getSourcePosition(figure);
        PrecisionPoint p2 = getTargetPosition(figure);

        Rectangle targetRect = getTargetAnchor().getOwner().getBounds();
        targetRect = getOutlineBox(targetRect);

        double angle = Geometry.getOvalAngle(p2.x, p2.y, targetRect);
        double degrees = Math.toDegrees(angle);

        shape.addArc(targetRect, (float) (degrees), ARCANGLE);
        shape.lineTo(p1);
        shape.close();
    }

}
