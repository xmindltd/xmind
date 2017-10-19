package org.xmind.ui.internal.svgsupport;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.Graphics;

/**
 * 
 * @author Enki Xiong
 *
 */
public class SVGTransform {

    private static class Translate implements TransformElement {

        private float dx, dy;

        public Translate(float dx, float dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public void transform(Graphics graphics) {
            graphics.translate(dx, dy);
        }

        @Override
        public String toString() {
            return "translate:" + dx + "," + dy; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static class Scale implements TransformElement {
        private float hScale = 1.0f;
        private float vScale = 1.0f;

        public Scale(float hScale) {
            this(hScale, hScale);
        }

        public Scale(float hScale, float vScale) {
            this.hScale = hScale;
            this.vScale = vScale;
        }

        public void transform(Graphics graphics) {
            graphics.scale(hScale, vScale);
        }

        @Override
        public String toString() {
            return "scale:" + hScale + "," + vScale; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private static class Rotate implements TransformElement {
        private float angle;
        private float dx, dy;

        public Rotate(float angle) {
            this.angle = angle;
        }

        Rotate(float angle, float dx, float dy) {
            this(angle);
            this.dx = dx;
            this.dy = dy;
        }

        public void transform(Graphics graphics) {
            graphics.translate(dx, dy);
            graphics.rotate(angle);
            graphics.translate(-dx, -dy);
        }

        @Override
        public String toString() {
            return "rotate:" + angle; //$NON-NLS-1$
        }

    }

    public static final Pattern pattern = Pattern
            .compile("(translate)|(rotate)|(scale)|([+-]?\\d+(\\.\\d+)?)"); //$NON-NLS-1$

    private LinkedList<TransformElement> list = new LinkedList<TransformElement>();
    private float[] matrix = new float[] { 1, 0, 0, 1, 0, 0 };
    private boolean isMatrix = false;

    public void parseTransform(String transform) {

        if (transform.startsWith("matrix")) { //$NON-NLS-1$
            isMatrix = true;
            parseTransformMatrix(transform);
        } else {
            parseTransformList(transform);
        }

    }

    private void parseTransformList(String transform) {
        final Matcher match = pattern.matcher(transform);
        LinkedList<String> tokens = new LinkedList<String>();
        while (match.find()) {
            tokens.add(match.group());
        }
        while (!tokens.isEmpty()) {
            String type = tokens.removeFirst();

            if (type.equals(SVGDefinitionConstants.TRANSLATE)) {
                list.add(new Translate(Float.valueOf(tokens.removeFirst()),
                        Float.valueOf(tokens.removeFirst())));
                continue;
            }

            if (type.equals(SVGDefinitionConstants.ROTATE)) {
                String angle = tokens.removeFirst();
                if (tokens.isEmpty()) {
                    list.addLast(new Rotate(Float.valueOf(angle)));
                    break;
                }
                String tmp = tokens.getFirst();
                if (tmp.equals(SVGDefinitionConstants.TRANSLATE)
                        || tmp.equals(SVGDefinitionConstants.ROTATE)
                        || tmp.equals(SVGDefinitionConstants.SCALE)) {
                    list.addLast(new Rotate(Float.valueOf(angle)));
                } else {
                    list.addLast(new Rotate(Float.valueOf(angle),
                            Float.valueOf(tokens.removeFirst()),
                            Float.valueOf(tokens.removeFirst())));
                }
                continue;

            }

            if (type.equals(SVGDefinitionConstants.SCALE)) {
                String hScale = tokens.removeFirst();
                if (tokens.isEmpty()) {
                    list.addLast(new Scale(Float.valueOf(hScale)));
                    break;
                }
                String tmp = tokens.getFirst();
                if (tmp.equals(SVGDefinitionConstants.TRANSLATE)
                        || tmp.equals(SVGDefinitionConstants.ROTATE)
                        || tmp.equals(SVGDefinitionConstants.SCALE)) {
                    list.addLast(new Scale(Float.valueOf(hScale)));
                } else {
                    list.addLast(new Scale(Float.valueOf(hScale),
                            Float.valueOf(tokens.removeFirst())));
                }
                continue;
            }

        }
    }

    private void parseTransformMatrix(String transform) {
        String[] strs = transform
                .split(SVGDefinitionConstants.LEFT_BRACKET_REGEX)[1]
                        .split(SVGDefinitionConstants.RIGHT_BRACKET_REGEX)[0]
                                .split(" +"); //$NON-NLS-1$
        matrix[0] = Float.valueOf(strs[0]);
        matrix[1] = Float.valueOf(strs[1]);
        matrix[2] = Float.valueOf(strs[2]);
        matrix[3] = Float.valueOf(strs[3]);
        matrix[4] = Float.valueOf(strs[4]);
        matrix[5] = Float.valueOf(strs[5]);
    }

    public LinkedList<TransformElement> getList() {
        return list;
    }

    public boolean isMatrix() {
        return isMatrix;
    }

}
