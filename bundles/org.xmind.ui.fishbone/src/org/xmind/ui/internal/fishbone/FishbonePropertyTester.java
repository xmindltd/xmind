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
package org.xmind.ui.internal.fishbone;

import org.eclipse.core.runtime.Assert;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.internal.fishbone.structures.MainFishboneStructure;
import org.xmind.ui.mindmap.IBranchPart;

public class FishbonePropertyTester implements IBranchPropertyTester {

    private static final String P_UPWARDS = "upwards"; //$NON-NLS-1$

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        if (P_UPWARDS.equals(property)) {
            if (expectedValue == null)
                return isBranchUpwards(branch);
            if (expectedValue instanceof Boolean)
                return ((Boolean) expectedValue).booleanValue() == isBranchUpwards(branch);
        }
        Assert.isTrue(false);
        return false;
    }

    private boolean isBranchUpwards(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            IStructure sa = parent.getBranchPolicy().getStructure(parent);
            if (sa instanceof MainFishboneStructure) {
                MainFishboneStructure mfsa = (MainFishboneStructure) sa;
                return mfsa.isChildUpwards(parent, branch);
            }
        }
        return false;
    }

}