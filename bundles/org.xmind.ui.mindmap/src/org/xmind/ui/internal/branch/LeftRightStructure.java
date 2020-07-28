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
import org.xmind.gef.draw2d.IAnchor;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.internal.mindmap.TopicPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

public class LeftRightStructure extends AbstractBranchStructure {

    private boolean leftwards;

    private ITransformer t = new HorizontalFlipper();

    protected LeftRightStructure(boolean leftwards) {
        this.leftwards = leftwards;
        this.t.setEnabled(leftwards);
    }

    public boolean isLeftwards() {
        return leftwards;
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        t.setOrigin(ref);
        int y = ref.y;
//        IFigure tf = branch.getTopicPart().getFigure();
//        ITopicDecoration shape = ((TopicFigure) tf).getDecoration();
//        if (Styles.TOPIC_SHAPE_UNDERLINE.equals(shape.getId()))
//            y += tf.getPreferredSize().height / 2;

        Rectangle topicBounds = info.getCheckedClientArea();
        topicBounds = t.tr(topicBounds);
        int x = topicBounds.right();

        IFigure pmFigure = plusMinus.getFigure();
        Dimension size = pmFigure.getPreferredSize();

        Rectangle r = new Rectangle(x, y - size.height / 2, size.width,
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

        int x = refBounds.right() + majorSpacing;

        int num = subBranches.size();

        int anchorHeight = 0;
        TopicPart topicPart = (TopicPart) branch.getTopicPart();
        if (topicPart != null) {
            IAnchor anchor = topicPart.getSourceAnchor(null);
            anchorHeight = (int) anchor
                    .getLocation(leftwards ? PositionConstants.EAST
                            : PositionConstants.WEST, 0)
                    .getDifference(anchor.getReferencePoint()).height;
        }

//        int totalHeight = calcTotalChildrenHeight(branch, minorSpacing, true);
//        int top = totalHeight / 2;
//        int y = ref.y - top;
        int distanceTop = calcDistanceTop(branch, minorSpacing, true);
        int y = ref.y + (int) anchorHeight - distanceTop;
        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);

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
                Rectangle r = new Rectangle(x, y, insSize.width,
                        insSize.height);
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
        if (isLeftwards())
            return PositionConstants.WEST;
        return PositionConstants.EAST;
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
        int dx = childRef.x - topicBounds.right();
        if (dx > 0) {
            if (childRef.y >= branchBounds.y
                    && childRef.y < branchBounds.bottom()
                    && dx < MindMapUI.SEARCH_RANGE) {
                return dx;
            }
            int dy = childRef.y - ref.y;
            int d = dy * dy + dx * dx;
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
        int totalChildrenHeight = calcTotalChildrenHeight(branch, minorSpacing,
                false);
        int totalHeight = totalChildrenHeight + insHeight + minorSpacing;
        int startY = ref.y - totalHeight / 2;
        return calcInsIndex(branch, startY, childRef, insHeight, minorSpacing,
                withDisabled);
    }

    private int calcInsIndex(IBranchPart branch, int startY, Point childRef,
            int childHeight, int spacing, boolean withDisabled) {
        int ret = 0;
        int sum = 0;
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        for (IBranchPart subBranch : subBranches) {
            IFigure subFigure = subBranch.getFigure();
            int h = getBorderedSize(branch, subBranch).height;//subFigure.getPreferredSize().height;
            int hint = startY + sum + (childHeight + h + spacing) / 2;
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

    private int calcDistanceTop(IBranchPart branch, int minorSpacing,
            boolean withInsertion) {
        int totalAnchorHeight = 0;
        int firstDistanceTop = 0;

        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        for (int i = 0; i < num; i++) {
            IBranchPart subBranch = subBranches.get(i);
            int h = getBorderedSize(branch, subBranch).height;

            TopicPart topicPart = (TopicPart) subBranch.getTopicPart();
            int anchorDistanceTop = 0;
            if (topicPart != null) {
                IAnchor anchor = topicPart.getTargetAnchor(null);
                anchorDistanceTop = (int) anchor.getLocation(leftwards
                        ? PositionConstants.EAST : PositionConstants.WEST, 0).y
                        - subBranch.getFigure().getBounds().y;
            }

            if (i == 0) {
                firstDistanceTop = anchorDistanceTop;
                totalAnchorHeight += num == 1 ? 0 : h - anchorDistanceTop;
                continue;
            }

            totalAnchorHeight += minorSpacing;
            if (i == num - 1) {
                totalAnchorHeight += anchorDistanceTop;
                break;
            }

            totalAnchorHeight += h;
        }
        if (withInsertion) {
            IInsertion ins = getCurrentInsertion(branch);
            if (ins != null) {
                Dimension insSize = ins.getSize();
                if (insSize != null) {
                    totalAnchorHeight += minorSpacing + insSize.height;
                }
            }
        }
        return totalAnchorHeight / 2 + firstDistanceTop;

    }

    private int calcTotalChildrenHeight(IBranchPart branch, int minorSpacing,
            boolean withInsertion) {
        int totalHeight = 0;
        Iterator<IBranchPart> it = branch.getSubBranches().iterator();
        while (it.hasNext()) {
            IBranchPart subBranch = it.next();
            int h = getBorderedSize(branch, subBranch).height;
//            System.out.println(h);
            totalHeight += h;//subBranch.getFigure().getPreferredSize().height;
            if (it.hasNext()) {
                totalHeight += minorSpacing;
            }
        }
        if (withInsertion) {
            IInsertion ins = getCurrentInsertion(branch);
            if (ins != null) {
                Dimension insSize = ins.getSize();
                if (insSize != null) {
                    totalHeight += minorSpacing + insSize.height;
                }
            }
        }
        return totalHeight;
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

}