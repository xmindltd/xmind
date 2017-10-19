package org.xmind.ui.internal.branch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.IFreeableBranchStructureExtension;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.ILockableBranchStructureExtension;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.tools.ParentSearchKey;
import org.xmind.ui.util.MindMapUtils;

public abstract class BaseRadialStructure extends AbstractBranchStructure
        implements ILockableBranchStructureExtension,
        IFreeableBranchStructureExtension {

    protected static class RadiationInsertion extends Insertion {

        boolean right;

        public RadiationInsertion(IBranchPart parent, int index, Dimension size,
                boolean right) {
            super(parent, index, size);
            this.right = right;
        }

        public void pushIn() {
            super.pushIn();
        }

        public void pullOut() {
            super.pullOut();
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof RadiationInsertion))
                return false;
            RadiationInsertion that = (RadiationInsertion) obj;
            return super.equals(obj) && this.right == that.right;
        }

    }

    public static final String CACHE_NUMBER_RIGHT_BRANCHES = RadialStructure.class
            .getName() + ".numberRightBranches"; //$NON-NLS-1$
    public Set<IBranchPart> calculatingBranches = new HashSet<IBranchPart>();

    public BaseRadialStructure() {
        super();
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof RadialData);
    }

    protected Object createStructureData(IBranchPart branch) {
        return new RadialData(branch);
    }

    protected RadialData getRadialData(IBranchPart branch) {
        return (RadialData) this.getStructureData(branch);
    }

    public boolean isChildLeft(IBranchPart branch, IBranchPart child) {
        if (branch == null)
            return true;

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
            left = index >= getRadialData(branch).getNumRight();
        } else if (branch.getSummaryBranches().contains(child)) {
            left = isSummaryChildLeft(branch, child);
        } else if (branch.getCalloutBranches().contains(child)) {
            left = isChildLeft(branch.getParentBranch(), branch);
        } else {
            left = RadialUtils.isLeft(getReference(branch).x,
                    getReference(child).x);
        }
        calculatingBranches.remove(branch);
        return left;
    }

    public boolean isSummaryChildLeft(IBranchPart branch, IBranchPart child) {
        ISummaryPart summary = MindMapUtils.findAttachedSummary(branch, child);
        if (summary != null) {
            int direction = getSummaryDirection(branch, summary);
            return direction == PositionConstants.WEST;
        }
        return false;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosingBranches = summary.getEnclosingBranches();
        if (!enclosingBranches.isEmpty()) {
            IBranchPart subBranch = enclosingBranches.get(0);
            int index = subBranch.getBranchIndex();
            if (index >= 0) {
                if (index >= getRadialData(branch).getNumRight()) {
                    return PositionConstants.WEST;
                }
            }
        }
        return PositionConstants.EAST;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return PositionConstants.SOUTH;
    }

    public Point getReference(IBranchPart branch) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            return ((IReferencedFigure) topic.getFigure()).getReference();
        }
        return ((IReferencedFigure) branch.getFigure()).getReference();
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        IFigure fig = plusMinus.getFigure();
        Rectangle topicBounds = info.getCheckedClientArea();
        Dimension pmSize = fig.getPreferredSize();
        int pmx = ref.x - pmSize.width / 2;
        int pmy = topicBounds.bottom();
        Rectangle pmBounds = new Rectangle(pmx, pmy, pmSize.width,
                pmSize.height);
        info.put(fig, pmBounds);
    }

    protected Dimension getOffset(IBranchPart subBranch) {
        Point pos = (Point) MindMapUtils.getCache(subBranch,
                IBranchPart.CACHE_PREF_POSITION);
        if (pos != null) {
            return new Dimension(pos.x, pos.y);
        }
        return null;
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
            if (!branch.getSubBranches().isEmpty())
                return getSubTopicPart(branch, 0);
        } else if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
            int numSecond = getRadialData(branch).getNumLeft();
            if (numSecond > 0) {
                int numFirst = getRadialData(branch).getNumRight();
                return getSubTopicPart(branch, numFirst);
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        return isChildLeft(branch, subBranch) ? PositionConstants.EAST
                : PositionConstants.WEST;
    }

    public int getSourceOrientation(IBranchPart branch) {
        return PositionConstants.NONE;
    }

    public boolean isChildrenFreeable(IBranchPart branch) {
        return branch.isCentral();
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        Point parentRef = getReference(branch);
        Point childRef = key.getFigure().getReference();
        Rectangle childBounds = key.getFigure().getBounds();
        Point childAnc = RadialUtils.isLeft(parentRef.x, childRef.x)
                ? childBounds.getRight() : childBounds.getLeft();
        int d = (int) childAnc.getDistance(parentRef);
        Dimension ovalSize = getRadialData(branch).getOvalSize();
        int r = Math.max(ovalSize.width, ovalSize.height);
        if (d <= r + 50)
            return d;
        if (d <= r + 300)
            return d * d;
        return super.calcChildDistance(branch, key);
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        Point ref;
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            ref = ((IReferencedFigure) topic.getFigure()).getReference();
        } else {
            ref = ((IReferencedFigure) branch.getFigure()).getReference();
        }
        return new RadiationInsertion(branch, calcInsIndex(branch, key, true),
                calcInsSize(branch, key), calcInsSide(branch, ref, key));
    }

    public void lock(IBranchPart branch) {
        MindMapUtils.setCache(branch, CACHE_NUMBER_RIGHT_BRANCHES,
                getRadialData(branch).getNumRight());
    }

    public void unlock(IBranchPart branch) {
        MindMapUtils.flushCache(branch, CACHE_NUMBER_RIGHT_BRANCHES);
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
        if (insRight) {
            y -= insHeight / 2;
        }

        int ret = 0;
        for (int i = 0; i < num; i++) {
            if (i == numRight) {
                y = startY - cache.getLeftSumSpacing() / 2;
                if (!insRight) {
                    y -= insHeight / 2;
                }
                right = false;
            }

            IBranchPart subbranch = subBranches.get(i);
            IFigure subFigure = subbranch.getFigure();
            Insets refIns = RadialUtils.getRefInsets(subFigure, right);
            int hint = y - refIns.top + (refIns.getHeight() + insHeight) / 2;
            if (i < numRight) {
                if (insRight && childRef.y < hint)
                    return ret;
                if (withDisabled || subFigure.isEnabled())
                    ret++;
                if (i == numRight - 1 && childRef.x > ref.x
                        && childRef.y >= hint)
                    return ret;
            } else {
                if (!insRight && childRef.y < hint) {
                    return ret;
                }
                if (withDisabled || subFigure.isEnabled())
                    ret++;
            }
            y += childrenSpacings[i];
        }
        return withDisabled ? num : -1;
    }

    private Dimension calcInsSize(IBranchPart branch, ParentSearchKey key) {
        return key.getFigure().getSize();
    }

    private boolean calcInsSide(IBranchPart branch, Point branchRef,
            ParentSearchKey key) {
        Point childRef = key.getFigure().getReference();
        return childRef.x > branchRef.x;
        // if Child on the right of Branch, return true;
    }

    protected RadiationInsertion getCurrentInsertion(IBranchPart branch) {
        IInsertion insertion = super.getCurrentInsertion(branch);
        return insertion instanceof RadiationInsertion
                ? (RadiationInsertion) insertion : null;
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
    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        Point firstLoc = calcFirstChildPosition(branch, key);
        if (subBranches.isEmpty())
            return firstLoc;

        int index = calcInsIndex(branch, key, true);
        RadialData cache = getRadialData(branch);

        int subSize = subBranches.size();
        int right = cache.getNumRight();
        int left = subSize - right;

        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        IBranchPart first = subBranches.get(0);
        Rectangle fBounds = first.getFigure().getBounds();
        if (index == 0) {
            int x = fBounds.x + inventSize.width / 2;
            int y = fBounds.y - (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        if (index == subSize) {
            if (subSize == 1 && isWithinThreshold(first)) {
                if (fBounds.bottom() > 0) {
                    int x = fBounds.x + inventSize.width / 2;
                    int y = fBounds.bottom()
                            + (insSize.height + inventSize.height) / 2;
                    return new Point(x, y);
                } else {
                    return new Point(firstLoc.x, -firstLoc.y);
                }
            }

            if (left == 0)
                return firstLoc.getNegated();

            IBranchPart sub = subBranches.get(subBranches.size() - 1);
            Rectangle bounds = sub.getFigure().getBounds();
            if (left == 1 && bounds.y > 0)
                return new Point(-firstLoc.x, firstLoc.y);

            int x = bounds.right() - inventSize.width / 2;
            int y = bounds.y - (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        if (index == right) {
            boolean isRight = (left == 1 && right == 1)
                    || isRight(subBranches, child, right);

            IBranchPart sub = isRight ? subBranches.get(index - 1)
                    : subBranches.get(index);
            Rectangle bounds = sub.getFigure().getBounds();
            int x;
            if (isRight)
                x = bounds.x + inventSize.width / 2;
            else
                x = bounds.right() - inventSize.width / 2;
            int y = bounds.bottom() + (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        boolean isRight = index < right;

        return calcInventPosition(subBranches.get(isRight ? index - 1 : index),
                subBranches.get(isRight ? index : index - 1), key, isRight);
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

        int right = getRadialData(branch).getNumRight();

        Dimension inventSize = key.getInvent().getSize();
        if (index == oldIndex) {
            if (index == right - 1 && key.getCursorPos().x < 0
                    && (!subBranches.get(right).getFigure().isEnabled()))
                index += 1;
            int delta = getTopicSize(subBranches.get(index)).width / 2
                    - inventSize.width / 2;
            int deltaX = index < right ? -delta : delta;
            return getReference(subBranches.get(index)).getTranslated(deltaX,
                    0);
        }
        return calcInsertPosition(branch, child, key);
    }

    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        int y = -(getMinorSpacing(branch) * 3 / 4 + 8) * 4;
        int x = getRadialData(branch).getX(y, true);

        return getReference(branch).getTranslated(x, y)
                .getTranslated(key.getInvent().getSize().width / 2, 0);
    }

    protected boolean isWithinThreshold(IBranchPart subBranch) {
        // 200 Threshold
        return getBranchWeight(subBranch) < (int) (200 * (Math.log(2) + 1));
    }

    protected int getBranchWeight(IBranchPart branch) {
        // 10 minorSpacing
        if (branch == null)
            return 0;
        IFigure figure = branch.getFigure();
        if (figure == null)
            return 0;
        return figure.getPreferredSize().height + (10 * 3 / 4 + 8) * 2;
    }

    @Override
    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isRightOrUp) {
        Dimension inventSize = key.getInvent().getSize();

        Rectangle oriBounds = orientation.getFigure().getBounds();
        Rectangle assBounds = assist.getFigure().getBounds();

        int x;
        if (isRightOrUp)
            x = (oriBounds.x + assBounds.x) / 2 + inventSize.width / 2;
        else
            x = (oriBounds.right() + assBounds.right()) / 2
                    - inventSize.width / 2;

        int y = (oriBounds.bottom() + assBounds.y) / 2;

        Point loc = new Point(x, y);

        IBranchPart parent = orientation.getParentBranch();
        IFigure figure = parent.getTopicPart().getFigure();

        Rectangle bounds = figure.getBounds();
        if (isRightOrUp) {
            if (bounds.contains(x - inventSize.width / 2,
                    y - inventSize.height / 2)
                    || bounds.contains(x - inventSize.width / 2,
                            y + inventSize.height / 2))
                return loc.getTranslated(
                        bounds.right() - (loc.x - inventSize.width / 2)
                                + getMajorSpacing(parent),
                        0);
        } else {
            if (bounds.contains(x + inventSize.width / 2,
                    y - inventSize.height / 2)
                    || bounds.contains(x + inventSize.width / 2,
                            y + inventSize.height / 2))
                return loc.getTranslated((loc.x - inventSize.width / 2)
                        - bounds.x - getMajorSpacing(parent), 0);
        }

        return loc;
    }

    protected boolean isRight(List<IBranchPart> subBranches, IBranchPart child,
            int right) {
        int rightWeight = 0;
        int leftWeight = 0;
        for (int i = 0; i < right; i++)
            rightWeight += getBranchWeight(subBranches.get(i));

        for (int i = right; i < subBranches.size(); i++)
            leftWeight += getBranchWeight(subBranches.get(i));

        int insWeight = getBranchWeight(child);
        int newRightWeight = rightWeight + insWeight;

        int halfWeight = (rightWeight + leftWeight) / 2;

        return (newRightWeight <= (newRightWeight + leftWeight) / 2)
                || (rightWeight <= leftWeight && (newRightWeight
                        - halfWeight > halfWeight - rightWeight));
    }

}