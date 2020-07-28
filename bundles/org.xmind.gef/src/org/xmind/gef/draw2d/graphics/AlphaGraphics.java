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
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;
import org.xmind.gef.draw2d.IUseTransparency;

/**
 * @author Frank Shaka
 */
public class AlphaGraphics extends Graphics implements IUseTransparency {

    private Graphics delegate;

    private int alphaMask = 0xff;

    private int localAlpha;

    private GradientPattern localBgPattern;

    private GradientPattern localFgPattern;

    public AlphaGraphics(Graphics delegate) {
        this.delegate = delegate;
        delegate.pushState();
        this.localAlpha = delegate.getAlpha();
        delegate.setAlpha(getWorkingAlpha(localAlpha));
    }

    protected Graphics getDelegate() {
        return delegate;
    }

    public int getMainAlpha() {
        return alphaMask;
    }

    public void setMainAlpha(int alphaMask) {
        this.alphaMask = alphaMask;
        delegate.setAlpha(getWorkingAlpha(localAlpha));
    }

    public int getSubAlpha() {
        return getAlpha();
    }

    public void setSubAlpha(int alpha) {
        setAlpha(alpha);
    }

    public void setAlpha(int alpha) {
        localAlpha = alpha;
        delegate.setAlpha(getWorkingAlpha(alpha));
    }

    private int getWorkingAlpha(int alpha) {
        return alpha * getMainAlpha() / 0xff;
    }

    public int getAlpha() {
        return localAlpha;
    }

    public void setBackgroundPattern(Pattern pattern) {
        if (pattern instanceof GradientPattern) {
            GradientPattern gp = (GradientPattern) pattern;
            if (localBgPattern != null)
                localBgPattern.dispose();
            localBgPattern = new GradientPattern(gp.getDevice(), gp.x1, gp.y1,
                    gp.x2, gp.y2, gp.color1, getWorkingAlpha(gp.alpha1),
                    gp.color2, getWorkingAlpha(gp.alpha2));
            delegate.setBackgroundPattern(localBgPattern);
        } else {
            delegate.setBackgroundPattern(pattern);
        }
    }

    public void setForegroundPattern(Pattern pattern) {
        if (pattern instanceof GradientPattern) {
            GradientPattern gp = (GradientPattern) pattern;
            if (localFgPattern != null)
                localFgPattern.dispose();
            localFgPattern = new GradientPattern(gp.getDevice(), gp.x1, gp.y1,
                    gp.x2, gp.y2, gp.color1, getWorkingAlpha(gp.alpha1),
                    gp.color2, getWorkingAlpha(gp.alpha2));
            delegate.setForegroundPattern(localFgPattern);
        } else {
            delegate.setForegroundPattern(pattern);
        }
    }

    public void dispose() {
        delegate.popState();
        if (localBgPattern != null) {
            localBgPattern.dispose();
            localBgPattern = null;
        }
        if (localFgPattern != null) {
            localFgPattern.dispose();
            localFgPattern = null;
        }
    }

    // --------------------------
    //  Delegating Methods 
    // --------------------------

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

    public boolean equals(Object obj) {
        return super.equals(obj);
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

    public Color getBackgroundColor() {
        return delegate.getBackgroundColor();
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

    public Color getForegroundColor() {
        return delegate.getForegroundColor();
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

    public int hashCode() {
        return delegate.hashCode();
    }

    public void popState() {
        delegate.popState();
    }

    public void pushState() {
        delegate.pushState();
    }

    public void restoreState() {
        delegate.restoreState();
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

    public void setBackgroundColor(Color rgb) {
        delegate.setBackgroundColor(rgb);
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

    public void setForegroundColor(Color rgb) {
        delegate.setForegroundColor(rgb);
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

    public String toString() {
        return delegate.toString();
    }

    public void translate(float dx, float dy) {
        delegate.translate(dx, dy);
    }

    public void translate(int dx, int dy) {
        delegate.translate(dx, dy);
    }

    // ===========================================================
    //   Since 3.5
    // ===========================================================

    public void setAdvanced(boolean advanced) {
        delegate.setAdvanced(advanced);
    }

    public float getLineWidthFloat() {
        return delegate.getLineWidthFloat();
    }

    @Override
    public LineAttributes getLineAttributes() {
        return super.getLineAttributes();
    }

    @Override
    public float getLineMiterLimit() {
        return super.getLineMiterLimit();
    }

    public boolean getAdvanced() {
        return delegate.getAdvanced();
    }

    public void setLineAttributes(LineAttributes attributes) {
        delegate.setLineAttributes(attributes);
    }

    public void setLineDash(float[] value) {
        delegate.setLineDash(value);
    };

    public void setLineMiterLimit(float miterLimit) {
        delegate.setLineMiterLimit(miterLimit);
    }

    public void setLineWidthFloat(float width) {
        delegate.setLineWidthFloat(width);
    }

}
