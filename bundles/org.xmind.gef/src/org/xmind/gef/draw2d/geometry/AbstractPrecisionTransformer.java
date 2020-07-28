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
public abstract class AbstractPrecisionTransformer implements
        IPrecisionTransformer {

    private PrecisionPoint origin = new PrecisionPoint();

    private boolean enabled = true;

    public AbstractPrecisionTransformer() {
    }

    /**
     * 
     */
    public AbstractPrecisionTransformer(PrecisionPoint origin) {
        this.origin.setLocation(origin);
    }

    /**
     * @see org.xmind.util.geometry.IPrecisionTransformer#getOrigin()
     */
    public PrecisionPoint getOrigin() {
        return origin;
    }

    /**
     * @see org.xmind.util.geometry.IPrecisionTransformer#isEnabled()
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @see org.xmind.util.geometry.IPrecisionTransformer#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOrigin(double x, double y) {
        this.origin.setLocation(x, y);
    }

    /**
     * @see org.xmind.util.geometry.IPrecisionTransformer#setOrigin(org.xmind.util.geometry.PrecisionPoint)
     */
    public void setOrigin(PrecisionPoint origin) {
        setOrigin(origin.x, origin.y);
    }

    public PrecisionDimension r(PrecisionDimension d) {
        return t(d);
    }

    public PrecisionInsets r(PrecisionInsets i) {
        return t(i);
    }

    public PrecisionPoint r(PrecisionPoint p) {
        return t(p);
    }

    public PrecisionRectangle r(PrecisionRectangle r) {
        return t(r);
    }

    public PrecisionDimension rd(double w, double h) {
        return r(new PrecisionDimension(w, h));
    }

    public PrecisionDimension rd(double w, double h, PrecisionDimension result) {
        return r(result.setSize(w, h));
    }

    public PrecisionDimension rd(PrecisionDimension d) {
        return r(new PrecisionDimension(d));
    }

    public PrecisionDimension rd(PrecisionDimension d, PrecisionDimension result) {
        return r(result.setSize(d));
    }

    public PrecisionInsets ri(double t, double l, double b, double r) {
        return r(new PrecisionInsets(t, l, b, r));
    }

    public PrecisionInsets ri(double t, double l, double b, double r,
            PrecisionInsets result) {
        return r(result.setInsets(t, l, b, r));
    }

    public PrecisionInsets ri(PrecisionInsets i) {
        return r(new PrecisionInsets(i));
    }

    public PrecisionInsets ri(PrecisionInsets i, PrecisionInsets result) {
        return r(result.setInsets(i));
    }

    public PrecisionPoint rp(double x, double y) {
        return r(new PrecisionPoint(x, y));
    }

    public PrecisionPoint rp(double x, double y, PrecisionPoint result) {
        return r(result.setLocation(x, y));
    }

    public PrecisionPoint rp(PrecisionPoint p) {
        return r(new PrecisionPoint(p));
    }

    public PrecisionPoint rp(PrecisionPoint p, PrecisionPoint result) {
        return r(result.setLocation(p));
    }

    public PrecisionRectangle rr(double x, double y, double w, double h) {
        return r(new PrecisionRectangle(x, y, w, h));
    }

    public PrecisionRectangle rr(double x, double y, double w, double h,
            PrecisionRectangle result) {
        return r(result.setBounds(x, y, w, h));
    }

    public PrecisionRectangle rr(PrecisionRectangle r) {
        return r(new PrecisionRectangle(r));
    }

    public PrecisionRectangle rr(PrecisionRectangle r, PrecisionRectangle result) {
        return r(result.setBounds(r));
    }

    public PrecisionDimension t(PrecisionDimension d) {
        return d;
    }

    public PrecisionInsets t(PrecisionInsets i) {
        return i;
    }

    public PrecisionPoint t(PrecisionPoint p) {
        return p;
    }

    public PrecisionRectangle t(PrecisionRectangle r) {
        return r;
    }

    public PrecisionDimension td(double w, double h) {
        return t(new PrecisionDimension(w, h));
    }

    public PrecisionDimension td(double w, double h, PrecisionDimension result) {
        return t(result.setSize(w, h));
    }

    public PrecisionDimension td(PrecisionDimension d) {
        return t(new PrecisionDimension(d));
    }

    public PrecisionDimension td(PrecisionDimension d, PrecisionDimension result) {
        return t(result.setSize(d));
    }

    public PrecisionInsets ti(double t, double l, double b, double r) {
        return t(new PrecisionInsets(t, l, b, r));
    }

    public PrecisionInsets ti(double t, double l, double b, double r,
            PrecisionInsets result) {
        return t(result.setInsets(t, l, b, r));
    }

    public PrecisionInsets ti(PrecisionInsets i) {
        return t(new PrecisionInsets(i));
    }

    public PrecisionInsets ti(PrecisionInsets i, PrecisionInsets result) {
        return t(result.setInsets(i));
    }

    public PrecisionPoint tp(double x, double y) {
        return t(new PrecisionPoint(x, y));
    }

    public PrecisionPoint tp(double x, double y, PrecisionPoint result) {
        return t(result.setLocation(x, y));
    }

    public PrecisionPoint tp(PrecisionPoint p) {
        return t(new PrecisionPoint(p));
    }

    public PrecisionPoint tp(PrecisionPoint p, PrecisionPoint result) {
        return t(result.setLocation(p));
    }

    public PrecisionRectangle tr(double x, double y, double w, double h) {
        return t(new PrecisionRectangle(x, y, w, h));
    }

    public PrecisionRectangle tr(double x, double y, double w, double h,
            PrecisionRectangle result) {
        return t(result.setBounds(x, y, w, h));
    }

    public PrecisionRectangle tr(PrecisionRectangle r) {
        return t(new PrecisionRectangle(r));
    }

    public PrecisionRectangle tr(PrecisionRectangle r, PrecisionRectangle result) {
        return t(result.setBounds(r));
    }

}