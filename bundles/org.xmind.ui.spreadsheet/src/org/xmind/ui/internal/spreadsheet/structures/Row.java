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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;

public class Row extends BranchStructureData {

    private Chart ownedChart;

    private List<Cell> cells = new ArrayList<Cell>();

    private int prefCellHeight = -1;

    private Integer y = null;

    private Integer height = null;

    public Row(IBranchPart head, Chart ownedChart) {
        super(head);
        Assert.isNotNull(head);
        Assert.isNotNull(ownedChart);
        this.ownedChart = ownedChart;
    }

    public Chart getOwnedChart() {
        return ownedChart;
    }

    public IBranchPart getHead() {
        return getBranch();
    }

    void addCell(Cell cell) {
        cells.add(cell);
    }

    void removeCell(Cell cell) {
        cells.remove(cell);
    }

    public List<Cell> getCells() {
        return cells;
    }

    public int getPrefCellHeight() {
        if (prefCellHeight < 0) {
            prefCellHeight = calcPrefCellHeight();
        }
        return prefCellHeight;
    }

    private int calcPrefCellHeight() {
        int h = 0;
        for (Cell cell : cells) {
            h = Math.max(h, cell.getPrefContentSize().height);
        }
        return h;
    }

    public int getPrefHeight() {
        return Math.max(
                getHead().getTopicPart().getFigure().getPreferredSize().height,
                getPrefCellHeight());
    }

    public Cell findCellByColumn(Column col) {
        for (Cell cell : cells) {
            if (cell.getOwnedColumn() == col)
                return cell;
        }
        return null;
    }

    public Cell findCellByItem(IBranchPart itemBranch) {
        for (Cell cell : cells) {
            if (cell.findItem(itemBranch) != null)
                return cell;
        }
        return null;
    }

    public Item findItem(IBranchPart itemBranch) {
        for (Cell cell : cells) {
            Item item = cell.findItem(itemBranch);
            if (item != null)
                return item;
        }
        return null;
    }

    public Cell getPreviousCell(Cell cell) {
        int index = getCellIndex(cell);
        if (index > 0)
            return cells.get(index - 1);
        return null;
    }

    public Cell getNextCell(Cell cell) {
        int index = getCellIndex(cell);
        if (index < cells.size() - 1)
            return cells.get(index + 1);
        return null;
    }

    public int getCellIndex(Cell cell) {
        return cells.indexOf(cell);
    }

    public int getTop() {
        ensurePosition();
        return y.intValue();
    }

    public int getHeight() {
        ensurePosition();
        return height.intValue();
    }

    private void ensurePosition() {
        if (this.y != null && this.height != null)
            return;

        int lineWidth = getOwnedChart().getLineWidth();
        Row prev = getOwnedChart().getPreviousRow(this);
        if (prev != null) {
            this.y = Integer
                    .valueOf(prev.getTop() + prev.getHeight() + lineWidth);
        } else {
            int intY = getOwnedChart().getTitle().getTopicPart().getFigure()
                    .getBounds().bottom() + lineWidth;
            if (getOwnedChart().getTitle().getInfoPart() != null) {
                intY += getOwnedChart().getTitle().getInfoPart().getFigure()
                        .getBounds().height;
            }
            if (getOwnedChart().hasColumns()) {
                intY += getOwnedChart().getColumnHeadHeight() + lineWidth
                        + getOwnedChart().getMajorSpacing();
            }
            this.y = Integer.valueOf(intY);
        }
        int headHeight = getBranch().getTopicPart().getFigure()
                .getBounds().height;
        int cellHeight = 0;
        for (Cell cell : cells) {
            cellHeight = Math.max(cellHeight, cell.getContentHeight());
        }
        int h = Math.max(headHeight, cellHeight);
        this.height = Integer.valueOf(h + getOwnedChart().getMinorSpacing());
    }

    public Cell findCell(Point point) {
        if (getTop() < point.y && getTop() + getHeight() > point.y) {
            for (Cell cell : cells) {
                Column col = cell.getOwnedColumn();
                if (col.getLeft() < point.x
                        && col.getLeft() + col.getWidth() > point.x)
                    return cell;
            }
        }
        return null;
    }

    public int getMajorSpacing() {
        return super.getMajorSpacing();
    }

    public int getMinorSpacing() {
        return super.getMinorSpacing();
    }

    public String toString() {
        return getHead().toString();
    }
}