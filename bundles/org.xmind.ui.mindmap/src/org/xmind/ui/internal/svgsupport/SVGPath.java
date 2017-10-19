package org.xmind.ui.internal.svgsupport;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final public class SVGPath extends SVGShape {

    private List<PathElement> elements;

    SVGPath() {
        super();
        elements = Collections.emptyList();
    }

    public SVGPath(List<PathElement> elements) {
        this.elements = elements;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parsePath(implemention, parent);
    }

    private void parsePath(Element implemention, SVGShape parent) {
        if (implemention.hasAttribute(SVGDefinitionConstants.DEFINITION)) {
            String pathDefinitionString = implemention
                    .getAttribute(SVGDefinitionConstants.DEFINITION);
            PathParser parser = PathParser.getInstance();
            elements = parser.parseSVGPath(pathDefinitionString);
        }
    }

    @Override
    Path generatePath(Display device) {
        Path path = new Path(device);
        for (PathElement ele : elements) {
            ele.addToPath(path);
        }
        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return INVALID_RECT;
    }

    @Override
    protected SVGShape clone() {
        SVGPath path = new SVGPath();
        path.elements = this.elements;
        path.setInfo(getInfo().clone());
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGPath) {
            SVGPath path = (SVGPath) obj;
            if (elements.size() == path.elements.size()) {
                for (int i = 0; i < elements.size(); i++) {
                    if (!elements.get(i).equals(path.elements.get(i)))
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + getInfo().hashCode();
        for (PathElement element : elements)
            result = result * 31 + element.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SVGPath( elements:" + elements.size() + " " //$NON-NLS-1$//$NON-NLS-2$
                + getInfo().toString() + " )"; //$NON-NLS-1$
    }

}
