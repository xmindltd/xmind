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

import static org.xmind.ui.gallery.NavigationViewer.BIG_HEIGHT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationItemLayout extends AbstractLayout {

    private static final int H_SPACING = 10;

    private static final int BIG_TOP_MARGIN = 10;

    private static final int SMALL_TOP_MARGIN = 25;

    private IFigure center = null;

    private Map<IFigure, Rectangle> cache = null;

    private int centerX = -1;

    private int childrenWidth = -1;

    private int offset = 0;

    private int maxOffset = -1;

    private int minOffset = -1;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
     */
    public void layout(IFigure container) {
        buildCache(container);
        Rectangle r = container.getClientArea(Rectangle.SINGLETON);
        int x = r.x + r.width / 2 - centerX + offset;
        int y = r.y + BIG_TOP_MARGIN;
        for (Entry<IFigure, Rectangle> en : cache.entrySet()) {
            IFigure child = en.getKey();
            Rectangle b = en.getValue();
            child.setBounds(b.getTranslated(x, y));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.
     * draw2d.IFigure, int, int)
     */
    @Override
    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
        if (wHint >= 0 && hHint >= 0)
            return new Dimension(wHint, hHint);
        buildCache(container);
        int width = childrenWidth + H_SPACING + H_SPACING;
        int height = BIG_HEIGHT + BIG_TOP_MARGIN + BIG_TOP_MARGIN;
        return new Dimension(width, height);
    }

    private void buildCache(IFigure container) {
        if (cache != null)
            return;
        cache = new HashMap<IFigure, Rectangle>();
        List children = container.getChildren();
        int x = 0;
        int min = 1000;
        int max = 0;
        for (int i = 0; i < children.size(); i++) {
            IFigure child = (IFigure) children.get(i);
            Dimension size = child.getPreferredSize();
            int cx = x + size.width / 2;
            int y;
            if (child == center) {
                centerX = cx;
                y = 0;
            } else {
                y = SMALL_TOP_MARGIN - BIG_TOP_MARGIN;
            }
            cache.put(child, new Rectangle(x, y, size.width, size.height));
            x += size.width + H_SPACING;
            min = Math.min(min, cx);
            max = Math.max(max, cx);
        }
        childrenWidth = x - H_SPACING;
        if (centerX < 0) {
            centerX = childrenWidth / 2;
        }
        minOffset = centerX - max;
        maxOffset = centerX - min;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.draw2d.AbstractLayout#setConstraint(org.eclipse.draw2d.IFigure
     * , java.lang.Object)
     */
    @Override
    public void setConstraint(IFigure child, Object constraint) {
        if (child == constraint) {
            this.center = child;
        }
        super.setConstraint(child, constraint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.AbstractLayout#invalidate()
     */
    @Override
    public void invalidate() {
        super.invalidate();
        cache = null;
        centerX = -1;
        childrenWidth = -1;
        maxOffset = -1;
        minOffset = -1;
    }

    /**
     * @param offset
     *            the offset to set
     */
    public void addOffset(IFigure container, int offset) {
        offset += this.offset;
        buildCache(container);
        this.offset = Math.max(minOffset, Math.min(maxOffset, offset));
    }

    /**
     * 
     */
    public void resetOffset(IFigure container) {
        this.offset = 0;
    }

}
