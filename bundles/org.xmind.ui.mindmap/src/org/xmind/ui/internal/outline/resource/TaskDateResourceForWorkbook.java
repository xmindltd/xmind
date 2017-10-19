package org.xmind.ui.internal.outline.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.ui.internal.outline.OutlineIndexModelPart;
import org.xmind.ui.mindmap.IWorkbookRef;

public class TaskDateResourceForWorkbook extends AbstractIndexResource
        implements ITaskDateResource {

    private IWorkbookRef workbookRef;

    private int taskDateType;

    private Set<String> startDates = new HashSet<String>();

    private Map<String, Set<ITopic>> startDateToTopics = new HashMap<String, Set<ITopic>>();

    private Set<String> endDates = new HashSet<String>();

    private Map<String, Set<ITopic>> endDateToTopics = new HashMap<String, Set<ITopic>>();

    private Set<ITopic> allEndDateTopics = new HashSet<ITopic>();

    private Set<ITopic> allStartDateTopics = new HashSet<ITopic>();

    private Map<ITopic, String> topicToEndDate = new HashMap<ITopic, String>();

    private Map<ITopic, String> topicToStartDate = new HashMap<ITopic, String>();

    public TaskDateResourceForWorkbook(IWorkbookRef workbookRef,
            int taskDateType) {
        Assert.isNotNull(workbookRef);
        this.workbookRef = workbookRef;
        this.taskDateType = taskDateType;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            startDates.clear();
            startDateToTopics.clear();
            endDates.clear();
            endDateToTopics.clear();
        }

        collectResourceForWorkbook(workbookRef);
    }

    public Object getSource() {
        return workbookRef;
    }

    public void reset(Object source, boolean update) {
        Assert.isNotNull(source);
        this.workbookRef = (IWorkbookRef) source;
        init(update);
    }

    protected void collectResourceForTopic(ITopic topic) {
        ITopicExtension ext = topic.getExtension("org.xmind.ui.taskInfo"); //$NON-NLS-1$
        if (ext == null)
            return;
        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE)
            collectStartDateResource(topic, ext);
        else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
            collectEndDateResource(topic, ext);
    }

    private void collectStartDateResource(ITopic topic, ITopicExtension ext) {
        ITopicExtensionElement content = ext.getContent();
        List<ITopicExtensionElement> children = content
                .getChildren("start-date"); //$NON-NLS-1$
        if (!children.isEmpty()) {
            for (ITopicExtensionElement element : children) {
                String assignee = element.getTextContent();
                topicToStartDate.put(topic, assignee);
                allStartDateTopics.add(topic);

                if (startDateToTopics.containsKey(assignee)) {
                    Set<ITopic> assignedTopics = startDateToTopics
                            .get(assignee);
                    if (assignedTopics == null) {
                        assignedTopics = new HashSet<ITopic>();
                        startDateToTopics.put(assignee, assignedTopics);
                    }
                    assignedTopics.add(topic);
                } else {
                    startDates.add(assignee);
                    Set<ITopic> assignedTopics = new HashSet<ITopic>();
                    assignedTopics.add(topic);
                    startDateToTopics.put(assignee, assignedTopics);
                }
            }
        }
    }

    private void collectEndDateResource(ITopic topic, ITopicExtension ext) {
        ITopicExtensionElement content = ext.getContent();
        List<ITopicExtensionElement> children = content.getChildren("end-date"); //$NON-NLS-1$
        if (!children.isEmpty()) {
            for (ITopicExtensionElement element : children) {
                String assignee = element.getTextContent();
                topicToEndDate.put(topic, assignee);
                allEndDateTopics.add(topic);

                if (endDateToTopics.containsKey(assignee)) {
                    Set<ITopic> assignedTopics = endDateToTopics.get(assignee);
                    if (assignedTopics == null) {
                        assignedTopics = new HashSet<ITopic>();
                        endDateToTopics.put(assignee, assignedTopics);
                    }
                    assignedTopics.add(topic);
                } else {
                    endDates.add(assignee);
                    Set<ITopic> assignedTopics = new HashSet<ITopic>();
                    assignedTopics.add(topic);
                    endDateToTopics.put(assignee, assignedTopics);
                }
            }
        }
    }

    public Set<String> getTaskDates() {
        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE)
            return startDates;
        else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
            return endDates;
        return new HashSet<String>();
    }

    public Set<ITopic> getTopics(String taskDate) {
        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE)
            return startDateToTopics.get(taskDate);
        else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
            return endDateToTopics.get(taskDate);
        return new HashSet<ITopic>();
    }

    public void setTaskDateResourceType(int type) {
        this.taskDateType = type;
    }

    public Set<ITopic> getAllTopicsForTaskDate() {
        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE)
            return allStartDateTopics;
        else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
            return allEndDateTopics;
        return new HashSet<ITopic>();
    }

    public String getTaskDate(ITopic topic) {
        if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_STARTDATE)
            return topicToStartDate.get(topic);
        else if (taskDateType == OutlineIndexModelPart.OUTLINE_TYPE_BY_ENDDATE)
            return topicToEndDate.get(topic);
        return null;
    }

    public int getTaskDateResourceType() {
        return taskDateType;
    }

}
