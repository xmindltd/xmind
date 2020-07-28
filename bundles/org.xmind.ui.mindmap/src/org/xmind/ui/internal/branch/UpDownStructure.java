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
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.draw2d.geometry.VerticalFlipper;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class UpDownStructure extends AbstractBranchStructure {

    private boolean upwards;

    private ITransformer t = new VerticalFlipper();

    public UpDownStructure(boolean upwards) {
        this.upwards = upwards;
        this.t.setEnabled(upwards);
    }

    public boolean isUpwards() {
        return upwards;
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        t.setOrigin(ref);
        int x = ref.x;

        Rectangle topicBounds = info.getCheckedClientArea();
        topicBounds = t.tr(topicBounds);
        int y = topicBounds.bottom();

        IFigure pmFigure = plusMinus.getFigure();
        Dimension size = pmFigure.getPreferredSize();
        Rectangle r = new Rectangle(x - size.width / 2, y, size.width,
                size.height);
        info.put(pmFigure, t.rr(r));
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);
        Point ref = info.getReference();
        t.setOrigin(ref);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = t.tr(refBounds);

        int y = refBounds.bottom() + majorSpacing;

        int num = subBranches.size();
        int totalWidth = calcTotalChildrenWidth(branch, minorSpacing, true);

        int left = totalWidth / 2;
        int x = ref.x - left;
        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);

        for (int i = 0; i < num; i++) {
            if (insertion != null && i == insertion.getIndex()) {
                Rectangle r = insertion.createRectangle(x, y);
                info.add(t.rr(r));
                x += r.width + minorSpacing;
            }

            IBranchPart subBranchPart = subBranches.get(i);
            IFigure subBranchFigure = subBranchPart.getFigure();
            Insets ins = helper.getInsets(subBranchPart);

            Dimension size = subBranchFigure.getPreferredSize();
            Rectangle r = new Rectangle(x + ins.left, y + ins.top, size.width,
                    size.height);
            info.put(subBranchFigure, t.rr(r));
            x += size.width + ins.getWidth() + minorSpacing;
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

//    public Object calcNavigation(IBranchPart branch, int direction) {
//        switch (direction) {
//        case PositionConstants.WEST:
//            return NAVI_PREV;
//        case PositionConstants.EAST:
//            return NAVI_NEXT;
//        case PositionConstants.NORTH:
//            return isUpwards() ? NAVI_CHILD : NAVI_PARENT;
//        case PositionConstants.SOUTH:
//            return isUpwards() ? NAVI_PARENT : NAVI_CHILD;
//        }
//        return NAVI_SELF;
//    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
        } else if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
        } else if (!sequential) {
            if (isUpwards()) {
                if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            } else {
                if (GEF.REQ_NAV_UP.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (!branch.getSubBranches().isEmpty()) {
            if (isUpwards()) {
                if (GEF.REQ_NAV_UP.equals(navReqType)) {
                    return getSubTopicPart(branch, 0);
                }
            } else {
                if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
                    return getSubTopicPart(branch, 0);
                }
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public int getSourceOrientation(IBranchPart branch) {
        if (isUpwards())
            return PositionConstants.NORTH;
        return PositionConstants.SOUTH;
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return calcChildTargetOrientation();
    }

//    public int calcChildTargetOrientation(IBranchPart branch,
//            IReferencedFigure childFigure) {
//        return calcChildTargetOrientation();
//    }

    private int calcChildTargetOrientation() {
        if (isUpwards())
            return PositionConstants.SOUTH;
        return PositionConstants.NORTH;
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        IFigure branchFigure = branch.getFigure();
        IReferencedFigure topicFigure = (IReferencedFigure) branch
                .getTopicPart().getFigure();
        Point ref = topicFigure.getReference();
        t.setOrigin(ref);
        Point childRef = t.tp(getChildRef(branch, ref, key));
        Rectangle branchBounds = t.tr(branchFigure.getBounds());
        Rectangle topicBounds = t.tr(topicFigure.getBounds());
        int dy = childRef.y - topicBounds.bottom();
        if (dy > 0) {
            if (childRef.x >= branchBounds.x
                    && childRef.x < branchBounds.right()
                    && dy < MindMapUI.SEARCH_RANGE) {
                return dy;
            }
            int dx = childRef.x - ref.x;
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
        Dimension insSize = calcInsSize(branch, key);
        int insWidth = insSize.width;
        int minorSpacing = getMinorSpacing(branch);
        int totalChildrenWidth = calcTotalChildrenWidth(branch, minorSpacing,
                false);
        int totalWidth = totalChildrenWidth + insWidth + minorSpacing;
        int x = ref.x - totalWidth / 2;
        return calcInsIndex(branch, x, childRef, minorSpacing, insWidth,
                withDisabled);
    }

    private int calcInsIndex(IBranchPart branch, int x, Point childRef,
            int spacing, int childWidth, boolean withDisabled) {
        int ret = 0;
        int sum = 0;
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        for (IBranchPart subBranch : subBranches) {
            IFigure subFigure = subBranch.getFigure();
            int w = subFigure.getPreferredSize().width;
            int hint = x + sum + (childWidth + w + spacing) / 2;
            if (childRef.x < hint) {
                return ret;
            }
            sum += w + spacing;
            if (withDisabled || subFigure.isEnabled())
                ret++;
        }
        return withDisabled ? num : -1;
    }

    private Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize().scale(0.8);
    }

    private int calcTotalChildrenWidth(IBranchPart branch, int minorSpacing,
            boolean withInsertion) {
        int totalWidth = 0;
        Iterator<IBranchPart> it = branch.getSubBranches().iterator();
        while (it.hasNext()) {
            IBranchPart subBranch = it.next();
            totalWidth += subBranch.getFigure().getPreferredSize().width;
            if (it.hasNext()) {
                totalWidth += minorSpacing;
            }
        }
        if (withInsertion) {
            IInsertion ins = getCurrentInsertion(branch);
            if (ins != null) {
                Dimension insSize = ins.getSize();
                if (insSize != null) {
                    totalWidth += minorSpacing + insSize.width;
                }
            }
        }
        return totalWidth;
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key, true),
                calcInsSize(branch, key));
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return upwards ? PositionConstants.NORTH : PositionConstants.SOUTH;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.EAST;
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

        int minorSpacing = getMinorSpacing(branch);
        int index = calcInsIndex(branch, key, true);
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        List<IBoundaryPart> boundaries = branch.getBoundaries();

        if (index == 0) {
            IBranchPart sub = subBranches.get(0);
            Rectangle bounds = sub.getFigure().getBounds();
            if (!boundaries.isEmpty()) {
                for (IBoundaryPart boundary : boundaries) {
                    Rectangle bBounds = boundary.getFigure().getBounds();
                    List<IBranchPart> enclosingBranches = boundary
                            .getEnclosingBranches();
                    if ((!enclosingBranches.isEmpty())
                            && sub.equals(enclosingBranches.get(0)))
                        bounds = bBounds.contains(bounds) ? bBounds : bounds;
                }
            }

            int x = bounds.x - minorSpacing - insSize.width / 2;
            int y;
            if (isUpwards())
                y = bounds.bottom() - inventSize.width / 2;
            else
                y = bounds.y + inventSize.width / 2;

            return new Point(x, y);
        }

        if (index == subBranches.size()) {
            IBranchPart sub = subBranches.get(subBranches.size() - 1);
            Rectangle bounds = sub.getFigure().getBounds();
            if (!boundaries.isEmpty()) {
                for (IBoundaryPart boundary : boundaries) {
                    Rectangle bBounds = boundary.getFigure().getBounds();
                    List<IBranchPart> enclosingBranches = boundary
                            .getEnclosingBranches();
                    if ((!enclosingBranches.isEmpty())
                            && sub.equals(enclosingBranches
                                    .get(enclosingBranches.size() - 1)))
                        bounds = bBounds.contains(bounds) ? bBounds : bounds;
                }
            }

            int x = bounds.right() + minorSpacing + insSize.width / 2;
            int y;
            if (isUpwards())
                y = bounds.bottom() - inventSize.width / 2;
            else
                y = bounds.y + inventSize.width / 2;

            return new Point(x, y);
        }
        return calcInventPosition(subBranches.get(index - 1),
                subBranches.get(index), key, isUpwards());
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

        if (index == oldIndex) {
            IBranchPart sub = subBranches.get(index);
            int delta = sub.getTopicPart().getFigure().getSize().height / 2
                    - inventSize.height / 2;
            return getFigureLocation(sub.getFigure()).getTranslated(0,
                    isUpwards() ? delta : -delta);
        }

        return calcInsertPosition(branch, child, key);
    }

    @Override
    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int y = branch.getFigure().getSize().height / 2
                + getMajorSpacing(branch)
                + key.getInvent().getSize().height / 2;
        return getFigureLocation(branch.getFigure()).getTranslated(0,
                isUpwards() ? -y : y);
    }

    @Override
    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isRightOrUp) {
        int minorSpacing = getMinorSpacing(orientation.getParentBranch());
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        Rectangle oriBounds = orientation.getFigure().getBounds();
        Rectangle assBounds = assist.getFigure().getBounds();

        Rectangle lBounds = oriBounds;
        Rectangle rBounds = assBounds;

        List<IBoundaryPart> boundaries = orientation.getParentBranch()
                .getBoundaries();
        if (!boundaries.isEmpty()) {
            for (IBoundaryPart boundary : boundaries) {
                List<IBranchPart> enclosingBranches = boundary
                        .getEnclosingBranches();
                Rectangle bBounds = boundary.getFigure().getBounds();

                if ((!enclosingBranches.isEmpty()) && orientation.equals(
                        enclosingBranches.get(enclosingBranches.size() - 1)))
                    lBounds = bBounds.contains(lBounds) ? bBounds : lBounds;

                if ((!enclosingBranches.isEmpty())
                        && assist.equals(enclosingBranches.get(0)))
                    rBounds = bBounds.contains(rBounds) ? bBounds : rBounds;
            }
        }

        boolean isBefourBounds;
        Rectangle bounds;
        if (lBounds.equals(oriBounds)) {
            bounds = lBounds;
            isBefourBounds = false;
        } else if (rBounds.equals(assBounds)) {
            bounds = rBounds;
            isBefourBounds = true;
        } else {
            if (isRightOrUp) {
                if (lBounds.bottom() < rBounds.bottom()) {
                    bounds = lBounds;
                    isBefourBounds = false;
                } else {
                    bounds = rBounds;
                    isBefourBounds = true;
                }
            } else {
                if (lBounds.y > rBounds.y) {
                    bounds = lBounds;
                    isBefourBounds = false;
                } else {
                    bounds = rBounds;
                    isBefourBounds = true;
                }
            }
        }

        int x;
        if (isBefourBounds)
            x = bounds.x - minorSpacing - insSize.width / 2;
        else
            x = bounds.right() + minorSpacing + insSize.width / 2;

        int y;
        if (isRightOrUp)
            y = bounds.bottom() - inventSize.height / 2;
        else
            y = bounds.y + inventSize.height / 2;

        return new Point(x, y);
    }
}