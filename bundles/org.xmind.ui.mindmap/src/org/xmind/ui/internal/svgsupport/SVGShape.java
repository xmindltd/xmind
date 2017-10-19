package org.xmind.ui.internal.svgsupport;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

@SuppressWarnings("restriction")
abstract class SVGShape implements SVGDefinition, Cloneable {

    public static final PrecisionRectangle INVALID_RECT = new PrecisionRectangle(
            0f, 0f, -1f, -1f);
    private static final int RATIO = 255; // ratio from svg to swt

    static Map<String, SVGDefinition> idRefs = new HashMap<String, SVGDefinition>();

    private SVGShapeInfoMap info;
    private PrecisionRectangle rect;
    private PathData pathData;
    private ResourceManager resourceManager;

    SVGShape() {
        this.info = new SVGShapeInfoMap();
    }

    abstract Path generatePath(Display device);

    abstract PrecisionRectangle generateRectangle();

    void parse(Element implementation, SVGShape parent) {
        info.parse(implementation, parent == null ? null : parent.info);
    }

    static SVGShape parseShape(Element ele, SVGShape parent) {
        String type = ele.getTagName();
        if (type == null)
            return null;

        SVGShape shape = SVGShapeFactory.createSVGShape(type);

        if (shape != null)
            shape.parse(ele, parent);
        return shape;
    }

    float getFloatAttribute(Element ele, String name) {
        if (ele.hasAttribute(name))
            return Float.valueOf(ele.getAttribute(name));
        return 0f;
    }

    void paintImage(Graphics graphics, Display device) {
        Path path = null;
        if (pathData == null) {
            path = generatePath(device);
            pathData = path.getPathData();
        } else
            path = new Path(device, pathData);

        if (this.rect == null || this.rect == INVALID_RECT)
            this.rect = generateRectangle();

        paint(graphics, rect, path);

        path.dispose();
    }

    private void paint(Graphics graphics, PrecisionRectangle rect, Path path) {

        // pre paint 
        boolean statePushed = prePaint(graphics);

        Color background = null;
        Color foreground = null;
        Pattern pattern = null;
        PrecisionRectangle correctRect = null;

        // check bounds 
        if (rect == INVALID_RECT) {
            float[] rectNums = new float[4];
            path.getBounds(rectNums);
            if (Util.isWindows()) {
                float[] autoScaleDown = DPIUtil.autoScaleDown(rectNums);
                rectNums[0] = autoScaleDown[0];
                rectNums[1] = autoScaleDown[1];
                rectNums[2] = autoScaleDown[2];
                rectNums[3] = autoScaleDown[3];
            }

            correctRect = new PrecisionRectangle(rectNums[0], rectNums[1],
                    rectNums[2], rectNums[3]);
        } else {
            correctRect = rect;
        }

        // paint fill 
        SVGColor co = info.getFillColor();
        if (co != null) {
            float alpha = RATIO * info.getFillOpacity();
            graphics.setFillRule(info.getFillRule()
                    .equals(SVGShapeInfoMap.FILL_RULE_NONZERO_STRING)
                            ? SWT.FILL_WINDING : SWT.FILL_EVEN_ODD);
            if (co.getLinearGradient() != null) {
                pattern = getLinearGradientPattern(co, alpha, correctRect);
                graphics.setBackgroundPattern(pattern);
            } else {
                background = resourceManager.createColor(co.getRGB());
                graphics.setBackgroundColor(background);
            }
            graphics.fillPath(path);
        }

        // paint stroke
        co = info.getStrokeColor();
        if (co != null) {
            float alpha = RATIO * info.getStrokeOpacity();
            if (co.getLinearGradient() != null) {
                PrecisionRectangle rectExpand = new PrecisionRectangle(
                        correctRect);
                rectExpand.expand(info.getLineWidth(), info.getLineWidth());
                pattern = getLinearGradientPattern(co, alpha, rectExpand);
                graphics.setForegroundPattern(pattern);
            } else {
                foreground = resourceManager.createColor(co.getRGB());
                graphics.setAlpha((int) alpha);
                graphics.setForegroundColor(foreground);
            }
            setApplyLineStyle(graphics);
            graphics.drawPath(path);
        }

        // post paint 
        postPaint(graphics, statePushed);
    }

    private Pattern getLinearGradientPattern(SVGColor co, float alpha,
            PrecisionRectangle rect) {
        LinearGradient linearGradient = (LinearGradient) co.getLinearGradient();
        SVGColor back = linearGradient.getBackGroundColor();
        SVGColor fore = linearGradient.getForeGroundColor();

        Color foreground = resourceManager.createColor(fore.getRGB());
        Color background = resourceManager.createColor(back.getRGB());

        Pattern pattern = (Pattern) resourceManager
                .create(new PatternResourceDescriptor(
                        (float) (rect.x + linearGradient.getX1() * rect.width),
                        (float) (rect.y + linearGradient.getY1() * rect.height),
                        (float) (rect.x + linearGradient.getX2() * rect.width),
                        (float) (rect.y + linearGradient.getY2() * rect.height),
                        (int) (alpha * linearGradient.getForeOpacity()),
                        foreground,
                        (int) (alpha * linearGradient.getBackOpacity()),
                        background));
        return pattern;
    }

    boolean prePaint(Graphics graphics) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Exception e) {
            statePushed = false;
        }

        if (haveTransformProperty()) {
            //FIXME  apply transform matrix : transform="matrix(m1 m2 m3 m4 m5 m6)"
            for (TransformElement element : this.info.getTransform()
                    .getList()) {
                element.transform(graphics);
            }
        }

        return statePushed;
    }

    private boolean haveTransformProperty() {
        if (info != null && info.getTransform() != null) {
            SVGTransform transform = info.getTransform();
            if (transform.getList() != null)
                return true;
        }
        return false;
    }

    private void setApplyLineStyle(Graphics graphics) {

        LineAttributes lineAttribute = new LineAttributes(
                this.info.getLineWidth());

        lineAttribute.style = this.info.getLineStyle();
        lineAttribute.dash = info.getLineDash();
        lineAttribute.dashOffset = this.info.getLineDashOffset();
        lineAttribute.join = this.info.getLineJoin();
        lineAttribute.miterLimit = this.info.getLineMiterLimit();

        graphics.setLineAttributes(lineAttribute);
    }

    void postPaint(Graphics graphics, boolean statePushed) {
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    @Override
    protected SVGShape clone() {
        return this.clone();
    }

    void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    ResourceManager getResourceManager() {
        return resourceManager;
    }

    public SVGShapeInfoMap getInfo() {
        return info;
    }

    public void setInfo(SVGShapeInfoMap info) {
        this.info = info;
    }

}
