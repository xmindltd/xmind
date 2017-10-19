package org.xmind.ui.internal.outline.resource;

import java.util.Set;

import org.xmind.core.ITopic;

public interface ITaskDateResource extends IOutlineResource {

    public Set<String> getTaskDates();

    public Set<ITopic> getTopics(String taskDate);

    public Set<ITopic> getAllTopicsForTaskDate();

    public String getTaskDate(ITopic topic);

    public void setTaskDateResourceType(int type);

    public int getTaskDateResourceType();

}
