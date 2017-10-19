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

/**
 * @author Frank Shaka
 * 
 */
public class SimpleColorMaskGraphics extends ColorMaskGraphics {

    private RGB color;

    /**
     * @param delegate
     */
    public SimpleColorMaskGraphics(Graphics delegate, RGB color) {
        super(delegate, color);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.graphics.ColorMaskGraphics#init(java.lang.Object[])
     */
    @Override
    protected void init(Object... args) {
        this.color = (RGB) args[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.graphics.ColorMaskGraphics#getMaskColor(org.eclipse
     * .swt.graphics.RGB)
     */
    @Override
    protected RGB getMaskColor(RGB rgb) {
        return color;
    }

}
