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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class SimpleLineFigure extends Figure {

    public Point source;

    public Point target;

    private int alpha = 0xff;

    private int lineWidth = 1;

    public SimpleLineFigure() {
        this.source = new Point(0, 0);
        this.target = new Point(0, 0);
        pack();
    }

    /**
     * 
     */
    public SimpleLineFigure(Point source, Point target) {
        this.source = source.getCopy();
        this.target = target.getCopy();
        pack();
    }

    public void pack() {
        setBounds(Rectangle.SINGLETON.setLocation(source).setSize(0, 0).union(
                target.x, target.y).expand(getLineWidth(), getLineWidth()));
        repaint();
    }

    public void setLocations(Point source, Point target) {
        this.source.setLocation(source);
        this.target.setLocation(target);
        pack();
    }

    /**
     * @return the alpha
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * @param alpha
     *            the alpha to set
     */
    public void setAlpha(int alpha) {
        if (alpha == this.alpha)
            return;

        this.alpha = alpha;
        repaint();
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
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        Color c = getLocalForegroundColor();
        if (c != null && alpha > 0) {
            graphics.setAntialias(SWT.ON);
            graphics.setAlpha(alpha);
            graphics.setForegroundColor(c);
            graphics.setLineStyle(SWT.LINE_SOLID);
            graphics.setLineWidth(getLineWidth());
            graphics.drawLine(source, target);
        }
    }

}