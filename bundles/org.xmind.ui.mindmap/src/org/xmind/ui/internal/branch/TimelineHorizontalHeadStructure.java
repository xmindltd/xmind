package org.xmind.ui.internal.branch;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.draw2d.geometry.VerticalFlipper;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IInfoPart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class TimelineHorizontalHeadStructure extends AbstractBranchStructure {

    private ITransformer t = new VerticalFlipper();

    protected void addExtraSpaces(IBranchPart branch,
            ReferencedLayoutData data) {
        super.addExtraSpaces(branch, data);
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
        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);

        Point ref = info.getReference();
        t.setOrigin(ref);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = t.tr(refBounds);

        int y = ref.y;
        int x = refBounds.right() + majorSpacing;

        int num = subBranches.size();

        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);

        TimelineHorizontalData thd = getCastedData(branch);

        int xUpBefore = x;
        int xDownBefore = x;
        int xUp = x;
        int xDown = x;
        for (int i = 0; i < num; i++) {
            t.setEnabled(thd.isUpwardBranch(i));
            if (thd.isUpwardBranch(i)) {
                x = xUp > xDownBefore + majorSpacing ? xUp
                        : xDownBefore + majorSpacing;
            } else {
                x = xDown > xUpBefore + majorSpacing ? xDown
                        : xUpBefore + majorSpacing;
            }

            if (insertion != null && i == insertion.getIndex()) {
                Rectangle r = insertion.createRectangle(x,
                        y - insertion.getSize().height / 2);
                info.add(t.rr(r));
                x += r.width + majorSpacing;
            }
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();
            Insets ins = helper.getInsets(subBranch);

            Dimension size = subBranchFigure.getPreferredSize();
            IFigure topicFigure = subBranch.getTopicPart().getFigure();
            Dimension topicSize = topicFigure.getSize();
            int infoHeight = 0;
            IInfoPart infoPart = subBranch.getInfoPart();
            if (infoPart != null) {
                infoHeight = infoPart.getFigure().getSize().height;
            }
            int dy = topicSize.height == 0 ? size.height / 2
                    : thd.isUpwardBranch(i) ? topicSize.height / 2 + infoHeight
                            : topicSize.height / 2;
            Rectangle r = new Rectangle(x + ins.left, y - dy, size.width,
                    size.height);
            info.put(subBranchFigure, t.rr(r));

            if (thd.isUpwardBranch(i)) {
                xUpBefore = x + topicFigure.getPreferredSize().width
                        + majorSpacing;
                xUp = x + size.width + ins.getWidth() + minorSpacing;
            } else {
                xDownBefore = x + topicFigure.getPreferredSize().width
                        + majorSpacing;
                xDown = x + size.width + ins.getWidth() + minorSpacing;
            }
        }

        if (insertion != null && num == insertion.getIndex()) {
            Dimension insSize = insertion.getSize();
            if (insSize != null) {
                Rectangle r = new Rectangle(x, y - insSize.height / 2,
                        insSize.width, insSize.height);
                info.add(t.rr(r));
            }
        }
    }

    @Override
    protected Object createStructureData(IBranchPart branch) {
        return new TimelineHorizontalData(branch);
    }

    @Override
    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof TimelineHorizontalData);
    }

    private TimelineHorizontalData getCastedData(IBranchPart branch) {
        return (TimelineHorizontalData) super.getStructureData(branch);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
        } else if (!sequential) {
            if (getCastedData(branch)
                    .isUpwardBranch(sourceChild.getBranchIndex())) {
                if (GEF.REQ_NAV_UP.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            } else {
                if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    @Override
    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (!branch.getSubBranches().isEmpty()) {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType))
                return getSubTopicPart(branch, 0);
        }

        return super.calcNavigation(branch, navReqType);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        IReferencedFigure topicFigure = (IReferencedFigure) branch
                .getTopicPart().getFigure();
        Point ref = topicFigure.getReference();
        t.setOrigin(ref);
        Point childRef = t.tp(getChildRef(branch, ref, key));
        Rectangle topicBounds = t.tr(topicFigure.getBounds());
        List<IBranchPart> subBranches = branch.getSubBranches();
        int totalWidth = 0;
        if (!subBranches.isEmpty()) {
            int fx = subBranches.get(0).getFigure().getBounds().x;
            int lx = subBranches.get(subBranches.size() - 1).getFigure()
                    .getBounds().right();
            int slx = 0;
            if (subBranches.size() > 1)
                slx = subBranches.get(subBranches.size() - 2).getFigure()
                        .getBounds().right();
            totalWidth = lx > slx ? lx - fx : slx - lx;
        }
        int dy = childRef.y - topicBounds.bottom();
        int dx = childRef.x - topicBounds.right();
        if (childRef.y >= topicBounds.y - MindMapUI.SEARCH_RANGE / 2
                && childRef.y < topicBounds.bottom()
                        + MindMapUI.SEARCH_RANGE / 2) {
            if (dx > 0 && dx < totalWidth + MindMapUI.SEARCH_RANGE) {
                return dx;
            }
            int d = dx * dx + dy * dy;
            return d;
        }
        return super.calcChildDistance(branch, key);
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        if (branch.getSubBranches().isEmpty() || branch.isFolded())
            return withDisabled ? 0 : -1;

        ITopicPart topic = branch.getTopicPart();
        if (topic == null)
            return withDisabled ? 0 : -1;

        IFigure topicFigure = topic.getFigure();
        Point ref = ((IReferencedFigure) topicFigure).getReference();
        t.setOrigin(ref);
        Point childRef = t.tp(getChildRef(branch, ref, key));
        List<IBranchPart> subBranches = branch.getSubBranches();

        int x = childRef.x - ref.x;
        int ret = 0;
        for (IBranchPart sub : subBranches) {
            IFigure tf = sub.getTopicPart().getFigure();
            Point tr = ((IReferencedFigure) tf).getReference();
            int d = tr.x - ref.x;
            if (x < d)
                return ret;
            ret++;
        }
        return withDisabled ? subBranches.size() : -1;
    }

    private Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize().scale(0.8);
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.EAST;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.WEST;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key, true),
                calcInsSize(branch, key));
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.EAST;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosingBranches = summary.getEnclosingBranches();
        if (!enclosingBranches.isEmpty()) {
            IBranchPart sub = enclosingBranches.get(0);
            if (getCastedData(branch).isUpwardBranch(sub.getBranchIndex()))
                return PositionConstants.NORTH;
            return PositionConstants.SOUTH;
        }
        return PositionConstants.NORTH;
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
        if (subBranches.isEmpty())
            return calcFirstChildPosition(branch, key);

        int majorSpacing = getMajorSpacing(branch);
        int index = calcInsIndex(branch, key, true);
        Dimension insSize = key.getFigure().getSize();

        int y = getFigureLocation(branch.getFigure()).y;
        if (index < 2 && index != subBranches.size()) {
            IBranchPart sub = subBranches.get(index);
            Rectangle bounds = sub.getFigure().getBounds();
            int x = bounds.x - majorSpacing - insSize.width / 2;
            return new Point(x, y);
        }

        if (index == subBranches.size()) {
            IBranchPart sub = subBranches.get(subBranches.size() - 1);
            Rectangle bounds = sub.getTopicPart().getFigure().getBounds();
            int x = bounds.right() + majorSpacing + insSize.width / 2;
            return new Point(x, y);
        }

        return calcInventPosition(subBranches.get(index - 1),
                subBranches.get(index - 2), key, true);
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

        if (index == oldIndex) {
            IBranchPart sub = subBranches.get(index);
            return getFigureLocation(sub.getFigure()).getTranslated(0, 0);
        }

        return calcInsertPosition(branch, child, key);
    }

    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        Point loc = getFigureLocation(branch.getFigure());
        return loc.getTranslated(branch.getFigure().getPreferredSize().width
                + getMajorSpacing(branch), 0);
    }

    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isRightOrUp) {
        Dimension insSize = key.getFigure().getSize();
        int minorSpacing = getMinorSpacing(orientation.getParentBranch());
        int majorSpacing = getMajorSpacing(orientation.getParentBranch());

        int x1 = assist.getFigure().getBounds().right() + minorSpacing;
        int x2 = orientation.getTopicPart().getFigure().getBounds().right()
                + majorSpacing;

        return new Point(
                x1 > x2 ? x1 + insSize.width / 2 : x2 + insSize.width / 2,
                getFigureLocation(orientation.getFigure()).y);
    }

    public boolean isChildUpwards(IBranchPart branch, IBranchPart child) {
        return getCastedData(branch).isUpwardBranch(child.getBranchIndex());
    }
}
