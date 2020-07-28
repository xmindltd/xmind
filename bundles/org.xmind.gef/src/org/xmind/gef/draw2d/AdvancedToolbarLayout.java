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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class AdvancedToolbarLayout extends org.eclipse.draw2d.ToolbarLayout {

    private int majorAlignment = ALIGN_CENTER;

    private boolean stretchMajorAxis = false;

    private int innerMinorAlignment = ALIGN_CENTER;

    /**
     * 
     */
    public AdvancedToolbarLayout() {
        this(false);
    }

    /**
     * @param isHorizontal
     */
    public AdvancedToolbarLayout(boolean isHorizontal) {
        super(isHorizontal);
        setMinorAlignment(ALIGN_CENTER);
    }

    /**
     * @param majorAlignment
     *            the majorAlignment to set
     */
    public void setMajorAlignment(int majorAlignment) {
        this.majorAlignment = majorAlignment;
    }

    /**
     * @return the majorAlignment
     */
    public int getMajorAlignment() {
        return majorAlignment;
    }

    /**
     * @return the stretchMajorAxis
     */
    public boolean isStretchMajorAxis() {
        return stretchMajorAxis;
    }

    /**
     * @param stretchMajorAxis
     *            the stretchMajorAxis to set
     */
    public void setStretchMajorAxis(boolean stretchMajorAxis) {
        this.stretchMajorAxis = stretchMajorAxis;
    }

    public int getInnerMinorAlignment() {
        return innerMinorAlignment;
    }

    public void setInnerMinorAlignment(int innerMinorAlignment) {
        this.innerMinorAlignment = innerMinorAlignment;
    }

    /**
     * @see org.eclipse.draw2d.ToolbarLayout#layout(org.eclipse.draw2d.IFigure)
     */
    @Override
    public void layout(IFigure parent) {
        List children = parent.getChildren();
        int numChildren = children.size();
        Rectangle clientArea = transposer.t(parent.getClientArea());
        int x = clientArea.x;
        int y = clientArea.y;
        int prefSpacing;
//        int availableHeight = clientArea.height;

        Dimension prefSizes[] = new Dimension[numChildren];
        Dimension minSizes[] = new Dimension[numChildren];

        // Calculate the width and height hints.  If it's a vertical ToolBarLayout,
        // then ignore the height hint (set it to -1); otherwise, ignore the 
        // width hint.  These hints will be passed to the children of the parent
        // figure when getting their preferred size. 
        int wHint = -1;
        int hHint = -1;
        if (isHorizontal()) {
            hHint = parent.getClientArea(Rectangle.SINGLETON).height;
        } else {
            wHint = parent.getClientArea(Rectangle.SINGLETON).width;
        }

        /*
         * Calculate sum of preferred heights of all children(totalHeight).
         * Calculate sum of minimum heights of all children(minHeight). Cache
         * Preferred Sizes and Minimum Sizes of all children.
         * 
         * totalHeight is the sum of the preferred heights of all children
         * totalMinHeight is the sum of the minimum heights of all children
         * prefMinSumHeight is the sum of the difference between all children's
         * preferred heights and minimum heights. (This is used as a ratio to
         * calculate how much each child will shrink).
         */
        IFigure child;
        int totalHeight = 0;
        int totalWidth = 0;
//        int totalMinHeight = 0;
//        int prefMinSumHeight = 0;

        for (int i = 0; i < numChildren; i++) {
            child = (IFigure) children.get(i);

            prefSizes[i] = transposer.t(child.getPreferredSize(wHint, hHint));
            minSizes[i] = transposer.t(child.getMinimumSize(wHint, hHint));

            totalHeight += prefSizes[i].height;
            totalWidth = Math.max(totalWidth, prefSizes[i].width);
//            totalMinHeight += minSizes[i].height;
        }

        if (stretchMajorAxis && numChildren > 1) {
            prefSpacing = (clientArea.height - totalHeight) / (numChildren - 1);
            totalHeight = clientArea.height;
        } else {
            prefSpacing = getSpacing();
            totalHeight += (numChildren - 1) * getSpacing();
        }
//        totalMinHeight += (numChildren - 1) * spacing;
//        prefMinSumHeight = totalHeight - totalMinHeight;
        /*
         * The total amount that the children must be shrunk is the sum of the
         * preferred Heights of the children minus Max(the available area and
         * the sum of the minimum heights of the children).
         * 
         * amntShrinkHeight is the combined amount that the children must shrink
         * amntShrinkCurrentHeight is the amount each child will shrink
         * respectively
         */
//        int amntShrinkHeight = totalHeight - Math.max(availableHeight, totalMinHeight);
//
//        if (amntShrinkHeight < 0) {
//            amntShrinkHeight = 0;
//        }
        if (majorAlignment == ALIGN_CENTER) {
            y += (clientArea.height - totalHeight) / 2;
        } else if (majorAlignment == ALIGN_BOTTOMRIGHT) {
            y += clientArea.height - totalHeight;
        }

        if (isStretchMinorAxis())
            totalWidth = Integer.MAX_VALUE;
        totalWidth = Math.max(0, Math.min(clientArea.width, totalWidth));

        int adjust = clientArea.width - totalWidth;
        switch (getMinorAlignment()) {
        case ALIGN_TOPLEFT:
            adjust = 0;
            break;
        case ALIGN_CENTER:
            adjust /= 2;
            break;
        case ALIGN_BOTTOMRIGHT:
            break;
        }

        for (int i = 0; i < numChildren; i++) {
//            int amntShrinkCurrentHeight = 0;
            int prefHeight = prefSizes[i].height;
//            int minHeight = minSizes[i].height;
            int prefWidth = prefSizes[i].width;
            int minWidth = minSizes[i].width;
            Rectangle newBounds = new Rectangle(x, y, prefWidth, prefHeight);

            child = (IFigure) children.get(i);
//            if (prefMinSumHeight != 0)
//                amntShrinkCurrentHeight = 
//                        (prefHeight - minHeight) * amntShrinkHeight / (prefMinSumHeight);

            int width = Math.min(prefWidth, //totalWidth);
                    transposer.t(child.getMaximumSize()).width);
            if (isStretchMinorAxis())
                width = transposer.t(child.getMaximumSize()).width;
            width = Math.max(minWidth, Math.min(clientArea.width, width));
            newBounds.width = width;

            int adjust2 = totalWidth - width;
            switch (innerMinorAlignment) {
            case ALIGN_BOTTOMRIGHT:
                adjust2 = 0;
                break;
            case ALIGN_CENTER:
                adjust2 /= 2;
                break;
            case ALIGN_TOPLEFT:
                break;
            }
            newBounds.x += adjust + adjust2;
//            newBounds.height -= amntShrinkCurrentHeight;
            child.setBounds(transposer.t(newBounds));

//            amntShrinkHeight -= amntShrinkCurrentHeight;
//            prefMinSumHeight -= (prefHeight - minHeight);
            y += newBounds.height + prefSpacing;
        }
    }

}