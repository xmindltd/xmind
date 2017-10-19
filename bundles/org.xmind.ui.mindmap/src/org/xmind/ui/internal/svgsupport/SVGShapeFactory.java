package org.xmind.ui.internal.svgsupport;

public class SVGShapeFactory {

    static SVGShape createSVGShape(String type) {
        SVGShape shape = null;

        if (SVGDefinitionConstants.TAG_RECT.equals(type)) {
            shape = new SVGRectangleShape();
        } else if (SVGDefinitionConstants.TAG_CIRCLE.equals(type)) {
            shape = new SVGCircleShape();
        } else if (SVGDefinitionConstants.TAG_ELLIPSE.equals(type)) {
            shape = new SVGEllipseShape();
        } else if (SVGDefinitionConstants.TAG_LINE.equals(type)) {
            shape = new SVGLineShape();
        } else if (SVGDefinitionConstants.TAG_POLYLINE.equals(type)) {
            shape = new SVGPolyLineShape();
        } else if (SVGDefinitionConstants.TAG_POLYGON.equals(type)) {
            shape = new SVGPolygonShape();
        } else if (SVGDefinitionConstants.TAG_GROUP.equals(type)) {
            shape = new SVGGroup();
        } else if (SVGDefinitionConstants.TAG_PATH.equals(type)) {
            shape = new SVGPath();
        } else if (SVGDefinitionConstants.TAG_TEXT.equals(type)) {
            shape = new SVGTextElement();
        } else if (SVGDefinitionConstants.TAG_USE.equals(type)) {
            shape = new SVGUseElement();
        }

        return shape;
    }
}
