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
import org.xmind.gef.draw2d.IOriginBased2;

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
 * @author MANGOSOFT
 */
public interface ITransformer extends IOriginBased2 {

    Point t(Point p);

    Dimension t(Dimension d);

    Rectangle t(Rectangle r);

    Insets t(Insets i);

    Point r(Point p);

    Dimension r(Dimension d);

    Rectangle r(Rectangle r);

    Insets r(Insets i);

    Point tp(Point p);

    Point tp(int x, int y);

    Point tp(Point p, Point result);

    Point tp(int x, int y, Point result);

    Dimension td(Dimension d);

    Dimension td(int w, int h);

    Dimension td(Dimension d, Dimension result);

    Dimension td(int w, int h, Dimension result);

    Rectangle tr(Rectangle r);

    Rectangle tr(int x, int y, int w, int h);

    Rectangle tr(Rectangle r, Rectangle result);

    Rectangle tr(int x, int y, int w, int h, Rectangle result);

    Insets ti(Insets i);

    Insets ti(int t, int l, int b, int r);

    Insets ti(Insets i, Insets result);

    Insets ti(int t, int l, int b, int r, Insets result);

    Point rp(Point p);

    Point rp(int x, int y);

    Point rp(Point p, Point result);

    Point rp(int x, int y, Point result);

    Dimension rd(Dimension d);

    Dimension rd(int w, int h);

    Dimension rd(Dimension d, Dimension result);

    Dimension rd(int w, int h, Dimension result);

    Rectangle rr(Rectangle r);

    Rectangle rr(int x, int y, int w, int h);

    Rectangle rr(Rectangle r, Rectangle result);

    Rectangle rr(int x, int y, int w, int h, Rectangle result);

    Insets ri(Insets i);

    Insets ri(int t, int l, int b, int r);

    Insets ri(Insets i, Insets result);

    Insets ri(int t, int l, int b, int r, Insets result);

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

}