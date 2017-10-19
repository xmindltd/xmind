package org.xmind.ui.internal.outline.resource;

import java.util.Set;

import org.xmind.core.ITopic;

public interface IAZResource extends IOutlineResource {

    public Set<ITopic> getTopics();

    public boolean isPositiveSequence();

    public void setSequence(boolean isPositiveSequence);

}
