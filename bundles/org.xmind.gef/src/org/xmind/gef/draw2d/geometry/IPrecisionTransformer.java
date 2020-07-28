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
 * <ul>
 * <li><code>t()</code>: perform a sort of transformation and return the result
 * in the given geometrical object.</li>
 * <li><code>r()</code>: perform a sort of <b>reverse</b> transformation and
 * return the result in the given geometrical object.</li>
 * <li><code>t*()</code>: perform a sort of transformation and return the result
 * in the <code>result</code> object or a new object.</li>
 * <li><code>r*()</code>: perform a sort of <b>reverse</b> transformation and
 * return the result in the <code>result</code> object or a new object.</li>
 * </ul>
 * 
 * @author Frank Shaka
 */
public interface IPrecisionTransformer {

    PrecisionPoint t(PrecisionPoint p);

    PrecisionDimension t(PrecisionDimension d);

    PrecisionRectangle t(PrecisionRectangle r);

    PrecisionInsets t(PrecisionInsets i);

    PrecisionPoint r(PrecisionPoint p);

    PrecisionDimension r(PrecisionDimension d);

    PrecisionRectangle r(PrecisionRectangle r);

    PrecisionInsets r(PrecisionInsets i);

    PrecisionPoint tp(PrecisionPoint p);

    PrecisionPoint tp(double x, double y);

    PrecisionPoint tp(PrecisionPoint p, PrecisionPoint result);

    PrecisionPoint tp(double x, double y, PrecisionPoint result);

    PrecisionDimension td(PrecisionDimension d);

    PrecisionDimension td(double w, double h);

    PrecisionDimension td(PrecisionDimension d, PrecisionDimension result);

    PrecisionDimension td(double w, double h, PrecisionDimension result);

    PrecisionRectangle tr(PrecisionRectangle r);

    PrecisionRectangle tr(double x, double y, double w, double h);

    PrecisionRectangle tr(PrecisionRectangle r, PrecisionRectangle result);

    PrecisionRectangle tr(double x, double y, double w, double h,
            PrecisionRectangle result);

    PrecisionInsets ti(PrecisionInsets i);

    PrecisionInsets ti(double t, double l, double b, double r);

    PrecisionInsets ti(PrecisionInsets i, PrecisionInsets result);

    PrecisionInsets ti(double t, double l, double b, double r,
            PrecisionInsets result);

    PrecisionPoint rp(PrecisionPoint p);

    PrecisionPoint rp(double x, double y);

    PrecisionPoint rp(PrecisionPoint p, PrecisionPoint result);

    PrecisionPoint rp(double x, double y, PrecisionPoint result);

    PrecisionDimension rd(PrecisionDimension d);

    PrecisionDimension rd(double w, double h);

    PrecisionDimension rd(PrecisionDimension d, PrecisionDimension result);

    PrecisionDimension rd(double w, double h, PrecisionDimension result);

    PrecisionRectangle rr(PrecisionRectangle r);

    PrecisionRectangle rr(double x, double y, double w, double h);

    PrecisionRectangle rr(PrecisionRectangle r, PrecisionRectangle result);

    PrecisionRectangle rr(double x, double y, double w, double h,
            PrecisionRectangle result);

    PrecisionInsets ri(PrecisionInsets i);

    PrecisionInsets ri(double t, double l, double b, double r);

    PrecisionInsets ri(PrecisionInsets i, PrecisionInsets result);

    PrecisionInsets ri(double t, double l, double b, double r,
            PrecisionInsets result);

    PrecisionPoint getOrigin();

    void setOrigin(PrecisionPoint origin);

    void setOrigin(double x, double y);

    boolean isEnabled();

    void setEnabled(boolean enabled);

}