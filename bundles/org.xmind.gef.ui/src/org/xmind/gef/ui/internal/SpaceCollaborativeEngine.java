package org.xmind.gef.ui.internal;

import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.ui.gallery.ContentPane;

public class SpaceCollaborativeEngine {

    private WeakHashMap<Object, ContentPane> managedContentPanes = new WeakHashMap<Object, ContentPane>();

    public void register(Object key, ContentPane contentPane) {
        managedContentPanes.put(key, contentPane);
    }

    public void refreshMinorSpace() {
        int maxCount = 0;
        ContentPane contentPane = null;
        Set<Object> keys = managedContentPanes.keySet();
        for (Object key : keys) {
            ContentPane cp = managedContentPanes.get(key);
            int size = cp.getChildren().size();
            if (size >= maxCount) {
                contentPane = cp;
                maxCount = size;
            }
        }
        List elements = contentPane.getChildren();
        if (contentPane == null || elements.isEmpty())
            return;

        Rectangle clientArea = contentPane.getClientArea();
        int totalSize = clientArea.width;
        int elementSize = ((IFigure) elements.get(0)).getPreferredSize(-1,
                -1).width;

        int calculatedCount = totalSize / elementSize;
        int calculatedTotalSpace = totalSize - calculatedCount * elementSize;

        int calculatedEachSpace = 0;
        if (calculatedCount > 1) {
            calculatedEachSpace = calculatedTotalSpace / (calculatedCount - 1);
        }

        Set<Object> managedKeys = managedContentPanes.keySet();
        for (Object key : managedKeys) {
            ContentPane cp = managedContentPanes.get(key);
            int oldSpace = cp.getMinorSpacing();
            if (oldSpace != calculatedEachSpace) {
                cp.setMinorSpacing(calculatedEachSpace);
            }
        }
    }

}
