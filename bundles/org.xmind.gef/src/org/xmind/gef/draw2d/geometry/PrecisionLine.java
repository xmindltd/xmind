/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.draw2d.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frank Shaka
 */
public class PrecisionLine extends PrecisionPointPairBase implements Cloneable,
        java.io.Serializable {

    public static final double DEFAULT_TOLERANCE = 0.0001;

    public enum LineType {
        LineSegment, Ray, Line;
    }

    public enum Side {
        Left, Right, OnLine;
    }

    private static final long serialVersionUID = 8018247328242867430L;

    public static boolean contains(PrecisionPoint origin,
            PrecisionPoint terminus, PrecisionPoint p, double tolerance) {
        return origin.getDistance(p) + p.getDistance(terminus) <= origin
                .getDistance(terminus)
                + tolerance;
    }

    /**
     * Returns the coefficients of the generalized equation of the line passing
     * through points (x1,y1) and (x2,y2) Generalized line equation: ax+by=c =>
     * a==result[0], b==result[1], c==result[2]
     * 
     * @param x1 -
     *            x coordinate of the 1st point
     * @param y1 -
     *            y coordinate of the 1st point
     * @param x2 -
     *            x coordinate of the 2nd point
     * @param y2 -
     *            y coordinate of the 2nd point
     * @return the coefficients of the generalized equation of the line passing
     *         through points (x1,y1) and (x2,y2)
     */
    public static double[] getLineEquation(double x1, double y1, double x2,
            double y2) {
        double equation[] = new double[3];
        for (int i = 0; i < 3; i++)
            equation[i] = 0;

        if (x1 == x2 && y1 == y2)
            return equation;

        if (x1 == x2) {
            equation[0] = 1;
            equation[1] = 0;
            equation[2] = x1;
            return equation;
        }

        equation[0] = (y1 - y2) / (x2 - x1);
        equation[1] = 1.0;
        equation[2] = y2 + equation[0] * x2;
        return equation;
    }

    private LineType lineType;

    public PrecisionLine(PrecisionPoint origin, PrecisionPoint terminus) {
        this(origin, terminus, LineType.LineSegment);
    }

    public PrecisionLine(PrecisionPoint origin, PrecisionPoint terminus,
            LineType lineType) {
        super(origin, terminus);
        this.lineType = lineType;
    }

    public PrecisionLine(double x1, double y1, double x2, double y2) {
        this(x1, y1, x2, y2, LineType.LineSegment);
    }

    public PrecisionLine(double x1, double y1, double x2, double y2,
            LineType lineType) {
        super(x1, y1, x2, y2);
        this.lineType = lineType;
    }

    public boolean contains(PrecisionPoint p, double tolerance) {
        boolean b = contains(point1, point2, p, tolerance);
        if (lineType == LineType.Line || lineType == LineType.Ray) {
            b |= contains(point1, p, point2, tolerance);
            if (lineType == LineType.Line)
                b |= contains(point2, p, point1, tolerance);
        }
        return b;
    }

    public boolean contains(PrecisionPoint p) {
        return contains(p, DEFAULT_TOLERANCE);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PrecisionLine))
            return false;
        PrecisionLine ls = (PrecisionLine) obj;
        return point1.equals(ls.point1) && point2.equals(ls.point2)
                && lineType == ls.lineType;
    }

    public PrecisionLine getCopy() {
        return new PrecisionLine(point1, point2, lineType);
    }

    public double[] getEquation() {
        return getLineEquation(point1.x, point1.y, point2.x, point2.y);
    }

    public List<PrecisionPoint> getLinesIntersections(PrecisionLine line) {
        List<PrecisionPoint> intersections = new ArrayList<PrecisionPoint>();
        double temp[] = getEquation();
        double a1 = temp[0];
        double b1 = temp[1];
        double c1 = temp[2];

        temp = line.getEquation();
        double a2 = temp[0];
        double b2 = temp[1];
        double c2 = temp[2];

        // Cramer's rule for the system of linear equations
        double det = a1 * b2 - b1 * a2;
        if (det == 0) {
            if (a1 == a2 && b1 == b2 && c1 == c2) {
                // if lines are the same, then instead of infinite number of intersections
                // we will put the end points of the line segment passed as an argument
                intersections.add(line.getOrigin().getCopy());
                intersections.add(line.getTerminus().getCopy());
            }
        } else {
            intersections.add(new PrecisionPoint((c1 * b2 - b1 * c2) / det, (a1
                    * c2 - c1 * a2)
                    / det));
        }
        return intersections;
    }

    public LineType getLineType() {
        return lineType;
    }

    public PrecisionPoint getOrigin() {
        return point1;
    }

    public PrecisionPoint getTerminus() {
        return point2;
    }

    public PrecisionPoint intersect(PrecisionLine line) {
        return intersect(line, DEFAULT_TOLERANCE);
    }

    /**
     * Determines the intersect point between this line and the line passed in
     * as a parameter. If they intersect, then true is returned and the point
     * reference passed in will be set to the intersect point. If they don't
     * intersect, then the method returns <code>false</code>.
     * 
     * @param line
     *            <code>LineSeg</code> to test the intersection against.
     * @param nTolerance
     *            int tolerance value for detecting the intersection.
     * @return <code>Point</code> that represents the intersection with this
     *         line, or <code>null</code> if the calculation is not possible.
     */
    public PrecisionPoint intersect(final PrecisionLine line,
            final double nTolerance) {
        List<PrecisionPoint> intersections = getLinesIntersections(line);
        if (intersections.size() > 1) {
            intersections.add(point1.getCopy());
            intersections.add(point2.getCopy());
        }
        for (int i = 0; i < intersections.size(); i++) {
            PrecisionPoint result = intersections.get(i).getCopy();
            if (contains(result, nTolerance)
                    && line.contains(result, nTolerance)) {
                return result;
            }
        }
        return null;
    }

    public double length() {
        return point1.getDistance(point2);
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public void setOrigin(PrecisionPoint origin) {
        point1.setLocation(origin);
    }

    public void setTerminus(PrecisionPoint terminus) {
        point2.setLocation(terminus);
    }

    public PrecisionLine swap() {
        return (PrecisionLine) super.swap();
    }

    /**
     * Constant to avoid divide by zero errors.
     */
    private static final double BIGSLOPE = 999999.99999;

    /**
     * Calculates the slope of this line segment (y=mx+b)
     * 
     * @return <code>float</code> the slope of this segment. If the slope is
     *         not defined such as when the line segment is vertical, then the
     *         constant <code>BIGSLOPE</code> is returned to avoid divide by
     *         zero errors.
     */
    public final double slope() {
        if (isVertical())
            return BIGSLOPE;

        return (point2.y - point1.y) / (point2.x - point1.x);
    }

    /**
     * Determines if this a vertical segment
     * 
     * @return <code>boolean</code> <code>true</code> if vertical,
     *         <code>false</code> otherwise.
     */
    public final boolean isVertical() {
        return (point1.x == point2.x);
    }

    public double getAngle() {
        return getTerminus().getAngle(point1);
    }

    public double getRelativeAngle(PrecisionLine another) {
        return getAngle() - another.getAngle();
    }

    public Side getSide(PrecisionPoint p) {
        if (point1.equals(p) || point2.equals(p))
            return Side.OnLine;
        double v = (p.x - point1.x) * (point2.y - point1.y) - (p.y - point1.y)
                * (point2.x - point1.x);
        if (Math.abs(v) < DEFAULT_TOLERANCE)
            return Side.OnLine;
        return v > 0 ? Side.Left : Side.Right;
//        
//        PrecisionLine line = new PrecisionLine(getOrigin(), p, LineType.Ray);
//        double angle = line.getRelativeAngle(this);
//        double sin = Math.sin(angle);
//        return sin == 0 ? Side.OnLine : (sin > 0 ? Side.Left : Side.Right);
    }

    public String toString() {
        return lineType + "[" + point1 + ", " + point2 + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}