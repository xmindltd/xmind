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

public class Transformer implements ITransformer {

    private Point origin = new Point();

    private boolean enabled = true;

    /**
     * 
     */
    public Transformer() {
    }

    /**
     * 
     * @param origin
     */
    public Transformer(Point origin) {
        this.origin.setLocation(origin);
    }

    /**
     * @return the origin
     */
    public Point getOrigin() {
        return origin;
    }

    /**
     * @see org.xmind.util.geometry.ITransformer#setOrigin(org.eclipse.draw2d.geometry.Point)
     */
    public void setOrigin(Point o) {
        this.origin.setLocation(o);
    }

    public void setOrigin(int x, int y) {
        this.origin.setLocation(x, y);
    }

    /**
     * @see org.xmind.util.geometry.ITransformer#isEnabled()
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @see org.xmind.util.geometry.ITransformer#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Dimension r(Dimension d) {
        return t(d);
    }

    public Insets r(Insets i) {
        return t(i);
    }

    public Point r(Point p) {
        return t(p);
    }

    public Rectangle r(Rectangle r) {
        return t(r);
    }

    public Dimension rd(int w, int h) {
        return r(new Dimension(w, h));
    }

    public Dimension rd(int w, int h, Dimension result) {
        result.width = w;
        result.height = h;
        return r(result);
    }

    public Dimension rd(Dimension d) {
        return r(new Dimension(d));
    }

    public Dimension rd(Dimension d, Dimension result) {
        result.setSize(d);
        return r(result);
    }

    public Insets ri(int t, int l, int b, int r) {
        return r(new Insets(t, l, b, r));
    }

    public Insets ri(int t, int l, int b, int r, Insets result) {
        result.top = t;
        result.left = l;
        result.bottom = b;
        result.right = r;
        return r(result);
    }

    public Insets ri(Insets i) {
        return r(new Insets(i));
    }

    public Insets ri(Insets i, Insets result) {
        result.top = i.top;
        result.left = i.left;
        result.bottom = i.bottom;
        result.right = i.right;
        return r(result);
    }

    public Point rp(int x, int y) {
        return r(new Point(x, y));
    }

    public Point rp(int x, int y, Point result) {
        return r(result.setLocation(x, y));
    }

    public Point rp(Point p) {
        return r(new Point(p));
    }

    public Point rp(Point p, Point result) {
        return r(result.setLocation(p));
    }

    public Rectangle rr(int x, int y, int w, int h) {
        return r(new Rectangle(x, y, w, h));
    }

    public Rectangle rr(int x, int y, int w, int h, Rectangle result) {
        result.setLocation(x, y);
        result.setSize(w, h);
        return r(result);
    }

    public Rectangle rr(Rectangle r) {
        return r(new Rectangle(r));
    }

    public Rectangle rr(Rectangle r, Rectangle result) {
        return r(result.setBounds(r));
    }

    public Dimension t(Dimension d) {
        return d;
    }

    public Insets t(Insets i) {
        return i;
    }

    public Point t(Point p) {
        return p;
    }

    public Rectangle t(Rectangle r) {
        return r;
    }

    public Dimension td(int w, int h) {
        return t(new Dimension(w, h));
    }

    public Dimension td(int w, int h, Dimension result) {
        result.width = w;
        result.height = h;
        return t(result);
    }

    public Dimension td(Dimension d) {
        return t(new Dimension(d));
    }

    public Dimension td(Dimension d, Dimension result) {
        result.setSize(d);
        return t(result);
    }

    public Insets ti(int t, int l, int b, int r) {
        return t(new Insets(t, l, b, r));
    }

    public Insets ti(int t, int l, int b, int r, Insets result) {
        result.top = t;
        result.left = l;
        result.bottom = b;
        result.right = r;
        return t(result);
    }

    public Insets ti(Insets i) {
        return t(new Insets(i));
    }

    public Insets ti(Insets i, Insets result) {
        result.top = i.top;
        result.left = i.left;
        result.bottom = i.bottom;
        result.right = i.right;
        return t(result);
    }

    public Point tp(int x, int y) {
        return t(new Point(x, y));
    }

    public Point tp(int x, int y, Point result) {
        return t(result.setLocation(x, y));
    }

    public Point tp(Point p) {
        return t(new Point(p));
    }

    public Point tp(Point p, Point result) {
        return t(result.setLocation(p));
    }

    public Rectangle tr(int x, int y, int w, int h) {
        return t(new Rectangle(x, y, w, h));
    }

    public Rectangle tr(int x, int y, int w, int h, Rectangle result) {
        result.setLocation(x, y);
        result.setSize(w, h);
        return t(result);
    }

    public Rectangle tr(Rectangle r) {
        return t(new Rectangle(r));
    }

    public Rectangle tr(Rectangle r, Rectangle result) {
        return t(result.setBounds(r));
    }

}