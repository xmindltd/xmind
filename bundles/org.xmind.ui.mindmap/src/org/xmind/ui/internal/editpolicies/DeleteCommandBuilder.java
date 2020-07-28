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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xmind.core.IBoundary;
import org.xmind.core.IImage;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.DeleteBoundaryCommand;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.commands.DeleteRelationshipCommand;
import org.xmind.ui.commands.DeleteSummaryCommand;
import org.xmind.ui.commands.DeleteTopicCommand;
import org.xmind.ui.commands.ModifyImageAlignmentCommand;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.util.MindMapUtils;

public class DeleteCommandBuilder extends CommandBuilder {

    private static final Collection<Object> EMPTY = Collections.emptySet();

    private Stack<Object> deleting = null;

    private Set<IRelationship> relationships = null;

    private Map<ITopic, Set<ITopicRange>> subRanges = null;

    private Set<Object> deleted = null;

//    private ITopicRefCounter topicLinkRef = null;

    public DeleteCommandBuilder(IViewer viewer, CommandBuilder delegate) {
        super(viewer, delegate);
    }

    public DeleteCommandBuilder(IViewer viewer, ICommandStack commandStack) {
        super(viewer, commandStack);
    }

    public void delete(Object element) {
        if (element instanceof ITopic) {
            deleteTopic((ITopic) element, true);
        } else if (element instanceof IBoundary) {
            deleteBoundary((IBoundary) element, true);
        } else if (element instanceof ISummary) {
            deleteSummary((ISummary) element, true);
        } else if (element instanceof IRelationship) {
            deleteRelationship((IRelationship) element, true);
        } else if (element instanceof IMarkerRef) {
            deleteMarkerRef((IMarkerRef) element, true);
        } else if (element instanceof IImage) {
            deleteImage((IImage) element, true);
        }
    }

    protected boolean startDeleting(Object element) {
        if (isDeleting(element) || isDeleted(element))
            return false;

        if (deleting == null)
            deleting = new Stack<Object>();
        deleting.push(element);
        return true;
    }

    protected boolean isDeleting(Object element) {
        return deleting != null && deleting.contains(element);
    }

    protected void endDeleting() {
        if (deleting != null) {
            deleting.pop();
        }
    }

    protected void addDeleted(Object element) {
        if (deleted == null)
            deleted = new HashSet<Object>();
        deleted.add(element);
    }

    protected void removeDeleted(Object element) {
        if (deleted != null) {
            deleted.remove(element);
        }
    }

    protected boolean isDeleted(Object element) {
        return deleted != null && deleted.contains(element);
    }

    protected Collection<Object> getDeleted() {
        return deleted == null ? EMPTY : deleted;
    }

    protected Collection<Object> getDeleting() {
        return deleting == null ? EMPTY : deleting;
    }

//    protected void deleteTopic(ITopic topic, boolean isCutPrev,
//            boolean sourceCollectable) {
////        modifyTopicLinkRef(topic, isCutPrev);
//        deleteTopic(topic, sourceCollectable);
//    }

    protected void deleteTopic(ITopic topic, boolean sourceCollectable) {
        if (!startDeleting(topic))
            return;

        deleteRelsByTopic(topic);

        ITopic parent = topic.getParent();
        if (parent != null) {
            String topicType = topic.getType();
            if (ITopic.ATTACHED.equals(topicType)) {
                Set<ITopicRange> ranges = getSubRanges(parent);
                deleteTopicInRanges(topic, ranges, parent);
            } else if (ITopic.SUMMARY.equals(topicType)) {
                ISummary summary = findSummaryBySummaryTopic(topic, parent);
                if (summary != null) {
                    deleteSummary(summary, false);
                }
            }
        }
        add(new DeleteTopicCommand(topic), sourceCollectable);

        addDeleted(topic);
        endDeleting();
    }

    protected void deleteTopicInRanges(ITopic topic, Set<ITopicRange> ranges,
            ITopic parent) {
        if (ranges.isEmpty())
            return;

        int index = topic.getIndex();
        if (index < 0)
            return;

        for (Object o : ranges.toArray()) {
            ITopicRange range = (ITopicRange) o;
            int start = range.getStartIndex();
            int end = range.getEndIndex();
            if (start == end && start == index) {
                deleteTopicRange(range);
            } else {
                if (start > index) {
                    add(new ModifyRangeCommand(range, start - 1, true), false);
                }
                if (end >= index) {
                    add(new ModifyRangeCommand(range, end - 1, false), false);
                }
            }
        }
    }

    protected void deleteTopicRange(ITopicRange range) {
        if (range instanceof IBoundary) {
            deleteBoundary((IBoundary) range, false);
        } else if (range instanceof ISummary) {
            deleteSummary((ISummary) range, false);
        }
    }

    protected void deleteRelsByTopic(ITopic topic) {
        deleteRelByRelEnd(topic);
        for (ITopic child : topic.getAllChildren()) {
            deleteRelsByTopic(child);
        }
        for (IBoundary boundary : topic.getBoundaries()) {
            deleteRelByRelEnd(boundary);
        }
    }

//    private void modifyTopicLinkRef(ITopic topic, boolean isCutPrev) {
//        if (topicLinkRef == null) {
//            IWorkbook workbook = topic.getOwnedWorkbook();
//            topicLinkRef = (ITopicRefCounter) workbook
//                    .getAdapter(ITopicRefCounter.class);
//        }
//
//        String targetId = topic.getId();
//        List<ITopic> linkedTopics = topicLinkRef.getLinkTopics(targetId);
//        if (linkedTopics != null && !linkedTopics.isEmpty()) {
//            ModifyTopicHyperlinkCommand command = new ModifyTopicHyperlinkCommand(
//                    linkedTopics, null);
//            add(command, false);
//
//            if (!isCutPrev) {
////            topicLinkRef.removeTopicLinks(targetId);
//                ModifyTopicLinkCommand cmd = new ModifyTopicLinkCommand(
//                        linkedTopics, null);
//                add(cmd, false);
//            }
//        }
//
//        List<ITopic> children = topic.getAllChildren();
//        if (children != null && !children.isEmpty()) {
//            for (ITopic child : children) {
//                modifyTopicLinkRef(child, isCutPrev);
//            }
//        }
//    }

    protected void deleteRelByRelEnd(IRelationshipEnd end) {
        if (hasRelationship()) {
            String id = end.getId();
            for (Object o : getRelationships().toArray()) {
                IRelationship r = (IRelationship) o;
                if (id.equals(r.getEnd1Id()) || id.equals(r.getEnd2Id())) {
                    deleteRelationship(r, false);
                }
            }
        }
    }

    protected boolean hasRelationship() {
        return !getRelationships().isEmpty();
    }

    protected Set<IRelationship> getRelationships() {
        if (relationships == null) {
            relationships = new HashSet<IRelationship>();
            ISheet sheet = (ISheet) getViewer().getAdapter(ISheet.class);
            if (sheet != null) {
                relationships.addAll(sheet.getRelationships());
            }
        }
        return relationships;
    }

    protected ISummary findSummaryBySummaryTopic(ITopic topic, ITopic parent) {
        return MindMapUtils.findSummaryBySummaryTopic(topic, parent,
                getSubRanges(parent));
    }

    protected Set<ITopicRange> getSubRanges(ITopic parent) {
        if (subRanges == null)
            subRanges = new HashMap<ITopic, Set<ITopicRange>>();
        Set<ITopicRange> ranges = subRanges.get(parent);
        if (ranges == null) {
            ranges = new HashSet<ITopicRange>();
            if (parent != null) {
                ranges.addAll(parent.getBoundaries());
                ranges.addAll(parent.getSummaries());
                subRanges.put(parent, ranges);
            }
        }
        return ranges;
    }

    protected void removeSubRange(ITopicRange range, ITopic parent) {
        if (subRanges != null) {
            Set<ITopicRange> ranges = subRanges.get(parent);
            if (ranges != null) {
                ranges.remove(range);
            }
        }
    }

    protected void addSubRange(ITopicRange range, ITopic parent) {
        if (subRanges == null)
            subRanges = new HashMap<ITopic, Set<ITopicRange>>();
        Set<ITopicRange> ranges = subRanges.get(parent);
        if (ranges == null) {
            ranges = new HashSet<ITopicRange>();
        }
        ranges.add(range);
    }

    protected void deleteBoundary(IBoundary boundary,
            boolean sourceCollectable) {
        if (!startDeleting(boundary))
            return;

        deleteRelByRelEnd(boundary);

        ITopic parent = boundary.getParent();
        add(new DeleteBoundaryCommand(boundary), sourceCollectable);
        removeSubRange(boundary, parent);

        endDeleting();
        addDeleted(boundary);
    }

    protected void deleteSummary(ISummary summary, boolean sourceCollectable) {
        if (!startDeleting(summary))
            return;

        ITopic parent = summary.getParent();
        ITopic summaryTopic = summary.getTopic();
        if (summaryTopic != null) {
            deleteTopic(summaryTopic, false);
        }
        add(new DeleteSummaryCommand(summary), sourceCollectable);
        removeSubRange(summary, parent);

        endDeleting();
        addDeleted(summary);
    }

    protected void deleteRelationship(IRelationship relationship,
            boolean sourceCollectable) {
        if (!startDeleting(relationship))
            return;

        add(new DeleteRelationshipCommand(relationship), sourceCollectable);
        if (relationships != null) {
            relationships.remove(relationship);
        }

        endDeleting();
        addDeleted(relationship);
    }

    protected void deleteMarkerRef(IMarkerRef markerRef,
            boolean sourceCollectable) {
        if (!startDeleting(markerRef))
            return;

        add(new DeleteMarkerCommand(markerRef), sourceCollectable);

        endDeleting();
        addDeleted(markerRef);
    }

    protected void deleteImage(IImage image, boolean sourceCollectable) {
        if (!startDeleting(image))
            return;

        add(new ModifyImageSourceCommand(image, null), sourceCollectable);
        add(new ModifyImageAlignmentCommand(image, null), sourceCollectable);
        add(new ModifyImageSizeCommand(image, IImage.UNSPECIFIED,
                IImage.UNSPECIFIED), sourceCollectable);

        endDeleting();
        addDeleted(image);
    }

}