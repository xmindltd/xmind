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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.Core;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.event.MouseDragEvent;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.ITool;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.IBranchDoubleClickSupport;
import org.xmind.ui.branch.IBranchMoveSupport;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.internal.spreadsheet.ColumnHeadEditTool;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public class SpreadsheetStructure extends AbstractBranchStructure
        implements IBranchDoubleClickSupport, IBranchMoveSupport {

    private Set<IBranchPart> invalidatingBranches = null;

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof Chart);
    }

    protected Object createStructureData(IBranchPart branch) {
        return new Chart(branch);
    }

    public Chart getChart(IBranchPart branch) {
        return (Chart) super.getStructureData(branch);
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        if (!plusMinus.getFigure().isVisible()) {
            info.put(plusMinus.getFigure(), info.createInitBounds());
            return;
        }

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
        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);
        int halfMinorSpacing1 = minorSpacing / 2;
        int halfMinorSpacing2 = minorSpacing - halfMinorSpacing1;
        int halfMajorSpacing1 = majorSpacing / 2;
        int halfMajorSpacing2 = majorSpacing - halfMajorSpacing1;

        Rectangle area = info.getCheckedClientArea();
        int chartWidth = area.width;
        Chart chart = getChart(branch);
        int lineWidth = chart.getLineWidth();

        int y = area.bottom() + lineWidth;
        if (chart.hasColumns()) {
            y += chart.getColumnHeadHeight() + lineWidth + majorSpacing;
        }

        int x = area.x;

        IInsertion ins = getCurrentInsertion(branch);

        for (int i = 0; i < subBranches.size(); i++) {
            if (ins != null && i == ins.getIndex()) {
                Rectangle insArea = new Rectangle(x, y + halfMinorSpacing1,
                        chartWidth, ins.getSize().height);
                info.add(insArea);
                y += insArea.height + minorSpacing + lineWidth;
            }
            y += halfMinorSpacing1;
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();
            Dimension size = subBranchFigure.getPreferredSize();
            Rectangle r = new Rectangle(
                    x + halfMajorSpacing1 - halfMinorSpacing1, y, size.width,
                    size.height);
            info.put(subBranchFigure, r);
            y += size.height + halfMinorSpacing2 + lineWidth;
        }

        if (ins != null && ins.getIndex() == subBranches.size()) {
            Rectangle insArea = new Rectangle(x, y + halfMinorSpacing1,
                    chartWidth, ins.getSize().height);
            info.add(insArea);
        }
        info.addMargins(lineWidth + halfMajorSpacing2,
                lineWidth + halfMinorSpacing2, lineWidth + halfMajorSpacing1,
                lineWidth + halfMinorSpacing1);
    }

    protected void invalidateBranch(IBranchPart branch) {
        super.invalidateBranch(branch);
        if (!isInvalidatingBranch(branch)) {
            addInvalidatingBranch(branch);
            for (IBranchPart sub : branch.getSubBranches()) {
                Object flag = MindMapUtils.getCache(sub,
                        Spreadsheet.CACHE_INVALIDATING);
                if (!(flag instanceof Boolean)
                        || !((Boolean) flag).booleanValue()) {
                    invalidateChild(sub);
                }
                MindMapUtils.flushCache(sub, Spreadsheet.CACHE_INVALIDATING);
            }
            removeInvalidatingBranch(branch);
        }
    }

    private void invalidateChild(IBranchPart sub) {
        sub.getFigure().invalidate();
    }

    private void removeInvalidatingBranch(IBranchPart branch) {
        invalidatingBranches.remove(branch);
        if (invalidatingBranches.isEmpty())
            invalidatingBranches = null;
    }

    private void addInvalidatingBranch(IBranchPart branch) {
        if (invalidatingBranches == null)
            invalidatingBranches = new HashSet<IBranchPart>();
        invalidatingBranches.add(branch);
    }

    private boolean isInvalidatingBranch(IBranchPart branch) {
        return invalidatingBranches != null
                && invalidatingBranches.contains(branch);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            return getSubTopicPart(branch, 0);
        }
        return super.calcNavigation(branch, navReqType);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
        } else if (GEF.REQ_NAV_UP.equals(navReqType)) {
            int sourceIndex = sourceChild.getBranchIndex();
            if (!sequential && sourceIndex == 0)
                return branch.getTopicPart();
            return getSubTopicPart(branch, sourceIndex - 1);
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        Point pos = key.getCursorPos();
        IFigure branchFigure = branch.getFigure();
        Chart chart = getChart(branch);
        int width = chart.getRowHeadWidth();
        Point topLeft = branch.getTopicPart().getFigure().getBounds()
                .getBottomLeft();
        Rectangle r = new Rectangle(topLeft.x, topLeft.y, width,
                branchFigure.getBounds().bottom() - topLeft.y);
        if (!branch.getSubBranches().isEmpty() && !branch.isFolded()) {
            if (r.contains(pos))
                return 1;
        }
        Point childLoc = key.getFigure().getBounds().getLocation();
        if (r.x < pos.x && r.right() > pos.x) {
            if (childLoc.y > r.y && childLoc.y < r.bottom())
                return 1;
        }
        return -1;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        int newIndex = calcInsIndex(branch, key, true);
        Dimension newSize = calcInsSize(key.getFigure());
        return new Insertion(branch, newIndex, newSize);
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        if (branch.getSubBranches().isEmpty() || branch.isFolded())
            return withDisabled ? 0 : -1;

        Point pos = key.getCursorPos();

        Chart chart = getChart(branch);
        int lineWidth = chart.getLineWidth();
        int majorSpacing = chart.getMajorSpacing();
        int minorSpacing = chart.getMinorSpacing();

        int y = branch.getFigure().getBounds().y + chart.getTitleAreaHeight()
                + lineWidth * 2;
        if (!chart.getColumns().isEmpty()) {
            y += chart.getColumnHeadHeight() + majorSpacing;
        }

        Dimension insSize = calcInsSize(key.getFigure());
        int insHeight = insSize.height + lineWidth + minorSpacing;

        List<IBranchPart> subbranches = branch.getSubBranches();
        int num = subbranches.size();
        int ret = 0;

        for (IBranchPart subBranch : branch.getSubBranches()) {
            IFigure subFigure = subBranch.getFigure();
            int h = subFigure.getSize().height + lineWidth + minorSpacing;
            int hint = y + (h + insHeight) / 2;

            if (pos.y < hint)
                return ret;

            if (withDisabled || subFigure.isEnabled())
                ret++;

            y += h;
        }

        return withDisabled ? num : -1;
    }

    private Dimension calcInsSize(IReferencedFigure child) {
        return child.getSize().scale(0.8);
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.EAST;
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.NONE;
    }

    public boolean handleDoubleClick(IBranchPart branch, Point pos) {
        Chart chart = getChart(branch);
        Cell cell = chart.findCell(pos);
        if (cell != null) {
            handleDoubleClickInCell(cell);
        } else {
            ColumnHead colHead = chart.findColumnHead(pos);
            if (colHead != null) {
                handleDoubleClickInColumnHead(chart, colHead);
            }
        }
        return true;
    }

    private void handleDoubleClickInColumnHead(Chart chart,
            ColumnHead colHead) {
        IBranchPart chartBranch = chart.getTitle();
        EditDomain domain = chartBranch.getSite().getDomain();
        if (domain != null) {
            ITool tool = domain.getTool(Spreadsheet.TOOL_EDIT_COLUMN_HEAD);
            if (tool != null && tool instanceof ColumnHeadEditTool) {
                ColumnHeadEditTool editTool = (ColumnHeadEditTool) tool;
                editTool.setTargetViewer(chartBranch.getSite().getViewer());
                domain.setActiveTool(Spreadsheet.TOOL_EDIT_COLUMN_HEAD);
                if (domain.getActiveTool() == editTool) {
                    domain.handleRequest(new Request(GEF.REQ_EDIT)
                            .setPrimaryTarget(chartBranch)
                            .setViewer(chartBranch.getSite().getViewer())
                            .setParameter(Spreadsheet.PARAM_CHART, chart)
                            .setParameter(Spreadsheet.PARAM_COLUMN_HEAD,
                                    colHead)
                            .setParameter(Spreadsheet.PARAM_COLUMN,
                                    chart.findColumn(colHead)));
                }
            }
        }
    }

    private void handleDoubleClickInCell(Cell cell) {
        IBranchPart rowBranch = cell.getOwnedRow().getHead();
        ITopicPart rowTopic = rowBranch.getTopicPart();
        if (rowTopic == null)
            return;

        EditDomain domain = rowTopic.getSite().getDomain();
        if (domain == null)
            return;

        Request request = new Request(MindMapUI.REQ_CREATE_CHILD);
        request.setDomain(domain);
        request.setViewer(rowTopic.getSite().getViewer());
        request.setPrimaryTarget(rowTopic);
        request.setParameter(MindMapUI.PARAM_WITH_ANIMATION, Boolean.TRUE);
        request.setParameter(MindMapUI.PARAM_PROPERTY_PREFIX + Core.Labels,
                cell.getOwnedColumn().getHead().getLabels());
        domain.handleRequest(request);
    }

    public boolean canMove(IBranchPart branch, MouseDragEvent me) {
        Chart chart = getChart(branch);
        ColumnHead colHead = chart.findColumnHead(me.startingLocation);
        if (colHead != null) {
            MindMapUtils.setCache(branch,
                    Spreadsheet.CACHE_MOVE_SOURCE_COLUMN_HEAD, colHead);
            return true;
        }
        return false;
    }

    public String getMoveTool(IBranchPart branch, MouseDragEvent me) {
        return Spreadsheet.TOOL_MOVE_COLUMN;
    }

    public int calcColumnInsertionIndex(IBranchPart branch, Point pos) {
        Chart chart = getChart(branch);
        IInsertion colIns = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_COLUMN_INSERTION);
        int insWidth = colIns == null ? 0
                : colIns.getSize().width + chart.getMinorSpacing();
        List<Column> cols = chart.getColumns();
        int lineWidth = chart.getLineWidth();
        int x = chart.getTitle().getFigure().getBounds().x + lineWidth
                + chart.getRowHeadWidth() + chart.getMinorSpacing() + lineWidth;
        for (int index = 0; index < cols.size(); index++) {
            Column col = cols.get(index);
            //x += insWidth * index / (cols.size() + 1);
            int colWidth = col.getWidth();
            int w = colWidth + insWidth / (cols.size() + 1);
            if (pos.x < x + w / 2)
                return index;
            x += w + lineWidth;
        }
        return cols.size();
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.SOUTH)
            return 1;
        if (direction == PositionConstants.NORTH)
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        int index = calcInsIndex(branch, key, true);
        if (subBranches.isEmpty()) {
            return calcFirstChildPosition(branch, key);
        }

        IBranchPart sub = index == subBranches.size()
                ? subBranches.get(subBranches.size() - 1)
                : subBranches.get(index);

        int deltaY = (key.getFigure().getSize().height
                + sub.getFigure().getSize().height) / 2;

        return getFigureLocation(sub.getFigure()).getTranslated(
                (key.getInvent().getSize().width
                        - sub.getTopicPart().getFigure().getSize().width) / 2,
                index == subBranches.size()
                        ? deltaY + getMajorSpacing(branch) / 2 : -deltaY);
    }

    protected Point calcMovePosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        List<Integer> disables = getDisableBranches(branch);

        int index = calcInsIndex(branch, key, true);
        int oldIndex = getOldIndex(branch, child);
        if (disables != null) {
            if (disables.contains(index - 1)) {
                index--;
                oldIndex = index;
            } else if (disables.contains(index)) {
                oldIndex = index;
            }
        }
        Dimension inventSize = key.getInvent().getSize();

        if (index == oldIndex)
            return getFigureLocation(subBranches.get(index).getFigure())
                    .getTranslated((inventSize.width - subBranches.get(index)
                            .getTopicPart().getFigure().getSize().width) / 2,
                            0);

        return calcInsertPosition(branch, child, key);
    }
}
