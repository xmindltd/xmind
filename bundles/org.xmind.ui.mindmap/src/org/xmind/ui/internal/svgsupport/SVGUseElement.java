package org.xmind.ui.internal.svgsupport;

import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final class SVGUseElement extends SVGShape {
    private float x;
    private float y;
    private SVGShape link;

    SVGUseElement() {
        super();
    }

    public SVGUseElement(SVGShape link) {
        this.link = link;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseUse(implemention, parent);
    }

    private void parseUse(Element implemention, SVGShape parent) {
        if (implemention.hasAttribute(SVGDefinitionConstants.XLINK_HREF)) {
            String hrefId = implemention
                    .getAttribute(SVGDefinitionConstants.XLINK_HREF);
            String[] strs = hrefId.split(SVGDefinitionConstants.REF_BEGIN);
            if (strs.length >= 2) {
                link = ((SVGShape) idRefs.get(strs[1])).clone();
            }
            if (link != null)
                link.getInfo().add(getInfo());
        }
        x = getFloatAttribute(implemention, SVGDefinitionConstants.X);
        y = getFloatAttribute(implemention, SVGDefinitionConstants.Y);
    }

    @Override
    void paintImage(Graphics graphics, Display device) {
        if (link != null) {
            boolean statePushed = super.prePaint(graphics);
            graphics.translate(x, y);
            link.paintImage(graphics, device);
            super.postPaint(graphics, statePushed);
        }
    }

    @Override
    public void setResourceManager(ResourceManager resourceManager) {
        super.setResourceManager(resourceManager);
        this.link.setResourceManager(resourceManager);
    }

    @Override
    Path generatePath(Display device) {
        // not implement
        return null;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        // not implement
        return null;
    }

    @Override
    protected SVGShape clone() {
        // not implement 
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGUseElement) {
            SVGUseElement use = (SVGUseElement) obj;
            if (getInfo().equals(use.getInfo()) && x == use.x && y == use.y
                    && link.equals(use.link))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        result = result * 31 + Float.floatToIntBits(x);
        result = result * 31 + Float.floatToIntBits(y);
        result = result * 31 + link.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SVGUse(" + getInfo().toString() + link.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
