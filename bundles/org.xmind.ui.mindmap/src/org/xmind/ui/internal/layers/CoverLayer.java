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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.IRelayerableFigure;
import org.xmind.gef.draw2d.IRelayeredPane;

public class CoverLayer extends BaseLayer
        implements IRelayeredPane, FigureListener {

    private List<IFigure> backgroundLayers = null;

    private List<IRelayerableFigure> highlights = null;

    public CoverLayer() {
        setMainAlpha(0x80);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.layers.IRelayeredPane#addRelayerableFigure(org.
     * xmind.ui.internal.figures.IRelayerableFigure)
     */
    public void addRelayerableFigure(IRelayerableFigure figure) {
        if (figure == null)
            return;
        if (highlights == null)
            highlights = new ArrayList<IRelayerableFigure>();
        highlights.add(figure);
        figure.setRelayered(true);
        figure.addFigureListener(this);
        repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.layers.IRelayeredPane#removeRelayerableFigure(org
     * .xmind.ui.internal.figures.IRelayerableFigure)
     */
    public void removeRelayerableFigure(IRelayerableFigure figure) {
        if (figure == null)
            return;
        if (highlights == null)
            return;
        highlights.remove(figure);
        figure.setRelayered(false);
        figure.removeFigureListener(this);
        repaint();
    }

    public List<IRelayerableFigure> getRelayerableFigures() {
        return highlights;
    }

    public void addBackgroundLayer(IFigure layer) {
        if (layer == null)
            return;
        if (backgroundLayers == null)
            backgroundLayers = new ArrayList<IFigure>();
        backgroundLayers.add(layer);
        repaint();
    }

    public void removeBackgroundLayer(IFigure layer) {
        if (layer == null)
            return;
        if (backgroundLayers != null) {
            backgroundLayers.remove(layer);
            repaint();
        }
    }

    public void paint(Graphics graphics) {
        simplePaint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        if (highlights != null && !highlights.isEmpty()) {
            paintCover(graphics);
        }
    }

    protected void paintCover(Graphics graphics) {
        graphics.setAlpha(getMainAlpha());
        graphics.setBackgroundColor(getCoverColor());
        graphics.fillRectangle(getBounds());
    }

    protected Color getCoverColor() {
        Color color = getLocalBackgroundColor();
        if (color == null)
            color = ColorConstants.black;
        return color;
    }

    protected void paintClientArea(Graphics graphics) {
        if (usesSubAlphaGraphics()) {
            Graphics ag = createAlphaGraphics(graphics, getSubAlpha());
            paintChildren(ag);
            ag.dispose();
        } else {
            graphics.setAlpha(getSubAlpha());
            paintChildren(graphics);
        }
    }

    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);

        if (highlights != null) {
            Rectangle clip = Rectangle.SINGLETON;
            for (int i = 0; i < highlights.size(); i++) {
                IRelayerableFigure child = highlights.get(i);
                if (child.isVisible()
                        && child.intersects(graphics.getClip(clip))) {
                    paintHighlight(graphics, child);
                }
            }
        }
    }

    private void paintHighlight(Graphics graphics, IRelayerableFigure figure) {
        paintBackgroundLayers(graphics, figure);
        graphics.clipRect(figure.getBounds());
        figure.paintRelayered(graphics);
        graphics.restoreState();
    }

    private void paintBackgroundLayers(Graphics graphics,
            IRelayerableFigure figure) {
        if (backgroundLayers == null || backgroundLayers.isEmpty())
            return;

        for (IFigure layer : backgroundLayers) {
            graphics.clipRect(figure.getBounds());
            layer.paint(graphics);
            graphics.restoreState();
        }
    }

    public void figureMoved(IFigure source) {
        repaint();
    }

}