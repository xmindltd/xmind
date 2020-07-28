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

import static org.xmind.ui.gallery.GalleryLayout.ALIGN_CENTER;
import static org.xmind.ui.gallery.GalleryLayout.ALIGN_FILL;

import java.util.Iterator;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.AdvancedToolbarLayout;
import org.xmind.gef.ui.internal.SpaceCollaborativeEngine;

public class ContentPane extends Figure {

    private class ContentPaneFlowLayout extends FlowLayout {

        /**
         * Holds the necessary information for layout calculations.
         */
        private class WorkingData2 {
            public Rectangle bounds[], area;
            public IFigure row[];
            public int rowHeight, rowWidth, rowCount, rowX, rowY, maxWidth;
        }

        private WorkingData2 data2 = null;

        private int minorSpacing2 = 0;

        public ContentPaneFlowLayout(boolean isHorizontal) {
            super(isHorizontal);
        }

        public void setMinorSpacing2(int minorSpacing2) {
            this.minorSpacing2 = minorSpacing2;
        }

        public int getMinorSpacing2() {
            return minorSpacing2;
        }

        @Override
        public void layout(IFigure parent) {
            data2 = new WorkingData2();
            Rectangle relativeArea = parent.getClientArea();
            data2.area = transposer.t(relativeArea);

            Iterator iterator = parent.getChildren().iterator();
            int dx;

            // Calculate the hints to be passed to children
            int wHint = -1;
            int hHint = -1;
            if (isHorizontal())
                wHint = parent.getClientArea().width;
            else
                hHint = parent.getClientArea().height;

            initVariables2(parent);
            initRow2();
            while (iterator.hasNext()) {
                IFigure f = (IFigure) iterator.next();
                Dimension pref = transposer.t(getChildSize(f, wHint, hHint));
                Rectangle r = new Rectangle(0, 0, pref.width, pref.height);

                if (data2.rowCount > 0) {
                    if (data2.rowWidth + pref.width > data2.maxWidth)
                        layoutRow(parent);
                }
                r.x = data2.rowX;
                r.y = data2.rowY;
                dx = r.width + Math.max(getMinorSpacing(), getMinorSpacing2());
                data2.rowX += dx;
                data2.rowWidth += dx;
                data2.rowHeight = Math.max(data2.rowHeight, r.height);
                data2.row[data2.rowCount] = f;
                data2.bounds[data2.rowCount] = r;
                data2.rowCount++;
            }
            if (data2.rowCount != 0)
                layoutRow(parent);
            data2 = null;
        }

        @Override
        protected void layoutRow(IFigure parent) {
            int majorAdjustment = 0;
            int minorAdjustment = 0;
            int correctMajorAlignment = getMajorAlignment();
            int correctMinorAlignment = getMinorAlignment();

            majorAdjustment = data2.area.width - data2.rowWidth
                    + Math.max(getMinorSpacing(), getMinorSpacing2());

            switch (correctMajorAlignment) {
            case ALIGN_TOPLEFT:
                majorAdjustment = 0;
                break;
            case ALIGN_CENTER:
                majorAdjustment /= 2;
                break;
            case ALIGN_BOTTOMRIGHT:
                break;
            }

            for (int j = 0; j < data2.rowCount; j++) {
                if (isStretchMinorAxis()) {
                    data2.bounds[j].height = data2.rowHeight;
                } else {
                    minorAdjustment = data2.rowHeight - data2.bounds[j].height;
                    switch (correctMinorAlignment) {
                    case ALIGN_TOPLEFT:
                        minorAdjustment = 0;
                        break;
                    case ALIGN_CENTER:
                        minorAdjustment /= 2;
                        break;
                    case ALIGN_BOTTOMRIGHT:
                        break;
                    }
                    data2.bounds[j].y += minorAdjustment;
                }
                data2.bounds[j].x += majorAdjustment;

                setBoundsOfChild(parent, data2.row[j],
                        transposer.t(data2.bounds[j]));
            }
            data2.rowY += getMajorSpacing() + data2.rowHeight;
            initRow2();
        }

        private void initRow2() {
            data2.rowX = 0;
            data2.rowHeight = 0;
            data2.rowWidth = 0;
            data2.rowCount = 0;
        }

        private void initVariables2(IFigure parent) {
            data2.row = new IFigure[parent.getChildren().size()];
            data2.bounds = new Rectangle[data2.row.length];
            data2.maxWidth = data2.area.width;
        }
    }

    private AdvancedToolbarLayout layout = null;

    private FlowLayout wrapLayout = null;

    private int minorAlign = -1;

    private int minorSpacing = -1;

    private SpaceCollaborativeEngine spaceCollaborativeEngine = null;

    /**
     * 
     */
    public ContentPane() {
        this(false, false, false);
    }

    /**
     * @param isHorizontal
     * @param stretchMinorAxis
     * @param wrap
     */
    public ContentPane(boolean isHorizontal, boolean stretchMinorAxis,
            boolean wrap) {
        if (wrap) {
            wrapLayout = new ContentPaneFlowLayout(isHorizontal);
            wrapLayout.setStretchMinorAxis(stretchMinorAxis);
            wrapLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
            wrapLayout.setMinorAlignment(FlowLayout.ALIGN_CENTER);
            wrapLayout.setMajorSpacing(10);
            wrapLayout.setMinorSpacing(5);
            super.setLayoutManager(wrapLayout);
        } else {
            layout = new AdvancedToolbarLayout(isHorizontal);
            layout.setStretchMinorAxis(stretchMinorAxis);
            layout.setMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setMajorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setInnerMinorAlignment(AdvancedToolbarLayout.ALIGN_CENTER);
            layout.setSpacing(10);
            super.setLayoutManager(layout);
        }

    }

    public void setLayoutManager(LayoutManager manager) {
        // Do nothing to prevent external layout manager to be set. 
    }

    public boolean isHorizontal() {
        if (isWrap())
            return wrapLayout.isHorizontal();
        return layout.isHorizontal();
    }

    public void setHorizontal(boolean horizontal) {
        if (horizontal == isHorizontal())
            return;

        if (wrapLayout != null)
            wrapLayout.setHorizontal(horizontal);
        if (layout != null)
            layout.setHorizontal(horizontal);
        revalidate();
    }

    public boolean isWrap() {
        return getLayoutManager() == wrapLayout;
    }

    public void setWrap(boolean wrap) {
        if (wrap == isWrap())
            return;
        if (wrap) {
            if (wrapLayout == null) {
                boolean horizontal = isHorizontal();
                int majorAlignment = getMajorAlignment();
                int minorAlignment = getMinorAlignment();
                int majorSpacing = getMajorSpacing();
                int minorSpacing = getMinorSpacing();
                wrapLayout = new ContentPaneFlowLayout(horizontal);
                wrapLayout.setMajorAlignment(majorAlignment);
                wrapLayout.setMajorSpacing(majorSpacing);
                wrapLayout.setMinorSpacing(minorSpacing);
                boolean fill = minorAlignment == ALIGN_FILL;
                wrapLayout.setStretchMinorAxis(fill);
                wrapLayout.setMinorAlignment(
                        fill ? ALIGN_CENTER : minorAlignment);
            }
            super.setLayoutManager(wrapLayout);
        } else {
            if (layout == null) {
                boolean horizontal = isHorizontal();
                int majorAlignment = getMajorAlignment();
                int minorAlignment = getMinorAlignment();
                layout = new AdvancedToolbarLayout(horizontal);
                layout.setMajorAlignment(majorAlignment);
                layout.setSpacing(minorSpacing);
                boolean fill = minorAlignment == ALIGN_FILL;
                layout.setStretchMinorAxis(fill);
                layout.setMinorAlignment(fill ? ALIGN_CENTER : minorAlignment);
            }
            super.setLayoutManager(layout);
        }
    }

    public int getMajorAlignment() {
        if (isWrap())
            return wrapLayout.getMajorAlignment();
        return layout.getMajorAlignment();
    }

    public int getMinorAlignment() {
        return minorAlign;
    }

    public void setMajorAlignment(int alignment) {
        if (alignment == getMajorAlignment())
            return;

        if (wrapLayout != null)
            wrapLayout.setMajorAlignment(alignment);
        if (layout != null)
            layout.setMajorAlignment(alignment);
        revalidate();
    }

    public void setMinorAlignment(int alignment) {
        if (minorAlign >= 0 && alignment == getMinorAlignment())
            return;

        this.minorAlign = alignment;
        boolean fill = alignment == ALIGN_FILL;
        if (wrapLayout != null) {
            wrapLayout.setStretchMinorAxis(fill);
            wrapLayout.setMinorAlignment(fill ? ALIGN_CENTER : alignment);
        }
        if (layout != null) {
            layout.setStretchMinorAxis(fill);
            layout.setInnerMinorAlignment(fill ? ALIGN_CENTER : alignment);
        }
        revalidate();
    }

    public int getMajorSpacing() {
        if (isWrap())
            return wrapLayout.getMajorSpacing();
        return layout.getSpacing();
    }

    public void setMajorSpacing(int spacing) {
        if (spacing == getMajorSpacing())
            return;

        if (wrapLayout != null)
            wrapLayout.setMajorSpacing(spacing);
        if (layout != null)
            layout.setSpacing(spacing);
        revalidate();
    }

    public int getMinorSpacing() {
        return minorSpacing;
    }

    public void setMinorSpacing(int spacing) {
        if (minorSpacing >= 0 && spacing == getMinorSpacing())
            return;

        this.minorSpacing = spacing;
        if (wrapLayout != null)
            ((ContentPaneFlowLayout) wrapLayout).setMinorSpacing2(spacing);
        revalidate();
    }

    @Override
    public void invalidate() {
        if (getSpaceCollaborativeEngine() != null) {
            getSpaceCollaborativeEngine().refreshMinorSpace();
        }
        super.invalidate();
    }

    public SpaceCollaborativeEngine getSpaceCollaborativeEngine() {
        return spaceCollaborativeEngine;
    }

    public void setSpaceCollaborativeEngine(
            SpaceCollaborativeEngine spaceCollaborativeEngine) {
        this.spaceCollaborativeEngine = spaceCollaborativeEngine;
    }

}
