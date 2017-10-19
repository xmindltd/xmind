/**
 * 
 */
package org.xmind.ui.datepicker;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.graphics.AlphaGraphics;

public class BaseFigure extends Figure {

    public static Object PRESELECTED = "PRESELECTED"; //$NON-NLS-1$

    public static Object SELECTED = "SELECTED"; //$NON-NLS-1$

    public static Object PRESSED = "PRESSED"; //$NON-NLS-1$

    public static Object CONTENT = "CONTENT"; //$NON-NLS-1$

    private double alpha = 255;

    private LayeredPane pane = new LayeredPane();

    public BaseFigure() {
        setLayoutManager(new StackLayout());
        setOpaque(false);
        add(pane);
        addLayers();
        setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
    }

    protected void addLayers() {
        addFeedbackLayers();
        addContentLayer();
    }

    protected void addFeedbackLayers() {
        addSelectFeedbackLayer();
        addPressFeedbackLayer();
        addPreselectFeedbackLayer();
    }

    protected void addPreselectFeedbackLayer() {
        pane.add(new PreselectFeedbackLayer(), PRESELECTED, -1);
    }

    protected void addPressFeedbackLayer() {
        pane.add(new PressFeedbackLayer(), PRESSED, -1);
    }

    protected void addSelectFeedbackLayer() {
        pane.add(new SelectFeedbackLayer(), SELECTED, -1);
    }

    protected void addContentLayer() {
        pane.add(new TextLayer(), CONTENT, -1);
    }

    public void setContentLayer(IFigure content) {
        removeOldContent();
        pane.add(content, CONTENT, -1);
    }

    private void removeOldContent() {
        Layer content = pane.getLayer(CONTENT);
        if (content != null)
            pane.remove(content);
    }

    public void setContentLayerAfter(Layer content, Object after) {
        removeOldContent();
        pane.addLayerAfter(content, CONTENT, after);
    }

    public void setContentLayerBefore(Layer content, Object before) {
        removeOldContent();
        pane.addLayerBefore(content, CONTENT, before);
    }

    public IFigure getContent() {
        return pane.getLayer(CONTENT);
    }

    public String getText() {
        IFigure content = getContent();
        if (content instanceof TextLayer)
            return ((TextLayer) content).getText();
        return null;
    }

    public void setText(String text) {
        IFigure content = getContent();
        if (content instanceof TextLayer)
            ((TextLayer) content).setText(text);
    }

    public void setTextCandidates(String[] candidates) {
        IFigure content = getContent();
        if (content instanceof TextLayer)
            ((TextLayer) content).setCandidates(candidates);
    }

    public void setTextAlpha(int alpha) {
        IFigure content = getContent();
        if (content instanceof TextLayer)
            ((TextLayer) content).setAlpha(alpha);
    }

    @Override
    public void paint(Graphics graphics) {
        AlphaGraphics g = new AlphaGraphics(graphics);
        g.setMainAlpha((int) alpha);
        g.setAntialias(SWT.ON);
        super.paint(g);
        g.dispose();
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        repaint();
    }

    public double getAlpha() {
        return alpha;
    }

    public boolean isPreselected() {
        Layer layer = pane.getLayer(PRESELECTED);
        return layer != null && layer.isVisible();
    }

    public void setPreselected(boolean value) {
        Layer layer = pane.getLayer(PRESELECTED);

        if (layer != null) {
            layer.setVisible(value);
        }
    }

    public boolean isSelected() {
        Layer layer = pane.getLayer(SELECTED);
        return layer != null && layer.isVisible();
    }

    public void setSelected(boolean value) {
        Layer layer = pane.getLayer(SELECTED);
        if (layer != null) {
            layer.setVisible(value);
            this.setForegroundColor(
                    value ? ColorConstants.white : ColorConstants.black);
        }
    }

    public boolean isPressed() {
        Layer layer = pane.getLayer(PRESSED);
        return layer != null && layer.isVisible();
    }

    public void setPressed(boolean value) {
        Layer layer = pane.getLayer(PRESSED);
        if (layer != null) {
            layer.setVisible(value);
        }
        IFigure content = getContent();
        if (content instanceof TextLayer)
            ((TextLayer) content).setOffset(value ? 1 : 0, value ? 1 : 0);
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        Dimension psize = super.getPreferredSize(wHint, hHint);
        if (getParent() != null) {
            LayoutManager layout = getParent().getLayoutManager();
            if (layout != null) {
                Object constraint = layout.getConstraint(this);
                if (constraint instanceof GridData) {
                    int h = ((GridData) constraint).horizontalSpan;
                    if (h > 1) {
                        psize = psize.getCopy();
                        psize.width = psize.width / h;
                    }
                }
            }
        }
        return psize;
    }

}
