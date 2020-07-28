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
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.xmind.de.erichseifert.vectorgraphics2d.util.DataUtils;
import org.xmind.de.erichseifert.vectorgraphics2d.util.GraphicsUtils;

/**
 * {@code Graphics2D} implementation that saves all operations to a string in
 * the <i>Scaled Vector Graphics</i> (SVG) format.
 *
 * @author Jason Wong
 */
public class SVGGraphics2D extends VectorGraphics2D {
    /** Mapping of stroke endcap values from Java to SVG. */
    private static final Map<Integer, String> STROKE_ENDCAPS = DataUtils.map(
            new Integer[] { BasicStroke.CAP_BUTT, BasicStroke.CAP_ROUND,
                    BasicStroke.CAP_SQUARE }, new String[] { "butt", "round", //$NON-NLS-1$ //$NON-NLS-2$
                    "square" }); //$NON-NLS-1$

    /** Mapping of line join values for path drawing from Java to SVG. */
    private static final Map<Integer, String> STROKE_LINEJOIN = DataUtils.map(
            new Integer[] { BasicStroke.JOIN_MITER, BasicStroke.JOIN_ROUND,
                    BasicStroke.JOIN_BEVEL }, new String[] { "miter", "round",//$NON-NLS-1$ //$NON-NLS-2$
                    "bevel" });//$NON-NLS-1$

    /** Prefix string for ids of clipping paths. */
    private static final String CLIP_PATH_ID = "clip"; //$NON-NLS-1$
    /** Number of the current clipping path. */
    private long clipCounter;

    /**
     * Constructor that initializes a new {@code SVGGraphics2D} instance. The
     * document dimension must be specified as parameters.
     *
     * @param x
     *            Left offset of document.
     * @param y
     *            Top offset of document.
     * @param width
     *            Width of document.
     * @param height
     *            Height of document.
     */
    public SVGGraphics2D(double x, double y, double width, double height) {
        super(x, y, width, height);
        writeHeader();
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeString(String str, double x, double y) {
        // Escape string
        str = str.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        float fontSize = getFont().getSize2D();
        // float leading = getFont().getLineMetrics("",
        // getFontRenderContext()).getLeading();

        /*
         * // Extract lines String[] lines = str.replaceAll("\r\n",
         * "\n").replaceAll("\r", "\n").split("\n");
         *
         * // Output lines writeln("<text style=\"font:", fontSize, "px ",
         * getFont().getFamily(), "\">"); for (int i = 0; i < lines.length; i++)
         * { String line = lines[i]; writeln(" <tspan x=\"", x, "\" y=\"", y +
         * i*fontSize + ((i>0) ? leading : 0f), "\">", line, "</tspan>"); }
         * writeln("</text>");
         */

        str = str.replaceAll("[\r\n]", "");
        writeln("<text x=\"", x, "\" y=\"", y, "\" style=\"font:", fontSize,
                "px ", getFont().getFamily(), "\">", str, "</text>");
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeImage(Image img, int imgWidth, int imgHeight, double x,
            double y, double width, double height) {
        BufferedImage bufferedImg = GraphicsUtils.toBufferedImage(img);
        String imgData = getSvg(bufferedImg);
        write("<image x=\"", x, "\" y=\"", y, "\" ", "width=\"", width,
                "\" height=\"", height, "\" ", "xlink:href=\"", imgData, "\" ",
                "/>"); //$NON-NLS-1$
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D s = new Path2D.Double(Path2D.WIND_NON_ZERO, xPoints.length);
        write("<polygon points=\""); //$NON-NLS-1$
        for (int i = 0; i < nPoints; i++) {
            if (i == 0) {
                s.moveTo(xPoints[i], yPoints[i]);
            } else {
                s.lineTo(xPoints[i], yPoints[i]);
                write(" "); //$NON-NLS-1$
            }
            write(xPoints[i], ",", yPoints[i]); //$NON-NLS-1$
        }
        write("\" "); //$NON-NLS-1$
        s.closePath();
        writeClosingDraw(s);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D s = new Path2D.Double(Path2D.WIND_NON_ZERO, xPoints.length);
        write("<polyline points=\""); //$NON-NLS-1$
        for (int i = 0; i < nPoints; i++) {
            if (i == 0) {
                s.moveTo(xPoints[i], yPoints[i]);
            } else {
                s.lineTo(xPoints[i], yPoints[i]);
                write(" "); //$NON-NLS-1$
            }
            write(xPoints[i], ",", yPoints[i]); //$NON-NLS-1$
        }
        write("\" "); //$NON-NLS-1$
        writeClosingDraw(s);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path2D s = new Path2D.Double(Path2D.WIND_NON_ZERO, xPoints.length);
        write("<polygon points=\""); //$NON-NLS-1$
        for (int i = 0; i < nPoints; i++) {
            if (i == 0) {
                s.moveTo(xPoints[i], yPoints[i]);
            } else {
                s.lineTo(xPoints[i], yPoints[i]);
                write(" "); //$NON-NLS-1$
            }
            write(xPoints[i], ",", yPoints[i]); //$NON-NLS-1$
        }
        write("\" "); //$NON-NLS-1$
        s.closePath();
        writeClosingFill(s);
    }

    @Override
    protected void setAffineTransform(AffineTransform tx) {
        if (getTransform().equals(tx)) {
            return;
        }
        // Close previous transformation group
        if (isTransformed()) {
            writeln("</g>"); //$NON-NLS-1$
        }
        // Set transformation matrix
        super.setAffineTransform(tx);
        // Begin new transformation group
        if (isTransformed()) {
            double[] matrix = new double[6];
            getTransform().getMatrix(matrix);
            writeln("<g transform=\"matrix(", DataUtils.join(" ", matrix), //$NON-NLS-1$ //$NON-NLS-2$
                    ") \">"); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("nls")
    @Override
    protected void writeHeader() {
        Rectangle2D bounds = getBounds();
        double x = bounds.getX();
        double y = bounds.getY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writeln("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" ",
                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
        writeln("<svg version=\"1.2\" xmlns=\"http://www.w3.org/2000/svg\" ",
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" ", "x=\"", x,
                "\" y=\"", y, "\" ", "width=\"", w, "\" height=\"", h, "\" "
                        + "viewBox=\"", x, " ", y, " ", w, " ", h, "\"", ">");
        writeln("<style type=\"text/css\"><![CDATA[");
        writeln("text { font:", getFont().getSize2D(), "px ", getFont()
                .getFamily(), "; }");
        writeln("]]></style>");
    }

    @Override
    protected void writeClosingDraw(Shape s) {
        write("style=\"fill:none;stroke:", getSvg(getColor())); //$NON-NLS-1$
        if (getStroke() instanceof BasicStroke) {
            BasicStroke stroke = (BasicStroke) getStroke();
            if (stroke.getLineWidth() != 1f) {
                write(";stroke-width:", stroke.getLineWidth()); //$NON-NLS-1$
            }
            if (stroke.getEndCap() != BasicStroke.CAP_BUTT) {
                write(";stroke-linecap:", //$NON-NLS-1$
                        STROKE_ENDCAPS.get(stroke.getEndCap()));
            }
            if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
                write(";stroke-linejoin:", //$NON-NLS-1$
                        STROKE_LINEJOIN.get(stroke.getLineJoin()));
            }
            // write(";stroke-miterlimit:", s.getMiterLimit());
            if (stroke.getDashArray() != null
                    && stroke.getDashArray().length > 0) {
                write(";stroke-dasharray:", //$NON-NLS-1$
                        DataUtils.join(",", stroke.getDashArray())); //$NON-NLS-1$
                write(";stroke-dashoffset:", stroke.getDashPhase()); //$NON-NLS-1$
            }
        }
        if (getClip() != null) {
            write("\" clip-path=\"url(#", getClipId(), ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        writeln("\" />"); //$NON-NLS-1$
    }

    @Override
    protected void writeClosingFill(Shape s) {
        if (getPaint() instanceof Color) {
            write("style=\"fill:", getSvg(getColor()), ";stroke:none"); //$NON-NLS-1$ //$NON-NLS-2$
            if (getClip() != null) {
                write("\" clip-path=\"url(#", getClipId(), ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            writeln("\" />"); //$NON-NLS-1$
        } else {
            write("style=\"stroke:none\" />"); //$NON-NLS-1$
            super.writeClosingFill(s);
        }
    }

    @Override
    protected void writeShape(Shape s) {
        writeClip();
        writeUnclippedShape(s);
    }

    /**
     * Returns the id of the current clipping path.
     *
     * @return id string of the current clipping path.
     */
    private String getClipId() {
        return CLIP_PATH_ID + clipCounter;
    }

    /**
     * Generates a new id for a clipping path.
     *
     * @return id string of the next clipping path.
     */
    private String nextClipId() {
        clipCounter++;
        return getClipId();
    }

    /**
     * Writes the current clipping path.
     */
    private void writeClip() {
        Shape clip = getClip();
        if (clip == null) {
            return;
        }
        write("<clipPath id=\"", nextClipId(), "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        writeUnclippedShape(clip);
        write("/>"); //$NON-NLS-1$
        writeln("</clipPath>"); //$NON-NLS-1$
    }

    /**
     * Writes the specified shape without clipping path information.
     *
     * @param s
     *            Shape to be written.
     */
    @SuppressWarnings("nls")
    private void writeUnclippedShape(Shape s) {
        if (s instanceof Line2D) {
            Line2D l = (Line2D) s;
            double x1 = l.getX1();
            double y1 = l.getY1();
            double x2 = l.getX2();
            double y2 = l.getY2();
            write("<line x1=\"", x1, "\" y1=\"", y1, "\" x2=\"", x2,
                    "\" y2=\"", y2, "\" ");
        } else if (s instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D) s;
            double x = r.getX();
            double y = r.getY();
            double width = r.getWidth();
            double height = r.getHeight();
            write("<rect x=\"", x, "\" y=\"", y, "\" width=\"", width,
                    "\" height=\"", height, "\" ");
        } else if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            double x = r.getX();
            double y = r.getY();
            double width = r.getWidth();
            double height = r.getHeight();
            double arcWidth = r.getArcWidth();
            double arcHeight = r.getArcHeight();
            write("<rect x=\"", x, "\" y=\"", y, "\" width=\"", width,
                    "\" height=\"", height, "\" rx=\"", arcWidth, "\" ry=\"",
                    arcHeight, "\" "); //$NON-NLS-1$
        } else if (s instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) s;
            double x = e.getX();
            double y = e.getY();
            double rx = e.getWidth() / 2.0;
            double ry = e.getHeight() / 2.0;
            write("<ellipse cx=\"", x + rx, "\" cy=\"", y + ry, "\" rx=\"", rx,
                    "\" ry=\"", ry, "\" ");
        } else {
            write("<path d=\"");
            writePath(s);
            write("\" ");
        }
    }

    /**
     * Writes the beginning of the specified shape without any closing commands.
     *
     * @param s
     *            Shape to be written.
     */
    @SuppressWarnings("nls")
    protected void writePath(Shape s) {
        PathIterator segments = s.getPathIterator(null);
        double[] coords = new double[6];
        for (int i = 0; !segments.isDone(); i++, segments.next()) {
            if (i > 0) {
                write(" ");
            }
            int segmentType = segments.currentSegment(coords);
            switch (segmentType) {
            case PathIterator.SEG_MOVETO:
                write("M", coords[0], ",", coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                write("L", coords[0], ",", coords[1]);
                break;
            case PathIterator.SEG_CUBICTO:
                write("C", coords[0], ",", coords[1], " ", coords[2], ",",
                        coords[3], " ", coords[4], ",", coords[5]);
                break;
            case PathIterator.SEG_QUADTO:
                write("Q", coords[0], ",", coords[1], " ", coords[2], ",",
                        coords[3]);
                break;
            case PathIterator.SEG_CLOSE:
                write("Z");
                break;
            default:
                throw new IllegalStateException("Unknown path operation.");
            }
        }
    }

    /**
     * Converts a {@code Color} object to an SVG color statement.
     *
     * @param c
     *            Color object.
     * @return String representation in SVG compatible format.
     */
    private static String getSvg(Color c) {
        String color = "rgb(" + c.getRed() + "," + c.getGreen() + "," //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + c.getBlue() + ")"; //$NON-NLS-1$
        if (c.getAlpha() < 255) {
            double opacity = c.getAlpha() / 255.0;
            color += ";opacity:" + opacity; //$NON-NLS-1$
        }
        return color;
    }

    /**
     * Converts a {@code BufferedImage} object to an SVG base64 encoded string.
     *
     * @param bufferedImg
     *            Image object.
     * @return String representation in SVG base64 format.
     */
    private static String getSvg(BufferedImage bufferedImg) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImg, "png", data); //$NON-NLS-1$
        } catch (IOException e) {
            return ""; //$NON-NLS-1$
        }
        String dataBase64 = DatatypeConverter.printBase64Binary(data
                .toByteArray());
        return "data:image/png;base64," + dataBase64; //$NON-NLS-1$
    }

    @Override
    protected String getFooter() {
        String footer = ""; //$NON-NLS-1$
        // Close any previous transformation groups
        if (isTransformed()) {
            footer += "</g>\n"; //$NON-NLS-1$
        }
        footer += "</svg>\n"; //$NON-NLS-1$
        return footer;
    }

    @Override
    public String toString() {
        String doc = super.toString();
        doc = doc.replaceAll("<g transform=\"[^\"]*\">\n*</g>\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
        return doc;
    }

}
