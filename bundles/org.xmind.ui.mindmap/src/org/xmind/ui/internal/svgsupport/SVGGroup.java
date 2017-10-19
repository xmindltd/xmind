package org.xmind.ui.internal.svgsupport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final public class SVGGroup extends SVGShape {

    private List<SVGShape> children;

    SVGGroup() {
        super();
        children = new ArrayList<SVGShape>();
    }

    public SVGGroup(List<SVGShape> children) {
        this.children = children;
    }

    @Override
    void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseGroup(implemention, parent);
    }

    private void parseGroup(Element implemention, SVGShape parent) {
        NodeList list = implemention.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                SVGShape shape = parseShape(((Element) node), this);
                if (shape != null)
                    this.children.add(shape);
            }
        }
    }

    @Override
    void paintImage(Graphics graphics, Display device) {

        boolean statePushed = super.prePaint(graphics);
        for (SVGShape shape : children) {
            shape.paintImage(graphics, device);
        }
        super.postPaint(graphics, statePushed);
    }

    @Override
    Path generatePath(Display device) {
        return null;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return null;
    }

    @Override
    public void setResourceManager(ResourceManager resourceManager) {
        super.setResourceManager(resourceManager);
        for (SVGShape shape : children)
            shape.setResourceManager(resourceManager);
    }

    @Override
    protected SVGShape clone() {
        SVGGroup group = new SVGGroup();
        group.setInfo(getInfo().clone());

        group.children = new ArrayList<SVGShape>();
        for (SVGShape shape : children) {
            group.children.add(shape.clone());
        }

        return group;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof SVGGroup) {
            SVGGroup group = (SVGGroup) obj;
            if (children.size() == group.children.size()) {
                for (int i = 0; i < children.size(); i++) {
                    if (!children.get(i).equals(group.children.get(i)))
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
        for (SVGShape shape : children)
            result = result * 31 + shape.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String str = "SVGGroup( children Size: "; //$NON-NLS-1$
        str += children.size();
        str += getInfo().toString();
        str += " )"; //$NON-NLS-1$
        return str;
    }
}
