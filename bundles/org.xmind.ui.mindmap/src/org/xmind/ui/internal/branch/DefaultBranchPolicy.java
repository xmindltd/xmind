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
package org.xmind.ui.internal.branch;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.branch.IBranchStructure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class DefaultBranchPolicy extends AbstractBranchPolicy {

    protected static final String RADIAL_STRUCTURE_ID = "org.xmind.ui.branchStructure.radial"; //$NON-NLS-1$

    protected static IBranchStructure radial = null;

    DefaultBranchPolicy(BranchPolicyManager manager) {
        super(manager, "org.xmind.ui.map.unbalanced"); //$NON-NLS-1$
    }

    protected IBranchHook createHook(IBranchPart branch) {
        return new MapBranchHook();
    }

    protected IBranchStructure getPredefinedStructure(String structureId) {
        if (RADIAL_STRUCTURE_ID.equals(structureId)) {
            if (radial == null)
                radial = new RadialStructure();
            return radial;
        }
        return super.getPredefinedStructure(structureId);
    }

    protected IStructure createDefaultStructureAlgorithm() {
        return getPredefinedStructure(getDefaultStructureId());
    }

    protected boolean isUnmodifiableProperty(IBranchPart branch,
            String propertyKey, String secondaryKey) {
        return false;
    }

    protected String calcAdditionalStructureId(IBranchPart branch,
            IBranchPart parent) {
        if (branch.isCentral()) {
            return RADIAL_STRUCTURE_ID;
        }

        String branchType = branch.getBranchType();
        if (MindMapUI.BRANCH_FLOATING.equals(branchType)) {
            Point p = (Point) MindMapUtils.getCache(branch,
                    IBranchPart.CACHE_PREF_POSITION);
            if (p != null && p.x < 0)
                return LEFT_STRUCTURE_ID;
            return getDefaultStructureId();
        }

        if (parent == null)
            return getDefaultStructureId();

        if (getPolicyId().equals(parent.getBranchPolicyId())) {
            if (MindMapUI.BRANCH_SUB.equals(branchType)) {
                String id = (String) MindMapUtils.getCache(parent,
                        CACHE_STRUCTURE_ID);
                return id == null ? getDefaultStructureId() : id;
            }

            if (MindMapUI.BRANCH_CALLOUT.equals(branchType)) {
                String id = (String) MindMapUtils.getCache(parent,
                        CACHE_STRUCTURE_ID);
                return id == null ? getDefaultStructureId() : id;
            }

            if (MindMapUI.BRANCH_MAIN.equals(branchType)) {
                IStructure sa = parent.getBranchPolicy().getStructure(parent);
                if (sa instanceof RadialStructure) {
                    RadialStructure rsa = (RadialStructure) sa;
                    if (rsa.isChildLeft(parent, branch))
                        return LEFT_STRUCTURE_ID;
                    return getDefaultStructureId();
                }
            }
        }
        return null;
    }

    protected String getDefaultStructureId() {
        return RIGHT_STRUCTURE_ID;
    }

}