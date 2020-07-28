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
package org.xmind.ui.internal.editpolicies;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.GEF;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;

public class PositionSearcher {

    private IBranchPart sourceBranch;

    private boolean upLeft;

    private boolean horizontal;

    private Rectangle sourceBounds = null;

    private Point sourceCenter = null;

    private Dimension minDiff = null;

    private IPart result = null;

    public PositionSearcher(IBranchPart sourceBranch, String navType) {
        this.sourceBranch = sourceBranch;
        this.upLeft = GEF.REQ_NAV_UP.equals(navType)
                || GEF.REQ_NAV_LEFT.equals(navType);
        this.horizontal = GEF.REQ_NAV_LEFT.equals(navType)
                || GEF.REQ_NAV_RIGHT.equals(navType);
    }

    public IPart search() {
        minDiff = null;
        result = null;
        sourceBounds = null;
        sourceCenter = null;
        search(sourceBranch.getSite().getRoot());
        return result;
    }

    private void search(IPart context) {
        if (context instanceof IBranchPart) {
            searchBranch((IBranchPart) context);
            return;
        }
        for (IPart child : context.getChildren()) {
            search(child);
        }
    }

    private void searchBranch(IBranchPart branch) {
        checkBranch(branch);
        if (branch.canSearchChild()) {
            for (IBranchPart sub : branch.getSubBranches()) {
                searchBranch(sub);
            }
            for (IBranchPart sum : branch.getSummaryBranches()) {
                searchBranch(sum);
            }
        }
    }

    private void checkBranch(IBranchPart branch) {
        if (sourceBounds == null)
            sourceBounds = getBounds(sourceBranch);
        if (sourceCenter == null)
            sourceCenter = sourceBounds.getCenter();

        Rectangle bounds = getBounds(branch);
        int x = bounds.x + bounds.width / 2;
        int y = bounds.y + bounds.height / 2;
        int dx, dy;
        if (horizontal) {
            if ((upLeft && x >= sourceBounds.x)
                    || (!upLeft && x <= sourceBounds.right()))
                return;
            dy = Math.abs(sourceCenter.y - y);
            dx = Math.abs(sourceBounds.x - x);
        } else {
            if ((upLeft && y >= sourceBounds.y)
                    || (!upLeft && y <= sourceBounds.bottom()))
                return;
            dx = Math.abs(sourceCenter.x - x);
            dy = Math.abs(sourceBounds.y - y);
        }

        if (minDiff == null) {
            minDiff = new Dimension(dx, dy);
            result = branch.getTopicPart();
            return;
        }

        if (horizontal) {
            if (dy > minDiff.height) {
                return;
            } else if (dy == minDiff.height) {
                if (dx >= minDiff.width)
                    return;
            } else {
                if (dx >= minDiff.width
                        && dx - minDiff.width >= minDiff.height - dy)
                    return;
            }
        } else {
            if (dx > minDiff.width) {
                return;
            } else if (dx == minDiff.width) {
                if (dy >= minDiff.height)
                    return;
            } else {
                if (dy >= minDiff.height
                        && dy - minDiff.height >= minDiff.width - dx)
                    return;
            }
        }

        minDiff.width = dx;
        minDiff.height = dy;
        result = branch.getTopicPart();
    }

    private Rectangle getBounds(IBranchPart branch) {
        return branch.getTopicPart().getFigure().getBounds();
    }
}