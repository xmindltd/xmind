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

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.PrecisionLine.LineType;

public class Geometry {

    public static final double MIN_DISTANCE = 0.00000001;

    public static final double TWO_PI = PI * 2;

    public static final double HALF_PI = PI / 2;

    public static final double NEG_HALF_PI = -PI / 2;

    public static final int QUADRANT_ONE = 1;

    public static final int QUADRANT_TWO = 2;

    public static final int QUADRANT_THREE = 3;

    public static final int QUADRANT_FOUR = 4;

    public static final int SIDE_ONE = 1;

    public static final int SIDE_TWO = 2;

    public static final int SIDE_THREE = 3;

    public static final int SIDE_FOUR = 4;

    public static final int SIDE_FIVE = 5;

    public static final int SIDE_SIX = 6;

    public static final int SIDE_SEVEN = 7;

    public static final int SIDE_EIGHT = 8;

    protected Geometry() {
    }

    /**
     * Calculates the radian angle of the line from (x, y) to (0, 0).
     * 
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     * @return The radian angle of the line from (x, y) to (0, 0). Varies from
     *         <code>-pi</code>(excluded) to <code>pi</code>(included).
     *         <code>0</code> means rightwards; <code>pi/2</code> means
     *         downwards; <code>pi</code> means leftwards; <code>-pi/2</code>
     *         means upwards.
     */
    public static double getAngle(double x, double y) {
        if (x == 0) {
            if (y > 0)
                return HALF_PI;
            if (y < 0)
                return NEG_HALF_PI;
            return 0;
        }
        double a = atan(y / x);
        if (x > 0)
            return a;
        if (y < 0)
            return a - PI;
        return a + PI;
    }

    public static double getAngle(Dimension d) {
        return getAngle(d.width, d.height);
    }

    public static double getAngle(PrecisionDimension d) {
        return getAngle(d.width, d.height);
    }

    public static double getAngle(Point p) {
        return getAngle(p.x, p.y);
    }

    public static double getAngle(PrecisionPoint p) {
        return getAngle(p.x, p.y);
    }

    public static double getAngle(Point p, Point origin) {
        return getAngle(p.x - origin.x, p.y - origin.y);
    }

    public static double getAngle(PrecisionPoint p, PrecisionPoint origin) {
        return getAngle(p.x - origin.x, p.y - origin.y);
    }

    /**
     * 
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param deltaAngle
     *            The angle between the line (x1, y1)-(x, y) and the line (x2,
     *            y2)-(x1, y1)
     * @param amount
     *            The amount of the ratio of the distance (x1, y1)-(x, y) to the
     *            distance (x1, y1)-(x2, y2)
     * @param minDistance
     *            The minimum distance from (x1, y1) to (x2, y2) to avoid
     *            division by zero
     * @return An point by the <code>deltaAngle</code> and <code>amount</code>
     *         from (x1, y1) to (x2, y2).
     */
    public static PrecisionPoint getPoint(double x1, double y1, double x2,
            double y2, double deltaAngle, double amount, double minDistance,
            PrecisionPoint result) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double d = Math.max(Math.hypot(dx, dy), minDistance);
        double angle = getAngle(dx, dy);
        return result.setLocation(x1, y1).move(angle + deltaAngle, d * amount);
    }

    public static PrecisionPoint getPoint(double x1, double y1, double x2,
            double y2, double deltaAngle, double amount, PrecisionPoint result) {
        return getPoint(x1, y1, x2, y2, deltaAngle, amount, MIN_DISTANCE,
                result);
    }

    public static PrecisionPoint getPoint2(double x1, double y1, double x2,
            double y2, double deltaAngle, double dist, PrecisionPoint result) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = getAngle(dx, dy);
        return result.setLocation(x1, y1).move(angle + deltaAngle, dist);
    }

    /**
     * Calculates the amount of the ratio of the distance (x1, y1)-(x, y) to the
     * distance (x1, y1)-(x2, y2).
     * 
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param minDistance
     *            The minimum distance from (x1, y1) to (x2, y2) to avoid
     *            division by zero
     * @return
     * @throws IllegalArgumentException
     *             if minDistance is equal to or less than zero
     */
    public static double getAmount(double x, double y, double x1, double y1,
            double x2, double y2, double minDistance) {
        if (minDistance <= 0)
            throw new IllegalArgumentException();
        double d = Math.hypot(x - x1, y - y1);
        double d2 = Math.max(Math.hypot(x2 - x1, y2 - y1), minDistance);
        return d / d2;
    }

    public static double getAmount(double x, double y, double x1, double y1,
            double x2, double y2) {
        return getAmount(x, y, x1, y1, x2, y2, MIN_DISTANCE);
    }

    /**
     * Calculates the angle between the line (x1, y1)-(x, y) and the line (x1,
     * y1)-(x2, y2).
     * 
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double getDeltaAngle(double x, double y, double x1,
            double y1, double x2, double y2) {
        double angle = getAngle(x - x1, y - y1);
        double angle2 = getAngle(x2 - x1, y2 - y1);
        return constrainAngleRadian(angle - angle2);
    }

    /**
     * Converts a polar coordinate system to rectangular coordinate system.
     * 
     * @param angle
     *            The angular coordinate; <code>0</code> means rightwards;
     *            <code>pi/2</code> means downwards; <code>pi</code> means
     *            leftwards; <code>-pi/2</code> means upwards.
     * @param distance
     *            The radial coordinate.
     * @return A point representing the given position in rectangular coordinate
     *         form.
     */
    public static Point getPoint(double angle, double distance) {
        double x = distance * cos(angle);
        double y = distance * sin(angle);
        return new Point((int) x, (int) y);
    }

    /**
     * 
     * @param angle
     * @param distance
     * @return
     */
    public static Point getPoint(Point p, double angle, double distance) {
        return p.getTranslated(getPoint(angle, distance));
    }

    public static boolean isSameAngleDegree(double angle1, double angle2,
            double tolerance) {
        return Math.abs(constrainAngleDegree(angle1)
                - constrainAngleDegree(angle2)) < tolerance;
    }

    public static double constrainAngleDegree(double angle) {
        angle = angle - ((int) (angle / 360)) * 360;
        if (angle < -180)
            angle += 360;
        else if (angle > 180)
            angle -= 360;
        return angle;
    }

    public static boolean isSameAngleRadian(double angle1, double angle2,
            double tolerance) {
        return Math.abs(constrainAngleRadian(angle1)
                - constrainAngleRadian(angle2)) < tolerance;
    }

    public static double constrainAngleRadian(double angle) {
        angle = angle - ((int) (angle / TWO_PI)) * TWO_PI;
        if (angle < NEG_HALF_PI)
            angle += TWO_PI;
        else if (angle > PI)
            angle -= TWO_PI;
        return angle;
    }

    public static Insets expand(Insets ins, int exp) {
        return ins.add(new Insets(exp, exp, exp, exp));
    }

    public static Insets expand(Insets ins, int width, int height) {
        return ins.add(new Insets(height, width, height, width));
    }

    public static Insets getExpanded(Insets ins, int exp) {
        return expand(new Insets(ins), exp);
    }

    public static Insets getExpanded(Insets ins, int width, int height) {
        return expand(new Insets(ins), width, height);
    }

    public static Insets union(Insets ins, Insets other) {
        if (other == null)
            return ins;
        return union(ins, other.top, other.left, other.bottom, other.right);
    }

    public static Insets union(Insets ins, int top, int left, int bottom,
            int right) {
        if (ins == null)
            return new Insets(top, left, bottom, right);
        ins.top = Math.max(ins.top, top);
        ins.left = Math.max(ins.left, left);
        ins.bottom = Math.max(ins.bottom, bottom);
        ins.right = Math.max(ins.right, right);
        return ins;
    }

    public static Insets add(Insets ins, Insets other) {
        if (other == null)
            return ins;
        if (ins == null)
            return new Insets(other);
        return ins.add(other);
    }

    public static Insets add(Insets ins, int margin) {
        return add(ins, margin, margin, margin, margin);
    }

    public static Insets add(Insets ins, int top, int left, int bottom,
            int right) {
        if (ins == null)
            return new Insets(top, left, bottom, right);
        ins.top += top;
        ins.left += left;
        ins.bottom += bottom;
        ins.right += right;
        return ins;
    }

    public static Insets getUnioned(Insets ins, Insets other) {
        if (other == null)
            return ins == null ? null : new Insets(ins);
        if (ins == null)
            return new Insets(other);
        return union(new Insets(ins), other);
    }

    public static Insets remove(Insets from, Insets toRemove) {
        from.top = max(0, from.top - toRemove.top);
        from.bottom = max(0, from.bottom - toRemove.bottom);
        from.left = max(0, from.left - toRemove.left);
        from.right = max(0, from.right - toRemove.right);
        return from;
    }

    public static Insets getRemoved(Insets from, Insets toRemove) {
        return remove(new Insets(from), toRemove);
    }

    public static Insets scale(Insets ins, double scale) {
        ins.top = (int) (Math.floor(ins.top * scale));
        ins.left = (int) (Math.floor(ins.left * scale));
        ins.bottom = (int) (Math.floor(ins.bottom * scale));
        ins.right = (int) (Math.floor(ins.right * scale));
        return ins;
    }

    public static Insets getScaled(Insets ins, double scale) {
        return scale(new Insets(ins), scale);
    }

    public static Rectangle shrink(Rectangle r, Insets i) {
        r.x += i.left;
        r.y += i.top;
        r.width -= i.getWidth();
        r.height -= i.getHeight();
        return r;
    }

    public static Rectangle getShrinked(Rectangle r, Insets i) {
        return shrink(r.getCopy(), i);
    }

    public static Insets getCropped(Rectangle box, Rectangle clip) {
        Insets ins = new Insets();
        ins.top = clip.y - box.y;
        ins.left = clip.x - box.x;
        ins.bottom = box.bottom() - clip.bottom();
        ins.right = box.right() - clip.right();
        return ins;
    }

    /**
     * Calculates distances from a point to the four sides of a rectangle.
     * 
     * @param p
     *            the point
     * @param box
     *            the rectangle
     * @return the distances from the point to the four sides of the rectangle
     */
    public static Insets getInsets(Point p, Rectangle box) {
        return new Insets(p.y - box.y, p.x - box.x, box.bottom() - p.y,
                box.right() - p.x);
    }

    public static Rectangle getExpanded(Point p, Insets ins) {
        return getExpanded(p.x, p.y, ins);
    }

    public static Rectangle getExpanded(int x, int y, Insets ins) {
        return new Rectangle(x - ins.left, y - ins.top, ins.getWidth(),
                ins.getHeight());
    }

    public static Rectangle getBounds(Rectangle box, boolean outline,
            int lineWidth, int expansion) {
        if (outline)
            box = box.getResized(-lineWidth, -lineWidth);
        if (expansion != 0)
            box = box.getExpanded(expansion, expansion);
        return box;
    }

    public static Point getLocation(int orientation, Rectangle box,
            boolean outline, int lineWidth, int expansion) {
        box = getBounds(box, outline, lineWidth, expansion);
        switch (orientation) {
        case PositionConstants.EAST:
            return box.getRight();
        case PositionConstants.WEST:
            return box.getLeft();
        case PositionConstants.SOUTH:
            return box.getBottom();
        case PositionConstants.NORTH:
            return box.getTop();
        case PositionConstants.NORTH_EAST:
            return box.getTopRight();
        case PositionConstants.NORTH_WEST:
            return box.getTopLeft();
        case PositionConstants.SOUTH_EAST:
            return box.getBottomRight();
        case PositionConstants.SOUTH_WEST:
            return box.getBottomLeft();
        }
        return box.getCenter();
    }

    /**
     * @param originSize
     * @param constrainedSize
     * @return
     */
    public static Dimension getScaledConstrainedSize(Dimension originSize,
            Dimension constrainedSize) {
        return getScaledConstrainedSize(originSize.width, originSize.height,
                constrainedSize.width, constrainedSize.height);
    }

    public static Dimension getScaledConstrainedSize(int w, int h,
            int maxWidth, int maxHeight) {
        if (w == 0 || h == 0)
            return new Dimension();
        if (maxWidth < 0 && maxHeight < 0)
            return new Dimension(w, h);
        if (w <= maxWidth && h <= maxHeight)
            return new Dimension(w, h);
        int nw = w * maxHeight / h;
        int nh = h * maxWidth / w;
        if (maxWidth < 0)
            return new Dimension(nw, maxHeight);
        if (maxHeight < 0)
            return new Dimension(maxWidth, nh);
        if (nw < maxWidth)
            maxWidth = nw;
        if (nh < maxHeight)
            maxHeight = nh;
        return new Dimension(Math.max(1, maxWidth), Math.max(1, maxHeight));
    }

    public static Point getPopupLocation(Dimension size, Rectangle host,
            Rectangle display) {
        Point p = host.getBottomLeft();
        if (p.y + size.height > display.bottom()) {
            p.y = host.y - size.height;
            if (p.y < 0) {
                p.y = display.y - size.height;
            }
        }
        if (p.y < display.y) {
            p.y = display.y;
        }
        if (p.x + size.width > display.right()) {
            p.x = display.right() - size.width;
        }
        if (p.x < display.x) {
            p.x = display.x;
        }
        return p;
    }

    public static int getOrientation(Point p) {
        return getOrientation(p.x, p.y, 0, 0);
    }

    public static int getOrientation(Point p, Point origin) {
        if (p == null)
            return PositionConstants.NONE;
        return getOrientation(p.x, p.y, origin.x, origin.y);
    }

    public static int getOrientation(int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 < y2)
            return PositionConstants.NORTH;
        if (x1 > x2 && y1 < y2)
            return PositionConstants.NORTH_EAST;
        if (x1 > x2 && y1 == y2)
            return PositionConstants.EAST;
        if (x1 > x2 && y1 > y2)
            return PositionConstants.SOUTH_EAST;
        if (x1 == x2 && y1 > y2)
            return PositionConstants.SOUTH;
        if (x1 < x2 && y1 > y2)
            return PositionConstants.SOUTH_WEST;
        if (x1 < x2 && y1 == y2)
            return PositionConstants.WEST;
        if (x1 < x2 && y1 < y2)
            return PositionConstants.NORTH_WEST;
        return PositionConstants.NONE;
    }

    public static int getOppositePosition(int position) {
        if (position == PositionConstants.EAST)
            return PositionConstants.WEST;
        if (position == PositionConstants.WEST)
            return PositionConstants.EAST;
        if (position == PositionConstants.SOUTH)
            return PositionConstants.NORTH;
        if (position == PositionConstants.NORTH)
            return PositionConstants.SOUTH;
        if (position == PositionConstants.NORTH_EAST)
            return PositionConstants.SOUTH_WEST;
        if (position == PositionConstants.NORTH_WEST)
            return PositionConstants.SOUTH_EAST;
        if (position == PositionConstants.SOUTH_EAST)
            return PositionConstants.NORTH_WEST;
        if (position == PositionConstants.SOUTH_WEST)
            return PositionConstants.NORTH_EAST;
        if (position == PositionConstants.NORTH_SOUTH)
            return PositionConstants.EAST_WEST;
        if (position == PositionConstants.EAST_WEST)
            return PositionConstants.NORTH_SOUTH;
        return position;
    }

    public static Point getPositionWithin(int alignment, Rectangle boundingBox,
            Dimension toPlace) {
        int x;
        if ((alignment & PositionConstants.LEFT) != 0) {
            x = boundingBox.x;
        } else if ((alignment & PositionConstants.RIGHT) != 0) {
            x = boundingBox.right() - toPlace.width;
        } else {
            x = boundingBox.x + (boundingBox.width - toPlace.width) / 2;
        }
        int y;
        if ((alignment & PositionConstants.TOP) != 0) {
            y = boundingBox.y;
        } else if ((alignment & PositionConstants.BOTTOM) != 0) {
            y = boundingBox.bottom() - toPlace.height;
        } else {
            y = boundingBox.y + (boundingBox.height - toPlace.height) / 2;
        }
        return new Point(x, y);
    }

    public static Point getRotatedPoint(Point p, double angle) {
        angle += getAngle(p);
        double distance = p.getDistance(new Point());
        return getPoint(angle, distance);
    }

    public static PointList getRotatedRectangle(Rectangle r, double theta) {
        PointList pl = new PointList(4);
        pl.addPoint(getRotatedPoint(r.getTopLeft(), theta));
        pl.addPoint(getRotatedPoint(r.getTopRight(), theta));
        pl.addPoint(getRotatedPoint(r.getBottomLeft(), theta));
        pl.addPoint(getRotatedPoint(r.getBottomRight(), theta));
        return pl;
    }

    public static Dimension getConstrainedSize(Dimension size,
            Dimension minSize, Dimension maxSize) {
        return new Dimension(
                constrain(size.width, minSize.width, maxSize.width), constrain(
                        size.height, minSize.height, maxSize.height));
    }

    public static int constrain(int i, int min, int max) {
        return Math.max(Math.min(i, max), min);
    }

    public static double constrain(double d, double min, double max) {
        return Math.max(Math.min(d, max), min);
    }

    public static Dimension getScaledConstrainedSize2(Dimension size,
            Dimension origin, Dimension minSize, Dimension maxSize) {
        return getScaledConstrainedSize2(size.width, size.height,
                slope(origin), minSize.width, minSize.height, maxSize.width,
                maxSize.height);
    }

    public static Dimension getScaledConstrainedSize2(double width,
            double height, Dimension origin, Dimension minSize,
            Dimension maxSize) {
        return getScaledConstrainedSize2(width, height, slope(origin),
                minSize.width, minSize.height, maxSize.width, maxSize.height);
    }

    public static Dimension getScaledConstrainedSize2(double width,
            double height, double originWidth, double originHeight,
            double minWidth, double minHeight, double maxWidth, double maxHeight) {
        return getScaledConstrainedSize2(width, height,
                slope(originWidth, originHeight), minWidth, minHeight,
                maxWidth, maxHeight);
    }

    public static Dimension getScaledConstrainedSize2(double width,
            double height, double slope, double minWidth, double minHeight,
            double maxWidth, double maxHeight) {
        double prefH = constrain(width * slope, minHeight, maxHeight);
        if (height < prefH) {
            if (height >= 0)
                height = prefH;
            else
                width = constrain(width, minWidth, maxWidth);
        } else {
            if (height >= 0)
                width = constrain(height / slope, minWidth, maxWidth);
        }
        height = constrain(width * slope, minHeight, maxHeight);
        width = constrain(height / slope, minWidth, maxWidth);
        return new Dimension((int) (Math.floor(width)),
                (int) (Math.floor(height)));
    }

    public static double slope(Dimension size) {
        return slope(size.width, size.height);
    }

    public static double slope(double width, double height) {
        return height / width;
    }

    /**
     * Method getLineSegments. Converts the points of this polyline into a list
     * of <code>PrecisionLine</code> objects
     * 
     * @param points
     *            PointList to get PrecisionLine equivalents of.
     * @return List of PrecisionLine objects.
     */
    public static List<PrecisionLine> getLineSegments(
            List<PrecisionPoint> points) {
        if (points.size() <= 1)
            return new ArrayList<PrecisionLine>(0);
        ArrayList<PrecisionLine> lines = new ArrayList<PrecisionLine>(
                points.size() - 1);
        for (int i = 0; i < points.size() - 1; i++) {
            lines.add(new PrecisionLine(points.get(i), points.get(i + 1),
                    LineType.LineSegment));
        }
        return lines;
    }

    public static List<PrecisionLine> getLineSegments(PrecisionPoint... points) {
        return getLineSegments(Arrays.asList(points));
    }

    /**
     * Method getLineSegments. Converts the points of this polyline into a list
     * of <code>PrecisionLine</code> objects
     * 
     * @param points
     *            PointList to get PrecisionLine equivalents of.
     * @return List of PrecisionLine objects.
     */
    public static List<PrecisionLine> getLineSegments(PrecisionPoint origin,
            List<PrecisionPoint> points) {
        if (points.size() <= 1)
            return new ArrayList<PrecisionLine>(0);

        ArrayList<PrecisionLine> lines = new ArrayList<PrecisionLine>(
                points.size() - 1);

        for (int i = 0; i < points.size() - 1; i++) {
            lines.add(new PrecisionLine(new PrecisionPoint(origin)
                    .translate(points.get(i)), new PrecisionPoint(origin)
                    .translate(points.get(i + 1)), LineType.LineSegment));
        }
        return lines;
    }

    public static PrecisionPoint getRayXLine(PrecisionLine ray,
            PrecisionLine line, int tolerance) {
        List<PrecisionPoint> intersections = ray.getLinesIntersections(line);
        for (int i = 0; i < intersections.size(); i++) {
            PrecisionPoint p = intersections.get(i);
            if (rayContainsPoint(ray, p, tolerance))
                return p;
        }
        return null;
    }

    public static PrecisionPoint getRayXLineSeg(PrecisionLine ray,
            PrecisionLine lineSeg, int tolerance) {
        List<PrecisionPoint> intersections = ray.getLinesIntersections(lineSeg);
        for (int i = 0; i < intersections.size(); i++) {
            PrecisionPoint p = intersections.get(i);
            if (lineSeg.contains(p, tolerance)
                    && rayContainsPoint(ray, p, tolerance))
                return p;
        }
        return null;
    }

    public static PrecisionPoint getRayXRect(PrecisionLine ray,
            PrecisionRectangle r, int tolerance) {
        List<PrecisionLine> lines = getLineSegments(r.getTopLeft(),
                r.getTopRight(), r.getBottomRight(), r.getBottomLeft(),
                r.getTopLeft());
        for (PrecisionLine line : lines) {
            PrecisionPoint p = getRayXLineSeg(ray, line, tolerance);
            if (p != null)
                return p;
        }
        return null;
    }

    public static PrecisionPoint getRayXRectFarther(PrecisionLine ray,
            PrecisionRectangle r, int tolerance) {
        PrecisionPoint ret = null;
        double dist = -1;
        List<PrecisionLine> lines = getLineSegments(r.getTopLeft(),
                r.getTopRight(), r.getBottomRight(), r.getBottomLeft(),
                r.getTopLeft());
        for (PrecisionLine line : lines) {
            PrecisionPoint p = getRayXLineSeg(ray, line, tolerance);
            if (p != null) {
                if (ret == null) {
                    ret = p;
                    dist = ret.getDistance(ray.getOrigin());
                } else {
                    double d = p.getDistance(ray.getOrigin());
                    if (d > dist) {
                        ret = p;
                        dist = d;
                    }
                }
            }
        }
        return ret;
    }

    public static boolean rayContainsPoint(PrecisionLine ray, PrecisionPoint p,
            int tolerance) {
        return ray.contains(p, tolerance)
                || new PrecisionLine(ray.getOrigin(), p).contains(
                        ray.getTerminus(), tolerance);
    }

//    public static PrecisionPoint getCenter( PrecisionPoint p1, PrecisionPoint p2 ) {
//        return new PrecisionPoint( ( p1.x + p2.x ) / 2, ( p1.y + p2.y) / 2 );
//    }

    public static Rectangle union(Rectangle r1, Rectangle r2) {
        if (r2 == null)
            return r1;
        if (r1 == null)
            return r2.getCopy();
        return r1.union(r2);
    }

    public static PrecisionRectangle union(PrecisionRectangle r1,
            PrecisionRectangle r2) {
        if (r2 == null)
            return r1;
        if (r1 == null)
            return r2.getCopy();
        return r1.union(r2);
    }

    public static Rectangle intersect(Rectangle r1, Rectangle r2) {
        if (r2 == null)
            return r1;
        if (r1 == null)
            return r2.getCopy();
        return r1.intersect(r2);
    }

    public static PrecisionRectangle intersect(PrecisionRectangle r1,
            PrecisionRectangle r2) {
        if (r2 == null)
            return r1;
        if (r1 == null)
            return r2.getCopy();
        return r1.intersect(r2);
    }

    public static PrecisionPoint[] intersectQuadBezier(PrecisionLine line,
            double x1, double y1, double x2, double y2, double x3, double y3) {
        double u1 = x1 - 2 * x2 + x3;
        double v1 = y1 - 2 * y2 + y3;
        double u2 = 2 * x2 - 2 * x3;
        double v2 = 2 * y2 - 2 * y3;
        double u3 = x3;
        double v3 = y3;
        double[] eq = line.getEquation();
        double a = eq[0] * u1 + eq[1] * v1;
        double b = eq[0] * u2 + eq[1] * v2;
        double c = eq[0] * u3 + eq[1] * v3 - eq[2];
        double delta = b * b - 4 * a * c;
        if (Math.abs(delta) < 0.000000001) {
            double t = -b / (2 * a);
            if (t >= 0 && t <= 1) {
                double x = u1 * t * t + u2 * t + u3;
                double y = v1 * t * t + v2 * t + v3;
                PrecisionPoint p = new PrecisionPoint(x, y);
                if (line.contains(p))
                    return new PrecisionPoint[] { p };
            }
        } else if (delta > 0) {
            double d = Math.sqrt(delta);
            List<PrecisionPoint> list = new ArrayList<PrecisionPoint>();
            double t1 = (-b + d) / (2 * a);
            if (t1 >= 0 && t1 <= 1) {
                double x = u1 * t1 * t1 + u2 * t1 + u3;
                double y = v1 * t1 * t1 + v2 * t1 + v3;
                PrecisionPoint p = new PrecisionPoint(x, y);
                if (line.contains(p)) {
                    list.add(p);
                }
            }
            double t2 = (-b - d) / (2 * a);
            if (t2 >= 0 && t2 <= 1) {
                double x = u1 * t2 * t2 + u2 * t2 + u3;
                double y = v1 * t2 * t2 + v2 * t2 + v3;
                PrecisionPoint p = new PrecisionPoint(x, y);
                if (line.contains(p)) {
                    list.add(p);
                }
            }
            return list.toArray(new PrecisionPoint[0]);
        }
        return new PrecisionPoint[0];
    }

    public static PrecisionPoint getChopBoxLocation(double refX, double refY,
            Rectangle r, double expansion) {
        double centerX = r.x + 0.5 * r.width;
        double centerY = r.y + 0.5 * r.height;
        double dx = refX - centerX;
        double dy = refY - centerY;
        if (dx == 0)
            return new PrecisionPoint(refX, (dy > 0) ? r.bottom() + expansion
                    : r.y - expansion);
        if (dy == 0)
            return new PrecisionPoint((dx > 0) ? r.right() + expansion : r.x
                    - expansion, refY);
        double scale = 0.5 / Math.max(Math.abs(dx) / r.width, Math.abs(dy)
                / r.height);
        dx *= scale;
        dy *= scale;
        double d = Math.hypot(dx, dy);
        if (d != 0) {
            double s = expansion / d;
            dx += dx * s;
            dy += dy * s;
        }
        centerX += dx;
        centerY += dy;
        return new PrecisionPoint(centerX, centerY);
    }

    public static Point getChopBoxLocation(int x, int y, Rectangle box) {
        float centerX = box.x + 0.5f * box.width;
        float centerY = box.y + 0.5f * box.height;

        float dx = x - centerX;
        float dy = y - centerY;

//        when preference in the center
        if (dx == 0)
            return new Point(x, (dy > 0) ? box.bottom() : box.y);
        if (dy == 0)
            return new Point((dx > 0) ? box.right() : box.x, y);

        // r.width, r.height, dx, and dy are guaranteed to be non-zero.
        float scale = 0.5f / Math.max(Math.abs(dx) / box.width, Math.abs(dy)
                / box.height);

        dx *= scale;
        dy *= scale;
        centerX += dx;
        centerY += dy;

        return new Point(Math.round(centerX), Math.round(centerY));
    }

    public static Point getChopBoxLocation(Point reference, Rectangle box) {
        return getChopBoxLocation(reference.x, reference.y, box);
    }

    public static PrecisionPoint getChopOvalLocation(double refX, double refY,
            Rectangle r, double expansion) {
        double cx = r.x + r.width * 0.5d;
        double cy = r.y + r.height * 0.5d;
        double rx = refX - cx;
        double ry = refY - cy;
        if (rx == 0)
            return new PrecisionPoint(refX, (ry > 0) ? r.bottom() + expansion
                    : r.y - expansion);
        if (ry == 0)
            return new PrecisionPoint((rx > 0) ? r.right() + expansion : r.x
                    - expansion, refY);

        double scaleX = (rx > 0) ? 0.5d : -0.5d;
        double scaleY = (ry > 0) ? 0.5d : -0.5d;

        // ref.x, ref.y, r.width, r.height != 0 => safe to proceed

        double k = (ry * r.width) / (rx * r.height);
        k = k * k;

        double dx = r.width * scaleX / sqrt(1 + k);
        double dy = r.height * scaleY / sqrt(1 + 1 / k);
        double d = Math.hypot(dx, dy);
        if (d != 0) {
            double s = expansion / d;
            dx += dx * s;
            dy += dy * s;
        }
        return new PrecisionPoint(cx + dx, cy + dy);
    }

    public static Point getChopOvalLocation(int x, int y, Rectangle r) {
        Point ref = r.getCenter().negate().translate(x, y);

        if (ref.x == 0)
            return new Point(x, (ref.y > 0) ? r.bottom() : r.y);
        if (ref.y == 0)
            return new Point((ref.x > 0) ? r.right() : r.x, y);

        float dx = (ref.x > 0) ? 0.5f : -0.5f;
        float dy = (ref.y > 0) ? 0.5f : -0.5f;

        // ref.x, ref.y, r.width, r.height != 0 => safe to proceed

        float k = (float) (ref.y * r.width) / (ref.x * r.height);
        k = k * k;

        return r.getCenter().translate((int) (r.width * dx / sqrt(1 + k)),
                (int) (r.height * dy / sqrt(1 + 1 / k)));
    }

    public static Point getChopOvalLocation(Point reference, Rectangle r) {
        return getChopOvalLocation(reference.x, reference.y, r);
    }

    public static PrecisionPointPair calculatePositionPair(PrecisionPoint from,
            PrecisionPoint to, double w) {
        PrecisionPointPair result = new PrecisionPointPair(from.getCopy(),
                from.getCopy());
        if (from.equals(to))
            return result;

        PrecisionDimension d = from.getDifference(to);
        double wScale = d.width == 0 ? 1 : Math.abs(w / d.width);
        double hScale = d.height == 0 ? 1 : Math.abs(w / d.height);
        d.scale(wScale, hScale);
        result.translate(d);
        d.width = -d.width;
        result.translateFirstPoint(d.transpose());
        result.translateSecondPoint(d.negate());

        return result;
    }

    public static double getDistance(PrecisionPoint p, PrecisionLine line) {
        PrecisionPoint p1 = line.getOrigin();
        PrecisionPoint p2 = line.getTerminus();
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        if (dx == 0 && dy == 0)
            return 0;
        if (dy == 0)
            return Math.abs(p.y - p1.y);
        if (dx == 0)
            return Math.abs(p.x - p1.x);
        double k = dx / dy;
        double m = dy / dx;
        double y = (p1.y * k - p1.x + p.y * m + p.x) / (k + m);
        double x = (y - p1.y) * k + p1.x;
        return Math.hypot(p.x - x, p.y - y);
    }

//     *          --------------------------
//     *          |           |            |
//     *          |     3     |      4     |
//     *          ------------------------------>               
//     *          |           |            |
//     *          |     2     |      1     |
//     *          --------------------------
//     *                     \/
    public static int getQuadrant(double x, double y, Point center) {
        if (x <= center.x && y <= center.y) {
            return QUADRANT_THREE;
        } else if (x <= center.x && y >= center.y) {
            return QUADRANT_TWO;
        } else if (x >= center.x && y <= center.y) {
            return QUADRANT_FOUR;
        } else {
            return QUADRANT_ONE;
        }
    }

    /**
     * Calculate angle of point(x,y)->rectangle center; Angles are interpreted
     * such that 0 degrees is at the 3 o'clock position. A positive value
     * indicates a counter-clockwise rotation while a negative value indicates a
     * clockwise rotation. the returned angle is in the range <i>-pi</i> through
     * <i>pi</i>.
     */
    public static double getAngle(double x, double y, Rectangle r) {
        double cx = r.x + r.width * 0.5d;
        double cy = r.y + r.height * 0.5d;

        if (x == cx && y >= cy)
            return HALF_PI;
        if (x == cx && y <= cy)
            return NEG_HALF_PI;

        double angrad = Math.atan((y - cy) / (x - cx));
        int quadrant = getQuadrant(x, y, r.getCenter());

        if (quadrant == QUADRANT_ONE) {
            angrad = -angrad;
        } else if (quadrant == QUADRANT_TWO) {
            angrad = -PI - angrad;
        } else if (quadrant == QUADRANT_THREE) {
            angrad = PI - angrad;
        } else if (quadrant == QUADRANT_FOUR) {
            angrad = -angrad;
        }

        return angrad;
    }

    /**
     * Calculate oval angle of point correspondence;Angles are interpreted such
     * that 0 degrees is at the 3 o'clock position;the returned angle is in the
     * range <i>-pi</i> through <i>pi</i>.
     * 
     * @param x
     *            x-coordinate of the point on the ellipse
     * @param y
     *            y-coordinate of the point on the ellipse
     * @param r
     *            oval external rectangle
     * @return
     */
    public static double getOvalAngle(double x, double y, Rectangle r) {
        double cx = r.x + r.width * 0.5d;

        double k = 2 * (x - cx) / r.width;
        if (k > 1)
            k = 1;
        if (k < -1)
            k = -1;

        double angrad = Math.acos(k);
        int quadrant = getQuadrant(x, y, r.getCenter());

        if (quadrant == QUADRANT_ONE || quadrant == QUADRANT_TWO) {
            angrad = -angrad;
        }
        return angrad;
    }

    /**
     * Calculate point(x,y) which edge contains.
     * 
     * @param x
     * @param y
     * @param r
     * @return
     */
    public static int getSide(double x, double y, Rectangle r) {
        double cx = r.x + r.width * 0.5d;
        double cy = r.y + r.height * 0.5d;

        if ((x >= r.x && x <= cx) && Math.abs(y - r.y) <= MIN_DISTANCE) {
            return SIDE_ONE;
        } else if ((x >= cx && x <= r.x + r.width * 1d)
                && Math.abs(y - r.y) <= MIN_DISTANCE) {
            return SIDE_TWO;
        } else if ((y >= r.y && y <= cy)
                && Math.abs((x - (r.x + r.width * 1.0d))) <= MIN_DISTANCE) {
            return SIDE_THREE;
        } else if ((y >= cy && y <= r.y + r.height * 1d)
                && Math.abs((x - (r.x + r.width * 1.0d))) <= MIN_DISTANCE) {
            return SIDE_FOUR;
        } else if ((x >= cx && x <= r.x + r.width * 1d)
                && Math.abs((y - (r.y + r.height * 1.0d))) <= MIN_DISTANCE) {
            return SIDE_FIVE;
        } else if ((x >= r.x && x <= cx)
                && Math.abs((y - (r.y + r.height * 1.0d))) <= MIN_DISTANCE) {
            return SIDE_SIX;
        } else if ((y >= cy && y <= r.y + r.height * 1.0d)
                && (Math.abs(x - r.x) <= MIN_DISTANCE)) {
            return SIDE_SEVEN;
        } else {
            return SIDE_EIGHT;
        }
    }
}