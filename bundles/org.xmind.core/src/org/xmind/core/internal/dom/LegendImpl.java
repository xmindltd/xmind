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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_X;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_Y;
import static org.xmind.core.internal.dom.DOMConstants.TAG_POSITION;
import static org.xmind.core.internal.dom.NumberUtils.safeParseInt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Legend;
import org.xmind.core.marker.IMarker;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.Point;

public class LegendImpl extends Legend implements ICoreEventSource {

    private Element sheetElement;

    private SheetImpl ownedSheet;

    public LegendImpl(Element sheetElement, SheetImpl ownedSheet) {
        this.sheetElement = sheetElement;
        this.ownedSheet = ownedSheet;
    }

    public Element getSheetElement() {
        return sheetElement;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof LegendImpl))
            return false;
        LegendImpl that = (LegendImpl) obj;
        return this.sheetElement == that.sheetElement;
    }

    public int hashCode() {
        return sheetElement.hashCode();
    }

    public String toString() {
        return "LGD{" + ownedSheet + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public ISheet getOwnedSheet() {
        return ownedSheet;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedSheet.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(sheetElement)
                || getImplementation() == null;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        return super.getAdapter(adapter);
    }

    public Element getImplementation() {
        return DOMUtils.getFirstChildElementByTag(sheetElement,
                DOMConstants.TAG_LEGEND);
    }

    private Element ensureImplementation() {
        return DOMUtils.ensureChildElement(sheetElement,
                DOMConstants.TAG_LEGEND);
    }

    private void checkImplementation() {
        Element implementation = getImplementation();
        if (!implementation.hasAttributes()
                && !implementation.hasChildNodes()) {
            sheetElement.removeChild(implementation);
        }
    }

    public boolean isEmpty() {
        Element implementation = getImplementation();
        if (implementation != null) {
            return !implementation.hasChildNodes();
        }
        return true;
    }

    public boolean isVisible() {
        Element implementation = getImplementation();
        if (implementation != null) {
            return DOMConstants.VAL_VISIBLE.equals(
                    implementation.getAttribute(DOMConstants.ATTR_VISIBILITY));
        }
        return false;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (!isVisible()) {
                Element implementation = ensureImplementation();
                DOMUtils.setAttribute(implementation,
                        DOMConstants.ATTR_VISIBILITY, DOMConstants.VAL_VISIBLE);
                fireValueChange(Core.Visibility, Boolean.FALSE, Boolean.TRUE);
            }
        } else {
            if (isVisible()) {
                Element implementation = getImplementation();
                if (implementation != null) {
                    DOMUtils.setAttribute(implementation,
                            DOMConstants.ATTR_VISIBILITY,
                            DOMConstants.VAL_HIDDEN);
                    checkImplementation();
                    fireValueChange(Core.Visibility, Boolean.TRUE,
                            Boolean.FALSE);
                }
            }
        }
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
        if (implementation != null) {
            Element e = DOMUtils.getFirstChildElementByTag(implementation,
                    TAG_POSITION);
            if (e != null)
                return e.hasAttribute(ATTR_X) && e.hasAttribute(ATTR_Y);
        }
        return false;
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

    private Element getItemsElement() {
        Element implementation = getImplementation();
        if (implementation != null) {
            return DOMUtils.getFirstChildElementByTag(implementation,
                    DOMConstants.TAG_MARKER_DESCRIPTIONS);
        }
        return null;
    }

    private Element ensureItemsElement() {
        Element implementation = ensureImplementation();
        return DOMUtils.ensureChildElement(implementation,
                DOMConstants.TAG_MARKER_DESCRIPTIONS);
    }

    public String getMarkerDescription(String markerId) {
        Element itemsEle = getItemsElement();
        Element item = findItem(itemsEle, markerId);
        if (item != null) {
            String description = DOMUtils.getAttribute(item,
                    DOMConstants.ATTR_DESCRIPTION);
            if (description != null)
                return description;
        }
        IMarker marker = getOwnedWorkbook().getMarkerSheet()
                .findMarker(markerId);
        if (marker != null)
            return marker.getName();
        return ""; //$NON-NLS-1$
    }

    private Element findItem(Element itemsEle, String markerId) {
        if (itemsEle != null) {
            Iterator<Element> it = DOMUtils.childElementIterByTag(itemsEle,
                    DOMConstants.TAG_MARKER_DESCRIPTION);
            while (it.hasNext()) {
                Element item = it.next();
                String m = DOMUtils.getAttribute(item,
                        DOMConstants.ATTR_MARKER_ID);
                if (m != null && m.equals(markerId)) {
                    return item;
                }
            }
        }
        return null;
    }

    public void setMarkerDescription(String markerId, String description) {
        String oldValue = null;
        Element itemsEle = getItemsElement();
        Element item = findItem(itemsEle, markerId);
        if (item != null) {
            oldValue = DOMUtils.getAttribute(item,
                    DOMConstants.ATTR_DESCRIPTION);
            if (description != null) {
                DOMUtils.setAttribute(item, DOMConstants.ATTR_DESCRIPTION,
                        description);
            } else {
                itemsEle.removeChild(item);
                if (!itemsEle.hasChildNodes()) {
                    Node p = itemsEle.getParentNode();
                    if (p != null)
                        p.removeChild(itemsEle);
                }
                checkImplementation();
            }
        } else if (description != null) {
            if (itemsEle == null)
                itemsEle = ensureItemsElement();
            item = DOMUtils.createElement(itemsEle,
                    DOMConstants.TAG_MARKER_DESCRIPTION);
            DOMUtils.setAttribute(item, DOMConstants.ATTR_MARKER_ID, markerId);
            DOMUtils.setAttribute(item, DOMConstants.ATTR_DESCRIPTION,
                    description);
        }
        fireTargetValueChange(Core.MarkerDescription, markerId, oldValue,
                description);
    }

    public Set<String> getMarkerIds() {
        Element itemsEle = getItemsElement();
        if (itemsEle != null) {
            Iterator<Element> it = DOMUtils.childElementIterByTag(itemsEle,
                    DOMConstants.TAG_MARKER_DESCRIPTION);
            if (it.hasNext()) {
                ArrayList<String> list = new ArrayList<String>(
                        itemsEle.getChildNodes().getLength());
                while (it.hasNext()) {
                    Element item = it.next();
                    String markerId = DOMUtils.getAttribute(item,
                            DOMConstants.ATTR_MARKER_ID);
                    if (markerId != null) {
                        list.add(markerId);
                    }
                }
                if (!list.isEmpty())
                    return DOMUtils.unmodifiableSet(list);
            }
        }
        return NO_MARKER_IDS;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    protected void fireValueChange(String type, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, type, oldValue,
                newValue);
    }

    protected void fireTargetValueChange(String type, Object target,
            Object oldValue, Object newValue) {
        getCoreEventSupport().dispatchTargetValueChange(this, type, target,
                oldValue, newValue);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedSheet.getCoreEventSupport();
    }
}
