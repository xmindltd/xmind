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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Frank Shaka
 */
public class VerticalFlipper extends Transformer {

    public VerticalFlipper() {
        super();
    }

    public VerticalFlipper(Point origin) {
        super(origin);
    }

    public Insets t(Insets i) {
        if (isEnabled()) {
            int temp = i.top;
            i.top = i.bottom;
            i.bottom = temp;
        }
        return i;
    }

    public Point t(Point p) {
        if (isEnabled()) {
            p.y = ty(p.y);
        }
        return p;
    }

    protected int ty(int y) {
        int oy = getOrigin().y;
        return oy + oy - y;
    }

    public Rectangle t(Rectangle r) {
        if (isEnabled()) {
            r.y = ty(r.bottom());
        }
        return r;
    }

//    /**
//     * @see org.xmind.util.geometry.Transformer#tp(org.eclipse.draw2d.geometry.Point)
//     */
//    @Override
//    public Point t(Point p) {
//        return isEnabled() ? new Point(p.x, getOrigin().y * 2 - p.y) : super
//                .t(p);
//    }
//
//    /**
//     * @see org.xmind.util.geometry.Transformer#tp(org.eclipse.draw2d.geometry.Rectangle)
//     */
//    @Override
//    public Rectangle t(Rectangle r) {
//        return isEnabled() ? new Rectangle(t(r.getLocation()), t(r
//                .getBottomRight())).resize(-1, -1) : super.t(r);
//    }
//
//    /**
//     * @see org.xmind.util.geometry.Transformer#tp(org.eclipse.draw2d.geometry.Insets)
//     */
//    @Override
//    public Insets t(Insets ins) {
//        return isEnabled() ? new Insets(ins.bottom, ins.left, ins.top,
//                ins.right) : super.t(ins);
//    }
//
//    /**
//     * @see org.xmind.util.geometry.Transformer#rp(org.eclipse.draw2d.geometry.Rectangle)
//     */
//    @Override
//    public Rectangle r(Rectangle r) {
//        return isEnabled() ? new Rectangle(r(r.getLocation()), r(r
//                .getBottomRight())).resize(-1, -1) : super.r(r);
//    }
}