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

import java.io.Serializable;

import org.eclipse.draw2d.geometry.Dimension;

public class PrecisionDimension implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8002279770296919048L;

    /**
     * The width in double precision.
     */
    public double width;
    /**
     * The height in double precision.
     */
    public double height;

    /**
     * 
     */
    public PrecisionDimension() {
    }

    /**
     * @param d
     */
    public PrecisionDimension(Dimension d) {
        this.width = d.width;
        this.height = d.height;
    }

    /**
     * @param width
     * @param height
     */
    public PrecisionDimension(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public PrecisionDimension(PrecisionDimension d) {
        this.width = d.width;
        this.height = d.height;
    }

    public PrecisionDimension transpose() {
        double temp = width;
        width = height;
        height = temp;
        return this;
    }

    public PrecisionDimension negate() {
        width = 0 - width;
        height = 0 - height;
        return this;
    }

    public PrecisionDimension expand(double w, double h) {
        width += w;
        height += h;
        return this;
    }

    public PrecisionDimension union(PrecisionDimension d) {
        width = Math.max(width, d.width);
        height = Math.max(height, d.height);
        return this;
    }

    public Dimension toDraw2DDimension() {
        return new Dimension((int) Math.floor(width + 0.000000001),
                (int) Math.floor(height + 0.000000001));
    }

    public Dimension toBiggerDraw2DDimension() {
        return new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
    }

    public PrecisionDimension scale(double factor) {
        return scale(factor, factor);
    }

    public PrecisionDimension scale(double wScale, double hScale) {
        width *= wScale;
        height *= hScale;
        return this;
    }

    public PrecisionDimension getCopy() {
        return new PrecisionDimension(width, height);
    }

    public PrecisionDimension getScale(double factor) {
        return getCopy().scale(factor);
    }

    public PrecisionDimension getScale(double wScale, double hScale) {
        return getCopy().scale(wScale, hScale);
    }

    public double getDiagonal() {
        return Math.sqrt(getDiagonal2());
    }

    public double getDiagonal2() {
        return width * width + height * height;
    }

    public PrecisionDimension setSize(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PrecisionDimension setSize(PrecisionDimension d) {
        return setSize(d.width, d.height);
    }

    public String toString() {
        return "PrecisionDimensin(" + width + "," + height + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}