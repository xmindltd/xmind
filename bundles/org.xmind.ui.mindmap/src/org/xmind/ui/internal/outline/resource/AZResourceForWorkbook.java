package org.xmind.ui.internal.outline.resource;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.ui.mindmap.IWorkbookRef;

public class AZResourceForWorkbook extends AbstractIndexResource
        implements IAZResource {

    private IWorkbookRef workbookRef;

    private Set<ITopic> topics = new HashSet<ITopic>();

    private boolean isPositiveSequence;

    public AZResourceForWorkbook(IWorkbookRef workbookRef) {
        Assert.isNotNull(workbookRef);
        this.workbookRef = workbookRef;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            topics.clear();
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
