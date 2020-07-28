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
package org.xmind.gef.draw2d;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;

public class ReferencedLayoutData {

    private Point reference;

    private Map<Object, Rectangle> contents = new HashMap<Object, Rectangle>();

    private Rectangle clientArea = null;

    public ReferencedLayoutData() {
        this.reference = new Point();
    }

    public ReferencedLayoutData(Point reference) {
        this.reference = new Point(reference);
    }

    public Point getReference() {
        return reference;
    }

    public void put(IFigure figure, Rectangle preferredBounds) {
        if (figure == null || preferredBounds == null)
            return;
        contents.put(figure, preferredBounds);
        clientArea = Geometry.union(clientArea, preferredBounds);
    }

    public void add(Rectangle blankArea) {
        if (blankArea == null)
            return;
        clientArea = Geometry.union(clientArea, blankArea);
    }

    public Rectangle get(Object figure) {
        return contents.get(figure);
    }

    public void addMargins(Insets margin) {
        if (clientArea == null) {
            clientArea = Geometry.getExpanded(reference.x, reference.y, margin);
        } else {
            clientArea.expand(margin);
        }
    }

    public void addMargins(int top, int left, int bottom, int right) {
        if (clientArea == null) {
            clientArea = new Rectangle(reference.x - left, reference.y - top,
                    reference.x + left + right, reference.y + top + bottom);
        } else {
            clientArea.x -= left;
            clientArea.y -= top;
            clientArea.width += left + right;
            clientArea.height += top + bottom;
        }
    }

    public void translate(int dx, int dy) {
        for (Rectangle r : contents.values()) {
            r.translate(dx, dy);
        }
        if (clientArea != null) {
            clientArea.translate(dx, dy);
        }
    }

    public Rectangle getClientArea() {
        return clientArea;
    }

    public Rectangle getCheckedClientArea() {
        return clientArea == null ? createInitBounds() : clientArea;
    }

    public Rectangle createInitBounds(Point ref) {
        return new Rectangle(ref.x, ref.y, 0, 0);
    }

    public Rectangle createInitBounds() {
        return createInitBounds(reference);
    }

    public ReferencedLayoutData copy() {
        ReferencedLayoutData cloned = new ReferencedLayoutData();
        cloned.reference = this.reference == null ? null
                : new Point(this.reference);
        cloned.contents = this.contents == null ? null
                : new HashMap<Object, Rectangle>(this.contents);
        cloned.clientArea = this.clientArea == null ? null
                : new Rectangle(this.clientArea);
        return cloned;
    }

}