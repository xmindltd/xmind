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

public class TreeStructure extends AbstractBranchStructure {

    private boolean leftwards;

    private ITransformer t = new HorizontalFlipper();

    protected TreeStructure(boolean leftwards) {
        this.leftwards = leftwards;
        this.t.setEnabled(leftwards);
    }

    public boolean isLeftwards() {
        return leftwards;
    }

    protected void doFillPlusMinus(IBranchPart branch,
            IPlusMinusPart plusMinus, LayoutInfo info) {
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
        info.put(pmFigure, t.r(r));
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {
        int majorSpacing = getMajorSpacing(branch);
        int minorSpacing = getMinorSpacing(branch);
        Point ref = info.getReference();
        t.setOrigin(ref);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = t.tr(refBounds);

        int x = ref.x + majorSpacing;
        int y = refBounds.bottom() + majorSpacing;

        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
        int num = subBranches.size();
        for (int i = 0; i < num; i++) {
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
            y += size.height + ins.getHeight() + minorSpacing;
        }

        if (insertion != null && num == insertion.getIndex()) {
            Dimension insSize = insertion.getSize();
            if (insSize != null) {
                Rectangle r = new Rectangle(x, y, insSize.width, insSize.height);
                info.add(t.rr(r));
            }
        }
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (!branch.getSubBranches().isEmpty()) {
            if (isLeftwards()) {
                if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                    return getSubTopicPart(branch, 0);
                }
            } else {
                if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                    return getSubTopicPart(branch, 0);
                }
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
            if (isLeftwards()) {
                if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                    return branch.getTopicPart();
                }
            } else {
                if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
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
        return calcChildTargetOrientation();
    }

    private int calcChildTargetOrientation() {
        if (isLeftwards())
            return PositionConstants.EAST;
        return PositionConstants.WEST;
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
        Rectangle childBounds = t.tr(key.getFigure().getBounds());
        int dx = childRef.x - ref.x;
        int dy = childRef.y - topicBounds.bottom();
        if (dy > 0 && childBounds.x >= ref.x) {
            if (childRef.x < branchBounds.right() + MindMapUI.SEARCH_RANGE / 2) {
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
        int y = topicBounds.bottom();
        if (!branch.getSubBranches().isEmpty()) {
            IPlusMinusPart plusMinus = branch.getPlusMinus();
            if (plusMinus != null) {
                y += plusMinus.getFigure().getSize().height;
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
//            int h = subFigure.getPreferredSize().height;
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
        if (leftwards)
            return PositionConstants.WEST;
        return PositionConstants.EAST;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
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
        return getFigureLocation(branch.getFigure()).getTranslated(
                isLeftwards() ? -x : x,
                branch.getFigure().getSize().height / 2
                        + getMajorSpacing(branch)
                        + key.getFigure().getSize().height / 2);
    }

}