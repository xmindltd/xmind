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

public class Row2 implements Comparable<Row2> {

    private Chart2 ownedChart;

    private RowHead head;

    private List<Cell2> cells = new ArrayList<Cell2>();

    private int prefCellHeight = -1;

    private Integer y = null;

    private Integer height = null;

    public Row2(Chart2 ownedChart, RowHead head) {
        Assert.isNotNull(ownedChart);
        Assert.isNotNull(head);
        this.ownedChart = ownedChart;
        this.head = head;
    }

    public Chart2 getOwnedChart() {
        return ownedChart;
    }

    public RowHead getHead() {
        return head;
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

    public int compareTo(Row2 o) {
        return getOwnedChart().getPrefRowOrder().compareRows(getHead(),
                o.getHead());
    }

    public int getPrefCellHeight() {
        if (prefCellHeight < 0) {
            prefCellHeight = calcPrefCellHeight();
        }
        return prefCellHeight;
    }

    private int calcPrefCellHeight() {
        int h = head.getPrefSize().height;
        for (Cell2 cell : cells) {
            h = Math.max(h, cell.getPrefContentSize().height);
        }
        return h;
    }

    public int getPrefHeight() {
        return Math.max(head.getPrefSize().height, getPrefCellHeight());
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
        Row2 prev = getOwnedChart().getPreviousRow(this);
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
                intY += getOwnedChart().getColHeadHeight() + lineWidth
                        + getOwnedChart().getMinorSpacing();
            }
            this.y = Integer.valueOf(intY);
        }
        int headHeight = getHead().getPrefSize().height;
        int cellHeight = 0;
        for (Cell2 cell : cells) {
            cellHeight = Math.max(cellHeight, cell.getContentHeight());
        }
        int h = Math.max(headHeight, cellHeight);
        this.height = Integer.valueOf(h + getOwnedChart().getMinorSpacing());
    }

    public String toString() {
        return getHead().toString();
    }
}