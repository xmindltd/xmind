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
public class HorizontalFlipper extends Transformer {

    public HorizontalFlipper() {
        super();
    }

    public HorizontalFlipper(Point origin) {
        super(origin);
    }

    public Insets t(Insets i) {
        if (isEnabled()) {
            int temp = i.left;
            i.left = i.right;
            i.right = temp;
        }
        return i;
    }

    public Point t(Point p) {
        if (isEnabled()) {
            p.x = tx(p.x);
        }
        return p;
    }

    protected int tx(int x) {
        int ox = getOrigin().x;
        return ox + ox - x;
    }

    public Rectangle t(Rectangle r) {
        if (isEnabled()) {
            r.x = tx(r.right());
        }
        return r;
    }

//    @Override
//    public Rectangle t(Rectangle r) {
//        return isEnabled() ? new Rectangle(t(r.getLocation()), t(r
//                .getBottomRight())).resize(-1, -1) : super.t(r);
//    }
//
//    @Override
//    public Point t(Point p) {
//        return isEnabled() ? new Point(getOrigin().x * 2 - p.x, p.y) : super
//                .t(p);
//    }
//
//    @Override
//    public Insets t(Insets ins) {
//        return isEnabled() ? new Insets(ins.top, ins.right, ins.bottom,
//                ins.left) : super.t(ins);
//    }
//
//    @Override
//    public Rectangle r(Rectangle r) {
//        return isEnabled() ? new Rectangle(r(r.getLocation()), r(r
//                .getBottomRight())).resize(-1, -1) : super.r(r);
//    }
}