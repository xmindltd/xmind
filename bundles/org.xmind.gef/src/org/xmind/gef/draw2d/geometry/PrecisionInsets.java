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

import org.eclipse.draw2d.geometry.Insets;

/**
 * @author Frank Shaka
 */
public class PrecisionInsets {

    public double left;
    public double right;
    public double top;
    public double bottom;

    /**
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public PrecisionInsets(double top, double left, double bottom, double right) {
        super();
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public PrecisionInsets(Insets ins) {
        this(ins.top, ins.left, ins.bottom, ins.right);
    }

    public PrecisionInsets(PrecisionInsets ins) {
        this(ins.top, ins.left, ins.bottom, ins.right);
    }

    public PrecisionInsets(double allside) {
        this(allside, allside, allside, allside);
    }

    public PrecisionInsets() {
        this(0);
    }

    public Insets toDraw2DInsets() {
        return toDraw2DInsets(top, left, bottom, right);
    }

    public static Insets toDraw2DInsets(double top, double left, double bottom,
            double right) {
        return new Insets((int) Math.floor(top + 0.000000001), (int) Math
                .floor(left + 0.000000001), (int) Math
                .floor(bottom + 0.000000001), (int) Math
                .floor(right + 0.000000001));
    }

    public Insets getBiggerDraw2DInsets() {
        return toBiggerDraw2DInsets(top, left, bottom, right);
    }

    public Insets toBiggerDraw2DInsets(double top, double left, double bottom,
            double right) {
        return new Insets((int) Math.ceil(top), (int) Math.ceil(left),
                (int) Math.ceil(bottom), (int) Math.ceil(right));
    }

    public PrecisionInsets add(PrecisionInsets ins) {
        return add(ins.top, ins.left, ins.bottom, ins.right);
    }

    public PrecisionInsets add(Insets ins) {
        return add(ins.top, ins.left, ins.bottom, ins.right);
    }

    public PrecisionInsets add(double allSide) {
        return add(allSide, allSide, allSide, allSide);
    }

    public PrecisionInsets add(double t, double l, double b, double r) {
        this.top += t;
        this.left += l;
        this.bottom += b;
        this.right += r;
        return this;
    }

    public PrecisionInsets getAdded(PrecisionInsets ins) {
        return new PrecisionInsets(this).add(ins);
    }

    public PrecisionInsets getAdded(double t, double l, double b, double r) {
        return new PrecisionInsets(this).add(t, l, b, r);
    }

    public PrecisionInsets getAdded(double allSide) {
        return new PrecisionInsets(this).add(allSide);
    }

    public PrecisionInsets remove(PrecisionInsets ins) {
        this.top -= ins.top;
        this.left -= ins.left;
        this.bottom -= ins.bottom;
        this.right -= ins.right;
        return this;
    }

    public PrecisionInsets getRemoved(PrecisionInsets ins) {
        return new PrecisionInsets(this).remove(ins);
    }

    public PrecisionInsets negate() {
        top = -top;
        left = -left;
        bottom = -bottom;
        right = -right;
        return this;
    }

    public PrecisionInsets getNegated() {
        return new PrecisionInsets(this).negate();
    }

    /**
     * Returns true if all values are 0.
     * 
     * @return true if all values are 0
     * @since 2.0
     */
    public boolean isEmpty() {
        return (left == 0 && right == 0 && top == 0 && bottom == 0);
    }

    public double getWidth() {
        return left + right;
    }

    public double getHeight() {
        return top + bottom;
    }

    public PrecisionRectangle getBounds(PrecisionPoint p) {
        return new PrecisionRectangle(p.x - left, p.y - top, getWidth(),
                getHeight());
    }

    public PrecisionDimension getSize() {
        return new PrecisionDimension(getWidth(), getHeight());
    }

    public PrecisionInsets setInsets(double top, double left, double bottom,
            double right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        return this;
    }

    public PrecisionInsets setInsets(PrecisionInsets insets) {
        return setInsets(insets.top, insets.left, insets.bottom, insets.right);
    }

    /**
     * Transposes this object. Top and Left are exchanged. Bottom and Right are
     * exchanged. Can be used in orientation changes.
     * 
     * @return <code>this</code> for convenience
     * @since 2.0
     */
    public PrecisionInsets transpose() {
        double temp = top;
        top = left;
        left = temp;
        temp = right;
        right = bottom;
        bottom = temp;
        return this;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PrecisionInsets))
            return false;
        PrecisionInsets ins = (PrecisionInsets) obj;
        return ins.top == top && ins.left == left && ins.bottom == bottom
                && ins.right == right;
    }

    public String toString() {
        return "PrecisionInsets(t=" + top + ",l=" + left + //$NON-NLS-2$//$NON-NLS-1$
                ",b=" + bottom + ",r=" + right + ")";//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
    }
}