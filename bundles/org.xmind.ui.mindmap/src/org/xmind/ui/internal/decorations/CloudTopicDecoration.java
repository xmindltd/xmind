package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionDimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;
import org.xmind.ui.internal.svgsupport.SvgPathParser;

public class CloudTopicDecoration extends AbstractTopicDecoration {

    private static final float scaleLeft = 0.17f;
    private static final float scaleRight = 0.16f;
    private static final float scaleTop = 0.22f;
    private static final float scaleBottom = 0.27f;

    private static final float RATIO = 2.5f;

    private String svgPath;

    public CloudTopicDecoration() {

    }

    public CloudTopicDecoration(String id) {
        super(id);
    }

    public CloudTopicDecoration(String id, String svgPath) {
        super(id);
        this.svgPath = svgPath;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        Path path = new Path(Display.getCurrent());
        SvgPathParser parser = SvgPathParser.getInstance();

        float halfLineWidth = getLineWidth() * 0.5f;
        if (purpose == CHECK) {
            parser.parseSvgPath(path, box.getCenter().x - halfLineWidth,
                    box.getCenter().y + halfLineWidth,
                    box.width + getLineWidth(), box.height + getLineWidth(),
                    svgPath);
        } else {
            parser.parseSvgPath(path, box.getCenter().x + 1,
                    box.getCenter().y + 1, box.width, box.height, svgPath);
        }

        shape.addPath(path);
        path.close();
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        float scaleWidth = 1 - scaleLeft - scaleRight;
        float scaleHeight = 1 - scaleTop - scaleBottom;
        Rectangle bounds = figure.getBounds();
        Point tf = bounds.getTopLeft().getTranslated(bounds.width * scaleLeft,
                bounds.height * scaleTop);
        Rectangle area = new Rectangle(tf.x, tf.y,
                (int) (bounds.width * scaleWidth),
                (int) (bounds.height * scaleHeight));

        PrecisionPoint p1 = Geometry.getChopBoxLocation(refX, refY, area,
                expansion);

        PrecisionPoint p2 = Geometry.getChopBoxLocation(refX, refY,
                getOutlineBox(figure), expansion);

        return calcAnchorLocation(figure, p1, p2);
    }

    private PrecisionPoint calcAnchorLocation(IFigure figure, PrecisionPoint p1,
            PrecisionPoint p2) {
        if (p1.getDistance(p2) < (getLineWidth() == 0 ? 1 : getLineWidth()))
            return p2;

        PrecisionPoint p3 = new PrecisionPoint((p1.x + p2.x) / 2,
                (p1.y + p2.y) / 2);
        if (containsPoint(figure, (float) p3.x, (float) p3.y))
            return calcAnchorLocation(figure, p3, p2);
        else
            return calcAnchorLocation(figure, p1, p3);
    }

    private boolean containsPoint(IFigure figure, float x, float y) {
        checkValidation(figure);
        GC gc = GraphicsUtils.getAdvanced().getGC();
        gc.setLineWidth(getCheckingLineWidth());
        Path shape = new Path(Display.getCurrent());
        sketch(figure, shape, getOutlineBox(figure), FILL);
        boolean ret = shape.contains(x, y, gc, false);
        shape.close();
        shape.dispose();
        return ret;
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        float scaleWidth = 1 - scaleLeft - scaleRight;
        float scaleHeight = 1 - scaleTop - scaleBottom;
        Insets insets = new Insets(
                (int) ((height + getTopMargin() + getLineWidth()) / scaleHeight
                        * scaleTop),
                (int) ((width + getLeftMargin() + getLineWidth()) / scaleWidth
                        * scaleLeft),
                (int) ((height + getBottomMargin() + getLineWidth())
                        / scaleHeight * scaleBottom),
                (int) ((width + getRightMargin() + getLineWidth()) / scaleWidth
                        * scaleRight));

        PrecisionDimension dimension = expandWHitAsRatio(
                new PrecisionDimension(width + insets.left + insets.right,
                        height + insets.top + insets.bottom));
        Insets inset = new Insets();
        inset.top = (int) ((dimension.height - height) * scaleTop
                / (scaleTop + scaleBottom));
        inset.left = (int) ((dimension.width - width) * scaleLeft
                / (scaleLeft + scaleRight));
        inset.bottom = (int) ((dimension.height - height) * scaleBottom
                / (scaleTop + scaleBottom));
        inset.right = (int) ((dimension.width - width) * scaleRight
                / (scaleLeft + scaleRight));
        return inset;
    }

    private PrecisionDimension expandWHitAsRatio(
            PrecisionDimension whitDimension) {
        PrecisionDimension dimension = whitDimension;
        if (whitDimension.width > whitDimension.height * RATIO) {
            dimension.height = (int) Math.ceil(whitDimension.width / RATIO);
            dimension.width = (int) (dimension.height * RATIO);
        } else if (whitDimension.width < whitDimension.height * RATIO) {
            dimension.width = (int) Math.ceil(whitDimension.height * RATIO);
        }
        return dimension;
    }

}
