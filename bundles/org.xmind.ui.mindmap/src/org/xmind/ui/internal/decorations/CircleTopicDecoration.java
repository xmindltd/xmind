package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class CircleTopicDecoration extends AbstractTopicDecoration {

    public CircleTopicDecoration() {
    }

    public CircleTopicDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        if (purpose == CHECK) {
            shape.addArc(
                    box.getExpanded(getLineWidth() / 2, getLineWidth() / 2), 0,
                    360);
        } else {
            shape.addArc(box, 0, 360);
        }
        shape.close();
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        double diameter = Math.sqrt(width * width + height * height);
        int margin = (getTopMargin() + getLeftMargin()) / 2;
        return new Insets((int) (diameter - height) / 2 + margin,
                (int) (diameter - width) / 2 + margin,
                (int) (diameter - height) / 2 + margin,
                (int) (diameter - width) / 2 + margin);
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        return Geometry.getChopOvalLocation(refX, refY, figure.getBounds(),
                expansion);
    }
}
