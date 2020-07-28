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
public class PrecisionPointList {

    private int size;

    private PrecisionPoint[] points;

    private PrecisionRectangle bounds;

    public PrecisionPointList() {
        this(0);
    }

    public PrecisionPointList(int size) {
        this.size = size;
        this.points = new PrecisionPoint[size];
        this.bounds = null;
    }

    public void addAll(PrecisionPointList source) {
        ensureCapacity(size + source.size);
        System.arraycopy(source.points, 0, points, size, source.size);
        size += source.size;
    }

    public void addPoint(PrecisionPoint p) {
        addPoint(p.x, p.y);
    }

    public void addPoint(double x, double y) {
        bounds = null;
        int index = size;
        ensureCapacity(size + 1);
        points[index] = new PrecisionPoint(x, y);
        size++;
    }

    private void ensureCapacity(int newSize) {
        if (points.length < newSize) {
            PrecisionPoint[] old = points;
            points = new PrecisionPoint[newSize];
            System.arraycopy(old, 0, points, 0, size);
        }
    }

    public PrecisionRectangle getBounds() {
        if (bounds == null) {
            bounds = new PrecisionRectangle();
            if (size > 0) {
                bounds.setLocation(getPoint(0));
                for (int i = 1; i < size; i++) {
                    bounds.union(getPoint(i));
                }
            }
        }
        return bounds;
    }

    public PrecisionPointList getCopy() {
        PrecisionPointList result = create();
        copyTo(result);
        return result;
    }

    /**
     * @param result
     */
    protected void copyTo(PrecisionPointList result) {
        result.setSize(size);
        for (int i = 0; i < size; i++) {
            result.setPoint(getPoint(i), i);
        }
        result.bounds = null;
    }

    protected PrecisionPointList create() {
        return new PrecisionPointList(size);
    }

    public PrecisionPoint getFirstPoint() {
        return getPoint(0);
    }

    public PrecisionPoint getLastPoint() {
        return getPoint(size - 1);
    }

    public PrecisionPoint getMidPoint() {
        if (size % 2 == 0) {
            return getPoint(size / 2 - 1).getCenter(getPoint(size / 2));
        }
        return getPoint(size / 2);
    }

    public PrecisionPoint getPoint(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size); //$NON-NLS-1$ //$NON-NLS-2$
        return points[index];
    }

    public void insertPoint(PrecisionPoint p, int index) {
        if (bounds != null && !bounds.contains(p))
            bounds = null;
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size); //$NON-NLS-1$ //$NON-NLS-2$
        int length = points.length;
        PrecisionPoint[] old = points;
        points = new PrecisionPoint[length + 1];
        System.arraycopy(old, 0, points, 0, index);
        System.arraycopy(old, index, points, index + 1, length - index);
        if (p == null) {
            points[index] = null;
        } else if (points[index] == null) {
            points[index] = new PrecisionPoint(p);
        } else {
            points[index].setLocation(p);
        }
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void scale(double amount) {
        for (PrecisionPoint p : points) {
            if (p != null) {
                p.scale(amount);
            }
        }
        bounds = null;
    }

    public void translate(double dx, double dy) {
        if (dx == 0 && dy == 0)
            return;
        for (PrecisionPoint p : points) {
            if (p != null) {
                p.translate(dx, dy);
            }
        }
        if (bounds != null)
            bounds.translate(dx, dy);
    }

    public void translate(PrecisionDimension d) {
        translate(d.width, d.height);
    }

    public void removeAllPoints() {
        bounds = null;
        size = 0;
    }

    public PrecisionPoint removePoint(int index) {
        bounds = null;
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size); //$NON-NLS-1$ //$NON-NLS-2$
        PrecisionPoint p = getPoint(index);
        if (index != size - 1) {
            System
                    .arraycopy(points, index + 1, points, index, size - index
                            - 1);
        }
        size--;
        return p;
    }

    public void reverse() {
        PrecisionPoint temp;
        for (int i = 0, j = size - 1; i < size; i++, j--) {
            temp = points[i];
            points[i] = points[j];
            points[j] = temp;
        }
    }

    public void setPoint(PrecisionPoint p, int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size); //$NON-NLS-1$ //$NON-NLS-2$
        if (bounds != null && !bounds.contains(p))
            bounds = null;
        if (p == null) {
            points[index] = null;
        } else if (points[index] == null) {
            points[index] = new PrecisionPoint(p);
        } else {
            points[index].setLocation(p);
        }
    }

    public void setSize(int newSize) {
        if (points.length > newSize) {
            size = newSize;
            return;
        }
        PrecisionPoint[] newArray = new PrecisionPoint[newSize];
        System.arraycopy(points, 0, newArray, 0, points.length);
        points = newArray;
        size = newSize;
    }

    public int size() {
        return size;
    }

    public PrecisionPoint[] toArray() {
        if (points.length != size) {
            PrecisionPoint[] old = points;
            points = new PrecisionPoint[size];
            System.arraycopy(old, 0, points, 0, size());
        }
        return points;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(size * 20 + 22);
        sb.append("PrecisionPointList:["); //$NON-NLS-1$
        for (int i = 0; i < size; i++) {
            PrecisionPoint p = getPoint(i);
            if (p != null)
                sb.append("(" + p.x + "," + p.y + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (i < size - 1)
                sb.append(","); //$NON-NLS-1$
        }
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

}