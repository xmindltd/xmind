package org.xmind.ui.internal.branch;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.tools.ParentSearchKey;

public class UnbalancedStructure extends ClockwiseRadialStructure {

    @Override
    protected Object createStructureData(IBranchPart branch) {
        return new UnbalancedData(branch);
    }

    @Override
    protected Point calcInsertPosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();

        int index = calcInsIndex(branch, key, true);

        RadialData cache = getRadialData(branch);
        int right = cache.getNumRight();
        int left = cache.getNumLeft();

        Dimension insSize = key.getFigure().getSize();
        Dimension inventSize = key.getInvent().getSize();
        if (getReference(key.getFeedback()).x > 0) {
            if (right == 0)
                return calcFirstChildPosition(branch, key);

            IBranchPart first = subBranches.get(0);
            Rectangle fBounds = first.getFigure().getBounds();
            if (index == 0) {
                int x = fBounds.x + inventSize.width / 2;
                int y = fBounds.y - (insSize.height + inventSize.height) / 2;
                return new Point(x, y);
            }

            if (right == 1) {
                if (fBounds.bottom() > 0) {
                    int x = fBounds.x + inventSize.width / 2;
                    int y = fBounds.bottom()
                            + (insSize.height + inventSize.height) / 2;
                    return new Point(x, y);
                } else {
                    Point loc = calcFirstChildPosition(branch, key);
                    return new Point(loc.x, -loc.y);
                }
            }

            if (index == right) {
                IBranchPart sub = subBranches.get(right - 1);
                Rectangle bounds = sub.getFigure().getBounds();
                int x = bounds.x + inventSize.width / 2;
                int y = bounds.bottom()
                        + (insSize.height + inventSize.height) / 2;
                return new Point(x, y);
            }

            return calcInventPosition(subBranches.get(index - 1),
                    subBranches.get(index), key, true);
        } else {
            if (left == 0) {
                return calcFirstChildPosition(branch, key).getNegated();
            }

            if (right >= 0 && right < subBranches.size()) {
                IBranchPart leftFirst = subBranches.get(right);
                if (index == right) {
                    Rectangle lFBounds = leftFirst.getFigure().getBounds();
                    int x = lFBounds.right() - inventSize.width / 2;
                    int y = lFBounds.bottom()
                            + (insSize.height + inventSize.height) / 2;
                    return new Point(x, y);
                }

                if (left == 1) {
                    Rectangle lFBounds = leftFirst.getFigure().getBounds();
                    if (lFBounds.y < 0) {
                        int x = lFBounds.right() - inventSize.width / 2;
                        int y = lFBounds.y
                                - (insSize.height + inventSize.height) / 2;
                        return new Point(x, y);
                    } else {
                        Point loc = calcFirstChildPosition(branch, key)
                                .getNegated();
                        return new Point(loc.x, -loc.y);
                    }
                }
            }

            if (index == subBranches.size()) {
                IBranchPart sub = subBranches.get(subBranches.size() - 1);
                Rectangle bounds = sub.getFigure().getBounds();
                int x = bounds.right() - inventSize.width / 2;
                int y = bounds.y - (insSize.height + inventSize.height) / 2;
                return new Point(x, y);
            }

            return calcInventPosition(subBranches.get(index),
                    subBranches.get(index - 1), key, false);
        }
    }

    @Override
    protected Point calcMovePosition(IBranchPart branch, IBranchPart child,
            ParentSearchKey key) {
        List<IBranchPart> subBranches = branch.getSubBranches();
        List<Integer> disables = getDisableBranches(branch);

        RadialData cache = getRadialData(branch);
        int right = cache.getNumRight();
        int left = subBranches.size() - right;

        int oldIndex = getOldIndex(branch, child);
        int index = calcInsIndex(branch, key, true);
        if (disables != null) {
            if (disables.contains(index - 1)) {
                index--;
                oldIndex = index;
            } else if (disables.contains(index)) {
                oldIndex = index;
            }
        }

        Dimension inventSize = key.getInvent().getSize();
        if (getReference(key.getFeedback()).x > 0) {
            if (right == 0 || subBranches.size() == 1)
                return calcFirstChildPosition(branch, key);
            if (oldIndex < right) {
                if (index == oldIndex)
                    return getReference(subBranches.get(index)).getTranslated(
                            -getTopicSize(subBranches.get(index)).width / 2
                                    + inventSize.width / 2,
                            0);

            } else {
                if (index == right) {
                    IBranchPart sub = subBranches.get(index - 1);
                    Point loc = getReference(sub).getTranslated(
                            -getTopicSize(sub).width / 2 + inventSize.width / 2,
                            0);
                    if (right == 1)
                        return new Point(loc.x, -loc.y);
                }
            }
        } else {
            if (left == 0 || subBranches.size() == 1)
                return calcFirstChildPosition(branch, key).getNegated();

            if (oldIndex < right) {
                if (index == right - 1) {
                    IBranchPart sub = subBranches.get(index + 1);
                    if (!sub.getFigure().isEnabled())
                        return getReference(sub)
                                .getTranslated(getTopicSize(sub).width / 2
                                        - inventSize.width / 2, 0);

                    Rectangle bounds = sub.getFigure().getBounds();
                    int x = bounds.right() - inventSize.width / 2;
                    int y = bounds.bottom() + (key.getFigure().getSize().height
                            + inventSize.height) / 2;
                    return new Point(x, y);
                }
            } else {
                if (index == oldIndex) {
                    IBranchPart sub = subBranches.get(index);
                    return getReference(sub).getTranslated(
                            getTopicSize(sub).width / 2 - inventSize.width / 2,
                            0);
                }
            }
        }
        return calcInsertPosition(branch, child, key);
    }
}
