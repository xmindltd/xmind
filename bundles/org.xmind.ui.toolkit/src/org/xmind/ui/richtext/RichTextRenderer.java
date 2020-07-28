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

import static org.xmind.ui.richtext.IRichDocument.EMPTY_HYPERLINK;
import static org.xmind.ui.richtext.IRichDocument.EMPTY_IMAGES;
import static org.xmind.ui.richtext.IRichDocument.EMPTY_LINE_STYLES;
import static org.xmind.ui.richtext.IRichDocument.EMPTY_TEXT_STYLES;
import static org.xmind.ui.richtext.ImagePlaceHolder.PLACE_HOLDER;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.viewers.SWTUtils;

/**
 * @author Frank Shaka
 */
public class RichTextRenderer implements IRichTextRenderer {

    private class DocumentListener implements IDocumentListener {
        public void documentAboutToBeChanged(DocumentEvent event) {
            lineRangeBeforeChange = getLineRange(event.getOffset(),
                    event.getLength());
        }

        public void documentChanged(DocumentEvent event) {
            if (ignoreDocumentChange)
                return;
            updateDocumentPositions(event);
        }
    }

    private class RichDocumentListener implements IRichDocumentListener {

        public void imageChanged(IRichDocument document,
                ImagePlaceHolder[] oldStyles, ImagePlaceHolder[] newStyles) {
            asyncRefreshViewer();
        }

        public void lineStyleChanged(IRichDocument document,
                LineStyle[] oldStyles, LineStyle[] newStyles) {
            asyncRefreshViewer();
        }

        public void textStyleChanged(IRichDocument document,
                StyleRange[] oldStyles, StyleRange[] newStyles) {
            asyncRefreshViewer();
        }

        public void hyperlinkChanged(IRichDocument document,
                Hyperlink[] oldHyperlinks, Hyperlink[] newHyperlinks) {
            asyncRefreshViewer();
        }

    }

    private TextViewer viewer;

    private IRichDocument document;

    private IDocumentListener documentChangeHandler;

    private IRichDocumentListener richDocumentChangeHandler;

    private StyleRange selectionTextStyle;

    private LineStyle selectionLineStyle;

    private boolean ignoreDocumentChange = false;

    private Point lineRangeBeforeChange = null;

    private boolean asyncRefreshing = false;

    public RichTextRenderer(TextViewer viewer) {
        this.viewer = viewer;
        initialize(viewer);
    }

    private void initialize(final TextViewer viewer) {
        this.documentChangeHandler = new DocumentListener();
        this.richDocumentChangeHandler = new RichDocumentListener();
        IDocument doc = viewer.getDocument();
        if (doc instanceof IRichDocument) {
            this.document = (IRichDocument) doc;
            this.document.addDocumentListener(documentChangeHandler);
            this.document.addRichDocumentListener(richDocumentChangeHandler);
        }

        viewer.addTextInputListener(new ITextInputListener() {
            public void inputDocumentChanged(IDocument oldInput,
                    IDocument newInput) {
                if (oldInput != newInput) {
                    if (document != null) {
                        document.removeDocumentListener(documentChangeHandler);
                        document.removeRichDocumentListener(
                                richDocumentChangeHandler);
                    }
                    if (newInput != null && newInput instanceof IRichDocument) {
                        document = (IRichDocument) newInput;
                        document.addDocumentListener(documentChangeHandler);
                        document.addRichDocumentListener(
                                richDocumentChangeHandler);
                    } else {
                        document = null;
                    }
                    initialize();
                }
            }

            public void inputDocumentAboutToBeChanged(IDocument oldInput,
                    IDocument newInput) {
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection sel = event.getSelection();
                if (sel instanceof ITextSelection) {
                    ITextSelection ts = (ITextSelection) sel;
                    int offset = ts.getOffset();
                    int length = ts.getLength();
                    updateSelectionTextStyle(offset, length);
                    if (document != null) {
                        try {
                            int line = document.getLineOfOffset(offset);
                            updateSelectionLineStyle(line);
                        } catch (BadLocationException e) {
                        }
                    }
                }
            }

        });
        viewer.getTextWidget()
                .addPaintObjectListener(new PaintObjectListener() {
                    public void paintObject(PaintObjectEvent event) {
//                        if (document == null)
//                            return;

                        StyleRange style = event.style;
                        int start = style.start;
                        Image image = document.findImage(start);
//                        Image image = (Image) style.data;
                        if (image != null && !image.isDisposed()) {
                            GC gc = event.gc;
                            int x = event.x;
                            GlyphMetrics metrics = style.metrics;
                            if (metrics != null) {
                                int y = event.y + event.ascent - metrics.ascent;
                                gc.drawImage(image, x, y);
                            }
                        }
                    }
                });

        viewer.getTextWidget().addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0, SWT.CR)) {
                    if (document == null)
                        return;
                    handEntryKey();
                } else if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0,
                        SWT.BS)) {
                    if (document == null)
                        return;
                    handleBackspaceKey();
                }
            }
        });
    }

    private void handEntryKey() {
        if (LineStyle.NONE_STYLE.equals(selectionLineStyle.bulletStyle))
            return;
        List<LineStyle> lineStyles = getModifiableLineStyles();
        if (lineStyles == null || lineStyles.isEmpty())
            return;
        Point point = getSelectedLineRange();
        int startLine = point.x;
        if (find(startLine)) {
            LineStyle lineStyle = findLineStyleAt(startLine);
            lineStyles.remove(lineStyle);
            lineStyles.remove(findLineStyleAt(startLine - 1));
            document.setLineStyles(lineStyles.toArray(EMPTY_LINE_STYLES));
        }
    }

    private boolean find(int startLine) {
        try {
            IRegion r1 = document.getLineInformation(startLine - 1);
            String content1 = document.get(r1.getOffset(), r1.getLength());
            if (!"".equals(content1)) //$NON-NLS-1$
                return false;
            String style1 = findLineStyleAt(startLine - 1).bulletStyle;
            if (LineStyle.NONE_STYLE.equals(style1))
                return false;
            IRegion r = document.getLineInformation(startLine);
            String content2 = document.get(r.getOffset(), r.getLength());
            if (!"".equals(content2)) //$NON-NLS-1$
                return false;
            String style2 = findLineStyleAt(startLine).bulletStyle;
            if (LineStyle.NONE_STYLE.equals(style2))
                return false;
            return true;
        } catch (BadLocationException e) {
        }
        return false;
    }

    private void handleBackspaceKey() {
        try {
            IRegion lineInfo = document.getLineInformation(0);
            if (lineInfo.getLength() == 0 && lineInfo.getOffset() == 0) {
                getBulletModifier(LineStyle.NONE_STYLE).updateViewer(viewer, 0,
                        1);

            }
        } catch (BadLocationException e1) {
        }
    }

    private void initialize() {
        this.selectionTextStyle = (StyleRange) RichTextUtils.DEFAULT_STYLE
                .clone();
        this.ignoreDocumentChange = false;
        refreshViewer();
    }

    private void asyncRefreshViewer() {
        if (asyncRefreshing)
            return;
        asyncRefreshing = true;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                refreshViewer();
                asyncRefreshing = false;
            }
        });
    }

    private void refreshViewer() {
        if (document != null && viewer != null) {
            refreshViewer(document.getTextStyles());

            viewer.invalidateTextPresentation();
            LineStyle[] lineStyles = document.getLineStyles();
            int lineStyleIndex = lineStyles.length == 0 ? -1 : 0;
            LineStyle lineStyle = lineStyleIndex < 0 ? null
                    : lineStyles[lineStyleIndex];
            int lines = document.getNumberOfLines();
            for (int i = 0; i < lines; i++) {
                if (lineStyle != null && i == lineStyle.lineIndex) {
                    getAlignmentModifier(lineStyle.alignment)
                            .updateViewer(viewer, lineStyle.lineIndex, 1);

                    getBulletModifier(lineStyle.bulletStyle)
                            .updateViewer(viewer, lineStyle.lineIndex, 1);
                    lineStyleIndex++;
                    lineStyle = lineStyleIndex >= lineStyles.length ? null
                            : lineStyles[lineStyleIndex];

                } else {
                    getAlignmentModifier(SWT.LEFT).updateViewer(viewer, i, 1);
                    getBulletModifier(LineStyle.NONE_STYLE).updateViewer(viewer,
                            i, 1);
                }
            }
        }
    }

    public StyleRange getSelectionTextStyle() {
        updateSelectionTextStyle();
        return selectionTextStyle;
    }

    private void updateSelectionTextStyle() {
        Point range = viewer.getSelectedRange();
        updateSelectionTextStyle(range.x, range.y);
    }

    private void updateSelectionTextStyle(int offset, int length) {
        if (selectionTextStyle == null || offset != selectionTextStyle.start
                || length != selectionTextStyle.length) {
            StyleRange style = findTextStyleAt(offset, length);
            if (style == null) {
                style = RichTextUtils.DEFAULT_STYLE;
            }
            selectionTextStyle = (StyleRange) style.clone();
            selectionTextStyle.metrics = null;
        }
        selectionTextStyle.start = offset;
        selectionTextStyle.length = length;
    }

    protected StyleRange findTextStyleAt(int offset, int length) {
        return document == null ? null : document.findTextStyle(offset, length);
    }

    protected LineStyle getSelectionLineStyle() {
        updateSelectionLineStyle();
        return selectionLineStyle;
    }

    private void updateSelectionLineStyle() {
        Point range = getSelectedLineRange();
        updateSelectionLineStyle(range.x);
    }

    private void updateSelectionLineStyle(int startLine) {
        if (selectionLineStyle == null
                || selectionLineStyle.lineIndex != startLine) {
            LineStyle lineStyle = findLineStyleAt(startLine);
            if (lineStyle == null) {
                lineStyle = RichTextUtils.DEFAULT_LINE_STYLE;
            }
            selectionLineStyle = (LineStyle) lineStyle.clone();
        }
        selectionLineStyle.lineIndex = startLine;

        LineStyle style = findLineStyleAt(startLine);
        if (style != null)
            selectionLineStyle.bulletStyle = style.bulletStyle;
    }

    private LineStyle findLineStyleAt(int startLine) {
        return document == null ? null : document.findLineStyle(startLine);
    }

    public Color getSelectionBackground() {
        return RichTextUtils.getBackground(getSelectionTextStyle());
    }

    public Font getSelectionFont() {
        return RichTextUtils.getFont(getSelectionTextStyle());
    }

    public boolean getSelectionFontBold() {
        return RichTextUtils.isBold(getSelectionTextStyle());
    }

    public String getSelectionFontFace() {
        return RichTextUtils.getFontFace(getSelectionTextStyle());
    }

    public boolean getSelectionFontItalic() {
        return RichTextUtils.isItalic(getSelectionTextStyle());
    }

    public int getSelectionFontSize() {
        return RichTextUtils.getFontSize(getSelectionTextStyle());
    }

    public boolean getSelectionFontStrikeout() {
        return getSelectionTextStyle().strikeout;
    }

    public boolean getSelectionFontUnderline() {
        return getSelectionTextStyle().underline;
    }

    public Color getSelectionForeground() {
        return RichTextUtils.getForeground(getSelectionTextStyle());
    }

    public int getSelectionParagraphAlignment() {
        return getSelectionLineStyle().alignment;
    }

    public int getSelectionParagraphIndent() {
        return getSelectionLineStyle().indent;
    }

    public boolean getBulletSelectionParagraph() {
        LineStyle lineStyle = getSelectionLineStyle();
        return LineStyle.BULLET.equals(lineStyle.bulletStyle);
    }

    public boolean getNumberSelectionParagraph() {
        LineStyle lineStyle = getSelectionLineStyle();
        return LineStyle.NUMBER.equals(lineStyle.bulletStyle);
    }

    public void indentSelectionParagraph() {
        modifySelectionLineStyles(getIndentModifier(1));
    }

    public void bulletSelectionParagraph(boolean bullet) {
        String bulletStyle = bullet ? LineStyle.BULLET : LineStyle.NONE_STYLE;
        modifySelectionLineStyles(getBulletModifier(bulletStyle));
    }

    public void numberSelectionParagraph(boolean number) {
        String bulletStyle = number ? LineStyle.NUMBER : LineStyle.NONE_STYLE;
        modifySelectionLineStyles(getBulletModifier(bulletStyle));
    }

    public void insertHyperlink(String href) {
        insertHyperlink(href, null);
    }

    public void insertHyperlink(String href, String displayText) {
        Point range = viewer.getSelectedRange();

        updateSelectionTextStyle();
        commitLastChange();
        beginCompoundChange();

        range = modifyHyperlinksPosition(range);
        int start = range.x;
        int oldLength = range.y;

        int newLength = oldLength;
        if (displayText != null) {
            newLength = displayText.length();
            try {
                document.replace(start, oldLength, displayText);
            } catch (BadLocationException e) {
                e.printStackTrace();
                return;
            }
        }
        addHyperlinkToDocument(start, newLength, href);
        setSelectedRange(start + newLength, 0);
        endCompoundChange();
        commitLastChange();

    }

    private Point modifyHyperlinksPosition(Point range) {
        int start = range.x;
        int oldLength = range.y;
        int oldEnd = start + oldLength;
        int length = document.getLength();
        if (start == length)
            return range;
        List<Hyperlink> hyperlinks = getModifiableHyperlinks();
        for (int i = 0; i < hyperlinks.size(); i++) {
            Hyperlink hyper = hyperlinks.get(i);
            int hyperStart = hyper.start;
            int hyperEnd = hyper.end();
            if (oldEnd <= hyperStart)
                break;
            if (start >= hyperStart && oldEnd <= hyperEnd) {
                start = hyperStart;
                oldLength = hyperEnd - hyperStart;
                break;
            }
        }
        return new Point(start, oldLength);
    }

    private void addHyperlinkToDocument(int start, int newLength,
            String hyperlink) {
        int end = start + newLength;
        List<Hyperlink> oldHyperlinks = getModifiableHyperlinks();

        for (int i = 0; i < oldHyperlinks.size(); i++) {
            Hyperlink indexHyper = oldHyperlinks.get(i);
            if (end <= indexHyper.start)
                break;
            int hyperStart = indexHyper.start;
            int hyperEnd = indexHyper.end();
            if (hyperStart < end && start < hyperEnd) {
                if (end <= hyperEnd && start < hyperStart) {
                    indexHyper.length = hyperEnd - end;
                    indexHyper.start = end;
                }
                if (start > hyperStart && end > hyperEnd) {
                    indexHyper.length = end - hyperStart;
                }
                if (end <= hyperEnd && start >= hyperStart) {
                    oldHyperlinks.remove(i);
                    break;
                }
            }
        }

        int index = getInsertHyperlinkIndex(start, oldHyperlinks);
        Hyperlink hyper = new Hyperlink(start, newLength, hyperlink);
        oldHyperlinks.add(index, hyper);
        document.setHyperlinks(oldHyperlinks.toArray(EMPTY_HYPERLINK));
    }

    private List<Hyperlink> getModifiableHyperlinks() {
        Hyperlink[] oldHyperlinks = document.getHyperlinks();
        List<Hyperlink> newHyperlinks = new ArrayList<Hyperlink>(
                oldHyperlinks.length);
        for (Hyperlink hyper : oldHyperlinks)
            newHyperlinks.add((Hyperlink) hyper.clone());
        return newHyperlinks;
    }

    private int getInsertHyperlinkIndex(int offset,
            List<Hyperlink> hyperlinks) {
        int i;
        for (i = 0; i < hyperlinks.size(); i++) {
            Hyperlink hyperlink = hyperlinks.get(i);
            if (hyperlink.start >= offset)
                return i;
        }
        return i;
    }

    public Hyperlink[] getSelectionHyperlinks() {
        Point range = viewer.getSelectedRange();
        Hyperlink[] hyperlinks = document.getHyperlinks();
        List<Hyperlink> list = RichTextUtils.getHyperlinksInRange(hyperlinks,
                range.x, range.x + range.y);
        return list.toArray(new Hyperlink[list.size()]);
    }

    public void outdentSelectionParagraph() {
        modifySelectionLineStyles(getIndentModifier(-1));
    }

    public void insertImage(Image image) {
        Point range = viewer.getSelectedRange();
        int start = range.x;
        int oldLength = range.y;

        String placeHolder = PLACE_HOLDER;
        int newLength = placeHolder.length();

        updateSelectionTextStyle();
        commitLastChange();
        beginCompoundChange();

        try {
            document.replace(start, oldLength, placeHolder);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }

        addImageToDocument(image, start);
        addImageStyleRangeToDocument(image, start, newLength);

        setSelectedRange(start + newLength, 0);
        endCompoundChange();
        commitLastChange();

    }

    public void setSelectionBackground(Color color) {
        modifySelectionTextStyles(getBackgroundModifier(color));
    }

    public void setSelectionFont(Font font) {
        modifySelectionTextStyles(getFontModifier(font));
    }

    public void setSelectionFontBold(final boolean bold) {
        modifySelectionTextStyles(getBoldModifier(bold));
    }

    public void setSelectionFontFace(String fontFace) {
        modifySelectionTextStyles(getFontFaceModifier(fontFace));
    }

    public void setSelectionFontItalic(final boolean italic) {
        modifySelectionTextStyles(getItalicModifier(italic));
    }

    public void setSelectionFontSize(int size) {
        modifySelectionTextStyles(getFontSizeModifier(size));
    }

    public void setSelectionFontStrikeout(boolean strikeout) {
        modifySelectionTextStyles(getStrikeoutModifier(strikeout));
    }

    public void setSelectionFontUnderline(boolean underline) {
        modifySelectionTextStyles(getUnderlineModifier(underline));
    }

    public void setSelectionForeground(Color color) {
        modifySelectionTextStyles(getForegroundModifier(color));
    }

    public void setSelectionParagraphAlignment(int alignment) {
        modifySelectionLineStyles(getAlignmentModifier(alignment));
    }

    public void setSelectionParagraphIndent(int insertIndent) {
        modifySelectionLineStyles(getIndentReplacer(insertIndent));
    }

    protected void updateDocumentPositions(DocumentEvent event) {
        if (document == null)
            return;
        int start = event.getOffset();//the position of Cursor
        int oldLength = event.getLength();//the selection text's length
        int newLength = event.getText().length();//later input the text's length

        updateTextStylesInDocument(start, oldLength, newLength);
        updateLineStylesInDocument(start, oldLength, newLength,
                event.getText());
        updateImagesInDocument(start, oldLength, newLength);

        updateHyperlinksInDocument(start, oldLength, newLength);
    }

    private void updateHyperlinksInDocument(int start, int oldLength,
            int newLength) {
        List<Hyperlink> hyperlinks = getModifiableHyperlinks();
        RichTextUtils.updateHyperlinksPositions(start, oldLength, newLength,
                hyperlinks);
        Hyperlink[] modifiedHyperlinks = hyperlinks.toArray(EMPTY_HYPERLINK);
        document.setHyperlinks(modifiedHyperlinks);
    }

    private void updateTextStylesInDocument(int start, int oldLength,
            int newLength) {
        List<StyleRange> styles = getModifiableTextStyles();
        updateSelectionTextStyle(start, oldLength);
        RichTextUtils.replaceStyleRanges(start, oldLength, newLength, styles,
                selectionTextStyle);
        StyleRange[] modifiedStyleRanges = styles.toArray(EMPTY_TEXT_STYLES);
        document.setTextStyles(modifiedStyleRanges);
        selectionTextStyle.start = (start + newLength);
        selectionTextStyle.length = 0;
    }

    private void updateLineStylesInDocument(int start, int oldLength,
            int newLength, String text) {
        if (lineRangeBeforeChange == null || lineRangeBeforeChange.x < 0
                || lineRangeBeforeChange.y < 0)
            return;

        Point currentLineRange = getLineRange(start, newLength);
        int startLine = currentLineRange.x;
        int lineCount = currentLineRange.y;
        if (startLine < 0 || lineCount < 0)
            return;

        List<LineStyle> lineStyles = getModifiableLineStyles();
        RichTextUtils.updateLineStylePositions(startLine,
                lineRangeBeforeChange.y, currentLineRange.y, lineStyles,
                document);
        LineStyle[] modifiedLineStyles = lineStyles.toArray(EMPTY_LINE_STYLES);
        document.setLineStyles(modifiedLineStyles);

        lineRangeBeforeChange = null;
    }

    private void updateImagesInDocument(int start, int oldLength,
            int newLength) {
        List<ImagePlaceHolder> images = getModifiableImages();
        RichTextUtils.updateImagePositions(start, oldLength, newLength, images);
        ImagePlaceHolder[] modifiedImages = images.toArray(EMPTY_IMAGES);
        document.setImages(modifiedImages);
    }

    private List<StyleRange> getModifiableTextStyles() {
        StyleRange[] oldTextStyles = document.getTextStyles();
        List<StyleRange> newStyles = new ArrayList<StyleRange>(
                oldTextStyles.length);
        for (StyleRange textStyle : oldTextStyles) {
            newStyles.add((StyleRange) textStyle.clone());
        }
        return newStyles;
    }

    private void addImageStyleRangeToDocument(Image image, int start,
            int length) {
        List<StyleRange> styles = getModifiableTextStyles();
        StyleRange imageStyle = createImageStyle(start, length, image);
        RichTextUtils.replaceStyleRanges(start, length, length, styles,
                imageStyle);
        StyleRange[] modifiedStyleRanges = styles.toArray(EMPTY_TEXT_STYLES);
        document.setTextStyles(modifiedStyleRanges);
    }

    private void refreshViewer(StyleRange[] modifiedStyleRanges) {
        TextPresentation presentation = createPresentation(0,
                document.getLength(), modifiedStyleRanges);
        viewer.changeTextPresentation(presentation, true);
    }

    private void addImageToDocument(Image image, int start) {
        List<ImagePlaceHolder> oldImages = getModifiableImages();
        int index = getInsertImageIndex(start, oldImages);
        ImagePlaceHolder imagePlaceHolder = new ImagePlaceHolder(start, image);
        oldImages.add(index, imagePlaceHolder);
        document.setImages(oldImages.toArray(EMPTY_IMAGES));
    }

    private List<ImagePlaceHolder> getModifiableImages() {
        ImagePlaceHolder[] oldImages = document.getImages();
        ArrayList<ImagePlaceHolder> newImages = new ArrayList<ImagePlaceHolder>(
                oldImages.length);
        for (ImagePlaceHolder img : oldImages) {
            newImages.add((ImagePlaceHolder) img.clone());
        }
        return newImages;
    }

    private List<LineStyle> getModifiableLineStyles() {
        LineStyle[] oldLineStyles = document.getLineStyles();
        ArrayList<LineStyle> newLineStyles = new ArrayList<LineStyle>(
                oldLineStyles.length);
        for (LineStyle line : oldLineStyles) {
            newLineStyles.add((LineStyle) line.clone());
        }
        return newLineStyles;
    }

    private int getInsertImageIndex(int offset, List<ImagePlaceHolder> images) {
        int i;
        for (i = 0; i < images.size(); i++) {
            ImagePlaceHolder image = images.get(i);
            if (image.offset >= offset)
                return i;
        }
        return i;
    }

    private void setSelectedRange(int start, int length) {
        viewer.setSelectedRange(start, length);
        selectionTextStyle.start = start;
        selectionTextStyle.length = length;
    }

    private StyleRange createImageStyle(int start, int length, Image image) {
        StyleRange style = (StyleRange) selectionTextStyle.clone();
//        style.data = image;
        style.start = start;
        style.length = length;
        Rectangle rect = image.getBounds();
        style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
        return style;
    }

    private void modifySelectionTextStyles(IStyleRangeModifier modifier) {
        if (document == null || modifier == null)
            return;
        commitLastChange();
        beginCompoundChange();
        modifier.modify(getSelectionTextStyle());
        Point range = viewer.getSelectedRange();
        int start = range.x;
        int length = range.y;
        modifyTextStyles(start, length, modifier);
        endCompoundChange();
        commitLastChange();
    }

    protected void beginCompoundChange() {
        if (viewer != null) {
            IUndoManager undoManager = viewer.getUndoManager();
            if (undoManager != null) {
                undoManager.beginCompoundChange();
            }
        }
    }

    protected void endCompoundChange() {
        if (viewer != null) {
            IUndoManager undoManager = viewer.getUndoManager();
            if (undoManager != null) {
                undoManager.endCompoundChange();
            }
        }
    }

    protected void commitLastChange() {
        if (viewer != null) {
            IUndoManager undoManager = viewer.getUndoManager();
            if (undoManager != null
                    && undoManager instanceof RichTextViewerUndoManager) {
                ((RichTextViewerUndoManager) undoManager).commit();
            }
        }
    }

    private void modifyTextStyles(int start, int length,
            IStyleRangeModifier modifier) {
        if (document == null || modifier == null || length <= 0)
            return;
        List<StyleRange> styles = getModifiableTextStyles();
        boolean changed = RichTextUtils.modifyTextStyles(start, length, styles,
                modifier);
        if (changed) {
            StyleRange[] modifiedStyleRanges = styles
                    .toArray(EMPTY_TEXT_STYLES);
            document.setTextStyles(modifiedStyleRanges);
        }
    }

    private static TextPresentation createPresentation(int start, int length,
            StyleRange[] styleRanges) {
        TextPresentation presentation = new TextPresentation();
        StyleRange defaultStyle = createDefaultStyle(start, length);
        presentation.setDefaultStyleRange(defaultStyle);
        presentation.replaceStyleRanges(styleRanges);
        return presentation;
    }

    private static StyleRange createDefaultStyle(int start, int length) {
        StyleRange defaultStyle = (StyleRange) RichTextUtils.DEFAULT_STYLE
                .clone();
        defaultStyle.start = start;
        defaultStyle.length = length;
        return defaultStyle;
    }

    private void modifySelectionLineStyles(ILineStyleModifier modifier) {
        if (document == null || modifier == null)
            return;
        Point p = getSelectedLineRange();
        if (p.x < 0 || p.y < 0)
            return;
        commitLastChange();
        beginCompoundChange();
        int startLine = p.x;
        int lineCount = p.y;
        modifyLineStyles(startLine, lineCount, modifier);
        endCompoundChange();
        commitLastChange();
    }

    private Point getSelectedLineRange() {
        Point p = viewer.getSelectedRange();
        int offset = p.x;
        int length = p.y;
        return getLineRange(offset, length);
    }

    private Point getLineRange(int offset, int length) {
        if (document != null) {
            try {
                int startLine = document.getLineOfOffset(offset);
                int lineCount = document.getNumberOfLines(offset, length);
                return new Point(startLine, lineCount);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return new Point(-1, -1);
    }

    private void modifyLineStyles(int startLine, int lineCount,
            ILineStyleModifier modifier) {
        if (document == null || modifier == null || lineCount <= 0)
            return;
        List<LineStyle> lineStyles = getModifiableLineStyles();
        boolean changed = RichTextUtils.modifyLineStyles(startLine, lineCount,
                lineStyles, modifier);
        if (changed) {
            LineStyle[] modifiedLineStyles = lineStyles
                    .toArray(EMPTY_LINE_STYLES);
            modifier.updateViewer(viewer, startLine, lineCount);
            document.setLineStyles(modifiedLineStyles);
        }
    }

    private static StyleRangeModifier boldModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setBold(style, (Boolean) value);
        }
    };

    private static IStyleRangeModifier getBoldModifier(boolean bold) {
        boldModifier.setValue(bold);
        return boldModifier;
    }

    private static StyleRangeModifier italicModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setItalic(style, (Boolean) value);
        }
    };

    private static IStyleRangeModifier getItalicModifier(boolean italic) {
        italicModifier.setValue(italic);
        return italicModifier;
    }

    private static StyleRangeModifier underlineModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            boolean v = (Boolean) value;
            if (style.underline == v)
                return false;
            style.underline = v;
            return true;
        }
    };

    private static IStyleRangeModifier getUnderlineModifier(boolean italic) {
        underlineModifier.setValue(italic);
        return underlineModifier;
    }

    private static StyleRangeModifier strikeoutModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            boolean v = (Boolean) value;
            if (style.strikeout == v)
                return false;
            style.strikeout = v;
            return true;
        }
    };

    private static IStyleRangeModifier getStrikeoutModifier(boolean italic) {
        strikeoutModifier.setValue(italic);
        return strikeoutModifier;
    }

    private static StyleRangeModifier faceModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setFontFace(style, (String) value);
        }
    };

    private static IStyleRangeModifier getFontFaceModifier(String face) {
        faceModifier.setValue(face);
        return faceModifier;
    }

    private static StyleRangeModifier sizeModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setFontSize(style, (Integer) value);
        }
    };

    private static IStyleRangeModifier getFontSizeModifier(int size) {
        sizeModifier.setValue(size);
        return sizeModifier;
    }

    private static StyleRangeModifier fontModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setFont(style, (Font) value);
        }
    };

    private static IStyleRangeModifier getFontModifier(Font value) {
        fontModifier.setValue(value);
        return fontModifier;
    }

    private static StyleRangeModifier foregroundModifier = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setForeground(style, (Color) value);
        }
    };

    private static IStyleRangeModifier getForegroundModifier(Color color) {
        foregroundModifier.setValue(color);
        return foregroundModifier;
    }

    private static StyleRangeModifier backgroundModifer = new StyleRangeModifier() {
        protected boolean modify(StyleRange style, Object value) {
            return RichTextUtils.setBackground(style, (Color) value);
        }
    };

    private static IStyleRangeModifier getBackgroundModifier(Color color) {
        backgroundModifer.setValue(color);
        return backgroundModifer;
    }

    private static LineStyleModifier alignmentModifier = new LineStyleModifier() {
        protected boolean modify(LineStyle style, Object value) {
            int alignment = (Integer) value;
            if (style.alignment == alignment)
                return false;
            style.alignment = alignment;
            return true;
        }

        protected void updateViewer(TextViewer viewer, int startLine,
                int lineCount, Object value) {
            int alignment = (Integer) value;
            int endLine = startLine + lineCount;
            StyledText textWidget = viewer.getTextWidget();
            for (int line = startLine; line < endLine; line++) {
                int wLine = viewer.modelLine2WidgetLine(line);
                textWidget.setLineAlignment(wLine, 1, alignment);
            }
        }
    };

    private static ILineStyleModifier getAlignmentModifier(int alignment) {
        alignmentModifier.setValue(alignment);
        return alignmentModifier;
    }

    private static LineStyleModifier indentReplacer = new LineStyleModifier() {
        protected boolean modify(LineStyle style, Object value) {
            int indent = (Integer) value;
            if (indent == style.indent)
                return false;
            style.indent = indent;
            return true;
        }

        protected void updateViewer(TextViewer viewer, int startLine,
                int lineCount, Object value) {
            IDocument document = viewer.getDocument();
            if (document == null)
                return;
            int indent = (Integer) value;
            int endLine = startLine + lineCount;

            //restore old selection.
            Point range = viewer.getSelectedRange();
            range = new Point(range.x, range.y);
            try {
                for (int line = startLine; line < endLine; line++) {
                    int realDeltaIndent = RichTextUtils
                            .replaceDocumentIndent(document, line, indent);

                    if (line == startLine) {
                        range.x += realDeltaIndent;
                    } else {
                        range.y += realDeltaIndent;
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            viewer.setSelectedRange(range.x, range.y);
        }
    };

    private static ILineStyleModifier getIndentReplacer(int indent) {
        indentReplacer.setValue(indent);
        return indentReplacer;
    }

    private static LineStyleModifier indentModifier = new LineStyleModifier() {
        protected boolean modify(LineStyle style, Object value) {
            int deltaIndent = (Integer) value;
            if (deltaIndent == 0 || (deltaIndent < 0 && style.indent == 0))
                return false;
            style.indent += deltaIndent;
            return true;
        }

        protected void updateViewer(TextViewer viewer, int startLine,
                int lineCount, Object value) {
            IDocument document = viewer.getDocument();
            if (document == null)
                return;
            int deltaIndent = (Integer) value;
            int endLine = startLine + lineCount;

            //restore old selection.
            Point range = viewer.getSelectedRange();
            range = new Point(range.x, range.y);
            try {
                for (int line = startLine; line < endLine; line++) {
                    int realDeltaIndent = RichTextUtils.modifyDocumentIndent(
                            viewer, document, line, deltaIndent);

                    if (line == startLine) {
                        range.x += realDeltaIndent;
                    } else {
                        range.y += realDeltaIndent;
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            viewer.setSelectedRange(range.x, range.y);
        }
    };

    private static ILineStyleModifier getIndentModifier(int deltaIndent) {
        indentModifier.setValue(deltaIndent);
        return indentModifier;
    }

    private static LineStyleModifier bulletModifier = new LineStyleModifier() {
        protected boolean modify(LineStyle style, Object value) {
            String v = (String) value;
            if (style.bulletStyle.equals(v))
                return false;
            style.bulletStyle = v;
            return true;
        }

        protected void updateViewer(TextViewer viewer, int startLine,
                int lineCount, Object value) {
            String v = (String) value;
            StyledText styledText = viewer.getTextWidget();
            StyleRange style = new StyleRange();
            style.metrics = new GlyphMetrics(0, 0, 50);
            styledText.setLineBullet(startLine, lineCount, null);

            if (v.equals(LineStyle.BULLET)) {
                Bullet bullet = new Bullet(style);
                styledText.setLineBullet(startLine, lineCount, bullet);
            } else if (v.equals(LineStyle.NUMBER)) {
//                Bullet bullet = styledText.getLineBullet(startLine);
//                if (bullet.type != ST.BULLET_CUSTOM)
                Bullet bullet = new Bullet(ST.BULLET_CUSTOM, style);
                styledText.setLineBullet(startLine, lineCount, bullet);
            } else
                styledText.setLineBullet(startLine, lineCount, null);
        }
    };

    private static ILineStyleModifier getBulletModifier(String bullet) {
        bulletModifier.setValue(bullet);
        return bulletModifier;
    }
}