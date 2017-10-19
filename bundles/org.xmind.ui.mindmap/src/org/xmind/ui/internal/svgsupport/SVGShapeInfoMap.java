package org.xmind.ui.internal.svgsupport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.w3c.dom.Element;

public class SVGShapeInfoMap implements Cloneable {
    private static final float DEFAULT_STROKE_LINEJOIN_MITER = 4.0f;
    private static final float DEFAULT_STROKE_DASHOFFSET = 0f;
    public static final int DEFAULT_STROKE_LINEJOIN = SWT.JOIN_MITER;
    private static final float INVALID_FLOAT = -1f;
    private static final float MAX_OPACITY = 1f;
    private static final float MIN_OPACITY = 0f;
    public static final String ID = "id"; //$NON-NLS-1$
    public static final String FILL = "fill"; //$NON-NLS-1$
    public static final String FILL_RULE = "fill-rule"; //$NON-NLS-1$
    public static final String FILL_OPACITY = "fill-opacity"; //$NON-NLS-1$
    public static final String STROKE = "stroke"; //$NON-NLS-1$
    public static final String STROKE_WIDTH = "stroke-width"; //$NON-NLS-1$
    public static final String STROKE_OPACITY = "stroke-opacity"; //$NON-NLS-1$

    public static final String STROKE_LINESTYLE = "line-style"; //$NON-NLS-1$
    public static final String STROKE_LINECAP = "stroke-linecap"; //$NON-NLS-1$
//   line cap: butt | round | square
    public static final String STROKE_LINECAP_BUTT = "butt"; //$NON-NLS-1$
    public static final String STROKE_LINECAP_ROUND = "round"; //$NON-NLS-1$
    public static final String STROKE_LINECAP_SQUARE = "square"; //$NON-NLS-1$
    public static final String STROKE_DASHARRAY = "stroke-dasharray"; //$NON-NLS-1$
    public static final String STROKE_LINEJOIN = "stroke-linejoin"; //$NON-NLS-1$
//  line join :  miter | round | bevel
    public static final String STROKE_LINEJOIN_MITER = "miter"; //$NON-NLS-1$
    public static final String STROKE_LINEJOIN_ROUND = "round"; //$NON-NLS-1$
    public static final String STROKE_LINEJOIN_BEVEL = "bevel"; //$NON-NLS-1$
    public static final String STROKE_MITERLIMIT = "stroke-miterlimit"; //$NON-NLS-1$
    public static final String STROKE_DASHOFFSET = "stroke-dashoffset"; //$NON-NLS-1$

    public static final String FILL_RULE_EVENODD_STRING = "evenodd"; //$NON-NLS-1$
    public static final String FILL_RULE_NONZERO_STRING = "nonzero"; //$NON-NLS-1$

    public static final String TRANSFORM = "transform"; //$NON-NLS-1$
    private static final String NONE = "none"; //$NON-NLS-1$

    private Map<String, Object> map = new HashMap<String, Object>();

    private int idCount = 0;

    void parse(Element implementation, SVGShapeInfoMap infoMap) {
        if (infoMap != null) {
            map.putAll(infoMap.map);
            map.remove(TRANSFORM);
        }

        if (implementation.hasAttribute(ID))
            putId(implementation.getAttribute(ID));
        else
            putId(generateRandomId());
        parseStyles(implementation);
    }

    private synchronized String generateRandomId() {
        return "shapeId-" + (idCount++); //$NON-NLS-1$
    }

    private void parseStyles(Element implementation) {

        //FIXME don't handle the style format : style(a:b,c:d)

        parseFillStyle(implementation);
        parseStrokeStyle(implementation);
        parseLineAttributes(implementation);
        parseTransformStyle(implementation);

    }

    private void parseFillStyle(Element implementation) {

        String fill = implementation.getAttribute(FILL);

        if (implementation.hasAttribute(FILL) && !fill.equals(NONE)) {
            SVGColor color = ColorRecognizer.recognizeColor(fill,
                    SVGShape.idRefs);
            if (color != null)
                putFillColor(color);
        }

        if (implementation.hasAttribute(FILL_RULE))
            putFillRule(implementation.getAttribute(FILL_RULE));

        float opacity = getFloatAttribute(implementation, FILL_OPACITY);
        if (opacity != INVALID_FLOAT)
            putFillOpacity(opacity * getFillOpacity());

    }

    private void parseStrokeStyle(Element implementation) {
        String stroke = implementation.getAttribute(STROKE).trim();
        if (implementation.hasAttribute(STROKE) || !stroke.equals(NONE)) {
            SVGColor color = ColorRecognizer.recognizeColor(stroke,
                    SVGShape.idRefs);
            if (color != null)
                putStrokeColor(color);
        }

        float width = getFloatAttribute(implementation, STROKE_WIDTH);
        if (0 <= width)
            putLineWidth(width);

        float opacity = getFloatAttribute(implementation, STROKE_OPACITY);
        if (MIN_OPACITY <= opacity && MAX_OPACITY >= opacity)
            putStrokeOpacity(opacity * getStrokeOpacity());

    }

    private void parseLineAttributes(Element implementation) {
        if (implementation.hasAttribute(STROKE_LINECAP)) {
            String lineCap = implementation.getAttribute(STROKE_LINECAP);
            if (lineCap.equals(STROKE_LINECAP_ROUND)) {
                putLineCap(SWT.CAP_ROUND);
            } else if (lineCap.equals(STROKE_LINECAP_SQUARE)) {
                putLineCap(SWT.CAP_SQUARE);
            }
        }

        if (implementation.hasAttribute(STROKE_DASHARRAY)) {
            String dashArray = implementation.getAttribute(STROKE_DASHARRAY)
                    .trim();
            String[] strs = dashArray.split("[\\,|\\s]+"); //$NON-NLS-1$
            float[] dashArr = new float[strs.length];
            for (int i = 0; i < strs.length; i++)
                dashArr[i] = Float.valueOf(strs[i]);

            putLineDash(dashArr);
        }

        if (implementation.hasAttribute(STROKE_DASHOFFSET)) {
            String dashOffset = implementation.getAttribute(STROKE_DASHOFFSET);

            putLineDashOffSET(Float.valueOf(dashOffset));
        }

        if (implementation.hasAttribute(STROKE_LINEJOIN)) {
            String lineJoin = implementation.getAttribute(STROKE_LINEJOIN);
            if (lineJoin.equals(STROKE_LINEJOIN_ROUND))
                putLineJoin(SWT.JOIN_ROUND);
            else if (lineJoin.equals(STROKE_LINEJOIN_BEVEL))
                putLineJoin(SWT.JOIN_BEVEL);
        }

        if (implementation.hasAttribute(STROKE_MITERLIMIT)) {
            String miterLimit = implementation.getAttribute(STROKE_MITERLIMIT);
            putLineMiterLimit(Float.valueOf(miterLimit));
        }

    }

    private void parseTransformStyle(Element implementation) {
        SVGTransform svgTransform = new SVGTransform();
        if (implementation.hasAttribute(TRANSFORM)) {
            String transform = implementation.getAttribute(TRANSFORM).trim();
            svgTransform.parseTransform(transform);
            putTransform(svgTransform);
        }

    }

    float getFloatAttribute(Element ele, String name) {
        if (ele.hasAttribute(name))
            return Float.valueOf(ele.getAttribute(name));
        return INVALID_FLOAT;
    }

    @Override
    protected SVGShapeInfoMap clone() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.putAll(map);
        SVGShapeInfoMap infoMap = new SVGShapeInfoMap();
        infoMap.map = map;
        return infoMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SVGShapeInfoMap && getId() != null) {
            return getId().equals(((SVGShapeInfoMap) obj).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return " style:id=" + getId() + " "; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void add(SVGShapeInfoMap info) {
        for (String key : info.map.keySet()) {
            if (!map.containsKey(key) && !key.equals(TRANSFORM)) {
                map.put(key, info.map.get(key));
            } else {
                if (key.equals(FILL_OPACITY))
                    map.put(FILL_OPACITY,
                            getFillOpacity() * info.getFillOpacity());
                if (key.equals(STROKE_OPACITY))
                    map.put(STROKE_OPACITY,
                            getStrokeOpacity() * info.getStrokeOpacity());
            }
        }
    }

    public String getId() {
        return (String) map.get(ID);
    }

    public void putId(String id) {
        map.put(ID, id);
    }

    public SVGColor getFillColor() {
        return (SVGColor) map.get(FILL);
    }

    public void putFillColor(SVGColor fillColor) {
        map.put(FILL, fillColor);
    }

    public String getFillRule() {
        Object obj = map.get(FILL_RULE);
        if (obj == null)
            return FILL_RULE_NONZERO_STRING;

        return (String) obj;
    }

    public void putFillRule(String fillRule) {
        map.put(FILL_RULE, fillRule);
    }

    public float getFillOpacity() {
        Object obj = map.get(FILL_OPACITY);
        float opacity = MAX_OPACITY;
        if (obj != null)
            opacity = (Float) obj;

        return opacity;
    }

    public void putFillOpacity(float fillOpacity) {
        map.put(FILL_OPACITY, fillOpacity);
    }

    public SVGColor getStrokeColor() {
        return (SVGColor) map.get(STROKE);
    }

    public void putStrokeColor(SVGColor strokeColor) {
        map.put(STROKE, strokeColor);
    }

    public float getLineWidth() {
        Object obj = map.get(STROKE_WIDTH);
        float lineWidth = 1.0f;
        if (obj != null)
            lineWidth = (Float) obj;

        return lineWidth;
    }

    public void putLineWidth(float lineWidth) {
        map.put(STROKE_WIDTH, lineWidth);
    }

    public float getStrokeOpacity() {
        Object obj = map.get(STROKE_OPACITY);
        float opacity = MAX_OPACITY;
        if (obj != null)
            opacity = (Float) obj;

        return opacity;
    }

    public void putStrokeOpacity(float strokeOpacity) {
        map.put(STROKE_OPACITY, strokeOpacity);
    }

    public int getLineStyle() {
        Object obj = map.get(STROKE_LINESTYLE);
        if (obj == null)
            return SWT.LINE_CUSTOM;
        else
            return (Integer) obj;
    }

    public void putLineStyle(int style) {
        map.put(STROKE_LINESTYLE, style);
    }

    public int getLineCap() {
        Object obj = map.get(STROKE_LINECAP);
        if (obj == null)
            return SWT.CAP_FLAT;
        else
            return (Integer) obj;
    }

    public void putLineCap(int lineCap) {
        map.put(STROKE_LINECAP, lineCap);
    }

    public float[] getLineDash() {
        if (map.get(STROKE_DASHARRAY) == null)
            return null;
        else
            return (float[]) map.get(STROKE_DASHARRAY);
    }

    public void putLineDash(float[] lineDash) {
        map.put(STROKE_DASHARRAY, lineDash);
    }

    public int getLineJoin() {
        Object obj = map.get(STROKE_LINEJOIN);
        if (obj == null)
            return DEFAULT_STROKE_LINEJOIN;
        else
            return (Integer) obj;
    }

    public void putLineJoin(int lineJoin) {
        map.put(STROKE_LINEJOIN, lineJoin);
    }

    public float getLineMiterLimit() {
        Object obj = map.get(STROKE_LINEJOIN_MITER);
        if (obj == null)
            return DEFAULT_STROKE_LINEJOIN_MITER;
        else
            return (Float) obj;
    }

    public void putLineMiterLimit(float lineMiterLimit) {
        map.put(STROKE_LINEJOIN_MITER, lineMiterLimit);
    }

    public float getLineDashOffset() {
        Object obj = map.get(STROKE_DASHOFFSET);
        if (obj == null)
            return DEFAULT_STROKE_DASHOFFSET;
        else
            return (Integer) obj;
    }

    public void putLineDashOffSET(float lineDashOffput) {
        map.put(STROKE_DASHOFFSET, lineDashOffput);
    }

    public SVGTransform getTransform() {
        return (SVGTransform) map.get(TRANSFORM);
    }

    public void putTransform(SVGTransform transform) {
        map.put(TRANSFORM, transform);
    }

}
