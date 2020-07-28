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
package org.xmind.ui.internal.layers;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.ISkylightLayer;

public class SkylightLayer extends BaseLayer implements ISkylightLayer {

    private Rectangle skylight = null;

    public SkylightLayer() {
        setMainAlpha(0x80);
    }

    /* (non-Javadoc)
     * @see org.xmind.ui.internal.layers.ISkylightLayer#setSkylight(org.eclipse.draw2d.geometry.Rectangle)
     */
    public void setSkylight(Rectangle skylight) {
        if (skylight == null && this.skylight == null)
            return;
        if (skylight != null && skylight.equals(this.skylight))
            return;

        if (skylight == null) {
            this.skylight = null;
        } else {
            if (this.skylight == null)
                this.skylight = new Rectangle(skylight);
            else
                this.skylight.setBounds(skylight);
        }
        repaint();
    }

    /* (non-Javadoc)
     * @see org.xmind.ui.internal.layers.ISkylightLayer#getSkylight()
     */
    public Rectangle getSkylight() {
        return skylight;
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        if (skylight != null) {
            paintSkylight(graphics);
        }
    }

    public void paint(Graphics graphics) {
        simplePaint(graphics);
    }

    protected void paintSkylight(Graphics graphics) {
        graphics.setBackgroundColor(getCoverColor());
        graphics.setAlpha(getMainAlpha());
        Rectangle r = getBounds();
        int top = skylight.y - r.y;
        if (top > 0) {
            graphics.fillRectangle(r.x, r.y, r.width, top);
        }
        int left = skylight.x - r.x;
        if (left > 0) {
            graphics.fillRectangle(r.x, skylight.y, left, skylight.height);
        }
        int right = r.width - skylight.width - left;
        if (right > 0) {
            graphics.fillRectangle(skylight.right(), skylight.y, right,
                    skylight.height);
        }
        int bottom = r.height - skylight.height - top;
        if (bottom > 0) {
            graphics.fillRectangle(r.x, skylight.bottom(), r.width, bottom);
        }
    }

    protected Color getCoverColor() {
        Color color = getLocalBackgroundColor();
        if (color == null)
            color = ColorConstants.black;
        return color;
    }

}