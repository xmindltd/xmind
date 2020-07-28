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

public class Chart extends BranchStructureData {

    private List<Row> rows = null;

    private List<Column> cols = null;

    private int titleAreaHeight = -1;

    private int columnHeadHeight = -1;

    private int rowHeadWidth = -1;

    private int lineWidth = -1;

    private ColumnOrder prefColumnOrder = null;

    public Chart(IBranchPart branch) {
        super(branch);
    }

    void setContent(Row row, Column... col) {
        rows = Collections.singletonList(row);
        cols = Arrays.asList(col);
    }

    void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public IBranchPart getTitle() {
        return super.getBranch();
    }

    public List<Row> getRows() {
        ensureBuilt();
        return rows;
    }

    public List<Column> getColumns() {
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

    public int getNumValidColumns() {
        int num = 0;
        for (Column col : getColumns()) {
            if (!ColumnHead.EMPTY.equals(col.getHead())
                    && !col.getCells().isEmpty()) {
                num++;
            }
        }
        return num;
    }

    public Row getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRows().size())
            return null;
        return getRows().get(rowIndex);
    }

    public Column getColumn(int colIndex) {
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
        if (rows == null)
            rows = Collections.emptyList();
        if (cols == null) {
            cols = new ArrayList<Column>(1);
            cols.add(new Column(this, ColumnHead.EMPTY));
        }
    }

    private void lazyBuild() {
        if (getTitle() == null)
            return;

        Map<Row, List<Item>> rowItems = null;
        Map<Column, List<Item>> colItems = null;
        List<Column> cols2 = new ArrayList<Column>();
        for (IBranchPart rowHead : getTitle().getSubBranches()) {
            List<Item> items = new ArrayList<Item>();
            Row row = buildRow(rowHead, items);
            if (!items.isEmpty()) {
                if (rowItems == null)
                    rowItems = new HashMap<Row, List<Item>>();
                rowItems.put(row, items);
                for (Item item : items) {
                    ColumnHead prefHead = item.getPrefColumnHead();
                    if (prefHead != null) {
                        Column col = colItems == null ? null
                                : findColumn(colItems.keySet(), prefHead);
                        if (col == null) {
                            col = new Column(this, prefHead);
                            cols2.add(col);
                            List<Item> list = new ArrayList<Item>();
                            list.add(item);
                            if (colItems == null)
                                colItems = new HashMap<Column, List<Item>>();
                            colItems.put(col, list);
                        } else {
                            List<Item> list = colItems.get(col);
                            if (list == null) {
                                list = new ArrayList<Item>();
                                colItems.put(col, list);
                            }
                            list.add(item);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < cols2.size(); i++) {
            cols2.get(i).getHead().setIndex(i);
        }
        if (colItems != null) {
            cols = new ArrayList<Column>(
                    new TreeSet<Column>(colItems.keySet()));
        }
        if (cols == null)
            cols = new ArrayList<Column>(1);
        if (cols.isEmpty()) {
            cols.add(new Column(this, ColumnHead.EMPTY));
        }
        buildCells(rowItems, colItems);
    }

    private void buildCells(Map<Row, List<Item>> rowItems,
            Map<Column, List<Item>> colItems) {
        if (rows == null || cols == null)
            return;

        for (Row row : rows) {
            for (Column col : cols) {
                buildCell(row, col, rowItems, colItems);
            }
        }
    }

    private void buildCell(Row row, Column col, Map<Row, List<Item>> rowItems,
            Map<Column, List<Item>> colItems) {
        Cell cell = new Cell(this, row, col);
        row.addCell(cell);
        col.addCell(cell);
        List<Item> rItems = rowItems == null ? null : rowItems.get(row);
        List<Item> cItems = colItems == null ? null : colItems.get(col);
        if (rItems != null && cItems != null) {
            for (Item item : rItems) {
                if (cItems.contains(item)) {
                    cell.addItem(item);
                }
            }
        }
    }

    private Column findColumn(Collection<Column> columns, ColumnHead colHead) {
        for (Column col : columns) {
            if (colHead.equals(col.getHead()))
                return col;
        }
        return null;
    }

    private Row buildRow(IBranchPart rowHead, List<Item> rowItems) {
        Row row = new Row(rowHead, this);
        if (rows == null)
            rows = new ArrayList<Row>();
        rows.add(row);
        for (IBranchPart child : rowHead.getSubBranches()) {
            Item item = new Item(this, child);
            rowItems.add(item);
        }
        return row;
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
//        ILabelPart label = getBranch().getLabel();
//        if (label != null && label.getFigure().isVisible()) {
//            h = label.getFigure().getBounds().bottom() - y;
//        } else {
//            ITopicPart topicPart = getBranch().getTopicPart();
//            if (topicPart != null && topicPart.getFigure().isVisible()) {
//                h = topicPart.getFigure().getBounds().bottom() - y;
//            }
//        }
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

    public int getColumnHeadHeight() {
        if (columnHeadHeight < 0) {
            columnHeadHeight = calcColumnHeadHeight();
        }
        return columnHeadHeight;
    }

    private int calcColumnHeadHeight() {
        int sum = 0;
        for (Column c : getColumns()) {
            sum = Math.max(sum, c.getHead().getPrefSize().height);
        }
        return sum;
    }

    public int getRowHeadWidth() {
        if (rowHeadWidth < 0) {
            rowHeadWidth = calcRowHeadWidth();
        }
        return rowHeadWidth;
    }

    private int calcRowHeadWidth() {
        int sum = 0;
        for (Row row : getRows()) {
            IBranchPart head = row.getHead();
            ITopicPart topicPart = head.getTopicPart();
            if (topicPart != null) {
                sum = Math.max(sum, topicPart.getFigure().getBounds().width);
            }
            IInfoPart infoPart = head.getInfoPart();
            if (infoPart != null) {
                sum = Math.max(sum, infoPart.getFigure().getBounds().width);
            }
        }
        return sum;
    }

    public int getLineWidth() {
        if (lineWidth < 0) {
            IStyleSelector ss = StyleUtils.getStyleSelector(getBranch());
            String decorationId = StyleUtils.getString(getBranch(), ss,
                    Styles.ShapeClass, null);
            lineWidth = StyleUtils.getInteger(getBranch(), ss, Styles.LineWidth,
                    decorationId, 1);
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

    public Row getPreviousRow(Row row) {
        int index = getRowIndex(row);
        if (index > 0)
            return getRows().get(index - 1);
        return null;
    }

    public Row getNextRow(Row row) {
        int index = getRowIndex(row);
        if (index < getRows().size() - 1)
            return getRows().get(index + 1);
        return null;
    }

    public int getRowIndex(Row row) {
        return getRows().indexOf(row);
    }

    public Column getPreviousColumn(Column col) {
        int index = getColumnIndex(col);
        if (index > 0)
            return getColumns().get(index - 1);
        return null;
    }

    public Column getNextColumn(Column col) {
        int index = getColumnIndex(col);
        if (index < getColumns().size() - 1)
            return getColumns().get(index + 1);
        return null;
    }

    public int getColumnIndex(Column col) {
        return getColumns().indexOf(col);
    }

    public Cell findCell(Point point) {
        for (Row row : getRows()) {
            for (Cell cell : row.getCells()) {
                if (cell.getBounds().contains(point))
                    return cell;
            }
        }
        return null;
    }

    public ColumnHead findColumnHead(Point point) {
        if (hasRows()) {
            int y = getTitle().getTopicPart().getFigure().getBounds().bottom()
                    + getLineWidth();
            if (getTitle().getInfoPart() != null) {
                y += getTitle().getInfoPart().getFigure().getBounds().height;
            }
            if (point.y > y && point.y < y + getColumnHeadHeight()
                    + getMajorSpacing()) {
                for (Column col : getColumns()) {
                    int x = col.getLeft();
                    if (point.x > x && point.x < x + col.getWidth()) {
                        return col.getHead();
                    }
                }
            }
        }
        return null;
    }

    public Column findColumn(ColumnHead colHead) {
        for (Column col : getColumns()) {
            if (col.getHead().equals(colHead))
                return col;
        }
        return null;
    }

    public ColumnOrder getPrefColumnOrder() {
        if (prefColumnOrder == null) {
            prefColumnOrder = ColumnOrder
                    .createFromTopic(getTitle().getTopic());
        }
        return prefColumnOrder;
    }
}