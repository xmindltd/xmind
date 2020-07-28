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
import org.xmind.ui.internal.spreadsheet.RowHeadEditTool;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public class SpreadsheetColumnStructure extends AbstractBranchStructure
        implements IBranchDoubleClickSupport, IBranchMoveSupport {

    private Set<IBranchPart> invalidatingBranches = null;

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof Chart2);
    }

    protected Object createStructureData(IBranchPart branch) {
        return new Chart2(branch);
    }

    public Chart2 getChart(IBranchPart branch) {
        return (Chart2) super.getStructureData(branch);
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
        int chartHeight = area.height;
        Chart2 chart = getChart(branch);
        int lineWidth = chart.getLineWidth();

        int x = area.x + lineWidth;
        if (chart.hasRows()) {
            x += chart.getRowHeadWidth() + lineWidth + minorSpacing;
        } else {
            x += lineWidth + majorSpacing;
        }

        int y = area.bottom() + halfMinorSpacing1 + lineWidth;

        IInsertion ins = getCurrentInsertion(branch);

        for (int i = 0; i < subBranches.size(); i++) {
            if (ins != null && i == ins.getIndex()) {
                Rectangle insArea = new Rectangle(x + halfMinorSpacing1, y,
                        ins.getSize().width, chartHeight);
                info.add(insArea);
                x += insArea.width + minorSpacing + lineWidth;
            }
            x += halfMinorSpacing1;
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();
            Dimension size = subBranchFigure.getPreferredSize();
            Rectangle r = new Rectangle(x + halfMajorSpacing1, y, size.width,
                    size.height);
            info.put(subBranchFigure, r);
            x += size.width + halfMinorSpacing2;
        }

        if (ins != null && ins.getIndex() == subBranches.size()) {
            Rectangle insArea = new Rectangle(x, y, ins.getSize().width,
                    chartHeight);
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
        Chart2 chart = getChart(branch);
        int height = chart.getColHeadHeight();
        Point topLeft = branch.getTopicPart().getFigure().getBounds()
                .getBottomLeft();
        Rectangle r = new Rectangle(topLeft.x, topLeft.y,
                branchFigure.getBounds().right() - topLeft.x, height);
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

        Chart2 chart = getChart(branch);
        int lineWidth = chart.getLineWidth();
        int majorSpacing = chart.getMajorSpacing();
        int minorSpacing = chart.getMinorSpacing();

        int x = branch.getFigure().getBounds().x + lineWidth * 2;
        if (!chart.getRows().isEmpty()) {
            x += chart.getRowHeadWidth() + majorSpacing;
        }

        Dimension insSize = calcInsSize(key.getFigure());
        int insWidth = insSize.width + lineWidth + minorSpacing;

        List<IBranchPart> subbranches = branch.getSubBranches();
        int num = subbranches.size();
        int ret = 0;

        for (IBranchPart subBranch : branch.getSubBranches()) {
            IFigure subFigure = subBranch.getFigure();
            int w = subFigure.getSize().width + lineWidth + minorSpacing;
            int hint = x + (w + insWidth) / 2;

            if (pos.x < hint)
                return ret;

            if (withDisabled || subFigure.isEnabled())
                ret++;

            x += w;
        }

        return withDisabled ? num : -1;
    }

    private Dimension calcInsSize(IReferencedFigure child) {
        return child.getSize().scale(0.8);
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.EAST;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.SOUTH;
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.NONE;
    }

    public boolean handleDoubleClick(IBranchPart branch, Point pos) {
        Chart2 chart = getChart(branch);
        Cell2 cell = chart.findCell(pos);
        if (cell != null) {
            handleDoubleClickInCell(cell);
        } else {
            RowHead rowHead = chart.findRowHead(pos);
            if (rowHead != null) {
                handleDoubleClickInColumnHead(chart, rowHead);
            }
        }
        return true;
    }

    private void handleDoubleClickInColumnHead(Chart2 chart, RowHead rowHead) {
        IBranchPart chartBranch = chart.getTitle();
        EditDomain domain = chartBranch.getSite().getDomain();
        if (domain != null) {
            ITool tool = domain.getTool(Spreadsheet.TOOL_EDIT_ROW_HEAD);
            if (tool != null && tool instanceof RowHeadEditTool) {
                RowHeadEditTool editTool = (RowHeadEditTool) tool;
                editTool.setTargetViewer(chartBranch.getSite().getViewer());
                domain.setActiveTool(Spreadsheet.TOOL_EDIT_ROW_HEAD);
                if (domain.getActiveTool() == editTool) {
                    domain.handleRequest(
                            new Request(GEF.REQ_EDIT)
                                    .setPrimaryTarget(chartBranch)
                                    .setViewer(
                                            chartBranch.getSite().getViewer())
                            .setParameter(Spreadsheet.PARAM_CHART, chart)
                            .setParameter(Spreadsheet.PARAM_ROW_HEAD, rowHead)
                            .setParameter(Spreadsheet.PARAM_ROW,
                                    chart.findRow(rowHead)));
                }
            }
        }
    }

    private void handleDoubleClickInCell(Cell2 cell) {
        IBranchPart rowBranch = cell.getOwnedColumn().getHead();
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
                cell.getOwnedRow().getHead().getLabels());
        domain.handleRequest(request);
    }

    public boolean canMove(IBranchPart branch, MouseDragEvent me) {
        Chart2 chart = getChart(branch);
        RowHead rowHead = chart.findRowHead(me.startingLocation);
        if (rowHead != null) {
            MindMapUtils.setCache(branch,
                    Spreadsheet.CACHE_MOVE_SOURCE_ROW_HEAD, rowHead);
            return true;
        }
        return false;
    }

    public String getMoveTool(IBranchPart branch, MouseDragEvent me) {
        return Spreadsheet.TOOL_MOVE_ROW;
    }

    public int calcRowInsertionIndex(IBranchPart branch, Point pos) {
        Chart2 chart = getChart(branch);
        IInsertion rowIns = (IInsertion) MindMapUtils.getCache(branch,
                Spreadsheet.CACHE_ROW_INSERTION);
        int insHeight = rowIns == null ? 0
                : rowIns.getSize().height + chart.getMinorSpacing();
        List<Row2> rows = chart.getRows();
        int lineWidth = chart.getLineWidth();
        int y = chart.getTitle().getFigure().getBounds().y
                + chart.getTitleAreaHeight() + lineWidth
                + chart.getColHeadHeight() + chart.getMajorSpacing();
        for (int index = 0; index < rows.size(); index++) {
            Row2 row = rows.get(index);
            int colHeight = row.getHeight();
            int h = colHeight + insHeight / (rows.size() + 1);
            if (pos.y < y + h / 2)
                return index;
            y += h + lineWidth;
        }
        return rows.size();
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.EAST)
            return 1;
        if (direction == PositionConstants.WEST)
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        int index = calcInsIndex(branch, key, true);

        IBranchPart sub = index == subBranches.size()
                ? subBranches.get(subBranches.size() - 1)
                : subBranches.get(index);

        int deltaX = 0;
        if (index == subBranches.size())
            deltaX = (key.getInvent().getSize().width
                    + sub.getFigure().getSize().width) / 2
                    + getMinorSpacing(branch);
        else
            deltaX = (key.getInvent().getSize().width
                    - sub.getFigure().getSize().width) / 2
                    - key.getFigure().getSize().scale(0.8d).width
                    - getMinorSpacing(branch);

        return getFigureLocation(sub.getFigure()).getTranslated(deltaX,
                (key.getFigure().getSize().height
                        - sub.getTopicPart().getFigure().getSize().height) / 2);
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