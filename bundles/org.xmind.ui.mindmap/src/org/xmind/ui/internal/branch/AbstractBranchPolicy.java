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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.PositionConstants;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.gef.graphicalpolicy.AbstractGraphicalPolicy;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchStructure;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.mindmap.ICacheValueProvider;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tools.IToolHelper;
import org.xmind.ui.util.MindMapUtils;

public abstract class AbstractBranchPolicy extends AbstractGraphicalPolicy
        implements IBranchPolicy, ICacheValueProvider {

    protected static final String RIGHT_STRUCTURE_ID = "org.xmind.ui.branchStructure.right"; //$NON-NLS-1$

    protected static final String DOWN_STRUCTURE_ID = "org.xmind.ui.branchStructure.down"; //$NON-NLS-1$

    protected static final String UP_STRUCTURE_ID = "org.xmind.ui.branchStructure.up"; //$NON-NLS-1$

    protected static final String LEFT_STRUCTURE_ID = "org.xmind.ui.branchStructure.left"; //$NON-NLS-1$

    protected static IBranchStructure left = null;

    protected static IBranchStructure right = null;

    protected static IBranchStructure up = null;

    protected static IBranchStructure down = null;

    protected BranchPolicyManager manager;

    private String id;

    private Set<IBranchPart> calculationQueue;

    public AbstractBranchPolicy(BranchPolicyManager manager, String id) {
        this.manager = manager;
        this.id = id;
    }

    protected String getPolicyId() {
        return id;
    }

    public void activate(IGraphicalPart part) {
        super.activate(part);
        if (part instanceof IBranchPart) {
            activateBranch((IBranchPart) part);
        }
    }

    public void deactivate(IGraphicalPart part) {
        if (part instanceof IBranchPart) {
            deactivateBranch((IBranchPart) part);
        }
        super.deactivate(part);
    }

    public void postDeactivate(IBranchPart branch) {
    }

    protected void activateBranch(IBranchPart branch) {
        ICacheManager cm = MindMapUtils.getCacheManager(branch);
        if (cm != null) {
            cm.setValueProvider(CACHE_STRUCTURE_ID, this);
        }
        addHook(branch);
        flushStructureCache(branch, true, true);
    }

    protected void deactivateBranch(IBranchPart branch) {
        flushStructureCache(branch, true, true);
        removeHook(branch);
        ICacheManager cm = MindMapUtils.getCacheManager(branch);
        if (cm != null) {
            cm.removeValueProvider(CACHE_STRUCTURE_ID);
        }
    }

    protected void addHook(IBranchPart branch) {
        IBranchHook hook = createHook(branch);
        if (hook != null) {
            hook.hook(branch);
            MindMapUtils.setCache(branch, IBranchHook.CACHE_BRANCH_HOOK, hook);
        }
    }

    protected abstract IBranchHook createHook(IBranchPart branch);

    protected void removeHook(IBranchPart branch) {
        IBranchHook hook = (IBranchHook) MindMapUtils.getCache(branch,
                IBranchHook.CACHE_BRANCH_HOOK);
        if (hook != null) {
            MindMapUtils.flushCache(branch, IBranchHook.CACHE_BRANCH_HOOK);
            hook.unhook(branch);
        }
    }

    public void flushStructureCache(IBranchPart branch, boolean ancestors,
            boolean descendants) {
        flushStructureCache(branch);
        if (ancestors)
            flushParentStructureCache(branch);
        if (descendants)
            flushChildrenStructureCache(branch);
    }

    protected void flushStructureCache(IBranchPart branch) {
        MindMapUtils.flushCache(branch, CACHE_STRUCTURE_ID);
    }

    protected void flushParentStructureCache(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent == null)
            return;
        String policyId = parent.getBranchPolicyId();
        if (getPolicyId().equals(policyId)) {
            parent.getBranchPolicy().flushStructureCache(parent, true, false);
        }
    }

    protected void flushChildrenStructureCache(IBranchPart branch) {
        for (IBranchPart child : branch.getSubBranches()) {
            flushChildStructureCache(child);
        }
        for (IBranchPart child : branch.getCalloutBranches()) {
            flushChildStructureCache(child);
        }
        for (IBranchPart child : branch.getSummaryBranches()) {
            flushChildStructureCache(child);
        }
    }

    protected void flushChildStructureCache(IBranchPart child) {
        String policyId = child.getBranchPolicyId();
        if (getPolicyId().equals(policyId)) {
            child.getBranchPolicy().flushStructureCache(child, false, true);
        }
    }

    public IToolHelper getToolHelper(IBranchPart parent,
            Class<? extends IToolHelper> type) {
        return null;
    }

    public boolean isPropertyModifiable(IBranchPart branch, String propertyKey) {
        return isPropertyModifiable(branch, propertyKey, null);
    }

    public boolean isPropertyModifiable(IBranchPart branch, String propertyKey,
            String secondaryKey) {
        boolean modifiable = internalCheckPropertyModifiability(branch,
                propertyKey, secondaryKey);
        if (modifiable) {
            modifiable = !isUnmodifiableProperty(branch, propertyKey,
                    secondaryKey);
        }
        return modifiable;
    }

    protected abstract boolean isUnmodifiableProperty(IBranchPart branch,
            String propertyKey, String secondaryKey);

    protected boolean internalCheckPropertyModifiability(IBranchPart branch,
            String propertyKey, String secondaryKey) {
        if (Core.TopicFolded.equals(propertyKey))
            return isBranchFoldable(branch);
        if (Core.TopicHyperlink.equals(propertyKey))
            return isHyperlinkModifiable(branch);
        return true;
    }

    protected boolean isHyperlinkModifiable(IBranchPart branch) {
        ITopic t = branch.getTopic();
        String uri = t.getHyperlink();
        if (uri != null) {
            return MindMapUI.getProtocolManager().isHyperlinkModifiable(t, uri);
        }
        return true;
    }

    protected boolean isBranchFoldable(IBranchPart branch) {
        return !branch.isCentral();// && !branch.getSubBranches().isEmpty();
    }

    protected IStyleSelector createDefaultStyleSelector() {
        return DefaultBranchStyleSelector.getDefault();
    }

    public IStructure getStructure(IGraphicalPart part) {
        IBranchPart branch = (IBranchPart) part;
        String structureId = (String) MindMapUtils.getCache(branch,
                CACHE_STRUCTURE_ID);
        return getStructureAlgorithmById(part, structureId);
    }

    private IStructure getStructureAlgorithmById(IGraphicalPart part,
            String structureId) {
        if (structureId != null) {
            IStructureDescriptor structureDescriptor = manager
                    .getStructureDescriptor(structureId);
            if (structureDescriptor != null)
                return structureDescriptor.getAlgorithm();
            IBranchStructure sa = getPredefinedStructure(structureId);
            if (sa != null)
                return sa;
        }
        return super.getStructure(part);
    }

    public Object getValue(IPart part, String key) {
        IBranchPart branch = (IBranchPart) part;
        if (CACHE_STRUCTURE_ID.equals(key)) {
            return calculateStructureAlgorithmId(branch);
        }
        return null;
    }

    protected String calculateStructureAlgorithmId(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null && parent.getSummaryBranches().contains(branch)) {
            return calcSummaryBranchStructureType(branch, parent);
        }

        if (isCalculatingOn(branch))
            return getDefaultStructureId();

        startCalculationOn(branch);

        String structureId = calcAdditionalStructureId(branch, parent);

        endCalculationOn(branch);

        if (structureId != null)
            return structureId;

        return getDefaultStructureId();
    }

    protected abstract String calcAdditionalStructureId(IBranchPart branch,
            IBranchPart parent);

    private String calcSummaryBranchStructureType(IBranchPart branch,
            IBranchPart parent) {
        ISummaryPart summary = MindMapUtils.findAttachedSummary(parent, branch);
        if (summary != null) {
            IStructure sa = parent.getBranchPolicy().getStructure(parent);
            if (sa instanceof IBranchStructureExtension) {
                int direction = ((IBranchStructureExtension) sa)
                        .getSummaryDirection(parent, summary);
                if (direction == PositionConstants.WEST)
                    return LEFT_STRUCTURE_ID;
                if (direction == PositionConstants.NORTH)
                    return UP_STRUCTURE_ID;
                if (direction == PositionConstants.SOUTH)
                    return DOWN_STRUCTURE_ID;
            }
        }
        return RIGHT_STRUCTURE_ID;
    }

    protected boolean isCalculatingOn(IBranchPart branch) {
        return calculationQueue != null && calculationQueue.contains(branch);
    }

    protected void startCalculationOn(IBranchPart branch) {
        if (calculationQueue == null)
            calculationQueue = new HashSet<IBranchPart>();
        calculationQueue.add(branch);
    }

    protected void endCalculationOn(IBranchPart branch) {
        if (calculationQueue == null)
            return;
        calculationQueue.remove(branch);
        if (calculationQueue.isEmpty())
            calculationQueue = null;
    }

    protected IBranchStructure getPredefinedStructure(String structureId) {
        if (LEFT_STRUCTURE_ID.equals(structureId)) {
            if (left == null)
                left = new LeftStructure();
            return left;
        } else if (RIGHT_STRUCTURE_ID.equals(structureId)) {
            if (right == null)
                right = new RightStructure();
            return right;
        } else if (UP_STRUCTURE_ID.equals(structureId)) {
            if (up == null)
                up = new UpStructure();
            return up;
        } else if (DOWN_STRUCTURE_ID.equals(structureId)) {
            if (down == null)
                down = new DownStructure();
            return down;
        }
        return null;
    }

    protected abstract String getDefaultStructureId();

}