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
package org.xmind.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.EncodingUtils;

public class StyledLink extends Canvas {

    private static final int LINE_SPACING = 1;
    private static final int PARAGRAPH_SPACING = 7;

    private static class StyledParagraph {
        TextLayout text;
        int top;
    }

    private String text = ""; //$NON-NLS-1$

    private boolean styled;

    private StyledParagraph[] paragraphs = null;

    private int cachedWidth = -621735;

    private Point cachedSize = null;

    private boolean hovered = false;

    private Font boldFont = null;

    private Font italicFont = null;

    private Font boldItalicFont = null;

    private ListenerList listeners = new ListenerList();

    public StyledLink(Composite parent, int style) {
        super(parent, style | SWT.DOUBLE_BUFFERED);
        this.styled = (style & SWT.SIMPLE) == 0;
        super.setFont(FontUtils.getFont(JFaceResources.DEFAULT_FONT));
        super.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                drawParagraphs(e.gc);
            }
        });
        addMouseListener(new MouseListener() {

            boolean pressed = false;

            public void mouseUp(MouseEvent e) {
                Rectangle r = getBounds();
                if (pressed && e.x >= 0 && e.x < r.width && e.y >= 0
                        && e.y < r.height) {
                    fireLinkActivated(createLinkEvent(e));
                }
                pressed = false;
            }

            public void mouseDown(MouseEvent e) {
                pressed = true;
            }

            public void mouseDoubleClick(MouseEvent e) {
                pressed = false;
            }
        });
        addMouseTrackListener(new MouseTrackListener() {

            public void mouseHover(MouseEvent e) {
                setHovered(true);
            }

            public void mouseExit(MouseEvent e) {
                setHovered(false);
            }

            public void mouseEnter(MouseEvent e) {
                setHovered(true);
            }
        });
    }

    private void fireLinkActivated(HyperlinkEvent event) {
        for (Object listener : listeners.getListeners()) {
            ((IHyperlinkListener) listener).linkActivated(event);
        }
    }

    private HyperlinkEvent createLinkEvent(MouseEvent e) {
        HyperlinkEvent event = new HyperlinkEvent(this, null, getText(),
                e.stateMask);
        event.display = e.display;
        event.time = e.time;
        return event;
    }

    public void addHyperlinkListener(IHyperlinkListener listener) {
        listeners.add(listener);
    }

    public void removeHyperlinkListener(IHyperlinkListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setFont(Font font) {
        checkWidget();
        releaseLines();
        super.setFont(font);
    }

    @Override
    public void setBackground(Color color) {
        checkWidget();
        releaseLines();
        super.setBackground(color);
    }

    @Override
    public void setForeground(Color color) {
        checkWidget();
        releaseLines();
        super.setForeground(color);
    }

    @Override
    public void setEnabled(boolean enabled) {
        checkWidget();
        releaseLines();
        super.setEnabled(enabled);
    }

    private void releaseLines() {
        StyledParagraph[] ls = paragraphs;
        paragraphs = null;
        if (ls != null) {
            for (int i = 0; i < ls.length; i++) {
                ls[i].text.dispose();
            }
        }
    }

    private void ensureParagraphs(int wHint) {
        if (paragraphs != null)
            return;

        if (styled) {
            paragraphs = parseStyledText(text, wHint);
        } else {
            paragraphs = parseSimpleText(text, wHint);
        }
        cachedSize = new Point(0, 0);
        if (paragraphs.length == 0) {
            if (wHint >= 0)
                cachedSize.x = wHint;
        } else {
            for (StyledParagraph p : paragraphs) {
                Rectangle pBounds = p.text.getBounds();
                cachedSize.x = Math.max(cachedSize.x, pBounds.width);
                cachedSize.y = p.top + pBounds.height;
            }
        }
    }

    private StyledParagraph[] parseStyledText(String text, int wHint) {
        List<StyledParagraph> paragraphs = new ArrayList<StyledParagraph>(
                text.length() / 2);
        int contentStart = text.indexOf("<form>"); //$NON-NLS-1$
        if (contentStart >= 0) {
            contentStart += 6;
            int contentEnd = text.indexOf("</form>", contentStart); //$NON-NLS-1$
            if (contentEnd < 0)
                contentEnd = text.length();
            String content = text.substring(contentStart, contentEnd);

            int pStart = 0, pEnd = 0;
            int top = 0;
            String pText;
            StringBuilder pBuffer;
            List<StyleRange> styles;
            boolean bold = false, italic = false;
            while (pStart < content.length()) {
                if ("<p>".equals(content.substring(pStart, pStart + 3))) { //$NON-NLS-1$
                    pStart += 3;
                }
                pEnd = content.indexOf("</p>", pStart); //$NON-NLS-1$
                if (pEnd >= 0) {
                    pText = content.substring(pStart, pEnd);
                    pEnd += 4;
                } else {
                    pEnd = content.indexOf("<p>", pStart); //$NON-NLS-1$
                    if (pEnd >= 0) {
                        pText = content.substring(pStart, pEnd);
                        pEnd += 3;
                    } else {
                        pEnd = content.length();
                        pText = content.substring(pStart, pEnd);
                    }
                }
                pStart = pEnd;

                pBuffer = new StringBuilder(pText.length());
                styles = new ArrayList<StyleRange>(pText.length());

                int start = 0;
                int end = 0;
                while (start < pText.length()) {
                    if ("<b>".equals(pText.substring(start, start + 3))) { //$NON-NLS-1$
                        bold = true;
                        start += 3;
                    } else if ("<i>".equals(pText.substring(start, start + 3))) { //$NON-NLS-1$
                        italic = true;
                        start += 3;
                    } else if ("</b>".equals(pText.substring(start, start + 4))) { //$NON-NLS-1$
                        bold = false;
                        start += 4;
                    } else if ("</i>".equals(pText.substring(start, start + 4))) { //$NON-NLS-1$
                        italic = false;
                        start += 4;
                    } else if (pText.charAt(start) == '<') {
                        end = pText.indexOf('>', start + 1);
                        if (end < 0) {
                            end = pText.length();
                        } else {
                            end += 1;
                        }
                        start = end;
                    } else {
                        end = pText.indexOf('<', start);
                        if (end < 0)
                            end = pText.length();
                        int styleStart = pBuffer.length();
                        pBuffer.append(unescape(pText.substring(start, end)));
                        int styleEnd = pBuffer.length();
                        styles.add(newStyleRange(bold, italic, styleStart,
                                styleEnd));
                        start = end;
                    }
                }

                if (pBuffer.length() == 0) {
                    top += PARAGRAPH_SPACING;
                } else {
                    StyledParagraph paragraph = new StyledParagraph();
                    paragraph.text = new TextLayout(getDisplay());
                    paragraph.text.setFont(getFont());
                    paragraph.text.setAlignment(SWT.LEFT);
                    paragraph.text.setSpacing(LINE_SPACING);
                    paragraph.text.setText(pBuffer.toString());
                    for (StyleRange style : styles) {
                        paragraph.text.setStyle(style, style.start, style.start
                                + style.length);
                    }
                    paragraph.text.setWidth(wHint);
                    paragraph.top = top;
                    paragraphs.add(paragraph);
                    top += paragraph.text.getBounds().height
                            + PARAGRAPH_SPACING;
                }
            }
        }
        return paragraphs.toArray(new StyledParagraph[paragraphs.size()]);
    }

    private StyledParagraph[] parseSimpleText(String text, int wHint) {
        List<StyledParagraph> paragraphs = new ArrayList<StyledLink.StyledParagraph>(
                text.length() / 2);
        int start = 0;
        int end = 0;
        int top = 0;
        StyledParagraph paragraph;
        while (start < text.length()) {
            if (text.charAt(start) == '\r') {
                if (start + 1 < text.length() && text.charAt(start + 1) == '\n') {
                    start += 2;
                } else {
                    start += 1;
                }
                top += PARAGRAPH_SPACING;
            } else if (text.charAt(start) == '\n') {
                start += 1;
                top += PARAGRAPH_SPACING;
            } else {
                end = Math.min(text.indexOf('\n', start),
                        text.indexOf('\r', start));
                if (end < 0)
                    end = text.length();
                if (end > start) {
                    paragraph = new StyledParagraph();
                    paragraph.text = new TextLayout(getDisplay());
                    paragraph.text.setFont(getFont());
                    paragraph.text.setAlignment(SWT.LEFT);
                    paragraph.text.setSpacing(LINE_SPACING);
                    paragraph.text.setText(text.substring(start, end));
                    paragraph.text.setStyle(newStyle(false, false), 0, end
                            - start);
                    paragraph.text.setWidth(wHint);
                    paragraph.top = top;
                    paragraphs.add(paragraph);
                    top += paragraph.text.getBounds().height;
                }
                start = end;
            }
        }
        return paragraphs.toArray(new StyledParagraph[paragraphs.size()]);
    }

    private String unescape(String text) {
        return EncodingUtils.unescape(text);
    }

    private StyleRange newStyleRange(boolean bold, boolean italic, int start,
            int end) {
        StyleRange style = new StyleRange();
        applyStyle(style, bold, italic);
        style.start = start;
        style.length = end - start;
        return style;
    }

    private TextStyle newStyle(boolean bold, boolean italic) {
        TextStyle style = new TextStyle();
        applyStyle(style, bold, italic);
        return style;
    }

    private void applyStyle(TextStyle style, boolean bold, boolean italic) {
        style.background = getBackground();
        style.foreground = getForeground();
        style.underline = isEnabled() && hovered;
        style.underlineColor = getForeground();
        style.underlineStyle = SWT.UNDERLINE_LINK;
        style.strikeout = false;
        style.strikeoutColor = null;
        if (bold && italic) {
            style.font = getBoldItalicFont();
        } else if (bold) {
            style.font = getBoldFont();
        } else if (italic) {
            style.font = getItalicFont();
        } else {
            style.font = getFont();
        }
    }

    private Font getBoldFont() {
        if (boldFont == null) {
            boldFont = new Font(getDisplay(), FontUtils.bold(getFont()
                    .getFontData(), true));
        }
        return boldFont;
    }

    private Font getItalicFont() {
        if (italicFont == null) {
            italicFont = new Font(getDisplay(), FontUtils.italic(getFont()
                    .getFontData(), true));
        }
        return italicFont;
    }

    private Font getBoldItalicFont() {
        if (boldItalicFont == null) {
            boldItalicFont = new Font(getDisplay(), FontUtils.bold(
                    FontUtils.italic(getFont().getFontData(), true), true));
        }
        return boldItalicFont;
    }

    private void drawParagraphs(GC gc) {
        ensureParagraphs(cachedWidth);
        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);

        for (StyledParagraph p : paragraphs) {
            p.text.draw(gc, 0, p.top);
        }
    }

    private void setHovered(boolean hovered) {
        hovered = hovered && isEnabled();
        if (hovered == this.hovered)
            return;
        this.hovered = hovered;
        releaseLines();
        redraw();
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        if (changed || cachedWidth != wHint || paragraphs == null
                || cachedSize == null) {
            releaseLines();
            ensureParagraphs(wHint);
            cachedWidth = wHint;
        }
        return new Point(cachedSize.x, cachedSize.y);
    }

    @Override
    public void dispose() {
        releaseLines();
        if (boldFont != null) {
            boldFont.dispose();
            boldFont = null;
        }
        if (italicFont != null) {
            italicFont.dispose();
            italicFont = null;
        }
        if (boldItalicFont != null) {
            boldItalicFont.dispose();
            boldItalicFont = null;
        }
        super.dispose();
    }

    public void setText(String text) {
        checkWidget();
        if (text == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (text.equals(this.text))
            return;
        this.text = text;
        releaseLines();
        redraw();
    }

    public String getText() {
        return text;
    }

}