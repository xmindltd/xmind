package org.xmind.ui.internal.svgsupport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
final public class SVGTextElement extends SVGShape {

    private FontResourceDescriptor fontDescriptor;
    private List<TextSpan> spans;

    private static class TextSpan {
        private float x;
        private float y;
        private String text;

        public TextSpan(float x, float y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }
    }

    SVGTextElement() {
        super();
        this.spans = new ArrayList<SVGTextElement.TextSpan>();
    }

    public SVGTextElement(FontResourceDescriptor fontDescriptor) {
        this.fontDescriptor = fontDescriptor;
    }

    @Override
    public void parse(Element implemention, SVGShape parent) {
        super.parse(implemention, parent);
        parseText(implemention, parent);
    }

    @Override
    public Path generatePath(Display device) {

        //FIXME  correct x,y
        Font font = (Font) getResourceManager().create(fontDescriptor);
        Path path = new Path(device);
        for (TextSpan span : spans) {
            path.addString(span.text, new PrecisionPoint(span.x, span.y), font);
        }

        return path;
    }

    @Override
    PrecisionRectangle generateRectangle() {
        return INVALID_RECT;
    }

    private void parseText(Element implemention, SVGShape parent) {
        String fontFamily = implemention
                .getAttribute(SVGDefinitionConstants.FONT_FAMILY).trim();
        int fontSize = (int) getFloatAttribute(implemention,
                SVGDefinitionConstants.FONT_SIZE);
        String fontStyle = implemention
                .getAttribute(SVGDefinitionConstants.FONT_WEIGHT).trim();
        int style = 0;
        if (fontStyle.contains(SVGDefinitionConstants.BOLD)) {
            style |= SWT.BOLD;
        }
        if (fontStyle.contains(SVGDefinitionConstants.ITALIC)) {
            style |= SWT.ITALIC;
        }
        fontDescriptor = new FontResourceDescriptor(fontFamily, fontSize,
                style);
        NodeList list = implemention.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element ele = (Element) node;
                String text = ele.getTextContent();
                float x = getFloatAttribute(implemention,
                        SVGDefinitionConstants.X);
                float y = getFloatAttribute(implemention,
                        SVGDefinitionConstants.Y);

                this.spans.add(new TextSpan(x, y, text));
            }
        }
    }

    @Override
    protected SVGShape clone() {
        SVGTextElement text = new SVGTextElement();
        text.fontDescriptor = this.fontDescriptor;
        text.spans = spans;
        text.setInfo(getInfo().clone());
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof SVGTextElement) {
            SVGTextElement text = (SVGTextElement) obj;
            if (getInfo() != null && getInfo().equals(text.getInfo())
                    && fontDescriptor != null
                    && fontDescriptor.equals(text.fontDescriptor))
                if (spans.size() == text.spans.size()) {
                    for (int i = 0; i < spans.size(); i++) {
                        TextSpan span1 = spans.get(i);
                        TextSpan span2 = text.spans.get(i);
                        if (!(span1.x == span2.x && span1.y == span2.y
                                && span1.text.equals(span2.text)))
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
        result = result * 31 + fontDescriptor.hashCode();
        for (TextSpan span : spans) {
            result = result * 31 + Float.floatToIntBits(span.x);
            result = result * 31 + Float.floatToIntBits(span.y);
            result = result * 31 + span.text.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "SVGText(" + fontDescriptor.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void addText(float x, float y, String text) {
        if (spans == null) {
            spans = new ArrayList<TextSpan>();
        }
        spans.add(new TextSpan(x, y, text));
    }

}
