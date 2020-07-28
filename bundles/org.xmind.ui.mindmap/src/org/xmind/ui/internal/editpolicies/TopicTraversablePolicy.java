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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.INavigableBranchStructureExtension;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.INodePart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public class TopicTraversablePolicy extends MindMapTraversablePolicyBase {

    protected void findTraversables(Request request, IPart source,
            List<IPart> result) {
        ITopicPart topicPart = MindMapUtils.findTopicPart(source);
        if (topicPart != null) {
            IBranchPart branch = topicPart.getOwnerBranch();
            if (branch != null) {
                List<IBranchPart> results = new ArrayList<IBranchPart>();
                IStructure structure = branch.getBranchPolicy().getStructure(
                        branch);
                if (structure instanceof INavigableBranchStructureExtension) {
                    ((INavigableBranchStructureExtension) structure)
                            .calcTraversableChildren(branch, results);
                }

                IBranchPart parent = branch.getParentBranch();
                if (parent != null) {
                    IStructure parentStructure = parent.getBranchPolicy()
                            .getStructure(parent);
                    if (parentStructure instanceof INavigableBranchStructureExtension) {
                        ((INavigableBranchStructureExtension) parentStructure)
                                .calcTraversableBranches(parent, branch,
                                        results);
                    }
                }

                for (IBranchPart b : results) {
                    result.add(b.getTopicPart());
                }
            }
            if (topicPart instanceof INodePart) {
                INodePart node = (INodePart) topicPart;
                addTraversableResults(node.getSourceConnections(), result);
                addTraversableResults(node.getTargetConnections(), result);
            }
        }
    }

    protected IPart getTraverseSource(Request request) {
        IPart source = MindMapUtils.findTopicPart(request.getPrimaryTarget());
        if (source != null && source.hasRole(GEF.ROLE_TRAVERSABLE))
            return source;
        return null;
    }

}