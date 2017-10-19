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
package org.xmind.ui.internal.branch;

import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;

public class MapBranchPropertyTester implements IBranchPropertyTester {

    private static final String P_LEFT = "left"; //$NON-NLS-1$

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        if (P_LEFT.equals(property)) {
            if (expectedValue == null)
                return isLeft(branch);
            if (expectedValue instanceof Boolean)
                return isLeft(branch) == ((Boolean) expectedValue)
                        .booleanValue();
        }
        return false;
    }

    private boolean isLeft(IBranchPart branch) {
        IBranchPart parentBranch = branch.getParentBranch();
        if (parentBranch != null) {
            IBranchPolicy branchPolicy = parentBranch.getBranchPolicy();
            IStructure structure = branchPolicy.getStructure(parentBranch);
            if (structure instanceof BaseRadialStructure) {
                BaseRadialStructure rs = (BaseRadialStructure) structure;
                return rs.isChildLeft(parentBranch, branch);
            }
        }
        return false;
    }

}