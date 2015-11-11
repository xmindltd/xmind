package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractInfoDecoration;

public class RectangleInfoDecration extends AbstractInfoDecoration {

    public RectangleInfoDecration() {
        super();
    }

    public RectangleInfoDecration(String id) {
        super(id);
    }

    @Override
    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
        shape.addRectangle(box.x, box.y, box.width, box.height);
    }

}
