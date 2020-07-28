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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.graphics.GradientPattern;

public abstract class AbstractShapeDecoration extends AbstractDecoration
        implements IShapeDecoration {

    protected final static Rectangle NO_OWNER_BOX = new Rectangle();

    private Color fillColor = null;

    private int fillAlpha = 0xFF;

    private int lineAlpha = 0xFF;

    private Color lineColor = null;

    private int lineWidth = 1;

    private int lineStyle = SWT.LINE_SOLID;

    private boolean gradient = false;

    protected AbstractShapeDecoration() {
        super();
    }

    protected AbstractShapeDecoration(String id) {
        super(id);
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#isGradient()
     */
    public boolean isGradient() {
        return gradient;
    }

    /**
     * @return the fillColor
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#getLineAlpha()
     */
    public int getLineAlpha() {
        return lineAlpha;
    }

    /**
     * @return the outlineColor
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#getLineStyle()
     */
    public int getLineStyle() {
        return lineStyle;
    }

    /**
     * @return the lineWidth
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#getFillAlpha()
     */
    public int getFillAlpha() {
        return fillAlpha;
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#setFillAlpha(int)
     */
    public void setFillAlpha(IFigure figure, int alpha) {
        if (alpha == this.fillAlpha)
            return;
        this.fillAlpha = alpha;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @param c
     *            the fillColor to set
     */
    public void setFillColor(IFigure figure, Color c) {
        if (c == this.fillColor || (c != null && c.equals(this.fillColor)))
            return;
        this.fillColor = c;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#setGradient(boolean)
     */
    public void setGradient(IFigure figure, boolean gradient) {
        gradient = gradient && GEF.IS_PLATFORM_SUPPORT_GRADIENT;
        if (gradient == this.gradient)
            return;
        this.gradient = gradient;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#setLineAlpha(int)
     */
    public void setLineAlpha(IFigure figure, int alpha) {
        if (alpha == this.lineAlpha)
            return;
        this.lineAlpha = alpha;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @param c
     *            the outlineColor to set
     */
    public void setLineColor(IFigure figure, Color c) {
        if (c == this.lineColor || (c != null && c.equals(this.lineColor)))
            return;
        this.lineColor = c;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @see org.xmind.gef.draw2d.decoration.IShapeDecoration#setLineStyle(int)
     */
    public void setLineStyle(IFigure figure, int lineStyle) {
        if (lineStyle == this.lineStyle)
            return;
        this.lineStyle = lineStyle;
        if (figure != null) {
            repaint(figure);
        }
    }

    /**
     * @param lineWidth
     *            the lineWidth to set
     */
    public void setLineWidth(IFigure figure, int lineWidth) {
        if (lineWidth == this.lineWidth)
            return;
        this.lineWidth = lineWidth;
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
        invalidate();
    }

    protected void performPaint(IFigure figure, Graphics g) {
        if (isFillVisible(figure)) {
            fill(figure, g);
            g.restoreState();
        }

        if (isLineVisible(figure)) {
            outline(figure, g);
            g.restoreState();
        }
    }

    protected void outline(IFigure figure, Graphics g) {
        g.setAlpha(getAppliedLineAlpha());
        g.setForegroundColor(getLineColor(figure));
        g.setLineWidth(getLineWidth());
        g.setLineStyle(getLineStyle());
        paintOutline(figure, g);
    }

    protected void fill(IFigure figure, Graphics g) {
        Pattern bgPattern = null;
        int appliedAlpha = getAppliedFillAlpha();
        Color bgColor = getFillColor(figure);
        if (isGradient()) {
            bgPattern = createGradientPattern(figure, appliedAlpha, bgColor);
            g.setBackgroundPattern(bgPattern);
        } else {
            g.setAlpha(appliedAlpha);
            g.setBackgroundColor(bgColor);
        }
        paintFill(figure, g);
        if (bgPattern != null) {
            g.setBackgroundPattern(null);
            bgPattern.dispose();
        }
    }

    protected int getAppliedLineAlpha() {
        return (int) Math.floor(getLineAlpha() * 1.0 * getAlpha() / 0xFF);
    }

    protected int getAppliedFillAlpha() {
        return (int) Math.floor(getFillAlpha() * 1.0 * getAlpha() / 0xFF);
    }

    protected Color getFillColor(IFigure figure) {
        Color c = getFillColor();
        return c == null ? figure.getBackgroundColor() : c;
    }

    protected Color getLineColor(IFigure figure) {
        Color c = getLineColor();
        return c == null ? figure.getForegroundColor() : c;
    }

    protected Pattern createGradientPattern(IFigure figure, int alpha,
            Color bgColor) {
        Rectangle r = getFillBox(figure);
        int delta = (int) (r.height * 0.4);
        Pattern p = new GradientPattern(Display.getCurrent(), //
                r.x, r.y - delta, //
                r.x, r.y + r.height, //
                ColorConstants.white, alpha, //
                bgColor, alpha);
        return p;
    }

    protected abstract void paintFill(IFigure figure, Graphics graphics);

    protected abstract void paintOutline(IFigure figure, Graphics graphics);

    protected Rectangle getFillBox(IFigure figure) {
        return getFillBox(figure.getBounds());
    }

    protected Rectangle getFillBox(Rectangle box) {
        Rectangle ret = box.getCopy();
        int w = Math.min(ret.width - 1, Math
                .min(ret.height - 1, getLineWidth()));
        int half = w - w / 2;
        return ret.shrink(half, half);
    }

    protected Rectangle getOutlineBox(IFigure figure) {
        return getOutlineBox(figure.getBounds());
    }

    /**
     * @return
     */
    protected Rectangle getOutlineBox(Rectangle box) {
        Rectangle ret = box.getCopy();
        int w = Math.min(ret.width - 1, Math
                .min(ret.height - 1, getLineWidth()));
        int half = w - w / 2;
        return ret.shrink(half, half).resize(-1, -1);
    }

    /**
     * @param figure
     * @return the fillVisible
     */
    protected boolean isFillVisible(IFigure figure) {
        return getFillColor() != null;
    }

    /**
     * @param figure
     * @return the outlineVisible
     */
    protected boolean isLineVisible(IFigure figure) {
        return getLineColor() != null;
    }

}