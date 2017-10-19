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
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class TimelineVerticalHeadStructure extends AbstractBranchStructure {

    private ITransformer t = new HorizontalFlipper();

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
        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);

        Point ref = info.getReference();
        t.setOrigin(ref);
        TimelineVerticalData tvd = getCastedData(branch);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = t.tr(refBounds);

        int y = refBounds.bottom() + majorSpacing;

        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        int num = subBranches.size();

        int x = ref.x + majorSpacing;
        int yLeftBefore = y;
        int yRightBefore = y;
        int yLeft = y;
        int yRight = y;
        for (int i = 0; i < num; i++) {
            t.setEnabled(tvd.isLeftwardBranch(i));
            if (tvd.isLeftwardBranch(i)) {
                y = yRight > yLeftBefore + majorSpacing ? yRight
                        : yLeftBefore + majorSpacing;
            } else {
                y = yLeft > yRightBefore + majorSpacing ? yLeft
                        : yRightBefore + majorSpacing;
            }

            if (insertion != null && i == insertion.getIndex()) {
                Rectangle r = insertion.createRectangle(x, y);
                info.add(t.rr(r));
                y += r.height + minorSpacing;
            }
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();
            Insets ins = helper.getInsets(subBranch);
            ins = t.ti(ins);
            Dimension size = subBranchFigure.getPreferredSize();
            Rectangle r = new Rectangle(x + ins.left, y + ins.top, size.width,
                    size.height);
            info.put(subBranchFigure, t.rr(r));

            IFigure topicFigure = subBranch.getTopicPart().getFigure();
            if (tvd.isLeftwardBranch(i)) {
                yRightBefore = y + topicFigure.getPreferredSize().height
                        + majorSpacing;
                yRight = y + size.height + ins.getHeight() + minorSpacing;
            } else {
                yLeftBefore = y + topicFigure.getPreferredSize().height
                        + majorSpacing;
                yLeft = y + size.height + ins.getHeight() + minorSpacing;
            }
        }

        if (insertion != null && num == insertion.getIndex()) {
            Dimension insSize = insertion.getSize();
            if (insSize != null) {
                Rectangle r = new Rectangle(x, y, insSize.width,
                        insSize.height);
                info.add(t.rr(r));
            }
        }
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (!branch.getSubBranches().isEmpty()) {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                return getSubTopicPart(branch, 0);
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_UP.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
        } else if (!sequential) {
            if (getCastedData(branch)
                    .isLeftwardBranch(sourceChild.getBranchIndex())) {
                if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            } else {
                if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.SOUTH;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return calcChildTargetOrientation(subBranch);
    }

    private int calcChildTargetOrientation(IBranchPart subBranch) {
        if (subBranch.getParentBranch() != null
                && getCastedData(subBranch.getParentBranch())
                        .isLeftwardBranch(subBranch.getBranchIndex()))
            return PositionConstants.EAST;
        return PositionConstants.WEST;
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        IReferencedFigure topicFigure = (IReferencedFigure) branch
                .getTopicPart().getFigure();
        Point ref = topicFigure.getReference();
        t.setOrigin(ref);

        Point childRef = t.tp(getChildRef(branch, ref, key));
        Rectangle topicBounds = t.tr(topicFigure.getBounds());
        List<IBranchPart> subBranches = branch.getSubBranches();
        int totalHeight = 0;
        if (!subBranches.isEmpty()) {
            int fy = subBranches.get(0).getFigure().getBounds().y;
            int ly = subBranches.get(subBranches.size() - 1).getFigure()
                    .getBounds().bottom();
            int sly = 0;
            if (subBranches.size() > 1)
                sly = subBranches.get(subBranches.size() - 2).getFigure()
                        .getBounds().bottom();
            totalHeight = ly > sly ? ly - fy : sly - ly;
        }
        int dy = childRef.y - topicBounds.bottom();
        int dx = childRef.x - topicBounds.right();
        if (childRef.x >= topicBounds.x - MindMapUI.SEARCH_RANGE / 2
                && childRef.x < topicBounds.right()
                        + MindMapUI.SEARCH_RANGE / 2) {
            if (dy > 0 && dy < totalHeight + MindMapUI.SEARCH_RANGE) {
                return dy;
            }
            int d = dx * dx + dy * dy;
            return d;
        }
        return super.calcChildDistance(branch, key);
    }

    protected Object createStructureData(IBranchPart branch) {
        return new TimelineVerticalData(branch);
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof TimelineVerticalData);
    }

    private TimelineVerticalData getCastedData(IBranchPart branch) {
        return (TimelineVerticalData) super.getStructureData(branch);
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

        int y = childRef.y - ref.y;
        int ret = 0;
        for (IBranchPart sub : subBranches) {
            IFigure tf = sub.getTopicPart().getFigure();
            Point tr = ((IReferencedFigure) tf).getReference();
            int d = tr.y - ref.y;
            if (y < d)
                return ret;
            ret++;
        }
        return withDisabled ? subBranches.size() : -1;
    }

    private Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize().scale(0.8);
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key, true),
                calcInsSize(branch, key));
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.NORTH)
            return 1;
        if (direction == PositionConstants.SOUTH)
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosingBranches = summary.getEnclosingBranches();
        if (!enclosingBranches.isEmpty()) {
            IBranchPart sub = enclosingBranches.get(0);
            if (getCastedData(branch).isLeftwardBranch(sub.getBranchIndex()))
                return PositionConstants.WEST;
            return PositionConstants.EAST;
        }
        return PositionConstants.WEST;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public boolean isChildLeftwards(IBranchPart branch, IBranchPart child) {
        return getCastedData(branch).isLeftwardBranch(child.getBranchIndex());
    }

    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        if (subBranches.isEmpty())
            return calcFirstChildPosition(branch, key);

        TimelineVerticalData tvd = getCastedData(branch);

        int majorSpacing = getMajorSpacing(branch);
        int index = calcInsIndex(branch, key, true);
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        int x = getFigureLocation(branch.getFigure()).x;
        int dx = tvd.isLeftwardBranch(index)
                ? majorSpacing + inventSize.width / 2
                : -majorSpacing - inventSize.width / 2;
        if (index < 2 && index != subBranches.size()) {
            IBranchPart sub = subBranches.get(index);
            Rectangle bounds = sub.getFigure().getBounds();
            int y = bounds.y - majorSpacing - insSize.height / 2;
            return new Point(x + dx, y);
        }

        if (index == subBranches.size()) {
            IBranchPart sub = subBranches.get(subBranches.size() - 1);
            Rectangle bounds = sub.getTopicPart().getFigure().getBounds();
            return new Point(x + dx, bounds.bottom() + majorSpacing);
        }

        return calcInventPosition(subBranches.get(index - 1),
                subBranches.get(index - 2), key, tvd.isLeftwardBranch(index));
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

    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isRightOrUp) {
        Dimension insSize = key.getFigure().getSize();
        int minorSpacing = getMinorSpacing(orientation.getParentBranch());
        int majorSpacing = getMajorSpacing(orientation.getParentBranch());
        IFigure tf = assist.getTopicPart().getFigure();
        Dimension ts = tf.getSize();

        int x = getFigureLocation(assist.getFigure()).x
                + (isRightOrUp ? (-ts.width + insSize.width) / 2
                        : (ts.width - insSize.width) / 2);

        int y1 = assist.getFigure().getBounds().bottom() + minorSpacing;
        int y2 = orientation.getTopicPart().getFigure().getBounds().bottom()
                + majorSpacing;

        return new Point(x,
                y1 > y2 ? y1 + insSize.height / 2 : y2 + insSize.height / 2);
    }

    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int x = getMajorSpacing(branch) + key.getInvent().getSize().width / 2;
        return getFigureLocation(branch.getFigure()).getTranslated(x,
                branch.getFigure().getSize().height / 2
                        + getMajorSpacing(branch)
                        + key.getFigure().getSize().height / 2);
    }

}
