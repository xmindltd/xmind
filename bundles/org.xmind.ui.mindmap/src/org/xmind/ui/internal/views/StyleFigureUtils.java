/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.views;

import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionPointPair;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.GradientPattern;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.internal.svgsupport.SvgFileLoader;
import org.xmind.ui.internal.svgsupport.SvgPathParser;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class StyleFigureUtils {

    public static final int BOUNDARY_STEP = 16;
    public static final int BOUNDARY_PADDING = 8;
    public static final float CALLOUT_RRECT_PARAM = 0.2f;
    public static final float CALLOUT_ELLIPSE_STARTANGLE = -130;
    public static final float CALLOUT_ELLIPSE_ARCANGLE = 345;
    public static final int SPINY_WIDTH = 2;
    public static final int ROUNDED_CORNER = 7;
    public static final int ROUNDED_CORNER_ADAPTER = 30;

    private static final IStyleSheet defaultStyles = MindMapUI
            .getResourceManager().getDefaultStyleSheet();

    public static final IStyle defaultSheetStyle = findOrCreateDefaultStyle(
            Styles.FAMILY_MAP, IStyle.MAP);

    public static final IStyle defaultCentralStyle = findOrCreateDefaultStyle(
            Styles.FAMILY_CENTRAL_TOPIC, IStyle.TOPIC);

    public static final IStyle defaultMainStyle = findOrCreateDefaultStyle(
            Styles.FAMILY_MAIN_TOPIC, IStyle.TOPIC);

    public static final IStyle defaultRelationshipStyle = findOrCreateDefaultStyle(
            Styles.FAMILY_RELATIONSHIP, IStyle.RELATIONSHIP);

    private StyleFigureUtils() {
    }

    private static IStyle findOrCreateDefaultStyle(String family, String type) {
        IStyle style = defaultStyles.findStyle(family);
        if (style == null)
            style = defaultStyles.createStyle(type);
        return style;
    }

    public static void angledRel(Path shape, Rectangle relBounds, Point c1,
            Point c2) {
        int dx = relBounds.width / 8;
        int dy = relBounds.height / 8;
        shape.moveTo(relBounds.getBottomLeft());
        c1.setLocation(relBounds.getCenter().translate(-dx, -dy));
        shape.lineTo(c1);
        c2.setLocation(relBounds.getCenter().translate(dx, dy));
        shape.lineTo(c2);
        shape.lineTo(relBounds.getTopRight());
    }

    public static void calloutEllipse(Path shape, Rectangle r) {
        Rectangle outlineBox = r;
        shape.addArc(outlineBox.x, outlineBox.y, outlineBox.width,
                outlineBox.height, CALLOUT_ELLIPSE_STARTANGLE,
                CALLOUT_ELLIPSE_ARCANGLE);
        float h = outlineBox.height;
        shape.lineTo(outlineBox.x, outlineBox.y + h);
        shape.close();
    }

    public static void calloutRoundRect(Path shape, Rectangle r) {
        Rectangle box = r;
        float x = box.x;
        float y = box.y;
        float w = box.width;
        float h = box.height;

        float dy = h - box.height / 4.0f;
        float c = getAppliedCorner(r);
        shape.moveTo(x + w * CALLOUT_RRECT_PARAM, y + dy);
        shape.lineTo(x + w - c, y + dy);
        shape.addArc(x + w - c, y + dy - c, c, c, -90, 90);
        shape.lineTo(x + w, y + c);
        shape.addArc(x + w - c, y, c, c, 0, 90);
        shape.lineTo(x + c, y);
        shape.addArc(x, y, c, c, 90, 90);
        shape.lineTo(x, y + dy - c);
        shape.addArc(x, y + dy - c, c, c, 180, 90);
        shape.lineTo(box.x, box.y + h);
        shape.close();
    }

    protected static int getAppliedCorner(Rectangle r) {
        int t = Math.min(r.height, r.width);
        return ROUNDED_CORNER * t / ROUNDED_CORNER_ADAPTER;
    }

    public static void circle(Path shape, Rectangle r) {
        shape.addArc(r, 0, 360);
    }

    public static void parallelogram(Path shape, Rectangle r) {
        shape.moveTo(r.x + r.height * 0.5f, r.y);
        shape.lineTo(r.getBottomLeft());
        shape.lineTo(r.right() - r.height * 0.5f, r.bottom());
        shape.lineTo(r.getTopRight());
        shape.close();
    }

    public static void cloud(Path path, Rectangle r) {
        URL url = BundleUtility.find("org.xmind.ui", //$NON-NLS-1$
                "shapes/topic-shape-cloud.svg"); //$NON-NLS-1$

        SvgFileLoader loader = SvgFileLoader.getInstance();
        String svgPath = loader.loadSvgFile(url);

        SvgPathParser parser = SvgPathParser.getInstance();

        parser.parseSvgPath(path, r.getCenter().x, r.getCenter().y, r.width,
                r.height, svgPath);
    }

    public static void strokeCircle(Path path, Rectangle r) {
        URL url = BundleUtility.find("org.xmind.ui", //$NON-NLS-1$
                "shapes/topic-shape-stroke-circle.svg"); //$NON-NLS-1$

        SvgFileLoader loader = SvgFileLoader.getInstance();
        String svgPath = loader.loadSvgFile(url);

        SvgPathParser parser = SvgPathParser.getInstance();

        parser.parseSvgPath(path, r.getCenter().x, r.getCenter().y, r.width,
                r.height, svgPath);
    }

    public static void curvedRel(Path shape, Rectangle relBounds, Point c1,
            Point c2) {
//        int dx = -relBounds.width / 10;
//        int dy = relBounds.height / 10;
//        shape.moveTo(relBounds.getBottomLeft());
        int dx = -relBounds.width / 4;
        int dy = relBounds.height / 4;
        shape.moveTo(relBounds.getTopLeft());
        Point p1 = relBounds.getTop().translate(-dx, -dy);
        Point p2 = relBounds.getBottom().translate(dx, dy);
        shape.cubicTo(p1, p2, relBounds.getTopRight());
        c1.setLocation(p1.translate(0, dy));
        c2.setLocation(p2.translate(0, -dy));
    }

    public static void diamondTopic(Path shape, Rectangle r) {
        Rectangle r2 = r;
        shape.moveTo(r2.getLeft());
        shape.lineTo(r2.getBottom());
        shape.lineTo(r2.getRight());
        shape.lineTo(r2.getTop());
        shape.close();
    }

    public static void diamondArrow(Path shape, Point head, double angle,
            int lineWidth) {
        int side1 = lineWidth + 3;
        int side2 = lineWidth + 2;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle, side1);
        PrecisionPoint p2 = p.getMoved(angle - Math.PI / 2, side2);
        PrecisionPoint p3 = p.getMoved(angle + Math.PI, side1);
        PrecisionPoint p4 = p.getMoved(angle + Math.PI / 2, side2);
        shape.moveTo(p1);
        shape.lineTo(p2);
        shape.lineTo(p3);
        shape.lineTo(p4);
        shape.close();
    }

    public static void dotArrow(Path shape, Point head, double angle,
            int lineWidth) {
        PrecisionRectangle bounds = new PrecisionRectangle(head.x, head.y, 0, 0)
                .expand(lineWidth, lineWidth);
        shape.addArc(bounds, 0, 360);
    }

    public static void drawArrow(Graphics graphics, String arrowValue,
            Point head, Point tail, int lineWidth) {
        Path shape = new Path(Display.getCurrent());
        boolean fill = true;
        double angle = new PrecisionPoint(tail)
                .getAngle(new PrecisionPoint(head));

        if (Styles.ARROW_SHAPE_DIAMOND.equals(arrowValue)) {
            diamondArrow(shape, head, angle, lineWidth);
        } else if (Styles.ARROW_SHAPE_DOT.equals(arrowValue)) {
            dotArrow(shape, head, angle, lineWidth);
        } else if (Styles.ARROW_SHAPE_HERRINGBONE.equals(arrowValue)) {
            herringBone(shape, head, angle, lineWidth);
            fill = false;
        } else if (Styles.ARROW_SHAPE_SPEARHEAD.equals(arrowValue)) {
            spearhead(shape, head, angle, lineWidth);
        } else if (Styles.ARROW_SHAPE_SQUARE.equals(arrowValue)) {
            square(shape, head, angle, lineWidth);
        } else if (Styles.ARROW_SHAPE_TRIANGLE.equals(arrowValue)) {
            triangle(shape, head, angle, lineWidth);
        } else {
            normalArrow(shape, head, angle, lineWidth);
            fill = false;
        }

        Color fgColor = graphics.getForegroundColor();
        if (fgColor != null) {
            if (fill) {
                graphics.setBackgroundColor(fgColor);
                graphics.fillPath(shape);
            }
            graphics.drawPath(shape);
        }
        shape.dispose();
    }

    public static void drawBoundary(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template) {
        drawBoundary(graphics, bounds, null, style, template);
    }

    public static void drawBoundary(Graphics graphics, Rectangle bounds,
            HashMap<String, String> existedStyle, IStyle style,
            IStyle template) {
        Rectangle boundaryBounds = boundaryBounds(bounds);
        Path shape = new Path(Display.getCurrent());
        String shapeValue = getValue(existedStyle, Styles.ShapeClass, style,
                template);
        if (shapeValue == null
                || Styles.BOUNDARY_SHAPE_ROUNDEDRECT.equals(shapeValue)) {
            roundedRect(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_RECT.equals(shapeValue)) {
            rectangle(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_SCALLOPS.equals(shapeValue)) {
            scallops(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_TENSION.equals(shapeValue)) {
            tension(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_WAVES.equals(shapeValue)) {
            waves(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_POLYGON.equals(shapeValue)) {
            polygon(shape, boundaryBounds);
        } else if (Styles.BOUNDARY_SHAPE_ROUNDEDPOLYGON.equals(shapeValue)) {
            roundedPolygon(shape, boundaryBounds);
        } else {
            roundedRect(shape, boundaryBounds);
        }
        String fillColorValue = getValue(existedStyle, Styles.FillColor, style,
                template);

        if (fillColorValue != null) {
            Color fillColor = ColorUtils.getColor(fillColorValue);
            if (fillColor != null) {
                String opacityValue = getValue(existedStyle, Styles.Opacity,
                        style, template);
                double opacity = NumberUtils.safeParseDouble(opacityValue, 1);
                int alpha = (int) (opacity * 0xff);
                graphics.setAlpha(alpha);
                graphics.setBackgroundColor(fillColor);
                graphics.fillPath(shape);
            }
        }

        Color lineColor = getLineColor(existedStyle, style, template,
                ColorConstants.gray);
        String lineWidthValue = getValue(existedStyle, Styles.LineWidth, style,
                template);
        lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
        int lineWidth = NumberUtils.safeParseInt(lineWidthValue, 3);
        graphics.setLineWidth(lineWidth);

        String linePatternValue = getValue(existedStyle, Styles.LinePattern,
                style, template);
        int linePattern = StyleUtils.toSWTLineStyle(linePatternValue,
                SWT.LINE_DASH);
        graphics.setLineStyle(linePattern);
        graphics.setAlpha(0xff);
        graphics.setForegroundColor(lineColor);
        graphics.drawPath(shape);

        shape.dispose();
    }

    public static void drawMainBranches(Graphics graphics, Rectangle bounds,
            boolean spiny, boolean rainbow) {
        PrecisionPoint center = new PrecisionPoint(bounds.getCenter());
        double length = Math.min(bounds.width, bounds.height) / 3;
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * (i - 1) / 3;
            PrecisionPoint p = center.getMoved(angle, length);
            if (p.y < center.y)
                p.y += (center.y - p.y) / 6;
            else if (p.y > center.y)
                p.y -= (p.y - center.y) / 6;
            if (Math.abs(p.y - center.y) > 0.000001) {
                if (p.x < center.x)
                    p.x -= (center.x - p.x) / 6;
                else if (p.y > center.x)
                    p.x += (p.x - center.x) / 6;
            }
            Color c = rainbow ? ColorUtils.getRainbowColor(i, 6)
                    : ColorConstants.gray;
            graphics.setAlpha(0xff);
            graphics.setForegroundColor(c);
            graphics.setLineWidth(1);
            graphics.setLineStyle(SWT.LINE_SOLID);
            if (spiny) {
                PrecisionPoint c1 = center.getMoved(angle + Math.PI / 3,
                        SPINY_WIDTH);
                PrecisionPoint c2 = center.getMoved(angle - Math.PI / 3,
                        SPINY_WIDTH);
                Path shape = new Path(Display.getCurrent());
                shape.moveTo(p);
                shape.lineTo(c1);
                shape.lineTo(c2);
                shape.close();
                graphics.setBackgroundColor(c);
                graphics.fillPath(shape);
                graphics.drawPath(shape);
                shape.dispose();
            } else {
                graphics.drawLine(center.toDraw2DPoint(), p.toDraw2DPoint());
            }
            graphics.setBackgroundColor(ColorConstants.white);
            Rectangle oval = new PrecisionRectangle(center, center)
                    .getExpanded(4, 3).toDraw2DRectangle();
            graphics.fillOval(oval);
        }
    }

    public static void drawSheetBackground(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template) {
        drawSheetBackground(graphics, bounds, null, style, template, true);
    }

    public static void drawSheetBackground(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template, boolean withMainBranches) {
        drawSheetBackground(graphics, bounds, null, style, template,
                withMainBranches);
    }

    public static void drawSheetBackground(Graphics graphics, Rectangle bounds,
            HashMap<String, String> existedStyle, IStyle style, IStyle template,
            boolean withMainBranches) {
        Rectangle sheetBounds = sheetBounds(bounds);
        Color fillColor = null;
        String fillColorValue = getValue(existedStyle, Styles.FillColor, style,
                template);
        if (fillColorValue != null)
            fillColor = ColorUtils.getColor(fillColorValue);
        if (fillColor != null) {
            graphics.setAlpha(0xff);
            graphics.setBackgroundColor(fillColor);
            graphics.fillRectangle(sheetBounds);

        }
//        if (fillColor == null)
//            fillColor = ColorUtils.getColor("#e0e0e0"); //$NON-NLS-1$

//        if (withMainBranches) {
//            String spinyValue = getValue(Styles.SPINY_LINES, style, template);
//            String rainbowValue = getValue(Styles.RAINBOWCOLOR, style, template);
//            boolean spiny = Boolean.parseBoolean(spinyValue);
//            boolean rainbow = Boolean.parseBoolean(rainbowValue);
//            if (spiny || rainbow) {
//                drawMainBranches(graphics, bounds, spiny, rainbow);
//            }
//        }
    }

    public static void drawRelationship(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template) {
        drawRelationship(graphics, bounds, null, style, template);
    }

    public static void drawRelationship(Graphics graphics, Rectangle bounds,
            HashMap<String, String> existedStyle, IStyle style,
            IStyle template) {
        Rectangle relBounds = relBounds(bounds);
        Path shape = new Path(Display.getCurrent());

        Point c1 = new Point();
        Point c2 = new Point();

        String shapeValue = getValue(existedStyle, Styles.ShapeClass, style,
                template);
        if (Styles.REL_SHAPE_ANGLED.equals(shapeValue)) {
            angledRel(shape, relBounds, c1, c2);
        } else if (Styles.REL_SHAPE_STRAIGHT.equals(shapeValue)) {
            straightRel(shape, relBounds, c1, c2);
        } else {
            curvedRel(shape, relBounds, c1, c2);
        }

        Color lineColor = getLineColor(existedStyle, style, template,
                ColorConstants.gray);

        String lineWidthValue = getValue(existedStyle, Styles.LineWidth, style,
                template);
        lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
        int lineWidth = NumberUtils.safeParseInt(lineWidthValue, 3);
        graphics.setLineWidth(lineWidth);

        String linePatternValue = getValue(existedStyle, Styles.LinePattern,
                style, template);
        int linePattern = StyleUtils.toSWTLineStyle(linePatternValue,
                SWT.LINE_DOT);
        graphics.setLineStyle(linePattern);
        graphics.setAlpha(0xff);
        graphics.setForegroundColor(lineColor);
        graphics.drawPath(shape);
        shape.dispose();

        graphics.setLineStyle(SWT.LINE_SOLID);
        String beginArrowValue = getValue(existedStyle, Styles.ArrowBeginClass,
                style, template);
        if (beginArrowValue != null
                && !Styles.ARROW_SHAPE_NONE.equals(beginArrowValue)) {
            drawArrow(graphics, beginArrowValue, relBounds.getBottomLeft(), c1,
                    lineWidth);
        }

        String endArrowValue = getValue(existedStyle, Styles.ArrowEndClass,
                style, template);
        if (endArrowValue == null)
            endArrowValue = Styles.ARROW_SHAPE_NORMAL;
        if (!Styles.ARROW_SHAPE_NONE.equals(endArrowValue)) {
            drawArrow(graphics, endArrowValue, relBounds.getTopRight(), c2,
                    lineWidth);
        }

    }

    public static Color getBranchConnectionColor(IStyle style, IStyle template,
            IStyle parentStyle, IStyle parentTemplate, int preferredIndex,
            Color defaultLineColor) {
        Color lineColor = null;
        if (preferredIndex >= 0 && parentStyle != null) {
            String multiColors = getValue(Styles.MultiLineColors, parentStyle,
                    parentTemplate);
            if (multiColors == null)
                multiColors = template.getProperty(Styles.MultiLineColors);
            if (multiColors != null) {
                multiColors = multiColors.trim();
                String[] colors = multiColors.split("[\\s]+"); //$NON-NLS-1$
                if (colors.length > 0) {
                    preferredIndex %= colors.length;
                    String color = colors[preferredIndex].trim();
                    lineColor = ColorUtils.getColor(color);
                }
            }
        }
        if (lineColor == null) {
            lineColor = getLineColor(style, template, defaultLineColor);
        }
        return lineColor;
    }

    public static Color getLineColor(IStyle style, IStyle template,
            Color defaultLineColor) {
        return getLineColor(null, style, template, defaultLineColor);
    }

    public static Color getLineColor(HashMap<String, String> existedStyle,
            IStyle style, IStyle template, Color defaultLineColor) {
        Color lineColor = null;
        String lineColorValue = getValue(existedStyle, Styles.LineColor, style,
                template);
        if (lineColorValue != null)
            lineColor = ColorUtils.getColor(lineColorValue);
        if (lineColor == null) {
            lineColor = defaultLineColor;
        }
        return lineColor;
    }

    public static String getValue(String key, IStyle style, IStyle template) {
        return getValue(null, key, style, template);
    }

    public static String getValue(HashMap<String, String> existedStyle,
            String key, IStyle style, IStyle template) {
        String value = existedStyle == null ? null : existedStyle.get(key);
        value = style != null ? style.getProperty(key, value) : value;
        if (value == null) {
            value = template == null ? null : template.getProperty(key);
        }
        return value;
    }

    public static void drawLine(Graphics g, Rectangle srcBounds,
            IStyle srcStyle, IStyle srcTemplate, boolean srcCenterUnderline,
            Rectangle tgtBounds, IStyle tgtStyle, IStyle tgtTemplate,
            boolean tgtCenterUnderline, boolean tapered) {
        String line = getValue(Styles.LineClass, srcStyle, srcTemplate);
        if (Styles.BRANCH_CONN_NONE.equals(line))
            return;

        String lineWidth = getValue(Styles.LineWidth, srcStyle, srcTemplate);
        lineWidth = StyleUtils.trimNumber(lineWidth);
        int width = NumberUtils.safeParseInt(lineWidth, 1);
        srcBounds = srcBounds.getExpanded(-width / 2, -width / 2);
        int tgtWidth = NumberUtils.safeParseInt(StyleUtils.trimNumber(
                getValue(Styles.LineWidth, tgtStyle, tgtTemplate)), 1);
        tgtBounds = tgtBounds.getExpanded(-tgtWidth / 2, -tgtWidth / 2);

        String srcShape = getValue(Styles.ShapeClass, srcStyle, srcTemplate);
        String tgtShape = getValue(Styles.ShapeClass, tgtStyle, tgtTemplate);
        Point srcPos = getSourcePos(srcBounds, srcShape, tgtBounds, tgtShape,
                srcCenterUnderline);
        Point tgtPos = getTargetPos(tgtBounds, tgtShape, srcBounds, srcShape,
                tgtCenterUnderline);

        Path shape = new Path(Display.getCurrent());
        if (Styles.BRANCH_CONN_ELBOW.equals(line)) {
            elbow(shape, srcPos, tgtPos, tapered, width);
        } else if (Styles.BRANCH_CONN_ROUNDEDELBOW.equals(line)) {
            roundElbow(shape, srcPos, tgtPos, tapered, width);
        } else if (Styles.BRANCH_CONN_CURVE.equals(line)
                || Styles.BRANCH_CONN_ARROWED_CURVE.equals(line)) {
            curveConn(shape, srcPos, tgtPos, tapered, width);
        } else { // Straight and other unidentifiable line types
            straightConn(shape, srcPos, tgtPos, tapered, width);
        }

        g.setLineWidth(width);
        g.setLineStyle(SWT.LINE_SOLID);
        g.setAlpha(0xff);
        Color fgColor = g.getForegroundColor();
        if (fgColor != null) {
            if (tapered) {
                g.setBackgroundColor(fgColor);
                g.fillPath(shape);
            } else
                g.drawPath(shape);
        }

        shape.dispose();
    }

    public static Point getSourcePos(Rectangle srcBounds, String srcShape,
            Rectangle tgtBounds, String tgtShape, boolean centerUnderline) {
        if (Styles.TOPIC_SHAPE_UNDERLINE.equals(srcShape)) {
            if (centerUnderline) {
                if (tgtBounds.getCenter().x < srcBounds.getCenter().x)
                    return srcBounds.getLeft();
                return srcBounds.getRight();
            } else {
                if (tgtBounds.getCenter().x < srcBounds.getCenter().x)
                    return srcBounds.getBottomLeft();
                return srcBounds.getBottomRight();
            }
        }
        return Geometry.getChopBoxLocation(tgtBounds.getCenter(), srcBounds);
    }

    public static Point getTargetPos(Rectangle tgtBounds, String tgtShape,
            Rectangle srcBounds, String srcShape, boolean centerUnderline) {
        if (Styles.TOPIC_SHAPE_UNDERLINE.equals(tgtShape)) {
            if (centerUnderline) {
                if (tgtBounds.getCenter().x < srcBounds.getCenter().x)
                    return tgtBounds.getRight();
                return tgtBounds.getLeft();
            } else {
                if (tgtBounds.getCenter().x < srcBounds.getCenter().x)
                    return tgtBounds.getBottomRight();
                return tgtBounds.getBottomLeft();
            }
        }
        if (tgtBounds.getCenter().x < srcBounds.getCenter().x)
            return tgtBounds.getRight();
        return tgtBounds.getLeft();
//        return Geometry.getChopBoxLocation( srcBounds.getCenter(), tgtBounds );
    }

    public static void drawTopic(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template, boolean centerUnderline) {
        drawTopic(graphics, bounds, style, template, centerUnderline, false);
    }

    public static void drawTopic(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template, boolean centerUnderline,
            boolean isGradientColor) {
        drawTopic(graphics, bounds, null, style, template, centerUnderline,
                isGradientColor);
    }

    public static void drawTopic(Graphics graphics, Rectangle bounds,
            HashMap<String, String> existedStyle, IStyle style, IStyle template,
            boolean centerUnderline, boolean isGradientColor) {
        Rectangle topicBounds = topicBounds(bounds);
        Path shape = new Path(Display.getCurrent());

        boolean outline = true;
        boolean fill = true;

        String shapeValue = getValue(existedStyle, Styles.ShapeClass, style,
                template);
        if (shapeValue == null
                || Styles.TOPIC_SHAPE_ROUNDEDRECT.equals(shapeValue)) {
            roundedRect(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_ELLIPSE.equals(shapeValue)) {
            ellipse(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_RECT.equals(shapeValue)) {
            rectangle(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_UNDERLINE.equals(shapeValue)) {
            underline(shape, topicBounds, centerUnderline);
            fill = false;
        } else if (Styles.TOPIC_SHAPE_NO_BORDER.equals(shapeValue)) {
            noBorder(shape, topicBounds);
            outline = false;
        } else if (Styles.TOPIC_SHAPE_DIAMOND.equals(shapeValue)) {
            diamondTopic(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_CALLOUT_ELLIPSE.equals(shapeValue)) {
            calloutEllipse(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_CALLOUT_ROUNDEDRECT.equals(shapeValue)) {
            calloutRoundRect(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_CIRCLE.equals(shapeValue)) {
            Rectangle circleTopicBounds = circleTopicBounds(bounds);
            circle(shape, circleTopicBounds);
        } else if (Styles.TOPIC_SHAPE_PARALLELOGRAM.equals(shapeValue)) {
            parallelogram(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_CLOUD.equals(shapeValue)) {
            cloud(shape, topicBounds);
        } else if (Styles.TOPIC_SHAPE_STROKE_CIRCLE.equals(shapeValue)) {
            strokeCircle(shape, topicBounds);
        } else {
            roundedRect(shape, topicBounds);
        }

        String fillColorValue = getValue(existedStyle, Styles.FillColor, style,
                template);
        graphics.setAlpha(0xff);
        if (fillColorValue != null) {
            Color fillColor = ColorUtils.getColor(fillColorValue);
            if (fillColor != null) {
                Path newPath = new Path(Display.getCurrent(),
                        shape.getPathData());
                if (!fill) {
                    rectangle(newPath, topicBounds.expand(0, -1));
                }
                int x = topicBounds.x;
                int y1 = topicBounds.y - topicBounds.height / 4;
                int y2 = topicBounds.y + topicBounds.height;
                if (isGradientColor) {
                    GradientPattern bgPattern = new GradientPattern(
                            Display.getCurrent(), x, y1, x, y2,
                            ColorConstants.white, 0xff, fillColor, 0xff);
                    graphics.setBackgroundPattern(bgPattern);
                    graphics.fillPath(newPath);
                    bgPattern.dispose();
                } else {
                    graphics.setBackgroundColor(fillColor);
                    graphics.fillPath(newPath);
                }
                newPath.dispose();
            }
        }

        if (outline) {
            Color lineColor = getLineColor(style, template,
                    ColorConstants.gray);
            String lineColorValue = getValue(existedStyle,
                    Styles.BorderLineColor, style, template);
            if (lineColorValue != null)
                lineColor = ColorUtils.getColor(lineColorValue);

            if (lineColor != null) {
                String lineWidthValue = getValue(existedStyle,
                        Styles.BorderLineWidth, style, template);
                lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
                if (lineWidthValue == null)
                    lineWidthValue = getValue(existedStyle, Styles.LineWidth,
                            style, template);
                int lineWidth = NumberUtils.safeParseInt(lineWidthValue, 1);
                if (lineWidth > 0) {
                    graphics.setLineWidth(lineWidth);
                    graphics.setLineStyle(SWT.LINE_SOLID);
                    graphics.setForegroundColor(lineColor);
                    graphics.drawPath(shape);
                }
            }
        }
        shape.dispose();
    }

    public static void ellipse(Path shape, Rectangle r) {
        shape.addArc(r, 0, 360);
    }

    public static int getBoundaryPadding() {
        return BOUNDARY_PADDING;
    }

    public static void elbow(Path shape, Point p1, Point p2, boolean tapered,
            int width) {
        Point c = new Point(p1.x, p2.y);
        if (tapered) {
            PrecisionPoint _c = new PrecisionPoint(c);
            PrecisionPoint _p1 = new PrecisionPoint(p1);
            PrecisionPoint _p2 = new PrecisionPoint(p2);
            PrecisionPointPair _cc = Geometry.calculatePositionPair(_c, _p2,
                    0.5);
            PrecisionPointPair _pp2 = Geometry
                    .calculatePositionPair(_p2, _c, 0.5).swap();
            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _c,
                    width);
            double d = (p1.x > p2.x == p1.y > p2.y) ? width * 0.5
                    : -width * 0.5;
            _cc.p1().x -= d;
            _cc.p2().x += d;
            shape.moveTo(_pp1.p1());
            shape.lineTo(_cc.p1());
            shape.lineTo(_pp2.p1());
            shape.lineTo(_pp2.p2());
            shape.lineTo(_cc.p2());
            shape.lineTo(_pp1.p2());
            shape.close();
        } else {
            shape.moveTo(p1);
            shape.lineTo(c);
            shape.lineTo(p2);
        }
    }

    public static void roundElbow(Path shape, Point p1, Point p2,
            boolean tapered, int width) {
        Point c = new Point(p1.x, p2.y);
        int corner = getAppliedCorner(new Rectangle(p1, p2)) * 2;
        Point q1 = new Point(c.x, p1.y > p2.y ? c.y + corner : c.y - corner);
        Point q2 = new Point(p1.x > p2.x ? c.x - corner : c.x + corner, c.y);
        if (tapered) {
            PrecisionPoint _p1 = new PrecisionPoint(p1);
            PrecisionPoint _p2 = new PrecisionPoint(p2);
            PrecisionPoint _q1 = new PrecisionPoint(q1);
            PrecisionPoint _q2 = new PrecisionPoint(q2);
            PrecisionPoint _c1 = new PrecisionPoint(_q1.x,
                    _q1.y + (c.y - _q1.y) * 3 / 4);
            PrecisionPoint _c2 = new PrecisionPoint(
                    _q2.x + (c.x - _q2.x) * 3 / 4, _q2.y);
            PrecisionPoint _pc1 = new PrecisionPoint(_p1.x,
                    _p1.y + (_c1.y - _p1.y) * 2);
            PrecisionPoint _pc2 = new PrecisionPoint(
                    _p2.x + (_c2.x - _p2.x) * 2, _p2.y);

            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _c1,
                    width);
            PrecisionPointPair _qq1 = Geometry.calculatePositionPair(_q1, _c1,
                    width);
            PrecisionPointPair _cc1 = Geometry.calculatePositionPair(_c1, _pc1,
                    width);
            PrecisionPointPair _pp2 = Geometry
                    .calculatePositionPair(_p2, _c2, 0.5).swap();
            PrecisionPointPair _qq2 = Geometry
                    .calculatePositionPair(_q2, _c2, 0.5).swap();
            PrecisionPointPair _cc2 = Geometry
                    .calculatePositionPair(_c2, _pc2, 0.5).swap();
            double d = (p1.x > p2.x == p1.y > p2.y) ? width * 0.5
                    : -width * 0.5;
            _qq2.p1().x -= d;
            _qq2.p2().x += d;
            _cc2.p1().x -= d;
            _cc2.p2().x += d;

            shape.moveTo(_pp1.p1());
            shape.lineTo(_qq1.p1());
            shape.cubicTo(_cc1.p1(), _cc2.p1(), _qq2.p1());
            shape.lineTo(_pp2.p1());
            shape.lineTo(_pp2.p2());
            shape.lineTo(_qq2.p2());
            shape.cubicTo(_cc2.p2(), _cc1.p2(), _qq1.p2());
            shape.lineTo(_pp1.p2());
            shape.close();
        } else {
            shape.moveTo(p1);
            shape.lineTo(q1);
            shape.cubicTo(q1.x, q1.y + (c.y - q1.y) * 3 / 4,
                    q2.x + (c.x - q2.x) * 3 / 4, q2.y, q2.x, q2.y);
            shape.lineTo(p2);
        }
    }

    public static void straightConn(Path shape, Point p1, Point p2,
            boolean tapered, int width) {
        if (tapered) {
            PrecisionPoint _p1 = new PrecisionPoint(p1);
            PrecisionPoint _p2 = new PrecisionPoint(p2);
            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _p2,
                    width);
            PrecisionPointPair _pp2 = Geometry
                    .calculatePositionPair(_p2, _p1, 0.5).swap();
            shape.moveTo(_pp1.p1());
            shape.lineTo(_pp2.p1());
            shape.lineTo(_pp2.p2());
            shape.moveTo(_pp1.p2());
            shape.close();
        } else {
            shape.moveTo(p1);
            shape.lineTo(p2);
        }
    }

    public static void curveConn(Path shape, Point p1, Point p2,
            boolean tapered, int width) {
        if (tapered) {
            PrecisionPoint _p1 = new PrecisionPoint(p1);
            PrecisionPoint _p2 = new PrecisionPoint(p2);
            PrecisionPoint _c = new PrecisionPoint(
                    _p1.x + (_p2.x - _p1.x) * 2 / 10, _p2.y);
            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _p2,
                    width);
            PrecisionPointPair _pp2 = Geometry
                    .calculatePositionPair(_p2, _c, 0.5).swap();
            PrecisionPointPair _cc = Geometry.calculatePositionPair(_c, _p2,
                    0.5);
            double d = (p1.x > p2.x == p1.y > p2.y) ? width * 0.5
                    : -width * 0.5;
            _cc.p1().x -= d;
            _cc.p2().x += d;

            shape.moveTo(_pp1.p1());
            shape.quadTo(_cc.p1(), _pp2.p1());
            shape.lineTo(_pp2.p2());
            shape.quadTo(_cc.p2(), _pp1.p2());
            shape.close();
        } else {
            Point c = new Point(p1.x, p2.y);
            c.x += (p2.x - c.x) * 2 / 10;
            shape.moveTo(p1);
            shape.quadTo(c, p2);
        }
    }

    public static void herringBone(Path shape, Point head, double angle,
            int lineWidth) {
        int l = lineWidth * 2 + 4;
        int w = lineWidth * 2 + 2;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle, l / 2);
        PrecisionPoint p2 = p.getMoved(angle, l);
        PrecisionPoint p01 = p.getMoved(angle - Math.PI * 2 / 3, w);
        PrecisionPoint p02 = p.getMoved(angle + Math.PI * 2 / 3, w);
        PrecisionPoint p11 = p1.getMoved(angle - Math.PI * 2 / 3, w);
        PrecisionPoint p12 = p1.getMoved(angle + Math.PI * 2 / 3, w);
        PrecisionPoint p21 = p2.getMoved(angle - Math.PI * 2 / 3, w);
        PrecisionPoint p22 = p2.getMoved(angle + Math.PI * 2 / 3, w);
        shape.moveTo(p01);
        shape.lineTo(head);
        shape.lineTo(p02);
        shape.moveTo(p11);
        shape.lineTo(p1);
        shape.lineTo(p12);
        shape.moveTo(p21);
        shape.lineTo(p2);
        shape.lineTo(p22);
        shape.moveTo(head);
        shape.lineTo(p2);
    }

    public static void noBorder(Path shape, Rectangle r) {
        shape.addRectangle(r);
    }

    public static void normalArrow(Path shape, Point head, double angle,
            int lineWidth) {
        int side = lineWidth * 2 + 4;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle - Math.PI / 6, side);
        PrecisionPoint p2 = p.getMoved(angle + Math.PI / 6, side);
        shape.moveTo(p1);
        shape.lineTo(head);
        shape.lineTo(p2);
    }

    public static void rectangle(Path shape, Rectangle r) {
        shape.addRectangle(r);
    }

    public static void roundedRect(Path shape, Rectangle r) {
        shape.addRoundedRectangle(r, getAppliedCorner(r));
    }

    public static void scallops(Path shape, Rectangle box) {
        int margin = getBoundaryPadding() * 3 / 5;
        if (box.width <= margin * 2 || box.height <= margin * 2)
            return;

        float width = box.width - margin * 2;
        float height = box.height - margin * 2;
        float stepX = BOUNDARY_STEP;
        float stepY = BOUNDARY_STEP * 6 / 8;
        int numX = Math.max(1, (int) (width / stepX));
        int numY = Math.max(1, (int) (height / stepY));

        stepX = width / numX;
        stepY = height / numY;

        float x = box.x + margin;
        float y = box.y + margin;

        shape.moveTo(x, y);
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x + stepX / 4, y - margin, x + stepX * 3 / 4,
                    y - margin, x + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + margin, y + stepY / 4, x + margin,
                    y + stepY * 3 / 4, x, y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 4, y + margin, x - stepX * 3 / 4,
                    y + margin, x - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - margin, y - stepY / 4, x - margin,
                    y - stepY * 3 / 4, x, y - stepY);
            y -= stepY;
        }
        shape.close();
    }

    public static void spearhead(Path shape, Point head, double angle,
            int lineWidth) {
        int side = lineWidth * 2 + 6;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle - Math.PI / 8, side);
        PrecisionPoint p2 = p.getMoved(angle + Math.PI / 8, side);
        PrecisionPoint cp = p.getMoved(angle, side / 2);
        shape.moveTo(head);
        shape.lineTo(p1);
        shape.quadTo(cp, p2);
        shape.close();
    }

    public static void square(Path shape, Point head, double angle,
            int lineWidth) {
        int side = lineWidth + 2;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle - Math.PI / 4, side);
        PrecisionPoint p2 = p.getMoved(angle - Math.PI * 3 / 4, side);
        PrecisionPoint p3 = p.getMoved(angle + Math.PI * 3 / 4, side);
        PrecisionPoint p4 = p.getMoved(angle + Math.PI / 4, side);
        shape.moveTo(p1);
        shape.lineTo(p2);
        shape.lineTo(p3);
        shape.lineTo(p4);
        shape.close();
    }

    public static void straightRel(Path shape, Rectangle relBounds, Point c1,
            Point c2) {
        Point p = relBounds.getBottomLeft();
        shape.moveTo(p);
        c2.setLocation(p);
        p = relBounds.getTopRight();
        c1.setLocation(p);
        shape.lineTo(p);
    }

    public static void tension(Path shape, Rectangle box) {
        int margin = getBoundaryPadding() / 2;
        int margin2 = Math.max(1, margin / 4);
        if (box.width <= margin * 2 || box.height <= margin * 2)
            return;

        float width = box.width - margin2 * 2;
        float height = box.height - margin2 * 2;
        float stepX = BOUNDARY_STEP;
        float stepY = BOUNDARY_STEP;
        int numX = Math.max(1, (int) (width / stepX));
        int numY = Math.max(1, (int) (height / stepY));

        stepX = width / numX;
        stepY = height / numY;

        float x = box.x + margin2;
        float y = box.y + margin2;

        shape.moveTo(x, y);
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x + stepX / 4, y + margin, x + stepX * 3 / 4,
                    y + margin, x + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - margin, y + stepY / 4, x - margin,
                    y + stepY * 3 / 4, x, y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 4, y - margin, x - stepX * 3 / 4,
                    y - margin, x - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + margin, y - stepY / 4, x + margin,
                    y - stepY * 3 / 4, x, y - stepY);
            y -= stepY;
        }
        shape.close();
    }

    public static void triangle(Path shape, Point head, double angle,
            int lineWidth) {
        int side = lineWidth * 2 + 4;
        PrecisionPoint p = new PrecisionPoint(head);
        PrecisionPoint p1 = p.getMoved(angle - Math.PI / 6, side);
        PrecisionPoint p2 = p.getMoved(angle + Math.PI / 6, side);
        shape.moveTo(p1);
        shape.lineTo(head);
        shape.lineTo(p2);
        shape.close();
    }

    public static void underline(Path shape, Rectangle r, boolean center) {
        Rectangle r2 = r;
        if (center) {
            shape.moveTo(r2.getLeft());
            shape.lineTo(r2.getRight());
        } else {
            shape.moveTo(r2.getBottomLeft());
            shape.lineTo(r2.getBottomRight());
        }
    }

    public static void waves(Path shape, Rectangle box) {
        int margin = getBoundaryPadding() / 4;
        if (box.width <= margin * 2 || box.height <= margin * 2)
            return;

        float width = box.width - margin * 2;
        float height = box.height - margin * 2;
        float stepX = BOUNDARY_STEP;
        float stepY = BOUNDARY_STEP;
        int numX = Math.max(1, (int) (width / stepX));
        int numY = Math.max(1, (int) (height / stepY));

        stepX = width / numX;
        stepY = height / numY;

        float x = box.x + margin;
        float y = box.y + margin;

        float h = ((float) getBoundaryPadding()) / 4;
        shape.moveTo(x, y);
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x + stepX / 8, y - h, x + stepX * 3 / 8, y - h,
                    x + stepX / 2, y);
            shape.cubicTo(x + stepX * 5 / 8, y + h, x + stepX * 7 / 8, y + h,
                    x + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + h, y + stepY / 8, x + h, y + stepY * 3 / 8, x,
                    y + stepY / 2);
            shape.cubicTo(x - h, y + stepY * 5 / 8, x - h, y + stepY * 7 / 8, x,
                    y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 8, y + h, x - stepX * 3 / 8, y + h,
                    x - stepX / 2, y);
            shape.cubicTo(x - stepX * 5 / 8, y - h, x - stepX * 7 / 8, y - h,
                    x - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - h, y - stepY / 8, x - h, y - stepY * 3 / 8, x,
                    y - stepY / 2);
            shape.cubicTo(x + h, y - stepY * 5 / 8, x + h, y - stepY * 7 / 8, x,
                    y - stepY);
            y -= stepY;
        }
        shape.close();
    }

    public static void polygon(Path shape, Rectangle box) {
        shape.moveTo(box.x + box.width / 2, box.y);
        shape.lineTo(box.x, box.y + box.height / 4);
        shape.lineTo(box.x, box.bottom() - box.height / 4);
        shape.lineTo(box.x + box.width / 2, box.bottom());
        shape.lineTo(box.getBottomRight());
        shape.lineTo(box.getTopRight());
        shape.close();
    }

    public static void roundedPolygon(Path shape, Rectangle box) {
        int corner = 4;

        Point p0 = calcRoundedPoint(box.getTop(), box.getTopRight(), corner);
        shape.moveTo(p0);

        cubicAndLine(shape, box.getTopRight(), box.getTop(),
                box.getTopLeft().getTranslated(0, box.height / 4), corner);
        cubicAndLine(shape, box.getTop(),
                box.getTopLeft().getTranslated(0, box.height / 4),
                box.getBottomLeft().getTranslated(0, -box.height / 4), corner);
        cubicAndLine(shape, box.getTopLeft().getTranslated(0, box.height / 4),
                box.getBottomLeft().getTranslated(0, -box.height / 4),
                box.getBottom(), corner);
        cubicAndLine(shape,
                box.getBottomLeft().getTranslated(0, -box.height / 4),
                box.getBottom(), box.getBottomRight(), corner);
        cubicAndLine(shape, box.getBottom(), box.getBottomRight(),
                box.getTopRight(), corner);
        cubicAndLine(shape, box.getBottomRight(), box.getTopRight(),
                box.getTop(), corner);
        shape.close();
    }

    private static void cubicAndLine(Path shape, Point p0, Point p1, Point p2,
            int corner) {
        Point p3 = calcRoundedPoint(p1, p0, corner);
        Point p4 = calcRoundedPoint(p1, p2, corner);
        Point c1 = calcControlPoint(p3, p1);
        Point c2 = calcControlPoint(p4, p1);

        shape.cubicTo(c1, c2, p4);
        shape.lineTo(calcRoundedPoint(p2, p1, corner));
    }

    private static Point calcRoundedPoint(Point p1, Point p2, int corner) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;

        if (dx == 0) {
            if (dy > 0)
                return p1.getTranslated(0, corner);
            return p1.getTranslated(0, -corner);
        } else if (dy == 0) {
            if (dx > 0)
                return p1.getTranslated(corner, 0);
            return p1.getTranslated(-corner, 0);
        } else {
            double l = p1.getDistance(p2);
            double x = dx / l * corner;
            double y = dy / l * corner;
            return p1.getTranslated(x, y);
        }
    }

    private static Point calcControlPoint(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;

        return p1.getTranslated(dx * 0.447715f, dy * 0.447715f);
    }

    private static void drawtext(Graphics graphics, String text,
            Rectangle parentRect, IStyle style, IStyle template, Font font) {
        drawtext(graphics, text, parentRect, null, style, template, font);
    }

    private static void drawtext(Graphics graphics, String text,
            Rectangle parentRect, HashMap<String, String> existedStyle,
            IStyle style, IStyle template, Font font) {
        String fontColor = getValue(existedStyle, Styles.TextColor, style,
                template);
        RGB fontColorRGB = ColorUtils.toRGB(fontColor);
        if (fontColorRGB == null)
            return;

        graphics.setForegroundColor(ColorUtils.getColor(fontColorRGB));

        String textAlign = getValue(existedStyle, Styles.TextAlign, style,
                template);

        String textCase = getValue(existedStyle, Styles.TextCase, style,
                template);

        boolean isStrikedThrough = isStrikeout(existedStyle, style);
        boolean isUnderlined = isUnderline(existedStyle, style);

        if (Styles.UPPERCASE.equals(textCase)) {
            text = text.toUpperCase();
        } else if (Styles.LOWERCASE.equals(textCase)) {
            text = text.toLowerCase();
        } else if (Styles.CAPITALIZE.equals(textCase)) {
            text = capitalize(text);
        }
        Dimension textSize = GraphicsUtils.getAdvanced().getTextSize(text,
                font);
        graphics.setFont(font);
        int x = parentRect.x + (parentRect.width - textSize.width) / 4;
        int y = parentRect.y + (parentRect.height - textSize.height) / 2;

        if (Styles.ALIGN_CENTER.equals(textAlign)) {
            x += (parentRect.width - textSize.width) / 4;
        } else if (Styles.ALIGN_RIGHT.equals(textAlign)) {
            x += (parentRect.width - textSize.width) / 2;
        }

        graphics.drawText(text, x, y);

        y += textSize.height;

        if (isUnderlined) {
            Path underline = new Path(Display.getCurrent());
            underline.moveTo(x, y - 1);
            underline.lineTo(x + textSize.width, y - 1);
            graphics.drawPath(underline);
            underline.dispose();
        }
        if (isStrikedThrough) {
            Path strikeOutLine = new Path(Display.getCurrent());
            strikeOutLine.moveTo(x, y - textSize.height / 2);
            strikeOutLine.lineTo(x + textSize.width, y - textSize.height / 2);
            graphics.drawPath(strikeOutLine);
            strikeOutLine.dispose();
        }

        font.dispose();

    }

    private static String capitalize(String str) {
        StringBuffer stringbf = new StringBuffer();
        Matcher m = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
                .matcher(str);
        while (m.find()) {
            m.appendReplacement(stringbf,
                    m.group(1).toUpperCase() + m.group(2).toLowerCase());
        }
        return m.appendTail(stringbf).toString();
    }

    public static void drawThemeText(Graphics graphics, String text,
            Rectangle parentRect, IStyle style, IStyle template) {
        String fontName = getFontName(style, template);
        int fontDataStyle = getFontDataStyle(style);
        Font font = new Font(null, fontName, 2, fontDataStyle);

        drawtext(graphics, text, parentRect, style, template, font);

        font.dispose();
    }

    public static void drawStyleText(Graphics graphics, String text,
            Rectangle bounds, HashMap<String, String> existedStyle,
            IStyle style, IStyle template) {
        Rectangle parentRect = topicBounds(bounds);
        String fontName = getFontName(existedStyle, style, template);
        int fontDataStyle = getFontDataStyle(style);
        Font font = new Font(null, fontName, 7, fontDataStyle);

        drawtext(graphics, text, parentRect, existedStyle, style, template,
                font);

        font.dispose();
    }

    private static int getFontDataStyle(HashMap<String, String> existedStyle,
            IStyle style) {
        boolean isBold = isBold(existedStyle, style);
        boolean isItalic = isItalic(existedStyle, style);

        int fontDataStyle = SWT.NORMAL;
        if (isBold)
            fontDataStyle |= SWT.BOLD;
        if (isItalic)
            fontDataStyle |= SWT.ITALIC;
        return fontDataStyle;
    }

    private static boolean isBold(HashMap<String, String> existedStyle,
            IStyle style) {
        String weight = getValue(existedStyle, Styles.FontWeight, style, null);
        return weight != null && weight.contains(Styles.FONT_WEIGHT_BOLD);
    }

    private static boolean isItalic(HashMap<String, String> existedStyle,
            IStyle style) {
        String weight = getValue(existedStyle, Styles.FontStyle, style, null);
        return weight != null && weight.contains(Styles.FONT_STYLE_ITALIC);
    }

    private static boolean isUnderline(HashMap<String, String> existedStyle,
            IStyle style) {
        String weight = getValue(existedStyle, Styles.TextDecoration, style,
                null);
        return weight != null
                && weight.contains(Styles.TEXT_DECORATION_UNDERLINE);
    }

    private static boolean isStrikeout(HashMap<String, String> existedStyle,
            IStyle style) {
        String weight = getValue(existedStyle, Styles.TextDecoration, style,
                null);
        return weight != null
                && weight.contains(Styles.TEXT_DECORATION_LINE_THROUGH);
    }

    private static int getFontDataStyle(IStyle style) {
        return getFontDataStyle(null, style);
    }

    private static String getFontName(IStyle style, IStyle template) {
        return getFontName(null, style, template);
    }

    private static String getFontName(HashMap<String, String> existedStyle,
            IStyle style, IStyle template) {
        String fontName = getValue(existedStyle, Styles.FontFamily, style,
                template);
        String availableFontName = FontUtils.getAAvailableFontNameFor(fontName);
        fontName = availableFontName != null ? availableFontName : fontName;

        if (Styles.SYSTEM.equals(fontName)) {
            fontName = JFaceResources.getDefaultFont().getFontData()[0]
                    .getName();
        }
        return fontName;
    }

    private static Rectangle topicBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 9 / 20;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle circleTopicBounds(Rectangle r) {
        int width = Math.min(r.width * 6 / 8, r.height * 6 / 8);
        int height = width;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle boundaryBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 7 / 10;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle relBounds(Rectangle r) {
        int width = r.width * 6 / 8;
        int height = width * 7 / 10;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

    private static Rectangle sheetBounds(Rectangle r) {
        int width = r.width * 7 / 8;
        int height = width * 7 / 8;
        int x = r.x + r.width / 2 - width / 2;
        int y = r.y + r.height / 2 - height / 2;
        return new Rectangle(x, y, width, height);
    }

}