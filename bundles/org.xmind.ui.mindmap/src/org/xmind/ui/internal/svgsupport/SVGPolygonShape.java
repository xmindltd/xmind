package org.xmind.ui.internal.svgsupport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
final public class SVGPolygonShape extends SVGShape {
    private static final Pattern pattern = Pattern
            .compile("[+-]?\\d+(\\.\\d+)?"); //$NON-NLS-1$
    private List<PrecisionPoint> points;

    SVGPolygonShape() {
        super();
        points = new ArrayList<PrecisionPoint>();
    }

    public SVGPolygonShape(List<PrecisionPoint> points) {
        this.points = points;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parserPolygon(implemention, parent);
    }

    private void parserPolygon(Element implemention, SVGShape parent) {
        if (implemention.hasAttribute(SVGDefinitionConstants.POINTS)) {
            String pointString = implemention
                    .getAttribute(SVGDefinitionConstants.POINTS);
            final Matcher match = pattern.matcher(pointString);
            LinkedList<String> strs = new LinkedList<String>();
            while (match.find())
                strs.add(match.group());

            if (strs.size() % 2 == 1)
                return;

            while (!strs.isEmpty()) {
                float x = Float.valueOf(strs.removeFirst());
                float y = Float.valueOf(strs.removeFirst());
                points.add(new PrecisionPoint(x, y));
            }
        }

    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        if (!points.isEmpty()) {
            path.moveTo(points.get(0));
            for (int i = 1; i < points.size(); i++) {
                path.lineTo(points.get(i));
            }
            path.lineTo(points.get(0));
        }
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return INVALID_RECT;
    }

    @Override
    protected SVGShape clone() {

        SVGPolygonShape polygon = new SVGPolygonShape();
        polygon.points = this.points;
        polygon.setInfo(getInfo().clone());

        return polygon;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGPolygonShape) {

            SVGPolygonShape polygon = (SVGPolygonShape) obj;
            if (points.size() == polygon.points.size()) {
                for (int i = 0; i < points.size(); i++) {
                    if (!points.get(i).equals(polygon.points.get(i)))
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        for (PrecisionPoint p : points) {
            result = result * 31 + (int) Double.doubleToLongBits(p.x);
            result = result * 31 + (int) Double.doubleToLongBits(p.y);
        }
        return result;
    }

    @Override
    public String toString() {
        return "SVGPolygon(" + getInfo().toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
