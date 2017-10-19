/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.richtext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

/**
 * @author Frank Shaka
 */
public class RichDocument extends Document implements IRichDocument {

    private StyleRange[] textStyles = EMPTY_TEXT_STYLES;

    private ImagePlaceHolder[] images = EMPTY_IMAGES;

    private LineStyle[] lineStyles = EMPTY_LINE_STYLES;

    private Hyperlink[] hyperlinks = EMPTY_HYPERLINK;

    private List<IRichDocumentListener> listeners = null;

    public RichDocument() {
    }

    public RichDocument(String initialContent) {
        super(initialContent);
    }

    public void addRichDocumentListener(IRichDocumentListener listener) {
        if (listeners == null)
            listeners = new ArrayList<IRichDocumentListener>();
        listeners.add(listener);
    }

    public void removeRichDocumentListener(IRichDocumentListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
        if (listeners.isEmpty())
            listeners = null;
    }

    protected void fireTextStylesChanged(final StyleRange[] oldTextStyles,
            final StyleRange[] newTextStyles) {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IRichDocumentListener) listener).textStyleChanged(
                            RichDocument.this, oldTextStyles, newTextStyles);
                }
            });
        }
    }

    protected void fireImagesChanged(final ImagePlaceHolder[] oldImages,
            final ImagePlaceHolder[] newImages) {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IRichDocumentListener) listener).imageChanged(
                            RichDocument.this, oldImages, newImages);
                }
            });
        }
    }

    protected void fireLineStylesChanged(final LineStyle[] oldLineStyles,
            final LineStyle[] newLineStyles) {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IRichDocumentListener) listener).lineStyleChanged(
                            RichDocument.this, oldLineStyles, newLineStyles);
                }
            });
        }
    }

    protected void fireHyperlinkChanged(final Hyperlink[] oldHyperlinks,
            final Hyperlink[] newHyperlinks) {
        if (listeners == null)
            return;
        for (final Object listener : listeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IRichDocumentListener) listener).hyperlinkChanged(
                            RichDocument.this, oldHyperlinks, newHyperlinks);
                }
            });
        }
    }

    public StyleRange findTextStyle(int offset, int length) {
        int end = length == 0 ? 0 : 1;
        for (StyleRange sr : textStyles) {
            if (sr.start <= offset && offset <= sr.start + sr.length - end)
                return sr;
        }
        return null;
    }

    public Image findImage(int offset) {
        for (ImagePlaceHolder imagePlaceHolder : images) {
            if (imagePlaceHolder.offset == offset)
                return imagePlaceHolder.image;
        }
        return null;
    }

    public LineStyle findLineStyle(int startLine) {
        for (LineStyle style : lineStyles) {
            if (style.lineIndex == startLine)
                return style;
        }
        return null;
    }

    public Hyperlink findHyperlink(int offset) {
        for (Hyperlink hyper : hyperlinks) {
            int start = hyper.start;
            int end = hyper.end();
            if (start <= offset && offset <= end)
                return hyper;
        }
        return null;
    }

    public ImagePlaceHolder[] getImages() {
        return images;
    }

    public LineStyle[] getLineStyles() {
        return lineStyles;
    }

    public StyleRange[] getTextStyles() {
        return textStyles;
    }

    public Hyperlink[] getHyperlinks() {
        return hyperlinks;
    }

    public void setImages(ImagePlaceHolder[] images) {
        if (images == null)
            images = EMPTY_IMAGES;
        ImagePlaceHolder[] oldImages = this.images;
        ImagePlaceHolder[] newImages = images;
        if (!equals(oldImages, newImages)) {
            this.images = images;
            fireImagesChanged(oldImages, newImages);
        }
    }

    public void setLineStyles(LineStyle[] lines) {
        if (lines == null)
            lines = EMPTY_LINE_STYLES;
        LineStyle[] oldLineStyles = this.lineStyles;
        LineStyle[] newLineStyles = lines;
        if (!equals(oldLineStyles, newLineStyles)) {
            this.lineStyles = lines;
            fireLineStylesChanged(oldLineStyles, newLineStyles);
        }
    }

    public void setTextStyles(StyleRange[] styles) {
        if (styles == null)
            styles = EMPTY_TEXT_STYLES;
        StyleRange[] oldTextStyles = this.textStyles;
        StyleRange[] newTextStyles = styles;
        if (!equals(oldTextStyles, newTextStyles)) {
            this.textStyles = styles;
            fireTextStylesChanged(oldTextStyles, newTextStyles);
        }
    }

    public void setHyperlinks(Hyperlink[] hyperlinks) {
        if (hyperlinks == null)
            hyperlinks = EMPTY_HYPERLINK;
        Hyperlink[] oldHyperlinks = this.hyperlinks;
        Hyperlink[] newhyperlinks = hyperlinks;
        if (!equals(oldHyperlinks, newhyperlinks)) {
            this.hyperlinks = hyperlinks;
            fireHyperlinkChanged(oldHyperlinks, newhyperlinks);
        }
    }

    private static boolean equals(Object[] a1, Object[] a2) {
        return Arrays.equals(a1, a2);
    }

}