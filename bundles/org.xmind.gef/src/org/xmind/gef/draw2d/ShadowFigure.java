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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;
import org.xmind.gef.draw2d.graphics.ColorMaskGraphics;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.draw2d.graphics.GrayedGraphics;

public class ShadowFigure extends Figure {

    private class SourceHooker extends LayoutListener.Stub implements
            AncestorListener {

        public void invalidate(IFigure container) {
            super.invalidate(container);
            updateVisibility();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.draw2d.AncestorListener#ancestorAdded(org.eclipse.draw2d
         * .IFigure)
         */
        public void ancestorAdded(IFigure ancestor) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.draw2d.AncestorListener#ancestorMoved(org.eclipse.draw2d
         * .IFigure)
         */
        public void ancestorMoved(IFigure ancestor) {
            updateBounds();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.draw2d.AncestorListener#ancestorRemoved(org.eclipse.draw2d
         * .IFigure)
         */
        public void ancestorRemoved(IFigure ancestor) {
        }

    }

    private static int[] offsets = { 7, 0, 4, 2, 1, 1, 2, 4, //
            0, 7, -2, 4, -1, 1, -4, 2, //
            -7, 0, -4, -2, -1, -1, -2, -4, //
            0, -7, 2, -4, 1, -1, 4, -2 };
//private static int[] offsets = { 1, 1, 2, 0, 1, -1, 0, -2, -1, -2, -2, 0,
//-1, 1, 0, 2 };
//private static int[] offsets = {};

    private IFigure source = null;

    private int dx = 5;

    private int dy = 5;

    private int alpha = 0x80;

    private SourceHooker sourceHooker = new SourceHooker();

    public IFigure getSource() {
        return source;
    }

    public void setSource(IFigure source) {
        if (source == this.source)
            return;
        if (this.source != null) {
            this.source.removeLayoutListener(sourceHooker);
            this.source.removeAncestorListener(sourceHooker);
        }
        this.source = source;
        if (source != null) {
            source.addAncestorListener(sourceHooker);
            source.addLayoutListener(sourceHooker);
        }
        update();
        revalidate();
    }

    public int getOffsetX() {
        return dx;
    }

    public int getOffsetY() {
        return dy;
    }

    public void setOffsetX(int dx) {
        setOffset(dx, getOffsetY());
    }

    public void setOffsetY(int dy) {
        setOffset(getOffsetX(), dy);
    }

    public void setOffset(int dx, int dy) {
        boolean translate = (dx != this.dx) || (dy != this.dy);
        this.dx = dx;
        this.dy = dy;
        if (translate) {
            updateBounds();
        }
    }

    protected void update() {
        updateBounds();
        updateVisibility();
    }

    protected void updateBounds() {
        if (source != null) {
            setBounds(source.getBounds().getTranslated(getOffsetX(),
                    getOffsetY()).expand(10, 10));
        }
        repaint();
    }

    protected void updateVisibility() {
        if (source != null) {
            setVisible(source.isShowing());
        } else {
            setVisible(false);
        }
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        if (alpha == this.alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    public boolean isVisible() {
        return super.isVisible() && isSourceShowing();
    }

    private boolean isSourceShowing() {
        if (source == null)
            return false;
        if (source instanceof IShadowedFigure)
            return ((IShadowedFigure) source).isShadowShowing();
        return source.isShowing();
    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        int mainAlpha = getAlpha();
        if (offsets.length > 0) {
            int subAlpha = mainAlpha / (offsets.length / 2);
            graphics.pushState();
            graphics.translate(getOffsetX(), getOffsetY());
            for (int i = 0; i < offsets.length - 1; i += 2) {
                int offX = offsets[i];
                int offY = offsets[i + 1];
                graphics.translate(offX, offY);
                paintShadow(graphics, subAlpha);
                graphics.translate(-offX, -offY);
            }
            graphics.popState();
        } else {
            graphics.pushState();
            graphics.translate(getOffsetX(), getOffsetY());
            paintShadow(graphics, mainAlpha);
            graphics.popState();
        }
    }

    protected void paintShadow(Graphics graphics, int alpha) {
        AlphaGraphics g1 = new AlphaGraphics(graphics);
        g1.setMainAlpha(alpha);
        g1.setAlpha(graphics.getAlpha());
        if (source instanceof IShadowedFigure) {
            ((IShadowedFigure) source).paintShadow(g1);
        } else {
            ColorMaskGraphics g2 = createGlowGraphics(g1);
            paintSourceAsShadow(g2);
            g2.dispose();
        }
        g1.dispose();
    }

    protected void paintSourceAsShadow(Graphics g) {
        if (source != null) {
            source.paint(g);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.draw2d.GlowFigure#createGlowGraphics(org.eclipse.draw2d
     * .Graphics)
     */
    protected ColorMaskGraphics createGlowGraphics(Graphics graphics) {
        return new GrayedGraphics(graphics);
    }

}