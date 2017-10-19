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
            shape.moveTo(box.x + box.height * SCALE - halfLineWidth,
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

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {

        Rectangle r = getOutlineBox(figure);
        double cx = r.x + 0.5f * r.width;
        double cy = r.y + 0.5f * r.height;
        double dx = refX - cx;
        double dy = refY - cy;

        if (dx == 0)
            return new PrecisionPoint(refX,
                    (dy > 0) ? r.bottom() + expansion : r.y - expansion);
        if (dy == 0)
            return new PrecisionPoint(
                    (dx > 0) ? r.right() - r.height * SCALE * SCALE + expansion
                            : r.x + r.height * SCALE * SCALE - expansion,
                    refY);

        double scale = 0.5f
                / Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);

        dx = Math.round(dx *= scale);
        dy = Math.round(dy *= scale);

        if (Math.abs(dy) < r.height / 2 || ((dy >= r.height / 2
                && dx > r.width / 2 - r.height / 2)
                || (dy <= -r.height / 2 && dx < -r.width / 2 + r.height / 2))) {
            dx = (dx > 0) ? dx - (r.height * SCALE + dy) * SCALE
                    : dx + (r.height * SCALE - dy) * SCALE;

        }

        cx += dx;
        cy += dy;

        return new PrecisionPoint(cx, cy);
    }

}
