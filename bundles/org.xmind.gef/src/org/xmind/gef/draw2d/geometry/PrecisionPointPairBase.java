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
public abstract class PrecisionPointPairBase {

    protected PrecisionPoint point1 = new PrecisionPoint();

    protected PrecisionPoint point2 = new PrecisionPoint();

    public PrecisionPointPairBase() {
    }

    public PrecisionPointPairBase(PrecisionPoint point1, PrecisionPoint point2) {
        this(point1.x, point1.y, point2.x, point2.y);
    }

    public PrecisionPointPairBase(double x1, double y1, double x2, double y2) {
        this.point1.setLocation(x1, y1);
        this.point2.setLocation(x2, y2);
    }

    public PrecisionRectangle getBounds() {
        return new PrecisionRectangle(point1, point2);
    }

    public PrecisionPoint getCenter() {
        return point1.getCenter(point2);
    }

    public void translate(double dx, double dy) {
        translateFirstPoint(dx, dy);
        translateSecondPoint(dx, dy);
    }

    public void translate(double dx1, double dy1, double dx2, double dy2) {
        translateFirstPoint(dx1, dy1);
        translateSecondPoint(dx2, dy2);
    }

    public void translate(PrecisionDimension offset) {
        translate(offset.width, offset.height);
    }

    public void translate(PrecisionDimension originOffset,
            PrecisionDimension terminusOffset) {
        translate(originOffset.width, originOffset.height,
                terminusOffset.width, terminusOffset.height);
    }

    public void translateFirstPoint(double dx, double dy) {
        point1.translate(dx, dy);
    }

    public void translateFirstPoint(PrecisionDimension offset) {
        translateFirstPoint(offset.width, offset.height);
    }

    public void translateSecondPoint(double dx, double dy) {
        point2.translate(dx, dy);
    }

    public void translateSecondPoint(PrecisionDimension offset) {
        translateSecondPoint(offset.width, offset.height);
    }

    public PrecisionPointPairBase swap() {
        double x = point1.x;
        double y = point1.y;
        point1.setLocation(point2);
        point2.setLocation(x, y);
        return this;
    }

    public abstract PrecisionPointPairBase getCopy();

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PrecisionPointPair))
            return false;
        PrecisionPointPair pp = (PrecisionPointPair) obj;
        return point1.equals(pp.point1) && point2.equals(pp.point2);
    }

    public String toString() {
        return "[(" + point1.x + "," + point1.y + "), (" + point2.x + "," + point2.y + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    public int hashCode() {
        return point1.hashCode() ^ point2.hashCode();
    }

}