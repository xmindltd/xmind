package org.xmind.ui.internal.outline.resource;

import java.util.Set;

import org.xmind.core.ITopic;

public interface ILabelResource extends IOutlineResource {

    public Set<String> getLabels();

    public Set<ITopic> getTopics(String label);

}
