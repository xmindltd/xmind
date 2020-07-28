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

/**
 * @author Frank Shaka
 */
public class PrecisionPolygon {

    private PrecisionPointList points;

    public PrecisionPolygon() {
        this.points = new PrecisionPointList();
    }

    public PrecisionPolygon(int size) {
        this.points = new PrecisionPointList(size);
    }

    public PrecisionPolygon(PrecisionPointList points) {
        this.points = points.getCopy();
    }

    public PrecisionPointList getPoints() {
        return points;
    }

    public boolean contains(PrecisionPoint toTest, boolean outline) {
        translate(-toTest.x, -toTest.y);
        int t1, t2;
        PrecisionPoint prev = getPoint(points.size() - 1);
        t1 = prev.x >= 0 ? (prev.y >= 0 ? 0 : 3) : (prev.y >= 0 ? 1 : 2); // ��������
        int sum = 0, i = 0;
        if (prev.x != 0 || prev.y != 0) {
            for (; i < points.size(); i++) {
                PrecisionPoint current = getPoint(i);
                if ((current.x == 0 && current.y == 0) && outline)
                    break; // �����Ϊ����ζ���

                double f = current.y * prev.x - current.x * prev.y; // ������
                if (f == 0 && prev.x * current.x <= 0
                        && prev.y * current.y <= 0 && outline)
                    break; // ���ڱ���
                t2 = current.x >= 0 ? (current.y >= 0 ? 0 : 3)
                        : (current.y >= 0 ? 1 : 2); // ��������
                if (t2 == (t1 + 1) % 4)
                    sum += 1; // ���1
                else if (t2 == (t1 + 3) % 4)
                    sum -= 1;// ���2
                else if (t2 == (t1 + 2) % 4)// ���3
                {
                    if (f > 0)
                        sum += 2;
                    else
                        sum -= 2;
                }
                t1 = t2;
                prev = current;
            }
        }
        boolean result = ((i < points.size() && outline) || sum > 0);
        translate(toTest.x, toTest.y);
        return result;
    }

    public PrecisionPointList intersect(PrecisionLine line, double tolerance) {
        PrecisionPointList result = new PrecisionPointList();
        for (int i = 0; i < points.size(); i++) {
            PrecisionLine l = getLine(i);
            PrecisionPoint p = l.intersect(line, tolerance);
            if (p != null)
                result.addPoint(p);
        }
        return result;
    }

    public PrecisionPointList intersect(PrecisionLine line) {
        return intersect(line, PrecisionLine.DEFAULT_TOLERANCE);
    }

    public PrecisionPoint intersectFarthest(PrecisionLine line, double tolerance) {
        PrecisionPoint result = null;
        for (int i = 0; i < points.size(); i++) {
            PrecisionLine l = getLine(i);
            PrecisionPoint p = l.intersect(line, tolerance);
            if (p != null) {
                if (result == null
                        || (p != null && p.getDistance2(line.getOrigin()) > result
                                .getDistance2(line.getOrigin()))) {
                    result = p;
                }
            }
        }
        return result;
    }

    public PrecisionPoint intersectFarthest(PrecisionLine line) {
        return intersectFarthest(line, PrecisionLine.DEFAULT_TOLERANCE);
    }

    public PrecisionPoint intersectNearest(PrecisionLine line, double tolerance) {
        PrecisionPoint result = null;
        for (int i = 0; i < points.size(); i++) {
            PrecisionLine l = getLine(i);
            PrecisionPoint p = l.intersect(line, tolerance);
            if (p != null) {
                if (result == null
                        || p.getDistance2(line.getOrigin()) < result
                                .getDistance2(line.getOrigin())) {
                    result = p;
                }
            }
        }
        return result;
    }

    public PrecisionPoint intersectNearest(PrecisionLine line) {
        return intersectNearest(line, PrecisionLine.DEFAULT_TOLERANCE);
    }

    /**
     * @param index
     * @return
     */
    public PrecisionLine getLine(int index) {
        if (index < 0 || index >= points.size())
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + points.size()); //$NON-NLS-1$ //$NON-NLS-2$
        PrecisionPoint origin = points.getPoint(index);
        PrecisionPoint terminus;
        if (index < points.size() - 1)
            terminus = points.getPoint(index + 1);
        else
            terminus = points.getPoint(0);
        return new PrecisionLine(origin, terminus);
    }

    public PrecisionPolygon getCopy() {
        return new PrecisionPolygon(points.getCopy());
    }

    public void addAll(PrecisionPointList source) {
        points.addAll(source);
    }

    public void addPoint(double x, double y) {
        points.addPoint(x, y);
    }

    public void addPoint(PrecisionPoint p) {
        points.addPoint(p);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PrecisionPolygon))
            return false;
        PrecisionPolygon p = (PrecisionPolygon) obj;
        return p.points.equals(points);
    }

    public PrecisionRectangle getBounds() {
        return points.getBounds();
    }

    public PrecisionPoint getFirstPoint() {
        return points.getFirstPoint();
    }

    public PrecisionPoint getLastPoint() {
        return points.getLastPoint();
    }

    public PrecisionPoint getMidPoint() {
        return points.getMidPoint();
    }

    public PrecisionPoint getPoint(int index) {
        return points.getPoint(index);
    }

    public int hashCode() {
        return points.hashCode();
    }

    public void insertPoint(PrecisionPoint p, int index) {
        points.insertPoint(p, index);
    }

    public void removeAllPoints() {
        points.removeAllPoints();
    }

    public PrecisionPoint removePoint(int index) {
        return points.removePoint(index);
    }

    public void reverse() {
        points.reverse();
    }

    public void scale(double amount) {
        points.scale(amount);
    }

    public void setPoint(PrecisionPoint p, int index) {
        points.setPoint(p, index);
    }

    public void setSize(int newSize) {
        points.setSize(newSize);
    }

    public int size() {
        return points.size();
    }

    public PrecisionPoint[] toArray() {
        return points.toArray();
    }

    public String toString() {
        return points.toString();
    }

    public void translate(double dx, double dy) {
        points.translate(dx, dy);
    }

    public void translate(PrecisionDimension d) {
        points.translate(d);
    }

    public static final PrecisionPolygon createFromRect(PrecisionRectangle r) {
        PrecisionPolygon result = new PrecisionPolygon();
        result.addPoint(r.getTopLeft());
        result.addPoint(r.getTopRight());
        result.addPoint(r.getBottomRight());
        result.addPoint(r.getBottomLeft());
        return result;
    }

}