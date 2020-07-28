/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.de.erichseifert.vectorgraphics2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.UnsupportedEncodingException;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xmind.de.erichseifert.vectorgraphics2d.util.GraphicsUtils;

/**
 * Base for classed that want to implement vector export.
 *
 * @author Jason Wong
 */
public abstract class VectorGraphics2D extends Graphics2D {
    /** Constants to define how fonts are rendered. */
    public static enum FontRendering {
        /**
         * Constant indicating that fonts should be rendered as text objects.
         */
        TEXT,
        /** Constant indicating that fonts should be converted to vectors. */
        VECTORS
    }

    /** Maximal resolution for image rastering. */
    private static final int DEFAULT_PAINT_IMAGE_SIZE_MAXIMUM = 128;

    /** Document contents. */
    private final StringBuffer document;
    /** Rectangular bounds of the documents. */
    private final Rectangle2D bounds;
    /** Resolution in dots per inch that is used to raster paints. */
    private double resolution;
    /** Maximal size of images that are used to raster paints. */
    private int rasteredImageSizeMaximum;
    /** Font rendering mode. */
    private FontRendering fontRendering;
    /** Flag that stores whether affine transformations have been applied. */
    private boolean transformed;

    /** Rendering hints. */
    private final RenderingHints hints;
    /** Current background color. */
    private Color background;
    /** Current foreground color. */
    private Color color;
    /** Shape used for clipping paint operations. */
    private Shape clip;
    /** Method used for compositing. */
    private Composite composite;
    /** Device configuration settings. */
    private final GraphicsConfiguration deviceConfig;
    /** Current font. */
    private Font font;
    /** Context settings used to render fonts. */
    private final FontRenderContext fontRenderContext;
    /** Paint used to fill shapes. */
    private Paint paint;
    /** Stroke used for drawing shapes. */
    private Stroke stroke;
    /** Current transformation matrix. */
    private final AffineTransform transform;
    /** XOR mode used for rendering. */
    private Color xorMode;

    /**
     * Constructor to initialize a new {@code VectorGraphics2D} document. The
     * dimensions of the document must be passed.
     *
     * @param x
     *            Horizontal position of document origin.
     * @param y
     *            Vertical position of document origin.
     * @param width
     *            Width of document.
     * @param height
     *            Height of document.
     */
    public VectorGraphics2D(double x, double y, double width, double height) {
        hints = new RenderingHints(new HashMap<RenderingHints.Key, Object>());
        document = new StringBuffer();
        bounds = new Rectangle2D.Double(x, y, width, height);
        fontRendering = FontRendering.TEXT;
        resolution = 72.0;
        rasteredImageSizeMaximum = DEFAULT_PAINT_IMAGE_SIZE_MAXIMUM;

        background = Color.WHITE;
        color = Color.BLACK;
        composite = AlphaComposite.getInstance(AlphaComposite.CLEAR);
        deviceConfig = null;
        font = Font.decode(null);
        fontRenderContext = new FontRenderContext(null, false, true);
        paint = color;
        stroke = new BasicStroke(1f);
        transform = new AffineTransform();
        transformed = false;
        xorMode = Color.BLACK;
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        this.hints.putAll(hints);
    }

    @Override
    public void clip(Shape s) {
        if ((getClip() != null) && (s != null)) {
            Area clipAreaOld = new Area(getClip());
            Area clipAreaNew = new Area(s);
            clipAreaNew.intersect(clipAreaOld);
            s = clipAreaNew;
        }
        setClip(s);
    }

    @Override
    public void draw(Shape s) {
        writeShape(s);
        writeClosingDraw(s);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        draw(g.getOutline(x, y));
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        BufferedImage bimg = getTransformedImage(img, xform);
        drawImage(bimg, null, bimg.getMinX(), bimg.getMinY());
        return true;
    }

    /**
     * Returns a transformed version of an image.
     *
     * @param image
     *            Image to be transformed
     * @param xform
     *            Affine transform to be applied
     * @return Image with transformed content
     */
    private BufferedImage getTransformedImage(Image image, AffineTransform xform) {
        Integer interpolationType = (Integer) hints
                .get(RenderingHints.KEY_INTERPOLATION);
        if (RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                .equals(interpolationType)) {
            interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        } else if (RenderingHints.VALUE_INTERPOLATION_BILINEAR
                .equals(interpolationType)) {
            interpolationType = AffineTransformOp.TYPE_BILINEAR;
        } else {
            interpolationType = AffineTransformOp.TYPE_BICUBIC;
        }
        AffineTransformOp op = new AffineTransformOp(xform, interpolationType);
        BufferedImage bufferedImage = GraphicsUtils.toBufferedImage(image);
        return op.filter(bufferedImage, null);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        if (op != null) {
            img = op.filter(img, null);
        }
        drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        drawRenderedImage(img.createDefaultRendering(), xform);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        BufferedImage bimg = GraphicsUtils.toBufferedImage(img);
        drawImage(bimg, xform, null);
    }

    @Override
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        if (str != null && str.trim().isEmpty()) {
            return;
        }
        switch (getFontRendering()) {
        case VECTORS:
            TextLayout layout = new TextLayout(str, getFont(),
                    getFontRenderContext());
            Shape s = layout.getOutline(AffineTransform.getTranslateInstance(x,
                    y));
            fill(s);
            break;
        case TEXT:
            writeString(str, x, y);
            break;
        default:
            throw new IllegalStateException("Unknown font rendering mode."); //$NON-NLS-1$
        }
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        // TODO Take text formatting into account
        StringBuffer buf = new StringBuffer();
        for (char c = iterator.first(); c != AttributedCharacterIterator.DONE; c = iterator
                .next()) {
            buf.append(c);
        }
        drawString(buf.toString(), x, y);
    }

    @Override
    public void fill(Shape s) {
        writeShape(s);
        writeClosingFill(s);
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public Composite getComposite() {
        return composite;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return deviceConfig;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        if (RenderingHints.KEY_ANTIALIASING.equals(hintKey)) {
            return RenderingHints.VALUE_ANTIALIAS_OFF;
        } else if (RenderingHints.KEY_TEXT_ANTIALIASING.equals(hintKey)) {
            return RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        } else if (RenderingHints.KEY_FRACTIONALMETRICS.equals(hintKey)) {
            return RenderingHints.VALUE_FRACTIONALMETRICS_ON;
        }
        return hints.get(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return hints;
    }

    @Override
    public Stroke getStroke() {
        return stroke;
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            Shape sStroke = getStroke().createStrokedShape(s);
            return sStroke.intersects(rect);
        } else {
            return s.intersects(rect);
        }
    }

    @Override
    public void setBackground(Color color) {
        background = color;
    }

    @Override
    public void setComposite(Composite comp) {
        composite = comp;
    }

    @Override
    public void setPaint(Paint paint) {
        if (paint != null) {
            this.paint = paint;
            if (paint instanceof Color) {
                setColor((Color) paint);
            } else if (paint instanceof MultipleGradientPaint) {
                // Set brightest or least opaque color for gradients
                Color[] colors = ((MultipleGradientPaint) paint).getColors();
                if (colors.length == 1) {
                    setColor(colors[0]);
                } else if (colors.length > 1) {
                    Color colLight = colors[0];
                    float brightness = getBrightness(colLight);
                    int alpha = colLight.getAlpha();

                    for (int i = 1; i < colors.length; i++) {
                        Color c = colors[i];
                        float b = getBrightness(c);
                        int a = c.getAlpha();
                        if (b < brightness || a < alpha) {
                            colLight = c;
                            brightness = b;
                        }
                    }
                    setColor(colLight);
                }
            }
        }
    }

    /**
     * Utility method to get the brightness of a specified color.
     *
     * @param c
     *            Color.
     * @return Brightness value between 0f (black) and 1f (white).
     */
    private static float getBrightness(Color c) {
        return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        hints.put(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        this.hints.putAll(hints);
    }

    @Override
    public void setStroke(Stroke s) {
        stroke = s;
    }

    @Override
    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    @Override
    public void setTransform(AffineTransform tx) {
        setAffineTransform(tx);
    }

    /**
     * Sets the current transformation.
     *
     * @param tx
     *            Current transformation
     */
    protected void setAffineTransform(AffineTransform tx) {
        if (!transform.equals(tx)) {
            transform.setTransform(tx);
            transformed = true;
        }
    }

    @Override
    public void shear(double shx, double shy) {
        AffineTransform transform = getTransform();
        transform.shear(shx, shy);
        setAffineTransform(transform);
    }

    @Override
    public void transform(AffineTransform tx) {
        AffineTransform transform = getTransform();
        transform.concatenate(tx);
        setAffineTransform(transform);
    }

    @Override
    public void translate(int x, int y) {
        translate((double) x, (double) y);
    }

    @Override
    public void translate(double tx, double ty) {
        AffineTransform transform = getTransform();
        transform.translate(tx, ty);
        setAffineTransform(transform);
    }

    @Override
    public void rotate(double theta) {
        AffineTransform transform = getTransform();
        transform.rotate(theta);
        setAffineTransform(transform);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        AffineTransform transform = getTransform();
        transform.rotate(theta, x, y);
        setAffineTransform(transform);
    }

    @Override
    public void scale(double sx, double sy) {
        AffineTransform transform = getTransform();
        transform.scale(sx, sy);
        setAffineTransform(transform);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        // TODO Implement
        // throw new
        // UnsupportedOperationException("clearRect() isn't supported by VectorGraphics2D.");
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        clip(new Rectangle(x, y, width, height));
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // TODO Implement
        // throw new
        // UnsupportedOperationException("copyArea() isn't supported by VectorGraphics2D.");
    }

    @Override
    public Graphics create() {
        // TODO Implement
        return this;
    }

    @Override
    public void dispose() {
        // TODO Implement
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        draw(new Arc2D.Double(x, y, width, height, startAngle, arcAngle,
                Arc2D.OPEN));
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(observer),
                img.getHeight(observer), observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(observer),
                img.getHeight(observer), observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        int imgWidth = img.getWidth(observer);
        int imgHeight = img.getHeight(observer);
        writeImage(img, imgWidth, imgHeight, x, y, width, height);
        return true; // TODO Return only true if image data was complete
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        if (img == null) {
            return true;
        }

        int sx = Math.min(sx1, sx2);
        int sy = Math.min(sy1, sy2);
        int sw = Math.abs(sx2 - sx1);
        int sh = Math.abs(sy2 - sy1);
        int dx = Math.min(dx1, dx2);
        int dy = Math.min(dy1, dy2);
        int dw = Math.abs(dx2 - dx1);
        int dh = Math.abs(dy2 - dy1);

        // Draw image
        BufferedImage bufferedImg = GraphicsUtils.toBufferedImage(img);
        Image cropped = bufferedImg.getSubimage(sx, sy, sw, sh);
        return drawImage(cropped, dx, dy, dw, dh, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        if (img == null) {
            return true;
        }

        int sx = Math.min(sx1, sx2);
        int sy = Math.min(sy1, sy2);
        int sw = Math.abs(sx2 - sx1);
        int sh = Math.abs(sy2 - sy1);
        int dx = Math.min(dx1, dx2);
        int dy = Math.min(dy1, dy2);
        int dw = Math.abs(dx2 - dx1);
        int dh = Math.abs(dy2 - dy1);

        // Fill Rectangle with bgcolor
        Color bgcolorOld = getColor();
        setColor(bgcolor);
        fill(new Rectangle(dx, dy, dw, dh));
        setColor(bgcolorOld);

        // Draw image on rectangle
        BufferedImage bufferedImg = GraphicsUtils.toBufferedImage(img);
        Image cropped = bufferedImg.getSubimage(sx, sy, sw, sh);
        return drawImage(cropped, dx, dy, dw, dh, observer);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        draw(new Line2D.Double(x1, y1, x2, y2));
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Double(x, y, width, height));
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D p = new Path2D.Float();
        for (int i = 0; i < nPoints; i++) {
            if (i > 0) {
                p.lineTo(xPoints[i], yPoints[i]);
            } else {
                p.moveTo(xPoints[i], yPoints[i]);
            }
        }
        p.closePath();
        draw(p);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D p = new Path2D.Float();
        for (int i = 0; i < nPoints; i++) {
            if (i > 0) {
                p.lineTo(xPoints[i], yPoints[i]);
            } else {
                p.moveTo(xPoints[i], yPoints[i]);
            }
        }
        draw(p);
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        draw(new Rectangle2D.Double(x, y, width, height));
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Double(x, y, width, height, arcWidth,
                arcHeight));
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        fill(new Arc2D.Double(x, y, width, height, startAngle, arcAngle,
                Arc2D.PIE));
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        fill(new Ellipse2D.Double(x, y, width, height));
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D p = new Path2D.Float();
        for (int i = 0; i < nPoints; i++) {
            if (i > 0) {
                p.lineTo(xPoints[i], yPoints[i]);
            } else {
                p.moveTo(xPoints[i], yPoints[i]);
            }
        }
        p.closePath();

        fill(p);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle2D.Double(x, y, width, height));
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Double(x, y, width, height, arcWidth,
                arcHeight));
    }

    @Override
    public Shape getClip() {
        Shape clip = this.clip;
        if (clip != null) {
            try {
                clip = transform.createInverse().createTransformedShape(
                        this.clip);
            } catch (NoninvertibleTransformException e) {
                clip = null;
            }
        }
        return clip;
    }

    @Override
    public Rectangle getClipBounds() {
        if (getClip() == null) {
            return null;
        }
        return getClip().getBounds();
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        // TODO Find a better way for creating a new FontMetrics instance
        BufferedImage bi = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = bi.getGraphics();
        FontMetrics fontMetrics = g.getFontMetrics(getFont());
        g.dispose();
        bi = null;
        return fontMetrics;
    }

    @Override
    public void setClip(Shape clip) {
        if (clip != null) {
            this.clip = transform.createTransformedShape(clip);
        } else {
            this.clip = null;
        }
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        setClip(new Rectangle(x, y, width, height));
    }

    @Override
    public void setColor(Color c) {
        color = c;
    }

    @Override
    public void setFont(Font font) {
        if (!this.font.equals(font)) {
            this.font = font;
        }
    }

    @Override
    public void setPaintMode() {
        // TODO Implement
        // throw new
        // UnsupportedOperationException("setPaintMode() isn't supported.");
    }

    @Override
    public void setXORMode(Color c1) {
        xorMode = c1;
    }

    /**
     * Utility method for writing multiple objects to the SVG document.
     *
     * @param strs
     *            Objects to be written
     */
    protected void write(Object... strs) {
        for (Object o : strs) {
            String str = o.toString();
            if ((o instanceof Double) || (o instanceof Float)) {
                str = String.format(Locale.ENGLISH, "%.7f", o).replaceAll( //$NON-NLS-1$
                        "\\.?0+$", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            document.append(str);
        }
    }

    /**
     * Utility method for writing a line of multiple objects to the SVG
     * document.
     *
     * @param strs
     *            Objects to be written
     */
    protected void writeln(Object... strs) {
        write(strs);
        write("\n"); //$NON-NLS-1$
    }

    /**
     * Write the specified shape to the document. This does not necessarily
     * contain the actual command to paint the shape.
     *
     * @param s
     *            Shape to be written.
     */
    protected abstract void writeShape(Shape s);

    /**
     * Write the specified image to the document. A number of dimensions will
     * specify how the image will be placed in the document.
     *
     * @param img
     *            Image to be rendered.
     * @param imgWidth
     *            Number of pixels in horizontal direction.
     * @param imgHeight
     *            Number of pixels in vertical direction
     * @param x
     *            Horizontal position in document units where the upper left
     *            corner of the image should be placed.
     * @param y
     *            Vertical position in document units where the upper left
     *            corner of the image should be placed.
     * @param width
     *            Width of the image in document units.
     * @param height
     *            Height of the image in document units.
     */
    protected abstract void writeImage(Image img, int imgWidth, int imgHeight,
            double x, double y, double width, double height);

    /**
     * Write a text string to the document at a specified position.
     *
     * @param str
     *            Text to be rendered.
     * @param x
     *            Horizontal position in document units.
     * @param y
     *            Vertical position in document units.
     */
    protected abstract void writeString(String str, double x, double y);

    /**
     * Write a command to draw the outline of a previously inserted shape.
     *
     * @param s
     *            Shape that should be drawn.
     */
    protected abstract void writeClosingDraw(Shape s);

    /**
     * Write a command to fill the outline of a previously inserted shape.
     *
     * @param s
     *            Shape that should be filled.
     */
    protected void writeClosingFill(Shape s) {
        Rectangle2D shapeBounds = s.getBounds2D();

        // Calculate dimensions of shape with current transformations
        int wImage = (int) Math.ceil(shapeBounds.getWidth() * getResolution());
        int hImage = (int) Math.ceil(shapeBounds.getHeight() * getResolution());
        // Limit the size of images
        wImage = Math.min(wImage, rasteredImageSizeMaximum);
        hImage = Math.min(hImage, rasteredImageSizeMaximum);

        // Create image to paint draw gradient with current transformations
        BufferedImage paintImage = new BufferedImage(wImage, hImage,
                BufferedImage.TYPE_INT_ARGB);
        // Paint shape
        Graphics2D g = (Graphics2D) paintImage.getGraphics();
        g.scale(wImage / shapeBounds.getWidth(),
                hImage / shapeBounds.getHeight());
        g.translate(-shapeBounds.getX(), -shapeBounds.getY());
        g.setPaint(getPaint());
        g.fill(s);
        // Free resources
        g.dispose();

        // Output image of gradient
        writeImage(paintImage, wImage, hImage, shapeBounds.getX(),
                shapeBounds.getY(), shapeBounds.getWidth(),
                shapeBounds.getHeight());
    }

    /**
     * Write the header to start a new document.
     */
    protected abstract void writeHeader();

    /**
     * Returns a string of the footer to end a document.
     */
    protected abstract String getFooter();

    /**
     * Returns whether a distorting transformation has been applied to the
     * document.
     *
     * @return {@code true} if the document is distorted, otherwise
     *         {@code false}.
     */
    protected boolean isDistorted() {
        if (!isTransformed()) {
            return false;
        }
        int type = transform.getType();
        int otherButTranslatedOrScaled = ~(AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_MASK_SCALE);
        return (type & otherButTranslatedOrScaled) != 0;
    }

    @Override
    public String toString() {
        return document.toString() + getFooter();
    }

    protected StringBuffer getDocument() {
        return document;
    }

    /**
     * Encodes the painted data into a sequence of bytes.
     *
     * @return A byte array containing the data in the current file format.
     */
    public byte[] getBytes() {
        try {
            return toString().getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return toString().getBytes();
        }
    }

    /**
     * Returns the dimensions of the document.
     *
     * @return dimensions of the document.
     */
    public Rectangle2D getBounds() {
        Rectangle2D b = new Rectangle2D.Double();
        b.setFrame(bounds);
        return b;
    }

    /**
     * Returns the number of bytes of the document.
     *
     * @return size of the document in bytes.
     */
    protected int size() {
        return document.length();
    }

    /**
     * Returns how fonts should be rendered.
     *
     * @return Font rendering mode.
     */
    public FontRendering getFontRendering() {
        return fontRendering;
    }

    /**
     * Sets how fonts should be rendered. For example, they can be converted to
     * vector shapes.
     *
     * @param mode
     *            New font rendering mode.
     */
    public void setFontRendering(FontRendering mode) {
        fontRendering = mode;
    }

    /**
     * Returns whether an affine transformation like translation, scaling,
     * rotation or shearing has been applied to this graphics instance.
     *
     * @return {@code true} if the instance has been transformed, {@code false}
     *         otherwise
     */
    protected boolean isTransformed() {
        return transformed;
    }

    /**
     * Returns the resolution in pixels per inch.
     *
     * @return Resolution in pixels per inch.
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution in pixels per inch.
     *
     * @param resolution
     *            New resolution in pixels per inch.
     */
    public void setResolution(double resolution) {
        if (resolution <= 0.0) {
            throw new IllegalArgumentException(
                    "Only positive non-zero values allowed"); //$NON-NLS-1$
        }
        this.resolution = resolution;
    }

    /**
     * Returns the maximal size of images which are used to raster paints like
     * e.g. gradients, or patterns. The default value is 128.
     *
     * @return Current maximal image size in pixels.
     */
    public int getRasteredImageSizeMaximum() {
        return rasteredImageSizeMaximum;
    }

    /**
     * Sets the maximal size of images which are used to raster paints like e.g.
     * gradients, or patterns.
     *
     * @param paintImageSizeMaximum
     *            New maximal image size in pixels.
     */
    public void setRasteredImageSizeMaximum(int paintImageSizeMaximum) {
        this.rasteredImageSizeMaximum = paintImageSizeMaximum;
    }
}
