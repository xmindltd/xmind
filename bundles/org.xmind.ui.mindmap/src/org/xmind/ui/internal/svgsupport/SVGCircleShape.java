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
final public class SVGCircleShape extends SVGShape {

    private PrecisionPoint origin;
    private float r;

    SVGCircleShape() {
        super();
    }

    public SVGCircleShape(double x, double y, float r) {
        origin = new PrecisionPoint(x, y);
        this.r = r;
    }

    @Override
    public void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseCircle(implemention, parent);
    }

    private void parseCircle(Element implemention, SVGShape parent) {
        float x = getFloatAttribute(implemention, SVGDefinitionConstants.CX);
        float y = getFloatAttribute(implemention, SVGDefinitionConstants.CY);
        r = getFloatAttribute(implemention, SVGDefinitionConstants.R);

        origin = new PrecisionPoint(x - r, y - r);
    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        path.addArc(new PrecisionRectangle(origin.x, origin.y, 2 * r, 2 * r),
                0.0f, 360.0f);
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return new PrecisionRectangle(origin.x, origin.y, 2 * r, 2 * r);
    }

    @Override
    protected SVGShape clone() {
        SVGCircleShape circle = new SVGCircleShape();
        circle.setInfo(getInfo().clone());

        circle.origin = this.origin;
        circle.r = this.r;

        return circle;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGCircleShape) {
            SVGCircleShape circle = (SVGCircleShape) obj;
            if (circle.getInfo().equals(getInfo())
                    && origin.equals(circle.origin) && (r == circle.r)) {
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
        result = result * 31 + (int) Double.doubleToLongBits(r);
        return result;
    }

    @Override
    public String toString() {
        return "SVGCircle( origin:" + origin.toString() + "  " //$NON-NLS-1$ //$NON-NLS-2$
                + getInfo().toString() + " )"; //$NON-NLS-1$
    }

}
