package org.xmind.ui.internal.svgsupport;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final public class SVGRectangleShape extends SVGShape {

    private float x, y, rx, ry, height, width;

    SVGRectangleShape() {
        super();
    }

    public SVGRectangleShape(float x, float y, float rx, float ry, float height,
            float width) {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        this.height = height;
        this.width = width;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseRect(implemention, parent);
    }

    private void parseRect(Element implemention, SVGShape parent) {
        x = getFloatAttribute(implemention, SVGDefinitionConstants.X);
        y = getFloatAttribute(implemention, SVGDefinitionConstants.Y);

        rx = getFloatAttribute(implemention, SVGDefinitionConstants.RX);
        ry = getFloatAttribute(implemention, SVGDefinitionConstants.RY);
        if (rx != 0) {
            ry = ry == 0 ? rx : ry;
        } else {
            rx = ry;
        }

        width = getFloatAttribute(implemention, SVGDefinitionConstants.WIDTH);
        height = getFloatAttribute(implemention, SVGDefinitionConstants.HEIGHT);
    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        PrecisionRectangle rect = new PrecisionRectangle(x, y, width, height);
        path.addRoundedRectangle(rect, rx);
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return new PrecisionRectangle(x, y, width, height);
    }

    @Override
    protected SVGShape clone() {
        SVGRectangleShape rect = new SVGRectangleShape();
        rect.setInfo(getInfo().clone());
        rect.x = this.x;
        rect.y = this.y;
        rect.rx = this.rx;
        rect.ry = this.ry;
        rect.height = this.height;
        rect.width = this.width;
        return rect;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGRectangleShape) {
            SVGRectangleShape rect = (SVGRectangleShape) obj;
            if (rect.getInfo().equals(getInfo()) && (x == rect.x)
                    && (width == rect.width) && (height == rect.height)
                    && (rx == rect.rx) && (ry == rect.ry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        result = result * 31 + Float.floatToIntBits(x);
        result = result * 31 + Float.floatToIntBits(y);
        result = result * 31 + Float.floatToIntBits(width);
        result = result * 31 + Float.floatToIntBits(height);
        result = result * 31 + Float.floatToIntBits(rx);
        result = result * 31 + Float.floatToIntBits(ry);
        return result;
    }

    @Override
    public String toString() {
        return "SVGRectangle( x=" + x + " y=" + y //$NON-NLS-1$ //$NON-NLS-2$ 
                + " width=" + width + " height=" + height //$NON-NLS-1$ //$NON-NLS-2$
                + getInfo().toString() + ")"; //$NON-NLS-1$
    }

}
