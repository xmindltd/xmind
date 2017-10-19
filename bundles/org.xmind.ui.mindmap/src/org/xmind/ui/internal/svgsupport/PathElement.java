package org.xmind.ui.internal.svgsupport;

import org.xmind.gef.draw2d.geometry.PrecisionPoint;
import org.xmind.gef.draw2d.graphics.Path;

/**
 * 
 * @author Enki Xiong
 *
 */
abstract public class PathElement {

    private static class MoveElement extends PathElement {
        private PrecisionPoint p;

        public MoveElement(double x, double y) {
            p = new PrecisionPoint(x, y);
        }

        @Override
        public void addToPath(Path path) {
            path.moveTo(p);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MoveElement) {
                MoveElement element = (MoveElement) obj;
                return p.equals(element.p);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + (int) Double.doubleToLongBits(p.x);
            result = result * 31 + (int) Double.doubleToLongBits(p.y);
            return result;
        }

        @Override
        public String toString() {
            return "Move to (" + p.x + "," + p.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private static class ClosePathElement extends PathElement {

        @Override
        public void addToPath(Path path) {
            path.close();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClosePathElement)
                return true;
            return false;
        }

        @Override
        public String toString() {
            return "Close Path"; //$NON-NLS-1$
        }
    }

    private static class CurveElement extends PathElement {
        private PrecisionPoint control1;
        private PrecisionPoint control2;
        private PrecisionPoint dest;

        public CurveElement(double x1, double y1, double x2, double y2,
                double endX, double endY) {
            control1 = new PrecisionPoint(x1, y1);
            control2 = new PrecisionPoint(x2, y2);
            dest = new PrecisionPoint(endX, endY);
        }

        @Override
        public void addToPath(Path path) {
            path.cubicTo(control1, control2, dest);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MoveElement) {
                CurveElement element = (CurveElement) obj;
                return control1.equals(element.control1)
                        && control2.equals(element.control2)
                        && dest.equals(element.dest);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + (int) Double.doubleToLongBits(control1.x);
            result = result * 31 + (int) Double.doubleToLongBits(control1.y);
            result = result * 31 + (int) Double.doubleToLongBits(control2.x);
            result = result * 31 + (int) Double.doubleToLongBits(control2.y);
            result = result * 31 + (int) Double.doubleToLongBits(dest.x);
            result = result * 31 + (int) Double.doubleToLongBits(dest.y);
            return result;
        }

        @Override
        public String toString() {
            String str = "Curve to ("; //$NON-NLS-1$
            str += "control1:(" + control1.x + "," + control1.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            str += "control2:(" + control2.x + "," + control2.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            str += "dest(" + dest.x + "," + dest.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            str += ")"; //$NON-NLS-1$
            return str;
        }
    }

    private static class LineElement extends PathElement {
        private PrecisionPoint p;

        public LineElement(double x, double y) {
            p = new PrecisionPoint(x, y);
        }

        @Override
        public void addToPath(Path path) {
            path.lineTo(p);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LineElement) {
                LineElement element = (LineElement) obj;
                return p.equals(element.p);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + (int) Double.doubleToLongBits(p.x);
            result = result * 31 + (int) Double.doubleToLongBits(p.y);
            return result;
        }

        @Override
        public String toString() {
            return "Line to(" + p.x + " " + p.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private static class QuadraticBelzierCurveElement extends PathElement {
        private PrecisionPoint control;
        private PrecisionPoint dest;

        public QuadraticBelzierCurveElement(double x, double y, double endX,
                double endY) {
            control = new PrecisionPoint(x, y);
            dest = new PrecisionPoint(endX, endY);
        }

        @Override
        public void addToPath(Path path) {
            path.quadTo(control, dest);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MoveElement) {
                CurveElement element = (CurveElement) obj;
                return control.equals(element.control1)
                        && dest.equals(element.dest);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = result * 31 + (int) Double.doubleToLongBits(control.x);
            result = result * 31 + (int) Double.doubleToLongBits(control.y);
            result = result * 31 + (int) Double.doubleToLongBits(dest.x);
            result = result * 31 + (int) Double.doubleToLongBits(dest.y);
            return result;
        }

        @Override
        public String toString() {
            String str = "Quadratic Belzier Curve("; //$NON-NLS-1$
            str += "control1:(" + control.x + "," + control.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            str += "control2:(" + dest.x + " " + dest.y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            str += ")"; //$NON-NLS-1$
            return str;
        }
    }

    public abstract void addToPath(Path path);

    public static PathElement getMoveElement(double x, double y) {
        return new MoveElement(x, y);
    }

    public static PathElement getClosePathElement() {
        return new ClosePathElement();
    }

    public static PathElement getCurveElement(double x1, double y1, double x2,
            double y2, double endX, double endY) {
        return new CurveElement(x1, y1, x2, y2, endX, endY);
    }

    public static PathElement getQuadraticBelzierCurveElement(double x,
            double y, double endX, double endY) {
        return new QuadraticBelzierCurveElement(x, y, endX, endY);
    }

    public static PathElement getLineElement(double x, double y) {
        return new LineElement(x, y);
    }

}
