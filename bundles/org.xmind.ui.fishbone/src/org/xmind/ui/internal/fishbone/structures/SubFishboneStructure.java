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
package org.xmind.ui.internal.fishbone.structures;

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
import org.xmind.gef.draw2d.IRotatableReferencedFigure;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.HorizontalFlipper;
import org.xmind.gef.draw2d.geometry.ITransformer;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;
import org.xmind.gef.draw2d.geometry.PrecisionLine.Side;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.internal.fishbone.Fishbone;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

@SuppressWarnings("restriction")
public class SubFishboneStructure extends AbstractBranchStructure {

    private static final double sin = Math.sin(Math
            .toRadians(Fishbone.RotateAngle));

    private static final double cos = Math.cos(Math
            .toRadians(Fishbone.RotateAngle));

    private ISubDirection direction;

    private ITransformer t = new HorizontalFlipper();

    public SubFishboneStructure(ISubDirection direction) {
        this.direction = direction;
        this.t.setEnabled(false);
    }

    private double getPadding(IBranchPart branch) {
        return getCastedData(branch).getPadding();
    }

    public void fillLayoutData(IBranchPart branch, ReferencedLayoutData data) {
        super.fillLayoutData(branch, data);
        data.addMargins(new Insets((int) Math.ceil(getPadding(branch))));
    }

    protected void doFillPlusMinus(IBranchPart branch,
            IPlusMinusPart plusMinus, LayoutInfo info) {
        Point ref = info.getReference();
        SubFishboneData fd = getCastedData(branch);
        fd.r1.setOrigin(ref.x, ref.y);
        IFigure figure = plusMinus.getFigure();
        Dimension size = figure.getPreferredSize();
        PrecisionRectangle topicBounds = getNormalTopicBounds(branch, ref);
        int orientation = getSourceOrientation(branch);
        double halfWidth = size.width * 0.5d;
        double halfHeight = size.height * 0.5d;
        double centerX = orientation == PositionConstants.WEST ? topicBounds.x
                - halfWidth : topicBounds.right() + halfWidth;
        PrecisionPoint center = fd.r1.tp(new PrecisionPoint(centerX, ref.y));
        Point loc = center.translate(-halfWidth, -halfHeight).toDraw2DPoint();
        info.put(figure, new Rectangle(loc, size));
    }

    private PrecisionRectangle getNormalTopicBounds(IBranchPart branch,
            Point ref) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null) {
            IFigure figure = topic.getFigure();
            if (figure instanceof IRotatableReferencedFigure)
                return ((IRotatableReferencedFigure) figure)
                        .getNormalPreferredBounds(ref);
        }
        return new PrecisionRectangle(ref.x, ref.y, 0, 0);
    }

    @Override
    protected BoundaryLayoutHelper getBoundaryLayoutHelper(IBranchPart branch) {
        return super.getBoundaryLayoutHelper(branch);
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        Point ref = info.getReference();
        FishboneData fd = getCastedData(branch).getFishboneData();
        for (IBranchPart subBranch : subBranches) {
            IFigure figure = subBranch.getFigure();
            Rectangle rect = fd.getChildPrefBounds(subBranch,
                    new PrecisionPoint(ref));

            if (rect != null)
                info.put(figure, rect);
        }
    }

    @Override
    protected void doFillCalloutBranches(IBranchPart branch,
            List<IBranchPart> calloutBranches, LayoutInfo info) {
        Point ref = info.getReference();
        FishboneData fd = getCastedData(branch).getFishboneData();
        for (IBranchPart calloutBranch : calloutBranches) {
            IFigure figure = calloutBranch.getFigure();
            Rectangle rect = fd.getChildPrefBounds(calloutBranch,
                    new PrecisionPoint(ref));
            if (rect != null)
                info.put(figure, rect);
        }
    }

    protected Object createStructureData(IBranchPart branch) {
        return new SubFishboneData(branch, direction);
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof SubFishboneData);
    }

    protected SubFishboneData getCastedData(IBranchPart branch) {
        return (SubFishboneData) super.getStructureData(branch);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        PrecisionLine boneLine = getBoneLine(branch);
        PrecisionPoint source = calcSourceLocation(branch, boneLine);
        PrecisionLine sourceRay = getBoneRay(source);
        List<IBranchPart> subBranches = branch.getSubBranches();
        boolean folded = branch.isFolded();
//        PrecisionPoint offset = calcChildOffset(branch, key.getFeedback(),
//                boneLine, sourceRay);
        PrecisionLine childBoneLine = getChildBoneLine(branch,
                key.getFeedback());
        double offset = calcChildOffset(boneLine, sourceRay, childBoneLine);
        if (offset > 0) {
            double range;
            if (!subBranches.isEmpty() && !folded) {
                int lastIndex = direction.isChildrenTraverseReversed() ? 0
                        : subBranches.size() - 1;
                IBranchPart lastChild = subBranches.get(lastIndex);
//                PrecisionPoint lastOffset = calcChildOffset(branch, lastChild,
//                        boneLine, sourceRay);
                double lastOffset = calcChildOffset(boneLine, sourceRay,
                        getChildBoneLine(branch, lastChild));
                range = lastOffset + MindMapUI.SEARCH_RANGE;
            } else {
                range = MindMapUI.SEARCH_RANGE;
            }
            if (offset < range) {
                double distance = calcChildDistance(boneLine, childBoneLine);
                if (distance > 0 && distance < MindMapUI.SEARCH_RANGE)
                    return Math.max(1, (int) distance);
            }
        }
        return super.calcChildDistance(branch, key);
    }

    private double calcChildDistance(PrecisionLine boneLine,
            PrecisionLine childLine) {
        PrecisionPoint target = childLine.getOrigin();
        Side childSide = boneLine.getSide(target);
        if (needsCalcChildDistance(childSide))
            return Geometry.getDistance(target, boneLine);
        return -1;
    }

    private double calcChildOffset(PrecisionLine boneLine,
            PrecisionLine sourceRay, PrecisionLine childLine) {
        PrecisionPoint joint = boneLine.intersect(childLine);
        PrecisionDimension offset = joint.getDifference(sourceRay.getOrigin());
        double off = offset.getDiagonal();
        if (!sourceRay.contains(joint)) {
            off = -off;
        }
        return off;
    }

    private boolean needsCalcChildDistance(Side childSide) {
        if (childSide == Side.Right)
            return direction == ISubDirection.NER
                    || direction == ISubDirection.SE
                    || direction == ISubDirection.NW
                    || direction == ISubDirection.SWR;
        if (childSide == Side.Left)
            return direction == ISubDirection.NE
                    || direction == ISubDirection.SER
                    || direction == ISubDirection.NWR
                    || direction == ISubDirection.SW;
        return false;
    }

//    private PrecisionPoint calcChildOffset(IBranchPart branch,
//            IBranchPart child, PrecisionPoint source) {
//        
//        PrecisionPoint target = calcChildTargetLocation(branch, child, source);
//        PrecisionDimension d = target.getDifference(source);
//        SubFishboneStructureData fd = getCastedData(branch);
//        double w = d.height * fd.r1.cos() / fd.r1.sin();
//        double jointOffset = d.width - w;
//        if (direction.isRightHeaded())
//            jointOffset = -jointOffset;
//        double distance = direction.isDownwards() ? d.height : -d.height;
//        PrecisionPoint offset = new PrecisionPoint(jointOffset, distance);
//        return direction.isRotated() ? offset.transpose() : offset;
//    }

    private PrecisionLine getBoneLine(IBranchPart branch) {
        IAnchor anchor = ((INodePart) branch.getTopicPart())
                .getSourceAnchor(branch);
        int orientation = getSourceOrientation(branch);
        PrecisionPoint p1 = anchor.getLocation(
                Geometry.getOppositePosition(orientation), 0);
        PrecisionPoint p2 = anchor.getLocation(orientation, 0);
        return new PrecisionLine(p1, p2, LineType.Line);
    }

    private PrecisionLine getChildBoneLine(IBranchPart branch, IBranchPart child) {
        IAnchor anchor = ((INodePart) child.getTopicPart())
                .getTargetAnchor(branch);
        int orientation = getChildTargetOrientation(branch, child);
        PrecisionPoint p1 = anchor.getLocation(orientation, 0);
        double angle = direction.getSubDirection().getRotateAngle();
        PrecisionPoint p2 = p1.getMoved(Math.toRadians(angle), 100);
        return new PrecisionLine(p1, p2, LineType.Line);
    }

    private PrecisionPoint calcSourceLocation(IBranchPart branch,
            PrecisionLine boneLine) {
        if (!branch.getSubBranches().isEmpty() && !branch.isFolded()
                && direction.isRotated()) {
            PrecisionPoint ref = new PrecisionPoint(((IReferencedFigure) branch
                    .getTopicPart().getFigure()).getReference());
            PrecisionPoint p = ref.getMoved(
                    Math.toRadians(-direction.getRotateAngle()), 100);
            return boneLine.intersect(new PrecisionLine(ref, p, LineType.Line));
        }
        return ((INodePart) branch.getTopicPart()).getSourceAnchor(branch)
                .getLocation(getSourceOrientation(branch), 0);
    }

    private PrecisionLine getBoneRay(PrecisionPoint p) {
        double angle = direction.getRotateAngle();
        if (direction == ISubDirection.SER || direction == ISubDirection.NWR
                || direction == ISubDirection.NW
                || direction == ISubDirection.SW) {
            angle = 180 + angle;
        }
        return new PrecisionLine(p, p.getMoved(Math.toRadians(angle), 100),
                LineType.Ray);
    }

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key, true), key
                .getFigure().getSize());
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        if (branch.getSubBranches().isEmpty() || branch.isFolded())
            return withDisabled ? 0 : -1;

        PrecisionLine boneLine = getBoneLine(branch);
        PrecisionPoint source = calcSourceLocation(branch, boneLine);
        PrecisionLine sourceRay = getBoneRay(source);
        double offset = calcChildOffset(boneLine, sourceRay,
                getChildBoneLine(branch, key.getFeedback()));

        boolean reversed = direction.isChildrenTraverseReversed();
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();
        int ret = 0;
        for (IBranchPart subBranch : subBranches) {
            double subOffset = calcChildOffset(boneLine, sourceRay,
                    getChildBoneLine(branch, subBranch));
            if (reversed) {
                if (offset > subOffset)
                    return ret;
            } else {
                if (offset < subOffset)
                    return ret;
            }
            if (withDisabled || subBranch.getFigure().isEnabled()) {
                ret++;
            }
        }
        return withDisabled ? num : -1;
    }

//    private PrecisionPoint calcSourceLocation(IBranchPart branch) {
////        return new PrecisionPoint(((IReferencedFigure) branch.getTopicPart()
////                .getFigure()).getReference());
//        return ((INodePart) branch.getTopicPart()).getSourceAnchor(branch)
//                .getLocation(calcSourceOrientation(branch), 0);
//    }
//
//    private PrecisionPoint calcChildTargetLocation(IBranchPart branch,
//            IBranchPart child, PrecisionPoint source) {
//        ITopicPart topic = child.getTopicPart();
//        if (topic instanceof INodePart) {
//            IAnchor anchor = ((INodePart) topic).getTargetAnchor(branch);
//            if (anchor != null) {
//                return anchor.getLocation(calcChildTargetOrientation(branch,
//                        child), 0);
//            }
//        }
//        return new PrecisionPoint(getReference(child));
//    }

//    private Point getReference(IBranchPart branch) {
//        ITopicPart topic = branch.getTopicPart();
//        if (topic != null)
//            return ((IReferencedFigure) topic.getFigure()).getReference();
//        return ((IReferencedFigure) branch.getFigure()).getReference();
//    }

//    public Object calcNavigation(IBranchPart branch, int direction) {
//        int nav = this.direction.calcNavigation(direction);
//        if (nav == GEF.NAVI_PREV && branch.getBranchIndex() == 0)
//            nav = GEF.NAVI_PARENT;
//        return nav;
//    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (direction.isRotated()) {
            if (GEF.REQ_NAV_UP.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
            } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
            } else if (!sequential) {
                if (direction.isRightHeaded()) {
                    if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                        return branch.getTopicPart();
                    }
                } else {
                    if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                        return branch.getTopicPart();
                    }
                }
            }
        } else {
            String prevType = direction.isRightHeaded() ? GEF.REQ_NAV_RIGHT
                    : GEF.REQ_NAV_LEFT;
            String nextType = direction.isRightHeaded() ? GEF.REQ_NAV_LEFT
                    : GEF.REQ_NAV_RIGHT;
            if (prevType.equals(navReqType)) {
                ITopicPart prev = getSubTopicPart(branch,
                        sourceChild.getBranchIndex() - 1);
                if (prev == null && !sequential)
                    return branch.getTopicPart();
                return prev;
            } else if (nextType.equals(navReqType)) {
                return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (isNavChild(branch, navReqType)) {
            if (direction.isChildrenTraverseReversed())
                return getSubTopicPart(branch,
                        branch.getSubBranches().size() - 1);
            return getSubTopicPart(branch, 0);
        }
        return super.calcNavigation(branch, navReqType);
    }

    private boolean isNavChild(IBranchPart branch, String navReqType) {
        if (direction.isRotated()) {
            if (direction.isDownwards()) {
                if (GEF.REQ_NAV_DOWN.equals(navReqType))
                    return true;
            } else {
                if (GEF.REQ_NAV_UP.equals(navReqType))
                    return true;
            }
        }
        if (direction.isRightHeaded())
            return GEF.REQ_NAV_LEFT.equals(navReqType);
        return GEF.REQ_NAV_RIGHT.equals(navReqType);
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        if (!branch.getSubBranches().contains(subBranch)) {
            return direction.isRightHeaded() ? PositionConstants.EAST
                    : PositionConstants.WEST;
        }
        return direction.getChildTargetOrientation();
    }

    public int getSourceOrientation(IBranchPart branch) {
        return direction.getSourceOrientation();
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        return getRangeGrowthDirection();
    }

    private int getRangeGrowthDirection() {
        if (direction.isRotated())
            return PositionConstants.SOUTH;
        return direction.isRightHeaded() ? PositionConstants.EAST
                : PositionConstants.WEST;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        if (direction.isRotated())
            return direction.isRightHeaded() ? PositionConstants.EAST
                    : PositionConstants.EAST;
        return direction.isDownwards() ? PositionConstants.SOUTH
                : PositionConstants.NORTH;
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        int rangeGrowthDirection = getRangeGrowthDirection();
        if (direction == rangeGrowthDirection)
            return 1;
        if (direction == Geometry.getOppositePosition(rangeGrowthDirection))
            return -1;
        return super.getQuickMoveOffset(branch, child, direction);
    }

    @Override
    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        if (subBranches.isEmpty())
            return calcFirstChildPosition(branch, key);

        int index = calcInsIndex(branch, key, true);

        if (index == subBranches.size()) {
            return calcInventPosition(subBranches.get(index - 1), null, key,
                    false);
        }
        return calcInventPosition(subBranches.get(index), null, key, true);
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
        Dimension inventSize = key.getInvent().getSize();

        if (index == oldIndex) {
            if (direction.isRotated()) {
                IBranchPart sub = subBranches.get(index);
                Dimension size = sub.getTopicPart().getFigure().getSize();
                int deltaX = (size.width - inventSize.width) / 2;

                return getFigureLocation(sub.getFigure()).getTranslated(
                        direction.isRightHeaded() ? deltaX : -deltaX, 0);
            } else {
                IBranchPart sub = subBranches.get(index);
                Dimension size = sub.getTopicPart().getFigure().getSize();

                double w = (size.height * sin - size.width * cos)
                        / (sin * sin - cos * cos);

                double deltaX = size.width * 0.5d - w * cos + inventSize.width
                        * 0.5d + inventSize.width * cos * 0.5d;
                double deltaY = direction.isDownwards() ? (-size.height + inventSize.width
                        * sin) * 0.5d
                        : (size.height - inventSize.width * sin) * 0.5d;

                return getFigureLocation(sub.getFigure()).getTranslated(
                        direction.isRightHeaded() ? -deltaX : deltaX, deltaY);
            }
        }

        return calcInsertPosition(branch, child, key);
    }

    @Override
    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        if (direction.isRotated())
            return calcFirstNormalPosition(branch, key);
        else
            return clacFirstRotatedPosition(branch, key);
    }

    private Point calcFirstNormalPosition(IBranchPart branch,
            ParentSearchKey key) {
        Dimension size = branch.getTopicPart().getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        double h = (size.width * sin - size.height * cos)
                / (sin * sin - cos * cos);
        double x = h * sin * 0.5d + inventSize.height * cos / sin
                + inventSize.width * 0.5d;

        return getFigureLocation(branch.getFigure()).getTranslated(
                direction.isRightHeaded() ? -x : x,
                direction.isDownwards() ? inventSize.height * 0.5d
                        : -inventSize.height * 0.5d);
    }

    private Point clacFirstRotatedPosition(IBranchPart branch,
            ParentSearchKey key) {
        Dimension size = branch.getTopicPart().getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();

        double x = size.width * 0.5d + key.getFigure().getSize().height * sin
                * 0.5d + inventSize.width * (1 + cos) * 0.5d;

        return getFigureLocation(branch.getFigure()).getTranslated(
                direction.isRightHeaded() ? -x : x,
                size.height
                        * 0.5d
                        + (direction.isDownwards() ? inventSize.width * sin
                                * 0.5d : -inventSize.width * sin * 0.5d));
    }

    @Override
    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key, boolean isBeforeOrientation) {
        if (direction.isRotated())
            return calcNormalPosition(orientation, key, isBeforeOrientation);
        else
            return calcRotatedPosition(orientation, key, isBeforeOrientation);

    }

    private Point calcNormalPosition(IBranchPart orientation,
            ParentSearchKey key, boolean isBeforeOrientation) {
        Dimension subSize = orientation.getTopicPart().getFigure().getSize();
        Point loc = getFigureLocation(orientation.getTopicPart().getFigure());
        Rectangle bounds = orientation.getFigure().getBounds();
        int top = bounds.y;
        int bottom = bounds.bottom();
        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();
        double cot = cos / sin;

        double x;

        double right = loc.x + (subSize.width - inventSize.width) * 0.5d;
        double left = loc.x - (subSize.width - inventSize.width) * 0.5d;
        if (isBeforeOrientation) {
            if (direction.equals(ISubDirection.NWR)) {
                x = right
                        - (loc.y - top - subSize.height * 0.5d + (insSize.height + inventSize.height) * 0.5d)
                        * cot;
            } else if (direction.equals(ISubDirection.SWR)) {
                x = right
                        + (loc.y - top + subSize.height * 0.5d + (insSize.height - inventSize.height) * 0.5d)
                        * cot;
            } else if (direction.equals(ISubDirection.NER)) {
                x = left
                        + (loc.y - top - subSize.height * 0.5 + (insSize.height + inventSize.height) * 0.5d)
                        * cot;
            } else {
                x = left
                        - (loc.y - top + subSize.height * 0.5 + (insSize.height - inventSize.height) * 0.5d)
                        * cot;
            }
        } else {
            if (direction.equals(ISubDirection.NWR)) {
                x = right
                        + (bottom - loc.y + subSize.height * 0.5d + (insSize.height - inventSize.height) * 0.5d)
                        * cot;
            } else if (direction.equals(ISubDirection.SWR)) {
                x = right
                        - (bottom - loc.y - subSize.height * 0.5d + (insSize.height + inventSize.height) * 0.5d)
                        * cot;
            } else if (direction.equals(ISubDirection.NER)) {
                x = left
                        - (top - loc.y + subSize.height * 0.5 + (insSize.height - inventSize.height) * 0.5d)
                        * cot;
            } else {
                x = left
                        + (bottom - loc.y - subSize.height * 0.5 + (insSize.height + inventSize.height) * 0.5d)
                        * cot;
            }
        }

        double y;
        if (isBeforeOrientation)
            y = top - (insSize.height) * 0.5d;
        else
            y = bottom + (insSize.height) * 0.5d;

        return new Point().getTranslated(x, y);
    }

    private Point calcRotatedPosition(IBranchPart orientation,
            ParentSearchKey key, boolean isBeforeOrientation) {
        double baseY = getBoneLine(orientation.getParentBranch()).getOrigin().y;
        Dimension inventSize = key.getInvent().getSize();
        Dimension insSize = key.getFigure().getSize();

        double deltaY = inventSize.width * sin * 0.5d;
        double y = baseY + (direction.isDownwards() ? deltaY : -deltaY);

        double x;
        if (isBeforeOrientation) {
            double offset = calcBeforeOffset(orientation);

            double deltaX = (insSize.height / sin - inventSize.width * cos - inventSize.width) * 0.5d;

            x = offset + (direction.isRightHeaded() ? deltaX : -deltaX);
        } else {
            Rectangle pBounds = orientation.getParentBranch().getFigure()
                    .getBounds();

            x = direction.isRightHeaded() ? pBounds.x
                    - (inventSize.width * cos + inventSize.width) * 0.5
                    : pBounds.right()
                            + (inventSize.width * cos + inventSize.width) * 0.5;
        }

        return new Point().getTranslated(x, y);
    }

    private double calcBeforeOffset(IBranchPart branch) {
        double offset = 0.0d;
        PrecisionLine boneLine = getBoneLine(branch.getParentBranch());

        double cot = cos / sin;

        List<IBranchPart> callouts = branch.getCalloutBranches();
        if (callouts == null || callouts.isEmpty()) {
            IFigure figure = branch.getTopicPart().getFigure();
            Rectangle bounds = figure.getBounds();
            double height = ((TopicFigure) figure)
                    .getNormalPreferredBounds(new Point()).height;
            double delta = height * cos * cot;
            offset = direction.isRightHeaded() ? bounds.right() + delta
                    : bounds.x - delta;
        } else {
            double y = boneLine.getOrigin().y;
            Rectangle bounds = branch.getFigure().getBounds();
            offset = direction.isRightHeaded() ? bounds.right() : bounds.x;

            for (IBranchPart callout : callouts) {
                Rectangle calloutBounds = callout.getFigure().getBounds();
                if (direction.equals(ISubDirection.NE)) {
                    Point tl = calloutBounds.getTopLeft();
                    offset = Math.min(offset, tl.x - (y - tl.y) * cot);
                } else if (direction.equals(ISubDirection.SE)) {
                    Point bl = calloutBounds.getBottomLeft();
                    offset = Math.min(offset, bl.x - (bl.y - y) * cot);
                } else if (direction.equals(ISubDirection.NW)) {
                    Point tr = calloutBounds.getTopRight();
                    offset = Math.max(offset, tr.x + (y - tr.y) * cot);
                } else {
                    Point br = calloutBounds.getBottomRight();
                    offset = Math.max(offset, br.x + (br.y - y) * cot);
                }
            }
        }

        return offset;
    }
}