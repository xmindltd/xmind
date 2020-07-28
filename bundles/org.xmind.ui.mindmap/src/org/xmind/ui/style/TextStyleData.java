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
package org.xmind.ui.style;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.xmind.gef.GEF;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class TextStyleData {

    public String name;

    public int height;

    public boolean bold;

    public boolean italic;

    public RGB color;

    public boolean underline;

    public boolean strikeout;

    public int align;

    public int textCase;

    public TextStyleData() {
        this(JFaceResources.getDefaultFont().getFontData()[0]);
    }

    public TextStyleData(FontData fd) {
        this.name = fd.getName();
        this.height = fd.getHeight();
        this.bold = (fd.getStyle() & SWT.BOLD) != 0;
        this.italic = (fd.getStyle() & SWT.ITALIC) != 0;
        this.color = new RGB(0, 0, 0);
        this.underline = false;
        this.strikeout = false;

        this.align = PositionConstants.LEFT;
        this.textCase = GEF.MANUAL;
    }

    public TextStyleData(TextStyleData data) {
        this.name = data.name;
        this.height = data.height;
        this.bold = data.bold;
        this.italic = data.italic;
        this.color = data.color;
        this.underline = data.underline;
        this.strikeout = data.strikeout;

        this.align = data.align;
        this.textCase = data.textCase;
    }

    public FontData createFontData() {
        int style = SWT.NORMAL;
        if (bold)
            style |= SWT.BOLD;
        if (italic)
            style |= SWT.ITALIC;
        return new FontData(name, height, style);
    }

    public Font createFont() {
        return FontUtils.getFont(createFontData());
    }

    public TextStyle createTextStyle() {
        TextStyle textStyle = new TextStyle(createFont(),
                ColorUtils.getColor(color), null);
        textStyle.underline = underline;
        textStyle.strikeout = strikeout;

        return textStyle;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TextStyleData))
            return false;
        TextStyleData that = (TextStyleData) obj;
        return (this.name == that.name || (this.name != null && this.name
                .equals(that.name)))
                && this.height == that.height
                && this.bold == that.bold
                && this.italic == that.italic
                && (this.color == that.color || (this.color != null && this.color
                        .equals(that.color)))
                && this.underline == that.underline
                && this.strikeout == that.strikeout

                && this.align == that.align && this.textCase == that.textCase;
    }

    public int hashCode() {
        int c = height;
        if (bold)
            c ^= 1;
        if (italic)
            c ^= 1 << 1;
        if (underline)
            c ^= 1 << 2;
        if (strikeout)
            c ^= 1 << 3;
        if (name != null)
            c ^= name.hashCode();
        if (color != null)
            c ^= color.hashCode();
        c ^= align;
        c ^= textCase;
        return c;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(30);
        sb.append("{name="); //$NON-NLS-1$
        sb.append(name);
        sb.append(",height="); //$NON-NLS-1$
        sb.append(height);
        sb.append(",color="); //$NON-NLS-1$
        sb.append(color);
        sb.append(","); //$NON-NLS-1$
        if (bold)
            sb.append("bold"); //$NON-NLS-1$
        if (italic)
            sb.append("italic"); //$NON-NLS-1$
        if (underline)
            sb.append("underline"); //$NON-NLS-1$
        if (strikeout)
            sb.append("strikeout"); //$NON-NLS-1$
        sb.append(",align="); //$NON-NLS-1$
        sb.append(align);
        sb.append(textCase);
        sb.append("}"); //$NON-NLS-1$
        return sb.toString();
    }
}