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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IImageSpan;
import org.xmind.core.INotes;
import org.xmind.core.IParagraph;
import org.xmind.core.ISpan;
import org.xmind.core.ITextSpan;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.Property;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.richtext.Hyperlink;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.ImagePlaceHolder;
import org.xmind.ui.richtext.LineStyle;
import org.xmind.ui.richtext.RichTextUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.Logger;

public class HtmlNotesContentBuilder {

    private static final Map<String, String> EMPTY_CONTENTS = Collections
            .emptyMap();

    private RichDocumentNotesAdapter adapter;

    private IStyleSheet styleSheet;

    private IRichDocument document;

    private Iterator<StyleRange> textStyles;

    private Iterator<LineStyle> lineStyles;

    private Iterator<Hyperlink> hyperlinks;

    private StyleRange textStyle;

    private LineStyle lineStyle;

    private Hyperlink hyperlink;

    private IHtmlNotesContent result;

    private IParagraph p;

    private IHyperlinkSpan h = null;

    private int totalLines;

    private int offset = 0;

    private int lineEnd = 0;

    private Map<String, Map<String, String>> contentsCache = new HashMap<String, Map<String, String>>();

    public HtmlNotesContentBuilder(RichDocumentNotesAdapter adapter) {
        this.adapter = adapter;
    }

    public void build(IRichDocument document) {
        this.document = document;
        if ("".equals(document.get())) { //$NON-NLS-1$
            this.result = null;
            return;
        }

        this.textStyles = Arrays.asList(document.getTextStyles()).iterator();
        this.lineStyles = Arrays.asList(document.getLineStyles()).iterator();
        this.hyperlinks = Arrays.asList(document.getHyperlinks()).iterator();

        this.textStyle = textStyles.hasNext() ? textStyles.next() : null;
        this.lineStyle = lineStyles.hasNext() ? lineStyles.next() : null;
        this.hyperlink = hyperlinks.hasNext() ? hyperlinks.next() : null;

        this.totalLines = document.getNumberOfLines();

        this.styleSheet = adapter.getWorkbook().getStyleSheet();

        this.result = (IHtmlNotesContent) adapter.getWorkbook()
                .createNotesContent(INotes.HTML);

        for (int lineIndex = 0; lineIndex < totalLines; lineIndex++) {
            buildParagraph(lineIndex);
        }
    }

    private void buildParagraph(int lineIndex) {
        p = result.createParagraph();
        result.addParagraph(p);

        if (lineStyle != null && lineStyle.lineIndex == lineIndex) {
            applyStyleToParagraph(p, lineStyle);
            lineStyle = lineStyles.hasNext() ? lineStyles.next() : null;
        }
        IRegion lineRange;
        try {
            lineRange = document.getLineInformation(lineIndex);
        } catch (BadLocationException e) {
            Logger.log(e, "Failed to obtain line information"); //$NON-NLS-1$
            return;
        }
        int lineStart = lineRange.getOffset();
        int lineLength = lineRange.getLength();

        if (lineIndex < totalLines - 1) {
            lineLength += NotesConstants.LENGTH_DELIMITER;
        }
        lineEnd = lineStart + lineLength;

        while (offset < lineEnd) {
            buildLineContent();
        }
    }

    private void buildLineContent() {

        int next;
        StyleRange appliedStyle = null;

        if (isInHyperlink()) {
            next = Math.min(lineEnd, getNextHyperlinkEnd());
        } else {
            next = Math.min(lineEnd, getNextHyperlinkStart());
        }

        if (isInStyle()) {
            next = Math.min(next, Math.min(lineEnd, getNextStyleEnd()));
            appliedStyle = textStyle;
        } else {
            next = Math.min(next, Math.min(lineEnd, getNextStyleStart()));
        }

        if (isInHyperlink() && isHyperlinkStarting()) {
            startBuildingHyperlink();
        }

        ISpan span = createSpan(next, appliedStyle);
        if (span != null) {
            addSpan(span);
        }

        if (isInHyperlink() && isHyperlinkEnding(next)) {
            finishCurrentHyperlink();
        }

        if (isInStyle() && isStyleEnding(next)) {
            finishCurrentStyle();
        }
        offset = next;

    }

    private void finishCurrentStyle() {
        textStyle = textStyles.hasNext() ? textStyles.next() : null;
    }

    private boolean isStyleEnding(int next) {
        return textStyle != null && next == textStyle.start + textStyle.length;
    }

    private void finishCurrentHyperlink() {
        hyperlink = hyperlinks.hasNext() ? hyperlinks.next() : null;
        h = null;
    }

    private boolean isHyperlinkEnding(int next) {
        return hyperlink != null && next == hyperlink.end();
    }

    private void addSpan(ISpan span) {
        if (h != null) {
            h.addSpan(span);
        } else {
            p.addSpan(span);
        }
    }

    private ISpan createSpan(int next, StyleRange style) {
        String content = getTrimmedContent(next - offset);
        if (content != null) {
            if (style != null && style.metrics != null
                    && ImagePlaceHolder.PLACE_HOLDER.equals(content)) {
                return createImage(style);
            } else {
                return createText(content, style);
            }
        }
        return null;
    }

    private void startBuildingHyperlink() {
        h = result.createHyperlinkSpan(hyperlink.href);
        p.addSpan(h);
    }

    private boolean isHyperlinkStarting() {
        return hyperlink != null && offset == hyperlink.start;
    }

    private int getNextStyleStart() {
        return textStyle == null ? Integer.MAX_VALUE : textStyle.start;
    }

    private int getNextStyleEnd() {
        StyleRange style = textStyle;
        int endOfStyle = style.start + style.length;
        return style == null ? Integer.MAX_VALUE : endOfStyle;
    }

    private int getNextHyperlinkStart() {
        return hyperlink == null ? Integer.MAX_VALUE : hyperlink.start;
    }

    private int getNextHyperlinkEnd() {
        return hyperlink == null ? Integer.MAX_VALUE : hyperlink.end();
    }

    private boolean isInStyle() {
        if (textStyle != null) {
            int endOfStyle = textStyle.start + textStyle.length;
            return offset >= textStyle.start && offset < endOfStyle;
        }
        return false;
    }

    private boolean isInHyperlink() {
        return hyperlink != null && offset >= hyperlink.start
                && offset < hyperlink.end();
    }

    private ITextSpan createText(String content, StyleRange style) {
        ITextSpan text = result.createTextSpan(content);
        applyStyleToSpan(text, style);
        return text;
    }

    private IImageSpan createImage(StyleRange style) {
        Image image = document.findImage(style.start);
//        Image image = (Image) style.data;
        if (image != null) {
            String url = adapter.getImageUrl(image);
            if (url != null) {
                IImageSpan img = result.createImageSpan(url);
                return img;
            }
        }
        return null;
    }

    private String getTrimmedContent(int textLength) {
        String content;
        try {
            content = document.get(offset, textLength);
        } catch (BadLocationException e) {
            Logger.log(e, "Failed to obtain text contet"); //$NON-NLS-1$
            return null;
        }
        return trimContent(content);
    }

    public String trimContent(String content) {
//        return content.replaceAll(ImagePlaceHolder.PLACE_HOLDER
//                + "|\\r\\n|\\r|\\n", NotesConstants.EMPTY); //$NON-NLS-1$
        return content.replaceAll("\\r\\n|\\r|\\n", NotesConstants.EMPTY); //$NON-NLS-1$
    }

    private void applyStyleToParagraph(IParagraph p, LineStyle lineStyle) {
        if (lineStyle == null)
            return;

        Map<String, String> contents = new HashMap<String, String>();
        String align = toModelAlign(lineStyle.alignment);
//        if (align == null)
//            return;
        if (align != null)
            contents.put(Styles.TextAlign, align);
//        boolean bullet = lineStyle.bullet;
//        if (bullet)
//            contents.put(Styles.TextBullet, Styles.TEXT_STYLE_BULLET);
//        boolean number = lineStyle.number;
//        if (number)
//            contents.put(Styles.TextBullet, Styles.TEXT_STYLE_NUMBER);
        String bulletStyle = getBulletStyle(lineStyle.bulletStyle);
        if (bulletStyle != null)
            contents.put(Styles.TextBullet, bulletStyle);

        String styleId = getNewStyleId(contents, IStyle.PARAGRAPH);
        if (styleId == null)
            return;

        p.setStyleId(styleId);
//        p.setAttribute(DOMConstants.ATTR_STYLE_ID, styleId);
    }

    private String getBulletStyle(String bulletStyle) {
        if (LineStyle.BULLET.equals(bulletStyle))
            return Styles.TEXT_STYLE_BULLET;
        else if (LineStyle.NUMBER.equals(bulletStyle))
            return Styles.TEXT_STYLE_NUMBER;
        return null;
    }

    private void applyStyleToSpan(ISpan span, StyleRange style) {
        if (style == null)
            return;

        String fontName;
        int size;
        if (style.font != null) {
            FontData fontData = style.font.getFontData()[0];
            fontName = fontData.getName();
            if (RichTextUtils.DEFAULT_FONT_DATA.getName().equals(fontName))
                fontName = null;
            size = fontData.getHeight();
            if (RichTextUtils.DEFAULT_FONT_DATA.getHeight() == size)
                size = -1;
        } else {
            fontName = null;
            size = -1;
        }

        String availableFontName = FontUtils.getAAvailableFontNameFor(fontName);
        fontName = availableFontName != null ? availableFontName : fontName;

        boolean bold = RichTextUtils.isBold(style);
        boolean italic = RichTextUtils.isItalic(style);
        boolean underline = style.underline;
        boolean strikeout = style.strikeout;
        String foreground = style.foreground == null ? null
                : ColorUtils.toString(style.foreground);
        String background = style.background == null ? null
                : ColorUtils.toString(style.background);

        if (fontName == null && size < 0 && !bold && !italic && !underline
                && !strikeout && foreground == null && background == null)
            return;

        Map<String, String> contents = new HashMap<String, String>();
        if (fontName != null)
            contents.put(Styles.FontFamily, fontName);
        if (size > 0)
            contents.put(Styles.FontSize, StyleUtils.addUnitPoint(size));
        if (bold)
            contents.put(Styles.FontWeight, Styles.FONT_WEIGHT_BOLD);
        if (italic)
            contents.put(Styles.FontStyle, Styles.FONT_STYLE_ITALIC);
        if (underline || strikeout) {
            if (!underline) {
                contents.put(Styles.TextDecoration,
                        Styles.TEXT_DECORATION_LINE_THROUGH);
            } else if (!strikeout) {
                contents.put(Styles.TextDecoration,
                        Styles.TEXT_DECORATION_UNDERLINE);
            } else {
                contents.put(Styles.TextDecoration,
                        Styles.TEXT_UNDERLINE_AND_LINE_THROUGH);
            }
        }
        if (foreground != null)
            contents.put(Styles.TextColor, foreground);
        if (background != null)
            contents.put(Styles.BackgroundColor, background);
        String styleId = getNewStyleId(contents, IStyle.TEXT);
        if (styleId == null)
            return;

//        span.setAttribute(DOMConstants.ATTR_STYLE_ID, styleId);
        span.setStyleId(styleId);
    }

    private String getNewStyleId(Map<String, String> sourceContents,
            String styleType) {
        if (sourceContents.isEmpty())
            return null;
        IStyle similar = findSimilarStyle(sourceContents);
        if (similar != null)
            return similar.getId();

        IStyle newStyle = createStyle(styleType, sourceContents);
        if (newStyle != null)
            return newStyle.getId();
        return null;
    }

    private Map<String, String> getContents(IStyle style) {
        if (style == null)
            return EMPTY_CONTENTS;
        String id = style.getId();
        Map<String, String> map = contentsCache.get(id);
        if (map != null)
            return map;
        map = new HashMap<String, String>();
        contentsCache.put(id, map);
        Iterator<Property> it = style.properties();
        while (it.hasNext()) {
            Property p = it.next();
            map.put(p.key, p.value);
        }
        return map;
    }

    private IStyle findSimilarStyle(Map<String, String> sourceCotents) {
        Set<IStyle> styles = styleSheet.getStyles(IStyleSheet.NORMAL_STYLES);
        for (IStyle style : styles) {
            Map<String, String> contents = getContents(style);
            if (contents.equals(sourceCotents))
                return style;
        }
        return null;
    }

    private IStyle createStyle(String styleType, Map<String, String> contents) {
        IStyle newStyle = styleSheet.createStyle(styleType);
        for (Entry<String, String> en : contents.entrySet()) {
            newStyle.setProperty(en.getKey(), en.getValue());
        }
        styleSheet.addStyle(newStyle, IStyleSheet.NORMAL_STYLES);
        return newStyle;
    }

    private static String toModelAlign(int alignment) {
        if (alignment == SWT.CENTER)
            return Styles.ALIGN_CENTER;
        if (alignment == SWT.RIGHT)
            return Styles.ALIGN_RIGHT;
        return null;
    }

    public IHtmlNotesContent getResult() {
        return result;
    }

}