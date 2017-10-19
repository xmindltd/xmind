package org.xmind.ui.gallery;

import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.policy.AbstractEditPolicy;

public abstract class GalleryMovablePolicy extends AbstractEditPolicy {

    @Override
    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_MOVETO.equals(requestType);
    }

    @Override
    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_MOVETO.equals(type)) {
            moveGallery(request);
        }
    }

    protected abstract void moveGallery(Request request);

}
