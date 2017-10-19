package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.decoration.IDecoration;

public interface ILineDecoration2 extends IDecoration {

    /**
     * @return the color
     */
    Color getLineColor(int index);

    /**
     * 
     * @return
     */
    int getLineStyle(int index);

    /**
     * @return the width
     */
    int getLineWidth(int index);

    /**
     * 
     * @param figure
     * @param color
     */
    void setLineColor(IFigure figure, int index, Color color);

    /**
     * 
     * @param figure
     * @param width
     */
    void setLineWidth(IFigure figure, int index, int width);

    /**
     * 
     * @param figure
     * @param style
     */
    void setLineStyle(IFigure figure, int index, int style);

}
