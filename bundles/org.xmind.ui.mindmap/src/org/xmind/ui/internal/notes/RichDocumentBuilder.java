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
package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IImageSpan;
import org.xmind.core.INotesContent;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ISpan;
import org.xmind.core.ITextSpan;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichDocument;
import org.xmind.ui.richtext.RichTextUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class RichDocumentBuilder {

    private static boolean DEBUG = false;

    private RichDocumentNotesAdapter adapter;

    private IRichDocument result;

    private StringBuilder totalText = new StringBuilder();

    private List<StyleRange> textStyles = new ArrayList<StyleRange>();

    private List<LineStyle> lineStyles = new ArrayList<LineStyle>();

    private List<ImagePlaceHolder> images = new ArrayList<ImagePlaceHolder>();

    private List<Hyperlink> hyperlinks = new ArrayList<Hyperlink>();

    private int lineIndex = 0;

    private int totalOffset = 0;

    private Stack<LineStyle> lineStyleStack = new Stack<LineStyle>();

    private Stack<StyleRange> textStyleStack = new Stack<StyleRange>();

    private StringBuilder currentLine = null;

    private StyleRange lastTextStyle = null;

    public RichDocumentBuilder(RichDocumentNotesAdapter adapter) {
        this.adapter = adapter;
    }

    public void build(INotesContent content) {
        if (content == null) {
            if (DEBUG)
                System.out.println("empty content"); //$NON-NLS-1$
            result = new RichDocument();
            return;
        }

        if (content instanceof IPlainNotesContent) {
            String text = ((IPlainNotesContent) content).getTextContent();
            if (text == null || "".equals(text)) { //$NON-NLS-1$
                if (DEBUG)
                    System.out.println("empty plain content"); //$NON-NLS-1$
                result = new RichDocument();
                return;
            }
            if (DEBUG)
                System.out.println("plain content"); //$NON-NLS-1$
            result = new RichDocument(text);
            return;
        }

        if (!(content instanceof IHtmlNotesContent)) {
            if (DEBUG)
                System.out.println("unknown content format"); //$NON-NLS-1$
            result = new RichDocument();
            return;
        }

        IHtmlNotesContent html = (IHtmlNotesContent) content;

//        if (DEBUG)
//            System.out.println(content);
//        content = DOMUtils.makeElementText(content, NS.XMAP,
//                WorkbookUtils.TAG_RICH_CONTENT, NS.Xhtml, NS.Xlink, NS.SVG);
//        Document doc;
//        try {
//            doc = DOMUtils.loadDocument(content.getBytes());
//        } catch (Throwable e) {
//            if (DEBUG) {
//                e.printStackTrace();
//            } else {
//                Logger.log(e, "Failed to parse html notes"); //$NON-NLS-1$
//            }
//            result = new RichDocument();
//            return;
//        }

        readContent(html);
//        
//        readElement(doc.getDocumentElement());
        if (endsWith(totalText, NotesConstants.LINE_DELIMITER)) {
            if (DEBUG) {
                System.out.println("-- delete last line delimiter -- "); //$NON-NLS-1$
            }
            deleteLastLineDelimiter();
        }
        result = new RichDocument(totalText.toString());
        result.setTextStyles(
                textStyles.toArray(new StyleRange[textStyles.size()]));
        result.setLineStyles(
                lineStyles.toArray(new LineStyle[lineStyles.size()]));
        result.setImages(images.toArray(new ImagePlaceHolder[images.size()]));
        result.setHyperlinks(
                hyperlinks.toArray(new Hyperlink[hyperlinks.size()]));

        if (DEBUG) {
            System.out.println(result.get());
            System.out.println(Arrays.toString(result.getTextStyles()));
            System.out.println(Arrays.toString(result.getLineStyles()));
            System.out.println(Arrays.toString(result.getImages()));
            System.out.println(Arrays.toString(result.getHyperlinks()));
        }
    }

    private boolean endsWith(StringBuilder sb, String s) {
        if (s.length() == 0 || sb.length() < s.length())
            return false;

        for (int i = 0; i < s.length(); i++) {
            char c = sb.charAt(sb.length() - i - 1);
            char c2 = s.charAt(s.length() - i - 1);
            if (c != c2)
                return false;
        }
        return true;
    }

    private void deleteLastLineDelimiter() {
        int length = NotesConstants.LENGTH_DELIMITER;
        totalText.delete(totalText.length() - length, totalText.length());
        if (lastTextStyle != null
                && lastTextStyle.start + lastTextStyle.length == totalOffset) {
            if (lastTextStyle.length <= length) {
                textStyles.remove(lastTextStyle);
            } else {
                lastTextStyle.length -= length;
            }
        }
    }

    private void readContent(IHtmlNotesContent html) {
        for (IParagraph p : html.getParagraphs()) {
            readParagraph(p);
        }
    }

    private void readParagraph(IParagraph p) {
        if (currentLine != null) {
            endLine();
        }
        if (DEBUG) {
            System.out.println("start line: " + lineIndex); //$NON-NLS-1$
        }
        LineStyle currentLineStyle = createLineStyle(p);
        if (currentLineStyle != null) {
            currentLineStyle.lineIndex = lineIndex;
            lineStyles.add(currentLineStyle);
            if (DEBUG)
                System.out.println("line style added: " + currentLineStyle); //$NON-NLS-1$
        }
        lineStyleStack.push(currentLineStyle);
        if (DEBUG)
            System.out.println("line style pushed: " + currentLineStyle); //$NON-NLS-1$
        currentLine = new StringBuilder();
        readParagraphContent(p);
        endLine();
        lineStyleStack.pop();
    }

    private LineStyle createLineStyle(IParagraph p) {
        LineStyle currentLineStyle = newLineStyle();
        IStyle style = getStyle(p);
        if (style != null) {
            String alignment = style.getProperty(Styles.TextAlign);
            if (DEBUG) {
                System.out.println("alingment: " + alignment); //$NON-NLS-1$
            }
            if (alignment != null) {
                currentLineStyle.alignment = toSWTAlignment(alignment);
            }
//            String bullet = style.getProperty(Styles.TextBullet);
//            if (DEBUG)
//                System.out.println("bullet: " + bullet); //$NON-NLS-1$
//            if (bullet != null) {
//                boolean isBullet = Styles.TEXT_STYLE_BULLET.equals(bullet) ? true
//                        : false;
//                currentLineStyle.bullet = isBullet;
//            }
            String bulletStyle = style.getProperty(Styles.TextBullet);
            if (DEBUG)
                System.out.println("bulletStyle: " + bulletStyle); //$NON-NLS-1$
            if (bulletStyle != null) {
                currentLineStyle.bulletStyle = getBulletStyle(bulletStyle);
            }
        } else if (DEBUG) {
            System.out.println("no line style"); //$NON-NLS-1$
        }
        return currentLineStyle;
    }

    private String getBulletStyle(String bulletStyle) {
        if (LineStyle.BULLET.equals(bulletStyle))
            return LineStyle.BULLET;
        else if (LineStyle.NUMBER.equals(bulletStyle))
            return LineStyle.NUMBER;
        return LineStyle.NONE_STYLE;
    }

    private void readParagraphContent(IParagraph p) {
        for (ISpan span : p.getSpans()) {
            if (span instanceof IImageSpan) {
                readImage((IImageSpan) span);
            } else if (span instanceof ITextSpan) {
                readText((ITextSpan) span);
            } else if (span instanceof IHyperlinkSpan) {
                readHyperlink((IHyperlinkSpan) span);
            }
        }
    }

//    private void readElementChildren(Element ele) {
//        NodeList children = ele.getChildNodes();
//        for (int i = 0; i < children.getLength(); i++) {
//            Node child = children.item(i);
//            short nodeType = child.getNodeType();
//            if (nodeType == Node.ELEMENT_NODE) {
//                readElement((Element) child);
//            } else if (nodeType == Node.TEXT_NODE) {
//                readText((Text) child);
//            }
//        }
//    }

    private void endLine() {
        if (DEBUG) {
            System.out.println("end line: " + lineIndex); //$NON-NLS-1$
        }
        sumLineIndent();
        appendLineDelimiter();
        lineIndex++;
        currentLine = null;
    }

    private void ensureLineStart() {
        if (currentLine != null)
            return;

        LineStyle currentLineStyle = newLineStyle();
        currentLineStyle.lineIndex = lineIndex;
        if (!lineStyleStack.isEmpty())
            lineStyleStack.pop();
        lineStyleStack.push(currentLineStyle);
        currentLine = new StringBuilder();
    }

    private void appendLineDelimiter() {
        totalText.append(NotesConstants.LINE_DELIMITER);
        if (lastTextStyle != null
                && lastTextStyle.start + lastTextStyle.length == totalOffset) {
            lastTextStyle.length += NotesConstants.LENGTH_DELIMITER;
        }
        totalOffset += NotesConstants.LENGTH_DELIMITER;
    }

    private void sumLineIndent() {
        if (currentLine == null)
            return;

        if (lineStyleStack.isEmpty())
            return;

        int indent = calcIndentCount(currentLine);
        LineStyle lastLineStyle = lineStyleStack.peek();
        lastLineStyle.indent = indent;
    }

    private LineStyle newLineStyle() {
        LineStyle lastLineStyle = lineStyleStack.isEmpty() ? null
                : lineStyleStack.peek();
        if (DEBUG) {
            System.out.println("last line style: " + lastLineStyle); //$NON-NLS-1$
        }
        if (lastLineStyle == null)
            return (LineStyle) RichTextUtils.DEFAULT_LINE_STYLE.clone();
        return (LineStyle) lastLineStyle.clone();
    }

    private StyleRange newTextStyle() {
        StyleRange lastTextStyle = textStyleStack.isEmpty() ? null
                : textStyleStack.peek();
        if (lastTextStyle == null)
            return null;
        return (StyleRange) lastTextStyle.clone();
    }

    private void readImage(IImageSpan span) {
        String uri = span.getSource();//DOMUtils.getAttribute(ele, DOMConstants.ATTR_SRC);
        if (uri != null) {
            Image image = adapter.getImageFromUrl(uri);
            if (image != null) {
                ensureLineStart();
                String s = ImagePlaceHolder.PLACE_HOLDER;
                int length = s.length();
                totalText.append(s);
                currentLine.append(s);
                StyleRange style = (StyleRange) RichTextUtils.DEFAULT_STYLE
                        .clone();
                style.start = totalOffset;
                style.length = length;
//                style.data = image;
                Rectangle rect = image.getBounds();
                style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
                ImagePlaceHolder imagePlaceHolder = new ImagePlaceHolder(
                        totalOffset, image);
                images.add(imagePlaceHolder);

                if (!RichTextUtils.merge(style, lastTextStyle)) {
                    textStyles.add(style);
                    lastTextStyle = style;
                }
                totalOffset += length;
            }
        }
    }

    private void readHyperlink(IHyperlinkSpan span) {
        String urlString = span.getHref();
        int start = totalOffset;
        for (ISpan s : span.getSpans()) {
            if (s instanceof ITextSpan) {
                readText((ITextSpan) s);
            } else if (s instanceof IImageSpan) {
                readImage((IImageSpan) s);
            }
        }
        int length = totalOffset - start;
        Hyperlink hyperlink = new Hyperlink(start, length, urlString);
        hyperlinks.add(hyperlink);
    }

    private void readText(ITextSpan span) {
        StyleRange textStyle = createTextStyle(getStyle(span));
        textStyleStack.push(textStyle);
        appendText(span.getTextContent(), textStyle);
        //readElementChildren(ele);
        textStyleStack.pop();
    }

    private void appendText(String text, StyleRange textStyle) {
        ensureLineStart();
        int length = text.length();
        totalText.append(text);
        currentLine.append(text);
        if (textStyle != null) {
            textStyle.start = totalOffset;
            textStyle.length = length;
            // Merge with last style range if possible
            if (!RichTextUtils.merge(textStyle, lastTextStyle)) {
                textStyles.add(textStyle);
                lastTextStyle = textStyle;
            }
        }
        totalOffset += length;
    }

//    private void readText(Text text) {
//        appendText(text.getTextContent(), newTextStyle());
//    }

    private StyleRange createTextStyle(IStyle style) {
        StyleRange textStyle = newTextStyle();
        if (style == null)
            return textStyle;

        String name = style.getProperty(Styles.FontFamily);
        String availableFontName = FontUtils.getAAvailableFontNameFor(name);
        name = availableFontName != null ? availableFontName : name;

        if (Styles.SYSTEM.equals(name)) {
            name = JFaceResources.getDefaultFont().getFontData()[0].getName();
        }
        String height = style.getProperty(Styles.FontSize);
        String weight = style.getProperty(Styles.FontWeight);
        String fontStyle = style.getProperty(Styles.FontStyle);
        String foreground = style.getProperty(Styles.TextColor);
        String background = style.getProperty(Styles.BackgroundColor);
        String decoration = style.getProperty(Styles.TextDecoration);
        if (name == null && height == null && weight == null
                && fontStyle == null && foreground == null && background == null
                && decoration == null)
            return textStyle;

        if (name == null)
            name = RichTextUtils.DEFAULT_FONT_DATA.getName();
        int size = NumberUtils.safeParseInt(StyleUtils.trimNumber(height),
                RichTextUtils.DEFAULT_FONT_DATA.getHeight());
        boolean bold = weight != null
                && weight.contains(Styles.FONT_WEIGHT_BOLD);
        boolean italic = fontStyle != null
                && fontStyle.contains(Styles.FONT_STYLE_ITALIC);

        if (textStyle == null)
            textStyle = new StyleRange();
        textStyle.font = FontUtils.getFont(name, size, bold, italic);
        textStyle.foreground = ColorUtils.getColor(foreground);
        textStyle.background = ColorUtils.getColor(background);
        textStyle.underline = decoration != null
                && decoration.contains(Styles.TEXT_DECORATION_UNDERLINE);
        textStyle.strikeout = decoration != null
                && decoration.contains(Styles.TEXT_DECORATION_LINE_THROUGH);
        return textStyle;
    }

    private int toSWTAlignment(String value) {
        if (Styles.ALIGN_CENTER.equals(value))
            return SWT.CENTER;
        if (Styles.ALIGN_RIGHT.equals(value))
            return SWT.RIGHT;
        return SWT.LEFT;
    }

    private int calcIndentCount(StringBuilder line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c != '\t') {
                return indent;
            }
            indent++;
        }
        return indent;
    }

    private IStyle getStyle(IStyled styled) {
        String styleId = styled.getStyleId();
        if (styleId != null) {
            return adapter.getWorkbook().getStyleSheet().findStyle(styleId);
        }
        return null;
    }

    public IRichDocument getResult() {
        return result;
    }

}