package org.xmind.ui.internal.svgsupport;

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
final public class SVGPolyLineShape extends SVGShape {
    public static Pattern pattern = Pattern.compile("[+-]?\\d+(\\.\\d+)?"); //$NON-NLS-1$
    private List<PrecisionPoint> points;

    SVGPolyLineShape() {
        super();
    }

    public SVGPolyLineShape(List<PrecisionPoint> points) {
        this.points = points;
    }

    @Override
    public void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parsePolyLine(implemention, parent);
    }

    private void parsePolyLine(Element implemention, SVGShape parent) {
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

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        if (!points.isEmpty()) {
            path.moveTo(points.get(0));
            for (int i = 1; i < points.size(); i++) {
                path.lineTo(points.get(i));
            }
        }
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return INVALID_RECT;
    }

    @Override
    protected SVGShape clone() {

        SVGPolyLineShape polyline = new SVGPolyLineShape();
        polyline.setInfo(getInfo().clone());
        polyline.points = this.points;

        return polyline;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGPolyLineShape) {

            SVGPolyLineShape polyline = (SVGPolyLineShape) obj;
            if (points.size() == polyline.points.size()) {
                for (int i = 0; i < points.size(); i++) {
                    if (!points.get(i).equals(polyline.points.get(i)))
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
        String str = "SVGPolyline(" + getInfo().toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return str;
    }
}
