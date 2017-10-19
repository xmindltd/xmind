package org.xmind.ui.datepicker;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;

public class TextLayer extends Layer {

    private int alpha = 0xff;

    private String text;

    private int dx = 0;

    private int dy = 0;

    private Dimension computedSize;

    private String[] candidates;

    private Dimension candidatesSize;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public void setOffset(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
        repaint();
    }

    public void setCandidates(String[] candidates) {
        this.candidates = candidates;
        candidatesSize = null;
        revalidate();
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        candidatesSize = null;
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        if (computedSize == null) {
            computeSize(wHint, hHint);
        }
        return computedSize;
    }

    private void computeSize(int wHint, int hHint) {
        computedSize = GraphicsUtils.getAdvanced().getTextSize(
                text == null || "".equals(text) ? "A" : text, getFont()) //$NON-NLS-1$  //$NON-NLS-2$ 
                .expand(10, 8);
        computeCandidatesSize();
        computedSize.union(candidatesSize);
    }

    private void computeCandidatesSize() {
        if (candidatesSize != null)
            return;
        candidatesSize = new Dimension();
        if (candidates == null || candidates.length == 0)
            return;
        for (String str : candidates) {
            candidatesSize.union(GraphicsUtils.getAdvanced().getTextSize(str,
                    getFont()));
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        computedSize = null;
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);

        if (text == null || "".equals(text)) //$NON-NLS-1$
            return;

        graphics.setAlpha(alpha);
        graphics.setForegroundColor(getForegroundColor());
        graphics.setBackgroundColor(getBackgroundColor());
        graphics.setFont(getFont());
        Dimension textSize = GraphicsUtils.getAdvanced().getTextSize(text,
                getFont());
        Rectangle b = getBounds();
        graphics.drawText(text, b.x + (b.width - textSize.width) / 2 + dx, b.y
                + (b.height - textSize.height) / 2 + dy);
    }

}
