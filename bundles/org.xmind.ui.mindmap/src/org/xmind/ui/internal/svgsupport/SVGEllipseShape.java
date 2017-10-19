package org.xmind.ui.internal.svgsupport;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final public class SVGEllipseShape extends SVGShape {
    private PrecisionPoint origin;
    private float height, width;

    SVGEllipseShape() {
        super();
    }

    public SVGEllipseShape(double x, double y, float height, float width) {
        origin = new PrecisionPoint(x, y);
        this.height = height;
        this.width = width;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseEllipse(implemention, parent);
    }

    private void parseEllipse(Element implemention, SVGShape parent) {
        float cx = getFloatAttribute(implemention, SVGDefinitionConstants.CX);
        float cy = getFloatAttribute(implemention, SVGDefinitionConstants.CY);
        float rx = getFloatAttribute(implemention, SVGDefinitionConstants.RX);
        float ry = getFloatAttribute(implemention, SVGDefinitionConstants.RY);

        origin = new PrecisionPoint(cx - ry, cy - ry);
        this.height = 2 * rx;
        this.width = 2 * ry;
        if (width == 0)
            width = height;

    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        path.addArc(new PrecisionRectangle(origin.x, origin.y, width, height),
                0.0f, 360.f);

        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return new PrecisionRectangle(origin.x, origin.y, width, height);
    }

    @Override
    protected SVGShape clone() {
        SVGEllipseShape ellipse = new SVGEllipseShape();
        ellipse.origin = this.origin;
        ellipse.height = this.height;
        ellipse.width = this.width;
        ellipse.setInfo(getInfo().clone());

        return ellipse;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGEllipseShape) {
            SVGEllipseShape ellipse = (SVGEllipseShape) obj;
            if (ellipse.getInfo().equals(getInfo())
                    && origin.equals(ellipse.origin) && (width == ellipse.width)
                    && (height == ellipse.height)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        result = result * 31 + (int) Double.doubleToLongBits(origin.x);
        result = result * 31 + (int) Double.doubleToLongBits(origin.y);
        result = result * 31 + Float.floatToIntBits(width);
        result = result * 31 + Float.floatToIntBits(height);
        return result;
    }

    @Override
    public String toString() {
        return "SVGEllipse( origin:" + origin.toString() //$NON-NLS-1$ 
                + getInfo().toString() + " )"; //$NON-NLS-1$
    }
}
