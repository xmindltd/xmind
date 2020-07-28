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

import static org.eclipse.draw2d.PositionConstants.BOTTOM;
import static org.eclipse.draw2d.PositionConstants.CENTER;
import static org.eclipse.draw2d.PositionConstants.EAST;
import static org.eclipse.draw2d.PositionConstants.RIGHT;
import static org.eclipse.draw2d.PositionConstants.SOUTH;
import static org.eclipse.draw2d.PositionConstants.WEST;

import org.eclipse.draw2d.AbstractLabeledBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Color;

/**
 * @author Frank Shaka
 */
public class TitleBorder extends AbstractLabeledBorder {

    private int textAlignment = PositionConstants.CENTER;

    private int textPlacement = PositionConstants.NORTH;

    private Insets textPadding = new Insets(1, 2, 1, 2);

    private Color fillColor = null;

    private static final Dimension NO_EXTENT = new Dimension(0, 0);

    /**
     * 
     */
    public TitleBorder() {
        this(""); //$NON-NLS-1$
    }

    /**
     * @param s
     */
    public TitleBorder(String s) {
        super(s);
        setTextColor(ColorConstants.listForeground);
    }

    /**
     * @see org.eclipse.draw2d.AbstractLabeledBorder#calculateInsets(org.eclipse.draw2d.IFigure)
     */
    @Override
    protected Insets calculateInsets(IFigure figure) {
        switch (getTextPlacement()) {
        case EAST:
            return new Insets(0, getTextExtents(figure).width
                    + getTextPadding().getWidth(), 0, 0);
        case SOUTH:
            return new Insets(0, 0, getTextExtents(figure).height
                    + getTextPadding().getHeight(), 0);
        case WEST:
            return new Insets(0, 0, 0, getTextExtents(figure).width
                    + getTextPadding().getWidth());
        default:
            return new Insets(getTextExtents(figure).height
                    + getTextPadding().getHeight(), 0, 0, 0);
        }
    }

    /**
     * @see org.eclipse.draw2d.AbstractLabeledBorder#getTextExtents(org.eclipse.draw2d.IFigure)
     */
    @Override
    protected Dimension getTextExtents(IFigure f) {
        if (getLabel() == null || "".equals(getLabel())) { //$NON-NLS-1$
            return NO_EXTENT;
        }
        return super.getTextExtents(f);
    }

    /**
     * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
     *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
     */
    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        tempRect.setBounds(getPaintRectangle(figure, insets));
        Insets padding = getTextPadding();
        Dimension extent = getTextExtents(figure);
        int placement = getTextPlacement();
        switch (placement) {
        case EAST:
            tempRect.width = Math.min(tempRect.width, extent.width
                    + padding.getWidth());
            break;
        case SOUTH:
            int h = Math.min(tempRect.height, extent.height
                    + padding.getHeight());
            tempRect.y = tempRect.bottom() - h;
            tempRect.height = h;
            break;
        case WEST:
            int w = Math.min(tempRect.width, extent.width + padding.getWidth());
            tempRect.x = tempRect.right() - w;
            tempRect.width = w;
            break;
        default:
            tempRect.height = Math.min(tempRect.height, extent.height
                    + padding.getHeight());
        }
        graphics.clipRect(tempRect);
        Color bgColor = getBackgroundColor();
        if (bgColor != null) {
            graphics.setBackgroundColor(bgColor);
            graphics.fillRectangle(tempRect);
        }

        int x = tempRect.x + padding.left;
        int y = tempRect.y + padding.top;
        if (placement == EAST || placement == WEST) {
            int freeSpace = tempRect.height - padding.getHeight()
                    - extent.height;
            switch (getTextAlignment()) {
            case CENTER:
                y += freeSpace / 2;
                break;
            case BOTTOM:
                y += freeSpace;
                break;
            }
        } else {
            int freeSpace = tempRect.width - padding.getWidth() - extent.width;
            switch (getTextAlignment()) {
            case CENTER:
                x += freeSpace / 2;
                break;
            case RIGHT:
                x += freeSpace;
                break;
            }
        }
        graphics.setFont(getFont(figure));
        graphics.setForegroundColor(getTextColor());
        graphics.drawText(getLabel(), x, y);
    }

    /**
     * @return the textAlignment
     */
    public int getTextAlignment() {
        return textAlignment;
    }

    /**
     * @return the textPlacement
     */
    public int getTextPlacement() {
        return textPlacement;
    }

    /**
     * @return the textPadding
     */
    public Insets getTextPadding() {
        return textPadding;
    }

    /**
     * @return the background color
     */
    public Color getBackgroundColor() {
        return fillColor;
    }

    /**
     * Sets the alignment of the Text relative to the figure's client area. The
     * text alignment must be orthogonal to the text placement. For example, if
     * the placement is EAST, then the text can be aligned using TOP, CENTER, or
     * BOTTOM. Valid values are:
     * <UL>
     * <LI><EM>{@link PositionConstants#CENTER}</EM>
     * <LI>{@link PositionConstants#TOP}
     * <LI>{@link PositionConstants#BOTTOM}
     * <LI>{@link PositionConstants#LEFT}
     * <LI>{@link PositionConstants#RIGHT}
     * </UL>
     * 
     * @see #setTextPlacement(int)
     * @param textAlignment
     *            the text alignment to set
     */
    public void setTextAlignment(int textAlignment) {
        this.textAlignment = textAlignment;
    }

    /**
     * Sets the placement of text relative to the figure's client area. Valid
     * values are:
     * <UL>
     * <LI><EM>{@link PositionConstants#EAST}</EM>
     * <LI>{@link PositionConstants#NORTH}
     * <LI>{@link PositionConstants#SOUTH}
     * <LI>{@link PositionConstants#WEST}
     * </UL>
     * 
     * @param textPlacement
     *            the text placement to set
     */
    public void setTextPlacement(int textPlacement) {
        this.textPlacement = textPlacement;
        invalidate();
    }

    /**
     * Sets the padding around the text.
     * 
     * @param textPadding
     *            the text padding to set
     */
    public void setTextPadding(Insets textPadding) {
        this.textPadding = textPadding;
        invalidate();
    }

    public void setTextPadding(int all) {
        this.textPadding = new Insets(all);
        invalidate();
    }

    /**
     * Sets the background color of the area within the boundaries of this
     * border. If the argument is <code>null</code>, the title area will not
     * be filled.
     * 
     * @param color
     *            the background color to set, may be <code>null</code>
     */
    public void setBackgroundColor(Color color) {
        this.fillColor = color;
    }

}