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

import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.osgi.util.NLS;
import org.xmind.core.IBoundary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddBoundaryCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.DeleteBoundaryCommand;
import org.xmind.ui.commands.DeleteTopicCommand;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class CreateTopicCommandBuilder extends CommandBuilder {

    private ITopic sourceTopic;

    private List<ITopic> sources;

    private String createType;

    private ITopic targetParent;

    private String targetType;

    private int targetIndex;

    private int sourceIndex;

    private ITopic createdTopic = null;

    public CreateTopicCommandBuilder(IViewer viewer, CommandBuilder delegate,
            ITopic sourceTopic, String createType) {
        super(viewer, delegate);
        init(sourceTopic, createType);
    }

    public CreateTopicCommandBuilder(IViewer viewer,
            ICommandStack commandStack, ITopic sourceTopic, String createType) {
        super(viewer, commandStack);
        init(sourceTopic, createType);
    }

    public CreateTopicCommandBuilder(IViewer viewer,
            ICommandStack commandStack, ITopic sourceTopic, String createType,
            List<ITopic> sources) {
        this(viewer, commandStack, sourceTopic, createType);
        this.sources = sources;
    }

    private void init(ITopic sourceTopic, String createType) {
        this.sourceTopic = sourceTopic;
        this.createType = createType;
        this.sourceIndex = sourceTopic.getIndex();
        if (MindMapUI.REQ_CREATE_CHILD.equals(createType)) {
            this.targetParent = sourceTopic;
            this.targetType = ITopic.ATTACHED;
            this.targetIndex = targetParent.getChildren(targetType).size();
        } else if (MindMapUI.REQ_CREATE_CALLOUT.equals(createType)) {
            this.targetParent = sourceTopic;
            this.targetType = ITopic.CALLOUT;
            this.targetIndex = targetParent.getChildren(targetType).size();
        } else {
            this.targetParent = sourceTopic.getParent();
            if (this.targetParent != null) {
                if (GEF.REQ_CREATE.equals(createType)) {
                    this.targetIndex = sourceTopic.getIndex() + 1;
                    this.targetType = sourceTopic.getType();
                } else if (MindMapUI.REQ_CREATE_BEFORE.equals(createType)
                        || MindMapUI.REQ_CREATE_PARENT.equals(createType)
                        || MindMapUI.REQ_DUPLICATE_TOPIC.equals(createType)) {
                    this.targetIndex = sourceTopic.getIndex();
                    this.targetType = sourceTopic.getType();
                } else {
                    this.targetIndex = -1;
                    this.targetType = null;
                }
            } else {
                this.targetIndex = -1;
                this.targetType = null;
            }
        }
    }

    public ITopic getCreatedTopic() {
        return createdTopic;
    }

    public String getCreateType() {
        return createType;
    }

    public ITopic getSourceTopic() {
        return sourceTopic;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public ITopic getTargetParent() {
        return targetParent;
    }

    public String getTargetType() {
        return targetType;
    }

    public void createTopic() {
        if (!canStart())
            return;

        CreateTopicCommand create = new CreateTopicCommand(
                sourceTopic.getOwnedWorkbook());
        add(create, true);

        createdTopic = (ITopic) create.getSource();
        if (createdTopic == null)
            return;

        preAdd();
        add(new AddTopicCommand(createdTopic, targetParent, targetIndex,
                targetType), true);
        postAdded();
    }

    public void createDuplicateTopic() {
        if (!canStart())
            return;

        createdTopic = sourceTopic.getOwnedWorkbook().cloneTopic(sourceTopic);
        if (createdTopic == null)
            return;

        ensureParentUnfolded();
        add(new AddTopicCommand(createdTopic, targetParent, targetIndex,
                targetType), true);
        postAdded();
    }

    public boolean canStart() {
        return super.canStart() && targetParent != null && targetType != null;
    }

    private void preAdd() {
        ensureParentUnfolded();
        setNewTitle();
        if (ITopic.CALLOUT.equals(targetType)) {
            add(new ModifyPositionCommand(createdTopic,
                    new org.xmind.core.util.Point(0, 0)), false);
        }
    }

    private void postAdded() {
        if (MindMapUI.REQ_CREATE_PARENT.equals(createType)) {
            if (sources != null) {
                for (ITopic sourceTopic : sources) {
                    if (ITopic.DETACHED.equals(targetType)) {
                        add(new ModifyPositionCommand(createdTopic,
                                sourceTopic.getPosition()), false);
                        add(new ModifyPositionCommand(sourceTopic, null), false);
                    }
                    add(new DeleteTopicCommand(sourceTopic), false);
                    add(new AddTopicCommand(sourceTopic, createdTopic), false);
                    moveOverallBoundaries(sourceTopic, createdTopic);
                }
            }
        } else {
            if (ITopic.ATTACHED.equals(targetType)) {
                if (GEF.REQ_CREATE.equals(createType)
                        || MindMapUI.REQ_CREATE_BEFORE.equals(createType)
                        || MindMapUI.REQ_DUPLICATE_TOPIC.equals(createType)) {
                    if (sourceIndex >= 0)
                        modifyRanges();
                }
            }
        }

    }

    private void moveOverallBoundaries(ITopic fromTopic, ITopic toTopic) {
        IBoundary overallBoundary = null;
        for (Object o : fromTopic.getBoundaries().toArray()) {
            IBoundary boundary = (IBoundary) o;
            if (boundary.isMasterBoundary() && overallBoundary == null) {
                overallBoundary = boundary;
            }
            add(new DeleteBoundaryCommand(boundary), false);
        }

        if (overallBoundary != null) {
            if (!hasOverallBoundaries(toTopic)) {
                add(new AddBoundaryCommand(overallBoundary, toTopic), false);
            }
        }
    }

    private boolean hasOverallBoundaries(ITopic topic) {
        for (IBoundary boundary : topic.getBoundaries()) {
            if (boundary.isMasterBoundary())
                return true;
        }
        return false;
    }

    private void modifyRanges() {
        modifyRanges(targetParent.getBoundaries());
        modifyRanges(targetParent.getSummaries());
    }

    private void modifyRanges(Collection<? extends ITopicRange> ranges) {
        for (ITopicRange range : ranges) {
            int startIndex = range.getStartIndex();
            int endIndex = range.getEndIndex();
            if (startIndex >= 0 && endIndex >= 0) {
                if (startIndex > sourceIndex) {
                    add(new ModifyRangeCommand(range, startIndex + 1, true),
                            false);
                }
                if (endIndex >= sourceIndex) {
                    add(new ModifyRangeCommand(range, endIndex + 1, false),
                            false);
                }
            }
        }
    }

    private void setNewTitle() {
        add(new ModifyTitleTextCommand(createdTopic, getNewTitle()), false);
    }

    private String getNewTitle() {
        if (ITopic.DETACHED.equals(targetType)) {
            return MindMapMessages.TitleText_FloatingTopic;
        } else if (ITopic.CALLOUT.equals(targetType)) {
            return MindMapMessages.TitleText_CalloutTopic;
        } else {
            int size = targetParent.getChildren(targetType).size();
            int newNumber = size + 1;
            if (targetParent.isRoot()) {
                return NLS.bind(MindMapMessages.TitleText_MainTopic, newNumber);
            } else {
                return NLS.bind(MindMapMessages.TitleText_Subtopic, newNumber);
            }
        }
    }

    private void ensureParentUnfolded() {
        if (targetParent.isFolded()) {
            add(new ModifyFoldedCommand(targetParent, false), false);
        }
    }

    protected void handlePendingCommands() {
        super.handlePendingCommands();
        if (ITopic.DETACHED.equals(targetType)) {
            if (GEF.REQ_CREATE.equals(createType)) {
                setNewPosition(true);
            } else if (MindMapUI.REQ_CREATE_BEFORE.equals(createType)
                    || MindMapUI.REQ_DUPLICATE_TOPIC.equals(createType)) {
                setNewPosition(false);
            }
        }
    }

    private void setNewPosition(boolean lowerOrUpper) {
        Point newPosition = calcNewPosition(lowerOrUpper);
        add(new ModifyPositionCommand(createdTopic,
                MindMapUtils.toModelPosition(newPosition)), false);
    }

    private Point calcNewPosition(boolean lowerOrUpper) {
        IPart sourcePart = getViewer().findPart(sourceTopic);
        if (sourcePart != null && sourcePart instanceof ITopicPart) {
            IBranchPart sourceBranch = ((ITopicPart) sourcePart)
                    .getOwnerBranch();
            if (sourceBranch != null) {
                IPart targetPart = getViewer().findPart(createdTopic);
                if (targetPart != null && targetPart instanceof ITopicPart) {
                    IBranchPart targetBranch = ((ITopicPart) targetPart)
                            .getOwnerBranch();
                    if (targetBranch != null) {
                        IReferencedFigure sourceFigure = (IReferencedFigure) sourceBranch
                                .getFigure();
                        Point sourcePosition = sourceFigure.getReference();
                        Insets sourceIns = sourceFigure
                                .getReferenceDescription();
                        IReferencedFigure targetFigure = (IReferencedFigure) targetBranch
                                .getFigure();
                        Insets targetIns = targetFigure
                                .getReferenceDescription();
                        return new Point(sourcePosition.x, sourcePosition.y
                                + sourceIns.bottom + targetIns.top + 10);
                    }
                }
            }
        }
        org.xmind.core.util.Point sourcePosition = sourceTopic.getPosition();
        if (sourcePosition != null)
            return new Point(sourcePosition.x, sourcePosition.y + 60);
        return null;
    }

}