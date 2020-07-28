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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class Transposer extends Transformer {

    public Transposer() {
        super();
    }

    public Transposer(Point origin) {
        super(origin);
    }

    public Dimension t(Dimension d) {
        if (isEnabled()) {
            d.transpose();
        }
        return d;
    }

    public Insets t(Insets i) {
        if (isEnabled()) {
            i.transpose();
        }
        return i;
//        return isEnabled() ? ins.getTransposed() : super.t(ins);
    }

    public Point t(Point p) {
        if (isEnabled()) {
            int o = getOrigin().x - getOrigin().y;
            int temp = p.y + o;
            p.y = p.x - o;
            p.x = temp;
        }
        return p;
//        return isEnabled() ? new Point(getOrigin().x + p.y - getOrigin().y,
//                getOrigin().y + p.x - getOrigin().x) : super.t(p);
    }

    public Rectangle t(Rectangle r) {
        if (isEnabled()) {
            int o = getOrigin().x - getOrigin().y;
            int temp = r.y + o;
            r.y = r.x - o;
            r.x = temp;
            temp = r.width;
            r.width = r.height;
            r.height = temp;
        }
        return r;
//        return isEnabled() ? new Rectangle(t(r.getLocation()), t(r.getSize()))
//                : super.t(r);
    }

//    public Rectangle r(Rectangle r) {
//        return isEnabled() ? new Rectangle(r(r.getLocation()), r(r.getSize()))
//                : super.r(r);
//    }
}