/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.draw2d;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.FreeformListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.PrecisionPoint;

public abstract class AbstractAnchor implements IAnchor, FigureListener,
        FreeformListener, PropertyChangeListener {

    private List<IAnchorListener> listeners = null;

    private IFigure owner;

    private Set<String> propertiesToMove = null;

    protected AbstractAnchor() {
    }

    public AbstractAnchor(IFigure owner) {
        setOwner(owner);
    }

    protected void setOwner(IFigure owner) {
        if (owner == this.owner)
            return;

        if (getOwner() != null && hasAnchorListener()) {
            unhookOwner(getOwner());
        }
        this.owner = owner;
        if (getOwner() != null && hasAnchorListener()) {
            hookOwner(getOwner());
        }
    }

    protected void hookOwner(IFigure owner) {
        owner.addFigureListener(this);
        if (owner instanceof FreeformFigure) {
            ((FreeformFigure) owner).addFreeformListener(this);
        }
        if (propertiesToMove != null) {
            for (String property : propertiesToMove) {
                owner.addPropertyChangeListener(property, this);
            }
        }
    }

    protected void unhookOwner(IFigure owner) {
        owner.removeFigureListener(this);
        if (owner instanceof FreeformFigure) {
            ((FreeformFigure) owner).removeFreeformListener(this);
        }
        if (propertiesToMove != null) {
            for (String property : propertiesToMove) {
                owner.removePropertyChangeListener(property, this);
            }
        }
    }

    public PrecisionPoint getLocation(int orientation, double expansion) {
        switch (orientation) {
        case PositionConstants.WEST:
            return getWest(expansion);
        case PositionConstants.SOUTH:
            return getSouth(expansion);
        case PositionConstants.NORTH:
            return getNorth(expansion);
        }
        return getEast(expansion);
    }

    protected PrecisionPoint getEast(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x + 100, ref.y, expansion);
    }

    protected PrecisionPoint getNorth(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x, ref.y - 100, expansion);
    }

    protected PrecisionPoint getSouth(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x, ref.y + 100, expansion);
    }

    protected PrecisionPoint getWest(double expansion) {
        PrecisionPoint ref = getReferencePoint();
        return getLocation(ref.x - 100, ref.y, expansion);
    }

    public PrecisionPoint getLocation(PrecisionPoint reference, double expansion) {
        return getLocation(reference.x, reference.y, expansion);
    }

    public void addAnchorListener(IAnchorListener listener) {
        boolean hadListener = hasAnchorListener();
        if (listeners == null)
            listeners = new ArrayList<IAnchorListener>();
        listeners.add(listener);
        if (!hadListener && hasAnchorListener() && getOwner() != null) {
            hookOwner(getOwner());
        }
    }

    public IFigure getOwner() {
        return owner;
    }

    public PrecisionPoint getReferencePoint() {
        if (getOwner() == null)
            return new PrecisionPoint();
        if (getOwner() instanceof IReferencedFigure) {
            return new PrecisionPoint(((IReferencedFigure) getOwner())
                    .getReference());
        }
        Rectangle r = getOwner().getBounds();
        return new PrecisionPoint(r.x + r.width * 0.5, r.y + r.height * 0.5);
    }

    public void removeAnchorListener(IAnchorListener listener) {
        if (listeners == null)
            return;
        boolean hadListener = hasAnchorListener();
        listeners.remove(listener);
        if (hadListener && !hasAnchorListener() && getOwner() != null) {
            unhookOwner(getOwner());
        }
    }

    public void figureMoved(IFigure source) {
        fireAnchorMoved();
    }

    public void notifyFreeformExtentChanged() {
        fireAnchorMoved();
    }

    protected void fireAnchorMoved() {
        if (listeners == null)
            return;
        for (Object listener : listeners.toArray()) {
            ((IAnchorListener) listener).anchorMoved(this);
        }
    }

    protected boolean hasAnchorListener() {
        return listeners != null && !listeners.isEmpty();
    }

    public void addAnchorMoveProperty(String... properties) {
        for (String property : properties) {
            addMoveProperty(property);
        }
        fireAnchorMoved();
    }

    /**
     * @param property
     */
    private void addMoveProperty(String property) {
        if (propertiesToMove == null) {
            propertiesToMove = new HashSet<String>();
        }
        if (!propertiesToMove.contains(property)) {
            if (getOwner() != null && hasAnchorListener()) {
                getOwner().addPropertyChangeListener(property, this);
            }
            propertiesToMove.add(property);
        }
    }

    public void removeAnchorMoveProperty(String... properties) {
        if (propertiesToMove == null)
            return;
        for (String property : properties) {
            removeMoveProperty(property);
        }
        fireAnchorMoved();
    }

    /**
     * @param property
     */
    private void removeMoveProperty(String property) {
        if (getOwner() != null) {
            getOwner().removePropertyChangeListener(property, this);
        }
        propertiesToMove.remove(property);
    }

    /*
     * (non-Javadoc)
     * 
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        fireAnchorMoved();
    }
}