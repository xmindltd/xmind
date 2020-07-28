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
package org.xmind.ui.color;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Frank Shaka
 */
public class ColorSelection implements IColorSelection {

    public static final ColorSelection EMPTY = new ColorSelection(0);

    private int type;
    private RGB color;

    public ColorSelection(RGB color) {
        this(CUSTOM, color);
    }

    public ColorSelection(int type) {
        this(type, null);
    }

    public ColorSelection(int type, RGB color) {
        this.type = type;
        this.color = color;
    }

    public int getType() {
        return type;
    }

    public RGB getColor() {
        return color;
    }

    public boolean isAutomatic() {
        return type == AUTO;
    }

    public boolean isNone() {
        return type == NONE;
    }

    public boolean isCustom() {
        return type == CUSTOM;
    }

    /**
     * @see org.eclipse.jface.viewers.ISelection#isEmpty()
     */
    public boolean isEmpty() {
        return type != AUTO && type != NONE && type != CUSTOM;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof ColorSelection))
            return false;
        ColorSelection that = (ColorSelection) obj;
        return this.type == that.type
                && (this.color == that.color || (this.color != null && this.color
                        .equals(that.color)));
    }

    public int hashCode() {
        if (color == null)
            return type;
        return type ^ color.hashCode();
    }

    public String toString() {
        String t;
        if (isAutomatic())
            t = "Automatic"; //$NON-NLS-1$
        else if (isNone())
            t = "None"; //$NON-NLS-1$
        else if (isCustom())
            t = "Custom"; //$NON-NLS-1$
        else
            t = "Unknown"; //$NON-NLS-1$
        return "[type=" + t + (color != null ? ", color=" + color : "") + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}