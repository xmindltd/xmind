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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

public class ScaledGraphics extends Graphics {

    /**
     * For Debugging: Once ScaledGraphics is proven to be replaceable by
     * {@link Graphics#scale(double)} on all supported platforms, we will turn
     * this switch off. Clients should check this value before creating
     * ScaledGraphics instances.
     */
    public static boolean SCALED_GRAPHICS_ENABLED = false;

    private static class PatternKey {

        GradientPattern pattern;

        double zoom;

        PatternKey() {
        }

        PatternKey(GradientPattern pattern, double zoom) {
            this.pattern = pattern;
            this.zoom = zoom;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof PatternKey))
                return false;
            PatternKey that = (PatternKey) obj;
            return this.pattern.equals(that.pattern) && this.zoom == that.zoom;
        }

        public int hashCode() {
            long bits = Double.doubleToLongBits(zoom);
            int hc = (int) (bits ^ (bits >>> 32));
            return pattern.hashCode() ^ hc;
        }

        void setValues(GradientPattern pattern, double zoom) {
            this.pattern = pattern;
            this.zoom = zoom;
        }
    }

    private static class FontHeightCache {
        Font font;
        int height;
    }

    protected static class FontKey {
        Font font;
        int height;

        protected FontKey() {/* empty constructor */
        }

        protected FontKey(Font font, int height) {
            this.font = font;
            this.height = height;
        }

        public boolean equals(Object obj) {
            return (((FontKey) obj).font.equals(font)
                    && ((FontKey) obj).height == height);
        }

        public int hashCode() {
            return font.hashCode() ^ height;
        }

        protected void setValues(Font font, int height) {
            this.font = font;
            this.height = height;
        }
    }

    /**
     * The internal state of the scaled graphics.
     */
    protected static class State {
        private double appliedX;
        private double appliedY;
        private Font font;
        private float lineWidth;
        private double zoom;

        private int[] lineDash;

        private Pattern background;

        private Pattern foreground;

        /**
         * Constructs a new, uninitialized State object.
         */
        protected State() {/* empty constructor */
        }

        /**
         * Constructs a new State object and initializes the properties based on
         * the given values.
         * 
         * @param zoom
         *            the zoom factor
         * @param x
         *            the x offset
         * @param y
         *            the y offset
         * @param font
         *            the font
         * @param lineWidth
         *            the line width
         */
        protected State(double zoom, double x, double y, Font font,
                float lineWidth, int[] lineDash, Pattern background,
                Pattern foreground) {
            this.zoom = zoom;
            this.appliedX = x;
            this.appliedY = y;
            this.font = font;
            this.lineWidth = lineWidth;
            this.lineDash = lineDash;
            this.background = background;
            this.foreground = foreground;
        }

        /**
         * Sets all the properties of the state object.
         * 
         * @param zoom
         *            the zoom factor
         * @param x
         *            the x offset
         * @param y
         *            the y offset
         * @param font
         *            the font
         * @param lineWidth
         *            the line width
         */
        protected void setValues(double zoom, double x, double y, Font font,
                float lineWidth, int[] lineDash, Pattern background,
                Pattern foreground) {
            this.zoom = zoom;
            this.appliedX = x;
            this.appliedY = y;
            this.font = font;
            this.lineWidth = lineWidth;
            this.lineDash = lineDash;
            this.background = background;
            this.foreground = foreground;
        }
    }

    private static int[][] intArrayCache = new int[8][];
    private final Rectangle tempRECT = new Rectangle();

    static {
        for (int i = 0; i < intArrayCache.length; i++)
            intArrayCache[i] = new int[i + 1];
    }

    private boolean allowText = true;
    //private static final Point PT = new Point();
    private Map<FontKey, Font> fontCache = new HashMap<FontKey, Font>();
    private Map<Font, FontData> fontDataCache = new HashMap<Font, FontData>();
    private FontKey fontKey = new FontKey();
    private double fractionalX;
    private double fractionalY;
    private Graphics graphics;
    private FontHeightCache localCache = new FontHeightCache();
    private Font localFont;
    private float localLineWidth;
    private List<State> stack = new ArrayList<State>();
    private int stackPointer = 0;
    private FontHeightCache targetCache = new FontHeightCache();

    private double zoom = 1.0;

    private int[] localDash = null;
    private Pattern localBackground = null;
    private Pattern localForeground = null;
    private Map<PatternKey, GradientPattern> patternCache = new HashMap<PatternKey, GradientPattern>();
    private PatternKey patternKey = new PatternKey();

    /**
     * Constructs a new ScaledGraphics based on the given Graphics object.
     * 
     * @param g
     *            the base graphics object
     */
    public ScaledGraphics(Graphics g) {
        graphics = g;
        localFont = g.getFont();
        localLineWidth = g.getLineWidth();
    }

    /**
     * @see Graphics#clipRect(Rectangle)
     */
    public void clipRect(Rectangle r) {
        graphics.clipRect(zoomClipRect(r));
    }

    Font createFont(FontData data) {
        return new Font(Display.getCurrent(), data);
    }

    /**
     * @see Graphics#dispose()
     */
    public void dispose() {
        //Remove all states from the stack
        while (stackPointer > 0) {
            popState();
        }

        //Dispose fonts
        for (Font font : fontCache.values()) {
            font.dispose();
        }

        for (Pattern pattern : patternCache.values()) {
            pattern.dispose();
        }

        //   Resource manager handles fonts 
    }

    /**
     * @see Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int w, int h, int offset, int sweep) {
        Rectangle z = zoomRect(x, y, w, h);
        if (z.isEmpty() || sweep == 0)
            return;
        graphics.drawArc(z, offset, sweep);
    }

    /**
     * @see Graphics#drawFocus(int, int, int, int)
     */
    public void drawFocus(int x, int y, int w, int h) {
        graphics.drawFocus(zoomRect(x, y, w, h));
    }

    /**
     * @see Graphics#drawImage(Image, int, int)
     */
    public void drawImage(Image srcImage, int x, int y) {
        org.eclipse.swt.graphics.Rectangle size = srcImage.getBounds();
        graphics.drawImage(srcImage, 0, 0, size.width, size.height,
                (int) (Math.floor((x * zoom + fractionalX))),
                (int) (Math.floor((y * zoom + fractionalY))),
                (int) (Math.floor((size.width * zoom + fractionalX))),
                (int) (Math.floor((size.height * zoom + fractionalY))));
    }

    /**
     * @see Graphics#drawImage(Image, int, int, int, int, int, int, int, int)
     */
    public void drawImage(Image srcImage, int sx, int sy, int sw, int sh,
            int tx, int ty, int tw, int th) {
        //"t" == target rectangle, "s" = source

        Rectangle t = zoomRect(tx, ty, tw, th);
        if (!t.isEmpty())
            graphics.drawImage(srcImage, sx, sy, sw, sh, t.x, t.y, t.width,
                    t.height);
    }

    /**
     * @see Graphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine((int) (Math.floor((x1 * zoom + fractionalX))),
                (int) (Math.floor((y1 * zoom + fractionalY))),
                (int) (Math.floor((x2 * zoom + fractionalX))),
                (int) (Math.floor((y2 * zoom + fractionalY))));
    }

    /**
     * @see Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int w, int h) {
        graphics.drawOval(zoomRect(x, y, w, h));
    }

    /**
     * @see Graphics#drawPoint(int, int)
     */
    public void drawPoint(int x, int y) {
        graphics.drawPoint((int) Math.floor(x * zoom + fractionalX),
                (int) Math.floor(y * zoom + fractionalY));
    }

    /**
     * @see Graphics#drawPolygon(int[])
     */
    public void drawPolygon(int[] points) {
        graphics.drawPolygon(zoomPointList(points));
    }

    /**
     * @see Graphics#drawPolygon(PointList)
     */
    public void drawPolygon(PointList points) {
        graphics.drawPolygon(zoomPointList(points.toIntArray()));
    }

    /**
     * @see Graphics#drawPolyline(int[])
     */
    public void drawPolyline(int[] points) {
        graphics.drawPolyline(zoomPointList(points));
    }

    /**
     * @see Graphics#drawPolyline(PointList)
     */
    public void drawPolyline(PointList points) {
        graphics.drawPolyline(zoomPointList(points.toIntArray()));
    }

    /**
     * @see Graphics#drawRectangle(int, int, int, int)
     */
    public void drawRectangle(int x, int y, int w, int h) {
        graphics.drawRectangle(zoomRect(x, y, w, h));
    }

    /**
     * @see Graphics#drawRoundRectangle(Rectangle, int, int)
     */
    public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        graphics.drawRoundRectangle(zoomRect(r.x, r.y, r.width, r.height),
                (int) (arcWidth * zoom), (int) (arcHeight * zoom));
    }

    /**
     * @see Graphics#drawString(String, int, int)
     */
    public void drawString(String s, int x, int y) {
        if (allowText)
            graphics.drawString(s, zoomTextPoint(x, y));
    }

    /**
     * @see Graphics#drawText(String, int, int)
     */
    public void drawText(String s, int x, int y) {
        if (allowText)
            graphics.drawText(s, zoomTextPoint(x, y));
    }

    /**
     * @see Graphics#drawText(String, int, int, int)
     */
    public void drawText(String s, int x, int y, int style) {
        if (allowText)
            graphics.drawText(s, zoomTextPoint(x, y), style);
    }

    /**
     * @see Graphics#drawTextLayout(TextLayout, int, int, int, int, Color,
     *      Color)
     */
    public void drawTextLayout(TextLayout layout, int x, int y,
            int selectionStart, int selectionEnd, Color selectionForeground,
            Color selectionBackground) {
        TextLayout scaled = zoomTextLayout(layout);
        graphics.drawTextLayout(scaled,
                (int) Math.floor(x * zoom + fractionalX),
                (int) Math.floor(y * zoom + fractionalY), selectionStart,
                selectionEnd, selectionBackground, selectionForeground);
        scaled.dispose();
    }

    /**
     * @see Graphics#fillArc(int, int, int, int, int, int)
     */
    public void fillArc(int x, int y, int w, int h, int offset, int sweep) {
        Rectangle z = zoomFillRect(x, y, w, h);
        if (z.isEmpty() || sweep == 0)
            return;
        graphics.fillArc(z, offset, sweep);
    }

    /**
     * @see Graphics#fillGradient(int, int, int, int, boolean)
     */
    public void fillGradient(int x, int y, int w, int h, boolean vertical) {
        graphics.fillGradient(zoomFillRect(x, y, w, h), vertical);
    }

    /**
     * @see Graphics#fillOval(int, int, int, int)
     */
    public void fillOval(int x, int y, int w, int h) {
        graphics.fillOval(zoomFillRect(x, y, w, h));
    }

    /**
     * @see Graphics#fillPolygon(int[])
     */
    public void fillPolygon(int[] points) {
        graphics.fillPolygon(zoomPointList(points));
    }

    /**
     * @see Graphics#fillPolygon(PointList)
     */
    public void fillPolygon(PointList points) {
        graphics.fillPolygon(zoomPointList(points.toIntArray()));
    }

    /**
     * @see Graphics#fillRectangle(int, int, int, int)
     */
    public void fillRectangle(int x, int y, int w, int h) {
        graphics.fillRectangle(zoomFillRect(x, y, w, h));
    }

    /**
     * @see Graphics#fillRoundRectangle(Rectangle, int, int)
     */
    public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
        graphics.fillRoundRectangle(zoomFillRect(r.x, r.y, r.width, r.height),
                (int) (arcWidth * zoom), (int) (arcHeight * zoom));
    }

    /**
     * @see Graphics#fillString(String, int, int)
     */
    public void fillString(String s, int x, int y) {
        if (allowText)
            graphics.fillString(s, zoomTextPoint(x, y));
    }

    /**
     * @see Graphics#fillText(String, int, int)
     */
    public void fillText(String s, int x, int y) {
        if (allowText)
            graphics.fillText(s, zoomTextPoint(x, y));
    }

    /**
     * @see Graphics#getAbsoluteScale()
     */
    public double getAbsoluteScale() {
        return zoom * graphics.getAbsoluteScale();
    }

    /**
     * @see Graphics#getAlpha()
     */
    public int getAlpha() {
        return graphics.getAlpha();
    }

    /**
     * @see Graphics#getAntialias()
     */
    public int getAntialias() {
        return graphics.getAntialias();
    }

    /**
     * @see Graphics#getBackgroundColor()
     */
    public Color getBackgroundColor() {
        return graphics.getBackgroundColor();
    }

    private Font getCachedFont(FontKey key) {
        Font font = fontCache.get(key);
        if (font != null)
            return font;

        key = new FontKey(key.font, key.height);
        Font zoomedFont = createZoomedFont(key.font, key.height);
        fontCache.put(key, zoomedFont);
        return zoomedFont;
    }

    private Font createZoomedFont(Font font, int height) {
        FontData[] fontData = font.getFontData();
        for (FontData f : fontData) {
            f.setHeight(height);
        }
        return new Font(Display.getCurrent(), fontData);
    }

    private FontData getCachedFontData(Font f) {
        FontData data = fontDataCache.get(f);
        if (data != null)
            return data;
        data = getLocalFont().getFontData()[0];
        fontDataCache.put(f, data);
        return data;
    }

    private Pattern getCachedPattern(PatternKey key) {
        GradientPattern pattern = patternCache.get(key);
        if (pattern != null)
            return pattern;

        key = new PatternKey(key.pattern, key.zoom);
        pattern = createZoomedPattern(key.pattern);
        patternCache.put(key, pattern);
        return pattern;
    }

    private GradientPattern createZoomedPattern(GradientPattern p1) {
        return new GradientPattern(p1.getDevice(), (float) (zoom * p1.x1),
                (float) (zoom * p1.y1), (float) (zoom * p1.x2),
                (float) (zoom * p1.y2), p1.color1, p1.alpha1, p1.color2,
                p1.alpha2);
    }

    /**
     * @see Graphics#getClip(Rectangle)
     */
    public Rectangle getClip(Rectangle rect) {
        graphics.getClip(rect);
        int x = (int) (rect.x / zoom);
        int y = (int) (rect.y / zoom);
        /*
         * If the clip rectangle is queried, perform an inverse zoom, and take
         * the ceiling of the resulting double. This is necessary because
         * forward scaling essentially performs a floor() function. Without
         * this, figures will think that they don't need to paint when actually
         * they do.
         */
        rect.width = (int) Math.ceil(rect.right() / zoom) - x;
        rect.height = (int) Math.ceil(rect.bottom() / zoom) - y;
        rect.x = x;
        rect.y = y;
        return rect;
    }

    /**
     * @see Graphics#getFillRule()
     */
    public int getFillRule() {
        return graphics.getFillRule();
    }

    /**
     * @see Graphics#getFont()
     */
    public Font getFont() {
        return getLocalFont();
    }

    /**
     * @see Graphics#getFontMetrics()
     */
    public FontMetrics getFontMetrics() {
        return FigureUtilities.getFontMetrics(localFont);
    }

    /**
     * @see Graphics#getForegroundColor()
     */
    public Color getForegroundColor() {
        return graphics.getForegroundColor();
    }

    /**
     * @see Graphics#getInterpolation()
     */
    public int getInterpolation() {
        return graphics.getInterpolation();
    }

    /**
     * @see Graphics#getLineCap()
     */
    public int getLineCap() {
        return graphics.getLineCap();
    }

    /**
     * @see Graphics#getLineJoin()
     */
    public int getLineJoin() {
        return graphics.getLineJoin();
    }

    /**
     * @see Graphics#getLineStyle()
     */
    public int getLineStyle() {
        return graphics.getLineStyle();
    }

    /**
     * @see Graphics#getLineWidth()
     */
    public int getLineWidth() {
        return (int) getLocalLineWidth();
    }

    public float getLineWidthFloat() {
        return getLocalLineWidth();
    }

    protected final Font getLocalFont() {
        return localFont;
    }

    protected final float getLocalLineWidth() {
        return localLineWidth;
    }

    /**
     * @see Graphics#getTextAntialias()
     */
    public int getTextAntialias() {
        return graphics.getTextAntialias();
    }

    /**
     * @see Graphics#getXORMode()
     */
    public boolean getXORMode() {
        return graphics.getXORMode();
    }

    /**
     * @see Graphics#popState()
     */
    public void popState() {
        graphics.popState();
        stackPointer--;
        restoreLocalState(stack.get(stackPointer));
        if (lastClipPath != null) {
            lastClipPath.dispose();
            lastClipPath = null;
        }
    }

    /**
     * @see Graphics#pushState()
     */
    public void pushState() {
        if (stack.size() > stackPointer) {
            State s = stack.get(stackPointer);
            s.setValues(zoom, fractionalX, fractionalY, getLocalFont(),
                    getLocalLineWidth(), localDash, localBackground,
                    localForeground);
        } else {
            stack.add(new State(zoom, fractionalX, fractionalY, getLocalFont(),
                    getLocalLineWidth(), localDash, localBackground,
                    localForeground));
        }
        stackPointer++;

        graphics.pushState();
    }

    protected void restoreLocalState(State state) {
        this.fractionalX = state.appliedX;
        this.fractionalY = state.appliedY;
        setScale(state.zoom);
        setLocalFont(state.font);
        setLocalLineWidth(state.lineWidth);
        setLocalLineDash(state.lineDash);
        setLocalBackgroundPattern(state.background);
        setLocalForegroundPattern(state.foreground);
    }

    /**
     * @see Graphics#restoreState()
     */
    public void restoreState() {
        graphics.restoreState();
        restoreLocalState(stack.get(stackPointer - 1));
    }

    /**
     * @see Graphics#scale(double)
     */
    public void scale(double amount) {
        setScale(zoom * amount);
    }

    /**
     * This method requires advanced graphics support. A check should be made to
     * ensure advanced graphics is supported in the user's environment before
     * calling this method. See {@link GCUtilities#supportsAdvancedGraphics()}.
     * 
     * @see Graphics#setAlpha(int)
     */
    public void setAlpha(int alpha) {
        graphics.setAlpha(alpha);
    }

    /**
     * This method requires advanced graphics support. A check should be made to
     * ensure advanced graphics is supported in the user's environment before
     * calling this method. See {@link GCUtilities#supportsAdvancedGraphics()}.
     * 
     * @see Graphics#setAntialias(int)
     */
    public void setAntialias(int value) {
        graphics.setAntialias(value);
    }

    /**
     * @see Graphics#setBackgroundColor(Color)
     */
    public void setBackgroundColor(Color rgb) {
        graphics.setBackgroundColor(rgb);
    }

    /**
     * @see Graphics#setClip(Rectangle)
     */
    public void setClip(Rectangle r) {
        graphics.setClip(zoomClipRect(r));
    }

    /**
     * @see Graphics#setFillRule(int)
     */
    public void setFillRule(int rule) {
        graphics.setFillRule(rule);
    }

    /**
     * @see Graphics#setFont(Font)
     */
    public void setFont(Font f) {
        setLocalFont(f);
    }

    /**
     * @see Graphics#setForegroundColor(Color)
     */
    public void setForegroundColor(Color rgb) {
        graphics.setForegroundColor(rgb);
    }

    /**
     * This method requires advanced graphics support. A check should be made to
     * ensure advanced graphics is supported in the user's environment before
     * calling this method. See {@link GCUtilities#supportsAdvancedGraphics()}.
     * 
     * @see org.eclipse.draw2d.Graphics#setInterpolation(int)
     */
    public void setInterpolation(int interpolation) {
        graphics.setInterpolation(interpolation);
    }

    /**
     * @see Graphics#setLineCap(int)
     */
    public void setLineCap(int cap) {
        graphics.setLineCap(cap);
    }

    /**
     * @see Graphics#setLineDash(int[])
     */
    public void setLineDash(int[] dash) {
        setLocalLineDash(dash);
    }

    private void setLocalLineDash(int[] dash) {
        localDash = dash;
        if (dash != null)
            graphics.setLineDash(zoomDash(dash));
    }

    /**
     * @see Graphics#setLineJoin(int)
     */
    public void setLineJoin(int join) {
        graphics.setLineJoin(join);
    }

    /**
     * @see Graphics#setLineStyle(int)
     */
    public void setLineStyle(int style) {
        graphics.setLineStyle(style);
    }

    /**
     * @see Graphics#setLineWidth(int)
     */
    public void setLineWidth(int width) {
        setLineWidthFloat(width);
    }

    public void setLineWidthFloat(float width) {
        setLocalLineWidth(width);
    }

    private void setLocalFont(Font f) {
        localFont = f;
        graphics.setFont(zoomFont(f));
    }

    private void setLocalLineWidth(float width) {
        localLineWidth = width;
        graphics.setLineWidth((int) zoomLineWidth(width));
    }

    void setScale(double value) {
        if (zoom == value)
            return;
        this.zoom = value;
        graphics.setFont(zoomFont(getLocalFont()));
        graphics.setLineWidth((int) zoomLineWidth(getLocalLineWidth()));
        if (localDash != null)
            graphics.setLineDash(zoomDash(localDash));
        if (localBackground != null)
            graphics.setBackgroundPattern(zoomPattern(localBackground));
        if (localForeground != null)
            graphics.setForegroundPattern(zoomPattern(localForeground));
    }

    /**
     * This method requires advanced graphics support. A check should be made to
     * ensure advanced graphics is supported in the user's environment before
     * calling this method. See {@link GCUtilities#supportsAdvancedGraphics()}.
     * 
     * @see Graphics#setTextAntialias(int)
     */
    public void setTextAntialias(int value) {
        graphics.setTextAntialias(value);
    }

    /**
     * @see Graphics#setXORMode(boolean)
     */
    public void setXORMode(boolean b) {
        graphics.setXORMode(b);
    }

    /**
     * @see Graphics#translate(int, int)
     */
    public void translate(int dx, int dy) {
        // fractionalX/Y is the fractional part left over from previous 
        // translates that gets lost in the integer approximation.
        double dxFloat = dx * zoom + fractionalX;
        double dyFloat = dy * zoom + fractionalY;
        fractionalX = dxFloat - Math.floor(dxFloat);
        fractionalY = dyFloat - Math.floor(dyFloat);
        graphics.translate((int) Math.floor(dxFloat),
                (int) Math.floor(dyFloat));
    }

    private Rectangle zoomClipRect(Rectangle r) {
        tempRECT.x = (int) (Math.floor(r.x * zoom + fractionalX));
        tempRECT.y = (int) (Math.floor(r.y * zoom + fractionalY));
        tempRECT.width = (int) (Math
                .ceil(((r.x + r.width) * zoom + fractionalX))) - tempRECT.x;
        tempRECT.height = (int) (Math
                .ceil(((r.y + r.height) * zoom + fractionalY))) - tempRECT.y;
        return tempRECT;
    }

    private Rectangle zoomFillRect(int x, int y, int w, int h) {
        tempRECT.x = (int) (Math.floor((x * zoom + fractionalX)));
        tempRECT.y = (int) (Math.floor((y * zoom + fractionalY)));
        tempRECT.width = (int) (Math.floor(((x + w - 1) * zoom + fractionalX)))
                - tempRECT.x + 1;
        tempRECT.height = (int) (Math.floor(((y + h - 1) * zoom + fractionalY)))
                - tempRECT.y + 1;
        return tempRECT;
    }

    private Font zoomFont(Font f) {
        if (f == null)
            f = Display.getCurrent().getSystemFont();
        FontData data = getCachedFontData(f);
        int zoomedFontHeight = zoomFontHeight(data.getHeight());
        allowText = zoomedFontHeight > 0;
        fontKey.setValues(f, zoomedFontHeight);
        return getCachedFont(fontKey);
    }

    protected int zoomFontHeight(int height) {
        return (int) (zoom * height);
    }

    protected float zoomLineWidth(float w) {
        return (float) (zoom * w);
    }

    private int[] zoomPointList(int[] points) {
        int[] scaled = null;

        // Look in cache for a integer array with the same length as 'points'
        for (int i = 0; i < intArrayCache.length; i++) {
            if (intArrayCache[i].length == points.length) {
                scaled = intArrayCache[i];

                // Move this integer array up one notch in the array
                if (i != 0) {
                    int[] temp = intArrayCache[i - 1];
                    intArrayCache[i - 1] = scaled;
                    intArrayCache[i] = temp;
                }
            }
        }

        // If no match is found, take the one that is last and resize it.
        if (scaled == null) {
            intArrayCache[intArrayCache.length - 1] = new int[points.length];
            scaled = intArrayCache[intArrayCache.length - 1];
        }

        // Scale the points
        for (int i = 0; (i + 1) < points.length; i += 2) {
            scaled[i] = (int) (Math.floor((points[i] * zoom + fractionalX)));
            scaled[i + 1] = (int) (Math
                    .floor((points[i + 1] * zoom + fractionalY)));
        }
        return scaled;
    }

    protected Rectangle zoomRect(int x, int y, int w, int h) {
        tempRECT.x = (int) (Math.floor(x * zoom + fractionalX));
        tempRECT.y = (int) (Math.floor(y * zoom + fractionalY));
        tempRECT.width = (int) (Math.floor(((x + w) * zoom + fractionalX)))
                - tempRECT.x;
        tempRECT.height = (int) (Math.floor(((y + h) * zoom + fractionalY)))
                - tempRECT.y;
        return tempRECT;
    }

    private TextLayout zoomTextLayout(TextLayout layout) {
        TextLayout zoomed = new TextLayout(Display.getCurrent());
        zoomed.setText(layout.getText());

        int zoomWidth = -1;

        if (layout.getWidth() != -1)
            zoomWidth = ((int) (layout.getWidth() * zoom));

        if (zoomWidth < -1 || zoomWidth == 0)
            return null;

        zoomed.setFont(zoomFont(layout.getFont()));
        zoomed.setAlignment(layout.getAlignment());
        zoomed.setAscent(layout.getAscent());
        zoomed.setDescent(layout.getDescent());
        zoomed.setOrientation(layout.getOrientation());
        zoomed.setSegments(layout.getSegments());
        zoomed.setSpacing(layout.getSpacing());
        zoomed.setTabs(layout.getTabs());

        zoomed.setWidth(zoomWidth);
        int length = layout.getText().length();
        if (length > 0) {
            int start = 0, offset = 1;
            TextStyle style = null, lastStyle = layout.getStyle(0);
            for (; offset <= length; offset++) {
                if (offset != length
                        && (style = layout.getStyle(offset)) == lastStyle)
                    continue;
                int end = offset - 1;

                if (lastStyle != null) {
                    TextStyle zoomedStyle = new TextStyle(
                            zoomFont(lastStyle.font), lastStyle.foreground,
                            lastStyle.background);
                    zoomedStyle.metrics = lastStyle.metrics;
                    zoomedStyle.rise = lastStyle.rise;
                    zoomedStyle.strikeout = lastStyle.strikeout;
                    zoomedStyle.underline = lastStyle.underline;
                    zoomed.setStyle(zoomedStyle, start, end);
                }
                lastStyle = style;
                start = offset;
            }
        }
        return zoomed;
    }

    private Point zoomTextPoint(int x, int y) {
        if (localCache.font != localFont) {
            //Font is different, re-calculate its height
            FontMetrics metric = FigureUtilities.getFontMetrics(localFont);
            localCache.height = metric.getHeight() - metric.getDescent();
            localCache.font = localFont;
        }
        if (targetCache.font != graphics.getFont()) {
            FontMetrics metric = graphics.getFontMetrics();
            targetCache.font = graphics.getFont();
            targetCache.height = metric.getHeight() - metric.getDescent();
        }
        return new Point(((int) (Math.floor((x * zoom) + fractionalX))),
                (int) (Math.floor((y + localCache.height - 1) * zoom
                        - targetCache.height + 1 + fractionalY)));
    }

    protected Graphics getGraphics() {
        return graphics;
    }

    /**
     * @see org.eclipse.draw2d.Graphics#drawPath(org.eclipse.swt.graphics.Path)
     */
    @Override
    public void drawPath(Path path) {
        Path zoomPath = zoomPath(path);
        getGraphics().drawPath(zoomPath);
        zoomPath.dispose();
    }

    /**
     * @see org.eclipse.draw2d.Graphics#fillPath(org.eclipse.swt.graphics.Path)
     */
    @Override
    public void fillPath(Path path) {
        Path zoomPath = zoomPath(path);
        getGraphics().fillPath(zoomPath);
        zoomPath.dispose();
    }

    /**
     * @param path
     * @return
     */
    private Path zoomPath(Path path) {
        PathData data = path.getPathData();
        Path newPath = new Path(path.getDevice());
        int index = 0;
        for (byte type : data.types) {
            switch (type) {
            case SWT.PATH_MOVE_TO:
                newPath.moveTo((float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]));
                break;
            case SWT.PATH_LINE_TO:
                newPath.lineTo((float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]));
                break;
            case SWT.PATH_CUBIC_TO:
                newPath.cubicTo((float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]));
                break;
            case SWT.PATH_QUAD_TO:
                newPath.quadTo((float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]),
                        (float) (zoom * data.points[index++]));
                break;
            case SWT.PATH_CLOSE:
                newPath.close();
                break;
            }
        }
        return newPath;
    }

    /**
     * @see org.eclipse.draw2d.Graphics#setBackgroundPattern(org.eclipse.swt.graphics.Pattern)
     */
    @Override
    public void setBackgroundPattern(Pattern pattern) {
        setLocalBackgroundPattern(pattern);
    }

    /**
     * @see org.eclipse.draw2d.Graphics#setForegroundPattern(org.eclipse.swt.graphics.Pattern)
     */
    @Override
    public void setForegroundPattern(Pattern pattern) {
        setLocalForegroundPattern(pattern);
    }

    private void setLocalBackgroundPattern(Pattern pattern) {
        localBackground = pattern;
        graphics.setBackgroundPattern(zoomPattern(pattern));
    }

    private void setLocalForegroundPattern(Pattern pattern) {
        localForeground = pattern;
        graphics.setForegroundPattern(zoomPattern(pattern));
    }

    /**
     * @param dash
     * @return
     */
    private int[] zoomDash(int[] dash) {
        if (dash == null || dash.length == 0) {
            dash = new int[1];
            dash[0] = 1;
            return dash;
        }
        int[] d = new int[dash.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = Math.max(1, (int) (zoom * dash[i]));
        }
        return d;
    }

    protected Pattern zoomPattern(Pattern pattern) {
        if (!(pattern instanceof GradientPattern))
            return pattern;
        patternKey.setValues((GradientPattern) pattern, zoom);
        return getCachedPattern(patternKey);
    }

    /**
     * @see org.eclipse.draw2d.Graphics#rotate(float)
     */
    @Override
    public void rotate(float degrees) {
        graphics.rotate(degrees);
    }

    public void translate(float dx, float dy) {
        graphics.translate(dx, dy);
    }

    private Path lastClipPath = null;

    public void setClip(Path path) {
        Path p = path == null ? null : zoomPath(path);
        graphics.setClip(p);
        if (lastClipPath != null) {
            lastClipPath.dispose();
            lastClipPath = null;
        }
        if (p != path) {
            lastClipPath = p;
        }
    }

    /**
     * @see org.eclipse.draw2d.Graphics#clipPath(org.eclipse.swt.graphics.Path)
     */
    public void clipPath(Path path) {
        Path scaledPath = createScaledPath(path);
        try {
            graphics.clipPath(scaledPath);
        } finally {
            scaledPath.dispose();
        }
    }

    /**
     * Scales given path by zoom factor
     * 
     * @param path
     *            Path to be scaled
     * @return Scaled path
     */
    private Path createScaledPath(Path path) {
        PathData p = path.getPathData();
        for (int i = 0; i < p.points.length; i += 2) {
            p.points[i] = (float) (p.points[i] * zoom + fractionalX);
            p.points[i + 1] = (float) (p.points[i + 1] * zoom + fractionalY);
        }
        Path scaledPath = new Path(path.getDevice());
        int index = 0;
        for (int i = 0; i < p.types.length; i++) {
            byte type = p.types[i];
            switch (type) {
            case SWT.PATH_MOVE_TO:
                scaledPath.moveTo(p.points[index], p.points[index + 1]);
                index += 2;
                break;
            case SWT.PATH_LINE_TO:
                scaledPath.lineTo(p.points[index], p.points[index + 1]);
                index += 2;
                break;
            case SWT.PATH_CUBIC_TO:
                scaledPath.cubicTo(p.points[index], p.points[index + 1],
                        p.points[index + 2], p.points[index + 3],
                        p.points[index + 4], p.points[index + 5]);
                index += 6;
                break;
            case SWT.PATH_QUAD_TO:
                scaledPath.quadTo(p.points[index], p.points[index + 1],
                        p.points[index + 2], p.points[index + 3]);
                index += 4;
                break;
            case SWT.PATH_CLOSE:
                scaledPath.close();
                break;
            }
        }
        return scaledPath;
    }

    // ==========================================================
    //    Since 3.5
    // ==========================================================

    public boolean getAdvanced() {
        return graphics.getAdvanced();
    }

    public LineAttributes getLineAttributes() {
        LineAttributes a = graphics.getLineAttributes();
        a.width = getLocalLineWidth();
        return a;
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

    public void setLineAttributes(LineAttributes attributes) {
        graphics.setLineAttributes(attributes);
        setLocalLineWidth(attributes.width);
    }

    public void setLineDash(float[] value) {
        graphics.setLineDash(value);
    }

}