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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;

/**
 * @author Frank Shaka
 * 
 */
public abstract class ColorMaskGraphics extends Graphics {

    private Graphics delegate;

    private Stack<Color> fgColors;

    private Stack<Color> bgColors;

    private Color localForeground;

    private Color localBackground;

    private int localAlpha;

    private Map<RGB, Color> workingColors;

    private Stack<Pattern> fgPatterns;

    private Stack<Pattern> bgPatterns;

    private Pattern currentFgPattern;

    private Pattern currentBgPattern;

    public ColorMaskGraphics(Graphics delegate, Object... args) {
        this.delegate = delegate;
        init(args);
        delegate.pushState();
        this.localForeground = delegate.getForegroundColor();
        this.localBackground = delegate.getBackgroundColor();
        pushColors();
        this.localAlpha = delegate.getAlpha();
        delegate.setForegroundColor(getWorkingColor(localForeground));
        delegate.setBackgroundColor(getWorkingColor(localBackground));
        delegate.setAlpha(0xff);
    }

    protected void init(Object... args) {
    }

    protected abstract RGB getMaskColor(RGB rgb);

    private void pushColors() {
        if (fgColors == null)
            fgColors = new Stack<Color>();
        fgColors.push(localForeground);
        if (bgColors == null)
            bgColors = new Stack<Color>();
        bgColors.push(localBackground);
    }

    private void restoreColors() {
        if (fgColors != null && !fgColors.isEmpty())
            localForeground = fgColors.peek();
        if (bgColors != null && !bgColors.isEmpty())
            localBackground = bgColors.peek();
    }

    private void popupColors() {
        if (fgColors != null && !fgColors.isEmpty())
            localForeground = fgColors.pop();
        if (bgColors != null && !bgColors.isEmpty())
            localBackground = bgColors.pop();
    }

    protected Graphics getDelegate() {
        return delegate;
    }

    protected Color getWorkingColor(Color c) {
        if (c == null)
            return null;
        RGB rgb = c.getRGB();
        if (workingColors == null)
            workingColors = new HashMap<RGB, Color>();
        Color grayed = workingColors.get(rgb);
        if (grayed == null) {
            grayed = createWorkingColor(c.getDevice(), rgb);
            workingColors.put(rgb, grayed);
        }
        return grayed;
    }

    private Color createWorkingColor(Device device, RGB rgb) {
        return new Color(device, getMaskColor(rgb));
    }

    public void setForegroundColor(Color rgb) {
        if (rgb.isDisposed()) {
            System.out.println("!!!Color Disposed: " + rgb); //$NON-NLS-1$
        }
        localForeground = rgb;
        delegate.setForegroundColor(getWorkingColor(rgb));
    }

    public void setBackgroundColor(Color rgb) {
        localBackground = rgb;
        delegate.setBackgroundColor(getWorkingColor(rgb));
    }

    public Color getForegroundColor() {
        return localForeground;
    }

    public Color getBackgroundColor() {
        return localBackground;
    }

    public void setAlpha(int alpha) {
        this.localAlpha = alpha;
        if (alpha == 0)
            delegate.setAlpha(alpha);
        else
            delegate.setAlpha(0xff);
    }

    public int getAlpha() {
        return localAlpha;
    }

    public void setForegroundPattern(Pattern pattern) {
        if (currentFgPattern != null) {
            currentFgPattern.dispose();
            currentFgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            GradientPattern gp = (GradientPattern) pattern;
            pattern = createGrayedPattern(gp);
            currentFgPattern = pattern;
        }
        delegate.setForegroundPattern(pattern);
    }

    public void setBackgroundPattern(Pattern pattern) {
        if (currentBgPattern != null) {
            currentBgPattern.dispose();
            currentBgPattern = null;
        }
        if (pattern instanceof GradientPattern) {
            GradientPattern gp = (GradientPattern) pattern;
            pattern = createGrayedPattern(gp);
            currentBgPattern = pattern;
        }
        delegate.setBackgroundPattern(pattern);
    }

    private Pattern createGrayedPattern(GradientPattern p) {
        return new GradientPattern(p.getDevice(), p.x1, p.y1, p.x2, p.y2,
                getWorkingColor(p.color1), 0xff, getWorkingColor(p.color2),
                0xff);
    }

    public void dispose() {
        popupColors();
        delegate.popState();
        if (workingColors != null) {
            for (Color c : workingColors.values()) {
                c.dispose();
            }
            workingColors = null;
        }
        if (currentFgPattern != null) {
            currentFgPattern.dispose();
            currentFgPattern = null;
        }
        if (currentBgPattern != null) {
            currentBgPattern.dispose();
            currentBgPattern = null;
        }
        if (fgPatterns != null) {
            for (Pattern p : fgPatterns) {
                if (p != null)
                    p.dispose();
            }
            fgPatterns = null;
        }
        if (bgPatterns != null) {
            for (Pattern p : bgPatterns) {
                if (p != null)
                    p.dispose();
            }
            bgPatterns = null;
        }
    }

    public void pushState() {
        delegate.pushState();
        pushColors();
        if (fgPatterns == null)
            fgPatterns = new Stack<Pattern>();
        fgPatterns.push(currentFgPattern);
        currentFgPattern = null;

        if (bgPatterns == null)
            bgPatterns = new Stack<Pattern>();
        bgPatterns.push(currentBgPattern);
        currentBgPattern = null;
    }

    public void popState() {
        delegate.popState();
        popupColors();
        if (currentFgPattern != null) {
            currentFgPattern.dispose();
            currentFgPattern = null;
        }
        if (fgPatterns != null) {
            currentFgPattern = fgPatterns.pop();
        }

        if (currentBgPattern != null) {
            currentBgPattern.dispose();
            currentBgPattern = null;
        }
        if (bgPatterns != null) {
            currentBgPattern = bgPatterns.pop();
        }
    }

    public void restoreState() {
        delegate.restoreState();
        restoreColors();
    }

    // ---------------------------
    //  Delegating Methods
    // ---------------------------

    public void clipRect(Rectangle r) {
        delegate.clipRect(r);
    }

    public void clipPath(Path path) {
        delegate.clipPath(path);
    }

    public void drawArc(int x, int y, int w, int h, int offset, int length) {
        delegate.drawArc(x, y, w, h, offset, length);
    }

    public void drawFocus(int x, int y, int w, int h) {
        delegate.drawFocus(x, y, w, h);
    }

    public void drawImage(Image srcImage, int x1, int y1, int w1, int h1,
            int x2, int y2, int w2, int h2) {
        delegate.drawImage(srcImage, x1, y1, w1, h1, x2, y2, w2, h2);
    }

    public void drawImage(Image srcImage, int x, int y) {
        delegate.drawImage(srcImage, x, y);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        delegate.drawLine(x1, y1, x2, y2);
    }

    public void drawOval(int x, int y, int w, int h) {
        delegate.drawOval(x, y, w, h);
    }

    public void drawPath(Path path) {
        delegate.drawPath(path);
    }

    public void drawPoint(int x, int y) {
        delegate.drawPoint(x, y);
    }

    public void drawPolygon(int[] points) {
        delegate.drawPolygon(points);
    }

    public void drawPolygon(PointList points) {
        delegate.drawPolygon(points);
    }

    public void drawPolyline(int[] points) {
        delegate.drawPolyline(points);
    }

    public void drawPolyline(PointList points) {
        delegate.drawPolyline(points);
    }

    public void drawRectangle(int x, int y, int width, int height) {
        delegate.drawRectangle(x, y, width, height);
    }

    public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        delegate.drawRoundRectangle(r, arcWidth, arcHeight);
    }

    public void drawString(String s, int x, int y) {
        delegate.drawString(s, x, y);
    }

    public void drawText(String s, int x, int y, int style) {
        delegate.drawText(s, x, y, style);
    }

    public void drawText(String s, int x, int y) {
        delegate.drawText(s, x, y);
    }

    public void drawTextLayout(TextLayout layout, int x, int y,
            int selectionStart, int selectionEnd, Color selectionForeground,
            Color selectionBackground) {
        delegate.drawTextLayout(layout, x, y, selectionStart, selectionEnd,
                selectionForeground, selectionBackground);
    }

    public void fillArc(int x, int y, int w, int h, int offset, int length) {
        delegate.fillArc(x, y, w, h, offset, length);
    }

    public void fillGradient(int x, int y, int w, int h, boolean vertical) {
        delegate.fillGradient(x, y, w, h, vertical);
    }

    public void fillOval(int x, int y, int w, int h) {
        delegate.fillOval(x, y, w, h);
    }

    public void fillPath(Path path) {
        delegate.fillPath(path);
    }

    public void fillPolygon(int[] points) {
        delegate.fillPolygon(points);
    }

    public void fillPolygon(PointList points) {
        delegate.fillPolygon(points);
    }

    public void fillRectangle(int x, int y, int width, int height) {
        delegate.fillRectangle(x, y, width, height);
    }

    public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        delegate.fillRoundRectangle(r, arcWidth, arcHeight);
    }

    public void fillString(String s, int x, int y) {
        delegate.fillString(s, x, y);
    }

    public void fillText(String s, int x, int y) {
        delegate.fillText(s, x, y);
    }

    public double getAbsoluteScale() {
        return delegate.getAbsoluteScale();
    }

    public int getAntialias() {
        return delegate.getAntialias();
    }

    public Rectangle getClip(Rectangle rect) {
        return delegate.getClip(rect);
    }

    public int getFillRule() {
        return delegate.getFillRule();
    }

    public Font getFont() {
        return delegate.getFont();
    }

    public FontMetrics getFontMetrics() {
        return delegate.getFontMetrics();
    }

    public int getInterpolation() {
        return delegate.getInterpolation();
    }

    public int getLineCap() {
        return delegate.getLineCap();
    }

    public int getLineJoin() {
        return delegate.getLineJoin();
    }

    public int getLineStyle() {
        return delegate.getLineStyle();
    }

    public int getLineWidth() {
        return delegate.getLineWidth();
    }

    public int getTextAntialias() {
        return delegate.getTextAntialias();
    }

    public boolean getXORMode() {
        return delegate.getXORMode();
    }

    public void rotate(float degrees) {
        delegate.rotate(degrees);
    }

    public void scale(double amount) {
        delegate.scale(amount);
    }

    public void scale(float horizontal, float vertical) {
        delegate.scale(horizontal, vertical);
    }

    public void setAntialias(int value) {
        delegate.setAntialias(value);
    }

    public void setClip(Path path) {
        delegate.setClip(path);
    }

    public void setClip(Rectangle r) {
        delegate.setClip(r);
    }

    public void setFillRule(int rule) {
        delegate.setFillRule(rule);
    }

    public void setFont(Font f) {
        delegate.setFont(f);
    }

    public void setInterpolation(int interpolation) {
        delegate.setInterpolation(interpolation);
    }

    public void setLineCap(int cap) {
        delegate.setLineCap(cap);
    }

    public void setLineDash(int[] dash) {
        delegate.setLineDash(dash);
    }

    public void setLineJoin(int join) {
        delegate.setLineJoin(join);
    }

    public void setLineStyle(int style) {
        delegate.setLineStyle(style);
    }

    public void setLineWidth(int width) {
        delegate.setLineWidth(width);
    }

    public void setTextAntialias(int value) {
        delegate.setTextAntialias(value);
    }

    public void setXORMode(boolean b) {
        delegate.setXORMode(b);
    }

    public void shear(float horz, float vert) {
        delegate.shear(horz, vert);
    }

    public void translate(float dx, float dy) {
        delegate.translate(dx, dy);
    }

    public void translate(int dx, int dy) {
        delegate.translate(dx, dy);
    }

    // ==========================================================
    //    Since 3.5
    // ==========================================================

    public boolean getAdvanced() {
        return delegate.getAdvanced();
    }

    public float getLineMiterLimit() {
        return delegate.getLineMiterLimit();
    }

    public LineAttributes getLineAttributes() {
        return delegate.getLineAttributes();
    }

    public float getLineWidthFloat() {
        return delegate.getLineWidthFloat();
    }

    public void setAdvanced(boolean advanced) {
        delegate.setAdvanced(advanced);
    }

    public void setLineMiterLimit(float miterLimit) {
        delegate.setLineMiterLimit(miterLimit);
    }

    public void setLineWidthFloat(float width) {
        delegate.setLineWidthFloat(width);
    }

    public void setLineAttributes(LineAttributes attributes) {
        delegate.setLineAttributes(attributes);
    }

    public void setLineDash(float[] value) {
        delegate.setLineDash(value);
    }

}
