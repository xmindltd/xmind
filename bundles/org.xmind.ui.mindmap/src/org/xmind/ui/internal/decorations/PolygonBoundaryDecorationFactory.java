package org.xmind.ui.internal.decorations;

import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IDecorationFactory;
import org.xmind.ui.mindmap.IBoundaryPart;

public class PolygonBoundaryDecorationFactory implements IDecorationFactory {

    public IDecoration createDecoration(String id, IGraphicalPart part) {
        return new PolygonBoundaryDecoration((IBoundaryPart) part, id);
    }

}
