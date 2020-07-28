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
package org.xmind.ui.gallery;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class ShadowBorder extends AbstractBorder {

    private int hDepth;
    private int vDepth;
    private int borderWidth;
    private Color shadowColor = ColorConstants.black;
    private Color borderColor = ColorConstants.black;
    private int borderAlpha = 0x20;
    private int shadowAlpha = 0x80;
    private boolean hideShadow = false;

    /**
     * Constructs a new ShadowBorder with parameters below:<br>
     * <br>
     * Shadow Horizontal Depth = 3<br>
     * Shadow Vertical Depth = 3<br>
     * Shadow Alpha = 0x80(128)<br>
     * Border Width = 1<br>
     * Border Color = black(r=0,g=0,b=0)<br>
     */
    public ShadowBorder() {
        this(1, 3, 3);
    }

    /**
     * Constructs a new ShadowBorder with specified parameters.<br>
     * 
     * @param borderWidth
     * @param horizontalDepth
     * @param verticalDepth
     */
    public ShadowBorder(int borderWidth, int horizontalDepth,
            int verticalDepth) {
        this.borderWidth = borderWidth;
        this.hDepth = horizontalDepth;
        this.vDepth = verticalDepth;
    }

    /**
     * @return the depth
     */
    public int getHorizontalShadowDepth() {
        return hDepth;
    }

    /**
     * @return the lineWidth
     */
    public int getBorderWidth() {
        return borderWidth;
    }

    public void setShadowDepths(int depth) {
        setHorizontalShadowDepth(depth);
        setVerticalShadowDepth(depth);
    }

    public void setShadowDepths(Dimension depths) {
        setHorizontalShadowDepth(depths.width);
        setVerticalShadowDepth(depths.height);
    }

    public Dimension getShadowDepths() {
        return new Dimension(getHorizontalShadowDepth(),
                getVerticalShadowDepth());
    }

    /**
     * @return the vDepth
     */
    public int getVerticalShadowDepth() {
        return vDepth;
    }

    /**
     * @param depth
     *            the depth to set
     */
    public void setHorizontalShadowDepth(int depth) {
        this.hDepth = depth;
    }

    /**
     * @param lineWidth
     *            the lineWidth to set
     */
    public void setBorderWidth(int lineWidth) {
        this.borderWidth = lineWidth;
    }

    /**
     * @param depth
     *            the vDepth to set
     */
    public void setVerticalShadowDepth(int depth) {
        this.vDepth = depth;
    }

    /**
     * @return the backColor
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * @param backColor
     *            the backColor to set
     */
    public void setShadowColor(Color backColor) {
        this.shadowColor = backColor;
    }

    /**
     * @return the borderColor
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * @return the shadowAlpha
     */
    public int getShadowAlpha() {
        return shadowAlpha;
    }

    public int getBorderAlpha() {
        return borderAlpha;
    }

    /**
     * @param borderColor
     *            the borderColor to set
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @param shadowAlpha
     *            the shadowAlpha to set
     */
    public void setShadowAlpha(int shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

    public void setBorderAlpha(int borderAlpha) {
        this.borderAlpha = borderAlpha;
    }

    public void reverseShadow() {
        hDepth = -hDepth;
        vDepth = -vDepth;
    }

    public boolean isShadowVisible() {
        return !hideShadow;
    }

    public void setShadowVisible(boolean visible) {
        this.hideShadow = !visible;
    }

    public void hideShadow() {
        hideShadow = true;
    }

    public void showShadow() {
        hideShadow = false;
    }

    /**
     * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
     */
    public Insets getInsets(IFigure figure) {
        Insets ins = new Insets();
        int hd = getHorizontalShadowDepth();
        if (hd > 0) {
            ins.right += hd;
        } else {
            ins.left -= hd;
        }
        int vd = getVerticalShadowDepth();
        if (vd > 0) {
            ins.bottom += vd;
        } else {
            ins.top -= vd;
        }
        return ins;
    }

    /**
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
     *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        Rectangle r = getPaintRectangle(figure, insets);
        Rectangle c = r.shrink(getInsets(figure));
        int left = c.x;
        int right = c.right();
        int top = c.y;
        int bottom = c.bottom();
        int hd = getHorizontalShadowDepth();
        int vd = getVerticalShadowDepth();
        int bw = getBorderWidth();
        int height = c.height;
        int width = c.width;
        if (isShadowVisible() && (hd != 0 || vd != 0)) {
            Rectangle r1 = new Rectangle(left + hd, 0, width - Math.abs(hd),
                    Math.min(height, Math.abs(vd)));
            Rectangle r2 = new Rectangle(0, top + vd,
                    Math.min(width, Math.abs(hd)), height);
            if (hd < 0)
                r1.x -= hd;
            r1.y = vd > 0 ? bottom : top + vd;
            r2.x = hd > 0 ? right : left + hd;
            if (figure.isEnabled())
                graphics.setBackgroundColor(getShadowColor());
            else
                graphics.setBackgroundColor(ColorConstants.buttonLightest);
            graphics.setAlpha(getShadowAlpha());
            if (!r1.isEmpty())
                graphics.fillRectangle(r1);
            graphics.fillRectangle(r2);
        }
        if (bw > 0) {
            if (figure.isEnabled())
                graphics.setForegroundColor(getBorderColor());
            else
                graphics.setForegroundColor(ColorConstants.buttonDarker);
            graphics.setAlpha(getBorderAlpha());
            graphics.setLineStyle(SWT.LINE_SOLID);
            graphics.setLineWidth(bw);
            graphics.drawRectangle(left + bw / 2, top + bw / 2, width - bw,
                    height - bw);
        }
    }

}