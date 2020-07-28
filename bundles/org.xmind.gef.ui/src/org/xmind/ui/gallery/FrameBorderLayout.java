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

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 */
public class FrameBorderLayout extends AbstractHintLayout {

    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer CENTER = new Integer(PositionConstants.CENTER);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer TOP = new Integer(PositionConstants.TOP);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer BOTTOM = new Integer(PositionConstants.BOTTOM);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer LEFT = new Integer(PositionConstants.LEFT);
    /**
     * Constant to be used as a constraint for child figures
     */
    public static final Integer RIGHT = new Integer(PositionConstants.RIGHT);

    private IFigure center, left, top, bottom, right;
    private int vGap = 0, hGap = 0;

    /**
     * @see org.eclipse.draw2d.AbstractHintLayout#calculateMinimumSize(IFigure,
     *      int, int)
     */
    protected Dimension calculateMinimumSize(IFigure container, int wHint,
            int hHint) {
        Insets border = container.getInsets();
        return new Dimension(border.getWidth(), border.getHeight());
    }

    /**
     * @see AbstractLayout#calculatePreferredSize(IFigure, int, int)
     */
    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
        Insets border = container.getInsets();
        Dimension prefSize = new Dimension();

        if (center != null && center.isVisible()) {
            Dimension childSize = center.getPreferredSize();
            prefSize.setSize(childSize);
        }

        if (top != null && top.isVisible()) {
            Dimension childSize = top.getPreferredSize(prefSize.width, -1);
            prefSize.height += childSize.height + vGap;
        } else if (bottom != null && bottom.isVisible()) {
            Dimension childSize = bottom.getPreferredSize(prefSize.width, -1);
            prefSize.height += childSize.height + vGap;
        } else if (left != null && left.isVisible()) {
            Dimension childSize = left.getPreferredSize(-1, prefSize.height);
            prefSize.width += childSize.width + hGap;
        } else if (right != null && right.isVisible()) {
            Dimension childSize = right.getPreferredSize(-1, prefSize.height);
            prefSize.width += childSize.width + hGap;
        }

        prefSize.height += border.getHeight();
        prefSize.width += border.getHeight();

        return prefSize;
    }

    /**
     * @see org.eclipse.draw2d.LayoutManager#layout(IFigure)
     */
    public void layout(IFigure container) {
        Rectangle area = container.getClientArea();
        Rectangle rect = new Rectangle();

        int wHint = area.width;
        int hHint = area.height;

        Dimension centerSize;
        if (center != null && center.isVisible()) {
            centerSize = center.getPreferredSize(wHint, hHint);
        } else {
            centerSize = new Dimension();
        }

        if (top != null && top.isVisible()) {
            rect.setLocation(area.x, area.y);
            rect.setSize(area.width,
                    Math.max(0, area.height - centerSize.height - vGap));
            top.setBounds(rect);
            area.y += rect.height + vGap;
            area.height -= rect.height + vGap;
        } else if (bottom != null && bottom.isVisible()) {
            rect.setLocation(area.x, area.y + centerSize.height + vGap);
            rect.setSize(area.width,
                    Math.max(0, area.height - centerSize.height - vGap));
            bottom.setBounds(rect);
            area.height -= rect.height + vGap;
        } else if (left != null && left.isVisible()) {
            rect.setLocation(area.x, area.y);
            rect.setSize(Math.max(0, area.width - centerSize.width - hGap),
                    area.height);
            left.setBounds(rect);
            area.x += rect.width + hGap;
            area.width -= rect.width + hGap;
        } else if (right != null && right.isVisible()) {
            rect.setLocation(area.x + centerSize.width + hGap, area.y);
            rect.setSize(Math.max(0, area.width - centerSize.width - hGap),
                    area.height);
            right.setBounds(rect);
            area.width -= rect.width + hGap;
        }

        if (center != null && center.isVisible()) {
            if (area.width < 0)
                area.width = 0;
            if (area.height < 0)
                area.height = 0;
            center.setBounds(area);
        }
    }

    /**
     * @see org.eclipse.draw2d.AbstractLayout#remove(IFigure)
     */
    public void remove(IFigure child) {
        if (center == child) {
            center = null;
        } else if (top == child) {
            top = null;
        } else if (bottom == child) {
            bottom = null;
        } else if (right == child) {
            right = null;
        } else if (left == child) {
            left = null;
        }
    }

    /**
     * Sets the location of hte given child in this layout. Valid constraints:
     * <UL>
     * <LI>{@link #CENTER}</LI>
     * <LI>{@link #TOP}</LI>
     * <LI>{@link #BOTTOM}</LI>
     * <LI>{@link #LEFT}</LI>
     * <LI>{@link #RIGHT}</LI>
     * <LI><code>null</code> (to remove a child's constraint)</LI>
     * </UL>
     * 
     * <p>
     * Ensure that the given Figure is indeed a child of the Figure on which
     * this layout has been set. Proper behaviour cannot be guaranteed if that
     * is not the case. Also ensure that every child has a valid constraint.
     * </p>
     * <p>
     * Passing a <code>null</code> constraint will invoke
     * {@link #remove(IFigure)}.
     * </p>
     * <p>
     * If the given child was assigned another constraint earlier, it will be
     * re-assigned to the new constraint. If there is another child with the
     * given constraint, it will be over-ridden so that the given child now has
     * that constraint.
     * </p>
     * 
     * @see org.eclipse.draw2d.AbstractLayout#setConstraint(IFigure, Object)
     */
    public void setConstraint(IFigure child, Object constraint) {
        remove(child);
        super.setConstraint(child, constraint);
        if (constraint == null) {
            return;
        }

        switch (((Integer) constraint).intValue()) {
        case PositionConstants.CENTER:
            center = child;
            break;
        case PositionConstants.TOP:
            top = child;
            break;
        case PositionConstants.BOTTOM:
            bottom = child;
            break;
        case PositionConstants.RIGHT:
            right = child;
            break;
        case PositionConstants.LEFT:
            left = child;
            break;
        default:
            break;
        }
    }

    /**
     * Sets the horizontal spacing to be used between the children. Default is
     * 0.
     * 
     * @param gap
     *            The horizonal spacing
     */
    public void setHorizontalSpacing(int gap) {
        hGap = gap;
    }

    /**
     * Sets the vertical spacing ot be used between the children. Default is 0.
     * 
     * @param gap
     *            The vertical spacing
     */
    public void setVerticalSpacing(int gap) {
        vGap = gap;
    }

}