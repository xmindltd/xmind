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
import org.xmind.core.ITopic;
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

public class ColumnStructure extends AbstractBranchStructure implements
        ICreatableBranchStructureExtension, IMovableBranchStructureExtension {

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof Column2);
    }

    protected Object createStructureData(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            Chart2 chart = null;
            IStructure sa = parent.getBranchPolicy().getStructure(parent);
            if (sa instanceof SpreadsheetColumnStructure) {
                chart = ((SpreadsheetColumnStructure) sa).getChart(parent);
            }
            if (chart == null) {
                chart = new Chart2(parent);
            }
            return chart.getColumn(branch.getBranchIndex());
        }

        Chart2 chart = new Chart2(null);
        Column2 col = new Column2(branch, chart);
        Row2 row = new Row2(chart, RowHead.EMPTY);
        chart.setContent(col, row);
        chart.setLineWidth(1);
        Cell2 cell = new Cell2(chart, col, row);
        col.addCell(cell);
        for (IBranchPart sub : branch.getSubBranches()) {
            cell.addItem(new Item2(chart, sub));
        }
        return col;
    }

    public Column2 getColumn(IBranchPart branch) {
        return (Column2) super.getStructureData(branch);
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        int x = ref.x;

        Rectangle topicBounds = info.getCheckedClientArea();
        int y = topicBounds.bottom();

        IFigure pmFigure = plusMinus.getFigure();
        Dimension size = pmFigure.getPreferredSize();
        Rectangle r = new Rectangle(x - size.width / 2, y, size.width,
                size.height);
        info.put(pmFigure, r);
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        // overrides fillSubBranches() instead as below:
    }

    protected void fillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        Rectangle area = info.getCheckedClientArea();

        Column2 col = getColumn(branch);
        Chart2 chart = col.getOwnedChart();
        int lineWidth = chart.getLineWidth();
        int cellSpacing = chart.getMinorSpacing();
        int itemSpacing = col.getMinorSpacing();

        IInsertion insertion = getCurrentInsertion(branch);
        RowHead insHead = (RowHead) MindMapUtils.getCache(branch,
                Spreadsheet.KEY_INSERTION_ROW_HEAD);

        int startX = info.getReference().x - col.getPrefCellWidth() / 2;
        int y = area.y + chart.getColHeadHeight() + cellSpacing / 2 + lineWidth;
        List<Row2> rows = chart.getRows();
        IInsertion rowIns = (IInsertion) MindMapUtils.getCache(chart.getTitle(),
                Spreadsheet.CACHE_ROW_INSERTION);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            if (rowIns != null && rowIns.getIndex() == rowIndex) {
                y += rowIns.getSize().height + chart.getMinorSpacing()
                        + lineWidth;
            }

            Row2 row = rows.get(rowIndex);
            int x = startX;
            boolean insertionInRow = insertion != null
                    && row.getHead().equals(insHead);
            Cell2 cell = col.findCellByRow(row);
            if (cell != null) {
                info.add(new Rectangle(x, y, col.getPrefCellWidth(),
                        cell.getContentHeight()));
                List<Item2> items = cell.getItems();
                int num = items.size();
                int itemY = y;
                for (int i = 0; i < num; i++) {
                    Item2 item = items.get(i);
                    if (insertionInRow && insertion.getIndex() == i) {
                        Rectangle r = insertion.createRectangle(x, y);
                        info.add(r);
                        itemY += r.height + itemSpacing;
                    }
                    IBranchPart child = item.getBranch();
                    IFigure childFigure = child.getFigure();
                    Dimension size = childFigure.getPreferredSize();

                    int bh = 0;
                    if (!branch.getBoundaries().isEmpty()) {
                        for (IBoundaryPart boundary : branch.getBoundaries()) {
                            List<IBranchPart> enclosingBranches = boundary
                                    .getEnclosingBranches();
                            if ((!enclosingBranches.isEmpty())
                                    && child.equals(enclosingBranches.get(0))) {
                                bh = boundary.getFigure().getInsets().top;
                                if (boundary.getTitle() != null && boundary
                                        .getTitle().getFigure() != null) {
                                    Dimension s = boundary.getTitle()
                                            .getFigure().getPreferredSize();
                                    bh = Math.max(bh, s.height);
                                }
                                List<ITopic> topics = boundary.getBoundary()
                                        .getEnclosingTopics();
                                if (topics.size() > 1) {
                                    itemY += bh;
                                }
                                bh = 0;
                            }
                            if ((!enclosingBranches.isEmpty())
                                    && child.equals(enclosingBranches.get(
                                            enclosingBranches.size() - 1))) {
                                bh = boundary.getFigure().getInsets().bottom;
                            }
                        }
                    }

                    Rectangle childBounds = new Rectangle(x, itemY, size.width,
                            size.height + 10);
                    info.put(childFigure, childBounds);
                    itemY += size.height + itemSpacing + bh;
                }
                if (insertionInRow && insertion.getIndex() == num) {
                    info.add(insertion.createRectangle(x, y));
                }
            } else if (insertionInRow) {
                info.add(insertion.createRectangle(x, y));
            }
            y += row.getPrefCellHeight() + cellSpacing + lineWidth;
        }
        if (rowIns != null && rowIns.getIndex() == rows.size()) {
            info.add(new Rectangle(startX, y, rowIns.getSize().width, 1));
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
            Column2 row = getColumn(branch);
            if (!row.getCells().isEmpty()) {
                Cell2 cell = row.getCells().get(0);
                if (!cell.getItems().isEmpty()) {
                    Item2 item = cell.getItems().get(0);
                    return item.getBranch().getTopicPart();
                }
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            Column2 row = getColumn(branch);
            Cell2 cell = row.findCellByItem(sourceChild);
            if (cell != null) {
                Cell2 prev = row.getPreviousCell(cell);
                if (prev == null) {
                    if (!sequential)
                        return branch.getTopicPart();
                }
            }
        } else if (GEF.REQ_NAV_UP.equals(navReqType)) {
            Column2 row = getColumn(branch);
            Item2 item = row.findItem(sourceChild);
            if (item != null) {
                Item2 prev = item.getPreviousItem();
                if (prev != null)
                    return prev.getBranch().getTopicPart();
            }
        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            Column2 row = getColumn(branch);
            Item2 item = row.findItem(sourceChild);
            if (item != null) {
                Item2 next = item.getNextItem();
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
        Column2 row = getColumn(branch);
        Item2 startItem = row.findItem(startChild);
        if (startItem != null) {
            Cell2 cell = startItem.getOwnedCell();
            Item2 endItem = row.findItem(endChild);
            if (endItem != null && cell == endItem.getOwnedCell()) {
                int startIndex = cell.getItemIndex(startItem);
                int endIndex = cell.getItemIndex(endItem);
                if (startIndex >= 0 && endIndex >= 0) {
                    boolean decreasing = endIndex < startIndex;
                    for (int i = startIndex; decreasing ? i >= endIndex
                            : i <= endIndex;) {
                        Item2 item = cell.getItems().get(i);
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
        Column2 col = getColumn(branch);
        Chart2 chart = col.getOwnedChart();
        int rowX = col.getLeft();
        int rowWidth = col.getWidth();
        if (pos.x > rowX && pos.x < rowX + rowWidth) {
            if (!chart.hasRows()) {
                int y = chart.getTitle().getFigure().getBounds().y;
                int h = chart.getTitle().getFigure().getBounds().height;
                int childY = key.getFigure().getBounds().y;
                if (childY > y && childY < y + h)
                    return 0;
            }
            Cell2 cell = col.findCell(pos);
            if (cell != null) {
                if (cell.getItems().isEmpty())
                    return 0;
                int colY = cell.getOwnedRow().getTop();
                int offset = pos.y - colY;
                int index = 0;
                int last = cell.getItems().size() - 1;
                for (Item2 item : cell.getItems()) {
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
        Column2 col = getColumn(branch);
        Chart2 chart = col.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell2 cell = col.findCell(pos);
        if (cell == null || cell.getItems().isEmpty())
            return -1;

        Dimension insSize = getInsSize(key.getFigure());
        int y = cell.getY() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = col.getMinorSpacing();
        int disabled = 0;
        for (Item2 item : cell.getItems()) {
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
        Column2 column = getColumn(branch);
        Chart2 chart = column.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell2 cell = column.findCell(pos);
        if (cell == null || cell.getItems().isEmpty())
            return -1;

        Dimension insSize = getInsSize(key.getFigure());
        int y = cell.getY() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = column.getMinorSpacing();
        int disabled = 0;
        for (Item2 item : cell.getItems()) {
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
        Column2 col = getColumn(branch);
        Chart2 chart = col.getOwnedChart();
        Point pos = key.getCursorPos();
        Cell2 cell = col.findCell(pos);
        if (cell == null)
            return null;

        Dimension insSize = getInsSize(key.getFigure());
        if (cell.getItems().isEmpty()) {
            return new CellInsertion2(branch, -1, insSize,
                    cell.getOwnedRow().getHead());
        }
        int y = cell.getY() + chart.getMinorSpacing() / 2;
        int insHeight = insSize.height;
        int spacing = col.getMinorSpacing();//getMinorSpacing(branch);
        int index = 0;
        for (Item2 item : cell.getItems()) {
            IBranchPart itemBranch = item.getBranch();
            Dimension itemSize = itemBranch.getFigure().getSize();
            int hint = y + (itemSize.height + insHeight) / 2;
            if (pos.y < hint) {
                return new CellInsertion2(branch, index, insSize,
                        cell.getOwnedRow().getHead());
            }
            y += itemSize.height + spacing;
            if (withDisabled || itemBranch.getFigure().isEnabled())
                index++;
        }
        return new CellInsertion2(branch, withDisabled ? index : -1, insSize,
                cell.getOwnedRow().getHead());
    }

    private Dimension getInsSize(IReferencedFigure child) {
        return child.getSize();
    }

    public void decorateMoveInRequest(IBranchPart targetParent,
            ParentSearchKey childKey, IBranchPart sourceParent,
            Request request) {
        RowHead rowHead = (RowHead) MindMapUtils.getCache(targetParent,
                Spreadsheet.KEY_INSERTION_ROW_HEAD);
        if (rowHead != null) {
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    new HashSet<String>(rowHead.getLabels()));
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
        Column2 col = getColumn(branch);
        Cell2 cell = col.findCellByItem(sourceChild);
        if (cell != null) {
            RowHead rowHead = cell.getOwnedRow().getHead();
            request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                    new HashSet<String>(rowHead.getLabels()));
        }
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.EAST) {
            Column2 col = getColumn(branch);
            Item2 item = col.findItem(child);
            if (item != null) {
                Item2 next = item.getNextItem();
                if (next != null)
                    return next.getBranch().getBranchIndex()
                            - child.getBranchIndex();
            }
        } else if (direction == PositionConstants.WEST) {
            Column2 col = getColumn(branch);
            Item2 item = col.findItem(child);
            if (item != null) {
                Item2 next = item.getPreviousItem();
                if (next != null)
                    return next.getBranch().getBranchIndex()
                            - child.getBranchIndex();
            }
        }
        return super.getQuickMoveOffset(branch, child, direction);
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        Column2 col = getColumn(branch);
        Cell2 cell = col.findCell(key.getCursorPos());
        if (cell == null) {
            return calcFirstChildPosition(branch, key);
        }

        List<Item2> items = cell.getItems();

        Dimension inventSize = key.getInvent().getSize();
        Dimension insSize = key.getFigure().getSize();

        if (items.isEmpty())
            return new Point(getFigureLocation(branch.getFigure()).x,
                    cell.getY() + (insSize.height < cell.getHeight()
                            ? insSize.height / 2 : cell.getHeight() / 2));

        IInsertion insertion = calcInsertion(branch, key);
        int index = insertion == null ? -1 : insertion.getIndex();

        IBranchPart sub = index == items.size()
                ? items.get(items.size() - 1).getBranch()
                : items.get(index).getBranch();

        int deltaY = (insSize.height + sub.getFigure().getSize().height) / 2;

        return getFigureLocation(sub.getFigure()).getTranslated(
                (inventSize.width
                        - sub.getTopicPart().getFigure().getSize().width) / 2,
                index == items.size() ? deltaY : -deltaY);
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

        Column2 col = getColumn(branch);
        Cell2 cell = col.findCell(key.getCursorPos());
        List<Item2> items = cell.getItems();

        Dimension inventSize = key.getInvent().getSize();
        Dimension insSize = key.getFigure().getSize();

        if (items.isEmpty())
            return new Point(getFigureLocation(branch.getFigure()).x,
                    cell.getY() + (insSize.height < cell.getHeight()
                            ? insSize.height / 2 : cell.getHeight() / 2));

        if (index == oldIndex || (index == -1 && !items.get(items.size() - 1)
                .getBranch().getFigure().isEnabled())) {
            IBranchPart sub = items.get(
                    itemIndex == items.size() ? items.size() - 1 : itemIndex)
                    .getBranch();
            if (cell.equals(col.findCellByItem(sub)))
                return getFigureLocation(sub.getFigure())
                        .getTranslated((inventSize.width - sub.getTopicPart()
                                .getFigure().getSize().width) / 2, 0);
        }

        return calcInsertPosition(branch, child, key);
    }

    public boolean isBranchMoved(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        return true;
    }

    protected int getOldIndex(IBranchPart branch, IBranchPart child) {
        Column2 col = getColumn(branch);

        if (branch.equals(child.getParentBranch())) {
            Cell2 cell = col.findCellByItem(child);
            int count = 0;
            for (int i = 0; i < col.getCells().indexOf(cell); i++)
                count += col.getCells().get(i).getItems().size();
            Item2 item = col.findItem(child);
            return count + cell.getItems().indexOf(item);
        } else {
            int index = 0;
            for (Cell2 cell : col.getCells()) {
                for (Item2 item : cell.getItems()) {
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

        Column2 col = getColumn(branch);
        int index = 0;
        for (Cell2 cell : col.getCells()) {
            for (Item2 item : cell.getItems()) {
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
