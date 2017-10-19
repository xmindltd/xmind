/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.gallery;

import static org.xmind.ui.gallery.NavigationViewer.BIG_ALPHA;
import static org.xmind.ui.gallery.NavigationViewer.BIG_HEIGHT;
import static org.xmind.ui.gallery.NavigationViewer.SMALL_ALPHA;
import static org.xmind.ui.gallery.NavigationViewer.SMALL_HEIGHT;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationItemFigure extends Figure {

    private class MaskBorder extends LineBorder {

        /**
         * 
         */
        public MaskBorder() {
            super(ColorConstants.lightGray, 1);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
         */
        public Insets getInsets(IFigure figure) {
            return NO_INSETS;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
         * org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
         */
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            if (alpha > 0) {
                tempRect.setBounds(getPaintRectangle(figure, insets));
                graphics.setAlpha(alpha);
                graphics.setBackgroundColor(ColorConstants.black);
                graphics.fillRectangle(tempRect);
            } else {
                super.paint(figure, graphics, insets);
            }
        }

    }

    private static final double MIN_SCALE = ((double) SMALL_HEIGHT)
            / ((double) BIG_HEIGHT);

    private static final double MAX_SCALE = 1;

    private Image image = null;

    private String text = null;

    private double state = 0;

    private int alpha = 0;

    /**
     * 
     */
    public NavigationItemFigure() {
        setBorder(new MaskBorder());
        setOpaque(true);
        setBackgroundColor(ColorConstants.white);
        updateState();
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(Image image) {
        if (image == this.image)
            return;
        this.image = image;
        repaint();
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        if (text == this.text || (text != null && text.equals(this.text)))
            return;
        this.text = text;
        repaint();
    }

    /**
     * @return the state
     */
    public double getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(double state) {
        if (state == this.state)
            return;
        this.state = state;
        updateState();
        revalidate();
    }

    private void updateState() {
        int s = (int) seg(SMALL_HEIGHT, BIG_HEIGHT, state);
        setPreferredSize(s, s);
        alpha = (int) seg(SMALL_ALPHA, BIG_ALPHA, state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAdvanced(true);
        graphics.setAntialias(SWT.ON);
        super.paint(graphics);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);

        Rectangle b = getBounds();
        float cx = b.x + ((float) b.width) / 2;
        float cy = b.y + ((float) b.height) / 2;
        double scale = seg(MIN_SCALE, MAX_SCALE, state);
        Rectangle r = getClientArea(Rectangle.SINGLETON);
        float x = r.x - ((float) BIG_HEIGHT - b.width) / 2 - cx;
        float y = r.y - ((float) BIG_HEIGHT - b.height) / 2 - cy;
        float w = r.width + (BIG_HEIGHT - b.width);
        float h = r.height + (BIG_HEIGHT - b.height);

        graphics.pushState();

        try {
            // Configure graphics:
            graphics.translate(cx, cy);
            graphics.scale(scale);

            if (image != null) {
                paintImage(graphics, x, y, w, h);
            } else if (text != null) {
                paintText(graphics, x, y, w, h);
            }
        } finally {
            graphics.restoreState();
            graphics.popState();
        }
    }

    private void paintImage(Graphics graphics, float x, float y, float w,
            float h) {
        org.eclipse.swt.graphics.Rectangle ir = image.getBounds();
        float iw = ir.width;
        float ih = ir.height;
        float tw = iw;
        float th = ih;
        if (tw > w) {
            th = th * w / tw;
            tw = w;
        }
        if (th > h) {
            tw = tw * h / th;
            th = h;
        }
        float tx = x + (w - tw) / 2;
        float ty = y + (h - th) / 2;
        graphics.drawImage(image, 0, 0, (int) iw, (int) ih, (int) tx, (int) ty,
                (int) tw, (int) th);
    }

    private void paintText(Graphics graphics, float x, float y, float w, float h) {
        float m = w * 0.05f;
        x += m;
        y += m;
        w -= m + m;
        h -= m + m;
        Dimension s = GraphicsUtils.getAdvanced().getTextSize(text,
                graphics.getFont());
        float tw = s.width;
        float th = s.height;
        double ts = 1;
        if (tw > w) {
            th = th * w / tw;
            ts = ts * w / tw;
            tw = w;
        }
        if (th > h) {
            tw = tw * h / th;
            ts = ts * h / th;
            th = h;
        }
        float tx = x + (w - tw) / 2;
        float ty = y + (h - th) / 2;
        if (ts == 1) {
            graphics.drawText(text, (int) tx, (int) ty);
        } else {
            graphics.scale(ts);
            graphics.drawText(text, (int) (tx / ts), (int) (ty / ts));
        }
    }

    private static double seg(double min, double max, double ratio) {
        return min + (max - min) * ratio;
    }

}
