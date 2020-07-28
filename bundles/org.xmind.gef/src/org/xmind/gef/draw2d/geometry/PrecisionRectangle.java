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

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 */
public class PrecisionRectangle {

    /** Double value for height */
    public double height;

    /** Double value for width */
    public double width;

    /** Double value for X */
    public double x;

    /** Double value for Y */
    public double y;

    /**
     * 
     */
    public PrecisionRectangle() {
        this(0, 0, 0, 0);
    }

    public PrecisionRectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public PrecisionRectangle(PrecisionRectangle rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Constructs a new PrecisionRectangle from the given integer Rectangle.
     * 
     * @param rect
     *            the base rectangle
     */
    public PrecisionRectangle(Rectangle rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    public PrecisionRectangle(PrecisionPoint loc, PrecisionDimension size) {
        this(loc.x, loc.y, size.width, size.height);
    }

    public PrecisionRectangle(PrecisionPoint p1, PrecisionPoint p2) {
        x = Math.min(p1.x, p2.x);
        y = Math.min(p1.y, p2.y);
        width = Math.abs(p1.x - p2.x);
        height = Math.abs(p1.y - p2.y);
    }

    public boolean contains(PrecisionPoint p) {
        return contains(p.x, p.y);
    }

    public boolean contains(PrecisionRectangle r) {
        return this.x <= r.x && this.y <= r.y && right() >= r.right()
                && bottom() >= r.bottom();
    }

    public boolean contains(double x, double y) {
        return y >= this.y && y < this.y + this.height && x >= this.x
                && x < this.x + this.width;
    }

    public PrecisionRectangle intersect(PrecisionRectangle rect) {
        double x1 = Math.max(x, rect.x);
        double x2 = Math.min(x + width, rect.x + rect.width);
        double y1 = Math.max(y, rect.y);
        double y2 = Math.min(y + height, rect.y + rect.height);
        if (((x2 - x1) < 0) || ((y2 - y1) < 0))
            return setBounds(0, 0, 0, 0); // no intersection
        else {
            return setBounds(x1, y1, x2 - x1, y2 - y1);
        }
    }

    public boolean intersects(Rectangle rect) {
        return intersects(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean intersects(PrecisionRectangle rect) {
        return intersects(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean intersects(double x, double y, double width, double height) {
        return x < this.x + this.width && y < this.y + this.height
                && x + width > this.x && y + height > this.y;
    }

    public PrecisionRectangle crop(PrecisionInsets insets) {
        x += insets.left;
        y += insets.top;
        width -= insets.getWidth();
        height -= insets.getHeight();
        return this;
    }

    public PrecisionRectangle expand(double h, double v) {
        return shrink(-h, -v);
    }

    public PrecisionRectangle expand(PrecisionInsets insets) {
        x -= insets.left;
        y -= insets.top;
        height += insets.getHeight();
        width += insets.getWidth();
        return this;
    }

    public PrecisionRectangle getExpanded(double h, double v) {
        return new PrecisionRectangle(this).expand(h, v);
    }

    public PrecisionRectangle getExpanded(PrecisionInsets insets) {
        return new PrecisionRectangle(this).expand(insets);
    }

    public PrecisionRectangle shrink(double h, double v) {
        x += h;
        width -= (h + h);
        y += v;
        height -= (v + v);
        return this;
    }

    /**
     * @see Rectangle#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof PrecisionRectangle) {
            PrecisionRectangle pr = (PrecisionRectangle) o;
            return super.equals(o) && Math.abs(pr.x - x) < 0.000000001
                    && Math.abs(pr.y - y) < 0.000000001
                    && Math.abs(pr.width - width) < 0.000000001
                    && Math.abs(pr.height - height) < 0.00000001;
        }
        return super.equals(o);
    }

    public PrecisionRectangle scale(double factor) {
        x *= factor;
        y *= factor;
        width *= factor;
        height *= factor;
        return this;
    }

    /**
     * @see org.eclipse.draw2d.geometry.Rectangle#performTranslate(int, int)
     */
    public PrecisionRectangle translate(double dx, double dy) {
        x += dx;
        y += dy;
        return this;
    }

    public PrecisionRectangle translate(PrecisionDimension d) {
        return translate(d.width, d.height);
    }

    public PrecisionRectangle translate(PrecisionPoint p) {
        return translate(p.x, p.y);

    }

    public double right() {
        return x + width;
    }

    public double bottom() {
        return y + height;
    }

    public PrecisionPoint getLocation() {
        return new PrecisionPoint(x, y);
    }

    public PrecisionPoint getTopLeft() {
        return new PrecisionPoint(x, y);
    }

    public PrecisionPoint getTopRight() {
        return new PrecisionPoint(x + width, y);
    }

    public PrecisionPoint getBottomLeft() {
        return new PrecisionPoint(x, y + height);
    }

    public PrecisionPoint getBottomRight() {
        return new PrecisionPoint(x + width, y + height);
    }

    public PrecisionDimension getSize() {
        return new PrecisionDimension(width, height);
    }

    public PrecisionPoint getCenter() {
        return new PrecisionPoint(x + width / 2, y + height / 2);
    }

    public Rectangle toDraw2DRectangle() {
        return toDraw2DRectangle(x, y, width, height);
    }

    public static Rectangle toDraw2DRectangle(double x, double y, double width,
            double height) {
        int intX = (int) Math.floor(x + 0.000000001);
        int intY = (int) Math.floor(y + 0.000000001);
        int intWidth = (int) Math.floor(width + x + 0.000000001) - intX;
        int intHeight = (int) Math.floor(height + y + 0.000000001) - intY;
        return new Rectangle(intX, intY, intWidth, intHeight);
    }

    public Rectangle getInnerBounds() {
        return getInnerBounds(x, y, width, height);
    }

    public Rectangle getOuterBounds() {
        return getOuterBounds(x, y, width, height);
    }

    public static Rectangle getInnerBounds(double x, double y, double width,
            double height) {
        int intX = (int) Math.ceil(x);
        int intY = (int) Math.ceil(y);
        int intWidth = (int) Math.floor(width + x) - intX;
        int intHeight = (int) Math.floor(height + y) - intY;
        return new Rectangle(intX, intY, intWidth, intHeight);
    }

    public static Rectangle getOuterBounds(double x, double y, double width,
            double height) {
        int intX = (int) Math.floor(x);
        int intY = (int) Math.floor(y);
        int intWidth = (int) Math.ceil(width + x) - intX;
        int intHeight = (int) Math.ceil(height + y) - intY;
        return new Rectangle(intX, intY, intWidth, intHeight);
    }

    public PrecisionRectangle setSize(PrecisionDimension d) {
        return setSize(d.width, d.height);
    }

    public PrecisionRectangle setSize(double w, double h) {
        this.width = w;
        this.height = h;
        return this;
    }

    public PrecisionRectangle setLocation(PrecisionPoint p) {
        return setLocation(p.x, p.y);
    }

    public PrecisionRectangle setLocation(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public PrecisionRectangle setBounds(PrecisionRectangle r) {
        return setBounds(r.x, r.y, r.width, r.height);
    }

    public PrecisionRectangle setBounds(Rectangle r) {
        return setBounds(r.x, r.y, r.width, r.height);
    }

    public PrecisionRectangle setBounds(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        return this;
    }

    public PrecisionInsets getInsets(double x, double y) {
        return new PrecisionInsets(y - this.y, x - this.x, bottom() - y,
                right() - x);
    }

    public PrecisionInsets getInsets(PrecisionPoint p) {
        return getInsets(p.x, p.y);
    }

    public PrecisionRectangle union(PrecisionPoint p) {
        if (p == null)
            return this;
        return union(p.x, p.y);
    }

    public PrecisionRectangle union(double x1, double y1) {
        if (x1 < x) {
            width += (x - x1);
            x = x1;
        } else {
            double right = x + width;
            if (x1 >= right) {
                right = x1;
                width = right - x;
            }
        }
        if (y1 < y) {
            height += (y - y1);
            y = y1;
        } else {
            double bottom = y + height;
            if (y1 >= bottom) {
                bottom = y1;
                height = bottom - y;
            }
        }
        return this;
    }

    public PrecisionRectangle union(PrecisionRectangle r) {
        if (r == null)
            return this;
        return union(r.x, r.y, r.width, r.height);
    }

    public PrecisionRectangle union(double x, double y, double w, double h) {
        double right = Math.max(this.x + this.width, x + w);
        double bottom = Math.max(this.y + this.height, y + h);
        this.x = Math.min(this.x, x);
        this.y = Math.min(this.y, y);
        this.width = right - this.x;
        this.height = bottom - this.y;
        return this;
    }

    public PrecisionRectangle resize(double w, double h) {
        this.width += w;
        this.height += h;
        return this;
    }

    public PrecisionRectangle getCopy() {
        return new PrecisionRectangle(this.x, this.y, this.width, this.height);
    }

    public PrecisionRectangle getResize(double w, double h) {
        return getCopy().resize(w, h);
    }

    public String toString() {
        return "PrecisionRectangle(" + x + "," + y + "," + width + "," + height + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

}