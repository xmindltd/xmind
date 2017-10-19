/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.branch;

import static org.xmind.ui.style.StyleUtils.getInteger;

import java.util.List;

import org.xmind.ui.mindmap.IBoundaryPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IBranchRangePart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public class BranchStructureData {

    private IBranchPart branch;

    private int majorSpacing = -1;

    private int minorSpacing = -1;

    public BranchStructureData(IBranchPart branch) {
        this.branch = branch;
    }

    protected IBranchPart getBranch() {
        return branch;
    }

    protected List<IBranchPart> getSubBranches() {
        return getBranch().getSubBranches();
    }

    protected int getMinorSpacing() {
        if (minorSpacing < 0) {
            minorSpacing = getInteger(getBranch(), getBranch()
                    .getBranchPolicy().getStyleSelector(getBranch()),
                    Styles.MinorSpacing, 5);
        }
        return minorSpacing;
    }

    protected int getMajorSpacing() {
        if (majorSpacing < 0) {
            majorSpacing = StyleUtils.getMajorSpacing(getBranch(), 5);
        }
        return majorSpacing;
    }

    protected boolean isInSameRangeWithLast(List<IBranchPart> subBranches,
            int index) {
        if (index <= 0 || index >= subBranches.size())
            return false;
        return isInSameRange(subBranches.get(index - 1), subBranches.get(index));
    }

    protected boolean isInSameRange(IBranchPart child1, IBranchPart child2) {
        for (IBoundaryPart boundary : getBranch().getBoundaries()) {
            if (containsBoth(boundary, child1, child2))
                return true;
        }
        for (ISummaryPart summary : getBranch().getSummaries()) {
            if (containsBoth(summary, child1, child2))
                return true;
        }
        return false;
    }

    protected boolean containsBoth(IBranchRangePart range, IBranchPart child1,
            IBranchPart child2) {
        return range.encloses(child1) && range.encloses(child2);
//        List<IBranchPart> branches = range.getEnclosingBranches();
//        return branches.contains(child1) && branches.contains(child2);
    }

}