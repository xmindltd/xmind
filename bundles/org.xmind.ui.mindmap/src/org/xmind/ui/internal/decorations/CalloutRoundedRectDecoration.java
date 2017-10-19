package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractCalloutTopicDecoration;

public class CalloutRoundedRectDecoration extends
        AbstractCalloutTopicDecoration implements ICorneredDecoration {

    private static final double M = (1 - Math.sqrt(2) / 2) * 0.8;

    private int cornerSize = 0;

    public CalloutRoundedRectDecoration() {
        super();
    }

    public CalloutRoundedRectDecoration(String id) {
        super(id);
    }

    protected void sketch(IFigure figure, Path shape, Rectangle box, int purpose) {
        float c = getAppliedCornerSize();
        if (c == 0) {
            shape.addRectangle(box.x, box.y, box.width, box.height);
        } else {
            shape.addRoundedRectangle(box, c);
        }
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        int c = (int) (M * getAppliedCornerSize()) + getLineWidth();
        return Geometry.union(super.getPreferredInsets(figure, width, height),
                c, c, c, c);
    }

    public int getCornerSize() {
        return cornerSize;
    }

    protected int getAppliedCornerSize() {
        return getCornerSize();// * getLineWidth();
    }

    public void setCornerSize(IFigure figure, int cornerSize) {
        if (cornerSize == this.cornerSize)
            return;

        this.cornerSize = cornerSize;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            figure.repaint();
        }
    }

}
