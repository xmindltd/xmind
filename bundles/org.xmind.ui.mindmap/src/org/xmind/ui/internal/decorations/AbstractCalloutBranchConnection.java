package org.xmind.ui.internal.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.ui.decorations.AbstractBranchConnection;

public abstract class AbstractCalloutBranchConnection
        extends AbstractBranchConnection implements IShapeDecoration {

    private boolean gradient = false;

    private Color fillColor = null;

    private int fillAlpha = 0xFF;

    private int lineAlpha = 0xFF;

    public AbstractCalloutBranchConnection() {
        super();
    }

    public AbstractCalloutBranchConnection(String id) {
        super(id);
    }

    protected Rectangle getOutlineBox(Rectangle box) {
        Rectangle ret = box.getCopy();
        int w = Math.min(ret.width - 1,
                Math.min(ret.height - 1, getLineWidth()));
        int half = w - w / 2;
        return ret.shrink(half, half).resize(-1, -1);
    }

    protected void drawLine(IFigure figure, Graphics graphics) {
        Path shape = new Path(Display.getCurrent());
        route(figure, shape);
        if (getFillColor() != null) {
            Color bg = graphics.getBackgroundColor();
            graphics.setBackgroundColor(getFillColor());
            paintPath(figure, graphics, shape, true);
            graphics.setBackgroundColor(bg);
        }
        if (graphics.getForegroundColor() != null)
            paintPath(figure, graphics, shape, false);
        shape.dispose();
    }

    @Override
    protected boolean usesFill() {
        return true;
    }

    public int getFillAlpha() {
        return fillAlpha;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public int getLineAlpha() {
        return lineAlpha;
    }

    public boolean isGradient() {
        return gradient;
    }

    public void setFillAlpha(IFigure figure, int alpha) {
        if (alpha == this.fillAlpha)
            return;
        this.fillAlpha = alpha;
        if (figure != null) {
            repaint(figure);
        }
    }

    public void setFillColor(IFigure figure, Color c) {
        if (c == this.fillColor || (c != null && c.equals(this.fillColor)))
            return;
        this.fillColor = c;
        if (figure != null) {
            repaint(figure);
        }
    }

    public void setGradient(IFigure figure, boolean gradient) {
        gradient = gradient && GEF.IS_PLATFORM_SUPPORT_GRADIENT;
        if (gradient == this.gradient)
            return;
        this.gradient = gradient;
        if (figure != null) {
            repaint(figure);
        }
    }

    public void setLineAlpha(IFigure figure, int alpha) {
        if (alpha == this.lineAlpha)
            return;
        this.lineAlpha = alpha;
        if (figure != null) {
            repaint(figure);
        }
    }

}
