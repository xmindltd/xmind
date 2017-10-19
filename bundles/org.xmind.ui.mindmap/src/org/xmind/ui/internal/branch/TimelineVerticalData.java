package org.xmind.ui.internal.branch;

import java.util.HashSet;
import java.util.Set;

import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;

public class TimelineVerticalData extends BranchStructureData {

    private Set<Integer> rightwards = null;

    public TimelineVerticalData(IBranchPart branch) {
        super(branch);
    }

    public boolean isLeftwardBranch(int index) {
        return !getRightwardBranch().contains(index);
    }

    public Set<Integer> getRightwardBranch() {
        if (rightwards == null)
            rightwards = calcUpwardBranchs();
        return rightwards;
    }

    private Set<Integer> calcUpwardBranchs() {
        Set<Integer> set = new HashSet<Integer>();
        int i = 0;
        IBranchPart lastChild = null;
        boolean rightward = true;
        for (IBranchPart sub : getBranch().getSubBranches()) {
            if (lastChild == null) {
                set.add(i);
            } else {
                if (!isInSameRange(lastChild, sub))
                    rightward = !rightward;

                if (rightward)
                    set.add(i);
            }
            lastChild = sub;
            i++;
        }
        return set;
    }

}
