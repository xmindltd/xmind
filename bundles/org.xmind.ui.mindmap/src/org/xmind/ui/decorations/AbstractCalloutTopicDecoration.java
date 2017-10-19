package org.xmind.ui.decorations;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public abstract class AbstractCalloutTopicDecoration extends
        AbstractTopicDecoration implements ICalloutTopicDecoration {

    private int fromLineStyle = SWT.LINE_SOLID;

    private String fromLineClass = null;

    private int fromLineWidth = 1;

    private Color fromLineColor = null;

    private Color fromFillColor = null;

    private int fromLineCorner = 0;

    protected AbstractCalloutTopicDecoration() {
        super();
    }

    protected AbstractCalloutTopicDecoration(String id) {
        super(id);
    }

    @Override
    protected void paintFill(IFigure figure, Graphics graphics) {
        //prevent paint fill
    }

    @Override
    protected void paintOutline(IFigure figure, Graphics graphics) {
        //prevent paint border
    }

    public void setFromLineColor(IFigure figure, Color fromLineColor) {
        this.fromLineColor = fromLineColor;
    }

    public void setFromLineWidth(IFigure figure, int fromLineWidth) {
        this.fromLineWidth = fromLineWidth;
    }

    public void setFromLineClass(IFigure figure, String fromLineClass) {
        this.fromLineClass = fromLineClass;
    }

    public void setFromLineStyle(IFigure figure, int fromLinePattern) {
        this.fromLineStyle = fromLinePattern;
    }

    public void setFromLineCorner(IFigure figure, int fromLineCorner) {
        this.fromLineCorner = fromLineCorner;
    }

    public void setFromFillColor(IFigure figure, Color fromFillColor) {
        this.fromFillColor = fromFillColor;
    }

    public Color getFromFillColor() {
        return fromFillColor;
    }

    public Color getFromLineColor() {
        return fromLineColor;
    }

    public int getFromLineWidth() {
        return fromLineWidth;
    }

    public String getFromLineClass() {
        return fromLineClass;
    }

    public int getFromLineStyle() {
        return fromLineStyle;
    }

    public int getFromLineCorner() {
        return fromLineCorner;
    }

}
