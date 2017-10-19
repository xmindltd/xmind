package org.xmind.ui.internal.branch;

import org.eclipse.core.runtime.Assert;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;

public class TimelineVerticalPropertyTester implements IBranchPropertyTester {

    private static final String P_LEFTWARDS = "leftwards"; //$NON-NLS-1$

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        if (P_LEFTWARDS.equals(property)) {
            if (expectedValue == null)
                return isBranchLeftwards(branch);
            if (expectedValue instanceof Boolean)
                return ((Boolean) expectedValue).booleanValue() == isBranchLeftwards(branch);
        }
        Assert.isTrue(false);
        return false;
    }

    private boolean isBranchLeftwards(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            IStructure sa = parent.getBranchPolicy().getStructure(parent);
            if (sa instanceof TimelineVerticalHeadStructure) {
                return ((TimelineVerticalHeadStructure) sa).isChildLeftwards(
                        parent, branch);
            }
        }
        return false;
    }

}
