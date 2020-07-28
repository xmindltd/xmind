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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.core.Core;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.ITopicRange;
import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.IIntersectionSolver;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRightNumberOfUnbalancedStructureCommand;
import org.xmind.ui.internal.branch.UnbalancedData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class TopicMovablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_MOVETO.equals(requestType)
                || GEF.REQ_COPYTO.equals(requestType)
                || GEF.REQ_ALIGN.equals(requestType)
                || GEF.REQ_MOVE_UP.equals(requestType)
                || GEF.REQ_MOVE_DOWN.equals(requestType)
                || GEF.REQ_MOVE_LEFT.equals(requestType)
                || GEF.REQ_MOVE_RIGHT.equals(requestType)
                || GEF.REQ_SORT.equals(requestType);
    }

    public void handle(Request request) {
        String requestType = request.getType();
        if (GEF.REQ_MOVETO.equals(requestType)
                || GEF.REQ_COPYTO.equals(requestType)) {
            moveOrCopyTopics(request);
        } else if (GEF.REQ_ALIGN.equals(requestType)) {
            alignTopics(request);
        } else if (GEF.REQ_SORT.equals(requestType)) {
            sortTopics(request);
        } else if (GEF.REQ_MOVE_UP.equals(requestType)
                || GEF.REQ_MOVE_DOWN.equals(requestType)
                || GEF.REQ_MOVE_LEFT.equals(requestType)
                || GEF.REQ_MOVE_RIGHT.equals(requestType)) {
            quickMove(request, requestType);
        }
    }

    private void quickMove(Request request, String direction) {
        int dir = getDirection(direction);
        if (dir < 0)
            return;

        IBranchPart parent = null;
        List<IBranchPart> branches = new ArrayList<IBranchPart>();
        for (IPart p : request.getTargets()) {
            IBranchPart branch = MindMapUtils.findBranch(p);
            if (branch != null) {
                if (!branch.getTopic().isAttached())
                    return;
                if (parent != null && parent != branch.getParentBranch())
                    return;
                if (parent == null)
                    parent = branch.getParentBranch();
                if (parent == null)
                    return;
                branches.add(branch);
            }
        }
        if (parent == null)
            return;

        IStructure structure = parent.getBranchPolicy().getStructure(parent);
        if (!(structure instanceof IBranchStructureExtension))
            return;

        Collections.sort(branches, new Comparator<IBranchPart>() {
            public int compare(IBranchPart o1, IBranchPart o2) {
                return o1.getBranchIndex() - o2.getBranchIndex();
            }
        });
        IBranchStructureExtension bse = (IBranchStructureExtension) structure;
        List<Command> commands = new ArrayList<Command>();
        int total = parent.getTopic().getChildren(ITopic.ATTACHED).size();
        for (int i = 0; i < branches.size(); i++) {
            IBranchPart child = branches.get(i);
            int offset = bse.getQuickMoveOffset(parent, child, dir);
            if (offset > 0) {
                for (int j = child.getBranchIndex() + 1; j < parent
                        .getSubBranches().size(); j++) {
                    IBranchPart b = parent.getSubBranches().get(j);
                    if (!branches.contains(b))
                        break;
                    offset++;
                }
            }
            ITopic topic = child.getTopic();
            int index = topic.getIndex();
            int newIndex = index + offset;
            if (newIndex < 0 || newIndex >= total)
                return;

            DeleteTopicCommand c1 = new DeleteTopicCommand(topic);
            AddTopicCommand c2 = new AddTopicCommand(topic, parent.getTopic(),
                    newIndex, ITopic.ATTACHED);
            commands.add(c1);
            commands.add(c2);
        }
        if (commands.isEmpty())
            return;

        CompoundCommand command = new CompoundCommand(commands);
        command.setLabel(CommandMessages.Command_MoveTopic);
        saveAndRun(command, request.getTargetDomain());
    }

    private int getDirection(String direction) {
        if (GEF.REQ_MOVE_UP.equals(direction))
            return PositionConstants.NORTH;
        if (GEF.REQ_MOVE_DOWN.equals(direction))
            return PositionConstants.SOUTH;
        if (GEF.REQ_MOVE_LEFT.equals(direction))
            return PositionConstants.WEST;
        if (GEF.REQ_MOVE_RIGHT.equals(direction))
            return PositionConstants.EAST;
        return -1;
    }

    private void sortTopics(Request request) {

    }

    private void alignTopics(Request request) {
        List<ITopic> topics = MindMapUtils.filterOutDescendents(
                MindMapUtils.getTopics(request.getTargets()), null);
        if (topics.isEmpty())
            return;

        Object param = request.getParameter(GEF.PARAM_ALIGNMENT);
        if (param == null || !(param instanceof Integer))
            return;

        int alignment = ((Integer) param).intValue();

        IViewer viewer = request.getTargetViewer();
        List<ITopicPart> topicParts = new ArrayList<ITopicPart>(topics.size());
        for (ITopic topic : topics) {
            IPart p = viewer.findPart(topic);
            if (p instanceof ITopicPart) {
                topicParts.add((ITopicPart) p);
            }
        }
        if (topicParts.isEmpty())
            return;

        TopicAlignmentSolver solver = new TopicAlignmentSolver(alignment);
        solver.recordInitPositions(topicParts);
        solver.solve();

        List<Command> commands = new ArrayList<Command>(topicParts.size());
        for (Object key : solver.getKeys(IIntersectionSolver.CATEGORY_FREE)) {
            if (key instanceof ITopicPart) {
                ITopicPart topicPart = (ITopicPart) key;
                Point pos = solver.getSolvedPosition(key);
                pos = getNewPosition(topicPart, pos);
                ITopic topic = topicPart.getTopic();
                commands.add(new ModifyPositionCommand(topic,
                        MindMapUtils.toModelPosition(pos)));
            }
        }
        if (commands.isEmpty())
            return;

        String commandLabel = getAlignCommandLabel(alignment);
        CompoundCommand cmd = new CompoundCommand(commands);
        cmd.setLabel(commandLabel);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private String getAlignCommandLabel(int alignment) {
        switch (alignment) {
        case PositionConstants.LEFT:
            return CommandMessages.Command_AlignLeft;
        case PositionConstants.CENTER:
            return CommandMessages.Command_AlignCenter;
        case PositionConstants.RIGHT:
            return CommandMessages.Command_AlignRight;
        case PositionConstants.TOP:
            return CommandMessages.Command_AlignTop;
        case PositionConstants.MIDDLE:
            return CommandMessages.Command_AlignMiddle;
        case PositionConstants.BOTTOM:
            return CommandMessages.Command_AlignBottom;
        }
        return CommandMessages.Command_Align;
    }

    private Point getNewPosition(ITopicPart topicPart, Point pos) {
        IBranchPart branch = topicPart.getOwnerBranch();
        if (branch != null) {
            IBranchPart parent = branch.getParentBranch();
            if (parent != null) {
                Point parentPos = ((IReferencedFigure) parent.getFigure())
                        .getReference();
                pos = new Point(pos.x - parentPos.x, pos.y - parentPos.y);
            }
        }
        return pos;
    }

    private boolean hasCallout(List<ITopic> topics) {
        if (topics == null || topics.isEmpty())
            return false;

        for (ITopic callout : topics) {
            if (ITopic.CALLOUT.equals(callout.getType()))
                return true;
        }

        return false;
    }

    private void moveOrCopyTopics(Request request) {
        List<IPart> sources = request.getTargets();
        List<ITopic> topics = MindMapUtils
                .filterOutDescendents(MindMapUtils.getTopics(sources), null);
        if (topics.isEmpty())
            return;
        Collections.sort(topics, Core.getTopicComparator());

        Point targetPosition = (Point) request.getParameter(GEF.PARAM_POSITION);
        ITopicPart parentPart = getTargetParent(request);
        boolean copy = Boolean.TRUE
                .equals(request.getParameter(MindMapUI.PARAM_COPY));

        if (targetPosition == null && parentPart == null && !copy)
            return;

        boolean relative = Boolean.TRUE
                .equals(request.getParameter(GEF.PARAM_POSITION_RELATIVE));
        int targetIndex = request.getIntParameter(GEF.PARAM_INDEX, -1);

        IViewer viewer = request.getTargetViewer();
        ITopic targetParent;
        String targetType;
        if (parentPart != null) {
            targetParent = parentPart.getTopic();
            //can't add itself as child
            if (topics.contains(targetParent))
                return;

            if (hasCallout(topics)) {
                targetType = ITopic.CALLOUT;
            } else {
                targetType = ITopic.ATTACHED;
            }
        } else {
            targetParent = (ITopic) viewer.getAdapter(ITopic.class);
            targetType = ITopic.DETACHED;

            parentPart = (ITopicPart) viewer.getAdapter(ITopicPart.class);
        }
        if (targetParent == null
                || (!copy && !isValidMoveToNewParent(targetParent, topics)))
            // Sorry, NO valid parent topic has been found to add these topics
            return;

        TopicMoveCommandBuilder builder = new TopicMoveCommandBuilder(viewer,
                request.getTargetCommandStack(), targetParent, targetIndex,
                targetType, targetPosition, relative);

        IBranchPart parentBranch = MindMapUtils.findBranch(parentPart);
        if (parentBranch == null)
            return;

        ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (centralTopic == null)
            return;

        String centralTopicStructure = centralTopic.getStructureClass();
        boolean isUnbalancedStructure = centralTopicStructure == null
                || UnbalancedData.STRUCTUREID_UNBALANCED
                        .equalsIgnoreCase(centralTopicStructure);

        if (isUnbalancedStructure) {
            ITopicExtension extension = centralTopic.createExtension(
                    UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
            ITopicExtensionElement element = extension.getContent()
                    .getCreatedChild(
                            UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);

            String preMoveRightNum = element.getTextContent();
            if (preMoveRightNum == null)
                preMoveRightNum = String.valueOf(0);
            int postMoveRightNum = Integer.valueOf(preMoveRightNum);
            for (IPart selected : sources) {
                IBranchPart mainBranch = MindMapUtils.findBranch(selected);
                if (mainBranch != null && !mainBranch.isCentral()) {
                    IBranchPart centralBranch = mainBranch.getParentBranch();
                    if (centralBranch != null && centralBranch.isCentral()) {
                        IStructure structure = centralBranch.getBranchPolicy()
                                .getStructure(centralBranch);
                        if ((((IBranchStructureExtension) structure)
                                .getChildTargetOrientation(centralBranch,
                                        mainBranch) == PositionConstants.WEST)) {
                            postMoveRightNum--;
                        }
                    }
                    if (parentBranch.isCentral()
                            && targetType != ITopic.DETACHED) {
                        Rectangle bounds = parentPart.getFigure().getBounds();
                        boolean positionRight = bounds.getCenter()
                                .getDifference((Point) request.getParameter(
                                        GEF.PARAM_POSITION_ABSOLUTE)).width < 0;

                        org.xmind.core.util.Point position = mainBranch
                                .getTopic().getPosition();
//                        boolean topicPosForceRight = position != null && position.x > 0;
//                        boolean posRight = position == null && positionRight;

                        boolean right = position != null ? position.x > 0
                                : positionRight;

                        if (right) {
                            postMoveRightNum++;
                        }
                    }
                }
            }

            if (!preMoveRightNum.equals(postMoveRightNum)) {
                ModifyRightNumberOfUnbalancedStructureCommand modifyRightNumberCommand = new ModifyRightNumberOfUnbalancedStructureCommand(
                        centralTopic, preMoveRightNum, postMoveRightNum);
                modifyRightNumberCommand
                        .setLabel(copy ? CommandMessages.Command_CopyTopic
                                : CommandMessages.Command_MoveTopic);
                builder.addPendingCommand(modifyRightNumberCommand, true);
            }
        }

        PropertyCommandBuilder builder2 = new PropertyCommandBuilder(viewer,
                builder, request);

        if (!builder.canStart())
            return;

        builder.setLabel(copy ? CommandMessages.Command_CopyTopic
                : CommandMessages.Command_MoveTopic);
        builder.start();
        builder2.start();
        if (copy)
            builder.copyTopics(topics);
        else
            builder.moveTopics(topics);

        builder2.addSources(topics.toArray(), true);

        builder2.end();
        builder.end();
//        saveAndRun(cmd, request.getTargetDomain());
        Command cmd = builder.getCommand();
        if (cmd instanceof ISourceProvider) {
            select(((ISourceProvider) cmd).getSources(), viewer);
        }
    }

    private ITopicPart getTargetParent(Request request) {
        Object param = request.getParameter(GEF.PARAM_PARENT);
        if (param instanceof ITopicPart)
            return (ITopicPart) param;
        return null;
    }

    private boolean isValidMoveToNewParent(ITopic newParent,
            List<ITopic> topics) {
        if (MindMapUtils.isAncestorInList(newParent, topics))
            return false;
        Set<ITopicRange> ranges = MindMapUtils.findContainedRanges(topics, true,
                false);
        if (!ranges.isEmpty()) {
            for (ITopicRange range : ranges) {
                ITopic summaryTopic = ((ISummary) range).getTopic();
                if (summaryTopic != null
                        && (summaryTopic.equals(newParent) || MindMapUtils
                                .isDescendentOf(newParent, summaryTopic)))
                    return false;
            }
        }
        return true;
    }

//    private Command createReorganizeCommand(Request request,
//            List<ITopic> topics, IViewer viewer, Point position,
//            boolean relative, ITopic parent, int index, String type,
//            boolean copy) {
//
//        TopicMoveCommandBuilder builder = new TopicMoveCommandBuilder(viewer,
//                parent, index, type, position, relative, copy);
//        for (ITopic t : topics) {
//            builder.moveTopic(t);
//        }
//        PropertyCommandBuilder propCmdBuilder = new PropertyCommandBuilder(
//                viewer);
//        propCmdBuilder.addFromRequest(request, false);
//        if (!propCmdBuilder.isEmpty()) {
//            for (Command c : propCmdBuilder.getCommands()) {
//                builder.addCommand(c, false);
//            }
//        }
//        return builder.createCommand();
//    }

}