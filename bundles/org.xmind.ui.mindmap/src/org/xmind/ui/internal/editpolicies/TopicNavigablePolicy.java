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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.INavigableBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicNavigablePolicy extends MindMapNavigablePolicyBase {

    private boolean ignoreCache = true;

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || MindMapUI.REQ_NAV_CHILD.equals(requestType)
                || MindMapUI.REQ_NAV_SIBLING.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (MindMapUI.REQ_NAV_CHILD.equals(reqType)) {
            navChild(request);
        } else if (MindMapUI.REQ_NAV_SIBLING.equals(reqType)) {
            navSibling(request);
        } else {
            super.handle(request);
        }
    }

    private void navChild(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) source;
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                IBranchPart child = findFirstChild(branch);
                if (child != null) {
                    ITopicPart childTopicPart = child.getTopicPart();
                    if (childTopicPart != null)
                        setNavigationResult(request,
                                Arrays.asList(childTopicPart));
                }
            }
        }
    }

    private void navSibling(Request request) {
        IPart source = request.getPrimaryTarget();
        if (source instanceof ITopicPart) {
            ITopicPart topicPart = (ITopicPart) source;
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                if (branch.isCentral()) {
                    IBranchPart child = findFirstChild(branch);
                    if (child != null) {
                        setNavigationResult(request,
                                Arrays.asList(child.getTopicPart()));
                    }
                }
                IBranchPart sibling = findSucceedingSiblingOrAncestor(branch);
                if (sibling != branch) {
                    setNavigationResult(request,
                            Arrays.asList(sibling.getTopicPart()));
                }
            }
        }
    }

    protected IPart findNewNavParts(Request request, String navType,
            List<IPart> sources) {
        ITopicPart topicPart = MindMapUtils.findTopicPart(request
                .getPrimaryTarget());
        if (topicPart != null) {
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                IPart navPart = findNavPart(branch, navType);
                if (navPart != null && navPart.getStatus().isActive()) {
                    return navPart;
                }
            }
        }
        return null;
    }

    private IPart findNavPart(IBranchPart branch, String navType) {
        IStructure structure = branch.getBranchPolicy().getStructure(branch);
        if (structure instanceof INavigableBranchStructureExtension) {
            IPart navPart = ((INavigableBranchStructureExtension) structure)
                    .calcNavigation(branch, navType);
            if (navPart == null) {
                IBranchPart parent = branch.getParentBranch();
                if (parent != null) {
                    IStructure parentStructure = parent.getBranchPolicy()
                            .getStructure(parent);
                    if (parentStructure instanceof INavigableBranchStructureExtension) {
                        navPart = ((INavigableBranchStructureExtension) parentStructure)
                                .calcChildNavigation(parent, branch, navType,
                                        false);
                    }
                } else {
                    navPart = calcNavThroughFloatingAndCentral(branch, navType);
                }
                if (navPart == null) {
                    ignoreCache = true;
                    navPart = calcNavByPosition(branch, navType);
                }
            }
            return navPart;
        }
        return null;
    }

    protected void setNavCaches(List<IPart> sources, IPart target,
            String navType) {
        if (!ignoreCache) {
//            super.setNavCaches(sources, target, navType);
        }
        ignoreCache = false;
    }

    private IPart calcNavThroughFloatingAndCentral(IBranchPart branch,
            String navType) {
        return calcNavByPosition(branch, navType);
    }

    private IPart calcNavByPosition(IBranchPart branch, String navType) {
        return new PositionSearcher(branch, navType).search();
    }

    protected void findSequentialNavParts(Request request, String navType,
            IPart sequenceStart, List<IPart> sources, List<IPart> result) {
        ITopicPart startTopic = MindMapUtils.findTopicPart(sequenceStart);
        if (startTopic != null) {
            ITopicPart sourceTopic = MindMapUtils.findTopicPart(request
                    .getPrimaryTarget());
            if (sourceTopic != null && isSibling(sourceTopic, startTopic)) {
                IBranchPart startBranch = startTopic.getOwnerBranch();
                IBranchPart sourceBranch = sourceTopic.getOwnerBranch();
                IBranchPart sourceParentBranch = sourceBranch.getParentBranch();
                if (sourceParentBranch != null) {
                    IStructure parentStructure = sourceParentBranch
                            .getBranchPolicy().getStructure(sourceParentBranch);
                    if (parentStructure instanceof INavigableBranchStructureExtension) {
                        INavigableBranchStructureExtension ext = (INavigableBranchStructureExtension) parentStructure;
                        IPart endPart = ext
                                .calcChildNavigation(sourceParentBranch,
                                        sourceBranch, navType, true);
                        IBranchPart endBranch = MindMapUtils
                                .findBranch(endPart);
                        if (endBranch == null) {
                            endBranch = sourceBranch;
                        }
                        List<IBranchPart> list = new ArrayList<IBranchPart>();
                        ext.calcSequentialNavigation(sourceParentBranch,
                                startBranch, endBranch, list);
                        for (IBranchPart branch : list) {
                            result.add(branch.getTopicPart());
                        }
                        request.setResult(GEF.RESULT_NEW_FOCUS,
                                endBranch.getTopicPart());
                    }
                } else {
                    addSeqPartsFromFloatingAndCentral(navType, sourceBranch,
                            startBranch, result);
                }
            }
            if (!result.contains(startTopic))
                result.add(startTopic);
        } else {
            super.findSequentialNavParts(request, navType, sequenceStart,
                    sources, result);
        }
    }

    private void addSeqPartsFromFloatingAndCentral(String navType,
            IBranchPart sourceBranch, IBranchPart startBranch,
            List<IPart> result) {
    }

    private boolean isSibling(ITopicPart t1, ITopicPart t2) {
        IBranchPart b1 = t1.getOwnerBranch();
        IBranchPart b2 = t2.getOwnerBranch();
        if (b1 != null && b2 != null) {
            return b1.getParentBranch() == b2.getParentBranch();
        }
        return false;
    }

    protected IPart findNextOrPrev(IPart source, boolean nextOrPrev) {
        if (source != null)
            return findNextOrPrevTopic(source, nextOrPrev);
        return super.findNextOrPrev(source, nextOrPrev);
    }

    private IPart findNextOrPrevTopic(IPart current, boolean nextOrPrev) {
        if (current instanceof ITopicPart) {
            IBranchPart branch = ((ITopicPart) current).getOwnerBranch();
            if (branch != null) {
                IBranchPart result;
                if (nextOrPrev) {
                    result = findFirstChild(branch);
                    if (result == null) {
                        result = findSucceedingSiblingOrAncestor(branch);
                    }
                } else {
                    result = findPrecedingBranch(branch);
                }
                if (result != null) {
                    return result.getTopicPart();
                }
            }
        }
        return current;
    }

    private IBranchPart findFirstChild(IBranchPart current) {
        List<IBranchPart> subBranches = current.getSubBranches();
        if (!subBranches.isEmpty()) {
            return subBranches.get(0);
        }
        subBranches = current.getSummaryBranches();
        if (!subBranches.isEmpty()) {
            return subBranches.get(0);
        }
        return null;
    }

    private IBranchPart findSucceedingSiblingOrAncestor(IBranchPart current) {
        IBranchPart parent = current.getParentBranch();
        if (parent != null) {
            List<IBranchPart> branches = parent.getSubBranches();
            int index = branches.indexOf(current);
            if (index >= 0 && index < branches.size() - 1) {
                return branches.get(index + 1);
            }
            int lastBranchIndex = branches.size() - 1;
            branches = parent.getSummaryBranches();
            if (branches.size() > 0 && index == lastBranchIndex) {
                return branches.get(0);
            }
            index = branches.indexOf(current);
            if (index >= 0 && index < branches.size() - 1) {
                return branches.get(index + 1);
            }

            return findSucceedingSiblingOrAncestor(parent);
        } else if (current.getParent() instanceof ISheetPart) {
            ISheetPart sheet = (ISheetPart) current.getParent();
            List<IBranchPart> floatingBranches = sheet.getFloatingBranches();
            if (current == sheet.getCentralBranch()) {
                if (!floatingBranches.isEmpty()) {
                    return floatingBranches.get(0);
                }
            } else {
                int index = floatingBranches.indexOf(current);
                if (index >= 0) {
                    if (index < floatingBranches.size() - 1) {
                        IBranchPart floatBranch = floatingBranches
                                .get(index + 1);
                        return floatBranch;
                    }
                    return sheet.getCentralBranch();
                }
            }
        }
        return current;
    }

    private IBranchPart findPrecedingBranch(IBranchPart current) {
        IBranchPart parent = current.getParentBranch();
        if (parent != null) {
            List<IBranchPart> branches = parent.getSubBranches();
            int index = branches.indexOf(current);
            if (index > 0) {
                return findLastDescendant(branches.get(index - 1));
            }

            int lastBranchIndex = branches.size() - 1;
            branches = parent.getSummaryBranches();
            index = branches.indexOf(current);
            if (index == 0) {
                return parent.getSubBranches().get(lastBranchIndex);
            }
            if (index > 0) {
                return findLastDescendant(branches.get(index - 1));
            }
            return parent;

        } else if (current.getParent() instanceof ISheetPart) {
            ISheetPart sheet = ((ISheetPart) current.getParent());
            List<IBranchPart> floatingBranches = sheet.getFloatingBranches();
            if (current == sheet.getCentralBranch()) {
                if (!floatingBranches.isEmpty()) {
                    IBranchPart lastFloating = floatingBranches
                            .get(floatingBranches.size() - 1);
                    return findLastDescendant(lastFloating);
                } else {
                    return findLastDescendant(current);
                }
            } else if (floatingBranches.contains(current)) {
                int index = floatingBranches.indexOf(current);
                if (index == 0) {
                    return findLastDescendant(sheet.getCentralBranch());
                }
                return findLastDescendant(floatingBranches.get(index - 1));
            }
        }
        return current;
    }

    private IBranchPart findLastDescendant(IBranchPart branch) {
        List<IBranchPart> summaryBranches = branch.getSummaryBranches();
        if (!summaryBranches.isEmpty()) {
            return findLastDescendant(summaryBranches.get(summaryBranches
                    .size() - 1));
        }
        List<IBranchPart> subBranches = branch.getSubBranches();
        if (!subBranches.isEmpty()) {
            return findLastDescendant(subBranches.get(subBranches.size() - 1));
        }
        return branch;
    }

}