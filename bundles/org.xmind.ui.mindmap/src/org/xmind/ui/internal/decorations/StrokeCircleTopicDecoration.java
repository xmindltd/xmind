package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractTopicDecoration;
import org.xmind.ui.internal.svgsupport.SvgPathParser;
import org.xmind.ui.mindmap.IBranchPart;

public class StrokeCircleTopicDecoration extends AbstractTopicDecoration {

    private static final float scaleLeft = 0.13f;
    private static final float scaleRight = 0.13f;
    private static final float scaleTop = 0.13f;
    private static final float scaleBottom = 0.27f;

    private IBranchPart branch;

    private String innerSvgPath;

    private String outerSvgPath;

    public StrokeCircleTopicDecoration() {
    }

    public StrokeCircleTopicDecoration(String id) {
        super(id);
    }

    public StrokeCircleTopicDecoration(String id, IBranchPart branch,
            String innerSvgPath, String outerSvgPath) {
        super(id);
        this.branch = branch;
        this.innerSvgPath = innerSvgPath;
        this.outerSvgPath = outerSvgPath;
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box,
            int purpose) {
        Path innerPath = new Path(Display.getCurrent());
        SvgPathParser parser = SvgPathParser.getInstance();

        float halfLineWidth = getLineWidth() * 0.5f;
        if (purpose == CHECK) {
            parser.parseSvgPath(innerPath, box.getCenter().x - halfLineWidth,
                    box.getCenter().y + halfLineWidth,
                    box.width + getLineWidth(), box.height + getLineWidth(),
                    innerSvgPath);
        } else {
            parser.parseSvgPath(innerPath, box.getCenter().x + 1,
                    box.getCenter().y + 1, box.width, box.height, innerSvgPath);
        }

        innerPath.close();
        shape.addPath(innerPath);
    }

    public PrecisionPoint getAnchorLocation(IFigure figure, double refX,
            double refY, double expansion) {
        if (branch != null && branch.getTopic().isRoot()) {
            float scaleWidth = 1 - scaleLeft - scaleRight;
            float scaleHeight = 1 - scaleTop - scaleBottom;
            Rectangle bounds = figure.getBounds();
            Point tf = bounds.getTopLeft().getTranslated(
                    bounds.width * scaleLeft, bounds.height * scaleTop);
            Rectangle area = new Rectangle(tf.x, tf.y,
                    (int) (bounds.width * scaleWidth),
                    (int) (bounds.height * scaleHeight));

            return Geometry.getChopBoxLocation(refX, refY, area, expansion);
        }

        return super.getAnchorLocation(figure, refX, refY, expansion);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        float scaleWidth = 1 - scaleLeft - scaleRight;
        float scaleHeight = 1 - scaleTop - scaleBottom;
        return new Insets(
                (int) ((height + getTopMargin() + getLineWidth()) / scaleHeight
                        * scaleTop),
                (int) ((width + getLeftMargin() + getLineWidth()) / scaleWidth
                        * scaleLeft),
                (int) ((height + getBottomMargin() + getLineWidth())
                        / scaleHeight * scaleBottom),
                (int) ((width + getRightMargin() + getLineWidth()) / scaleWidth
                        * scaleRight));
    }

    protected boolean containsPoint(IFigure figure, int x, int y,
            boolean outline) {
        checkValidation(figure);
        boolean ret = figure.getBounds().contains(x, y);
        return ret;
    }

    protected void paintOutline(IFigure figure, Graphics graphics) {
        Rectangle box = getOutlineBox(figure);
        if (getLineWidth() != 0 && outerSvgPath != null) {
            graphics.setBackgroundColor(graphics.getForegroundColor());
            Path outerPath = new Path(Display.getCurrent());
            SvgPathParser parser = SvgPathParser.getInstance();
            parser.parseSvgPath(outerPath, box.getCenter().x,
                    box.getCenter().y + 1, box.width, box.height, outerSvgPath);

            graphics.drawPath(outerPath);
            graphics.fillPath(outerPath);

            outerPath.close();
        } else {
            super.paintOutline(figure, graphics);
        }
    }

}
