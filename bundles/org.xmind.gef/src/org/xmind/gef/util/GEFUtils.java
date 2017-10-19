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
package org.xmind.gef.util;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

public class GEFUtils {

    private GEFUtils() {
    }

    /**
     * @param ort
     */
    public static Cursor getPositionCursor(int ort) {
        switch (ort) {
        case PositionConstants.EAST:
            return Cursors.SIZEE;
        case PositionConstants.WEST:
            return Cursors.SIZEW;
        case PositionConstants.NORTH:
            return Cursors.SIZEN;
        case PositionConstants.SOUTH:
            return Cursors.SIZES;
        case PositionConstants.NORTH_EAST:
            return Cursors.SIZENE;
        case PositionConstants.SOUTH_WEST:
            return Cursors.SIZESW;
        case PositionConstants.NORTH_WEST:
            return Cursors.SIZENW;
        case PositionConstants.SOUTH_EAST:
            return Cursors.SIZESE;
        }
        return null;
    }

    public static boolean equals(TextStyle style1, TextStyle style2) {
        if (style1 == style2)
            return true;
        if (style1 == null && style2 != null)
            return false;
        if (style1 != null && style2 == null)
            return false;

        if (style1.foreground != null) {
            if (!style1.foreground.equals(style2.foreground))
                return false;
        } else if (style2.foreground != null)
            return false;
        if (style1.background != null) {
            if (!style1.background.equals(style2.background))
                return false;
        } else if (style2.background != null)
            return false;
        if (style1.font != null) {
            if (!equals(style1.font, style2.font))
                return false;
        } else if (style2.font != null)
            return false;
        if (style1.metrics != null || style2.metrics != null)
            return false;
        if (style1.underline != style2.underline)
            return false;
        if (style1.strikeout != style2.strikeout)
            return false;
        if (style1.rise != style2.rise)
            return false;
        return true;
    }

    public static boolean equals(Font f1, Font f2) {
        if (f1 == f2)
            return true;
        if (f1 == null && f2 != null)
            return false;
        if (f2 == null && f1 != null)
            return false;

        if (!Util.isMac())
            return f1.equals(f2);

        if (f1.isDisposed() || f2.isDisposed())
            return false;

        FontData fd1 = f1.getFontData()[0];
        FontData fd2 = f2.getFontData()[0];
        return fd1.equals(fd2);
    }

}