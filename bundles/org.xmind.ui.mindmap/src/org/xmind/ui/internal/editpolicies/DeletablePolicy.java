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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.core.IRelationship;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicComponent;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyRightNumberOfUnbalancedStructureCommand;
import org.xmind.ui.internal.branch.UnbalancedData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class DeletablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_DELETE.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (GEF.REQ_DELETE.equals(reqType)) {
            delete(request);
        }
    }

    protected void delete(Request request) {
        IViewer viewer = request.getTargetViewer();
        List<IPart> targets = request.getTargets();

        List<Object> elements = getElementsToDelete(targets, viewer);
        if (elements == null || elements.isEmpty())
            return;

        IPart toFocus = MindMapUtils.findToFocus(targets, viewer);

        String label = getDeleteLabel(MindMapUI.getCategoryManager()
                .analyze(elements.toArray()).getMainCategory());

        DeleteCommandBuilder builder = new DeleteCommandBuilder(viewer,
                request.getTargetCommandStack());
        if (!builder.canStart())
            return;

        ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (centralTopic == null)
            return;

        String centralTopicStricture = centralTopic.getStructureClass();
        boolean isUnthrowedSideStructure = centralTopicStricture == null
                || UnbalancedData.STRUCTUREID_UNBALANCED
                        .equalsIgnoreCase(centralTopicStricture);

        if (isUnthrowedSideStructure) {
            ITopicExtension extension = centralTopic.createExtension(
                    UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
            ITopicExtensionElement ee = extension.getContent().getCreatedChild(
                    UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);
            String preDeleteRightNum = ee.getTextContent();
            if (preDeleteRightNum == null)
                preDeleteRightNum = String.valueOf(0);
            int postDeleteRightNum = Integer.valueOf(preDeleteRightNum);
            for (IPart topicPart : targets) {
                if (topicPart instanceof ITopicPart && !(ITopic.SUMMARY.equals(
                        ((ITopicPart) topicPart).getTopic().getType()))) {
                    IBranchPart mainBranch = MindMapUtils.findBranch(topicPart);
                    if (!mainBranch.isCentral()) {
                        IBranchPart centralBranch = mainBranch
                                .getParentBranch();
                        if (centralBranch != null
                                && centralBranch.isCentral()) {
                            IStructure structure = centralBranch
                                    .getBranchPolicy()
                                    .getStructure(centralBranch);
                            if ((((IBranchStructureExtension) structure)
                                    .getChildTargetOrientation(centralBranch,
                                            mainBranch) == PositionConstants.WEST)) {
                                postDeleteRightNum--;
                            }
                        }
                    }
                }
            }
            if (!preDeleteRightNum.equals(postDeleteRightNum)) {
                builder.addPendingCommand(
                        new ModifyRightNumberOfUnbalancedStructureCommand(
                                centralTopic, preDeleteRightNum,
                                postDeleteRightNum),
                        true);
            }
        }

        builder.start();
        builder.setLabel(label);

        for (Object element : elements) {
            builder.delete(element);
        }
        builder.end();

//        Command cmd = getDeleteCommand(targets, viewer);
//        if (cmd == null)
//            return;
//
//        saveAndRun(cmd, domain);

        if (toFocus != null) {
            select(toFocus, viewer);

        } else {
            ITopic topic = (ITopic) viewer.getAdapter(ITopic.class);
            if (topic != null) {
                select(topic, viewer);
            } else {
                viewer.setSelection(StructuredSelection.EMPTY, true);
            }
        }
    }

    protected List<Object> getElementsToDelete(List<IPart> targets,
            IViewer viewer) {
        ITopic rootTopic = (ITopic) viewer.getAdapter(ITopic.class);
        List<ITopic> topics = MindMapUtils.getTopics(targets);
        topics.remove(rootTopic);
        topics = MindMapUtils.filterOutDescendents(topics, rootTopic);

        List<Object> others = new ArrayList<Object>(
                targets.size() - topics.size());
        for (IPart p : targets) {
            Object m = MindMapUtils.getRealModel(p);
            if (m instanceof IRelationship) {
                IRelationship r = (IRelationship) m;
                if (!others.contains(r)) {
                    others.add(r);
                }
            } else if (m instanceof ITopicComponent && !(m instanceof ITopic)) {
                ITopic parent = ((ITopicComponent) m).getParent();
                if (parent != null && !topics.contains(parent)
                        && !MindMapUtils.isAncestorInList(parent, topics)) {
                    others.add(m);
                }
            }
        }
        if (topics.isEmpty() && others.isEmpty())
            return null;

        ArrayList<Object> list = new ArrayList<Object>(
                topics.size() + others.size());
        list.addAll(topics);
        list.addAll(others);
        return list;
    }

    protected String getDeleteLabel(String type) {
        if (MindMapUI.CATEGORY_TOPIC.equals(type))
            return CommandMessages.Command_DeleteTopic;
        if (MindMapUI.CATEGORY_RELATIONSHIP.equals(type))
            return CommandMessages.Command_DeleteRelationship;
        if (MindMapUI.CATEGORY_MARKER.equals(type))
            return CommandMessages.Command_DeleteMarker;
        if (MindMapUI.CATEGORY_SHEET.equals(type))
            return CommandMessages.Command_DeleteSheet;
        if (MindMapUI.CATEGORY_BOUNDARY.equals(type))
            return CommandMessages.Command_DeleteBoundary;
        return CommandMessages.Command_Delete;
    }

}