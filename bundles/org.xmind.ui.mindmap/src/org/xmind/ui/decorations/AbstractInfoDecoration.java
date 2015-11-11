package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.xmind.gef.draw2d.decoration.PathShapeDecoration;

public abstract class AbstractInfoDecoration extends PathShapeDecoration
        implements IInfoDecoration {

    private int left = 0;

    private int top = 0;

    private int right = 0;

    private int bottom = 0;

    public AbstractInfoDecoration() {
        super();
    }

    public AbstractInfoDecoration(String id) {
        super(id);
    }

    public Insets getPreferredInsets(IFigure figure, int width, int height) {
        return new Insets(getTopMargin() + getLineWidth(), getLeftMargin()
                + getLineWidth(), getBottomMargin() + getLineWidth(),
                getRightMargin() + getLineWidth());
    }

    public int getLeftMargin() {
        return left;
    }

    public int getTopMargin() {
        return top;
    }

    public int getRightMargin() {
        return right;
    }

    public int getBottomMargin() {
        return bottom;
    }

    public void setLeftMargin(IFigure figure, int value) {
        if (left == value)
            return;

        this.left = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setTopMargin(IFigure figure, int value) {
        if (top == value)
            return;

        this.top = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setRightMargin(IFigure figure, int value) {
        if (right == value)
            return;
        this.right = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

    public void setBottomMargin(IFigure figure, int value) {
        if (bottom == value)
            return;
        this.bottom = value;
        invalidate();
        if (figure != null) {
            figure.revalidate();
            repaint(figure);
        }
    }

}
