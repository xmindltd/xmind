package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

public class RichTextScanner implements ITokenScanner {

    private static final String regex = "\\b(https?|ftp|mailto|svn|irc|gopher|telnet|nntp|worldwind|news):(?:/{1,3})([\\w-]+\\.)+[\\w-]+[\\w-._/\\~%-+&#?!=()@:]*";//$NON-NLS-1$

    private IRichDocument document;

    private int offset;

    private int length;

    private StyleRange[] textStyles;

    private int styleIndex;

    private Hyperlink[] hyperlinks;

    private int hyperlinkIndex;

    private int lastOffset;

    private int lastLength;

    private List<AutoHyperlink> autoHyperRanges;

    private int autoIndex;

    public void setRange(IDocument document, int offset, int length) {
        if (!(document instanceof IRichDocument))
            return;
        this.document = (IRichDocument) document;
        this.offset = offset;
        this.length = length;
        this.textStyles = this.document.getTextStyles();
        this.styleIndex = 0;
        this.hyperlinks = this.document.getHyperlinks();
        this.hyperlinkIndex = 0;
        this.lastOffset = offset;
        this.lastLength = 0;
        getHyperlinkAt(offset);
        refreshAutoHyperlinks();
        this.autoIndex = 0;
    }

    public int getTokenLength() {
        return lastLength;
    }

    public int getTokenOffset() {
        return lastOffset;
    }

    public IToken nextToken() {

        int current = lastOffset + lastLength;
        if (current >= offset + length) {
            return Token.EOF;
        }
        int next = offset + length;
        Hyperlink h = getHyperlinkAt(current);
        AutoHyperlink a = getAutoHyperlinkAt(current);

        int hyperNext = 0;
        if (h != null) {
            if (current >= h.start) {
                hyperNext = Math.min(next, h.start + h.length);
            } else {
                hyperNext = Math.min(next, h.start);
            }
        }
        int autoNext = 0;
        if (a != null) {
            if (current >= a.start)
                autoNext = Math.min(next, a.start + a.length);
            else
                autoNext = Math.min(next, a.start);
        }
        int tempStart = 0, tempLength = 0;
        if (h != null && a != null) {
            if (h.start < a.start) {
                next = hyperNext;
                tempStart = h.start;
                tempLength = h.length;
            } else {
                next = autoNext;
                tempStart = a.start;
                tempLength = a.length;
            }
        } else if (h != null && a == null) {
            next = hyperNext;
            tempStart = h.start;
            tempLength = h.length;
        } else if (a != null && h == null) {
            next = autoNext;
            tempStart = a.start;
            tempLength = a.length;
        }

        StyleRange style = getTextStyleAt(current);
        if (style != null) {
            if (current >= style.start) {
                next = Math.min(next, style.start + style.length);
            } else {
                next = Math.min(next, style.start);
            }
        }

        this.lastOffset = current;
        this.lastLength = next - current;
        if (lastLength <= 0) {
            return Token.EOF;
        }

        if (!(style != null && current >= style.start && next <= style.start
                + style.length)) {
            style = null;
        }

//        if (h != null && current >= h.start && next <= h.start + h.length) {
//        if (a != null && current >= a.start && next <= a.start + a.len) {
        if (h != null || a != null) {
            if (current >= tempStart && next <= tempStart + tempLength) {
                if (style == null)
                    style = RichTextUtils.DEFAULT_STYLE;
                style = (StyleRange) style.clone();
                if (style.foreground == null
                        || style.foreground == RichTextUtils.DEFAULT_FOREGROUND
                        || style.foreground
                                .equals(RichTextUtils.DEFAULT_FOREGROUND)) {
                    style.foreground = Display.getCurrent()
                            .getSystemColor(SWT.COLOR_BLUE);
                }
                style.underline = true;
            }
        }
        return new Token(style);
    }

    public IToken nextToken1() {
        int current = lastOffset + lastLength;
        if (current >= offset + length) {
            return Token.EOF;
        }

        int next = offset + length;
        Hyperlink h = getHyperlinkAt(current);
        if (h != null) {
            if (current >= h.start) {
                next = Math.min(next, h.start + h.length);
            } else {
                next = Math.min(next, h.start);
            }
        }

        StyleRange style = getTextStyleAt(current);
        if (style != null) {
            if (current >= style.start) {
                next = Math.min(next, style.start + style.length);
            } else {
                next = Math.min(next, style.start);
            }
        }

        this.lastOffset = current;
        this.lastLength = next - current;
        if (lastLength <= 0) {
            return Token.EOF;
        }

        if (!(style != null && current >= style.start && next <= style.start
                + style.length)) {
            style = null;
        }

        if (h != null && current >= h.start && next <= h.start + h.length) {
            if (style == null)
                style = RichTextUtils.DEFAULT_STYLE;
            style = (StyleRange) style.clone();
            if (style.foreground == null
                    || style.foreground == RichTextUtils.DEFAULT_FOREGROUND
                    || style.foreground
                            .equals(RichTextUtils.DEFAULT_FOREGROUND)) {
                style.foreground = Display.getCurrent()
                        .getSystemColor(SWT.COLOR_BLUE);
            }
            style.underline = true;
        }
        return new Token(style);
    }

    private Hyperlink getHyperlinkAt(int position) {
        while (hyperlinkIndex < hyperlinks.length) {
            Hyperlink h = hyperlinks[hyperlinkIndex];
            if (position < h.start + h.length)
                return h;
            hyperlinkIndex++;
        }
        return null;
    }

    private AutoHyperlink getAutoHyperlinkAt(int position) {
        if (autoHyperRanges == null)
            return null;
        AutoHyperlink[] autoArray = autoHyperRanges
                .toArray(new AutoHyperlink[0]);
        while (autoIndex < autoArray.length) {
            AutoHyperlink autoHyperRange = autoArray[autoIndex];
            if (position < autoHyperRange.start + autoHyperRange.length)
                return autoHyperRange;
            autoIndex++;
        }
        return null;
    }

    private StyleRange getTextStyleAt(int position) {
        while (styleIndex < textStyles.length) {
            StyleRange style = textStyles[styleIndex];
            if (position < style.start + style.length)
                return style;
            styleIndex++;
        }
        return null;
    }

    private void refreshAutoHyperlinks() {
        if (autoHyperRanges != null)
            autoHyperRanges = null;
        String content = document.get();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            int start = matcher.start();
            int length = matcher.end() - start;
            if (autoHyperRanges == null)
                autoHyperRanges = new ArrayList<AutoHyperlink>();
            autoHyperRanges.add(new AutoHyperlink(start, length));
        }
    }
}
