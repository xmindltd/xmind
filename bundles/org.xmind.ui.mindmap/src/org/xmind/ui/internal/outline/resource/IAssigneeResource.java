package org.xmind.ui.internal.outline.resource;

import java.util.Set;

import org.xmind.core.ITopic;

public interface IAssigneeResource extends IOutlineResource {

    public Set<String> getAssignees();

    public Set<ITopic> getTopics(String assignee);

}
