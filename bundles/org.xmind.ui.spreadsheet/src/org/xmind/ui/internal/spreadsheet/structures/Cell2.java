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
package org.xmind.ui.internal.spreadsheet.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class Cell2 extends BranchStructureData {

    private Chart2 ownedChart;

    private Column2 ownedColumn;

    private Row2 ownedRow;

    private List<Item2> items = new ArrayList<Item2>();

    private Dimension prefContentSize = null;

    private int contentHeight = -1;

    private int contentWidth = -1;

    private Rectangle bounds = null;

    public Cell2(Chart2 ownedChart, Column2 ownedColumn, Row2 ownedRow) {
        super(ownedColumn.getHead());
        this.ownedChart = ownedChart;
        this.ownedColumn = ownedColumn;
        this.ownedRow = ownedRow;
    }

    public Chart2 getOwnedChart() {
        return ownedChart;
    }

    public Row2 getOwnedRow() {
        return ownedRow;
    }

    public Column2 getOwnedColumn() {
        return ownedColumn;
    }

    void addItem(Item2 item) {
        items.add(item);
        item.setOwnedCell(this);
    }

    void removeItem(Item2 item) {
        items.remove(item);
        item.setOwnedCell(null);
    }

    public List<Item2> getItems() {
        return items;
    }

    public Dimension getPrefContentSize() {
        if (prefContentSize == null) {
            prefContentSize = calcPrefContentSize();
        }
        return prefContentSize;
    }

    private Dimension calcPrefContentSize() {
        int w = 0;
        int h = 0;
        int minorSpacing = getOwnedColumn().getMinorSpacing();
        Iterator<Item2> it = items.iterator();
        while (it.hasNext()) {
            Item2 item = it.next();
            IFigure itemFigure = item.getBranch().getFigure();
            Dimension size = itemFigure.getPreferredSize();
            w = Math.max(w, size.width);

            int bh = 0;
            IBranchPart parent = item.getBranch().getParentBranch();
            if (parent != null && !parent.getBoundaries().isEmpty()) {
                for (IBoundaryPart boundary : parent.getBoundaries()) {
                    List<IBranchPart> enclosingBranches = boundary
                            .getEnclosingBranches();
                    if (!(enclosingBranches.isEmpty()) && item.getBranch()
                            .equals(enclosingBranches.get(0))) {
                        bh = boundary.getFigure().getInsets().getHeight();
                        if (boundary.getTitle() != null
                                && boundary.getTitle().getFigure() != null) {
                            Dimension s = boundary.getTitle().getFigure()
                                    .getPreferredSize();
                            bh += s.height;
                        }
                    }
                }
            }

            h += size.height;
            if (it.hasNext())
                h += minorSpacing + bh;
        }

        IInsertion ins = ((IInsertion) MindMapUtils.getCache(
                getOwnedColumn().getHead(), IInsertion.CACHE_INSERTION));
        RowHead insHead = (RowHead) MindMapUtils.getCache(
                getOwnedColumn().getHead(), Spreadsheet.KEY_INSERTION_ROW_HEAD);
        if (ins != null && getOwnedRow().getHead().equals(insHead))
            h += ins.getSize().height + minorSpacing;

        return new Dimension(w, h);
    }

    public int getContentHeight() {
        if (contentHeight < 0) {
            contentHeight = calcContentHeight();
        }
        return contentHeight;
    }

    private int calcContentHeight() {
        if (!items.isEmpty()) {
            Item2 first = items.get(0);
            Item2 last = items.get(items.size() - 1);
            int height = last.getBranch().getFigure().getBounds().bottom() - 10
                    - first.getBranch().getFigure().getBounds().y;
            IInsertion ins = (IInsertion) MindMapUtils.getCache(getBranch(),
                    IInsertion.CACHE_INSERTION);
            if (ins != null
                    && (ins.getIndex() == 0 || ins.getIndex() == items.size())
                    && getOwnedRow().getHead().equals(MindMapUtils.getCache(
                            getBranch(), Spreadsheet.KEY_INSERTION_ROW_HEAD))) {
                height += ins.getSize().height
                        + getOwnedColumn().getMajorSpacing();
            }

            int bh = 0;
            for (Item2 item : items) {
                IBranchPart parent = item.getBranch().getParentBranch();
                if (parent != null) {
                    List<IBoundaryPart> boundaries = parent.getBoundaries();
                    if (!boundaries.isEmpty()) {
                        for (IBoundaryPart boundary : boundaries) {
                            List<IBranchPart> enclosingBranches = boundary
                                    .getEnclosingBranches();
                            if ((!enclosingBranches.isEmpty())
                                    && item.getBranch()
                                            .equals(enclosingBranches.get(0))) {
                                bh = boundary.getFigure().getInsets()
                                        .getHeight()
                                        - getOwnedChart().getMinorSpacing() - 3;
                                if (boundary.getTitle() != null && boundary
                                        .getTitle().getFigure() != null) {
                                    Dimension s = boundary.getTitle()
                                            .getFigure().getPreferredSize();
                                    bh += s.height;
                                }
                            }
                        }
                    }
                }
            }

            return height + bh;
        }
        return 0;
    }

    public int getContentWidth() {
        if (contentWidth < 0) {
            contentWidth = calcContentWidth();
        }
        return contentWidth;
    }

    private int calcContentWidth() {
        if (!items.isEmpty()) {
            Item2 first = items.get(0);
            Item2 last = items.get(items.size() - 1);
            int width = last.getBranch().getFigure().getBounds().right()
                    - first.getBranch().getFigure().getBounds().x;
            IInsertion ins = (IInsertion) MindMapUtils.getCache(getBranch(),
                    IInsertion.CACHE_INSERTION);
            if (ins != null
                    && (ins.getIndex() == 0 || ins.getIndex() == items.size())
                    && getOwnedRow().getHead().equals(MindMapUtils.getCache(
                            getBranch(), Spreadsheet.KEY_INSERTION_ROW_HEAD))) {
                width += ins.getSize().width
                        + getOwnedColumn().getMinorSpacing();
            }
            return width;
        }
        return 0;
    }

    public Item2 findItem(IBranchPart itemBranch) {
        for (Item2 item : items) {
            if (item.getBranch() == itemBranch)
                return item;
        }
        return null;
    }

    public Item2 getPreviousItem(Item2 item) {
        int index = getItemIndex(item);
        if (index > 0)
            return items.get(index - 1);
        return null;
    }

    public Item2 getNextItem(Item2 item) {
        int index = getItemIndex(item);
        if (index < items.size() - 1)
            return items.get(index + 1);
        return null;
    }

    public int getItemIndex(Item2 item) {
        return items.indexOf(item);
    }

    public int getX() {
        return getOwnedColumn().getLeft();
    }

    public int getY() {
        return getOwnedRow().getTop();
    }

    public int getWidth() {
        return getOwnedColumn().getWidth();
    }

    public int getHeight() {
        return getOwnedRow().getHeight();
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
        return bounds;
    }

    public String toString() {
        return "[" + getOwnedRow().toString() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + getOwnedRow().toString() + "]"; //$NON-NLS-1$
    }

}