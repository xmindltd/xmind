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

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.xmind.gef.draw2d.ITransparentableFigure;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class BaseLayer extends FreeformLayer implements ITransparentableFigure {

    private int alpha = 0xff;

    private int subAlpha = 0xff;

    public int getMainAlpha() {
        return alpha;
    }

    public int getSubAlpha() {
        return subAlpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == this.alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == this.subAlpha)
            return;
        this.subAlpha = alpha;
        repaint();
    }

    /**
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        if (usesMainAlphaGraphics()) {
            Graphics ag = createAlphaGraphics(graphics, getMainAlpha());
            simplePaint(ag);
            ag.dispose();
        } else {
            graphics.setAlpha(getMainAlpha());
            simplePaint(graphics);
        }
    }

    protected boolean usesMainAlphaGraphics() {
        return getMainAlpha() >= 0 && getMainAlpha() < 0xff;
    }

    protected void simplePaint(Graphics graphics) {
        super.paint(graphics);
    }

    protected void paintClientArea(Graphics graphics) {
        if (usesSubAlphaGraphics()) {
            Graphics ag = createAlphaGraphics(graphics, getSubAlpha());
            simplePaintClientArea(ag);
            ag.dispose();
        } else {
            graphics.setAlpha(getSubAlpha());
            simplePaintClientArea(graphics);
        }
    }

    protected void simplePaintClientArea(Graphics graphics) {
        super.paintClientArea(graphics);
    }

    protected boolean usesSubAlphaGraphics() {
        return getSubAlpha() >= 0 && getSubAlpha() < 0xff;
    }

    protected Graphics createAlphaGraphics(Graphics graphics, int alpha) {
        AlphaGraphics ag = new AlphaGraphics(graphics);
        ag.setMainAlpha(alpha);
        return ag;
    }

}