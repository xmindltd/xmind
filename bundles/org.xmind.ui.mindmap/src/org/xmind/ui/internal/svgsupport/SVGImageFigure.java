package org.xmind.ui.internal.svgsupport;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ResourceManager;
import org.xmind.gef.draw2d.ReferencedFigure;

/**
 * @author Enki Xiong
 */
public class SVGImageFigure extends ReferencedFigure {

    private static final Rectangle IMAGE_CLIENT_AREA = new Rectangle();

    private static final int FLAG_STRETCHED = MAX_FLAG << 1;

    private static final int FLAG_CONSTRAINED = MAX_FLAG << 2;

    static {
        MAX_FLAG = FLAG_CONSTRAINED;
    }

    private SVGImageData svgData = null;

    private Dimension imgSize = null;

    private int alpha = -1;

    private ResourceManager manager;

    public SVGImageFigure() {
        this(null);
    }

    public SVGImageFigure(SVGImageData svgData) {
        setSVGData(svgData);
    }

    public void setSVGData(SVGImageData svgData) {
        if (this.svgData == svgData)
            return;
        this.svgData = svgData;
        setMinimumSize(new Dimension(1, 1));
        revalidate();
        repaint();
    }

    public Dimension getImageSize() {
        if (imgSize == null) {
            if (svgData != null) {
                imgSize = new Dimension(svgData.getSize());
            } else
                imgSize = new Dimension();
        }
        return imgSize;
    }

    public boolean isConstrained() {
        return getFlag(FLAG_CONSTRAINED);
    }

    public void setConstrained(boolean constrained) {
        if (constrained == isConstrained())
            return;
        setFlag(FLAG_CONSTRAINED, constrained);
        repaint();
    }

    public boolean isStretched() {
        return getFlag(FLAG_STRETCHED);
    }

    public void setStretched(boolean stretched) {
        if (stretched == isStretched())
            return;
        setFlag(FLAG_STRETCHED, stretched);
        repaint();
    }

    public SVGImageData getSVGData() {
        return svgData;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        if (this.alpha == alpha)
            return;
        this.alpha = alpha;
        repaint();
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        if (prefSize != null)
            return prefSize;
        if (getLayoutManager() != null) {
            Dimension d = getLayoutManager().getPreferredSize(this, wHint,
                    hHint);
            if (d != null)
                return d;
        }
        if (svgData != null) {
            return svgData.getSize();
        }
        return new Dimension(0, 0);
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(Graphics)
     */
    protected void paintFigure(final Graphics graphics) {
        super.paintFigure(graphics);

        SVGImageData svgData = getSVGData();
        if (svgData != null)
            svgData.paintFigure(graphics, getClientArea(IMAGE_CLIENT_AREA),
                    manager);
    }

    public ResourceManager getManager() {
        return manager;
    }

    public void setManager(ResourceManager manager) {
        this.manager = manager;
    }

}
