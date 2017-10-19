package org.xmind.ui.internal.branch;

import org.eclipse.core.runtime.Assert;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;

public class TimelineHorizontalPropertyTester implements IBranchPropertyTester {

    private static final String P_UPWARDS = "upwards"; //$NON-NLS-1$

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        if (P_UPWARDS.equals(property)) {
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
            if (sa instanceof TimelineHorizontalHeadStructure) {
                return ((TimelineHorizontalHeadStructure) sa).isChildUpwards(
                        parent, branch);
            }
        }
        return false;
    }

}
