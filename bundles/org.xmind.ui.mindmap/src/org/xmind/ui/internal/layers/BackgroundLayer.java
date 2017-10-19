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
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Pattern;

public class BackgroundLayer extends BaseLayer {

    private Pattern wallpaper = null;

    public BackgroundLayer() {
        setOpaque(true);
    }

    public void setWallpaper(Pattern wallpaper) {
        if (wallpaper == this.wallpaper)
            return;
        this.wallpaper = wallpaper;
        repaint();
    }

    public Pattern getWallpaper() {
        return wallpaper;
    }

    protected void paintFigure(Graphics graphics) {
        if (isOpaque() && wallpaper != null && !wallpaper.isDisposed()) {
            graphics.setAntialias(SWT.ON);
            graphics.setBackgroundPattern(wallpaper);
            graphics.fillRectangle(getBounds());
            graphics.restoreState();
            graphics.setAlpha(0xff - getSubAlpha());
        }

        super.paintFigure(graphics);
    }

}