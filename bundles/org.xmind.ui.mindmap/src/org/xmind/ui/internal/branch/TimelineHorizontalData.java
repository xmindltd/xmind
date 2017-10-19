package org.xmind.ui.internal.branch;

import java.util.HashSet;
import java.util.Set;

import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;

public class TimelineHorizontalData extends BranchStructureData {

    private Set<Integer> upwardBranch = null;

    public TimelineHorizontalData(IBranchPart branch) {
        super(branch);
    }

    public boolean isUpwardBranch(int index) {
        return getUpwardBranch().contains(index);
    }

    public Set<Integer> getUpwardBranch() {
        if (upwardBranch == null)
            upwardBranch = calcUpwardBranchs();
        return upwardBranch;
    }

    private Set<Integer> calcUpwardBranchs() {
        Set<Integer> set = new HashSet<Integer>();
        int i = 0;
        IBranchPart lastChild = null;
        boolean upwards = true;
        for (IBranchPart sub : getBranch().getSubBranches()) {
            if (lastChild == null) {
                set.add(i);
            } else {
                if (!isInSameRange(lastChild, sub))
                    upwards = !upwards;

                if (upwards)
                    set.add(i);
            }
            lastChild = sub;
            i++;
        }
        return set;
    }

}
