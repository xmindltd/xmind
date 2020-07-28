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
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public class AntiClockwiseRadialStructure extends BaseRadialStructure {

    @Override
    public boolean isChildLeft(IBranchPart branch, IBranchPart child) {
        if (branch == null) {
            return false;
        }
        if (branch.isCentral()) {
            Point pos = (Point) MindMapUtils.getCache(child,
                    IBranchPart.CACHE_PREF_POSITION);
            if (pos != null) {
                return RadialUtils.isLeft(0, pos.x);
            }
        }
        if (calculatingBranches.contains(branch)) {
            // avoid recursively calling
            return false;
        }
        calculatingBranches.add(branch);
        boolean left;
        int index = branch.getSubBranches().indexOf(child);
        if (index >= 0) {
            left = !(index >= getRadialData(branch).getNumRight());
        } else if (branch.getSummaryBranches().contains(child)) {
            left = !(isSummaryChildLeft(branch, child));
        } else if (branch.getCalloutBranches().contains(child)) {
            left = isChildLeft(branch.getParentBranch(), branch);
        } else {
            left = RadialUtils.isLeft(getReference(branch).x,
                    getReference(child).x);
        }
        calculatingBranches.remove(branch);
        return left;
    }

    @Override
    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();

        int[] childrenSpacings = cache.getChildrenSpacings();
        int num = subBranches.size();
        boolean right = false;
        RadiationInsertion insertion = getCurrentInsertion(branch);
        int insHeight = insertion == null ? 0 : insertion.getSize().height;

        int y = -cache.getRightSumSpacing() / 2;
        if (insertion != null && !insertion.right) {
            y -= insHeight / 2;
        }

        Point ref = info.getReference(); // the Center Topic's location 

        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = cache.getLeftSumSpacing() / 2;
                if (insertion != null)
                    if (insertion.right) {
                        y += insHeight / 2;
                    }
                right = true;
            }

            if (insertion != null && i == insertion.getIndex()) {
                if (i != numRight || insertion.right) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils
                            .getPrefBounds(insertion.getSize(), p, right);
                    info.add(insBounds);
                    if (insertion.right)
                        y -= insHeight;
                    else
                        y += insHeight;
                }
            }

            IBranchPart subBranch = subBranches.get(i); // to obtain the i st subTopic's bracnch.
            Rectangle r;
            Dimension offset = getOffset(subBranch);

            IFigure subFigure = subBranch.getFigure(); // the SubTopic's figure
            if (offset != null && subFigure instanceof IReferencedFigure) {
                Point subRef = ref.getTranslated(offset);
                r = ((IReferencedFigure) subFigure).getPreferredBounds(subRef);

            } else {
                int x = cache.getX(y, right);
                Point subRef = ref.getTranslated(x, y);
                r = RadialUtils.getPrefBounds(subBranch, subRef, right);

            }
            info.put(subFigure, r);

            if (i < numRight)
                y += childrenSpacings[i];
            else
                y -= childrenSpacings[i];

            if (insertion != null) {
                if ((i == numRight - 1 && insertion.getIndex() == numRight
                        && !insertion.right) || i == num) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils
                            .getPrefBounds(insertion.getSize(), p, right);
                    info.add(insBounds);

                    y += insHeight;
                }
            }
        }

    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {

        List<IBranchPart> subBranches = branch.getSubBranches();
        if (subBranches.isEmpty())
            return withDisabled ? 0 : -1;

        if (branch.isFolded())
            return withDisabled ? 0 : -1;

        ITopicPart topic = branch.getTopicPart();
        if (topic == null)
            return withDisabled ? 0 : -1;

        Point childRef = key.getFigure().getReference();
        Point ref = ((IReferencedFigure) topic.getFigure()).getReference();

        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();

        int[] childrenSpacings = cache.getChildrenSpacings();

        int num = subBranches.size();
        boolean right = true;

        Dimension insSize = calcInsSize(branch, key);
        int insHeight = insSize.height;
        boolean insRight = calcInsSide(branch, ref, key);

        int startY = ref.y;
        int y = startY - cache.getRightSumSpacing() / 2;
        if (!insRight) {
            y -= insHeight / 2;
        }

        int ret = 0;
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = startY + cache.getLeftSumSpacing() / 2;
                if (insRight) {
                    y += insHeight / 2;
                }
                //ret = num - 1;
                right = false;
            }

            IBranchPart subbranch = subBranches.get(i);
            IFigure subFigure = subbranch.getFigure();
            Insets refIns = RadialUtils.getRefInsets(subFigure, right);
            int hint;
            if (i < numRight) {
                hint = y - refIns.top + (refIns.getHeight() + insHeight) / 2;
            } else {
                hint = y + refIns.top - (refIns.getHeight() + insHeight) / 2;
            }
            if (i < numRight) {
                if (!insRight && childRef.y <= hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
                if (i == numRight - 1 && childRef.x < ref.x
                        && childRef.y >= hint)
                    return ret;
            } else {
                if (insRight && childRef.y > hint)//childRef.y >= hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
//                if (i == numRight && childRef.x > ref.x && childRef.y >= hint)
//                    return ret;
            }
            if (i < numRight)
                y += childrenSpacings[i];
            else
                y -= childrenSpacings[i];
        }
        return withDisabled ? num : -1;
    }

    public IPart calcChildNavigation(IBranchPart branch, // centre Topic 
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        int numRight = getRadialData(branch).getNumRight();
        int numLeft = getRadialData(branch).getNumLeft();
        int index = sourceChild.getBranchIndex();
        int num = branch.getSubBranches().size();

        if (GEF.REQ_NAV_UP.equals(navReqType)) { //UP
            if (index == 0)
                return getSubTopicPart(branch, num - 1);
            else if (index == numRight - 1)
                return getSubTopicPart(branch, index - 1);
            else if (index == numRight)
                return getSubTopicPart(branch, index + 1);
            else if (index > numRight) {
                if (index == num - 1)
                    return getSubTopicPart(branch, 0);
                else
                    return getSubTopicPart(branch, index + 1);
            } else
                return getSubTopicPart(branch, index - 1);

        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) { // DOWN
            if (index == 0)
                return getSubTopicPart(branch, index + 1);
            else if (index == numRight - 1)
                return getSubTopicPart(branch, index + 1);
            else if (index == numRight)
                return getSubTopicPart(branch, index - 1);
            else if (index > numRight)
                return getSubTopicPart(branch, index - 1);
            else
                return getSubTopicPart(branch, index + 1);

        } else if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            if (index < numRight)
                return null;
            else
                return branch.getTopicPart();
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            if (index >= numLeft)
                return null;
            else
                return branch.getTopicPart();
        }

        else if (!sequential) {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                int numFirst = getRadialData(branch).getNumRight();
                if (sourceChild.getBranchIndex() >= numFirst) {
                    return branch.getTopicPart();
                }
            } else if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                int numFirst = getRadialData(branch).getNumRight();
                if (sourceChild.getBranchIndex() < numFirst) {
                    return branch.getTopicPart();
                }
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        int num = branch.getSubBranches().size();
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            if (!branch.getSubBranches().isEmpty())
                return getSubTopicPart(branch, 0);
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType))
            return getSubTopicPart(branch, num - 1);
        return super.calcNavigation(branch, navReqType);
    }

    public Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize();
    }

    public boolean calcInsSide(IBranchPart branch, Point branchRef,
            ParentSearchKey key) {
        Point childRef = key.getFigure().getReference();
        return childRef.x > branchRef.x;
        // if Child on the right of Branch, return true;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosingBranches = summary.getEnclosingBranches();
        if (!enclosingBranches.isEmpty()) {
            IBranchPart subBranch = enclosingBranches.get(0);
            int index = subBranch.getBranchIndex();
            if (index >= 0) {
                if (index >= getRadialData(branch).getNumRight()) {
                    return PositionConstants.EAST;
                }
            }
        }
        return PositionConstants.WEST;
    }

    @Override
    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        Point firstLoc = calcFirstChildPosition(branch, key);
        if (subBranches.isEmpty())
            return firstLoc;

        int index = calcInsIndex(branch, key, true);
        RadialData cache = getRadialData(branch);

        int subSize = subBranches.size();
        int left = cache.getNumRight();
        int right = subSize - left;

        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        IBranchPart first = subBranches.get(0);
        Rectangle fBounds = first.getFigure().getBounds();
        if (index == 0) {
            int x = fBounds.right() - inventSize.width / 2;
            int y = fBounds.y - (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        if (index == subSize || index == -1) {
            if (subSize == 1 && isWithinThreshold(first)) {
                if (fBounds.bottom() > 0) {
                    int x = fBounds.right() - inventSize.width / 2;
                    int y = fBounds.bottom()
                            + (insSize.height + inventSize.height) / 2;
                    return new Point(x, y);
                } else {
                    return new Point(firstLoc.x, -firstLoc.y);
                }
            }

            if (right == 0)
                return firstLoc.getNegated();

            IBranchPart sub = subBranches.get(subSize - 1);
            Rectangle bounds = sub.getFigure().getBounds();
            if (right == 1 && bounds.y > 0)
                return new Point(-firstLoc.x, firstLoc.y);

            int x = bounds.x + inventSize.width / 2;
            int y = bounds.y - (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        if (index == left) {
            boolean isLeft = (left == 1 && right == 1)
                    || isRight(subBranches, child, left);
            IBranchPart sub = isLeft ? subBranches.get(index - 1)
                    : subBranches.get(index);
            Rectangle bounds = sub.getFigure().getBounds();
            int x;
            if (isLeft)
                x = bounds.right() - inventSize.width / 2;
            else
                x = bounds.x + inventSize.width / 2;
            int y = bounds.bottom() + (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        boolean isLeft = index < left;
        return calcInventPosition(subBranches.get(isLeft ? index - 1 : index),
                subBranches.get(isLeft ? index : index - 1), key, !isLeft);
    }

    @Override
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

        RadialData cache = getRadialData(branch);

        int left = cache.getNumRight();

        Dimension inventSize = key.getInvent().getSize();
        if (index == oldIndex) {
            if (index == left - 1 && key.getCursorPos().x > 0
                    && (!subBranches.get(left).getFigure().isEnabled()))
                index += 1;
            int delta = getTopicSize(subBranches.get(index)).width / 2
                    - inventSize.width / 2;
            int deltaX = index < left ? delta : -delta;
            return getReference(subBranches.get(index)).getTranslated(deltaX,
                    0);
        }
        return calcInsertPosition(branch, child, key);
    }

    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int y = -(getMinorSpacing(branch) * 3 / 4 + 8) * 4;
        int x = getRadialData(branch).getX(y, true);

        return getReference(branch).getTranslated(-x, y)
                .getTranslated(-key.getInvent().getSize().width / 2, 0);
    }

}
