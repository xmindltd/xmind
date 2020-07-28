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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.xmind.de.erichseifert.vectorgraphics2d.util.DataUtils;
import org.xmind.de.erichseifert.vectorgraphics2d.util.GraphicsUtils;

/**
 * {@code Graphics2D} implementation that saves all operations to a string in
 * the <i>Portable Document Format</i> (PDF).
 *
 * @author Jason Wong
 */
public class PDFGraphics2D extends VectorGraphics2D {
    /** Prefix string for PDF font resource ids. */
    protected static final String FONT_RESOURCE_PREFIX = "F"; //$NON-NLS-1$
    /** Prefix string for PDF image resource ids. */
    protected static final String IMAGE_RESOURCE_PREFIX = "Im"; //$NON-NLS-1$
    /** Prefix string for PDF transparency resource ids. */
    protected static final String TRANSPARENCY_RESOURCE_PREFIX = "T"; //$NON-NLS-1$

    /**
     * Constant to convert values from millimeters to PostScript®/PDF units
     * (1/72th inch).
     */
    protected static final double MM_IN_UNITS = 72.0 / 25.4;

    /** Mapping of stroke endcap values from Java to PDF. */
    private static final Map<Integer, Integer> STROKE_ENDCAPS = DataUtils.map(
            new Integer[] { BasicStroke.CAP_BUTT, BasicStroke.CAP_ROUND,
                    BasicStroke.CAP_SQUARE }, new Integer[] { 0, 1, 2 });

    /** Mapping of line join values for path drawing from Java to PDF. */
    private static final Map<Integer, Integer> STROKE_LINEJOIN = DataUtils.map(
            new Integer[] { BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND,
                    BasicStroke.JOIN_BEVEL }, new Integer[] { 0, 1, 2 });

    /** Id of the current PDF object. */
    private int curObjId;
    /** Mapping from objects to file positions. */
    private final Map<Integer, Integer> objPositions;
    /** Mapping from transparency levels to transparency resource ids. */
    private final Map<Double, String> transpResources;
    /** Mapping from image data to image resource ids. */
    private final Map<BufferedImage, String> imageResources;
    /** Mapping from font objects to font resource ids. */
    private final Map<Font, String> fontResources;
    /** File position of the actual content. */
    private int contentStart;

    private double scale;
    private double scaledOffsetX;
    private double scaledOffsetY;

    /**
     * Constructor that initializes a new {@code PDFGraphics2D} instance. The
     * document dimension must be specified as parameters.
     */
    public PDFGraphics2D(double x, double y, double width, double height,
            double scale, double scaledOffsetX, double scaledOffsetY) {
        super(x, y, width, height);
        this.scale = scale;
        this.scaledOffsetX = Math.round(scaledOffsetX * 100) / 100.0;
        this.scaledOffsetY = Math.round(scaledOffsetY * 100) / 100.0;
        curObjId = 1;
        objPositions = new TreeMap<Integer, Integer>();
        transpResources = new TreeMap<Double, String>();
        imageResources = new LinkedHashMap<BufferedImage, String>();
        fontResources = new LinkedHashMap<Font, String>();
        writeHeader();
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeString(String str, double x, double y) {
        // Escape string
        str = str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\t", "\\\\t")
                .replaceAll("\b", "\\\\b").replaceAll("\f", "\\\\f")
                .replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");

        float fontSize = getFont().getSize2D();
        float leading = getFont().getLineMetrics("", getFontRenderContext())
                .getLeading();

        // Start text and save current graphics state
        writeln("q BT");

        String fontResourceId = getFontResource(getFont());
        writeln("/", fontResourceId, " ", fontSize, " Tf");
        // Set leading
        writeln(fontSize + leading, " TL");

        // Undo swapping of y axis for text
        writeln("1 0 0 -1 ", x, " ", y, " cm");

        /*
         * // Extract lines String[] lines = str.replaceAll("\r\n",
         * "\n").replaceAll("\r", "\n").split("\n"); // Paint lines for (int i =
         * 0; i < lines.length; i++) { writeln("(", lines[i], ") ", (i == 0) ?
         * "Tj" : "'"); }
         */

        str = str.replaceAll("[\r\n]", "");
        writeln("(", str, ") Tj");

        // End text and restore previous graphics state
        writeln("ET Q");
    }

    @SuppressWarnings("nls")
    @Override
    public void setStroke(Stroke s) {
        super.setStroke(s);
        if (s instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke) s;
            writeln(bs.getLineWidth(), " w");
            writeln(STROKE_LINEJOIN.get(bs.getLineJoin()), " j");
            writeln(STROKE_ENDCAPS.get(bs.getEndCap()), " J");
            writeln("[", DataUtils.join(" ", bs.getDashArray()), "] ",
                    bs.getDashPhase(), " d");
        }
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeImage(Image img, int imgWidth, int imgHeight, double x,
            double y, double width, double height) {
        BufferedImage bufferedImg = GraphicsUtils.toBufferedImage(img);
        String imageResourceId = getImageResource(bufferedImg);
        // Save graphics state
        write("q ");
        // Take current transformations into account
        AffineTransform txCurrent = getTransform();
        if (!txCurrent.isIdentity()) {
            double[] matrix = new double[6];
            txCurrent.getMatrix(matrix);
            write(DataUtils.join(" ", matrix), " cm ");
        }
        // Move image to correct position and scale it to (width, height)
        write(width, " 0 0 ", height, " ", x - 2, " ", y - 2, " cm ");

        // Swap y axis
        write("1 0 0 -1 0 1 cm ");
        // Draw image
        write("/", imageResourceId, " Do ");
        // Restore old graphics state
        writeln("Q");
    }

    @SuppressWarnings("nls")
    @Override
    public void setColor(Color c) {
        if (c != null) {
            super.setColor(c);
            // Add a new graphics state to resources
            double a = c.getAlpha() / 255.0;
            String transpResourceId = getTransparencyResource(a);
            writeln("/", transpResourceId, " gs");
            double r = c.getRed() / 255.0;
            double g = c.getGreen() / 255.0;
            double b = c.getBlue() / 255.0;
            write(r, " ", g, " ", b, " rg ");
            writeln(r, " ", g, " ", b, " RG");
        }
    }

    @Override
    public void setClip(Shape clip) {
        if (getClip() != null) {
            writeln("Q");//$NON-NLS-1$
        }
        super.setClip(clip);
        if (getClip() != null) {
            writeln("q");//$NON-NLS-1$
            writeShape(getClip());
            writeln(" W n");//$NON-NLS-1$
        }
    }

    // TODO Correct transformations
    /*
     * @Override protected void setAffineTransform(AffineTransform tx) { if
     * (getTransform().equals(tx)) { return; } // Undo previous transforms if
     * (isTransformed()) { writeln("Q"); } // Set new transform
     * super.setAffineTransform(tx); // Write transform to document if
     * (isTransformed()) { writeln("q"); double[] matrix = new double[6];
     * getTransform().getMatrix(matrix); writeln(DataUtils.join(" ", matrix),
     * " cm"); } } //
     */

    @SuppressWarnings("nls")
    @Override
    protected void writeHeader() {
        Rectangle2D bounds = getBounds();
        int x = (int) Math.floor(bounds.getX());// * MM_IN_UNITS);
        int y = (int) Math.floor(bounds.getY());// * MM_IN_UNITS);
        int w = (int) Math.ceil(bounds.getWidth());// * MM_IN_UNITS);
        int h = (int) Math.ceil(bounds.getHeight());// * MM_IN_UNITS);

        writeln("%PDF-1.4");
        // Object 1
        writeObj("Type", "/Catalog", "Pages", "2 0 R");
        // Object 2
        writeObj("Type", "/Pages", "Kids", "[3 0 R]", "Count", "1");
        // Object 3
        writeObj("Type", "/Page", "Parent", "2 0 R", "MediaBox",
                String.format("[%d %d %d %d]", x, y, w, h), "Contents",
                "4 0 R", "Resources", "6 0 R");
        // Object 5
        writeln(nextObjId(size()), " 0 obj");
        writeDict("Length", "5 0 R");
        writeln("stream");
        contentStart = size();
        writeln("q");
        // Adjust page size and page origin
        writeln(scale, " 0 0 ", -scale, " " + scaledOffsetX + " ", h
                - scaledOffsetY, " cm");
    }

    /**
     * Write a PDF dictionary from the specified collection of objects. The
     * passed objects are converted to strings. Every object with odd position
     * is used as key, every object with even position is used as value.
     *
     * @param strs
     *            Objects to be written to dictionary
     */
    @SuppressWarnings("nls")
    protected void writeDict(Object... strs) {
        writeln("<<");
        for (int i = 0; i < strs.length; i += 2) {
            writeln("/", strs[i], " ", strs[i + 1]);
        }
        writeln(">>");
    }

    /**
     * Write a collection of elements to the document stream as PDF object. The
     * passed objects are converted to strings.
     *
     * @param strs
     *            Objects to be written to the document stream.
     * @return Id of the PDF object that was written.
     */
    protected int writeObj(Object... strs) {
        int objId = nextObjId(size());
        writeln(objId, " 0 obj"); //$NON-NLS-1$
        writeDict(strs);
        writeln("endobj"); //$NON-NLS-1$
        return objId;
    }

    /**
     * Returns the next PDF object id without incrementing.
     *
     * @return Next PDF object id.
     */
    protected int peekObjId() {
        return curObjId + 1;
    }

    /**
     * Returns a new PDF object id with every call.
     *
     * @param position
     *            File position of the object.
     * @return A new PDF object id.
     */
    private int nextObjId(int position) {
        objPositions.put(curObjId, position);
        return curObjId++;
    }

    /**
     * Returns the resource for the specified transparency level.
     *
     * @param a
     *            Transparency level.
     * @return A new PDF object id.
     */
    protected String getTransparencyResource(double a) {
        String name = transpResources.get(a);
        if (name == null) {
            name = String.format("%s%d", TRANSPARENCY_RESOURCE_PREFIX, //$NON-NLS-1$
                    transpResources.size() + 1);
            transpResources.put(a, name);
        }
        return name;
    }

    /**
     * Returns the resource for the specified image data.
     *
     * @param bufferedImg
     *            Image object with data.
     * @return A new PDF object id.
     */
    protected String getImageResource(BufferedImage bufferedImg) {
        String name = imageResources.get(bufferedImg);
        if (name == null) {
            name = String.format("%s%d", IMAGE_RESOURCE_PREFIX, //$NON-NLS-1$
                    imageResources.size() + 1);
            imageResources.put(bufferedImg, name);
        }
        return name;
    }

    /**
     * Returns the resource describing the specified font.
     *
     * @param font
     *            Font to be described.
     * @return A new PDF object id.
     */
    protected String getFontResource(Font font) {
        String name = fontResources.get(font);
        if (name == null) {
            name = String.format("%s%d", FONT_RESOURCE_PREFIX, //$NON-NLS-1$
                    fontResources.size() + 1);
            fontResources.put(font, name);
        }
        return name;
    }

    /**
     * Utility method for writing a tag closing fragment for drawing operations.
     */
    @Override
    protected void writeClosingDraw(Shape s) {
        writeln(" S"); //$NON-NLS-1$
    }

    /**
     * Utility method for writing a tag closing fragment for filling operations.
     */
    @Override
    protected void writeClosingFill(Shape s) {
        writeln(" f"); //$NON-NLS-1$
        if (!(getPaint() instanceof Color)) {
            super.writeClosingFill(s);
        }
    }

    /**
     * Utility method for writing an arbitrary shape to. It tries to translate
     * Java2D shapes to the corresponding PDF shape commands.
     */
    @SuppressWarnings("nls")
    @Override
    protected void writeShape(Shape s) {
        // TODO Correct transformations
        /*
         * if (s instanceof Line2D) { Line2D l = (Line2D) s; double x1 =
         * l.getX1(); double y1 = l.getY1(); double x2 = l.getX2(); double y2 =
         * l.getY2(); write(x1, " ", y1, " m ", x2, " ", y2, " l"); } else if (s
         * instanceof Rectangle2D) { Rectangle2D r = (Rectangle2D) s; double x =
         * r.getX(); double y = r.getY(); double width = r.getWidth(); double
         * height = r.getHeight(); write(x, " ", y, " ", width, " ", height,
         * " re"); } else //
         */
        {
            s = getTransform().createTransformedShape(s);
            PathIterator segments = s.getPathIterator(null);
            double[] coordsCur = new double[6];
            double[] pointPrev = new double[2];
            for (int i = 0; !segments.isDone(); i++, segments.next()) {
                if (i > 0) {
                    write(" ");
                }
                int segmentType = segments.currentSegment(coordsCur);
                switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                    write(coordsCur[0], " ", coordsCur[1], " m");
                    pointPrev[0] = coordsCur[0];
                    pointPrev[1] = coordsCur[1];
                    break;
                case PathIterator.SEG_LINETO:
                    write(coordsCur[0], " ", coordsCur[1], " l");
                    pointPrev[0] = coordsCur[0];
                    pointPrev[1] = coordsCur[1];
                    break;
                case PathIterator.SEG_CUBICTO:
                    write(coordsCur[0], " ", coordsCur[1], " ", coordsCur[2],
                            " ", coordsCur[3], " ", coordsCur[4], " ",
                            coordsCur[5], " c");
                    pointPrev[0] = coordsCur[4];
                    pointPrev[1] = coordsCur[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    double x1 = pointPrev[0] + 2.0 / 3.0
                            * (coordsCur[0] - pointPrev[0]);
                    double y1 = pointPrev[1] + 2.0 / 3.0
                            * (coordsCur[1] - pointPrev[1]);
                    double x2 = coordsCur[0] + 1.0 / 3.0
                            * (coordsCur[2] - coordsCur[0]);
                    double y2 = coordsCur[1] + 1.0 / 3.0
                            * (coordsCur[3] - coordsCur[1]);
                    double x3 = coordsCur[2];
                    double y3 = coordsCur[3];
                    write(x1, " ", y1, " ", x2, " ", y2, " ", x3, " ", y3, " c");
                    pointPrev[0] = x3;
                    pointPrev[1] = y3;
                    break;
                case PathIterator.SEG_CLOSE:
                    write("h");
                    break;
                default:
                    throw new IllegalStateException("Unknown path operation.");
                }
            }
        }
    }

    /**
     * Returns a string which represents the data of the specified image.
     *
     * @param bufferedImg
     *            Image to convert.
     * @return String representation of image in PDF hexadecimal format.
     */
    private String getPdf(BufferedImage bufferedImg) {
        int width = bufferedImg.getWidth();
        int height = bufferedImg.getHeight();
        int bands = bufferedImg.getSampleModel().getNumBands();
        StringBuffer str = new StringBuffer(width * height * bands * 2);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bufferedImg.getRGB(x, y) & 0xffffff;
                if (bands >= 3) {
                    String hex = String.format("%06x", pixel); //$NON-NLS-1$
                    str.append(hex);
                } else if (bands == 1) {
                    str.append(String.format("%02x", pixel)); //$NON-NLS-1$
                }
            }
            str.append('\n');
        }
        return str.append('>').toString();
    }

    @SuppressWarnings("nls")
    @Override
    protected String getFooter() {
        StringBuffer footer = new StringBuffer();
        // TODO Correct transformations
        /*
         * if (isTransformed()) { footer.append("Q\n"); }
         */
        if (getClip() != null) {
            footer.append("Q\n"); //$NON-NLS-1$
        }
        footer.append("Q"); //$NON-NLS-1$
        int contentEnd = size() + footer.length();
        footer.append('\n');
        footer.append("endstream\n"); //$NON-NLS-1$
        footer.append("endobj\n");

        int lenObjId = nextObjId(size() + footer.length());
        footer.append(lenObjId).append(" 0 obj\n");
        footer.append(contentEnd - contentStart).append('\n');
        footer.append("endobj\n");

        int resourcesObjId = nextObjId(size() + footer.length());
        footer.append(resourcesObjId).append(" 0 obj\n");
        footer.append("<<\n");
        footer.append(" /ProcSet [/PDF /Text /ImageB /ImageC /ImageI]\n");

        // Add resources for fonts
        if (!fontResources.isEmpty()) {
            footer.append(" /Font <<\n");
            for (Map.Entry<Font, String> entry : fontResources.entrySet()) {
                Font font = entry.getKey();
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId)
                        .append(" << /Type /Font").append(" /Subtype /")
                        .append("TrueType").append(" /BaseFont /")
                        .append(font.getPSName()).append(" >>\n");
            }
            footer.append(" >>\n");
        }

        // Add resources for images
        if (!imageResources.isEmpty()) {
            footer.append(" /XObject <<\n");

            int objIdOffset = 0;
            for (Map.Entry<BufferedImage, String> entry : imageResources
                    .entrySet()) {
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId).append(' ')
                        .append(curObjId + objIdOffset).append(" 0 R\n");
                objIdOffset++;
            }
            footer.append(" >>\n");
        }

        // Add resources for transparency levels
        if (!transpResources.isEmpty()) {
            footer.append(" /ExtGState <<\n");
            for (Map.Entry<Double, String> entry : transpResources.entrySet()) {
                Double alpha = entry.getKey();
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId)
                        .append(" << /Type /ExtGState").append(" /ca ")
                        .append(alpha).append(" /CA ").append(alpha)
                        .append(" >>\n");
            }
            footer.append(" >>\n");
        }

        footer.append(">>\n");
        footer.append("endobj\n");

        // Add data of images
        for (BufferedImage image : imageResources.keySet()) {
            int imageObjId = nextObjId(size() + footer.length());
            footer.append(imageObjId).append(" 0 obj\n");
            footer.append("<<\n");
            String imageData = getPdf(image);
            footer.append("/Type /XObject\n").append("/Subtype /Image\n")
                    .append("/Width ").append(image.getWidth()).append('\n')
                    .append("/Height ").append(image.getHeight()).append('\n')
                    .append("/ColorSpace /DeviceRGB\n")
                    .append("/BitsPerComponent 8\n").append("/Length ")
                    .append(imageData.length()).append('\n')
                    .append("/Filter /ASCIIHexDecode\n").append(">>\n")
                    .append("stream\n").append(imageData)
                    .append("\nendstream\n").append("endobj\n");
        }

        int objs = objPositions.size() + 1;

        int xrefPos = size() + footer.length();
        footer.append("xref\n");
        footer.append("0 ").append(objs).append('\n');
        // lines of xref entries must must be exactly 20 bytes long
        // (including line break) and thus end with <SPACE NEWLINE>
        footer.append(String.format("%010d %05d", 0, 65535)).append(" f \n");
        for (int pos : objPositions.values()) {
            footer.append(String.format("%010d %05d", pos, 0)).append(" n \n");
        }

        footer.append("trailer\n");
        footer.append("<<\n");
        footer.append("/Size ").append(objs).append('\n');
        footer.append("/Root 1 0 R\n");
        footer.append(">>\n");
        footer.append("startxref\n");
        footer.append(xrefPos).append('\n');

        footer.append("%%EOF\n");
        return footer.toString();
    }

    @SuppressWarnings("nls")
    public void writeFooter(Writer out) throws IOException {
        StringBuffer footer = new StringBuffer();

        if (getClip() != null) {
            footer.append("Q\n");
        }
        footer.append("Q");
        int contentEnd = size() + footer.length();
        footer.append('\n');
        footer.append("endstream\n");
        footer.append("endobj\n");

        int lenObjId = nextObjId(size() + footer.length());
        footer.append(lenObjId).append(" 0 obj\n");
        footer.append(contentEnd - contentStart).append('\n');
        footer.append("endobj\n");

        int resourcesObjId = nextObjId(size() + footer.length());
        footer.append(resourcesObjId).append(" 0 obj\n");
        footer.append("<<\n");
        footer.append(" /ProcSet [/PDF /Text /ImageB /ImageC /ImageI]\n");
        out.write(footer.toString());
        footer = new StringBuffer();

        // Add resources for fonts
        if (!fontResources.isEmpty()) {
            footer.append(" /Font <<\n");
            for (Map.Entry<Font, String> entry : fontResources.entrySet()) {
                Font font = entry.getKey();
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId)
                        .append(" << /Type /Font").append(" /Subtype /")
                        .append("TrueType").append(" /BaseFont /")
                        .append(font.getPSName()).append(" >>\n");
            }
            footer.append(" >>\n");
        }

        // Add resources for images
        if (!imageResources.isEmpty()) {
            footer.append(" /XObject <<\n");
            int objIdOffset = 0;
            for (Map.Entry<BufferedImage, String> entry : imageResources
                    .entrySet()) {
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId).append(' ')
                        .append(curObjId + objIdOffset).append(" 0 R\n");
                objIdOffset++;
            }
            footer.append(" >>\n");
        }

        // Add resources for transparency levels
        if (!transpResources.isEmpty()) {
            footer.append(" /ExtGState <<\n");
            for (Map.Entry<Double, String> entry : transpResources.entrySet()) {
                Double alpha = entry.getKey();
                String resourceId = entry.getValue();
                footer.append("  /").append(resourceId)
                        .append(" << /Type /ExtGState").append(" /ca ")
                        .append(alpha).append(" /CA ").append(alpha)
                        .append(" >>\n");
            }
            footer.append(" >>\n");
        }

        footer.append(">>\n");
        footer.append("endobj\n");
        out.write(footer.toString());
        footer = new StringBuffer();

        // Add data of images
        for (BufferedImage image : imageResources.keySet()) {
            int imageObjId = nextObjId(size() + footer.length());
            footer.append(imageObjId).append(" 0 obj\n");
            footer.append("<<\n");
            String imageData = getPdf(image);
            footer.append("/Type /XObject\n").append("/Subtype /Image\n")
                    .append("/Width ").append(image.getWidth()).append('\n')
                    .append("/Height ").append(image.getHeight()).append('\n')
                    .append("/ColorSpace /DeviceRGB\n")
                    .append("/BitsPerComponent 8\n").append("/Length ")
                    .append(imageData.length()).append('\n')
                    .append("/Filter /ASCIIHexDecode\n").append(">>\n")
                    .append("stream\n");
            out.write(footer.toString());
            out.write(imageData);
            out.write("\nendstream\n");
            out.write("endobj\n");
            footer = new StringBuffer();
        }

        int objs = objPositions.size() + 1;

        int xrefPos = size() + footer.length();
        footer.append("xref\n");
        footer.append("0 ").append(objs).append('\n');
        // lines of xref entries must must be exactly 20 bytes long
        // (including line break) and thus end with <SPACE NEWLINE>
        footer.append(String.format("%010d %05d", 0, 65535)).append(" f \n");
        for (int pos : objPositions.values()) {
            footer.append(String.format("%010d %05d", pos, 0)).append(" n \n");
        }

        footer.append("trailer\n");
        footer.append("<<\n");
        footer.append("/Size ").append(objs).append('\n');
        footer.append("/Root 1 0 R\n");
        footer.append(">>\n");
        footer.append("startxref\n");
        footer.append(xrefPos).append('\n');

        footer.append("%%EOF\n");
        out.write(footer.toString());
    }

    @Override
    public String toString() {
        // String doc = super.toString();
        String doc = super.getDocument().toString();
        // doc =
        // doc.replaceAll("q\n[0-9]+\\.?[0-9]* [0-9]+\\.?[0-9]* [0-9]+\\.?[0-9]* [0-9]+\\.?[0-9]* [0-9]+\\.?[0-9]* [0-9]+\\.?[0-9]* cm\nQ\n",
        // "");
        return doc;
    }

    @Override
    public byte[] getBytes() {
        try {
            return toString().getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return super.getBytes();
        }
    }
}
