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

import static java.lang.Math.abs;

/**
 * @author Frank Shaka
 */
public class PrecisionRotator extends AbstractPrecisionTransformer {

    private static final PrecisionPoint P = new PrecisionPoint();

    private static final PrecisionDimension D = new PrecisionDimension();

    private static final PrecisionRectangle R = new PrecisionRectangle();

    private double angle = 0;

    private double sin = 0d;

    private double cos = 1.0d;

    double cos2a = 1.0d;

    public PrecisionRotator() {
    }

    public PrecisionRotator(PrecisionPoint origin) {
        setOrigin(origin);
    }

    public double cos() {
        return cos;
    }

    /**
     * Returns the angle.
     * <ul>
     * <li>Represented in degrees;</li>
     * <li>0 degrees means horizontal rightwards;</li>
     * <li>Positive value means clock-wise.</li>
     * </ul>
     * 
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Returns the angle.
     * <ul>
     * <li>Represented in degrees;</li>
     * <li>Zero degrees means horizontal rightwards;</li>
     * <li>Positive value means clock-wise.</li>
     * </ul>
     * 
     * @param angle
     */
    public void setAngle(double angle) {
        this.angle = angle;
        double rad = Math.toRadians(angle);
        sin = Math.sin(rad);
        cos = Math.cos(rad);
        cos2a = Math.cos(rad * 2);
    }

    public double sin() {
        return sin;
    }

    public PrecisionDimension t(PrecisionDimension d) {
        if (isEnabled()) {
            double width = tWidth(d.width, d.height);
            d.height = tHeight(d.width, d.height);
            d.width = width;
        }
        return d;
    }

    public double tWidth(double w, double h) {
        return abs(w * cos) + abs(h * sin);
    }

    public double tHeight(double w, double h) {
        return abs(w * sin) + abs(h * cos);
    }

    public PrecisionInsets t(PrecisionInsets i) {
        if (isEnabled()) {
            double x0 = getOrigin().x;
            double y0 = getOrigin().y;
            PrecisionRectangle r = t(R.setBounds(x0 - i.left, y0 - i.top, i
                    .getWidth(), i.getHeight()));
            i.top = y0 - r.y;
            i.left = x0 - r.x;
            i.bottom = r.bottom() - y0;
            i.right = r.right() - x0;
        }
        return i;
    }

    public PrecisionPoint t(PrecisionPoint p) {
        if (isEnabled()) {
            double ox = getOrigin().x;
            double oy = getOrigin().y;
            double dx = p.x - ox;
            double dy = p.y - oy;
            p.x = ox + dx * cos - dy * sin;
            p.y = oy + dx * sin + dy * cos;
        }
        return p;
    }

    public PrecisionRectangle t(PrecisionRectangle r) {
        if (isEnabled()) {
            PrecisionPoint c = t(P.setLocation(r.x + r.width * 0.5d, r.y
                    + r.height * 0.5d));
            PrecisionDimension d = t(D.setSize(r.width, r.height));
            r.x = c.x - d.width / 2;
            r.y = c.y - d.height / 2;
            r.width = d.width;
            r.height = d.height;
        }
        return r;
    }

    public PrecisionPolygon tPolygon(PrecisionRectangle r) {
        PrecisionPolygon polygon = PrecisionPolygon.createFromRect(r);
        for (int i = 0; i < polygon.size(); i++) {
            polygon.setPoint(t(polygon.getPoint(i)), i);
        }
        return polygon;
    }

    public PrecisionDimension r(PrecisionDimension d) {
        return r(d, -1, -1);
    }

    public PrecisionDimension r(PrecisionDimension d, double wHint, double hHint) {
        if (isEnabled()) {
            double w, h;
            if (cos2a == 0) {
                h = hHint;
                w = wHint;
                if (d.width == d.height) {
                    double diag = abs(d.width / sin);
                    if (hHint > 0 && wHint < 0) {
                        w = diag - h;
                    } else if (wHint > 0 && hHint < 0) {
                        h = diag - w;
                    } else if (wHint < 0 && hHint < 0) {
                        w = h = diag / 2;
                    }
                }
            } else {
                w = (d.width * abs(cos) - d.height * abs(sin)) / cos2a;
                h = (d.height * abs(cos) - d.width * abs(sin)) / cos2a;
            }
            d.width = w;
            d.height = h;
        }
        return d;
    }

    public PrecisionInsets r(PrecisionInsets i) {
        return r(i, -1, -1);
    }

    public PrecisionInsets r(PrecisionInsets i, double wHint, double hHint) {
        if (isEnabled()) {
            double ox = getOrigin().x;
            double oy = getOrigin().y;
            PrecisionRectangle r = r(R.setBounds(ox - i.left, oy - i.top, i
                    .getWidth(), i.getHeight()), wHint, hHint);
            i.top = oy - r.y;
            i.left = ox - r.x;
            i.bottom = r.bottom() - oy;
            i.right = r.right() - ox;
        }
        return i;
    }

    public PrecisionPoint r(PrecisionPoint p) {
        if (isEnabled()) {
            double ox = getOrigin().x;
            double oy = getOrigin().y;
            double dx = p.x - ox;
            double dy = p.y - oy;
            p.x = ox + dx * cos + dy * sin;
            p.y = oy - dx * sin + dy * cos;
        }
        return p;
    }

    public PrecisionRectangle r(PrecisionRectangle r) {
        return r(r, -1, -1);
    }

    public PrecisionRectangle r(PrecisionRectangle r, double wHint, double hHint) {
        if (isEnabled()) {
            PrecisionPoint c = r(P.setLocation(r.x + r.width * 0.5d, r.y
                    + r.height * 0.5d));
            PrecisionDimension d = r(D.setSize(r.width, r.height), wHint, hHint);
            r.x = c.x - d.width / 2;
            r.y = c.y - d.height / 2;
            r.width = d.width;
            r.height = d.height;
        }
        return r;
    }

    public PrecisionPoint rp(PrecisionPoint p) {
        if (!isEnabled())
            return new PrecisionPoint(p);
        setAngle(-getAngle());
        PrecisionPoint p2 = tp(p);
        setAngle(-getAngle());
        return p2;
    }

}