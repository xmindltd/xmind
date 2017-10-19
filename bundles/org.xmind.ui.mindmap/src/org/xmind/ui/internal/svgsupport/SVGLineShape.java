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
final public class SVGLineShape extends SVGShape {

    private PrecisionPoint p1, p2;

    SVGLineShape() {
        super();
    }

    public SVGLineShape(double x1, double y1, double x2, double y2) {
        p1 = new PrecisionPoint(x1, y1);
        p2 = new PrecisionPoint(x2, y2);
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseLine(implemention, parent);
    }

    private void parseLine(Element implemention, SVGShape parent) {
        float x1 = getFloatAttribute(implemention, SVGDefinitionConstants.X1);
        float x2 = getFloatAttribute(implemention, SVGDefinitionConstants.X2);
        float y1 = getFloatAttribute(implemention, SVGDefinitionConstants.Y1);
        float y2 = getFloatAttribute(implemention, SVGDefinitionConstants.Y2);
        this.p1 = new PrecisionPoint(x1, y1);
        this.p2 = new PrecisionPoint(x2, y2);
    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        path.moveTo(p1);
        path.lineTo(p2);
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return new PrecisionRectangle(p1, p2);
    }

    @Override
    protected SVGShape clone() {
        SVGLineShape line = new SVGLineShape();
        line.setInfo(getInfo().clone());
        line.p1 = this.p1;
        line.p2 = this.p2;

        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGLineShape) {
            SVGLineShape line = (SVGLineShape) obj;
            if (line.getInfo().equals(getInfo()) && p1.equals(line.p1)
                    && p2.equals(line.p2)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        result = result * 31 + (int) Double.doubleToLongBits(p1.x);
        result = result * 31 + (int) Double.doubleToLongBits(p1.y);
        result = result * 31 + (int) Double.doubleToLongBits(p2.x);
        result = result * 31 + (int) Double.doubleToLongBits(p2.y);
        return result;
    }

    @Override
    public String toString() {
        return "SVGLine( p1:" + p1.toString() + "; p2:" + p2.toString() //$NON-NLS-1$ //$NON-NLS-2$
                + getInfo().toString() + " )"; //$NON-NLS-1$
    }
}
