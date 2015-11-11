package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecorationEx;

public interface IInfoDecoration extends IShapeDecoration, IShapeDecorationEx {

    public int getLeftMargin();

    /**
     * @param margin
     *            the leftMargin to set
     */
    public void setLeftMargin(IFigure figure, int value);

    /**
     * @return the topMargin
     */
    public int getTopMargin();

    /**
     * @param value
     *            the topMargin to set
     */
    public void setTopMargin(IFigure figure, int value);

    public int getRightMargin();

    public void setRightMargin(IFigure figure, int value);

    public int getBottomMargin();

    public void setBottomMargin(IFigure figure, int value);

}
