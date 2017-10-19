package org.xmind.ui.internal.outline.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;

public class LabelResourceForSheet extends AbstractIndexResource
        implements ILabelResource {

    private ISheet sheet;

    private Set<String> labels = new HashSet<String>();

    private Map<String, Set<ITopic>> labelToTopics = new HashMap<String, Set<ITopic>>();

    public LabelResourceForSheet(ISheet sheet) {
        Assert.isNotNull(sheet);
        this.sheet = sheet;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            labels.clear();
            labelToTopics.clear();
        }

        collectResourceForSheet(sheet);
    }

    protected void collectResourceForTopic(ITopic topic) {
        Set<String> labs = topic.getLabels();
        for (String lab : labs) {
            if (labelToTopics.containsKey(lab)) {
                Set<ITopic> topics = labelToTopics.get(lab);
                if (topics == null)
                    topics = new HashSet<ITopic>();
                topics.add(topic);
            } else {
                Set<ITopic> topics = new HashSet<ITopic>();
                topics.add(topic);
                labelToTopics.put(lab, topics);
            }
        }
        labels.addAll(labs);
    }

    public Object getSource() {
        return sheet;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public Set<ITopic> getTopics(String label) {
        return labelToTopics.get(label);
    }

    public void reset(Object source, boolean update) {
        Assert.isNotNull(source);
        this.sheet = (ISheet) source;
        init(update);
    }

}
