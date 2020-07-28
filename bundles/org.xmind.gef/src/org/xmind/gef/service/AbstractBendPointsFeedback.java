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
package org.xmind.gef.service;

import static org.eclipse.draw2d.PositionConstants.EAST;
import static org.eclipse.draw2d.PositionConstants.NORTH;
import static org.eclipse.draw2d.PositionConstants.NORTH_EAST;
import static org.eclipse.draw2d.PositionConstants.NORTH_WEST;
import static org.eclipse.draw2d.PositionConstants.SOUTH;
import static org.eclipse.draw2d.PositionConstants.SOUTH_EAST;
import static org.eclipse.draw2d.PositionConstants.SOUTH_WEST;
import static org.eclipse.draw2d.PositionConstants.WEST;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.draw2d.geometry.Geometry;

public abstract class AbstractBendPointsFeedback extends AbstractFeedback
        implements IBendPointsFeedback {

    public static final int[] ALL_ORIENTATIONS = new int[] { SOUTH_EAST,
            SOUTH_WEST, NORTH_WEST, NORTH_EAST, EAST, WEST, SOUTH, NORTH };

    private Map<Integer, IReferencedFigure> figures = null;

    private Rectangle bounds = null;

    private IRectangleProvider boundsProvider = null;

    private int hidePointLength = 0;

    private int[] orientations = null;

    public void addToLayer(IFigure layer) {
        if (figures == null) {
            figures = createFigures();
        }
        for (IFigure f : figures.values()) {
            layer.add(f);
        }
    }

    private Map<Integer, IReferencedFigure> createFigures() {
        HashMap<Integer, IReferencedFigure> map = new HashMap<Integer, IReferencedFigure>(
                ALL_ORIENTATIONS.length);
        for (int orientation : ALL_ORIENTATIONS) {
            map.put(orientation, createPointFigure(orientation));
        }
        return map;
    }

    protected abstract IReferencedFigure createPointFigure(int orientation);

    public boolean containsPoint(Point point) {
        if (figures != null && bounds != null) {
            if (getZoomManager() != null)
                point = getZoomManager().getScaled(point);
            for (IFigure f : figures.values()) {
                if (f.containsPoint(point) && f.isVisible()
                        && f.getParent() != null)
                    return true;
            }
        }
        return false;
    }

    public void removeFromLayer(IFigure layer) {
        if (figures != null) {
            for (IFigure f : figures.values()) {
                layer.remove(f);
            }
        }
    }

    public void update() {
        if (figures == null)
            return;

        if (boundsProvider != null) {
            Rectangle r = boundsProvider.getRectangle();
            if (r != null)
                setBounds(r);
        }

        if (bounds != null) {
            Rectangle scaledBounds = bounds;
            if (getZoomManager() != null)
                scaledBounds = getZoomManager().getScaled(scaledBounds);
            for (int orientation : ALL_ORIENTATIONS) {
                IReferencedFigure fig = figures.get(orientation);
                if (fig != null) {
                    Point p = calcPointPosition(fig, orientation, scaledBounds);
                    preUpdatePointFigure(fig, orientation, scaledBounds, p);
                    fig.setReference(p);
                    fig.setVisible(isPointVisible(fig, orientation,
                            scaledBounds));
                }
            }
            updateWithBounds(scaledBounds);
        }

        for (int orientation : ALL_ORIENTATIONS) {
            IReferencedFigure fig = figures.get(orientation);
            if (fig != null) {
                updatePointFigure(fig, orientation);
            }
        }
    }

    protected void updateWithBounds(Rectangle clientBounds) {
    }

    protected void preUpdatePointFigure(IReferencedFigure figure,
            int orientation, Rectangle bounds, Point preferredPosition) {
    }

    protected abstract void updatePointFigure(IReferencedFigure figure,
            int orientation);

    protected boolean isPointVisible(IReferencedFigure figure, int orientation,
            Rectangle box) {
        for (int o : getOrientations()) {
            if (o == orientation) {
                int length = getLength(orientation, box);
                return length < 0 || length >= hidePointLength;
            }
        }
        return false;
    }

    protected int getLength(int orientation, Rectangle box) {
        if (orientation == EAST || orientation == WEST)
            return box.height;
        if (orientation == NORTH || orientation == SOUTH)
            return box.width;
        return -1;
    }

    protected Point calcPointPosition(IReferencedFigure figure,
            int orientation, Rectangle box) {
        return Geometry.getLocation(orientation, box, true, 1, 0);
    }

    public int getOrientation(Point point) {
        if (figures != null && bounds != null) {
            if (getZoomManager() != null)
                point = getZoomManager().getScaled(point);
            for (int orientation : ALL_ORIENTATIONS) {
                IFigure f = figures.get(orientation);
                if (f.containsPoint(point) && f.isVisible()
                        && f.getParent() != null)
                    return orientation;
            }
        }
        return PositionConstants.NONE;
    }

    public int[] getOrientations() {
        if (orientations == null)
            return ALL_ORIENTATIONS;
        return orientations;
    }

    public void setOrientations(int[] orientations) {
        this.orientations = orientations;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public int getHidePointLength() {
        return hidePointLength;
    }

    public void setHidePointLength(int hidePointLength) {
        this.hidePointLength = hidePointLength;
    }

    public IRectangleProvider getBoundsProvider() {
        return boundsProvider;
    }

    public void setBoundsProvider(IRectangleProvider boundsProvider) {
        this.boundsProvider = boundsProvider;
    }

}