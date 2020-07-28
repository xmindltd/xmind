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
public class PrecisionHorizontalFlipper extends AbstractPrecisionTransformer {

    public PrecisionHorizontalFlipper() {
    }

    public PrecisionHorizontalFlipper(PrecisionPoint origin) {
        super(origin);
    }

    public PrecisionInsets t(PrecisionInsets i) {
        if (isEnabled()) {
            double temp = i.left;
            i.left = i.right;
            i.right = temp;
        }
        return i;
    }

    public PrecisionPoint t(PrecisionPoint p) {
        if (isEnabled()) {
            p.x = tx(p.x);
        }
        return p;
    }

    protected double tx(double x) {
        double ox = getOrigin().x;
        return ox + ox - x;
    }

    public PrecisionRectangle t(PrecisionRectangle r) {
        if (isEnabled()) {
            r.x = tx(r.right());
        }
        return r;
    }

}