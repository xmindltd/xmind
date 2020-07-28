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
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionInsets;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.AbstractBranchStructure;
import org.xmind.ui.branch.BoundaryLayoutHelper;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.branch.Insertion;
import org.xmind.ui.internal.figures.TopicFigure;
import org.xmind.ui.internal.fishbone.Fishbone;
import org.xmind.ui.internal.fishbone.decorations.MainFishboneBranchDecoration;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.IPlusMinusPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.ParentSearchKey;

@SuppressWarnings("restriction")
public class MainFishboneStructure extends AbstractBranchStructure {

    private static final double sin = Math
            .sin(Math.toRadians(Fishbone.RotateAngle));

    private static final double cos = Math
            .cos(Math.toRadians(Fishbone.RotateAngle));

    private static final double fMajor = 0.5d;
    private static final double fMinor = 5d;

    private final IMainDirection direction;

    public MainFishboneStructure(IMainDirection direction) {
        this.direction = direction;
    }

    protected void addExtraSpaces(IBranchPart branch,
            ReferencedLayoutData data) {
        super.addExtraSpaces(branch, data);
        Insets insets = new Insets();
        if (direction == IMainDirection.LeftHeaded) {
            insets.right = MainFishboneBranchDecoration.TailLength + 5;
        } else {
            insets.left = MainFishboneBranchDecoration.TailLength + 5;
        }
        data.addMargins(insets);
    }

    protected void doFillPlusMinus(IBranchPart branch, IPlusMinusPart plusMinus,
            LayoutInfo info) {
        Point ref = info.getReference();
        MainFishboneData fd = getCastedData(branch);
        fd.setOrigin(ref);

        Rectangle topicBounds = info.getCheckedClientArea();
        topicBounds = fd.hf.tr(topicBounds);

        IFigure figure = plusMinus.getFigure();
        Dimension size = figure.getPreferredSize();

        int x = topicBounds.right() + 1;
        int y = ref.y - size.height / 2;

        Rectangle r = new Rectangle(x, y, size.width, size.height);
        info.put(figure, fd.hf.rr(r));
    }

    protected void doFillSubBranches(IBranchPart branch,
            List<IBranchPart> subBranches, LayoutInfo info) {

        double mainSpacing = getMajorSpacing(branch) * fMajor;
        double subSpacing = getMinorSpacing(branch) * fMinor;

        Point ref = info.getReference();
        MainFishboneData fd = getCastedData(branch);
        fd.setOrigin(ref);

        Rectangle refBounds = info.getCheckedClientArea();
        refBounds = fd.hf.tr(refBounds);

        fd.upSide.start(refBounds.right() + mainSpacing);
        fd.downSide.start(fd.upSide.right + subSpacing);

        int num = subBranches.size();

        IInsertion insertion = getCurrentInsertion(branch);
        BoundaryLayoutHelper helper = getBoundaryLayoutHelper(branch);
//        int y = ref.y;
        Side lastSide = fd.downSide;
        double width = 0.0;
        for (int i = 0; i < num; i++) {
            IBranchPart subBranch = subBranches.get(i);
            IFigure subBranchFigure = subBranch.getFigure();

            Insets ins = helper.getInsets(subBranch);

            boolean upwards = getCastedData(branch).isUpwardBranch(i);
            Side side = upwards ? fd.upSide : fd.downSide;

            IPrecisionTransformer pvf = fd.pvf;
            pvf.setEnabled(!upwards);

//            PrecisionInsets fChildBorder = pvf.ti(fd.phf
//                    .ti(new PrecisionInsets(subBranchFigure.getInsets())));
            PrecisionInsets fChildBorder = pvf
                    .ti(fd.phf.ti(new PrecisionInsets(ins)));

            if (fChildBorder.left != 0) {
                side.useRight = false;
            }

            PrecisionRectangle childBounds = new PrecisionRectangle();
            double joint;
            double rotatedBottom;

            IBranchPolicy branchPolicy = subBranch.getBranchPolicy();
            IStructure sa = branchPolicy.getStructure(subBranch);
            FishboneData sub = getFishboneData(subBranch, sa);

            if (sub != null) {
                PrecisionInsets fChildBranchRotated = pvf
                        .ti(fd.phf.ti(sub.rBranchRefIns));
                PrecisionInsets fChildBranchNormal = fd.phf
                        .ti(sub.branchRefIns);
                PrecisionInsets fChildTopicNormal = fd.phf.ti(sub.topicRefIns);

                double left = fChildBranchRotated.left + fChildBorder.left;
                double bottom = fChildBranchRotated.bottom + mainSpacing
                        + fChildBorder.bottom;

                double jointOff = left
                        - (bottom * cos - fChildTopicNormal.bottom) / sin;

                if (side.useRight) {
                    double rotatedSpacing = (fChildBranchNormal.top
                            + fChildTopicNormal.bottom) / sin;
                    joint = side.rotatedBottom + rotatedSpacing;
                } else {
                    joint = Math.max(side.right, side.right + jointOff);
                }
                if (!upwards || i > 0) {
                    joint = Math.max(joint, lastSide.lastJoint + subSpacing);
                }

                Dimension size = subBranchFigure.getPreferredSize();
                childBounds.setSize(size.width, size.height);

                childBounds.x = joint - jointOff;
                if (side == fd.upSide) {
                    childBounds.y = ref.y - size.height - mainSpacing
                            - fChildBorder.bottom;
                } else {
                    childBounds.y = ref.y + mainSpacing + fChildBorder.bottom;///////////
                }
                if (fChildBorder.getHeight() != 0)
                    if (width <= childBounds.right())
                        width = childBounds.right();

                rotatedBottom = joint
                        + (fChildBranchNormal.bottom - fChildTopicNormal.bottom)
                                / sin
                        + subSpacing;
            } else {
                PrecisionInsets childBranchNormal = new PrecisionInsets(
                        ((IReferencedFigure) subBranchFigure)
                                .getReferenceDescription());
                PrecisionInsets fChildBranchNormal = pvf
                        .ti(fd.phf.ti(childBranchNormal));
                double bottom = fChildBranchNormal.bottom + mainSpacing;
                double rotatedSpacing = fChildBranchNormal.top * cos / sin;
                if (side.useRight) {
                    joint = side.rotatedBottom + rotatedSpacing;
                } else {
                    joint = side.right;
                }
                if (!upwards || i > 0) {
                    joint = Math.max(joint, lastSide.lastJoint + subSpacing);
                }
                double jointOff = bottom * cos / sin;
                Dimension size = subBranchFigure.getPreferredSize();
                childBounds.setSize(size.width, size.height);

                childBounds.x = joint + jointOff;
                if (side == fd.upSide) {
                    childBounds.y = ref.y - size.height - mainSpacing
                            - fChildBorder.bottom;////
                } else {
                    childBounds.y = ref.y + mainSpacing + fChildBorder.bottom;//
                }

                if (fChildBorder.getHeight() != 0)
                    if (width <= childBounds.right())
                        width = childBounds.right();

                rotatedBottom = joint + fChildBranchNormal.getWidth()
                        + fChildBranchNormal.bottom * cos / sin + subSpacing;
            }

            if (insertion != null && i >= insertion.getIndex()) {
                childBounds.x += insertion.getSize().width;
            }

            IPrecisionTransformer phf = fd.phf;
            PrecisionRectangle precRect = phf.rr(childBounds);
            Rectangle rect = precRect.toDraw2DRectangle();

            info.put(subBranchFigure, rect);

            side.lastJoint = joint;
            side.useRight = fChildBorder.right == 0;
            side.rotatedBottom = rotatedBottom;

            if (side.useRight)
                side.right = childBounds.right() + subSpacing / 3;
            else
                side.right = width + subSpacing / 3;
            lastSide = side;
        }
    }

    private FishboneData getFishboneData(IBranchPart subBranch, IStructure sa) {
        if (sa instanceof SubFishboneStructure) {
            return ((SubFishboneStructure) sa).getCastedData(subBranch)
                    .getFishboneData();
        }
        return null;
    }

    protected Object createStructureData(IBranchPart branch) {
        return new MainFishboneData(branch, direction.isTransformerEnabled());
    }

    protected boolean isValidStructureData(IBranchPart branch, Object data) {
        return super.isValidStructureData(branch, data)
                && (data instanceof MainFishboneData);
    }

    protected MainFishboneData getCastedData(IBranchPart branch) {
        return (MainFishboneData) super.getStructureData(branch);
    }

    public int calcChildDistance(IBranchPart branch, ParentSearchKey key) {
        Point childRef = key.getFigure().getReference();
        Point branchRef = getReference(branch);
        MainFishboneData fd = getCastedData(branch);
        fd.setOrigin(branchRef);
        PrecisionPoint source = calcSourceAnchorLocation(branch);
        int jointOffset = calcChildJointOffset(branch, key.getFeedback(),
                source);
        if (jointOffset > 0) {
            int range;
            Rectangle childrenNodesBounds = getChildrenNodesBounds(branch);
            if (childrenNodesBounds != null) {
                Rectangle r = fd.hf.tr(childrenNodesBounds);
                range = r.right() - branchRef.x + MindMapUI.SEARCH_RANGE / 2;
            } else {
                range = MindMapUI.SEARCH_RANGE;
            }
            if (jointOffset < range) {
                int dy = Math.abs(childRef.y - branchRef.y);
                if (dy < MindMapUI.SEARCH_RANGE)
                    return dy;
            }
        }
        return super.calcChildDistance(branch, key);
    }

    private PrecisionPoint calcSourceAnchorLocation(IBranchPart branch) {
        return ((INodePart) branch.getTopicPart()).getSourceAnchor(branch)
                .getLocation(getSourceOrientation(branch), 0);
    }

    private Rectangle getChildrenNodesBounds(IBranchPart branch) {
        Rectangle r = null;
        for (IBranchPart subbranch : branch.getSubBranches()) {
            r = Geometry.union(r,
                    subbranch.getTopicPart().getFigure().getBounds());
        }
        return r;
    }

    private int calcChildJointOffset(IBranchPart branch, IBranchPart child,
            PrecisionPoint source) {
        double angle = calcChildRotateAngle(branch, child);
        PrecisionPoint target = calcChildTargetLocation(branch, child, source);
        PrecisionDimension d = target.getDifference(source);
        int w = (int) Math
                .floor(d.height / Math.tan(Math.toRadians(angle)) + 0.0000001);
        int offset = (int) Math.floor(d.width - w + 0.0000001);
        if (direction == IMainDirection.RightHeaded)
            return -offset;
        return offset;
    }

    private PrecisionPoint calcChildTargetLocation(IBranchPart branch,
            IBranchPart child, PrecisionPoint source) {
        ITopicPart topic = child.getTopicPart();
        if (topic instanceof INodePart) {
            IAnchor anchor = ((INodePart) topic).getTargetAnchor(branch);
            if (anchor != null) {
                return anchor.getLocation(
                        getChildTargetOrientation(branch, child), 0);
            }
        }
        return new PrecisionPoint(getReference(child));
    }

    public boolean isChildUpwards(IBranchPart branch, IBranchPart child) {
        return isChildUpwards(branch, child,
                branch.getSubBranches().indexOf(child));
    }

    private boolean isChildUpwards(IBranchPart branch, IBranchPart child,
            int childIndex) {
        if (childIndex < 0) {
            Point branchRef = getReference(branch);
            Point childRef = getReference(child);
            return childRef.y < branchRef.y;
        }
        return getCastedData(branch).isUpwardBranch(childIndex);
    }

    private Point getReference(IBranchPart branch) {
        ITopicPart topic = branch.getTopicPart();
        if (topic != null)
            return ((IReferencedFigure) topic.getFigure()).getReference();
        return ((IReferencedFigure) branch.getFigure()).getReference();
    }

    private double calcChildRotateAngle(IBranchPart branch, IBranchPart child) {
        return isChildUpwards(branch, child)
                ? direction.getUpRotated().getRotateAngle()
                : direction.getDownRotated().getRotateAngle();
//        return direction.getChildRotateAngle(isChildUpwards(branch, child));
    }

    protected int calcInsIndex(IBranchPart branch, ParentSearchKey key,
            boolean withDisabled) {
        if (branch.getSubBranches().isEmpty() || branch.isFolded())
            return withDisabled ? 0 : -1;
        PrecisionPoint source = calcSourceAnchorLocation(branch);
        int jointOffset = calcChildJointOffset(branch, key.getFeedback(),
                source);
        List<IBranchPart> subBranches = branch.getSubBranches();
        int num = subBranches.size();

        IInsertion lastInsertion = getCurrentInsertion(branch);
        int ret = 0;
        for (int i = 0; i < num; i++) {
            IBranchPart subBranch = subBranches.get(i);
            int j = calcChildJointOffset(branch, subBranch, source);
            if (lastInsertion != null && i >= lastInsertion.getIndex()) {
                j -= lastInsertion.getSize().width;
            }

            int hint = j;
            if (jointOffset < hint)
                return ret;
            if (withDisabled || subBranch.getFigure().isEnabled())
                ret++;
        }
        return withDisabled ? num : -1;
    }

    private static final Dimension INSERTION_SIZE = new Dimension(30, 1);

    public IInsertion calcInsertion(IBranchPart branch, ParentSearchKey key) {
        return new Insertion(branch, calcInsIndex(branch, key, true),
                INSERTION_SIZE);
    }

    public IPart calcChildNavigation(IBranchPart branch,
            IBranchPart sourceChild, String navReqType, boolean sequential) {
        if (GEF.REQ_NAV_DOWN.equals(navReqType)
                || GEF.REQ_NAV_UP.equals(navReqType)) {
            int childIndex = sourceChild.getBranchIndex();
            boolean upwards = isChildUpwards(branch, sourceChild, childIndex);
            if ((upwards && GEF.REQ_NAV_DOWN.equals(navReqType))
                    || (!upwards && GEF.REQ_NAV_UP.equals(navReqType))) {
                for (int i = childIndex - 1; i >= 0; i--) {
                    IBranchPart sub = branch.getSubBranches().get(i);
                    if (isChildUpwards(branch, sub, i) != upwards)
                        return sub.getTopicPart();
                }
                return getSubTopicPart(branch, childIndex + 1);
            }
        } else {
            boolean next = (direction == IMainDirection.LeftHeaded
                    && GEF.REQ_NAV_RIGHT.equals(navReqType))
                    || (direction == IMainDirection.RightHeaded
                            && GEF.REQ_NAV_LEFT.equals(navReqType));
            boolean prev = (direction == IMainDirection.LeftHeaded
                    && GEF.REQ_NAV_LEFT.equals(navReqType))
                    || (direction == IMainDirection.RightHeaded
                            && GEF.REQ_NAV_RIGHT.equals(navReqType));
            if (next || prev) {
                int childIndex = sourceChild.getBranchIndex();
                boolean upwards = isChildUpwards(branch, sourceChild,
                        childIndex);
                for (int i = prev ? childIndex - 1 : childIndex + 1; prev
                        ? i >= 0 : i < branch.getSubBranches().size();) {
                    IBranchPart sub = branch.getSubBranches().get(i);
                    if (isChildUpwards(branch, sourceChild, i) == upwards) {
                        return sub.getTopicPart();
                    }
                    if (prev) {
                        i--;
                    } else {
                        i++;
                    }
                }
                if (prev && !sequential) {
                    IPart prevTopic = getSubTopicPart(branch, childIndex - 1);
                    if (prevTopic != null)
                        return prevTopic;
                    return branch.getTopicPart();
                }
                return getSubTopicPart(branch, childIndex + 1);
            }
        }
        return super.calcChildNavigation(branch, sourceChild, navReqType,
                sequential);
    }

    public IPart calcNavigation(IBranchPart branch, String navReqType) {
        if (direction == IMainDirection.RightHeaded) {
            if (GEF.REQ_NAV_LEFT.equals(navReqType)) {
                return getSubTopicPart(branch, 0);
            }
        } else {
            if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
                return getSubTopicPart(branch, 0);
            }
        }
        return super.calcNavigation(branch, navReqType);
    }

    public int getChildTargetOrientation(IBranchPart branch,
            IBranchPart subBranch) {
        if (branch != null) {
            int index = branch.getSubBranches().indexOf(subBranch);
            if (index < 0)
                return direction == IMainDirection.RightHeaded
                        ? PositionConstants.EAST : PositionConstants.WEST;
            if (isChildUpwards(branch, subBranch, index)) {
                if (direction == IMainDirection.RightHeaded)
                    return PositionConstants.EAST;
                return PositionConstants.WEST;
            }
        }
        if (direction == IMainDirection.RightHeaded)
            return PositionConstants.WEST;
        return PositionConstants.EAST;
    }

    public int getSourceOrientation(IBranchPart branch) {
        if (direction == IMainDirection.RightHeaded)
            return PositionConstants.WEST;
        return PositionConstants.EAST;
    }

    public int getRangeGrowthDirection(IBranchPart branch,
            IBranchRangePart range) {
        if (direction == IMainDirection.RightHeaded)
            return PositionConstants.WEST;
        return PositionConstants.EAST;
    }

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        List<IBranchPart> enclosing = summary.getEnclosingBranches();
        if (!enclosing.isEmpty()) {
            if (getCastedData(branch)
                    .isUpwardBranch(enclosing.get(0).getBranchIndex()))
                return PositionConstants.NORTH;
            return PositionConstants.SOUTH;
        }
        return PositionConstants.NORTH;
    }

    public int getQuickMoveOffset(IBranchPart branch, IBranchPart child,
            int direction) {
        if (direction == PositionConstants.EAST) {
            if (this.direction == IMainDirection.RightHeaded)
                return -1;
            return 1;
        } else if (direction == PositionConstants.WEST) {
            if (this.direction == IMainDirection.RightHeaded)
                return 1;
            return -1;
        }
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
        boolean upWard = (index + 1) % 2 == 0;
        boolean leftWard = IMainDirection.LeftHeaded.equals(direction);

        if (index == oldIndex) {
            IBranchPart sub = subBranches.get(index);
            Dimension size = sub.getTopicPart().getFigure().getSize();

            double w = (size.height * sin - size.width * cos)
                    / (sin * sin - cos * cos);

            double deltaX = size.width * 0.5d - w * cos
                    + inventSize.width * 0.5d + inventSize.width * cos * 0.5d;
            double deltaY = upWard
                    ? (-size.height + inventSize.width * sin) * 0.5d
                    : (size.height - inventSize.width * sin) * 0.5d;

            return getReference(sub).getTranslated(leftWard ? deltaX : -deltaX,
                    deltaY);
        }

        return calcInsertPosition(branch, child, key);
    }

    @Override
    protected Point calcInventPosition(IBranchPart orientation,
            IBranchPart assist, ParentSearchKey key,
            boolean isBeforeOrientation) {
        Dimension inventSize = key.getInvent().getSize();
        Dimension insSize = key.getFigure().getSize();

        double deltaY = inventSize.width * sin * 0.5d;

        boolean upwards = isChildUpwards(orientation.getParentBranch(),
                orientation);

        double y = upwards ^ isBeforeOrientation ? deltaY : -deltaY;

        double x;
        if (isBeforeOrientation) {
            double offset = calcBeforeOffset(orientation);

            double deltaX = (insSize.height / sin - inventSize.width * cos
                    - inventSize.width) * 0.5d;

            x = offset + (direction.equals(IMainDirection.RightHeaded) ? deltaX
                    : -deltaX);
        } else {
            Rectangle pBounds = orientation.getParentBranch().getFigure()
                    .getBounds();

            x = direction.equals(IMainDirection.RightHeaded) ? pBounds.x
                    : pBounds.right();
        }

        return new Point().getTranslated(x, y);
    }

    private double calcBeforeOffset(IBranchPart branch) {
        double offset = 0.0d;
        double cot = cos / sin;

        List<IBranchPart> callouts = branch.getCalloutBranches();
        if (callouts == null || callouts.isEmpty()) {
            IFigure figure = branch.getTopicPart().getFigure();
            Rectangle bounds = figure.getBounds();
            double height = ((TopicFigure) figure)
                    .getNormalPreferredBounds(new Point()).height;
            double delta = height * cos * cot;
            offset = direction.equals(IMainDirection.RightHeaded)
                    ? bounds.right() + delta : bounds.x - delta;
        } else {
            Rectangle bounds = branch.getFigure().getBounds();
            offset = direction.equals(IMainDirection.RightHeaded)
                    ? bounds.right() : bounds.x;

            for (IBranchPart callout : callouts) {
                Rectangle calloutBounds = callout.getFigure().getBounds();
                if (direction.equals(ISubDirection.NE)) {
                    Point tl = calloutBounds.getTopLeft();
                    offset = Math.min(offset, tl.x - (-tl.y) * cot);
                } else if (direction.equals(ISubDirection.SE)) {
                    Point bl = calloutBounds.getBottomLeft();
                    offset = Math.min(offset, bl.x - (bl.y) * cot);
                } else if (direction.equals(ISubDirection.NW)) {
                    Point tr = calloutBounds.getTopRight();
                    offset = Math.max(offset, tr.x + (tr.y) * cot);
                } else {
                    Point br = calloutBounds.getBottomRight();
                    offset = Math.max(offset, br.x + (br.y) * cot);
                }
            }
        }

        return offset;
    }

    @Override
    protected Point calcFirstChildPosition(IBranchPart branch,
            ParentSearchKey key) {
        double minorSpacing = getMinorSpacing(branch) * fMinor;
        double x = branch.getFigure().getSize().width * 0.5d
                + key.getFigure().getSize().width * 0.5d;
        return getFigureLocation(branch.getFigure()).getTranslated(
                IMainDirection.LeftHeaded.equals(direction) ? x : -x,
                -minorSpacing);
    }
}
