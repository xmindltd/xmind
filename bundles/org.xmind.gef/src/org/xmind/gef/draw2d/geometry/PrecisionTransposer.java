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

public class PrecisionTransposer extends AbstractPrecisionTransformer {

    public PrecisionTransposer() {
        super();
    }

    public PrecisionTransposer(PrecisionPoint origin) {
        super(origin);
    }

    public PrecisionDimension t(PrecisionDimension d) {
        if (isEnabled()) {
            d.transpose();
        }
        return super.t(d);
    }

    public PrecisionInsets t(PrecisionInsets i) {
        if (isEnabled()) {
            i.transpose();
        }
        return super.t(i);
    }

    public PrecisionPoint t(PrecisionPoint p) {
        if (isEnabled()) {
            double o = getOrigin().x - getOrigin().y;
            double x = p.y + o;
            p.y = p.x - o;
            p.x = x;
        }
        return super.t(p);
    }

    public PrecisionRectangle t(PrecisionRectangle r) {
        if (isEnabled()) {
            double o = getOrigin().x - getOrigin().y;
            double temp = r.y + o;
            r.y = r.x - o - r.width;
            r.x = temp - r.height;
            temp = r.width;
            r.width = r.height;
            r.height = temp;
        }
        return super.t(r);
    }

}