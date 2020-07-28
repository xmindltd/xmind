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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddBoundaryCommand;
import org.xmind.ui.commands.AddSummaryCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CloneTopicCommand;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.DeleteBoundaryCommand;
import org.xmind.ui.commands.DeleteSummaryCommand;
import org.xmind.ui.commands.DeleteTopicCommand;
import org.xmind.ui.commands.ModifyBoundaryMasterCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.util.MindMapUtils;

public class TopicMoveCommandBuilder extends DeleteCommandBuilder {

    private static class TopicInfo {
        public ITopic oldParent;
        public int oldIndex;
        public String oldType;
        public Point newPosition;
        public boolean needsReorganize;
    }

    private static final List<ITopic> EMPTY_TOPICS = Collections.emptyList();

    private ITopic targetParent;

    private int targetIndex;

    private String targetType;

    private Point targetPosition;

    private boolean relative;

    private int insertIndex;

    private Set<ITopic> cachedParent = null;

    private Map<ITopicRange, List<ITopic>> oldRangedTopics = null;

    private Set<ITopicRange> rangesToMove = null;

    public TopicMoveCommandBuilder(IViewer viewer, CommandBuilder delegate,
            ITopic targetParent, int targetIndex, String targetType,
            Point targetPosition, boolean relative) {
        super(viewer, delegate);
        init(targetParent, targetIndex, targetType, targetPosition, relative);
    }

    public TopicMoveCommandBuilder(IViewer viewer, ICommandStack commandStack,
            ITopic targetParent, int targetIndex, String targetType,
            Point targetPosition, boolean relative) {
        super(viewer, commandStack);
        init(targetParent, targetIndex, targetType, targetPosition, relative);
    }

    private void init(ITopic targetParent, int targetIndex, String targetType,
            Point targetPosition, boolean relative) {
        this.targetParent = targetParent;
        this.targetIndex = targetIndex;
        this.targetType = targetType;
        this.targetPosition = targetPosition;
        this.relative = relative;

        this.insertIndex = targetIndex;
        if (this.insertIndex < 0) {
            this.insertIndex = targetParent.getChildren(targetType).size();
        }
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public ITopic getTargetParent() {
        return targetParent;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    public String getTargetType() {
        return targetType;
    }

    public boolean isRelative() {
        return relative;
    }

    public int getInsertIndex() {
        return insertIndex;
    }

//    public void moveTopic(ITopic topic) {
//        moveTopic(topic, insertIndex);
//        insertIndex++;
//    }
//
//    public void copyTopic(ITopic topic) {
//        copyTopic(topic, insertIndex);
//        insertIndex++;
//    }

    public void copyTopics(List<ITopic> topics) {
        for (ITopic topic : topics) {
            copyTopic(topic, insertIndex);
            insertIndex++;
        }
    }

    public void moveTopics(List<ITopic> topics) {
        Map<ITopic, TopicInfo> oldInfo = new HashMap<ITopic, TopicInfo>(
                topics.size());
        for (ITopic topic : topics) {
            TopicInfo info = deleteTopic(topic, insertIndex);
            if (!oldInfo.containsKey(topic)) {
                oldInfo.put(topic, info);
            }
        }
        for (ITopic topic : topics) {
            moveTopic(topic, insertIndex, oldInfo.get(topic));
            insertIndex++;
        }
    }

    private TopicInfo deleteTopic(ITopic topic, int toIndex) {
        TopicInfo info = new TopicInfo();
        info.oldParent = topic.getParent();
        info.oldIndex = topic.getIndex();
        info.oldType = topic.getType();
        info.newPosition = calculateTargetPosition(topic);
        info.needsReorganize = needsReorganize(topic, toIndex, info.oldParent,
                info.oldIndex, info.oldType);
        if (info.needsReorganize) {
            if (!isCached(info.oldParent)) {
                for (ITopicRange range : getSubRanges(info.oldParent)) {
                    cacheOldRangedTopics(range, info.oldParent);
                }
            }
            deleteTopic(topic, true);
        }
        return info;
    }

    private void moveTopic(ITopic topic, int toIndex, TopicInfo info) {
//        ITopic oldParent = topic.getParent();
//        int oldIndex = topic.getIndex();
//        String oldType = topic.getType();
//        Point toPosition = calculateTargetPosition(topic);
//        boolean needsReorganize = needsReorganize(topic, toIndex, oldParent,
//                oldIndex, oldType);
//        if (needsReorganize) {
//            if (!isCached(oldParent)) {
//                for (ITopicRange range : getSubRanges(oldParent)) {
//                    cacheOldRangedTopics(range, oldParent);
//                }
//            }
//            deleteTopic(topic, true);
//        }

        add(new ModifyPositionCommand(topic,
                MindMapUtils.toModelPosition(info.newPosition)),
                !info.needsReorganize);
        if (info.needsReorganize) {
            addTopic(topic, getTargetParent(), toIndex, getTargetType(), true);
            if (rangesToMove != null && !rangesToMove.isEmpty()) {
                while (!rangesToMove.isEmpty()) {
                    ITopicRange range = rangesToMove.iterator().next();
                    moveRange(range, range.getParent());
                    rangesToMove.remove(range);
                }
            }
        }
    }

    private void addTopic(ITopic topic, ITopic toParent, int toIndex,
            String toType, boolean topicCollectable) {
        add(new AddTopicCommand(topic, toParent, toIndex, toType),
                topicCollectable);
        if (ITopic.ATTACHED.equals(toType)) {
            int topicIndex = topic.getIndex();
            enlargeOldBoundaries(topic, toParent, topicIndex);
            moveMasterBoundaries(topic, topicIndex);
        }
    }

    private void enlargeOldBoundaries(ITopic topic, ITopic parent, int index) {
        Set<ITopicRange> ranges = getSubRanges(parent);
        for (ITopicRange range : ranges) {
            int startIndex = range.getStartIndex();
            int endIndex = range.getEndIndex();
            if (index <= endIndex) {
                add(new ModifyRangeCommand(range, endIndex + 1, false), false);
            }
            if (index <= startIndex) {
                add(new ModifyRangeCommand(range, startIndex + 1, true), false);
            }
        }
    }

    private void moveMasterBoundaries(ITopic topic, int topicIndex) {
        IBoundary masterBoundary = null;
        for (Object o : getSubRanges(topic).toArray()) {
            ITopicRange range = (ITopicRange) o;
            if (range instanceof IBoundary) {
                IBoundary boundary = (IBoundary) range;
                if (boundary.isMasterBoundary() && masterBoundary == null) {
                    masterBoundary = boundary;
                    addDeleteRangeCommand(boundary, topic);
                }
            }
        }

        if (masterBoundary != null) {
            IBoundary existingSingleBoundary = findSingleBoundary(
                    getTargetParent(), topicIndex);
            if (existingSingleBoundary == null) {
                add(new ModifyBoundaryMasterCommand(masterBoundary, false),
                        false);
                add(new ModifyRangeCommand(masterBoundary, topicIndex, true),
                        false);
                add(new ModifyRangeCommand(masterBoundary, topicIndex, false),
                        false);
                addAddRangeCommand(masterBoundary, getTargetParent());
            }
        }
    }

    private IBoundary findSingleBoundary(ITopic parent, int childIndex) {
        for (ITopicRange range : getSubRanges(parent)) {
            if (range instanceof IBoundary
                    && range.getStartIndex() == childIndex
                    && range.getEndIndex() == childIndex)
                return (IBoundary) range;
        }
        return null;
    }

    private IBoundary findMasterBoundary(ITopic parent) {
        for (ITopicRange range : getSubRanges(parent)) {
            if (range instanceof IBoundary
                    && ((IBoundary) range).isMasterBoundary())
                return (IBoundary) range;
        }
        return null;
    }

    private void moveRange(ITopicRange range, ITopic oldParent) {
        ITopic summaryTopic = null;
        if (range instanceof ISummary) {
            summaryTopic = ((ISummary) range).getTopic();
            if (summaryTopic != null) {
                add(new DeleteTopicCommand(summaryTopic), false);
            }
        }
        addDeleteRangeCommand(range, oldParent);
        List<ITopic> rangedTopics = getOldRangedTopics(range);
        if (!rangedTopics.isEmpty()) {
            if (ITopic.ATTACHED.equals(getTargetType())) {
                int startIndex = rangedTopics.get(0).getIndex();
                int endIndex = rangedTopics.get(rangedTopics.size() - 1)
                        .getIndex();
                add(new ModifyRangeCommand(range, startIndex, true), false);
                add(new ModifyRangeCommand(range, endIndex, false), false);
                addAddRangeCommand(range, getTargetParent());
                if (summaryTopic != null) {
                    addTopic(summaryTopic, getTargetParent(), -1,
                            ITopic.SUMMARY, false);
                }
            } else if (range instanceof IBoundary && rangedTopics.size() == 1) {
                ITopic topic = rangedTopics.get(0);
                IBoundary overallBoundary = findMasterBoundary(topic);
                if (overallBoundary == null) {
                    add(new ModifyRangeCommand(range, -1, true), false);
                    add(new ModifyRangeCommand(range, -1, false), false);
                    add(new ModifyBoundaryMasterCommand((IBoundary) range,
                            true), false);
                    addAddRangeCommand(range, topic);
                }
            }
        }
    }

    private void addDeleteRangeCommand(ITopicRange range, ITopic oldParent) {
        if (range instanceof IBoundary) {
            add(new DeleteBoundaryCommand((IBoundary) range), false);
        } else if (range instanceof ISummary) {
            add(new DeleteSummaryCommand((ISummary) range), false);
        }
        removeSubRange(range, oldParent);
    }

    private void addAddRangeCommand(ITopicRange range, ITopic newParent) {
        if (range instanceof IBoundary) {
            add(new AddBoundaryCommand((IBoundary) range, newParent), false);
        } else if (range instanceof ISummary) {
            add(new AddSummaryCommand((ISummary) range, newParent), false);
        }
        addSubRange(range, newParent);
    }

    protected void deleteBoundary(IBoundary boundary,
            boolean sourceCollectable) {
        if (rangesToMove == null)
            rangesToMove = new HashSet<ITopicRange>();
        rangesToMove.add(boundary);
    }

    protected void deleteSummary(ISummary summary, boolean sourceCollectable) {
        if (rangesToMove == null)
            rangesToMove = new HashSet<ITopicRange>();
        rangesToMove.add(summary);
    }

    protected void deleteRelationship(IRelationship relationship,
            boolean sourceCollectable) {
    }

    private Point calculateTargetPosition(ITopic topic) {
        Point position = getTargetPosition();
        if (position == null)
            return null;

        IPart parentPart = getViewer().findPart(getTargetParent());
        if (parentPart == null)
            return null;

        Point parentPosition = getPosition((IGraphicalPart) parentPart);
        if (parentPosition == null)
            return null;

        if (isRelative()) {
            IPart topicPart = getViewer().findPart(topic);
            if (topicPart == null)
                return null;

            Point oldPosition = getPosition((IGraphicalPart) topicPart);
            return toNewPosition(parentPosition, oldPosition, position);
        }
        return toNewPosition(parentPosition, position);
    }

    protected static Point getPosition(IGraphicalPart part) {
        IFigure figure = part.getFigure();
        if (figure instanceof IReferencedFigure) {
            return ((IReferencedFigure) figure).getReference();
        }
        return figure.getBounds().getLocation();
    }

    private static Point toNewPosition(Point parentPosition, Point oldPosition,
            Point position) {
        int x = oldPosition.x + position.x - parentPosition.x;
        int y = oldPosition.y + position.y - parentPosition.y;
        return new Point(x, y);
    }

    private static Point toNewPosition(Point parentPosition, Point position) {
        int x = position.x - parentPosition.x;
        int y = position.y - parentPosition.y;
        return new Point(x, y);
    }

    private boolean needsReorganize(ITopic topic, int toIndex, ITopic oldParent,
            int oldIndex, String oldType) {
        if (!getTargetParent().equals(oldParent))
            return true;
        if (oldType != getTargetType()
                && (oldType == null || !oldType.equals(getTargetType())))
            return true;

        if (ITopic.ATTACHED.equals(oldType)
                && oldIndex != getValidAttachedIndex(oldParent, toIndex))
            return true;
        return false;
    }

    private int getValidAttachedIndex(ITopic parent, int index) {
        int size = parent.getChildren(ITopic.ATTACHED).size();
        return index >= size ? index - 1 : index;
    }

    protected boolean isCached(ITopic parent) {
        return cachedParent != null && cachedParent.contains(parent);
    }

    protected void cacheOldRangedTopics(ITopicRange range, ITopic parent) {
        if (cachedParent == null) {
            cachedParent = new HashSet<ITopic>();
        }
        cachedParent.add(parent);
        if (oldRangedTopics == null)
            oldRangedTopics = new HashMap<ITopicRange, List<ITopic>>();
        List<ITopic> list = oldRangedTopics.get(range);
        if (list == null) {
            list = new ArrayList<ITopic>(range.getEnclosingTopics());
            oldRangedTopics.put(range, list);
        }
    }

    protected List<ITopic> getOldRangedTopics(ITopicRange range) {
        if (oldRangedTopics != null) {
            List<ITopic> list = oldRangedTopics.get(range);
            if (list != null)
                return list;
        }
        return EMPTY_TOPICS;
    }

    private void copyTopic(ITopic topic, int toIndex) {
        Point position = calculateTargetPosition(topic);
        CloneTopicCommand clone = new CloneTopicCommand(
                getTargetParent().getOwnedWorkbook(), topic);
        add(clone, true);
        ITopic clonedTopic = (ITopic) clone.getSource();
        add(new ModifyPositionCommand(clonedTopic,
                MindMapUtils.toModelPosition(position)), false);
        addTopic(clonedTopic, getTargetParent(), toIndex, getTargetType(),
                true);
    }

}