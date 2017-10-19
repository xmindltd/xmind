package org.xmind.ui.internal.outline.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;

public class MarkerResourceForSheet extends AbstractIndexResource
        implements IMarkerResource {

    private Set<String> markerIds = new HashSet<String>();

    private Map<String, Set<ITopic>> idToTopics = new HashMap<String, Set<ITopic>>();

    private Map<String, IMarker> idToMarker = new HashMap<String, IMarker>();

    private ISheet sheet;

    public MarkerResourceForSheet(ISheet sheet) {
        Assert.isNotNull(sheet);
        this.sheet = sheet;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            markerIds.clear();
            idToTopics.clear();
            idToMarker.clear();
        }

        collectResourceForSheet(sheet);
    }

    public Set<String> getMarkerIds() {
        return markerIds;
    }

    public IMarker getMarker(String markerId) {
        return idToMarker.get(markerId);
    }

    public Set<ITopic> getTopics(String markerId) {
        return idToTopics.get(markerId);
    }

    public Object getSource() {
        return sheet;
    }

    public void reset(Object source, boolean update) {
        Assert.isNotNull(source);
        this.sheet = (ISheet) source;
        init(update);
    }

    protected void collectResourceForTopic(ITopic topic) {
        Set<IMarkerRef> markerRefs = topic.getMarkerRefs();
        for (IMarkerRef markerRef : markerRefs) {
            IMarker marker = markerRef.getMarker();
            String markerId = marker.getId();

            markerIds.add(markerId);

            Set<String> keySet = idToTopics.keySet();
            if (keySet.contains(markerId)) {
                Set<ITopic> topicsCollected = idToTopics.get(markerId);
                if (topicsCollected == null)
                    topicsCollected = new HashSet<ITopic>();
                topicsCollected.add(topic);
                idToTopics.put(markerId, topicsCollected);
                idToMarker.put(markerId, marker);
            } else {
                Set<ITopic> topicsCollected = new HashSet<ITopic>();
                topicsCollected.add(topic);
                idToTopics.put(markerId, topicsCollected);
                idToMarker.put(markerId, marker);
            }

        }
    }

}
