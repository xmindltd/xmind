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
import java.util.HashSet;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.ICreatableBranchStructureExtension;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.IMovableBranchStructureExtension;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public class RowStructure extends AbstractBranchStructure implements
        ICreatableBranchStructureExtension, IMovableBranchStructureExtension {

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof Row);
    }

    protected Object createStructureData(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            Chart chart = null;
            IStructure sa = parent.getBranchPolicy().getStructure(parent);
            if (sa instanceof SpreadsheetStructure) {
                chart = ((SpreadsheetStructure) sa).getChart(parent);
            }
            if (chart == null) {
                chart = new Chart(parent);
            }
            return chart.getRow(branch.getBranchIndex());
        }

        //TODO make row data available even without parent branch
        Chart chart = new Chart(null);
        Row row = new Row(branch, chart);
        Column col = new Column(chart, ColumnHead.EMPTY);
        chart.setContent(row, col);
        chart.setLineWidth(1);
        Cell cell = new Cell(chart, row, col);
        row.addCell(cell);
        for (IBranchPart sub : branch.getSubBranches()) {
            cell.addItem(new Item(chart, sub));
        }
        return row;
    }

    public Row getRow(IBranchPart branch) {
        return (Row) super.getStructureData(branch);
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        int y = ref.y;

        Rectangle topicBounds = info.getCheckedClientArea();
        int x = topicBounds.right();

        IFigure pmFigure = plusMinus.getFigure();
        Dimension size = pmFigure.getPreferredSize();
        Rectangle r = new Rectangle(x, y - size.height / 2, size.width,
                size.height);
        info.put(pmFigure, r);
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        // overrides fillSubBranches() instead as below:
    }

    protected void fillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
//        super.fillSubBranches(branch, subBranches, info);
        Rectangle area = info.getCheckedClientArea();

        Row row = getRow(branch);
        Chart chart = row.getOwnedChart();
        int lineWidth = chart.getLineWidth();
        int cellSpacing = chart.getMinorSpacing();
        int itemSpacing = row.getMinorSpacing();

        IInsertion insertion = getCurrentInsertion(branch);
        ColumnHead insHead = (ColumnHead) MindMapUtils.getCache(branch,
                Spreadsheet.KEY_INSERTION_COLUMN_HEAD);

        int startY = info.getReference().y - row.getPrefCellHeight() / 2;
        int x = area.x + chart.getRowHeadWidth() + cellSpacing + lineWidth;
        List<Column> columns = chart.getColumns();
        IInsertion colIns = (IInsertion) MindMapUtils.getCache(chart.getTitle(),
                Spreadsheet.CACHE_COLUMN_INSERTION);
        for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
            if (colIns != null && colIns.getIndex() == colIndex) {
                x += colIns.getSize().width + chart.getMinorSpacing()
                        + lineWidth;
            }

            Column col = columns.get(colIndex);
            int y = startY;
            boolean insertionInColumn = insertion != null
                    && col.getHead().equals(insHead);
            Cell cell = row.findCellByColumn(col);
            if (cell != null) {
                info.add(new Rectangle(x, y, col.getPrefCellWidth(),
                        cell.getContentHeight()));
                List<Item> items = cell.getItems();
                int num = items.size();
                for (int i = 0; i < num; i++) {
                    Item item = items.get(i);
                    if (insertionInColumn && insertion.getIndex() == i) {
                        Rectangle r = insertion.createRectangle(x, y);
                        info.add(r);
                        y += r.height + itemSpacing;
                    }
                    IBranchPart child = item.getBranch();
                    IFigure childFigure = child.getFigure();
                    Dimension size = childFigure.getPreferredSize();

                    int bw = 0;
                    int bh = 0;
                    List<IBoundaryPart> boundaries = branch.getBoundaries();
                    if (!boundaries.isEmpty()) {
                        for (IBoundaryPart boundary : boundaries) {
                            List<IBranchPart> enclosingBranches = boundary
                                    .getEnclosingBranches();
                            if (enclosingBranches.contains(child)) {
                                bw = boundary.getFigure().getInsets().left - 1;
                                if (boundary.getTitle() != null && boundary
                                        .getTitle().getFigure() != null)
                                    bw += 5;
                            }

                            if ((!enclosingBranches.isEmpty())
                                    && child.equals(enclosingBranches.get(0))) {
                                bh = boundary.getFigure().getInsets().top;
                                if (boundary.getTitle() != null && boundary
                                        .getTitle().getFigure() != null) {
                                    Dimension s = boundary.getTitle()
                                            .getFigure().getPreferredSize();
                                    bh = Math.max(bh, s.height);
                                }
//                                y += bh;
                                bh = 0;
                            }
                            if ((!enclosingBranches.isEmpty())
                                    && child.equals(enclosingBranches.get(
                                            enclosingBranches.size() - 1))) {
                                bh = boundary.getFigure().getInsets().bottom;
                            }
                        }

                    }

                    int headWidth = item.getPrefColumnHead()
                            .getPrefSize().width;
                    Rectangle childBounds = new Rectangle(x + bw, y,
                            (size.width > headWidth ? size.width : headWidth),
                            size.height);
                    info.put(childFigure, childBounds);
                    y += size.height + itemSpacing + bh + itemSpacing;
                }
                if (insertionInColumn && insertion.getIndex() == num) {
                    info.add(insertion.createRectangle(x, y));
                }
            } else if (insertionInColumn) {
                info.add(insertion.createRectangle(x, y));
            }
            x += col.getPrefCellWidth() + cellSpacing + lineWidth;
        }
        if (colIns != null && colIns.getIndex() == columns.size()) {
            info.add(new Rectangle(x, startY, colIns.getSize().width, 1));
        }
    }

    public void fillLayoutData(IBranchPart branch, ReferencedLayoutData data) {
        super.fillLayoutData(branch, data);
        MindMapUtils.flushCache(branch, Spreadsheet.CACHE_INVALIDATING);
    }

    protected void invalidateBranch(IBranchPart branch) {
        super.invalidateBranch(branch);
        MindMapUtils.setCache(branch, Spreadsheet.CACHE_INVALIDATING,
                Boolean.TRUE);
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.NONE;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.EAST;
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            Row row = getRow(branch);
            if (!row.getCells().isEmpty()) {
                Cell cell = row.getCells().get(0);
                if (!cell.getItems().isEmpty()) {
                    Item item = cell.getItems().get(0);
                    return item.getBranch().getTopicPart();
                }
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            Row row = getRow(branch);
            Cell cell = row.findCellByItem(sourceChild);
            if (cell != null) {
                Cell prev = row.getPreviousCell(cell);
                if (prev == null) {
                    if (!sequential)
                        return branch.getTopicPart();
                }
            }
        } else if (GEF.REQ_NAV_UP.equals(navReqType)) {
            Row row = getRow(branch);
            Item item = row.findItem(sourceChild);
            if (item != null) {
                Item prev = item.getPreviousItem();
                if (prev != null)
                    return prev.getBranch().getTopicPart();
            }
        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            Row row = getRow(branch);
            Item item = row.findItem(sourceChild);
            if (item != null) {
                Item next = item.getNextItem();
                if (next != null)
                    return next.getBranch().getTopicPart();
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public void calcSequentialNavigation(IBranchPart branch,
            IBranchPart startChild, IBranchPart endChild,
            List<IBranchPart> results) {
        Row row = getRow(branch);
        Item startItem = row.findItem(startChild);
        if (startItem != null) {
            Cell cell = startItem.getOwnedCell();
            Item endItem = row.findItem(endChild);
            if (endItem != null && cell == endItem.getOwnedCell()) {
                int startIndex = cell.getItemIndex(startItem);
                int endIndex = cell.getItemIndex(endItem);
                if (startIndex >= 0 && endIndex >= 0) {
                    boolean decreasing = endIndex < startIndex;
                    for (int i = startIndex; decreasing ? i >= endIndex
                            : i <= endIndex;) {
                        Item item = cell.getItems().get(i);
                        results.add(item.getBranch());
                        if (decreasing)
                            i--;
                        else
                            i++;
                    }
                }
            }
        }
        super.calcSequentialNavigation(branch, startChild, endChild, results);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        Point pos = key.getCursorPos();
        Row row = getRow(branch);
        Chart chart = row.getOwnedChart();
        int rowY = row.getTop();
        int rowHeight = row.getHeight();
        if (pos.y > rowY && pos.y < rowY + rowHeight) {
            if (!chart.hasColumns()) {
                int x = chart.getTitle().getFigure().getBounds().x;
                int w = chart.getTitle().getFigure().getBounds().width;
                int childX = key.getFigure().getBounds().x;
                if (childX > x && childX < x + w)
                    return 0;
            }
            Cell cell = row.findCell(pos);
            if (cell != null) {
                if (cell.getItems().isEmpty())
                    return 0;
                int colX = cell.getOwnedColumn().getLeft();
                int offset = pos.x - colX;
                int index = 0;
                int last = cell.getItems().size() - 1;
                for (Item item : cell.getItems()) {
                    Rectangle itemBounds = item.getBranch().getFigure()
                            .getBounds();
                    if (index == 0) {
                        if (pos.y < itemBounds.y)
                            return 0;
                    }
                    if (index == last) {
                        if (pos.y > itemBounds.bottom())
                            return 0;
                    }
                    if (pos.x < itemBounds.x)
                        return 0;

                    Rectangle itemTopicBounds = item.getBranch().getTopicPart()
                            .getFigure().getBounds();
                    if (pos.x < itemTopicBounds.right()) {
                        return offset;
                    }
                    index++;
                }
                return offset;
            }
        }
        return -1;
    }

    public int calcChildIndex(IBranchPart branch, ParentSearchKey key) {
        Row row = getRow(branch);
        Chart chart = row.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell cell = row.findCell(pos);
        if (cell == null || cell.getItems().isEmpty())
            return -1;

        Dimension insSize = getInsSize(key.getFigure());
        int y = row.getTop() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = row.getMinorSpacing();
        int disabled = 0;
        for (Item item : cell.getItems()) {
            IBranchPart itemBranch = item.getBranch();
            Dimension itemSize = itemBranch.getFigure().getSize();
            int hint = y + (itemSize.height + insHeight) / 2;
            if (pos.y < hint) {
                return itemBranch.getBranchIndex() - disabled;
            }
            y += itemSize.height + spacing;
            if (!itemBranch.getFigure().isEnabled())
                disabled++;
        }
        return -1;
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        Row row = getRow(branch);
        Chart chart = row.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell cell = row.findCell(pos);
        if (cell == null || cell.getItems().isEmpty())
            return -1;

        Dimension insSize = getInsSize(key.getFigure());
        int y = row.getTop() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = row.getMinorSpacing();
        int disabled = 0;
        for (Item item : cell.getItems()) {
            IBranchPart itemBranch = item.getBranch();
            Dimension itemSize = itemBranch.getFigure().getSize();
            int hint = y + (itemSize.height + insHeight) / 2;
            if (pos.y < hint) {
                return getOldIndex(branch, itemBranch) - disabled;
            }
            y += itemSize.height + spacing;
            if (!itemBranch.getFigure().isEnabled() && !withDisabled)
                disabled++;
        }

        return -1;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return calcInsertion(branch, key, true);
    }

    private Insertion calcInsertion(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        Row row = getRow(branch);
        Chart chart = row.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell cell = row.findCell(pos);
        if (cell == null)
            return null;

        Dimension insSize = getInsSize(key.getFigure());
        if (cell.getItems().isEmpty()) {
            return new CellInsertion(branch, -1, insSize,
                    cell.getOwnedColumn().getHead());
        }
        int y = row.getTop() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = row.getMinorSpacing();//getMinorSpacing(branch);
        int index = 0;
        for (Item item : cell.getItems()) {
            IBranchPart itemBranch = item.getBranch();
            Dimension itemSize = itemBranch.getFigure().getSize();
            int hint = y + (itemSize.height + insHeight) / 2;
            if (pos.y < hint) {
                return new CellInsertion(branch, index, insSize,
                        cell.getOwnedColumn().getHead());
            }
            y += itemSize.height + spacing;
            if (withDisabled || itemBranch.getFigure().isEnabled())
                index++;
        }
        return new CellInsertion(branch, withDisabled ? index : -1, insSize,
                cell.getOwnedColumn().getHead());
    }

    private Dimension getInsSize(IReferencedFigure child) {
        return child.getSize();
    }

    public void decorateMoveInRequest(IBranchPart targetParent,
            ParentSearchKey childKey, IBranchPart sourceParent,
            Request request) {
        ColumnHead colHead = (ColumnHead) MindMapUtils.getCache(targetParent,
                Spreadsheet.KEY_INSERTION_COLUMN_HEAD);
        if (colHead != null) {
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    new HashSet<String>(colHead.getLabels()));
        } else {
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    new HashSet<String>());
        }
    }

    public void decorateMoveOutRequest(IBranchPart sourceParent,
            ParentSearchKey childKey, IBranchPart targetParent,
            Request request) {
        request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                null);
    }

    public void decorateCreateRequest(IBranchPart branch,
            IBranchPart sourceChild, Request request) {
        Row row = getRow(branch);
        Cell cell = row.findCellByItem(sourceChild);
        if (cell != null) {
            ColumnHead colHead = cell.getOwnedColumn().getHead();
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    new HashSet<String>(colHead.getLabels()));
        }
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.SOUTH) {
            Row row = getRow(branch);
            Item item = row.findItem(child);
            if (item != null) {
                Item next = item.getNextItem();
                if (next != null)
                    return next.getBranch().getBranchIndex()
                            - child.getBranchIndex();
            }
        } else if (direction == PositionConstants.NORTH) {
            Row row = getRow(branch);
            Item item = row.findItem(child);
            if (item != null) {
                Item next = item.getPreviousItem();
                if (next != null)
                    return next.getBranch().getBranchIndex()
                            - child.getBranchIndex();
            }
        }
        return super.getQuickMoveOffset(branch, child, direction);
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        Row row = getRow(branch);
        Cell cell = row.findCell(key.getCursorPos());
        if (cell == null) {
            return calcFirstChildPosition(branch, key);
        }

        List<Item> items = cell.getItems();

        Dimension inventSize = key.getInvent().getSize();
        Dimension insSize = key.getFigure().getSize();

        if (items.isEmpty())
            return new Point(
                    cell.getX() + inventSize.width / 2
                            + row.getOwnedChart().getLineWidth(),
                    getFigureLocation(branch.getFigure()).y
                            - (cell.getHeight() - insSize.height > 0
                                    ? (cell.getHeight() - insSize.height) / 2
                                    : 0));

        IInsertion insertion = calcInsertion(branch, key);
        int itemIndex = insertion == null ? -1 : insertion.getIndex();

        IBranchPart sub = itemIndex == items.size()
                ? items.get(items.size() - 1).getBranch()
                : items.get(itemIndex).getBranch();

        int deltaY = (insSize.height + sub.getFigure().getSize().height) / 2;

        return getFigureLocation(sub.getFigure()).getTranslated(
                (inventSize.width
                        - sub.getTopicPart().getFigure().getSize().width) / 2,
                itemIndex == items.size() ? deltaY : -deltaY);
    }

    protected Point calcMovePosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<Integer> disables = getDisableBranches(branch);

        int index = calcInsIndex(branch, key, true);
        int oldIndex = getOldIndex(branch, child);
        IInsertion insertion = calcInsertion(branch, key);
        int itemIndex = insertion == null ? -1 : insertion.getIndex();
        if (disables != null) {
            if (disables.contains(index)) {
                oldIndex = index;
            } else if (disables.contains(index - 1) && itemIndex != 0) {
                index--;
                oldIndex = index;
                itemIndex--;
            }
        }

        Row row = getRow(branch);
        Cell cell = row.findCell(key.getCursorPos());

        if (cell != null) {
            List<Item> items = cell.getItems();

            Dimension inventSize = key.getInvent().getSize();
            Dimension insSize = key.getFigure().getSize();

            if (items.isEmpty())
                return new Point(
                        cell.getX() + inventSize.width / 2
                                + row.getOwnedChart().getLineWidth(),
                        getFigureLocation(branch.getFigure()).y
                                - (cell.getHeight() - insSize.height > 0
                                        ? (cell.getHeight() - insSize.height)
                                                / 2
                                        : 0));

            if (index == oldIndex
                    || (index == -1 && !items.get(items.size() - 1).getBranch()
                            .getFigure().isEnabled())) {
                IBranchPart sub = items.get(itemIndex == items.size()
                        ? items.size() - 1 : itemIndex).getBranch();
                if (cell.equals(row.findCellByItem(sub)))
                    return getFigureLocation(sub.getFigure())
                            .getTranslated(
                                    (inventSize.width - sub.getTopicPart()
                                            .getFigure().getSize().width) / 2,
                                    0);
            }
        }

        return calcInsertPosition(branch, child, key);
    }

    public boolean isBranchMoved(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        return true;
    }

    protected int getOldIndex(IBranchPart branch, IBranchPart child) {
        Row row = getRow(branch);

        if (branch.equals(child.getParentBranch())) {
            Cell cell = row.findCellByItem(child);
            int count = 0;
            for (int i = 0; i < row.getCells().indexOf(cell); i++) {
                count += row.getCells().get(i).getItems().size();
            }
            Item item = row.findItem(child);
            return count + cell.getItems().indexOf(item);
        } else {
            int index = 0;
            for (Cell cell : row.getCells()) {
                for (Item item : cell.getItems()) {
                    if (!item.getBranch().getFigure().isEnabled())
                        return index;
                    index++;
                }
            }
        }

        return -1;
    }

    protected List<Integer> getDisableBranches(IBranchPart branch) {
        List<Integer> disables = null;

        Row row = getRow(branch);
        int index = 0;
        for (Cell cell : row.getCells()) {
            for (Item item : cell.getItems()) {
                if (!item.getBranch().getFigure().isEnabled()) {
                    if (disables == null)
                        disables = new ArrayList<Integer>();
                    disables.add(index);
                }
                index++;
            }
        }

        return disables;
    }
}
