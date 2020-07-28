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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;

public class ShadowedLayer extends Layer {

    private double ratio = 0;

    private ShadowBorder shadowBorder;

    private boolean pressed = false;

    /**
     * 
     */
    public ShadowedLayer() {
        setLayoutManager(new AdvancedToolbarLayout(false));
        shadowBorder = new ShadowBorder();
        setBorder(shadowBorder);
        setForegroundColor(ColorConstants.black);
        setBackgroundColor(ColorConstants.white);
        setOpaque(true);
    }

    public void togglePressed() {
        if (!isEnabled())
            return;
        shadowBorder.reverseShadow();
        shadowBorder.setShadowVisible(!shadowBorder.isShadowVisible());
        pressed = !pressed;
        revalidate();
        repaint();
    }

    public void press() {
        if (!isEnabled() || isPressed())
            return;
        shadowBorder.setHorizontalShadowDepth(
                -Math.abs(shadowBorder.getHorizontalShadowDepth()));
        shadowBorder.setVerticalShadowDepth(
                -Math.abs(shadowBorder.getVerticalShadowDepth()));
        shadowBorder.hideShadow();
        pressed = true;
        revalidate();
        repaint();
    }

    public void unpress() {
        if (!isEnabled() || !isPressed())
            return;
        shadowBorder.setHorizontalShadowDepth(
                Math.abs(shadowBorder.getHorizontalShadowDepth()));
        shadowBorder.setVerticalShadowDepth(
                Math.abs(shadowBorder.getVerticalShadowDepth()));
        shadowBorder.showShadow();
        pressed = false;
        revalidate();
        repaint();
    }

    /**
     * @return the pressed
     */
    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        if (pressed)
            press();
        else
            unpress();
    }

    public ShadowBorder getShadowBorder() {
        return shadowBorder;
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        // do nothing
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintClientArea(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintClientArea(Graphics graphics) {
        if (isOpaque()) {
            graphics.fillRectangle(getClientArea());
        }
        super.paintClientArea(graphics);
    }

    /**
     * @return the ratio
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * Set the ratio of the preferred width to the preferred height ( width /
     * height ). A positive value means that, when the preferred size of the
     * figure is requested, the height of the preferred size will be made to fit
     * the ratio while the width remains. A negative value means the opposite (
     * the width will be made to fit the ratio while the height remains ). Zero
     * value means that no ratio will be kept when the preferred size is
     * requested.
     * 
     * @param ratio
     *            the ratio to set.
     */
    public void setRatio(double ratio) {
        if (ratio == this.ratio)
            return;
        this.ratio = ratio;
        revalidate();
    }

    /**
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        Dimension size = super.getPreferredSize(wHint, hHint);
        if (ratio > 0) {
            int width = wHint < 0 ? size.width : wHint;
            int height = (int) Math.ceil(width / ratio);
            size = new Dimension(width, height);
        } else if (ratio < 0) {
            int height = hHint < 0 ? size.height : hHint;
            int width = (int) Math.ceil(height * (-ratio));
            size = new Dimension(width, height);
        }
        return size;
    }

    public Color getBorderColor() {
        return shadowBorder.getBorderColor();
    }

    public int getBorderWidth() {
        return shadowBorder.getBorderWidth();
    }

    public Dimension getShadowDepths() {
        return shadowBorder.getShadowDepths();
    }

    public int getHorizontalShadowDepth() {
        return shadowBorder.getHorizontalShadowDepth();
    }

    public int getShadowAlpha() {
        return shadowBorder.getShadowAlpha();
    }

    public int getBorderAlpha() {
        return shadowBorder.getBorderAlpha();
    }

    public Color getShadowColor() {
        return shadowBorder.getShadowColor();
    }

    public int getVerticalShadowDepth() {
        return shadowBorder.getVerticalShadowDepth();
    }

    public void hideShadow() {
        shadowBorder.hideShadow();
        revalidate();
        repaint();
    }

    public boolean isShadowVisible() {
        return shadowBorder.isShadowVisible();
    }

    public void setBorderColor(Color borderColor) {
        shadowBorder.setBorderColor(borderColor);
        repaint();
    }

    public void setBorderWidth(int lineWidth) {
        shadowBorder.setBorderWidth(lineWidth);
        revalidate();
        repaint();
    }

    public void setShadowDepths(int depth) {
        shadowBorder.setShadowDepths(depth);
        revalidate();
        repaint();
    }

    public void setShadowDepths(Dimension depths) {
        shadowBorder.setShadowDepths(depths);
        revalidate();
        repaint();
    }

    public void setHorizontalShadowDepth(int depth) {
        shadowBorder.setHorizontalShadowDepth(depth);
        revalidate();
        repaint();
    }

    public void setShadowAlpha(int shadowAlpha) {
        shadowBorder.setShadowAlpha(shadowAlpha);
        repaint();
    }

    public void setBorderAlpha(int borderAlpha) {
        shadowBorder.setBorderAlpha(borderAlpha);
        repaint();
    }

    public void setShadowColor(Color backColor) {
        shadowBorder.setShadowColor(backColor);
        repaint();
    }

    public void setShadowVisible(boolean visible) {
        shadowBorder.setShadowVisible(visible);
        revalidate();
        repaint();
    }

    public void setVerticalShadowDepth(int depth) {
        shadowBorder.setVerticalShadowDepth(depth);
        revalidate();
        repaint();
    }

    public void showShadow() {
        shadowBorder.showShadow();
        revalidate();
        repaint();
    }

}