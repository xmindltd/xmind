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
package org.xmind.ui.tools;

import static org.xmind.ui.mindmap.MindMapUI.SEARCH_RANGE;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalEditPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IInsertableBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;

public class ParentSearcher {

    private static final int FARTHEST_RANGE = SEARCH_RANGE * SEARCH_RANGE;

    private int minDistance = Integer.MAX_VALUE;

    private IBranchPart targetParent = null;

    private boolean canDropInsideTopic;

    public ParentSearcher(boolean canDropInsideTopic) {
        this.canDropInsideTopic = canDropInsideTopic;
    }

    public ParentSearcher() {
        this(false);
    }

    public IBranchPart searchTargetParent(IPart root, ParentSearchKey key) {
        minDistance = Integer.MAX_VALUE;
        targetParent = null;
        search(root, key);
        if (minDistance > FARTHEST_RANGE)
            targetParent = null;
        return targetParent;
    }

    private void search(IPart parent, ParentSearchKey key) {
        if (parent instanceof IBranchPart) {
            searchBranch((IBranchPart) parent, key);
        } else {
            for (IPart p : parent.getChildren()) {
                search(p, key);
            }
        }
    }

    private void searchBranch(IBranchPart branch, ParentSearchKey key) {
        ITopicPart topic = branch.getTopicPart();
        if (topic == null)
            return;

        IFigure topicFigure = topic.getFigure();
        if (topicFigure == null || !topicFigure.isEnabled())
            return;

        if (canDropInsideTopic
                && ((IGraphicalEditPart) topic).containsPoint(key
                        .getCursorPos())) {
            minDistance = 0;
            targetParent = branch;
            return;
        }

        IStructure sa = branch.getBranchPolicy().getStructure(branch);
        if (sa instanceof IInsertableBranchStructureExtension) {
            int distance = ((IInsertableBranchStructureExtension) sa)
                    .calcChildDistance(branch, key);
            if (distance >= 0 && distance < minDistance) {
                minDistance = distance;
                targetParent = branch;
            }
        }

        if (branch.canSearchChild()) {
            for (IBranchPart subBranch : branch.getSubBranches()) {
                searchBranch(subBranch, key);
            }
            for (IBranchPart summaryBranch : branch.getSummaryBranches()) {
                searchBranch(summaryBranch, key);
            }
            for (IBranchPart callout : branch.getCalloutBranches()) {
                searchBranch(callout, key);
            }
        }
    }

    public int getIndex(IBranchPart targetParent, ParentSearchKey key) {
        if (targetParent != null) {
            if (canDropInsideTopic
                    && ((IGraphicalEditPart) targetParent.getTopicPart())
                            .containsPoint(key.getCursorPos()))
                return -1;

            IStructure sa = targetParent.getBranchPolicy().getStructure(
                    targetParent);
            if (sa instanceof IInsertableBranchStructureExtension) {
                return ((IInsertableBranchStructureExtension) sa)
                        .calcChildIndex(targetParent, key);
            }
        }
        return -1;
    }
}