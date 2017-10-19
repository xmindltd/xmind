package org.xmind.ui.internal.outline.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.ui.mindmap.IWorkbookRef;

public class MarkerResourceForWorkbook extends AbstractIndexResource
        implements IMarkerResource {

    private Set<String> markerIds = new HashSet<String>();

    private Map<String, Set<ITopic>> idToTopics = new HashMap<String, Set<ITopic>>();

    private Map<String, IMarker> idToMarker = new HashMap<String, IMarker>();

    private IWorkbookRef workbookRef;

    public MarkerResourceForWorkbook(IWorkbookRef workbookRef) {
        Assert.isNotNull(workbookRef);
        this.workbookRef = workbookRef;
        init(false);
    }

    private void init(boolean update) {
        if (update) {
            markerIds.clear();
            idToTopics.clear();
            idToMarker.clear();
        }

        collectResourceForWorkbook(workbookRef);
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
        return workbookRef;
    }

    public void reset(Object source, boolean update) {
        Assert.isNotNull(source);
        this.workbookRef = (IWorkbookRef) source;
        init(update);
    }

    protected void collectResourceForTopic(ITopic topic) {
        for (IMarkerRef markerRef : topic.getMarkerRefs()) {

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
