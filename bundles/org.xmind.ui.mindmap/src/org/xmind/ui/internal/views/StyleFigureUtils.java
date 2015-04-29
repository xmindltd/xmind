/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.views;

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
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
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

    public static void curvedRel(Path shape, Rectangle relBounds, Point c1,
            Point c2) {
        int dx = -relBounds.width / 10;
        int dy = relBounds.height / 10;
        shape.moveTo(relBounds.getBottomLeft());
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
        double angle = new PrecisionPoint(tail).getAngle(new PrecisionPoint(
                head));

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
        Path shape = new Path(Display.getCurrent());
        String shapeValue = getValue(Styles.ShapeClass, style, template);
        if (shapeValue == null
                || Styles.BOUNDARY_SHAPE_ROUNDEDRECT.equals(shapeValue)) {
            roundedRect(shape, bounds);
        } else if (Styles.BOUNDARY_SHAPE_RECT.equals(shapeValue)) {
            rectangle(shape, bounds);
        } else if (Styles.BOUNDARY_SHAPE_SCALLOPS.equals(shapeValue)) {
            scallops(shape, bounds);
        } else if (Styles.BOUNDARY_SHAPE_TENSION.equals(shapeValue)) {
            tension(shape, bounds);
        } else if (Styles.BOUNDARY_SHAPE_WAVES.equals(shapeValue)) {
            waves(shape, bounds);
        } else {
            roundedRect(shape, bounds);
        }
        String fillColorValue = getValue(Styles.FillColor, style, template);

        if (fillColorValue != null) {
            Color fillColor = ColorUtils.getColor(fillColorValue);
            if (fillColor != null) {
                String opacityValue = getValue(Styles.Opacity, style, template);
                double opacity = NumberUtils.safeParseDouble(opacityValue, 1);
                int alpha = (int) (opacity * 0xff);
                graphics.setAlpha(alpha);
                graphics.setBackgroundColor(fillColor);
                graphics.fillPath(shape);
            }
        }

        Color lineColor = getLineColor(style, template, ColorConstants.gray);
        String lineWidthValue = getValue(Styles.LineWidth, style, template);
        lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
        int lineWidth = NumberUtils.safeParseInt(lineWidthValue, 3);
        graphics.setLineWidth(lineWidth);

        String linePatternValue = getValue(Styles.LinePattern, style, template);
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
        drawSheetBackground(graphics, bounds, style, template, true);
    }

    public static void drawSheetBackground(Graphics graphics, Rectangle bounds,
            IStyle style, IStyle template, boolean withMainBranches) {
        Color fillColor = null;
        String fillColorValue = getValue(Styles.FillColor, style, template);
        if (fillColorValue != null)
            fillColor = ColorUtils.getColor(fillColorValue);
        if (fillColor != null) {
            graphics.setAlpha(0xff);
            graphics.setBackgroundColor(fillColor);
            graphics.fillRectangle(bounds);

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
        Path shape = new Path(Display.getCurrent());
        Point c1 = new Point();
        Point c2 = new Point();

        String shapeValue = getValue(Styles.ShapeClass, style, template);
        if (Styles.REL_SHAPE_ANGLED.equals(shapeValue)) {
            angledRel(shape, bounds, c1, c2);
        } else if (Styles.REL_SHAPE_STRAIGHT.equals(shapeValue)) {
            straightRel(shape, bounds, c1, c2);
        } else {
            curvedRel(shape, bounds, c1, c2);
        }

        Color lineColor = getLineColor(style, template, ColorConstants.gray);

        String lineWidthValue = getValue(Styles.LineWidth, style, template);
        lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
        int lineWidth = NumberUtils.safeParseInt(lineWidthValue, 3);
        graphics.setLineWidth(lineWidth);

        String linePatternValue = getValue(Styles.LinePattern, style, template);
        int linePattern = StyleUtils.toSWTLineStyle(linePatternValue,
                SWT.LINE_DOT);
        graphics.setLineStyle(linePattern);
        graphics.setAlpha(0xff);
        graphics.setForegroundColor(lineColor);
        graphics.drawPath(shape);
        shape.dispose();

        graphics.setLineStyle(SWT.LINE_SOLID);
        String beginArrowValue = getValue(Styles.ArrowBeginClass, style,
                template);
        if (beginArrowValue != null
                && !Styles.ARROW_SHAPE_NONE.equals(beginArrowValue)) {
            drawArrow(graphics, beginArrowValue, bounds.getBottomLeft(), c1,
                    lineWidth);
        }

        String endArrowValue = getValue(Styles.ArrowEndClass, style, template);
        if (endArrowValue == null)
            endArrowValue = Styles.ARROW_SHAPE_NORMAL;
        if (!Styles.ARROW_SHAPE_NONE.equals(endArrowValue)) {
            drawArrow(graphics, endArrowValue, bounds.getTopRight(), c2,
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
        Color lineColor = null;
        String lineColorValue = getValue(Styles.LineColor, style, template);
        if (lineColorValue != null)
            lineColor = ColorUtils.getColor(lineColorValue);
        if (lineColor == null) {
            lineColor = defaultLineColor;
        }
        return lineColor;
    }

    public static String getValue(String key, IStyle style, IStyle template) {
        String value = style == null ? null : style.getProperty(key);
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
        int tgtWidth = NumberUtils.safeParseInt(StyleUtils.trimNumber(getValue(
                Styles.LineWidth, tgtStyle, tgtTemplate)), 1);
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
        Path shape = new Path(Display.getCurrent());

        boolean outline = true;
        boolean fill = true;

        String shapeValue = getValue(Styles.ShapeClass, style, template);
        if (shapeValue == null
                || Styles.TOPIC_SHAPE_ROUNDEDRECT.equals(shapeValue)) {
            roundedRect(shape, bounds);
        } else if (Styles.TOPIC_SHAPE_ELLIPSE.equals(shapeValue)) {
            ellipse(shape, bounds);
        } else if (Styles.TOPIC_SHAPE_RECT.equals(shapeValue)) {
            rectangle(shape, bounds);
        } else if (Styles.TOPIC_SHAPE_UNDERLINE.equals(shapeValue)) {
            underline(shape, bounds, centerUnderline);
            fill = false;
        } else if (Styles.TOPIC_SHAPE_NO_BORDER.equals(shapeValue)) {
            noBorder(shape, bounds);
            outline = false;
        } else if (Styles.TOPIC_SHAPE_DIAMOND.equals(shapeValue)) {
            diamondTopic(shape, bounds);
        } else if (Styles.TOPIC_SHAPE_CALLOUT_ELLIPSE.equals(shapeValue)) {
            calloutEllipse(shape, bounds);
        } else if (Styles.TOPIC_SHAPE_CALLOUT_ROUNDEDRECT.equals(shapeValue)) {
            calloutRoundRect(shape, bounds);
        } else {
            roundedRect(shape, bounds);
        }

        String fillColorValue = getValue(Styles.FillColor, style, template);

        graphics.setAlpha(0xff);
        if (fillColorValue != null && fill) {
            Color fillColor = ColorUtils.getColor(fillColorValue);
            if (fillColor != null) {
                int x = bounds.x;
                int y1 = bounds.y - bounds.height / 4;
                int y2 = bounds.y + bounds.height;
                if (isGradientColor) {
                    GradientPattern bgPattern = new GradientPattern(
                            Display.getCurrent(), x, y1, x, y2,
                            ColorConstants.white, 0xff, fillColor, 0xff);
                    graphics.setBackgroundPattern(bgPattern);
                    graphics.fillPath(shape);
                    bgPattern.dispose();
                } else {
                    graphics.setBackgroundColor(fillColor);
                    graphics.fillPath(shape);
                }
            }
        }

        if (outline) {
            Color lineColor = getLineColor(style, template, ColorConstants.gray);
            String lineColorValue = getValue(Styles.BorderLineColor, style,
                    template);
            if (lineColorValue != null)
                lineColor = ColorUtils.getColor(lineColorValue);

            if (lineColor != null) {
                String lineWidthValue = getValue(Styles.BorderLineWidth, style,
                        template);
                lineWidthValue = StyleUtils.trimNumber(lineWidthValue);
                if (lineWidthValue == null)
                    lineWidthValue = getValue(Styles.LineWidth, style, template);
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
            PrecisionPointPair _pp2 = Geometry.calculatePositionPair(_p2, _c,
                    0.5).swap();
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
            PrecisionPoint _c1 = new PrecisionPoint(_q1.x, _q1.y
                    + (c.y - _q1.y) * 3 / 4);
            PrecisionPoint _c2 = new PrecisionPoint(_q2.x + (c.x - _q2.x) * 3
                    / 4, _q2.y);
            PrecisionPoint _pc1 = new PrecisionPoint(_p1.x, _p1.y
                    + (_c1.y - _p1.y) * 2);
            PrecisionPoint _pc2 = new PrecisionPoint(_p2.x + (_c2.x - _p2.x)
                    * 2, _p2.y);

            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _c1,
                    width);
            PrecisionPointPair _qq1 = Geometry.calculatePositionPair(_q1, _c1,
                    width);
            PrecisionPointPair _cc1 = Geometry.calculatePositionPair(_c1, _pc1,
                    width);
            PrecisionPointPair _pp2 = Geometry.calculatePositionPair(_p2, _c2,
                    0.5).swap();
            PrecisionPointPair _qq2 = Geometry.calculatePositionPair(_q2, _c2,
                    0.5).swap();
            PrecisionPointPair _cc2 = Geometry.calculatePositionPair(_c2, _pc2,
                    0.5).swap();
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
            shape.cubicTo(q1.x, q1.y + (c.y - q1.y) * 3 / 4, q2.x
                    + (c.x - q2.x) * 3 / 4, q2.y, q2.x, q2.y);
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
            PrecisionPointPair _pp2 = Geometry.calculatePositionPair(_p2, _p1,
                    0.5).swap();
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
            PrecisionPoint _c = new PrecisionPoint(_p1.x + (_p2.x - _p1.x) * 2
                    / 10, _p2.y);
            PrecisionPointPair _pp1 = Geometry.calculatePositionPair(_p1, _p2,
                    width);
            PrecisionPointPair _pp2 = Geometry.calculatePositionPair(_p2, _c,
                    0.5).swap();
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
            shape.cubicTo(x + stepX / 4, y - margin, x + stepX * 3 / 4, y
                    - margin, x + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + margin, y + stepY / 4, x + margin, y + stepY * 3
                    / 4, x, y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 4, y + margin, x - stepX * 3 / 4, y
                    + margin, x - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - margin, y - stepY / 4, x - margin, y - stepY * 3
                    / 4, x, y - stepY);
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
            shape.cubicTo(x + stepX / 4, y + margin, x + stepX * 3 / 4, y
                    + margin, x + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - margin, y + stepY / 4, x - margin, y + stepY * 3
                    / 4, x, y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 4, y - margin, x - stepX * 3 / 4, y
                    - margin, x - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + margin, y - stepY / 4, x + margin, y - stepY * 3
                    / 4, x, y - stepY);
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
            shape.cubicTo(x + stepX / 8, y - h, x + stepX * 3 / 8, y - h, x
                    + stepX / 2, y);
            shape.cubicTo(x + stepX * 5 / 8, y + h, x + stepX * 7 / 8, y + h, x
                    + stepX, y);
            x += stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x + h, y + stepY / 8, x + h, y + stepY * 3 / 8, x, y
                    + stepY / 2);
            shape.cubicTo(x - h, y + stepY * 5 / 8, x - h, y + stepY * 7 / 8,
                    x, y + stepY);
            y += stepY;
        }
        for (int i = 0; i < numX; i++) {
            shape.cubicTo(x - stepX / 8, y + h, x - stepX * 3 / 8, y + h, x
                    - stepX / 2, y);
            shape.cubicTo(x - stepX * 5 / 8, y - h, x - stepX * 7 / 8, y - h, x
                    - stepX, y);
            x -= stepX;
        }
        for (int i = 0; i < numY; i++) {
            shape.cubicTo(x - h, y - stepY / 8, x - h, y - stepY * 3 / 8, x, y
                    - stepY / 2);
            shape.cubicTo(x + h, y - stepY * 5 / 8, x + h, y - stepY * 7 / 8,
                    x, y - stepY);
            y -= stepY;
        }
        shape.close();
    }

    public static void drawtext(Graphics graphics, String text,
            Rectangle parentRect, IStyle style, IStyle template) {
        String fontColor = getValue(Styles.TextColor, style, template);
        RGB fontColorRGB = ColorUtils.toRGB(fontColor);
        if (fontColorRGB == null)
            return;

        graphics.setForegroundColor(ColorUtils.getColor(fontColorRGB));

        String textAlign = getValue(Styles.TextAlign, style, template);

        String fontName = getValue(Styles.FontFamily, style, template);
        if (Styles.SYSTEM.equals(fontName)) {
            fontName = JFaceResources.getDefaultFont().getFontData()[0]
                    .getName();
        }

        boolean isBold = StyleUtils.isBold(style);
        boolean isItalic = StyleUtils.isItalic(style);
        boolean isStrikedThrough = StyleUtils.isStrikeout(style);
        boolean isUnderlined = StyleUtils.isUnderline(style);

        int fontDataStyle = SWT.NORMAL;
        if (isBold)
            fontDataStyle |= SWT.BOLD;
        if (isItalic)
            fontDataStyle |= SWT.ITALIC;
        Font font = new Font(null, fontName, 7, fontDataStyle);

        Dimension textSize = GraphicsUtils.getAdvanced()
                .getTextSize(text, font);
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
}