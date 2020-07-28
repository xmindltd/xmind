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
package org.xmind.ui.richtext;

import static org.xmind.ui.richtext.ImagePlaceHolder.PLACE_HOLDER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.resources.FontUtils;

/**
 * @author Frank Shaka
 */
public class RichTextUtils {

    private static class SystemColorFactory {
        private static Color getColor(final int which) {
            Display display = Display.getCurrent();
            if (display != null)
                return display.getSystemColor(which);
            display = Display.getDefault();
            final Color result[] = new Color[1];
            display.syncExec(new Runnable() {
                public void run() {
                    synchronized (result) {
                        result[0] = Display.getCurrent().getSystemColor(which);
                    }
                }
            });
            synchronized (result) {
                return result[0];
            }
        }
    }

    static final String LineDelimiter = System.getProperty("line.separator"); //$NON-NLS-1$

    public static Font DEFAULT_FONT = JFaceResources.getDefaultFont();
    public static FontData DEFAULT_FONT_DATA = DEFAULT_FONT.getFontData()[0];
    public static Color DEFAULT_FOREGROUND = SystemColorFactory
            .getColor(SWT.COLOR_BLACK);
    public static Color DEFAULT_BACKGROUND = SystemColorFactory
            .getColor(SWT.COLOR_WHITE);

    public static final char INDENT_CHAR = '\t';
    public static final String EMPTY = ""; //$NON-NLS-1$

    public static final StyleRange DEFAULT_STYLE;

    static {
        DEFAULT_STYLE = new StyleRange(0, 0, DEFAULT_FOREGROUND,
                DEFAULT_BACKGROUND, 0);
        DEFAULT_STYLE.font = DEFAULT_FONT;
        DEFAULT_STYLE.borderColor = DEFAULT_FOREGROUND;
        DEFAULT_STYLE.borderStyle = SWT.NONE;
        DEFAULT_STYLE.strikeout = false;
        DEFAULT_STYLE.strikeoutColor = DEFAULT_FOREGROUND;
        DEFAULT_STYLE.underline = false;
        DEFAULT_STYLE.underlineColor = DEFAULT_FOREGROUND;
        DEFAULT_STYLE.underlineStyle = SWT.UNDERLINE_SINGLE;
    }

    public static final LineStyle DEFAULT_LINE_STYLE = new LineStyle();

    private RichTextUtils() {
    }

    public static boolean isItalic(StyleRange style) {
        return hasFontStyle(style, SWT.ITALIC);
    }

    public static boolean setItalic(StyleRange style, boolean italic) {
        return changeFontStyle(style, SWT.ITALIC, italic);
    }

    public static boolean isBold(StyleRange style) {
        return hasFontStyle(style, SWT.BOLD);
    }

    public static boolean setBold(StyleRange style, boolean bold) {
        return changeFontStyle(style, SWT.BOLD, bold);
    }

    public static boolean hasFontStyle(StyleRange style, int singleStyle) {
        return (getFontStyle(style) & singleStyle) != 0;
    }

    public static boolean changeFontStyle(StyleRange style, int singleStyle,
            boolean value) {
        int fontStyle = getFontStyle(style);
        int changedFontStyle = getChangedFontStyle(fontStyle, singleStyle,
                value);
        if (fontStyle == changedFontStyle)
            return false;
        setFontStyle(style, changedFontStyle);
        return true;
    }

    public static int getChangedFontStyle(int fontStyle, int singleStyle,
            boolean value) {
        if (value) {
            fontStyle |= singleStyle;
        } else {
            fontStyle &= ~singleStyle;
        }
        return fontStyle;
    }

    public static int getFontStyle(StyleRange style) {
        return style.font == null ? style.fontStyle
                : style.font.getFontData()[0].getStyle();
    }

    public static void setFontStyle(StyleRange style, int fontStyle) {
        style.fontStyle = fontStyle;
        if (style.font != null) {
            style.font = FontUtils.getStyled(style.font, fontStyle);
//            if ((fontStyle & SWT.BOLD) != 0) {
//                style.font = FontUtils.getBold(style.font);
//            }
//            if ((fontStyle & SWT.ITALIC) != 0) {
//                style.font = FontUtils.getItalic(style.font);
//            }
        }
    }

    public static int getFontStyle(boolean bold, boolean italic) {
        int fontStyle = SWT.NORMAL;
        if (bold)
            fontStyle |= SWT.BOLD;
        if (italic)
            fontStyle |= SWT.ITALIC;
        return fontStyle;
    }

    public static String getFontFace(StyleRange style) {
        return getFont(style).getFontData()[0].getName();
    }

    public static int getFontSize(StyleRange style) {
        return getFont(style).getFontData()[0].getHeight();
    }

    public static Font getFont(StyleRange style) {
        return style.font == null ? DEFAULT_FONT : style.font;
    }

    public static Color getForeground(StyleRange style) {
        return style.foreground == null ? DEFAULT_FOREGROUND : style.foreground;
    }

    public static Color getBackground(StyleRange style) {
        return style.background == null ? DEFAULT_BACKGROUND : style.background;
    }

    public static boolean setFont(StyleRange style, Font font) {
        if (font == style.font || (font != null && equals(font, style.font)))
            return false;
        style.font = font;
        return true;
    }

    public static boolean setFontFace(StyleRange style, String name) {
        if (name == null) {
            name = DEFAULT_FONT_DATA.getName();
        }
        if (getFontFace(style).equals(name)) {
            return false;
        }
        style.font = FontUtils.getNewName(getFont(style), name);
        return true;
    }

    public static boolean setFontSize(StyleRange style, int size) {
        if (size <= 0) {
            size = DEFAULT_FONT_DATA.getHeight();
        }
        if (getFontSize(style) == size)
            return false;
        style.font = FontUtils.getNewHeight(getFont(style), size);
        return true;
    }

    public static boolean setForeground(StyleRange style, Color color) {
        if (color == style.foreground
                || (color != null && color.equals(style.foreground)))
            return false;
        style.foreground = color;
        return true;
    }

    public static boolean setBackground(StyleRange style, Color color) {
        if (color == style.background
                || (color != null && color.equals(style.background)))
            return false;
        style.background = color;
        return true;
    }

    public static List<Hyperlink> getHyperlinksInRange(Hyperlink[] hyperlinks,
            int start, int end) {
        List<Hyperlink> results = new ArrayList<Hyperlink>(hyperlinks.length);
        for (Hyperlink hyperlink : hyperlinks) {
            if (hyperlink.start <= end && hyperlink.end() >= start) {
                results.add(hyperlink);
            }
        }
        return results;
    }

    public static LineStyle updateLineStylePositions(int startLine,
            int oldLineCount, int newLineCount, List<LineStyle> lineStyles,
            IDocument document) {

        int oldEndLine = startLine + oldLineCount;
        int deltaLines = newLineCount - oldLineCount;
        LineStyle firstInRange = null;
        int firstInRangeIndex = 0;
        for (int i = 0; i < lineStyles.size();) {
            LineStyle current = lineStyles.get(i);
            int currentLineIndex = current.lineIndex;

            if (currentLineIndex == 0) {
                String content = document.get();
                if ("".equals(content)) //$NON-NLS-1$
                    current.bulletStyle = LineStyle.NONE_STYLE;
            }

            if (currentLineIndex < startLine) {
                i++;
                continue;
            }

            if (currentLineIndex >= oldEndLine) {
                current.lineIndex += deltaLines;
                i++;
                continue;
            }

            if (currentLineIndex == startLine) {
                firstInRange = current;
                firstInRangeIndex = i;
                i++;
            } else {
                lineStyles.remove(i);
            }
        }

        int newEndLine = startLine + newLineCount;
        if (firstInRange != null) {
            int styleIndex = firstInRangeIndex + 1;
            for (int lineIndex = firstInRange.lineIndex
                    + 1; lineIndex < newEndLine; lineIndex++) {
                LineStyle lineStyle = (LineStyle) firstInRange.clone();
                lineStyle.lineIndex = lineIndex;
                lineStyle.indent = calcLineIndentCount(document, lineIndex);
                lineStyles.add(styleIndex, lineStyle);
                styleIndex++;
            }
        } else {
            int styleIndex = firstInRangeIndex;
            for (int lineIndex = startLine; lineIndex < newEndLine; lineIndex++) {
                int indent = calcLineIndentCount(document, lineIndex);
                if (indent > 0) {
                    LineStyle lineStyle = (LineStyle) RichTextUtils.DEFAULT_LINE_STYLE
                            .clone();
                    lineStyle.indent = indent;
                    lineStyles.add(styleIndex, lineStyle);
                    styleIndex++;
                }
            }
        }
        return firstInRange;
    }

    public static void updateImagePositions(int start, int oldLength,
            int newLength, List<ImagePlaceHolder> images) {
        int oldEnd = start + oldLength;
        int deltaOffset = newLength - oldLength;
        int imagePlaceHolderLength = PLACE_HOLDER.length();
        for (int i = 0; i < images.size();) {
            ImagePlaceHolder current = images.get(i);
            int currentStart = current.offset;
            int currentEnd = currentStart + imagePlaceHolderLength;

            // before range
            if (currentEnd <= start) {
                // simply skip
                i++;
                continue;
            }

            // after range
            if (currentStart >= oldEnd) {
                // push back to fit the new range
                current.offset += deltaOffset;
                // skip
                i++;
                continue;
            }

            // remove all images within the old range
            images.remove(i);
        }
    }

    /**
     * @param start
     *            --the position of Cursor
     * @param oldLength
     *            --the selection text's length
     * @param newLength
     *            --later input the text's length
     * @param hyperlinks
     */
    public static void updateHyperlinksPositions(int start, int oldLength,
            int newLength, List<Hyperlink> hyperlinks) {

        int oldEnd = start + oldLength;
        int deltaOffset = newLength - oldLength;

        for (int i = 0; i < hyperlinks.size();) {
            Hyperlink currentHyper = hyperlinks.get(i);
            int currentStart = currentHyper.start;
            int currentEnd = currentHyper.end();
            //in front of range
            if (oldEnd <= currentStart) {
                currentHyper.start += deltaOffset;
                i++;
                continue;
            }
            //behind of range
            if (start >= currentEnd) {
                i++;
                continue;
            }
            //the overlap at the front half of range
            if (oldEnd <= currentEnd && oldEnd > currentStart) {
                if (start >= currentStart) {
                    currentHyper.length += deltaOffset;
                    if (currentHyper.length == 0) {
                        hyperlinks.remove(i);
                        continue;
                    }
                } else {
                    currentHyper.length = currentHyper.end() - oldEnd;
                    currentHyper.start = oldEnd + deltaOffset;
                }
                i++;
                continue;
            }
            // the overlap at the behind half of range
            if (start >= currentStart && start < currentEnd) {
                if (oldEnd <= currentEnd) {
                    currentHyper.length += deltaOffset;
                    if (currentHyper.length == 0) {
                        hyperlinks.remove(i);
                        continue;
                    }
                } else {
                    currentHyper.length = start - currentStart;
                }
                i++;
                continue;
            }
            hyperlinks.remove(i);
        }

    }

    public static void replaceStyleRanges(int start, int oldLength,
            int newLength, List<StyleRange> styles, StyleRange replacement) {
        int oldEnd = start + oldLength;
        int deltaOffset = newLength - oldLength;
        StyleRange last = null;
        StyleRange lastStyleBeforeRange = null;
        int lastIndexBeforeRange = -1;
        for (int styleIndex = 0; styleIndex < styles.size();) {
            StyleRange current = styles.get(styleIndex);
            int currentStart = current.start;
            int currentLength = current.length;
            int currentEnd = currentStart + currentLength;

            // before the old range
            if (currentEnd <= start) {
                last = current;
                lastStyleBeforeRange = current;
                lastIndexBeforeRange = styleIndex;
                // simply skip to next style
                styleIndex++;
                continue;
            }

            // after the old range
            if (currentStart >= oldEnd) {
                // push back to fit the new range
                current.start += deltaOffset;
                // merge with the last style if possible
                if (merge(current, last)) {
                    styles.remove(styleIndex);
                } else {
                    styleIndex++;
                }
                continue;
            }

            // some part before the old range
            if (currentStart < start) {
                // make a copy of that part to protect it from modification
                StyleRange beforePart = (StyleRange) current.clone();
                beforePart.length = start - currentStart;
                styles.add(styleIndex, beforePart);
                // refresh the current style range
                currentStart = start;
                current.start = currentStart;
                current.length = currentEnd - currentStart;

                last = beforePart;
                lastStyleBeforeRange = beforePart;
                lastIndexBeforeRange = styleIndex;
                styleIndex++;
            }

            // some part after the old range
            if (currentEnd > oldEnd) {
                // make a copy of that part to protect it from modification
                StyleRange afterPart = (StyleRange) current.clone();
                afterPart.start = oldEnd;
                afterPart.length = currentEnd - oldEnd;
                // refresh the current style range
                styles.add(styleIndex + 1, afterPart);
                currentEnd = oldEnd;
                current.length = currentEnd - currentStart;
            }

            // remove all style ranges within the old range
            styles.remove(styleIndex);
        }

        StyleRange newStyle = (StyleRange) replacement.clone();
        newStyle.start = start;
        newStyle.length = newLength;
        if (!merge(newStyle, lastStyleBeforeRange)) {
            styles.add(lastIndexBeforeRange + 1, newStyle);
        }
    }

    public static boolean modifyTextStyles(int start, int length,
            List<StyleRange> styles, IStyleRangeModifier modifier) {
        if (length == 0 || modifier == null)
            return false;
        int end = start + length;
        boolean changed = false;
        StyleRange last = null;
        int unhandledStart = start;
        int unhandledEnd = end;
        int unhandledIndex = 0;

        for (int styleIndex = 0; styleIndex < styles.size();) {
            StyleRange current = styles.get(styleIndex);
            int currentStart = current.start;
            int currentEnd = currentStart + current.length;

            // before selection range
            if (currentEnd <= start) {
                // simply skip to next style
                last = current;
                styleIndex++;
                unhandledIndex = styleIndex;
                continue;
            }

            // after selection range
            if (currentStart >= end) {
                // merge with the last style if possible
                if (merge(current, last)) {
                    styles.remove(styleIndex);
                    changed = true;
                }
                // loop should end as the following style ranges
                // are all beyond the selection range and should
                // remain unmodified
                break;
            }

            // some part before selection range
            if (currentStart < start) {
                unhandledStart = currentEnd;
                // make a copy of that part to protect it from modification
                StyleRange beforePart = (StyleRange) current.clone();
                beforePart.length = start - currentStart;
                styles.add(styleIndex, beforePart);
                changed = true;
                last = beforePart;

                // refresh the current style range
                currentStart = start;
                current.start = currentStart;
                current.length = currentEnd - currentStart;

                styleIndex++;
            }

            // some part after selection range
            if (currentEnd > end) {
                unhandledEnd = currentStart;
                // make a copy of that part to protect it from modification
                StyleRange afterPart = (StyleRange) current.clone();
                afterPart.start = end;
                afterPart.length = currentEnd - end;
                styles.add(styleIndex + 1, afterPart);
                changed = true;
                // refresh the current style range
                currentEnd = end;
                current.length = currentEnd - currentStart;
            }

            // check if there's still unhandled regions before this range
            if (currentStart >= unhandledStart) {
                // modify and add the unhandled region before this range
                if (currentStart > unhandledStart) {
                    StyleRange newStyle = (StyleRange) RichTextUtils.DEFAULT_STYLE
                            .clone();
                    newStyle.start = unhandledStart;
                    newStyle.length = currentStart - unhandledStart;
                    modifier.modify(newStyle);
                    // merge with the last style if possible
                    if (!merge(newStyle, last)) {
                        styles.add(styleIndex, newStyle);
                        last = newStyle;
                        styleIndex++;
                    }
                    changed = true;
                }
                unhandledStart = currentEnd;
            }

            // modify current style
            changed |= modifier.modify(current);

            // merge with the last style if possible
            if (merge(current, last)) {
                styles.remove(styleIndex);
                changed = true;
            } else {
                last = current;
                styleIndex++;
            }
            unhandledIndex = styleIndex;
        }

        // check if there's still unhandled regions before this range
        if (unhandledEnd > unhandledStart) {
            // modify and add the unhandled region before this range
            StyleRange newStyle = (StyleRange) RichTextUtils.DEFAULT_STYLE
                    .clone();
            newStyle.start = unhandledStart;
            newStyle.length = unhandledEnd - unhandledStart;
            modifier.modify(newStyle);
            // merge with the last style if possible
            if (!merge(newStyle, last)) {
                styles.add(unhandledIndex, newStyle);
                changed = true;
            }
        }
        return changed;
    }

    public static boolean merge(StyleRange current, StyleRange last) {
        if (current == null)
            return false;
        if (current.length == 0)
            return true;
        if (last != null && last.start + last.length == current.start) {
            if (isSimilar(current, last)) {
                last.length = last.length + current.length;
                return true;
            }
            return false;
        }
        return isSimilar(current, RichTextUtils.DEFAULT_STYLE);
    }

    public static boolean modifyLineStyles(int startLine, int lineCount,
            List<LineStyle> lineStyles, ILineStyleModifier modifier) {
        int endLine = startLine + lineCount;
        int unhandledLine = startLine;
        int unhandledIndex = 0;

        boolean changed = false;

        for (int i = 0; i < lineStyles.size();) {
            LineStyle current = lineStyles.get(i);
            int currentLineIndex = current.lineIndex;

            if (currentLineIndex < startLine) {
                i++;
                unhandledIndex = i;
                continue;
            }
            if (currentLineIndex >= endLine) {
                break;
            }

            if (currentLineIndex > unhandledLine) {
                LineStyle lineStyle = new LineStyle(unhandledLine);
                boolean modified = modifier.modify(lineStyle);
                changed |= modified;
                if (modified && !lineStyle.isUnstyled()) {
                    lineStyles.add(i, lineStyle);
                    i++;
                }
            } else {
                boolean modified = modifier.modify(current);
                changed |= modified;
                if (modified && current.isUnstyled()) {
                    lineStyles.remove(i);
                } else {
                    i++;
                }
            }
            unhandledIndex = i;
            unhandledLine++;
        }

        for (int i = unhandledLine; i < endLine; i++) {
            LineStyle lineStyle = new LineStyle(i);
            boolean modified = modifier.modify(lineStyle);
            changed |= modified;
            if (modified && !lineStyle.isUnstyled()) {
                lineStyles.add(unhandledIndex, lineStyle);
            }
            unhandledIndex++;
        }

        return changed;
    }

    public static int replaceDocumentIndent(IDocument document, int line,
            int newIndent) throws BadLocationException {
        int realDeltaIndent = 0;

        IRegion region = document.getLineInformation(line);
        int lineOffset = region.getOffset();
        int lineLength = region.getLength();
        String lineContent = document.get(lineOffset, lineLength);
        int oldIndent = calcIndentCount(lineContent);
        if (oldIndent != newIndent) {
            char[] chars = new char[newIndent];
            Arrays.fill(chars, INDENT_CHAR);
            String value = String.valueOf(chars);
            document.replace(lineOffset, oldIndent, value);
            realDeltaIndent = newIndent - oldIndent;
        }

        return realDeltaIndent;
    }

    public static int modifyDocumentIndent(TextViewer viewer,
            IDocument document, int line, int deltaIndent)
                    throws BadLocationException {
        int realDeltaIndent = 0;
        if (deltaIndent == 0)
            return realDeltaIndent;

        IRegion region = document.getLineInformation(line);
        int lineOffset = region.getOffset();
//        StyledText styledText = viewer.getTextWidget();
//        styledText.setLineBullet(lineOffset, 1, null);
        if (deltaIndent > 0) {
            char[] chars = new char[deltaIndent];
            Arrays.fill(chars, INDENT_CHAR);
            String value = String.valueOf(chars);
            document.replace(lineOffset, 0, value);
            realDeltaIndent = deltaIndent;

//            Bullet bullet = styledText.getLineBullet(lineOffset);
//            StyleRange style = new StyleRange();
//            style.metrics = new GlyphMetrics(0, 0, 80);
//            Bullet bullet = new Bullet(ST.BULLET_NUMBER | ST.BULLET_TEXT, style);
//            bullet.text = ".";
//            styledText.setLineBullet(lineOffset, 1, bullet);
//            document.replace(0, 0, value);
        } else if (deltaIndent < 0) {
            int lineLength = region.getLength();
            String lineContent = document.get(lineOffset, lineLength);
            int oldIndent = calcIndentCount(lineContent);
            int deleteCount = Math.min(oldIndent, Math.abs(deltaIndent));
            if (deleteCount > 0) {
                document.replace(lineOffset, deleteCount, EMPTY);
                realDeltaIndent = -deleteCount;
            }
        }

        return realDeltaIndent;
    }

    public static int calcLineCount(String text) {
        int numDelimiter = 0;
        int length = text.length();
        for (int i = 0; i < length;) {
            if (match(text, i, LineDelimiter)) {
                numDelimiter++;
                i += LineDelimiter.length();
            } else {
                i++;
            }
        }
        return numDelimiter + 1;
    }

    private static boolean match(String source, int start, String target) {
        int length = target.length();
        if (start + length > source.length())
            return false;
        for (int i = 0; i < length; i++) {
            char c1 = source.charAt(start + i);
            char c2 = target.charAt(i);
            if (c1 != c2)
                return false;
        }
        return true;
    }

    public static int calcLineIndentCount(IDocument document, int line) {
        try {
            IRegion region = document.getLineInformation(line);
            int lineOffset = region.getOffset();
            int lineLength = region.getLength();
            String lineContent = document.get(lineOffset, lineLength);
            return calcIndentCount(lineContent);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int calcIndentCount(String content) {
        int indent = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c != '\t') {
                return indent;
            }
            indent++;
        }
        return indent;
    }

    public static boolean isSimilar(StyleRange sr1, StyleRange sr2) {
        if (sr1 == sr2)
            return true;
        if (sr1 == null || sr2 == null)
            return false;
        if (!equals(sr1, sr2))
            return false;
        if (sr1.fontStyle == sr2.fontStyle)
            return true;
        return false;
    }

    public static boolean equals(TextStyle style1, TextStyle style2) {
        if (style1 == style2)
            return true;
        if (style1 == null || style2 == null)
            return false;

        if (style1.foreground != null) {
            if (!style1.foreground.equals(style2.foreground))
                return false;
        } else if (style2.foreground != null)
            return false;
        if (style1.background != null) {
            if (!style1.background.equals(style2.background))
                return false;
        } else if (style2.background != null)
            return false;
        if (style1.font != null) {
            if (!equals(style1.font, style2.font))
                return false;
        } else if (style2.font != null)
            return false;
        if (style1.metrics != null || style2.metrics != null)
            return false;
        if (style1.underline != style2.underline)
            return false;
        if (style1.strikeout != style2.strikeout)
            return false;
        if (style1.rise != style2.rise)
            return false;
        return true;
    }

    public static boolean equals(Font f1, Font f2) {
        if (f1 == f2)
            return true;
        if (f1 == null && f2 != null)
            return false;
        if (f2 == null && f1 != null)
            return false;

        if (!Util.isMac())
            return f1.equals(f2);

        if (f1.isDisposed() || f2.isDisposed())
            return false;

        FontData fd1 = f1.getFontData()[0];
        FontData fd2 = f2.getFontData()[0];
        return fd1.equals(fd2);
    }

//    public static List<LineStyle> reduceLineStyles(int startLine,
//            List<LineStyle> lineStyles, IRichDocument document) {
//        if (lineStyles == null || lineStyles.isEmpty())
//            return null;
//        LineStyle lineStyle = lineStyles.get(startLine - 1);
//        lineStyle.bulletStyle = LineStyle.NONE_STYLE;
//        lineStyles.remove(startLine);
//        return lineStyles;
//    }

    public static void replaceHyperlinkHref(IRichDocument doc,
            Hyperlink hyperlink, String newHref) {

        Hyperlink newHyperlink = (Hyperlink) hyperlink.clone();
        newHyperlink.href = newHref;

        Hyperlink[] oldHyperlinks = doc.getHyperlinks();
        List<Hyperlink> newHyperlinks = new ArrayList<Hyperlink>(
                oldHyperlinks.length);
        for (Hyperlink oldHyperlink : oldHyperlinks) {
            if (oldHyperlink.start == hyperlink.start
                    && oldHyperlink.length == hyperlink.length) {
                newHyperlinks.add(newHyperlink);
            } else {
                newHyperlinks.add(oldHyperlink);
            }
        }
        doc.setHyperlinks(
                newHyperlinks.toArray(new Hyperlink[newHyperlinks.size()]));
    }

}