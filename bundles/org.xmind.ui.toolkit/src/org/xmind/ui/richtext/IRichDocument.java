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

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

/**
 * @author Frank Shaka
 */
public interface IRichDocument extends IDocument {

    public static final StyleRange[] EMPTY_TEXT_STYLES = new StyleRange[0];

    public static final LineStyle[] EMPTY_LINE_STYLES = new LineStyle[0];

    public static final ImagePlaceHolder[] EMPTY_IMAGES = new ImagePlaceHolder[0];

    public static final Hyperlink[] EMPTY_HYPERLINK = new Hyperlink[0];

    StyleRange[] getTextStyles();

    void setTextStyles(StyleRange[] styles);

    LineStyle[] getLineStyles();

    void setLineStyles(LineStyle[] lines);

    ImagePlaceHolder[] getImages();

    void setImages(ImagePlaceHolder[] images);

    StyleRange findTextStyle(int offset, int length);

    void setHyperlinks(Hyperlink[] hyperlinks);

    Hyperlink[] getHyperlinks();

    Image findImage(int offset);

    LineStyle findLineStyle(int startLine);

    Hyperlink findHyperlink(int offset);

    void addRichDocumentListener(IRichDocumentListener listener);

    void removeRichDocumentListener(IRichDocumentListener listener);

}