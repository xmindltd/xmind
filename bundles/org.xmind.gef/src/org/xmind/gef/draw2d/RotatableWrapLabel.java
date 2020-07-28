/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.gef.draw2d;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.util.GEFUtils;

import com.ibm.icu.text.BreakIterator;

/**
 * @author Frank Shaka
 */
@SuppressWarnings("restriction")
public class RotatableWrapLabel extends Figure implements ITextFigure,
        IWrapFigure, IRotatableFigure, ITransparentableFigure {

    private static final int FLAG_SINGLE_LINE = MAX_FLAG << 1;
    private static final int FLAG_ABBREVIATED = MAX_FLAG << 2;

    static {
        MAX_FLAG = FLAG_ABBREVIATED;
    }

    private static final float PADDING = 1.5f;
    private static final float RIGHT_MARGIN = 1.0f;
    private static final float[] RECT = new float[4];
    private static final PrecisionDimension D = new PrecisionDimension();
    private static final PrecisionRectangle R = new PrecisionRectangle();

    protected static final Dimension NO_TEXT_SIZE = new Dimension(1, 10);

    public static final int NORMAL = 0;
    public static final int ADVANCED = 1;

    public static final String ELLIPSE = "..."; //$NON-NLS-1$

    /*
     * Infos:
     */
    private String text = ""; //$NON-NLS-1$

    private TextStyle style = null;

    private int align = PositionConstants.LEFT;

    private int textCase = GEF.MANUAL;

    private int renderStyle = NORMAL;

    private int lineSpacing = -1;

    private int textAlpha = 0xff;

    private int fillAlpha = 0xff;

    private int prefWidth = -1;

    /*
     * Caches:
     */
    private Dimension cachedPrefSize = null;
    private String appliedText = null;
    private PrecisionRectangle textArea = null;
    private PrecisionDimension nonRotatedPrefSize = null;
    private PrecisionInsets rotatedInsets = null;
    private int cachedWidthHint = -1;
    private boolean showTabToSpace = false;

    private PrecisionRotator rotator = new PrecisionRotator();

    /**
     * 
     */
    public RotatableWrapLabel() {
    }

    public RotatableWrapLabel(String text) {
        setText(text);
    }

    public RotatableWrapLabel(int renderStyle) {
        this.renderStyle = renderStyle;
    }

    public RotatableWrapLabel(String text, int renderStyle) {
        setText(text);
        this.renderStyle = renderStyle;
    }

    public int getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(int prefWidth) {
        if (prefWidth == this.prefWidth)
            return;
        this.prefWidth = prefWidth;
        revalidate();
        repaint();
    }

    public boolean isSingleLine() {
        return getFlag(FLAG_SINGLE_LINE);
    }

    public void setSingleLine(boolean singleLine) {
        if (singleLine == isSingleLine())
            return;
        setFlag(FLAG_SINGLE_LINE, singleLine);
        revalidate();
        repaint();
    }

    public boolean isAbbreviated() {
        return getFlag(FLAG_ABBREVIATED);
    }

    public void setAbbreviated(boolean abbreviated) {
        if (abbreviated == isAbbreviated())
            return;
        setFlag(FLAG_ABBREVIATED, abbreviated);
        revalidate();
        repaint();
    }

    public void setMainAlpha(int alpha) {
        if (alpha == this.textAlpha)
            return;
        this.textAlpha = alpha;
        repaint();
    }

    public int getMainAlpha() {
        return textAlpha;
    }

    public void setSubAlpha(int alpha) {
        if (alpha == this.fillAlpha)
            return;
        this.fillAlpha = alpha;
        repaint();
    }

    public int getSubAlpha() {
        return fillAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.ITextFigure#getStyle()
     */
    public TextStyle getStyle() {
        return style;
    }

    public int getRenderStyle() {
        return renderStyle;
    }

    /**
     * @return the text of this label figure (never be null)
     */
    public String getText() {
        return text;
    }

    public void setShowTabToSpace(boolean showTabToSpace) {
        this.showTabToSpace = showTabToSpace;
    }

    private String getShowText() {
        if (showTabToSpace) {
            return getText() == null ? null : getText().replace("\t", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return getText();
    }

    /**
     * @see org.xmind.gef.draw2d.ITextFigure#getTextAlignment()
     */
    public int getTextAlignment() {
        return align;
    }

    /**
     * @see org.xmind.gef.draw2d.ITextFigure#getTextCase()
     */
    public int getTextCase() {
        return textCase;
    }

    /**
     * Gets the angle in degrees by which the label is rotated.
     * 
     * @return the rotateAngle
     */
    public double getRotationDegrees() {
        return rotator.getAngle();
    }

    /**
     * Sets the angle in degrees by which the label is rotated.
     * 
     * @param degrees
     *            the rotateAngle to set
     */
    public void setRotationDegrees(double degrees) {
        double oldAngle = getRotationDegrees();
        rotator.setAngle(degrees);
        if (getBorder() instanceof IRotatable) {
            ((IRotatable) getBorder()).setRotationDegrees(degrees);
        }
        if (getLayoutManager() instanceof IRotatable) {
            ((IRotatable) getLayoutManager()).setRotationDegrees(degrees);
        }
        for (Object child : getChildren()) {
            if (child instanceof IRotatable) {
                ((IRotatable) child).setRotationDegrees(degrees);
            }
        }
        if (degrees != oldAngle) {
            revalidate();
            repaint();
        }
    }

//    public Rotator getRotator() {
//        return rotator.getRotator();
//    }
//
//    public void setRotator(Rotator rotator) {
//        this.rotator.setRotator(rotator);
//        setRotateAngle(rotator.getAngle());
//    }

    /**
     * @see org.xmind.gef.draw2d.ITextFigure#setStyle(org.eclipse.swt.graphics.TextStyle)
     */
    public void setStyle(TextStyle style) {
        if (GEFUtils.equals(this.style, style))
            return;
        this.style = style;
        if (style != null) {
            super.setFont(style.font);
            super.setForegroundColor(style.foreground);
        } else {
            super.setFont(null);
            super.setForegroundColor(null);
        }
        repaint();
    }

    @Override
    public void setFont(Font f) {
        Font old = getLocalFont();
        super.setFont(f);
        if (getLocalFont() != old) {
            if (style != null)
                style.font = f;
        }
    }

    @Override
    public void setForegroundColor(Color fg) {
        Color old = getLocalForegroundColor();
        super.setForegroundColor(fg);
        if (getLocalForegroundColor() != old) {
            if (style != null)
                style.foreground = fg;
        }
    }

    public void setRenderStyle(int style) {
        if (style == this.renderStyle || (style != NORMAL && style != ADVANCED))
            return;
        this.renderStyle = style;
        revalidate();
        repaint();
    }

    /**
     * @see org.xmind.gef.draw2d.ITextFigure#setText(java.lang.String)
     */
    public void setText(String text) {
        // "text" will never be null.
        if (text == null)
            text = ""; //$NON-NLS-1$
        String t = text.replaceAll("\\r\\n|\\r", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        if (this.text.equals(t))
            return;
        this.text = t;
        revalidate();
        repaint();
    }

    public void setTextAlignment(int align) {
        if (this.align == align)
            return;
        this.align = align;
        repaint();
    }

    public void setTextCase(int textCase) {
        if (this.textCase == textCase)
            return;
        this.textCase = textCase;
        revalidate();
        repaint();
    }

    protected void receiveWidthCaches(int wHint) {
        if (wHint != cachedWidthHint) {
            flushCaches();
        }
        cachedWidthHint = wHint;
    }

    protected void flushCaches() {
        cachedPrefSize = null;
        appliedText = null;
        textArea = null;
        nonRotatedPrefSize = null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        if (prefSize != null)
            return prefSize;
        if (prefWidth > 0) {
            wHint = Math.max(0, prefWidth - getInsets().getWidth());
        } else if (wHint > 0) {
            wHint = Math.max(0, wHint - getInsets().getWidth());
        }
        receiveWidthCaches(wHint);
        if (getShowText().length() == 0)
            return NO_TEXT_SIZE;
        if (cachedPrefSize == null) {
            cachedPrefSize = calculateRotatedPreferredSize(wHint, hHint)
                    .toBiggerDraw2DDimension();
            cachedPrefSize.union(getMinimumSize(wHint, hHint));
        }
        return cachedPrefSize;
    }

    protected PrecisionDimension calculateRotatedPreferredSize(int wHint,
            int hHint) {
        PrecisionDimension d = getNormalPreferredSize(wHint, hHint);
        return rotator.td(d);
    }

    public PrecisionDimension getNormalPreferredSize(int wHint, int hHint) {
        if (prefWidth > 0) {
            wHint = Math.max(0, prefWidth - getInsets().getWidth());
        } else if (wHint > 0) {
            wHint = Math.max(0, wHint - getInsets().getWidth());
        }
        receiveWidthCaches(wHint);
        if (nonRotatedPrefSize == null) {
            nonRotatedPrefSize = calculateNormalPreferredSize(wHint);
        }
        return nonRotatedPrefSize;
    }

    /**
     * @param hint
     * @param hint2
     * @return
     */
    protected PrecisionDimension calculateNormalPreferredSize(int wHint) {
        PrecisionDimension d = getTextArea(wHint).getSize();
        Insets insets = getInsets();
        d.expand(insets.getWidth(), insets.getHeight());
        return d;
    }

    public String getAppliedText() {
        return getAppliedText(cachedWidthHint);
    }

    protected String getAppliedText(int wHint) {
        receiveWidthCaches(wHint);
        if (appliedText == null) {
            appliedText = calculateAppliedText(wHint);
        }
        return appliedText;
    }

    /**
     * @param wHint
     * @return
     */
    protected String calculateAppliedText(double wHint) {
        String theText = getShowText();
        if (wHint < 0 || theText.length() == 0)
            return theText;

        Font f = getFont();
        if (isSingleLine()) {
            if (isAbbreviated())
                return getAbbreviatedText(theText, f, wHint);
            return theText;
        }

        String[] lines = forceSplit(theText);
        StringBuilder accumlatedText = new StringBuilder(theText.length() + 10);
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            if (line.length() == 0) {
                accumlatedText.append('\n');
            } else {
                StringBuilder remainingLine = new StringBuilder(line);
                int i = 0;
                while (remainingLine.length() > 0) {
                    i = getLineWrapPosition(remainingLine.toString(), f, wHint);
                    if (i == 0)
                        break;

                    String substring = trim(remainingLine.substring(0, i));
                    if (isAbbreviated()) {
                        substring = getAbbreviatedText(substring, f, wHint);
                    }
                    accumlatedText.append(substring);
                    remainingLine.delete(0, i);
                    accumlatedText.append('\n');
                }
            }
        }
        if (accumlatedText.charAt(accumlatedText.length() - 1) == '\n')
            accumlatedText.deleteCharAt(accumlatedText.length() - 1);
        return accumlatedText.toString();
    }

    private static String[] forceSplit(String s) {
        List<String> buffer = new ArrayList<String>(s.length());
        int start = 0;
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            if (c == '\n') {
                buffer.add(s.substring(start, end));
                start = end + 1;
            }
        }
        buffer.add(s.substring(start));
        return buffer.toArray(new String[buffer.size()]);
    }

    private String getAbbreviatedText(String theText, Font f, double wHint) {
        String result = theText;
        int textLength = result.length();

        if (wHint > 0 && textLength > 0) {
            int tructionPosition = textLength;

            while (true) {
                int textWidth = getLooseTextSize(result, f).width;
                if (textWidth > wHint) {
                    tructionPosition = (int) ((double) result.length()
                            / (double) (textWidth) * (int) wHint);

                    if (tructionPosition < textLength && tructionPosition > 0) {
                        result = result.substring(0, tructionPosition);
                        continue;
                    }
                }
                break;
            }

            if (tructionPosition < textLength) {
                if (tructionPosition > ELLIPSE.length()) {
                    tructionPosition -= ELLIPSE.length();
                }
                return result.substring(0, tructionPosition) + ELLIPSE;
            }
        }

        return result;
    }

    /**
     * returns the position of last character within the supplied text that will
     * fit within the supplied width.
     * 
     * @param s
     *            a text string
     * @param f
     *            font used to draw the text string
     * @param w
     *            width in pixels.
     */
    protected int getLineWrapPosition(String s, Font f, double w) {
        // create an iterator for line breaking positions
        BreakIterator iter = BreakIterator.getLineInstance();
        iter.setText(s);
        int start = iter.first();
        int end = iter.next();

        // if the first line segment does not fit in the width,
        // determine the position within it where we need to cut
        if (getSubTextSize(s, start, end, f).width > w) {
            iter = BreakIterator.getWordInstance(); // BreakIterator.getCharacterInstance();
            iter.setText(s);
            start = iter.first();

            // if the first word does not fit in the width,
            // just return the full length of the very word
            end = iter.next();
            if (end == BreakIterator.DONE)
                return iter.last();
            if (getSubTextSize(s, start, end, f).width > w)
                return end;
        }

        // keep iterating as long as width permits
        do
            end = iter.next();
        while (end != BreakIterator.DONE
                && getSubTextSize(s, start, end, f).width <= w);
        return (end == BreakIterator.DONE) ? iter.last() : iter.previous();
    }

    private Dimension getSubTextSize(String s, int start, int end, Font f) {
        String t = trim(s.substring(start, end));
        t = getShowText(t, getTextCase());
        Dimension size = getLooseTextSize(t, f);
        return size;
    }

    protected String getShowText(String t, int textCase) {
        switch (textCase) {
        case GEF.MANUAL:
            return t;
        case GEF.UPPERCASE:
            return t.toUpperCase();
        case GEF.LOWERCASE:
            return t.toLowerCase();
        case GEF.CAPITALIZE:
            return capitalize(t);
        }
        return t;
    }

    private String capitalize(String str) {
        StringBuffer stringbf = new StringBuffer();
//        Matcher m = Pattern
//                .compile("([^\\s])([^\\s]*)", Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
//                .matcher(str);
        Matcher m = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE) //$NON-NLS-1$
                .matcher(str);
        while (m.find()) {
            m.appendReplacement(stringbf,
                    m.group(1).toUpperCase() + m.group(2).toLowerCase());
        }
        return m.appendTail(stringbf).toString();
    }

    private static String trim(String s) {
        return s.replaceAll("\\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

//    protected PrecisionDimension getTightTextSize(String s, Font f) {
//        if (s.length() == 0) {
//            int height = GraphicsUtils.getAdvanced().getFontMetrics(f)
//                    .getHeight();
//            return new PrecisionDimension(0, height);
//        } else if (!isNormalRenderStyle()) {
//            Path textShape = new Path(Display.getCurrent());
//            textShape.addString(s, 0, 0, f);
//            textShape.getBounds(_bounds);
//            textShape.dispose();
//            return new PrecisionDimension(_bounds[2], _bounds[3]);
//        }
//        return getLooseTextSize(s, f);
//    }

    protected Dimension getLooseTextSize(String s, Font f) {
        if (s.length() == 0) {
            int height = GraphicsUtils.getAdvanced().getFontMetrics(f)
                    .getHeight();
            return new Dimension(0, height);
        }
        Dimension size = GraphicsUtils.getAdvanced().getTextSize(s, f);
        if (!isNormalRenderStyle()) {
            Path p = new Path(Display.getCurrent());
            p.addString(s, 0, 0, f);
            p.getBounds(RECT);
            if (Util.isWindows()) {
                float[] autoScaleDown = DPIUtil.autoScaleDown(RECT);
                RECT[0] = autoScaleDown[0];
                RECT[1] = autoScaleDown[1];
                RECT[2] = autoScaleDown[2];
                RECT[3] = autoScaleDown[3];
            }
            p.dispose();
            size.width = Math.max(size.width, (int) Math.ceil(RECT[2]));
            size.height = Math.max(size.height, (int) Math.ceil(RECT[3]));
        }
        return size;
    }

//    protected PrecisionDimension getPrecisionLooseTextSize(String s, Font f) {
//        return new PrecisionDimension(getLooseTextSize(s, f));
//    }

    protected PrecisionRectangle getTextArea() {
        return getTextArea(cachedWidthHint);
    }

    /**
     * Returns the area of the label's text.
     * 
     * @param wHint
     * @return the area of this label's text
     */
    protected PrecisionRectangle getTextArea(int wHint) {
        receiveWidthCaches(wHint);
        if (textArea == null) {
            PrecisionDimension size = calculateTextSize(wHint);
            textArea = new PrecisionRectangle();
            float rightMargin = RIGHT_MARGIN;
            int height = getFont().getFontData()[0].getHeight();
            if (height > 30)
                rightMargin = rightMargin + 5;
            textArea.width = size.width + PADDING * 2 + rightMargin;
            textArea.height = size.height + PADDING * 2;
            textArea.x = -(textArea.width / 2);
            textArea.y = -(textArea.height / 2);
        }
        return textArea;
    }

    /**
     * Calculates and returns the size of the Label's text.
     * 
     * @param wHint
     * @return the size of the label's text, ignoring truncation
     */
    protected PrecisionDimension calculateTextSize(int wHint) {
        Font f = getFont();
        String theText = getAppliedText(wHint);
        String[] split = forceSplit(theText);
        PrecisionDimension size = D.setSize(0, 0);
        final int textCase = getTextCase();
        for (String s : split) {
            String t = getShowText(s, textCase);
            Dimension d = getLooseTextSize(t, f);
            if (size.height > 0)
                size.height += getLineSpacing();
            size.height += d.height;
            size.width = Math.max(size.width, d.width);
        }
        //size.union(getTextExtents(theText, f));
        return size;
    }

    public int getLineSpacing() {
        if (lineSpacing < 0)
            lineSpacing = calculateDefaultLineSpacing();
        return lineSpacing;
    }

    public void setLineSpacing(int spacing) {
        if (this.lineSpacing >= 0 && spacing == this.lineSpacing)
            return;

        this.lineSpacing = spacing;
        revalidate();
        repaint();
    }

    protected int calculateDefaultLineSpacing() {
        Dimension s1 = getLooseTextSize("X\nX", getFont()); //$NON-NLS-1$
        Dimension s = getLooseTextSize("X", getFont()); //$NON-NLS-1$
        return (int) Math.max(0, s1.height - s.height * 2);
    }

    @Override
    public Insets getInsets() {
        if (isRotated())
            return getRotatedInsets().toDraw2DInsets();
        return super.getInsets();
    }

    protected PrecisionInsets getRotatedInsets() {
        if (rotatedInsets == null)
            rotatedInsets = calculateRotatedInsets();
        return rotatedInsets;
    }

    protected PrecisionInsets calculateRotatedInsets() {
        PrecisionInsets ins = new PrecisionInsets(super.getInsets());
        return isRotated() ? rotator.t(ins) : ins;
    }

    /**
     * @see org.eclipse.draw2d.Figure#invalidate()
     */
    @Override
    public void invalidate() {
        flushCaches();
        rotatedInsets = null;
        super.invalidate();
    }

    protected boolean isRotated() {
        return Math.abs(getRotationDegrees()) > 0.0000001;
    }

    @Override
    protected void paintBorder(Graphics graphics) {
        graphics.setAlpha(getSubAlpha());
        super.paintBorder(graphics);
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        graphics.setTextAntialias(SWT.ON);

        if (getBorder() instanceof AbstractBackground) {
            int oldAlpha = graphics.getAlpha();
            graphics.setAlpha(getSubAlpha());
            ((AbstractBackground) getBorder()).paintBackground(this, graphics,
                    NO_INSETS);
            graphics.setAlpha(oldAlpha);
        }

        PrecisionPoint pCenter = calculateTextCenterLocation();
        Point center = null;
        boolean rotated = isRotated();

        Rectangle clientArea = getClientArea();

        double wHint = rotator
                .r(D.setSize(clientArea.width, clientArea.height)).width;
        PrecisionRectangle rect = new PrecisionRectangle(
                getTextArea((int) wHint));
        PrecisionDimension d = new PrecisionDimension(getSize());
        Insets ins = getInsets();
        double insWidth = ins.getWidth();
        double insHeight = ins.getHeight();
        if (rotated) {
            d = rotator.r(d, -1, rect.height + super.getInsets().getHeight());
            d.expand(-insWidth, -insHeight);
            rect.x -= (d.width - rect.width) / 2;
            rect.width = d.width;
            center = pCenter.toRoundedDraw2DPoint();
            rect.translate(new PrecisionPoint(center).getDifference(pCenter));
            graphics.translate(center);
            graphics.rotate((float) getRotationDegrees());
        } else {
            d.expand(-insWidth, -insHeight);
            rect.x -= (d.width - rect.width) / 2;
            rect.width = d.width;
            rect.translate(pCenter);
        }

        paintTextArea(graphics, rect);
        if (rotated && center != null) {
            graphics.translate(center.negate());
            graphics.rotate(-(float) getRotationDegrees());
        }
    }

    /**
     * @return
     */
    private PrecisionPoint calculateTextCenterLocation() {
        PrecisionRectangle rect = R.setBounds(getBounds());
        return rect.crop(getRotatedInsets()).getCenter();
    }

    /**
     * Paints the text area of this label using the given Graphics object.<br>
     * <br>
     * <b>IMPORTANT</b>: Subclasses should never use any method that might
     * access clipping in the given Graphics, such as <hi>
     * <li>{@link Graphics#getClip(Rectangle)},</li>
     * <li>{@link Graphics#setClip(Rectangle)},</li>
     * <li>{@link Graphics#setClip(org.eclipse.swt.graphics.Path)},</li>
     * <li>{@link Graphics#clipRect(Rectangle)},</li>
     * <li>{@link Graphics#translate(int, int)},</li>
     * <li>{@link Graphics#translate(float, float)},</li>
     * <li>{@link Graphics#translate(Point)},</li>
     * <li>{@link Graphics#pushState()},</li>
     * <li>{@link Graphics#restoreState()},</li> </hi><br>
     * <br>
     * for the given Graphics' coordinates may have been rotated and clipping is
     * no longer preserved.<br>
     * <br>
     * 
     * @param graphics
     * @param textArea
     * @see Graphics#rotate(float)
     */
    protected void paintTextArea(Graphics graphics,
            PrecisionRectangle textArea) {
        if (isOpaque() && getLocalBackgroundColor() != null) {
            int oldAlpha = graphics.getAlpha();
            graphics.setAlpha(getSubAlpha());
            Path bg = new Path(Display.getCurrent());
            bg.addRectangle(textArea);
            graphics.fillPath(bg);
            bg.dispose();
            graphics.setAlpha(oldAlpha);
        }
        int oldAlpha = graphics.getAlpha();
        graphics.setAlpha(getMainAlpha());
        paintText(graphics, getAppliedText((int) Math.ceil(textArea.width)),
                textArea, getFont());
        graphics.setAlpha(oldAlpha);
    }

    /**
     * <b>IMPORTANT</b>: Subclasses should never use any method that might
     * access clipping in the given Graphics, such as <hi>
     * <li>{@link Graphics#getClip(Rectangle)},</li>
     * <li>{@link Graphics#setClip(Rectangle)},</li>
     * <li>{@link Graphics#setClip(org.eclipse.swt.graphics.Path)},</li>
     * <li>{@link Graphics#clipRect(Rectangle)},</li>
     * <li>{@link Graphics#translate(int, int)},</li>
     * <li>{@link Graphics#translate(float, float)},</li>
     * <li>{@link Graphics#translate(Point)},</li>
     * <li>{@link Graphics#pushState()},</li>
     * <li>{@link Graphics#restoreState()},</li> </hi><br>
     * <br>
     * for the given Graphics' coordinates may have been rotated and clipping is
     * no longer preserved.<br>
     * <br>
     * 
     * @param graphics
     * @param text
     * @param textArea
     */
    protected void paintText(Graphics graphics, String text,
            PrecisionRectangle textArea, Font f) {
        String[] tokens = forceSplit(text);
        float textWidth = (float) textArea.width - PADDING * 2;
        float y = (float) textArea.y + PADDING;
        float vSpacing = getLineSpacing();
        final int wrapAlignment = getTextAlignment();
        final int textCase = getTextCase();
        boolean isUnderlined = isTextUnderlined();
        boolean isStrikedThrough = isTextStrikedThrough();

        //graphics.drawRectangle(textArea.toDraw2DRectangle().resize(-1, -1));

        for (String token : tokens) {
            float x = (float) textArea.x + PADDING;
            String t = getShowText(token, textCase);
            Dimension tokenSize = getLooseTextSize(t, f);
            float tokenWidth = tokenSize.width;
            float tokenHeight = tokenSize.height;
            float tokenHeightHalf = tokenHeight / 2;

            switch (wrapAlignment) {
            case PositionConstants.CENTER:
                x += (textWidth - tokenWidth) / 2;
                break;
            case PositionConstants.RIGHT:
                x += textWidth - tokenWidth - RIGHT_MARGIN;
                break;
            }

            paintText(graphics, t, x, y, tokenWidth, tokenHeight, f);

            y += tokenHeight;

            if (isUnderlined) {
                Path underline = new Path(Display.getCurrent());
                underline.moveTo(x, y - 1);
                underline.lineTo(x + tokenWidth, y - 1);
                graphics.drawPath(underline);
                underline.dispose();
            }
            if (isStrikedThrough) {
                Path strikeOutLine = new Path(Display.getCurrent());
                strikeOutLine.moveTo(x, y - tokenHeightHalf + 1);
                strikeOutLine.lineTo(x + tokenWidth, y - tokenHeightHalf + 1);
                graphics.drawPath(strikeOutLine);
                strikeOutLine.dispose();
            }

            y += vSpacing;
        }
    }

    protected void paintText(Graphics graphics, String token, float x, float y,
            float width, float height, Font f) {
        if (isNormalRenderStyle()) {
            graphics.translate(x, y);
            graphics.drawText(token, 0, 0);
            graphics.translate(-x, -y);
            return;
        }
        Path shape = new Path(Display.getCurrent());
        shape.addString(token, 0, 0, f);
        shape.getBounds(RECT);
        if (Util.isWindows()) {
            float[] autoScaleDown = DPIUtil.autoScaleDown(RECT);
            RECT[0] = autoScaleDown[0];
            RECT[1] = autoScaleDown[1];
            RECT[2] = autoScaleDown[2];
            RECT[3] = autoScaleDown[3];
        }

        float dx = (width - RECT[2]) / 2 - RECT[0];
        float dy = (height - RECT[3]) / 2 - RECT[1];
        if (Math.abs(dx) > 0.0000000001 || Math.abs(dy) > 0.0000000001) {
            shape.dispose();
            shape = new Path(Display.getCurrent());
            shape.addString(token, x + dx, y + dy, f);
        }
        drawTextShape(graphics, shape);
        shape.dispose();
    }

    protected void drawTextShape(Graphics graphics, Path shape) {
        graphics.setLineStyle(SWT.LINE_SOLID);
        graphics.setLineWidth(1);
        graphics.setFillRule(SWT.FILL_WINDING);
        graphics.setBackgroundColor(graphics.getForegroundColor());
        graphics.fillPath(shape);
    }

    protected boolean isNormalRenderStyle() {
        /// some fonts don't show correctly when use graphics.drawText(), so render it by path.
        boolean shownWrong = false;
        String fontName = null;
        if (getFont() != null) {
            fontName = (getFont().getFontData())[0].getName();
        }

        String[] wrongFontNames = { "Cambria Math", "Gabriola", "Javanese Text", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "Lucida Sans Unicode", "Microsoft Himalaya", "MV Boli", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "Myanmar Text", "Segoe MDL2 Assets" }; //$NON-NLS-1$ //$NON-NLS-2$
        String[] wrongFontPrefixs = { "Sitka" }; //$NON-NLS-1$

        if (fontName != null) {
            for (String name : wrongFontNames) {
                if (fontName.equals(name)) {
                    shownWrong = true;
                    break;
                }
            }
            if (!shownWrong) {
                for (String prefix : wrongFontPrefixs) {
                    if (fontName.startsWith(prefix)) {
                        shownWrong = true;
                        break;
                    }
                }
            }
        }

        return renderStyle == NORMAL && !shownWrong;
    }

    /**
     * @return
     */
    private boolean isTextStrikedThrough() {
        return getStyle() != null && getStyle().strikeout;
    }

    /**
     * @return
     */
    private boolean isTextUnderlined() {
        return getStyle() != null && getStyle().underline;
    }

    public String toString() {
        return "RotatableWrapLabl (" + getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
