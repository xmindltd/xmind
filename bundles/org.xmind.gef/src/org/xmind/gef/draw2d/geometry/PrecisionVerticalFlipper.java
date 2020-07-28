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
public class PrecisionVerticalFlipper extends AbstractPrecisionTransformer {

    public PrecisionVerticalFlipper() {
    }

    public PrecisionVerticalFlipper(PrecisionPoint origin) {
        super(origin);
    }

    public PrecisionInsets t(PrecisionInsets i) {
        if (isEnabled()) {
            double temp = i.top;
            i.top = i.bottom;
            i.bottom = temp;
        }
        return i;
    }

    public PrecisionPoint t(PrecisionPoint p) {
        if (isEnabled()) {
            p.y = ty(p.y);
        }
        return p;
    }

    protected double ty(double y) {
        double oy = getOrigin().y;
        return oy + oy - y;
    }

    public PrecisionRectangle t(PrecisionRectangle r) {
        if (isEnabled()) {
            r.y = ty(r.bottom());
        }
        return r;
    }

//    public PrecisionRectangle rr(PrecisionRectangle r) {
//        return isEnabled() ? new PrecisionRectangle(rp(r.getTopLeft()), rp(r
//                .getBottomRight())) : super.rr(r);
//    }
//
//    public PrecisionInsets ti(PrecisionInsets i) {
//        return isEnabled() ? new PrecisionInsets(i.bottom, i.left, i.top,
//                i.right) : super.ti(i);
//    }
//
//    public PrecisionPoint tp(PrecisionPoint p) {
//        return isEnabled() ? new PrecisionPoint(p.x, getOrigin().y - p.y)
//                : super.tp(p);
//    }
//
//    public PrecisionRectangle tr(PrecisionRectangle r) {
//        return isEnabled() ? new PrecisionRectangle(tp(r.getTopLeft()), tp(r
//                .getBottomRight())) : super.tr(r);
//    }

}