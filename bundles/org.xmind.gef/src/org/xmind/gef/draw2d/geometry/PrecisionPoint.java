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

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import org.eclipse.draw2d.geometry.Point;

public class PrecisionPoint implements Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 4518124297995254542L;

    public static final PrecisionPoint SINGLETON = new PrecisionPoint();

    public double x;

    public double y;

    /**
     * 
     */
    public PrecisionPoint() {
    }

    /**
     * @param x
     * @param y
     */
    public PrecisionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param x
     * @param y
     */
    public PrecisionPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param copy
     */
    public PrecisionPoint(Point copy) {
        this(copy.x, copy.y);
    }

    public PrecisionPoint(PrecisionPoint p) {
        this(p.x, p.y);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PrecisionPoint))
            return false;
        PrecisionPoint p = (PrecisionPoint) obj;
        return this.x == p.x && this.y == p.y;
    }

    public PrecisionRectangle getExpanded(PrecisionInsets ins) {
        return ins.getBounds(this);
    }

    public PrecisionRectangle getExpanded(double top, double left,
            double bottom, double right) {
        return new PrecisionRectangle(x - left, y - top, left + right, top
                + bottom);
    }

    public double getAngle() {
        return Geometry.getAngle(x, y);
    }

    public double getAngle(PrecisionPoint p) {
        return Geometry.getAngle(this.x - p.x, this.y - p.y);
    }

    public PrecisionPoint getCopy() {
        return new PrecisionPoint(x, y);
    }

    public PrecisionDimension getDifference(PrecisionPoint p) {
        return new PrecisionDimension(this.x - p.x, this.y - p.y);
    }

    /**
     * Calculates the distance from this Point to the one specified.
     * 
     * @param pt
     *            The Point being compared to this
     * @return The distance
     * @since 2.0
     */
    public double getDistance(PrecisionPoint pt) {
        return Math.sqrt(getDistance2(pt));
    }

    /**
     * Calculates the distance squared between this Point and the one specified.
     * If the distance squared is larger than the maximum integer value, then
     * <code>Integer.MAX_VALUE</code> will be returned.
     * 
     * @param pt
     *            The reference Point
     * @return distance<sup>2</sup>
     * @since 2.0
     */
    public double getDistance2(PrecisionPoint pt) {
        double i = pt.x - x;
        double j = pt.y - y;
        return i * i + j * j;
    }

    public double getXYDistance(PrecisionPoint p) {
        return Math.abs(x - p.x) + Math.abs(y - p.y);
    }

    public Point toDraw2DPoint() {
        return toDraw2DPoint(x, y);
    }

    public static Point toDraw2DPoint(double x, double y) {
        return new Point((int) Math.floor(x + 0.000000001), (int) Math
                .floor(y + 0.000000001));
    }

    public Point toRoundedDraw2DPoint() {
        return new Point((int) Math.round(x + 0.000000001), (int) Math
                .round(y + 0.000000001));
    }

    public PrecisionPoint getMoved(double angle, double distance) {
        return getCopy().move(angle, distance);
    }

    public PrecisionPoint getNegated() {
        return getCopy().negate();
    }

    public PrecisionPoint getScaled(double amount) {
        return getCopy().scale(amount);
    }

    public PrecisionPoint getScaled(double xAmount, double yAmount) {
        return getCopy().scale(xAmount, yAmount);
    }

    public PrecisionPoint getTranslated(double dx, double dy) {
        return getCopy().translate(dx, dy);
    }

    public PrecisionPoint getTranslated(PrecisionDimension d) {
        return getCopy().translate(d);
    }

    public PrecisionPoint getTranslated(PrecisionPoint p) {
        return getCopy().translate(p);
    }

    public PrecisionPoint getTransposed() {
        return getCopy().transpose();
    }

    public PrecisionPoint move(double angle, double distance) {
        x += distance * cos(angle);
        y += distance * sin(angle);
        return this;
    }

    public PrecisionPoint negate() {
        x = -x;
        y = -y;
        return this;
    }

    public PrecisionPoint translate(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    public PrecisionPoint translate(PrecisionDimension d) {
        return translate(d.width, d.height);
    }

    public PrecisionPoint translate(PrecisionPoint p) {
        return translate(p.x, p.y);
    }

    public PrecisionPoint scale(double amount) {
        return scale(amount, amount);
    }

    public PrecisionPoint scale(double xAmount, double yAmount) {
        x *= xAmount;
        y *= yAmount;
        return this;
    }

    public PrecisionPoint setLocation(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public PrecisionPoint setLocation(Point p) {
        return setLocation(p.x, p.y);
    }

    public PrecisionPoint setLocation(PrecisionPoint p) {
        return setLocation(p.x, p.y);
    }

    public PrecisionPoint move(PrecisionPoint dest, double xAmount,
            double yAmount) {
        this.x = this.x * (1 - xAmount) + dest.x * xAmount;
        this.y = this.y * (1 - yAmount) + dest.y * yAmount;
        return this;
    }

    public PrecisionPoint move(PrecisionPoint dest, double amount) {
        return move(dest, amount, amount);
    }

    public PrecisionPoint getMoved(PrecisionPoint dest, double xAmount,
            double yAmount) {
        return getCopy().move(dest, xAmount, yAmount);
    }

    public PrecisionPoint getMoved(PrecisionPoint dest, double amount) {
        return getCopy().move(dest, amount);
    }

    public PrecisionPoint getCenter(PrecisionPoint dest) {
        return getMoved(dest, 0.5);
    }

    public String toString() {
        return "PrecisionPoint(" + x + ", " + y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public PrecisionPoint transpose() {
        double temp = x;
        x = y;
        y = temp;
        return this;
    }

}