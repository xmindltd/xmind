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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

public class TitleLayer extends BaseLayer {

    private List<IFigure> onTop = new ArrayList<IFigure>();

    public void addOnTop(IFigure figure) {
        if (onTop.contains(figure))
            return;

        onTop.add(figure);
        repaint();
    }

    public void removeOnTop(IFigure figure) {
        if (!onTop.contains(figure))
            return;

        onTop.remove(figure);
        repaint();
    }

    protected void paintChildren(Graphics graphics) {
        IFigure child;

        Rectangle clip = Rectangle.SINGLETON;
        for (int i = 0; i < getChildren().size(); i++) {
            child = (IFigure) getChildren().get(i);
            if (!onTop.contains(child)) {
                if (child.isVisible()
                        && child.intersects(graphics.getClip(clip))) {
                    graphics.clipRect(child.getBounds());
                    child.paint(graphics);
                    graphics.restoreState();
                }
            }
        }
        paintOnTop(graphics);
    }

    private void paintOnTop(Graphics graphics) {
        Rectangle clip = Rectangle.SINGLETON;
        for (IFigure child : onTop) {
            if (child.isVisible() && child.intersects(graphics.getClip(clip))) {
                graphics.clipRect(child.getBounds());
                child.paint(graphics);
                graphics.restoreState();
            }
        }
    }

    public void remove(IFigure child) {
        super.remove(child);
        removeOnTop(child);
    }
}