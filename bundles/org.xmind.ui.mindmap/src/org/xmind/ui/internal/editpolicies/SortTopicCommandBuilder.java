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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.util.Point;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddBoundaryCommand;
import org.xmind.ui.commands.AddRelationshipCommand;
import org.xmind.ui.commands.AddSummaryCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.dialogs.DialogMessages;

/**
 * @author Karelun Huang
 */
public class SortTopicCommandBuilder extends DeleteCommandBuilder {

    private class Range {
        int startIndex;
        int endIndex;

        public Range(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj != this && !(obj instanceof Range))
                return false;
            Range that = (Range) obj;
            return this.startIndex == that.startIndex
                    && this.endIndex == that.endIndex;
        }
    }

    private String type;

    private Map<ITopicRange, Range> cacheRanges = null;

    private List<IRelationship> cacheRelations = null;

    private Map<ISummary, ITopic> cacheSummaryTopics = null;

    public SortTopicCommandBuilder(IViewer viewer, ICommandStack commandStack) {
        super(viewer, commandStack);
    }

    public void setSortType(String type) {
        this.type = type;
    }

    public void sort(ITopic parent) {
        List<ITopic> children = new ArrayList<ITopic>(
                parent.getChildren(ITopic.ATTACHED));
        if (children.isEmpty())
            return;
        cacheRelationships(parent);
        cacheTopicRanges(parent);
        List<ITopic> topics = resort(children);
        if (!canResort(topics)) {
            MessageDialog.openWarning(null,
                    DialogMessages.SortMessageDialog_Title,
                    DialogMessages.SortMessageDialog_Messages);
            return;
        }

        for (ITopic topic : children) {
            deleteTopic(topic, true);
        }
        children.clear();

        for (int i = 0; i < topics.size(); i++) {
            ITopic topic = topics.get(i);
            addTopic(topic, parent, i);
            modifyTopicPosition(topic);
        }
        topics.clear();

        addRelationships(parent);
        addTopicRanges(parent);
    }

    private void modifyTopicPosition(ITopic topic) {
        Point position = topic.getPosition();
        if (position == null)
            return;
        ModifyPositionCommand command = new ModifyPositionCommand(topic, null);
        add(command, true);

    }

    private boolean canResort(List<ITopic> topics) {
        if (cacheRanges == null)
            return true;
        for (Entry<ITopicRange, Range> entry : cacheRanges.entrySet()) {
            ITopicRange topicRange = entry.getKey();
            List<ITopic> enTopics = topicRange.getEnclosingTopics();
            if (enTopics.isEmpty() || enTopics.size() == 1)
                continue;
            List<Integer> newIndexList = null;
            for (ITopic topic : enTopics) {
                int index = topics.indexOf(topic);
                if (newIndexList == null)
                    newIndexList = new ArrayList<Integer>();
                newIndexList.add(index);
            }
            Collections.sort(newIndexList, new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return o1 - o2;
                }
            });
            int flag = -1;
            for (int index : newIndexList) {
                if (flag == -1) {
                    flag = index;
                    continue;
                }
                if (Math.abs(flag - index) > 1)
                    return false;
                flag = index;
            }
            int startIndex = newIndexList.get(0);
            int endIndex = newIndexList.get(newIndexList.size() - 1);
            Range range = entry.getValue();
            range.startIndex = startIndex;
            range.endIndex = endIndex;
            newIndexList.clear();
        }
        return true;
    }

    private void cacheRelationships(ITopic topic) {
        ISheet sheet = topic.getOwnedSheet();
        Set<IRelationship> relations = sheet.getRelationships();
        if (relations.isEmpty())
            return;
        Iterator<IRelationship> itera = relations.iterator();
        while (itera.hasNext()) {
            IRelationship next = itera.next();
            if (canAddInCache(topic, next)) {
                if (cacheRelations == null)
                    cacheRelations = new ArrayList<IRelationship>();
                cacheRelations.add(next);
            }
        }
    }

    private boolean canAddInCache(ITopic topic, IRelationship raltionship) {
        IWorkbook workbook = topic.getOwnedWorkbook();
        String end1Id = raltionship.getEnd1Id();
        String end2Id = raltionship.getEnd2Id();
        Object obj1 = workbook.getElementById(end1Id);
        Object obj2 = workbook.getElementById(end2Id);
        return isPosterity(topic, obj1) || isPosterity(topic, obj2);
    }

    private boolean isPosterity(ITopic parent, Object obj) {
        if (obj instanceof ITopicRange) {
            return isPosterityOfRange(parent, (ITopicRange) obj);
        } else if (obj instanceof ITopic) {
            return isPosterityOfTopic(parent, (ITopic) obj);
        }
        return false;
    }

    private boolean isPosterityOfTopic(ITopic parent, ITopic topic) {
        if (topic.isRoot())
            return false;
        ITopic parentTopic = topic.getParent();
        if (parentTopic.equals(parent))
            return true;
        if (parentTopic.isRoot())
            return false;
        return isPosterityOfTopic(parentTopic, topic);
    }

    private boolean isPosterityOfRange(ITopic parent, ITopicRange topicRange) {
        ITopic parentTopic = topicRange.getParent();
        if (parentTopic.equals(parent))
            return true;
        if (parentTopic.isRoot())
            return false;
        return isPosterityOfRange(parentTopic, topicRange);
    }

    private void addRelationships(ITopic parent) {
        if (cacheRelations == null || cacheRelations.isEmpty())
            return;
        for (IRelationship relationship : cacheRelations) {
            AddRelationshipCommand command = new AddRelationshipCommand(
                    relationship, parent.getOwnedSheet());
            add(command, true);
        }
    }

    private void addTopicRanges(ITopic parent) {
        if (cacheRanges == null || cacheRanges.isEmpty())
            return;
        for (Entry<ITopicRange, Range> entry : cacheRanges.entrySet()) {
            ITopicRange topicRange = entry.getKey();
            Range range = entry.getValue();
            if (topicRange instanceof ISummary) {
                ISummary summary = (ISummary) topicRange;
                ITopic summaryTopic = cacheSummaryTopics.get(summary);
                AddTopicCommand addTopicCommand = new AddTopicCommand(
                        summaryTopic, parent, -1, ITopic.SUMMARY);
                add(addTopicCommand, false);
                AddSummaryCommand summaryCommand = new AddSummaryCommand(
                        summary, parent);
                add(summaryCommand, false);
            } else if (topicRange instanceof IBoundary) {
                IBoundary boundary = (IBoundary) topicRange;
                AddBoundaryCommand boundaryCommand = new AddBoundaryCommand(
                        boundary, parent);
                add(boundaryCommand, false);
            }
            ModifyRangeCommand modifyStart = new ModifyRangeCommand(topicRange,
                    range.startIndex, true);
            add(modifyStart, false);
            ModifyRangeCommand modifyend = new ModifyRangeCommand(topicRange,
                    range.endIndex, false);
            add(modifyend, false);
        }
    }

    private void cacheTopicRanges(ITopic parent) {
        if (cacheRanges == null)
            cacheRanges = new HashMap<ITopicRange, Range>();
        if (cacheSummaryTopics == null)
            cacheSummaryTopics = new HashMap<ISummary, ITopic>();
        Set<IBoundary> boundaries = parent.getBoundaries();
        if (!boundaries.isEmpty()) {
            Iterator<IBoundary> itera = boundaries.iterator();
            while (itera.hasNext()) {
                IBoundary next = itera.next();
                if (!next.isMasterBoundary()) {
                    int startIndex = next.getStartIndex();
                    int endIndex = next.getEndIndex();
                    if (startIndex >= 0 && endIndex >= 0) {
                        cacheRanges.put(next, new Range(startIndex, endIndex));
                    }
                }
            }
        }
        Set<ISummary> summaries = parent.getSummaries();
        if (!summaries.isEmpty()) {
            Iterator<ISummary> itera = summaries.iterator();
            while (itera.hasNext()) {
                ISummary next = itera.next();
                int startIndex = next.getStartIndex();
                int endIndex = next.getEndIndex();
                if (startIndex >= 0 && endIndex >= 0) {
                    ITopic summaryTopic = next.getTopic();
                    cacheRanges.put(next, new Range(startIndex, endIndex));
                    cacheSummaryTopics.put(next, summaryTopic);
                }
            }
        }
    }

    private void addTopic(ITopic topic, ITopic parent, int toIndex) {
        AddTopicCommand command = new AddTopicCommand(topic, parent, toIndex,
                ITopic.ATTACHED);
        add(command, true);
    }

    private List<ITopic> resort(List<ITopic> oldTopics) {
        ArrayList<ITopic> newTopics = new ArrayList<ITopic>(oldTopics.size());
        for (ITopic topic : oldTopics)
            newTopics.add(topic);
        Collections.sort(newTopics, new Comparator<ITopic>() {
            public int compare(ITopic o1, ITopic o2) {
                if (ActionConstants.SORT_TITLE_ID.equals(type)) {
                    String text1 = o1.getTitleText();
                    String text2 = o2.getTitleText();
                    return text1.compareToIgnoreCase(text2);
                } else if (ActionConstants.SORT_PRIORITY_ID.equals(type)) {
                    int p1 = getPriority(o1);
                    int p2 = getPriority(o2);
                    return p1 - p2;
                } else if (ActionConstants.SORT_MODIFIED_ID.equals(type)) {
                    long time1 = o1.getModifiedTime();
                    long time2 = o2.getModifiedTime();
                    long ex = time1 - time2;
                    return (int) ex;
                }
                return 0;
            }

        });
        return newTopics;
    }

    private int getPriority(ITopic topic) {
        Iterator<IMarkerRef> itera = topic.getMarkerRefs().iterator();
        while (itera.hasNext()) {
            IMarkerRef next = itera.next();
            String markerId = next.getMarkerId();
            if (markerId.startsWith("priority")) { //$NON-NLS-1$
                int index = markerId.indexOf('-');
                String number = markerId.substring(index + 1);
                return Integer.parseInt(number);
            }
        }
        return Integer.MAX_VALUE;
    }
}
