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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * @author Frank Shaka
 */
public class SimpleRectangleFigure extends Figure implements
        ITransparentableFigure {

    public Rectangle client = new Rectangle(0, 0, 0, 0);

    private int borderAlpha = 0xff;

    private int fillAlpha = 0;

    private int lineWidth = 1;

    public SimpleRectangleFigure() {
    }

    public SimpleRectangleFigure(Rectangle client) {
        this.client.setBounds(client);
        pack();
    }

    public void pack() {
        setBounds(Rectangle.SINGLETON.setBounds(client).expand(getLineWidth(),
                getLineWidth()).resize(1, 1));
        revalidate();
        repaint();
    }

    public void setClient(int x, int y, int width, int height) {
        setClient(Rectangle.SINGLETON.setLocation(x, y).setSize(width, height));
    }

    public void setClient(Rectangle client) {
        if (client == null || client.equals(this.client))
            return;

        this.client.setBounds(client);
        pack();
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        if (lineWidth == this.lineWidth)
            return;
        this.lineWidth = lineWidth;
        pack();
        repaint();
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#getMainAlpha()
     */
    public int getMainAlpha() {
        return borderAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#getSubAlpha()
     */
    public int getSubAlpha() {
        return fillAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#setMainAlpha(int)
     */
    public void setMainAlpha(int alpha) {
        if (alpha == this.borderAlpha)
            return;
        this.borderAlpha = alpha;
        repaint();
    }

    /**
     * @see org.xmind.gef.draw2d.IUseTransparency#setSubAlpha(int)
     */
    public void setSubAlpha(int alpha) {
        if (alpha == this.fillAlpha)
            return;
        this.fillAlpha = alpha;
        repaint();
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        graphics.setLineStyle(SWT.LINE_SOLID);
        int w = getLineWidth();
        int w2 = w / 2;
        graphics.setLineWidth(w);
        Rectangle r = Rectangle.SINGLETON.setBounds(getBounds());
        Color fillColor = getLocalBackgroundColor();
        if (fillColor != null && getSubAlpha() > 0) {
            graphics.setAlpha(getSubAlpha());
            graphics.setBackgroundColor(fillColor);
            graphics.fillRectangle(r.shrink(w2, w2));
        }
        Color borderColor = getLocalForegroundColor();
        if (borderColor != null && getMainAlpha() > 0) {
            graphics.setAlpha(getMainAlpha());
            graphics.setForegroundColor(borderColor);
            graphics.drawRectangle(r.resize(-1, -1));
        }
    }

}