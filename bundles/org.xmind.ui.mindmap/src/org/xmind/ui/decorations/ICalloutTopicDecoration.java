package org.xmind.ui.decorations;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.xmind.gef.draw2d.decoration.IDecoration;

public interface ICalloutTopicDecoration extends IDecoration {

    public void setFromLineColor(IFigure figure, Color fromLineColor);

    public void setFromLineWidth(IFigure figure, int fromLineWidth);

    public void setFromLineClass(IFigure figure, String fromLineClass);

    public void setFromLineStyle(IFigure figure, int fromLinePattern);

    public void setFromLineCorner(IFigure figure, int fromLineCorner);

    public void setFromFillColor(IFigure figure, Color fromFillColor);

    public Color getFromFillColor();

    public Color getFromLineColor();

    public int getFromLineWidth();

    public String getFromLineClass();

    public int getFromLineStyle();

    public int getFromLineCorner();

}
