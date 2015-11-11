package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;

public class ParallelogramTopicDecoration extends AbstractTopicDecoration {

    private static final float SCALE = 0.5f;

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        if (purpose == CHECK) {
            float halfLineWidth = getLineWidth() * 0.5f;
            shape.moveTo(box.x - +box.height * SCALE + halfLineWidth,
                    box.y - halfLineWidth);
            shape.lineTo(box.x - halfLineWidth, box.bottom() + halfLineWidth);
            shape.lineTo(box.right() - box.height * SCALE + halfLineWidth,
                    box.bottom() + halfLineWidth);
            shape.lineTo(box.right() + halfLineWidth, box.y - halfLineWidth);
        } else {
            float scaledLineWidth = getLineWidth() * SCALE;
            shape.moveTo(box.x + box.height * SCALE, box.y);
            shape.lineTo(box.x + scaledLineWidth, box.bottom());
            shape.lineTo(box.right() - box.height * SCALE, box.bottom());
            shape.lineTo(box.right() - scaledLineWidth, box.y);
        }
        shape.close();
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return new Insets(getTopMargin() + getLineWidth(),
                getLeftMargin() + getLineWidth() + Math.round(height * SCALE)
                        + 1,
                getBottomMargin() + getLineWidth(), getRightMargin()
                        + getLineWidth() + Math.round(height * SCALE) + 1);
    }

    @Override
    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        boolean isVertical = false;

        Rectangle r = getOutlineBox(figure);
        double centerX = r.x + 0.5 * r.width;
        double centerY = r.y + 0.5 * r.height;
        double dx = refX - centerX;
        double dy = refY - centerY;

        if (Math.abs(dx) >= 99)
            if (expansion != 0.0) {
                expansion += r.height * SCALE;
            } else {
                expansion += r.height * SCALE * 0.5;
            }
        if (Math.abs(dy) > 99)
            isVertical = true;

        if (dx == 0)
            return new PrecisionPoint(refX,
                    (dy > 0) ? r.bottom() + expansion : r.y - expansion);
        if (dy == 0)
            return new PrecisionPoint(
                    (dx > 0) ? r.right() - r.height * SCALE + expansion
                            : r.x + r.height * SCALE - expansion,
                    refY);

        double scale = 0.5
                / Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);

        dx *= scale;
        dy *= scale;
        double d = Math.hypot(dx, dy);
        if (d != 0) {
            double s = expansion / d;
            dx += dx * s;
            dy += dy * s;
        }
        centerX += dx;
        centerY += dy;

        if (isVertical)
            return new PrecisionPoint(centerX, centerY);

        if (centerY >= r.y && centerY <= r.bottom()) {
            if (dx > 0) {
                double py = centerY - r.y;
                centerX -= py * SCALE * 2;
            } else {
                double py = r.bottom() - centerY;
                centerX += py * SCALE * 2;
            }
        }

        return new PrecisionPoint(centerX, centerY);
    }

}
