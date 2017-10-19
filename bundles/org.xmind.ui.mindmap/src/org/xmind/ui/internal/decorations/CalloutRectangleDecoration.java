package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractCalloutTopicDecoration;

public class CalloutRectangleDecoration extends AbstractCalloutTopicDecoration {

    public CalloutRectangleDecoration() {
        super();
    }

    public CalloutRectangleDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
        shape.addRectangle(box.x, box.y, box.width, box.height);
        shape.close();
    }

}
