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
package org.xmind.gef.draw2d.graphics;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.graphics.RGB;

public class GrayedGraphics extends ColorMaskGraphics {

    public GrayedGraphics(Graphics delegate) {
        super(delegate);
    }

    protected RGB getMaskColor(RGB rgb) {
        int l = lightness(rgb.red, rgb.green, rgb.blue);
        l += (0xff - l) / 2;
        return new RGB(l, l, l);
    }

    private static int lightness(int r, int g, int b) {
        return (int) (r * 0.3 + g * 0.59 + b * 0.11);
    }

}