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
package org.xmind.ui.internal.figures;

import org.eclipse.draw2d.Graphics;
import org.xmind.gef.draw2d.DecoratedShapeFigure;
import org.xmind.gef.draw2d.ITransparentableFigure;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;

public class InformationFigure extends DecoratedShapeFigure
        implements ITransparentableFigure {

    private int mainAlpha = 0xff;

    private int subAlpha = 0xff;

    private Object constraint;

    public int getMainAlpha() {
        return mainAlpha;
    }

    public int getSubAlpha() {
        return subAlpha;
    }

    public void setMainAlpha(int alpha) {
        if (alpha == this.mainAlpha)
            return;
        this.mainAlpha = alpha;
        repaint();
    }

    public void setSubAlpha(int alpha) {
        if (alpha == this.subAlpha)
            return;
        this.subAlpha = alpha;
        repaint();
    }

    public void paint(Graphics graphics) {
        if (getMainAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getMainAlpha());
            ag.setAlpha(graphics.getAlpha());
            try {
                doPaint(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaint(graphics);
        }
    }

    private void doPaint(Graphics graphics) {
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        if (getSubAlpha() < 0xff) {
            AlphaGraphics ag = new AlphaGraphics(graphics);
            ag.setMainAlpha(getSubAlpha());
            try {
                doPaintFigure(ag);
            } finally {
                ag.dispose();
            }
        } else {
            doPaintFigure(graphics);
        }
    }

    private void doPaintFigure(Graphics graphics) {
        super.paintFigure(graphics);
    }

    public void setConstraint(Object constraint) {
        if (this.constraint != constraint) {
            this.constraint = constraint;
            revalidate();
        }
    }

    public Object getConstraint() {
        return this.constraint;
    }

}
