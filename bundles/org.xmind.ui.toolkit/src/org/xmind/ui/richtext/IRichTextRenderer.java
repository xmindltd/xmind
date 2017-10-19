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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author Brian Sun
 * @author Frank Shaka
 */
public interface IRichTextRenderer {

    Color getSelectionBackground();

    Font getSelectionFont();

    boolean getSelectionFontBold();

    String getSelectionFontFace();

    boolean getSelectionFontItalic();

    int getSelectionFontSize();

    boolean getSelectionFontStrikeout();

    boolean getSelectionFontUnderline();

    Color getSelectionForeground();

    int getSelectionParagraphAlignment();

    int getSelectionParagraphIndent();

    Hyperlink[] getSelectionHyperlinks();

    boolean getBulletSelectionParagraph();

    boolean getNumberSelectionParagraph();

    void indentSelectionParagraph();

    void insertHyperlink(String href);

    void insertHyperlink(String href, String displayText);

    void insertImage(Image image);

    void outdentSelectionParagraph();

    void setSelectionBackground(Color color);

    void setSelectionFont(Font font);

    void setSelectionFontBold(boolean bold);

    void setSelectionFontFace(String fontFace);

    void setSelectionFontItalic(boolean italic);

    void setSelectionFontSize(int size);

    void setSelectionFontStrikeout(boolean strikeout);

    void setSelectionFontUnderline(boolean underline);

    void setSelectionForeground(Color color);

    void bulletSelectionParagraph(boolean bullet);

    void numberSelectionParagraph(boolean number);

    /**
     * SWT.LEFT, SWT.CENTER, SWT.RIGHT
     */
    void setSelectionParagraphAlignment(int alignment);

    void setSelectionParagraphIndent(int insertIndent);

}