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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;

/**
 * Rotate the coordinate 90 degrees clockwise.
 * 
 * @author frankshaka
 */
public class Rotate90Graphics extends Graphics {

    private Graphics graphics;

    private Pattern lastBgPattern = null;

    private Pattern lastFgPattern = null;

    public Rotate90Graphics(Graphics realGraphics) {
        this.graphics = realGraphics;
    }

    protected Graphics getGraphics() {
        return graphics;
    }

    protected float[] rotate(float x, float y) {
        return new float[] { -y, x };
    }

    protected int[] rotate(int x, int y) {
        return new int[] { -y, x };
    }

    protected Rectangle rotate(Rectangle r) {
        return new Rectangle(-r.y - r.height, r.x, r.height, r.width);
    }

    protected Point rotate(Point p) {
        return new Point(-p.y, p.x);
    }

    protected Path rotate(Path path) {
        PathData data = path.getPathData();
        Path newPath = new Path(path.getDevice());
        int index = 0;
        float[] points = data.points;
        float x, y, cx1, cy1, cx2, cy2;
        for (byte type : data.types) {
            switch (type) {
            case SWT.PATH_MOVE_TO:
                x = points[index++];
                y = points[index++];
                newPath.moveTo(-y, x);
                break;
            case SWT.PATH_LINE_TO:
                x = points[index++];
                y = points[index++];
                newPath.lineTo(-y, x);
                break;
            case SWT.PATH_CUBIC_TO:
                x = points[index++];
                y = points[index++];
                cx1 = points[index++];
                cy1 = points[index++];
                cx2 = points[index++];
                cy2 = points[index++];
                newPath.cubicTo(-y, x, -cy1, cx1, -cy2, cx2);
                break;
            case SWT.PATH_QUAD_TO:
                x = points[index++];
                y = points[index++];
                cx1 = points[index++];
                cy1 = points[index++];
                newPath.quadTo(-y, x, -cy1, cx1);
                break;
            case SWT.PATH_CLOSE:
                newPath.close();
                break;
            }
        }
        return newPath;
    }

    protected int[] rotate(int[] points) {
        int[] newPoints = new int[points.length];
        for (int i = 0; i < points.length; i += 2) {
            newPoints[i] = -points[i + 1];
            newPoints[i + 1] = points[i];
        }
        return newPoints;
    }

    protected PointList rotate(PointList points) {
        int size = points.size();
        PointList newPoints = new PointList(size);
        for (int i = 0; i < size; i++) {
            Point p = points.getPoint(i);
            newPoints.setPoint(rotate(p), i);
        }
        return newPoints;
    }

    private Pattern rotate(GradientPattern p) {
        GradientPattern p2 = new GradientPattern(p.getDevice(), //
                -p.y1, p.x1, //
                -p.y2, p.x2, //
                p.color1, p.alpha1, //
                p.color2, p.alpha2);
        return p2;
    }

    private Pattern rotate(ImagePattern p) {
        ImagePattern p2 = new ImagePattern(p.getDevice(), rotate(p.image));
        return p2;
    }

    private Image rotate(Image image) {

        return image;
    }

    public void clipRect(Rectangle r) {
        graphics.clipRect(rotate(r));
    }

    public void clipPath(Path path) {
        Path p2 = rotate((Path) path);
        graphics.clipPath(p2);
        p2.dispose();
    }

    public void dispose() {
        if (lastBgPattern != null) {
            lastBgPattern.dispose();
            lastBgPattern = null;
        }
        if (lastFgPattern != null) {
            lastFgPattern.dispose();
            lastFgPattern = null;
        }
    }

    public void drawArc(int x, int y, int w, int h, int offset, int length) {
        graphics.drawArc(-y - h, x, h, w, offset, length);
    }

    public void drawFocus(int x, int y, int w, int h) {
        graphics.drawFocus(-y - h, x, h, w);
    }

    public void drawImage(Image srcImage, int x1, int y1, int w1, int h1,
            int x2, int y2, int w2, int h2) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawImage(srcImage, x1, y1, w1, h1, x2, y2, w2, h2);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawImage(Image srcImage, int x, int y) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawImage(srcImage, x, y);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(-y1, x1, -y2, x2);
    }

    public void drawOval(int x, int y, int w, int h) {
        graphics.drawOval(-y - h, x, h, w);
    }

    public void drawPath(Path path) {
        Path p2 = rotate((Path) path);
        graphics.drawPath(p2);
        p2.dispose();
    }

    public void drawPoint(int x, int y) {
        graphics.drawPoint(-y, x);
    }

    public void drawPolygon(int[] points) {
        graphics.drawPolygon(rotate(points));
    }

    public void drawPolygon(PointList points) {
        graphics.drawPolygon(rotate(points));
    }

    public void drawPolyline(int[] points) {
        graphics.drawPolyline(rotate(points));
    }

    public void drawPolyline(PointList points) {
        graphics.drawPolyline(rotate(points));
    }

    public void drawRectangle(int x, int y, int width, int height) {
        graphics.drawRectangle(-y - height, x, height, width);
    }

    public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        graphics.drawRoundRectangle(rotate(r), arcHeight, arcWidth);
    }

    public void drawString(String s, int x, int y) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawString(s, x, y);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawText(String s, int x, int y, int style) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawText(s, x, y, style);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawText(String s, int x, int y) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawText(s, x, y);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void drawTextLayout(TextLayout layout, int x, int y,
            int selectionStart, int selectionEnd, Color selectionForeground,
            Color selectionBackground) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.drawTextLayout(layout, x, y, selectionStart, selectionEnd,
                selectionForeground, selectionBackground);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void fillArc(int x, int y, int w, int h, int offset, int length) {
        graphics.fillArc(-y - h, x, h, w, offset, length);
    }

    public void fillGradient(int x, int y, int w, int h, boolean vertical) {
        graphics.fillGradient(-y - h, x, h, w, !vertical);
    }

    public void fillOval(int x, int y, int w, int h) {
        graphics.fillOval(-y - h, x, h, w);
    }

    public void fillPath(Path path) {
        Path p2 = rotate((Path) path);
        graphics.fillPath(p2);
        p2.dispose();
    }

    public void fillPolygon(int[] points) {
        graphics.fillPolygon(rotate(points));
    }

    public void fillPolygon(PointList points) {
        graphics.fillPolygon(rotate(points));
    }

    public void fillRectangle(int x, int y, int width, int height) {
        graphics.fillRectangle(-y - height, x, height, width);
    }

    public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        graphics.fillRoundRectangle(rotate(r), arcHeight, arcWidth);
    }

    public void fillString(String s, int x, int y) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.fillString(s, x, y);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public void fillText(String s, int x, int y) {
        boolean statePushed = false;
        try {
            graphics.pushState();
            statePushed = true;
        } catch (Throwable t) {
            statePushed = false;
        }
        graphics.rotate(90);
        graphics.fillText(s, x, y);
        graphics.rotate(-90);
        if (statePushed) {
            try {
                graphics.restoreState();
                graphics.popState();
            } catch (Throwable t) {
            }
        }
    }

    public double getAbsoluteScale() {
        return graphics.getAbsoluteScale();
    }

    public int getAlpha() {
        return graphics.getAlpha();
    }

    public int getAntialias() {
        return graphics.getAntialias();
    }

    public Color getBackgroundColor() {
        return graphics.getBackgroundColor();
    }

    public Rectangle getClip(Rectangle rect) {
        rect = graphics.getClip(rect);
        int rx = rect.y;
        int ry = -rect.x - rect.width;
        int rw = rect.height;
        int rh = rect.width;
        rect.x = rx;
        rect.y = ry;
        rect.width = rw;
        rect.height = rh;
        return rect;
    }

    public int getFillRule() {
        return graphics.getFillRule();
    }

    public Font getFont() {
        return graphics.getFont();
    }

    public FontMetrics getFontMetrics() {
        return graphics.getFontMetrics();
    }

    public Color getForegroundColor() {
        return graphics.getForegroundColor();
    }

    public int getInterpolation() {
        return graphics.getInterpolation();
    }

    public int getLineCap() {
        return graphics.getLineCap();
    }

    public int getLineJoin() {
        return graphics.getLineJoin();
    }

    public int getLineStyle() {
        return graphics.getLineStyle();
    }

    public int getLineWidth() {
        return graphics.getLineWidth();
    }

    public int getTextAntialias() {
        return graphics.getTextAntialias();
    }

    public boolean getXORMode() {
        return graphics.getXORMode();
    }

    public void popState() {
        graphics.popState();
    }

    public void pushState() {
        graphics.pushState();
    }

    public void restoreState() {
        graphics.restoreState();
    }

    public void rotate(float degrees) {
        graphics.rotate(degrees);
    }

    public void scale(double amount) {
        graphics.scale(amount);
    }

    public void scale(float horizontal, float vertical) {
        graphics.scale(horizontal, vertical);
    }

    public void setAlpha(int alpha) {
        graphics.setAlpha(alpha);
    }

    public void setAntialias(int value) {
        graphics.setAntialias(value);
    }

    public void setBackgroundColor(Color rgb) {
        graphics.setBackgroundColor(rgb);
    }

    public void setBackgroundPattern(Pattern pattern) {
        if (lastBgPattern != null) {
            lastBgPattern.dispose();
            lastBgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            Pattern p = rotate((GradientPattern) pattern);
            graphics.setBackgroundPattern(p);
            lastBgPattern = p;
        } else if (pattern instanceof ImagePattern) {
            Pattern p = rotate((ImagePattern) pattern);
            graphics.setBackgroundPattern(p);
            lastBgPattern = p;
        } else {
            graphics.setBackgroundPattern(pattern);
        }
    }

    public void setClip(Path path) {
        Path p2 = rotate((Path) path);
        graphics.setClip(p2);
        p2.dispose();
    }

    public void setClip(Rectangle r) {
        graphics.setClip(rotate(r));
    }

    public void setFillRule(int rule) {
        graphics.setFillRule(rule);
    }

    public void setFont(Font f) {
        graphics.setFont(f);
    }

    public void setForegroundColor(Color rgb) {
        graphics.setForegroundColor(rgb);
    }

    public void setForegroundPattern(Pattern pattern) {
        if (lastFgPattern != null) {
            lastFgPattern.dispose();
            lastFgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            Pattern p = rotate((GradientPattern) pattern);
            graphics.setForegroundPattern(p);
            lastFgPattern = p;
        } else if (pattern instanceof ImagePattern) {
            Pattern p = rotate((ImagePattern) pattern);
            graphics.setForegroundPattern(p);
            lastFgPattern = p;
        } else {
            graphics.setForegroundPattern(pattern);
        }
    }

    public void setInterpolation(int interpolation) {
        graphics.setInterpolation(interpolation);
    }

    public void setLineCap(int cap) {
        graphics.setLineCap(cap);
    }

    public void setLineDash(int[] dash) {
        graphics.setLineDash(dash);
    }

    public void setLineJoin(int join) {
        graphics.setLineJoin(join);
    }

    public void setLineStyle(int style) {
        graphics.setLineStyle(style);
    }

    public void setLineWidth(int width) {
        graphics.setLineWidth(width);
    }

    public void setTextAntialias(int value) {
        graphics.setTextAntialias(value);
    }

    public void setXORMode(boolean b) {
        graphics.setXORMode(b);
    }

    public void shear(float horz, float vert) {
        graphics.shear(horz, vert);
    }

    public void translate(float dx, float dy) {
        graphics.translate(-dy, dx);
    }

    public void translate(int dx, int dy) {
        graphics.translate(-dy, dx);
    }

    // ==========================================================
    //    Since 3.5
    // ==========================================================

    public boolean getAdvanced() {
        return graphics.getAdvanced();
    }

    public float getLineWidthFloat() {
        return graphics.getLineWidthFloat();
    }

    public LineAttributes getLineAttributes() {
        return graphics.getLineAttributes();
    }

    public float getLineMiterLimit() {
        return graphics.getLineMiterLimit();
    }

    public void setAdvanced(boolean advanced) {
        graphics.setAdvanced(advanced);
    }

    public void setLineMiterLimit(float miterLimit) {
        graphics.setLineMiterLimit(miterLimit);
    }

    public void setLineWidthFloat(float width) {
        graphics.setLineWidthFloat(width);
    }

    public void setLineAttributes(LineAttributes attributes) {
        graphics.setLineAttributes(attributes);
    }

    public void setLineDash(float[] value) {
        graphics.setLineDash(value);
    }

}