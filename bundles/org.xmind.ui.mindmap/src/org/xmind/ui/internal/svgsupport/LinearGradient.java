package org.xmind.ui.internal.svgsupport;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Enki Xiong
 */
class LinearGradient implements SVGDefinition {

    private static class Stop {
        private SVGColor color;
        //FIXME to handle 3 stops and offset in stop
        private float offset;
        private float opacity = 1.0f;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (obj instanceof Stop) {
                Stop stop = (Stop) obj;
                return color.equals(stop.color) && (opacity == stop.opacity)
                        && (offset == stop.offset);
            }

            return false;
        }

        public SVGColor getColor() {
            return color;
        }

        public void setColor(SVGColor color) {
            this.color = color;
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }

        public float getOpacity() {
            return opacity;
        }

        public void setOpacity(float opacity) {
            this.opacity = opacity;
        }

    }

    private static final SVGColor FOREGROUND = new SVGColor(0xffff);
    private static final SVGColor BACKGROUND = new SVGColor(0);
    private static final float DEFAULT_OPACITY = 1.0f;
    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private List<Stop> stops;

    protected static LinearGradient parseLinearGradient(Element ele) {
        //FIXME handle the situation : x1,y1,x2,y2 is precision value
        LinearGradient linear = new LinearGradient();
        linear.x1 = Float
                .valueOf((ele.getAttribute(SVGDefinitionConstants.X1)
                        .split(SVGDefinitionConstants.HUNDRED_PERCENT)[0]))
                / 100;
        linear.x2 = Float
                .valueOf((ele.getAttribute(SVGDefinitionConstants.X2)
                        .split(SVGDefinitionConstants.HUNDRED_PERCENT)[0]))
                / 100;
        linear.y1 = Float
                .valueOf((ele.getAttribute(SVGDefinitionConstants.Y1)
                        .split(SVGDefinitionConstants.HUNDRED_PERCENT)[0]))
                / 100;
        linear.y2 = Float
                .valueOf((ele.getAttribute(SVGDefinitionConstants.Y2)
                        .split(SVGDefinitionConstants.HUNDRED_PERCENT)[0]))
                / 100;

        NodeList list = ele
                .getElementsByTagName(SVGDefinitionConstants.TAG_STOP);
        List<Stop> stops = new ArrayList<Stop>();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                stops.add(parseStop((Element) node));
            }
        }
        linear.setStops(stops);
        return linear;
    }

    private static Stop parseStop(Element ele) {
        Stop stop = new Stop();
        String co = ele.getAttribute(SVGDefinitionConstants.STOP_COLOR);
        stop.setColor(ColorRecognizer.recognizeColor(co, null));
        String opacity = ele.getAttribute(SVGDefinitionConstants.STOP_OPACITY);
        if (opacity != null && !opacity.equals("")) //$NON-NLS-1$
            stop.setOpacity(Float.valueOf(opacity));
        stop.setOffset(
                Float.parseFloat(ele.getAttribute(SVGDefinitionConstants.OFFSET)
                        .split(SVGDefinitionConstants.HUNDRED_PERCENT)[0]));
        return stop;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof LinearGradient) {
            LinearGradient linear = (LinearGradient) obj;
            if (getStops().size() == linear.getStops().size()) {
                for (int i = 0; i < getStops().size(); i++) {
                    if (!getStops().get(i).equals(linear.getStops().get(i)))
                        return false;
                }
                return x1 == linear.x1 && y1 == linear.y1 //
                        && x2 == linear.x2 && y2 == linear.y2;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Linear Gradient Color (" + getForeGroundColor().toString() //$NON-NLS-1$
                + "-->" //$NON-NLS-1$
                + getBackGroundColor().toString() + ")"; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + Float.floatToIntBits(x1);
        result = result * 31 + Float.floatToIntBits(y1);
        result = result * 31 + Float.floatToIntBits(x2);
        result = result * 31 + Float.floatToIntBits(y2);

        result = result * 31 + getForeGroundColor().hashCode();
        result = result * 31 + Float.floatToIntBits(getForeOpacity());
        result = result * 31 + getBackGroundColor().hashCode();
        result = result * 31 + Float.floatToIntBits(getBackOpacity());

        return result;
    }

    public SVGColor getForeGroundColor() {
        if (isValidLinearGradient()) {
            SVGColor foreground = getStops().get(0).getColor();
            if (foreground != null)
                return foreground;
        }
        return FOREGROUND;
    }

    public float getForeOpacity() {
        if (isValidLinearGradient())
            return getStops().get(0).getOpacity();
        else
            return DEFAULT_OPACITY;
    }

    public SVGColor getBackGroundColor() {
        if (isValidLinearGradient()) {
            SVGColor foreground = getStops().get(0).getColor();
            if (foreground != null)
                return foreground;
        }
        return BACKGROUND;
    }

    public float getBackOpacity() {
        if (isValidLinearGradient())
            return getStops().get(1).getOpacity();
        else
            return DEFAULT_OPACITY;
    }

    public float getX1() {
        return x1;
    }

    public float getX2() {
        return x2;
    }

    public float getY1() {
        return y1;
    }

    public float getY2() {
        return y2;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public List<Stop> getStops() {
        return stops;
    }

    private boolean isValidLinearGradient() {
        if (stops == null || stops.size() < 2)
            return false;
        return true;
    }

}
