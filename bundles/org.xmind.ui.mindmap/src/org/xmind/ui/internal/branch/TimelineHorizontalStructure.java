package org.xmind.ui.internal.branch;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IBranchConnections2;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class TimelineHorizontalStructure extends AbstractBranchStructure {

    private ITransformer t = new HorizontalFlipper();

    private boolean upwards;

    public TimelineHorizontalStructure(boolean upwards) {
        this.upwards = upwards;
    }

    @Override
    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        t.setOrigin(ref);

        Rectangle topicBounds = info.getCheckedClientArea();
        topicBounds = t.tr(topicBounds);

        IFigure pmFigure = plusMinus.getFigure();
        Dimension size = pmFigure.getPreferredSize();
        int x = ref.x - size.width / 2;
        int y = isUpwards(branch) ? topicBounds.y() - size.height
                : topicBounds.bottom();
        Rectangle r = new Rectangle(x, y, size.width, size.height);
        info.put(pmFigure, r);
    }

    @Override
    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);
        Point ref = info.getReference();
        t.setOrigin(ref);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = t.tr(refBounds);

        int x = ref.x + majorSpacing;

        int totalHeight = calcTotalChildrenHeight(branch, minorSpacing, true);
        int y = isUpwards(branch) ? refBounds.y() - totalHeight - majorSpacing
                : refBounds.bottom() + majorSpacing;

        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        int num = subBranches.size();
        for (int i = 0; i < num; i++) {
            if (insertion != null && i == insertion.getIndex()) {
                Rectangle r = insertion.createRectangle(x, y);
                info.add(r);
                y += r.height + minorSpacing;
            }
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();
            Insets ins = helper.getInsets(subBranch);
            Dimension size = subBranchFigure.getPreferredSize();
            Rectangle r = new Rectangle(x + ins.left,
                    y + (isUpwards(subBranch) ? ins.bottom : ins.top),
                    size.width, size.height);
            info.put(subBranchFigure, r);
            y += size.height + ins.getHeight() + minorSpacing;
        }

        if (insertion != null && num == insertion.getIndex()) {
            Dimension insSize = insertion.getSize();
            if (insSize != null) {
                Rectangle r = new Rectangle(x, y, insSize.width,
                        insSize.height);
                info.add(r);
            }
        }

    }

    protected void doFillCalloutBranches(IBranchPart branch,
            List<IBranchPart> calloutBranches, LayoutInfo info) {
        if (branch.isCentral()) {
            for (IBranchPart calloutBranch : calloutBranches) {
                calloutBranch.getFigure().setVisible(false);
            }
            IBranchConnections2 calloutConnections = branch
                    .getCalloutConnections();
            for (int index = 0; index < calloutBranches.size(); index++) {
                IDecoration decoration = calloutConnections
                        .getDecoration(index);
                if (decoration instanceof IBranchConnectionDecoration) {
                    ((IBranchConnectionDecoration) decoration)
                            .setVisible(branch.getFigure(), false);
                }
            }
            return;
        }

        IBranchPart parentBranch = branch.getParentBranch();
        int orientation = getChildTargetOrientation(parentBranch, branch);
        boolean left = PositionConstants.EAST == orientation;
        boolean right = PositionConstants.WEST == orientation;
        boolean south = PositionConstants.NORTH == orientation;
        boolean north = PositionConstants.SOUTH == orientation;

        for (IBranchPart calloutBranch : calloutBranches) {
            Rectangle parentTopicArea = info
                    .get(branch.getTopicPart().getFigure());
            Rectangle parentBranchArea = info.getCheckedClientArea();

            IReferencedFigure calloutBranchFigure = (IReferencedFigure) calloutBranch
                    .getFigure();

            ITopicPart calloutPart = calloutBranch.getTopicPart();
            if (calloutPart == null)
                continue;
            IFigure calloutFigure = calloutPart.getFigure();
            Dimension calloutSize = calloutFigure.getPreferredSize();

            //over parent topic center
            org.xmind.core.util.Point position = calloutBranch.getTopic()
                    .getPosition();
            if (position == null)
                position = new org.xmind.core.util.Point();
            boolean originPosition = position.x == 0 && position.y == 0;

            int offsetX = originPosition ? 0 : position.x;
            int offsetY = originPosition
                    ? -calloutSize.height / 2 - parentTopicArea.height / 2 - 10
                    : position.y;

            boolean upDown = offsetY < 0;

            int dummyCalloutX = parentTopicArea.getCenter().x + offsetX
                    - calloutSize.width / 2;

            if (left) {
                //limit left movable boundary
                int dx = (dummyCalloutX + calloutSize.width)
                        - (parentTopicArea.x + parentTopicArea.width);
                offsetX = dx > 0 ? offsetX - dx : offsetX;
            } else if (right) {
                //limit right movable boundary
                int dx = dummyCalloutX - parentTopicArea.x;
                offsetX = dx < 0 ? offsetX - dx : offsetX;
            }

            Point reference = info.getReference();
            Point translated = reference.getTranslated(offsetX, offsetY);
            Rectangle bounds = calloutBranchFigure
                    .getPreferredBounds(translated);

            int subRectX = left || south || north ? parentBranchArea.x
                    : parentBranchArea.x + parentTopicArea.width;
            int subRectY = left || right || north ? parentBranchArea.y
                    : parentBranchArea.y + parentTopicArea.height;
            int subRectWidth = left || right
                    ? parentBranchArea.width - parentTopicArea.width
                    : parentBranchArea.width;
            int subRectHeight = left || right ? parentBranchArea.height
                    : parentBranchArea.height - parentTopicArea.height;
            Rectangle subRect = new Rectangle(subRectX, subRectY, subRectWidth,
                    subRectHeight);
            boolean touchSub = subRect.touches(bounds);
            boolean touchParentTopic = bounds.touches(parentTopicArea);
            if (touchSub) {
                int y = upDown ? subRect.y - bounds.height - 10
                        : subRect.bottom() + 10;
                bounds.setY(y);
            } else if (touchParentTopic) {
                int y = upDown ? parentTopicArea.y - bounds.height - 10
                        : parentTopicArea.bottom() + 10;
                bounds.setY(y);
            }

            int y = isUpwards(branch)
                    ? Math.min(parentBranchArea.y,
                            bounds.bottom())
                    - calloutBranch.getFigure().getPreferredSize().height
                    : Math.max(parentBranchArea.bottom(), bounds.y);
            bounds.setY(y);

            info.put(calloutBranchFigure, bounds);
        }
    }

    private int calcTotalChildrenHeight(IBranchPart branch, int minorSpacing,
            boolean withInsertion) {
        int totalHeight = 0;
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        Iterator<IBranchPart> it = branch.getSubBranches().iterator();
        while (it.hasNext()) {
            IBranchPart subBranch = it.next();
            totalHeight += subBranch.getFigure().getPreferredSize().height
                    + helper.getInsets(subBranch).getHeight();
            if (it.hasNext())
                totalHeight += minorSpacing;
        }
        if (withInsertion) {
            IInsertion ins = getCurrentInsertion(branch);
            if (ins != null) {
                Dimension insSize = ins.getSize();
                if (insSize != null)
                    totalHeight += minorSpacing + insSize.height;
            }
        }
        return totalHeight;
    }

    public int getSourceOrientation(IBranchPart branch) {
        if (isUpwards(branch))
            return PositionConstants.NORTH;
        return PositionConstants.SOUTH;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return PositionConstants.WEST;
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (!branch.getSubBranches().isEmpty()) {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType))
                return getSubTopicPart(branch, 0);
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
            if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                return branch.getTopicPart();
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    @Override
    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        IFigure branchFigure = branch.getFigure();
        IReferencedFigure topicFigure = (IReferencedFigure) branch
                .getTopicPart().getFigure();
        Point ref = topicFigure.getReference();
        t.setOrigin(ref);
        Point childRef = t.tp(getChildRef(branch, ref, key));
        Rectangle branchBounds = t.tr(branchFigure.getBounds());
        Rectangle topicBounds = t.tr(topicFigure.getBounds());
        Rectangle childBounds = t.tr(key.getFigure().getBounds());
        int dx = childRef.x - ref.x;
        int dy = isUpwards(branch) ? topicBounds.y() - childRef.y
                : childRef.y - topicBounds.bottom();
        if (dy > 0 && childBounds.x <= ref.x) {
            if (childRef.x < branchBounds.right()
                    + MindMapUI.SEARCH_RANGE / 2) {
                return Math.abs(dx) + Math.abs(dy);
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
        Dimension insSize = calcInsSize(key.getFigure());
        int insHeight = insSize.height;
        int minorSpacing = getMinorSpacing(branch);
        Rectangle topicBounds = t.t(topicFigure.getBounds());
        int y = 0;
        if (isUpwards(branch)) {
            y = topicBounds.y();
            if (!branch.getSubBranches().isEmpty()) {
                IPlusMinusPart pm = branch.getPlusMinus();
                if (pm != null)
                    y -= pm.getFigure().getSize().height;

                y -= calcTotalChildrenHeight(branch, minorSpacing, true);
            }
        } else {
            y = topicBounds.bottom();
            if (!branch.getSubBranches().isEmpty()) {
                IPlusMinusPart plusMinus = branch.getPlusMinus();
                if (plusMinus != null) {
                    y += plusMinus.getFigure().getSize().height;
                }
            }
        }
        int ret = calcInsIndex(branch, y, childRef, insHeight, minorSpacing,
                withDisabled);
        return ret;
    }

    private int calcInsIndex(IBranchPart branch, int startY, Point childRef,
            int insHeight, int spacing, boolean withDisabled) {
        int ret = 0;
        int sum = 0;
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        for (IBranchPart subBranch : subBranches) {
            IFigure subFigure = subBranch.getFigure();
            int h = getBorderedSize(branch, subBranch).height;
            int hint = startY + sum + (insHeight + h + spacing) / 2;
            if (childRef.y < hint) {
                return ret;
            }
            sum += h + spacing;
            if (withDisabled || subFigure.isEnabled())
                ret++;
        }
        return withDisabled ? num : -1;
    }

    private Dimension calcInsSize(IReferencedFigure child) {
        return child.getSize().scale(0.8);
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        int newIndex = calcInsIndex(branch, key, true);
        Dimension newSize = calcInsSize(key.getFigure());
        return new Insertion(branch, newIndex, newSize);
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return PositionConstants.EAST;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        if (isUpwards(branch))
            return PositionConstants.NORTH;
        return PositionConstants.SOUTH;
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.SOUTH)
            return 1;
        if (direction == PositionConstants.NORTH)
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }

    @Override
    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int x = getMajorSpacing(branch) + key.getInvent().getSize().width / 2;
        int y = branch.getFigure().getSize().height / 2
                + getMajorSpacing(branch)
                + key.getFigure().getSize().height / 2;
        return getFigureLocation(branch.getFigure()).getTranslated(x,
                isUpwards(branch) ? -y : y);
    }

    private boolean isUpwards(IBranchPart branch) {
        return upwards;
    }

}
