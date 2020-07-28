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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class Chart2 extends BranchStructureData {

    private List<Column2> cols = null;

    private List<Row2> rows = null;

    private int titleAreaHeight = -1;

    private int colHeadHeight = -1;

    private int rowHeadWidth = -1;

    private int lineWidth = -1;

    private RowOrder prefRowOrder = null;

    public Chart2(IBranchPart branch) {
        super(branch);
    }

    void setContent(Column2 col, Row2... row) {
        cols = Collections.singletonList(col);
        rows = Arrays.asList(row);
    }

    void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public IBranchPart getTitle() {
        return super.getBranch();
    }

    public List<Row2> getRows() {
        ensureBuilt();
        return rows;
    }

    public List<Column2> getColumns() {
        ensureBuilt();
        return cols;
    }

    public boolean hasRows() {
        return !getRows().isEmpty();
    }

    public boolean hasColumns() {
        return !getColumns().isEmpty();
    }

    public int getNumRows() {
        return getRows().size();
    }

    public int getNumColumns() {
        return getColumns().size();
    }

    public int getNumValidRows() {
        int num = 0;
        for (Row2 row : getRows()) {
            if (!RowHead.EMPTY.equals(row.getHead())
                    && !row.getCells().isEmpty())
                num++;
        }
        return num;
    }

    public Row2 getFirstRow() {
        return getRow(0);
    }

    public Row2 getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRows().size())
            return null;
        return getRows().get(rowIndex);
    }

    public Column2 getColumn(int colIndex) {
        if (colIndex < 0 || colIndex >= getColumns().size())
            return null;
        return getColumns().get(colIndex);
    }

    private void ensureBuilt() {
        if (rows != null && cols != null)
            return;
        rows = null;
        cols = null;
        lazyBuild();
        if (cols == null)
            cols = Collections.emptyList();
        if (rows == null) {
            rows = Collections.emptyList();
        }
    }

    private void lazyBuild() {
        if (getTitle() == null)
            return;

        Map<Column2, List<Item2>> colItems = null;
        Map<Row2, List<Item2>> rowItems = null;
        List<Row2> rows2 = new ArrayList<Row2>();
        for (IBranchPart colHead : getTitle().getSubBranches()) {
            List<Item2> items = new ArrayList<Item2>();
            Column2 col = buildCol(colHead, items);
            if (!items.isEmpty()) {
                if (colItems == null)
                    colItems = new HashMap<Column2, List<Item2>>();
                colItems.put(col, items);
                for (Item2 item : items) {
                    RowHead prefHead = item.getPrefRowHead();
                    if (prefHead != null) {
                        Row2 row = rowItems == null ? null : findRow(
                                rowItems.keySet(), prefHead);
                        if (row == null) {
                            row = new Row2(this, prefHead);
                            rows2.add(row);
                            List<Item2> list = new ArrayList<Item2>();
                            list.add(item);
                            if (rowItems == null)
                                rowItems = new HashMap<Row2, List<Item2>>();
                            rowItems.put(row, list);
                        } else {
                            List<Item2> list = rowItems.get(row);
                            if (list == null) {
                                list = new ArrayList<Item2>();
                                rowItems.put(row, list);
                            }
                            list.add(item);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < rows2.size(); i++) {
            rows2.get(i).getHead().setIndex(i);
        }
        if (rowItems != null) {
            rows = new ArrayList<Row2>(new TreeSet<Row2>(rowItems.keySet()));
        }
        buildCells(colItems, rowItems);
    }

    private void buildCells(Map<Column2, List<Item2>> colItems,
            Map<Row2, List<Item2>> rowItems) {
        if (rows == null || cols == null)
            return;

        for (Column2 col : cols) {
            for (Row2 row : rows) {
                buildCell(col, row, colItems, rowItems);
            }
        }
    }

    private void buildCell(Column2 col, Row2 row,
            Map<Column2, List<Item2>> colItems, Map<Row2, List<Item2>> rowItems) {
        Cell2 cell = new Cell2(this, col, row);
        col.addCell(cell);
        row.addCell(cell);
        List<Item2> cItems = colItems == null ? null : colItems.get(col);
        List<Item2> rItems = rowItems == null ? null : rowItems.get(row);
        if (cItems != null && rItems != null) {
            for (Item2 item : rItems) {
                if (cItems.contains(item)) {
                    cell.addItem(item);
                }
            }
        }
    }

    private Row2 findRow(Collection<Row2> rows, RowHead rowHead) {
        for (Row2 row : rows) {
            if (rowHead.equals(row.getHead()))
                return row;
        }
        return null;
    }

    private Column2 buildCol(IBranchPart colHead, List<Item2> colItems) {
        Column2 col = new Column2(colHead, this);
        if (cols == null)
            cols = new ArrayList<Column2>();
        cols.add(col);
        for (IBranchPart child : colHead.getSubBranches()) {
            Item2 item = new Item2(this, child);
            colItems.add(item);
        }
        return col;
    }

    public int getTitleAreaHeight() {
        if (titleAreaHeight < 0) {
            titleAreaHeight = calcTitleAreaHeight();
        }
        return titleAreaHeight;
    }

    private int calcTitleAreaHeight() {
        int h = 0;
        int y = getBranch().getFigure().getBounds().y;
        IInfoPart info = getBranch().getInfoPart();
        if (info != null && info.getFigure().isVisible()) {
            h = info.getFigure().getBounds().bottom() - y;
        } else {
            ITopicPart topicPart = getBranch().getTopicPart();
            if (topicPart != null && topicPart.getFigure().isVisible()) {
                h = topicPart.getFigure().getBounds().bottom() - y;
            }
        }
        return h;
    }

    public int getColHeadHeight() {
        if (colHeadHeight < 0) {
            colHeadHeight = calcColHeadHeight();
        }
        return colHeadHeight;
    }

    private int calcColHeadHeight() {
        int maxTopicHeight = 0;
        int maxInfoHeight = 0;
        for (Column2 col : getColumns()) {
            IBranchPart head = col.getHead();
            ITopicPart topicPart = head.getTopicPart();
            if (topicPart != null)
                maxTopicHeight = Math.max(maxTopicHeight, topicPart.getFigure()
                        .getBounds().height);
            IInfoPart infoPart = head.getInfoPart();
            if (infoPart != null)
                maxInfoHeight = Math.max(maxInfoHeight, infoPart.getFigure()
                        .getBounds().height);
        }
        return maxTopicHeight + maxInfoHeight;
    }

    public int getRowHeadWidth() {
        if (rowHeadWidth < 0) {
            rowHeadWidth = calcRowHeadWidth();
        }
        return rowHeadWidth;
    }

    private int calcRowHeadWidth() {
        int sum = 0;
        for (Row2 row : getRows())
            sum = Math.max(sum, row.getHead().getPrefSize().width);
        return sum;
    }

    public int getLineWidth() {
        if (lineWidth < 0) {
            IStyleSelector ss = StyleUtils.getStyleSelector(getBranch());
            String decorationId = StyleUtils.getString(getBranch(), ss,
                    Styles.ShapeClass, null);
            lineWidth = StyleUtils.getInteger(getBranch(), ss,
                    Styles.LineWidth, decorationId, 1);
        }
        return lineWidth;
    }

    public int getMajorSpacing() {
        if (getBranch() == null)
            return 5;
        return super.getMajorSpacing();
    }

    public int getMinorSpacing() {
        if (getBranch() == null)
            return 1;
        return super.getMinorSpacing();
    }

    public Column2 getPreviousColumn(Column2 col) {
        int index = getColumnIndex(col);
        if (index > 0)
            return getColumns().get(index - 1);
        return null;
    }

    public Column2 getNextColumn(Column2 col) {
        int index = getColumnIndex(col);
        if (index < getColumns().size() - 1)
            return getColumns().get(index + 1);
        return null;
    }

    public int getRowIndex(Row2 row) {
        return getRows().indexOf(row);
    }

    public Row2 getPreviousRow(Row2 row) {
        int index = getRowIndex(row);
        if (index > 0)
            return getRows().get(index - 1);
        return null;
    }

    public Row2 getNextRow(Row2 row) {
        int index = getRowIndex(row);
        if (index < getRows().size() - 1)
            return getRows().get(index + 1);
        return null;
    }

    public int getColumnIndex(Column2 col) {
        return getColumns().indexOf(col);
    }

    public Cell2 findCell(Point point) {
        //TODO
        for (Column2 col : getColumns()) {
            for (Cell2 cell : col.getCells()) {
                if (cell.getBounds().contains(point))
                    return cell;
            }
        }
        return null;
    }

    public RowHead findRowHead(Point point) {
        if (hasRows()) {
            int x = getTitle().getTopicPart().getFigure().getBounds().x
                    - getMajorSpacing();
            if (point.x > x
                    && point.x < x + getRowHeadWidth() + getMajorSpacing() * 2) {
                for (Row2 row : getRows()) {
                    int y = row.getTop();
                    if (point.y > y && point.y < y + row.getHeight()) {
                        return row.getHead();
                    }
                }
            }
        }
        return null;
    }

    public Row2 findRow(RowHead colHead) {
        for (Row2 row : getRows()) {
            if (row.getHead().equals(colHead))
                return row;
        }
        return null;
    }

    public RowOrder getPrefRowOrder() {
        if (prefRowOrder == null) {
            prefRowOrder = RowOrder.createFromTopic(getTitle().getTopic());
        }
        return prefRowOrder;
    }
}