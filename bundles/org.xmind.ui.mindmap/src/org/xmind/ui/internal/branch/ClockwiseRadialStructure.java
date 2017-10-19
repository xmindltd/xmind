package org.xmind.ui.internal.branch;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.tools.ParentSearchKey;

public class ClockwiseRadialStructure extends BaseRadialStructure {

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        RadialData cache = getRadialData(branch);
        int numRight = cache.getNumRight();

        int[] childrenSpacings = cache.getChildrenSpacings();
        int num = subBranches.size();
        boolean right = true;
        RadiationInsertion insertion = getCurrentInsertion(branch);
        int insHeight = insertion == null ? 0 : insertion.getSize().height;

        int y = -cache.getRightSumSpacing() / 2;

        if (insertion != null && insertion.right) {
            y -= insHeight / 2;
        }

        Point ref = info.getReference();
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = cache.getLeftSumSpacing() / 2;
                if (insertion != null) {
                    if (!insertion.right) {
                        y += insHeight / 2;
                    }
                }
                right = false;
            }

            if (insertion != null && i == insertion.getIndex()) {
                if (i != numRight || !insertion.right) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(
                            insertion.getSize(), p, right);
                    info.add(insBounds);
                    if (insertion.right)
                        y += insHeight;
                    else
                        y -= insHeight;
                }
            }

            IBranchPart subBranch = subBranches.get(i);
            Rectangle r;
            Dimension offset = getOffset(subBranch);
            IFigure subFigure = subBranch.getFigure();
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
                if ((i == numRight - 1 && insertion.getIndex() == numRight && insertion.right)
                        || i == num) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(
                            insertion.getSize(), p, right);
                    info.add(insBounds);
                    y += insHeight;
                }
            }
        }
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        int numRight = getRadialData(branch).getNumRight();
        int index = sourceChild.getBranchIndex();
        int num = branch.getSubBranches().size();

        if (GEF.REQ_NAV_UP.equals(navReqType)) { //UP
            if (index == 0) {
                return getSubTopicPart(branch, num - 1);
            } else if (index == numRight - 1)
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

        } else if (!sequential) {
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

    @Override
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

        int startY = ref.y;
        int y = startY - cache.getRightSumSpacing() / 2;

        boolean insRight = calcInsSide(branch, ref, key);
        if (insRight) {
            if (numRight == 0)
                return 0;
            y -= insHeight / 2;
        }

        int ret = 0;
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = startY + cache.getLeftSumSpacing() / 2;
                if (!insRight) {
                    y += insHeight / 2;
                }
                right = false;
            }

            IBranchPart subbranch = subBranches.get(i); //get the i st SubTopicPart
            IFigure subFigure = subbranch.getFigure(); //get the i st SubTopicFigure 
            Insets refIns = RadialUtils.getRefInsets(subFigure, right);
//            int hint = y - refIns.top + (refIns.getHeight() + insHeight) / 2;
            int hint;
            if (i < numRight) {
                hint = y - refIns.top + (refIns.getHeight() + insHeight) / 2;
            } else {
                hint = y + refIns.top - (refIns.getHeight() + insHeight) / 2;
            }

            if (i < numRight) {
                if (insRight && childRef.y < hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
                if (i == numRight - 1 && childRef.x > ref.x
                        && childRef.y >= hint)
                    return ret;
            } else { //  on the left 
                if (!insRight && childRef.y > hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
            }
            if (i < numRight)
                y += childrenSpacings[i];
            else
                y -= childrenSpacings[i];

        }
        return withDisabled ? num : -1;
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

}
