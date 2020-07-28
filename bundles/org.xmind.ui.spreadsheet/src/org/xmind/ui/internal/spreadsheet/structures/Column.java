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
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.util.MindMapUtils;

public class Column implements Comparable<Column> {

    private Chart ownedChart;

    private ColumnHead head;

    private List<Cell> cells = new ArrayList<Cell>();

    private int prefCellWidth = -1;

    private Integer left = null;

    private Integer width = null;

    public Column(Chart ownedChart, ColumnHead head) {
        Assert.isNotNull(ownedChart);
        Assert.isNotNull(head);
        this.ownedChart = ownedChart;
        this.head = head;
    }

    public Chart getOwnedChart() {
        return ownedChart;
    }

    public ColumnHead getHead() {
        return head;
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

    public int compareTo(Column o) {
        return getOwnedChart().getPrefColumnOrder().compareColumns(getHead(),
                o.getHead());
    }

    public int getPrefCellWidth() {
        if (prefCellWidth < 0) {
            prefCellWidth = calcPrefCellWidth();
        }
        return prefCellWidth;
    }

    private int calcPrefCellWidth() {
        int w = head.getPrefSize().width;
        for (Cell cell : cells) {
            w = Math.max(w, cell.getPrefContentSize().width);
        }
        return w;
    }

    public int getPrefWidth() {
        return Math.max(head.getPrefSize().width, getPrefCellWidth());
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
        IInsertion ins = (IInsertion) MindMapUtils.getCache(
                getOwnedChart().getTitle(), Spreadsheet.CACHE_COLUMN_INSERTION);
        int x;
        if (index == 0) {

            x = getOwnedChart().getTitle().getFigure().getBounds().x + lineWidth
                    + getOwnedChart().getRowHeadWidth() + lineWidth
                    + getOwnedChart().getMinorSpacing();
        } else {
            Column prev = getOwnedChart().getColumn(index - 1);
            x = prev.getRight() + lineWidth;
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
//            System.out.println(head.toString() + ": (" + left.intValue() + ","
//                    + right + ")");
            if (ins != null && ins.getIndex() == numCols) {
                right -= ins.getSize().width + getOwnedChart().getMinorSpacing()
                        + lineWidth;
            }
            w = right - lineWidth - this.left.intValue();
        } else {
            int headWidth = head.getPrefSize().width;
            w = Math.max(headWidth, getPrefCellWidth())
                    + getOwnedChart().getMinorSpacing();
        }
        this.width = Integer.valueOf(w);
    }

    public String toString() {
        return getHead().toString();
    }
}