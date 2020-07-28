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
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class Column2 extends BranchStructureData {

    private Chart2 ownedChart;

    private List<Cell2> cells = new ArrayList<Cell2>();

    private int prefCellWidth = -1;

    private Integer left = null;

    private Integer width = null;

    public Column2(IBranchPart head, Chart2 ownedChart) {
        super(head);
        Assert.isNotNull(head);
        Assert.isNotNull(ownedChart);
        this.ownedChart = ownedChart;
    }

    public Chart2 getOwnedChart() {
        return ownedChart;
    }

    public IBranchPart getHead() {
        return getBranch();
    }

    void addCell(Cell2 cell) {
        cells.add(cell);
    }

    void removeCell(Cell2 cell) {
        cells.remove(cell);
    }

    public List<Cell2> getCells() {
        return cells;
    }

    public int getPrefCellWidth() {
        if (prefCellWidth < 0) {
            prefCellWidth = calcPrefCellWidth();
        }
        return prefCellWidth;
    }

    private int calcPrefCellWidth() {
        int w = 0;
        for (Cell2 cell : cells) {
            w = Math.max(w, cell.getPrefContentSize().width);
        }
        return w;
    }

    public int getPrefWidth() {
        return Math.max(
                getHead().getTopicPart().getFigure().getPreferredSize().width,
                getPrefCellWidth());
    }

    public Cell2 findCellByRow(Row2 row) {
        for (Cell2 cell : cells) {
            if (cell.getOwnedRow() == row)
                return cell;
        }
        return null;
    }

    public Cell2 findCellByItem(IBranchPart itemBranch) {
        for (Cell2 cell : cells) {
            if (cell.findItem(itemBranch) != null)
                return cell;
        }
        return null;
    }

    public Item2 findItem(IBranchPart itemBranch) {
        for (Cell2 cell : cells) {
            Item2 item = cell.findItem(itemBranch);
            if (item != null)
                return item;
        }
        return null;
    }

    public Cell2 getPreviousCell(Cell2 cell) {
        int index = getCellIndex(cell);
        if (index > 0)
            return cells.get(index - 1);
        return null;
    }

    public Cell2 getNextCell(Cell2 cell) {
        int index = getCellIndex(cell);
        if (index < cells.size() - 1)
            return cells.get(index + 1);
        return null;
    }

    public int getCellIndex(Cell2 cell) {
        return cells.indexOf(cell);
    }

    public int getLeft() {
        ensurePosition();
        return left.intValue();
    }

    public int getWidth() {
        ensurePosition();
        return width.intValue();
    }

    public int getRight() {
        return getLeft() + getWidth();
    }

    private void ensurePosition() {
        if (this.left != null && this.width != null)
            return;

        int lineWidth = getOwnedChart().getLineWidth();
        int index = getOwnedChart().getColumnIndex(this);
        IInsertion ins = (IInsertion) MindMapUtils.getCache(getOwnedChart()
                .getTitle(), Spreadsheet.CACHE_COLUMN_INSERTION);
        int x;
        if (index == 0) {
            x = getOwnedChart().getTitle().getFigure().getBounds().x
                    + lineWidth + getOwnedChart().getRowHeadWidth()
                    + getOwnedChart().getMajorSpacing();
        } else {
            Column2 prev = getOwnedChart().getColumn(index - 1);
            x = prev.getRight();
        }
        if (ins != null && ins.getIndex() == index) {
            x += ins.getSize().width + getOwnedChart().getMinorSpacing()
                    + lineWidth;
        }
        this.left = Integer.valueOf(x);

        int w;
        int numCols = getOwnedChart().getNumColumns();
        if (index == numCols - 1) {
            int right = getOwnedChart().getTitle().getFigure().getBounds()
                    .right();
            if (ins != null && ins.getIndex() == numCols) {
                right -= ins.getSize().width
                        + getOwnedChart().getMinorSpacing() + lineWidth;
            }
            w = right - lineWidth - this.left.intValue();
        } else {
            int headWidth = getBranch().getTopicPart().getFigure().getBounds().width;
            w = Math.max(headWidth, getPrefCellWidth())
                    + getOwnedChart().getMinorSpacing();
        }
        this.width = Integer.valueOf(w);
    }

    public Cell2 findCell(Point point) {
        if (getLeft() < point.x && getLeft() + getWidth() > point.x) {
            for (Cell2 cell : cells) {
                Row2 row = cell.getOwnedRow();
                if (row.getTop() < point.y
                        && row.getTop() + row.getHeight() > point.y)
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