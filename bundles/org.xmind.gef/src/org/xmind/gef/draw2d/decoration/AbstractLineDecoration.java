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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public abstract class AbstractLineDecoration extends AbstractDecoration
        implements ILineDecoration {

    private Color color = null;

    private int width = 1;

    private int lineStyle = SWT.LINE_SOLID;

    protected AbstractLineDecoration() {
        super();
    }

    protected AbstractLineDecoration(String id) {
        super(id);
    }

    /**
     * @see org.xmind.ui.mindmap.layers.decorations.IBranchConnectionDecoration#getLineColor()
     */
    public Color getLineColor() {
        return color;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    /**
     * @see org.xmind.ui.mindmap.layers.decorations.IBranchConnectionDecoration#getLineWidth()
     */
    public int getLineWidth() {
        return width;
    }

    /**
     * @see org.xmind.ui.mindmap.layers.decorations.IBranchConnectionDecoration#setLineColor(org.eclipse.swt.graphics.Color)
     */
    public void setLineColor(IFigure figure, Color color) {
        if (color == this.color || (color != null && color.equals(this.color)))
            return;
        this.color = color;
        if (figure != null) {
            repaint(figure);
        }
    }

    public void setLineStyle(IFigure figure, int style) {
        if (style == this.lineStyle)
            return;
        this.lineStyle = style;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @see org.xmind.ui.mindmap.layers.decorations.IBranchConnectionDecoration#setLineWidth(int)
     */
    public void setLineWidth(IFigure figure, int width) {
        if (width == this.width)
            return;
        this.width = width;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.AbstractDecoration#performPaint(org.eclipse.draw2d.IFigure,
     *      org.eclipse.draw2d.Graphics)
     */
    protected void performPaint(IFigure figure, Graphics g) {
        g.setAlpha(getAlpha());
        g.setForegroundColor(getLineColor(figure));
        g.setLineWidth(getLineWidth());
        g.setLineStyle(getLineStyle());
        decorateLine(figure, g);
        drawLine(figure, g);
    }

    protected void decorateLine(IFigure figure, Graphics g) {
    }

    /**
     * @param figure
     * @param g
     *            draw a line from the source position to the target position
     */
    protected abstract void drawLine(IFigure figure, Graphics g);

    protected Color getLineColor(IFigure figure) {
        Color c = getLineColor();
        return c == null ? figure.getForegroundColor() : c;
    }

}