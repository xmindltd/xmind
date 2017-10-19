package org.xmind.ui.internal.outline.resource;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;

public class AZResourceForSheet extends AbstractIndexResource
        implements IAZResource {

    private ISheet sheet;

    private Set<ITopic> topics = new HashSet<ITopic>();

    private boolean isPositiveSequence;

    public AZResourceForSheet(ISheet sheet) {
        Assert.isNotNull(sheet);
        this.sheet = sheet;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            topics.clear();
        }

        collectResourceForSheet(sheet);
    }

    public Object getSource() {
        return sheet;
    }

    public void reset(Object source, boolean update) {
        Assert.isNotNull(source);
        this.sheet = (ISheet) source;
        init(update);
    }

    public Set<ITopic> getTopics() {
        return topics;
    }

    protected void collectResourceForTopic(ITopic topic) {
        topics.add(topic);
    }

    public boolean isPositiveSequence() {
        return isPositiveSequence;
    }

    public void setSequence(boolean isPositiveSequence) {
        this.isPositiveSequence = isPositiveSequence;
    }

}
