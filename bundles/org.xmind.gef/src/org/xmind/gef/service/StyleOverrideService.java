package org.xmind.gef.service;

import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;

public class StyleOverrideService extends GraphicalViewerService implements
        IStyleValueProvider {

    public StyleOverrideService(IGraphicalViewer viewer) {
        super(viewer);
    }

    public boolean isKeyInteresting(IGraphicalPart part, String key) {
        return false;
    }

    public String getValue(IGraphicalPart part, String key) {
        return null;
    }

    @Override
    protected void activate() {
    }

    @Override
    protected void deactivate() {
    }

}
