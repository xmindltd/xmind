package org.xmind.ui.internal.outline.resource;

import java.util.Set;

import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;

public interface IMarkerResource extends IOutlineResource {

    public Set<String> getMarkerIds();

    public Set<ITopic> getTopics(String markerId);

    public IMarker getMarker(String markerId);

}
