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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_AMOUNT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ANGLE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_INDEX;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_X;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_Y;
import static org.xmind.core.internal.dom.DOMConstants.TAG_CONTROL_POINT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_CONTROL_POINTS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_POSITION;
import static org.xmind.core.internal.dom.NumberUtils.safeParseInt;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.ControlPoint;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.Point;

public class ControlPointImpl extends ControlPoint implements ICoreEventSource {

    private RelationshipImpl parent;

    private int index;

    public ControlPointImpl(RelationshipImpl parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public IRelationship getParent() {
        return parent;
    }

    public ISheet getOwnedSheet() {
        return parent.getOwnedSheet();
    }

    public IWorkbook getOwnedWorkbook() {
        return parent.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return parent.isOrphan();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ControlPointImpl))
            return false;
        ControlPointImpl that = (ControlPointImpl) obj;
        return this.index == that.index && this.parent.equals(that.parent);
    }

    public String toString() {
        return "CP{" + index + "," + parent.toString() + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImplementation());
        if (IRelationship.class.equals(adapter))
            return adapter.cast(parent);
        return super.getAdapter(adapter);
    }

    private Element getImplementation() {
        Element cps = DOMUtils.getFirstChildElementByTag(getRelElement(),
                TAG_CONTROL_POINTS);
        if (cps != null) {
            Element cp = findImplementation(cps);
            if (cp != null)
                return cp;
        }
        return null;
    }

    private Element findImplementation(Element cps) {
        Iterator<Element> cpIt = DOMUtils.childElementIterByTag(cps,
                TAG_CONTROL_POINT);
        String indexString = String.valueOf(index);
        while (cpIt.hasNext()) {
            Element cp = cpIt.next();
            if (indexString.equals(cp.getAttribute(ATTR_INDEX))) {
                return cp;
            }
        }
        return null;
    }

    private Element getRelElement() {
        return parent.getImplementation();
    }

    public double getPolarAmount() {
        Element ele = getImplementation();
        if (ele != null) {
            return NumberUtils.safeParseDouble(
                    DOMUtils.getAttribute(ele, ATTR_AMOUNT), 0.3);
        }
        return 0.3;
    }

    public double getPolarAngle() {
        Element ele = getImplementation();
        if (ele != null) {
            return NumberUtils
                    .safeParseDouble(DOMUtils.getAttribute(ele, ATTR_ANGLE), 0);
        }
        return 0;
    }

    public boolean usesPolarPosition() {
//        Element implementation = getImplementation();
//        if (implementation == null)
//            return false;
//        if (implementation.hasAttribute(ATTR_ANGLE)
//                || implementation.hasAttribute(ATTR_AMOUNT))
//            return true;
//        if (hasPosition())
//            return false;
//        return true;
        return false;
    }

    public boolean hasPolarAmount() {
        Element ele = getImplementation();
        if (ele != null) {
            return ele.hasAttribute(ATTR_AMOUNT);
        }
        return false;
    }

    public boolean hasPolarAngle() {
        Element ele = getImplementation();
        if (ele != null)
            return ele.hasAttribute(ATTR_ANGLE);
        return false;
    }

    public void resetPolarAmount() {
        removeAttribute(ATTR_AMOUNT);
    }

    public void resetPolarAngle() {
        removeAttribute(ATTR_ANGLE);
    }

    private void removeAttribute(String attrName) {
        Element ele = getImplementation();
        if (ele != null) {
            ele.removeAttribute(attrName);
            if (!ele.hasAttributes() && !ele.hasChildNodes()) {
                ele.getParentNode().removeChild(ele);
            }
        }
    }

    private Element ensureImplementation() {
        Element cps = DOMUtils.ensureChildElement(getRelElement(),
                TAG_CONTROL_POINTS);
        Element cp = findImplementation(cps);
        if (cp == null) {
            cp = DOMUtils.createElement(cps, TAG_CONTROL_POINT);
            cp.setAttribute(ATTR_INDEX, String.valueOf(index));
        }
        return cp;
    }

    public void setPolarAmount(double amount) {
        Element ele = ensureImplementation();
        DOMUtils.setAttribute(ele, ATTR_AMOUNT, String.valueOf(amount));
    }

    public void setPolarAngle(double angle) {
        Element ele = ensureImplementation();
        DOMUtils.setAttribute(ele, ATTR_ANGLE, String.valueOf(angle));
    }

    public Point getPosition() {
        Element implementation = getImplementation();
        if (implementation == null)
            return null;
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e == null)
            return null;
        String x = DOMUtils.getAttribute(e, ATTR_X);
        String y = DOMUtils.getAttribute(e, ATTR_Y);
        if (x == null && y == null)
            return null;
        return new Point(safeParseInt(x, 0), safeParseInt(y, 0));
    }

    public boolean hasPosition() {
        Element implementation = getImplementation();
        if (implementation == null)
            return false;
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e == null)
            return false;
        return e.hasAttribute(ATTR_X) && e.hasAttribute(ATTR_Y);
    }

    public void setPosition(int x, int y) {
        Element implementation = ensureImplementation();
        Point oldValue = getPosition();
        Element e = DOMUtils.ensureChildElement(implementation, TAG_POSITION);
        DOMUtils.setAttribute(e, ATTR_X, Integer.toString(x));
        DOMUtils.setAttribute(e, ATTR_Y, Integer.toString(y));
        Point newValue = getPosition();
        fireValueChange(Core.Position, oldValue, newValue);
    }

    protected void removePosition() {
        Element implementation = getImplementation();
        if (implementation == null)
            return;

        Point oldValue = getPosition();
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e != null)
            implementation.removeChild(e);
        Point newValue = getPosition();
        fireValueChange(Core.Position, oldValue, newValue);
    }

    public void setPosition(Point position) {
        if (position == null)
            removePosition();
        else
            setPosition(position.x, position.y);
    }

    protected void fireValueChange(String type, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, type, oldValue,
                newValue);
    }

    public ICoreEventSupport getCoreEventSupport() {
        // Use workbook's core event support directly, so that 
        // orphan components can have events broadcasted, which 
        // will enable transient actions (such as dragging topics 
        // or adjusting relationship control points, etc.) to 
        // perform correctly.
        return parent.getCoreEventSupport();
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

}
