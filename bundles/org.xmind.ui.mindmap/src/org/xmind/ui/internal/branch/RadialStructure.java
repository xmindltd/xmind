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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.tools.ParentSearchKey;

public class RadialStructure extends BaseRadialStructure {

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
                y = -cache.getLeftSumSpacing() / 2;
                if (insertion != null && !insertion.right) {
                    y -= insHeight / 2;
                }
                right = false;
            }

            if (insertion != null && i == insertion.getIndex()) {
                if (i != numRight || !insertion.right) {
                    Point p = ref.getTranslated(cache.getX(y, right), y);
                    Rectangle insBounds = RadialUtils.getPrefBounds(
                            insertion.getSize(), p, right);
                    info.add(insBounds);
                    y += insHeight;
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
            y += childrenSpacings[i];

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
        if (GEF.REQ_NAV_UP.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() - 1);
        } else if (GEF.REQ_NAV_DOWN.equals(navReqType)) {
            return getSubTopicPart(branch, sourceChild.getBranchIndex() + 1);
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

    public int getSummaryDirection(IBranchPart branch, ISummaryPart summary) {
        return super.getSummaryDirection(branch, summary);
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

        int subSize = subBranches.size();
        int right = cache.getNumRight();

        Dimension inventSize = key.getInvent().getSize();
        if (index == oldIndex) {
            if (subSize > right && index == right - 1
                    && key.getCursorPos().x < 0
                    && (!subBranches.get(right).getFigure().isEnabled()))
                index += 1;
            int delta = getTopicSize(subBranches.get(index)).width / 2
                    - inventSize.width / 2;
            int deltaX = index < right ? -delta : delta;
            return getReference(subBranches.get(index))
                    .getTranslated(deltaX, 0);
        }
        return calcInsertPosition(branch, child, key);
    }

    @Override
    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        Point firstLoc = calcFirstChildPosition(branch, key).getTranslated(
                key.getInvent().getSize().width / 2, 0);
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

        if (index == subSize || index == -1) {
            if (subSize == 1 && isWithinThreshold(first)) {
                if (fBounds.bottom() > 0) {
                    int x = fBounds.x + inventSize.width / 2;
                    int y = fBounds.bottom()
                            + (insSize.height + inventSize.height) / 2;
                    return new Point(x, y);
                }
                return new Point(firstLoc.x, -firstLoc.y);
            }

            if (left == 0)
                return new Point(-firstLoc.x, firstLoc.y);

            IBranchPart sub = subBranches.get(subSize - 1);
            Rectangle bounds = sub.getFigure().getBounds();
            if (left == 1 && bounds.bottom() < 0)
                return firstLoc.getNegated();

            int x = bounds.right() - inventSize.width / 2;
            int y = bounds.bottom() + (insSize.height + inventSize.height) / 2;
            return new Point(x, y);
        }

        if (index == right) {
            boolean isRight = (left == 1 && right == 1)
                    || isRight(subBranches, child, right);
            IBranchPart sub = isRight ? subBranches.get(index - 1)
                    : subBranches.get(index);
            Rectangle bounds = sub.getFigure().getBounds();
            int x;
            int y;
            if (isRight) {
                x = bounds.x + inventSize.width / 2;
                y = bounds.bottom() + (insSize.height + inventSize.height) / 2;
            } else {
                x = bounds.right() - inventSize.width / 2;
                y = bounds.y - (insSize.height + inventSize.height) / 2;
            }
            return new Point(x, y);
        }

        return calcInventPosition(subBranches.get(index - 1),
                subBranches.get(index), key, index < right);
    }

}