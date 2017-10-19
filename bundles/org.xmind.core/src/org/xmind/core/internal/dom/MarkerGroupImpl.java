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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_HIDDEN;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SINGLETON;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER;

import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.MarkerGroup;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.DOMUtils;

public class MarkerGroupImpl extends MarkerGroup implements ICoreEventSource {

    private Element implementation;

    private MarkerSheetImpl ownedSheet;

    public MarkerGroupImpl(Element implementation, MarkerSheetImpl ownedSheet) {
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedSheet = ownedSheet;
    }

    public Element getImplementation() {
        return implementation;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MarkerGroupImpl))
            return false;
        MarkerGroupImpl that = (MarkerGroupImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(implementation);
    }

    public String getName() {
        String name = implementation.getAttribute(ATTR_NAME);
        if (name.startsWith("%")) { //$NON-NLS-1$
            Properties properties = ownedSheet.getProperties();
            if (properties != null) {
                String key = name.substring(1);
                name = properties.getProperty(key, name);
            }
        }
        return name;
    }

    public IMarkerSheet getOwnedSheet() {
        return ownedSheet;
    }

    public IMarkerSheet getParent() {
        Node p = implementation.getParentNode();
        if (p == ownedSheet.getSheetElement()) {
            return ownedSheet;
        }
        return null;
    }

    public boolean isSingleton() {
        return Boolean
                .parseBoolean(implementation.getAttribute(ATTR_SINGLETON));
    }

    public boolean isHidden() {
        return Boolean.parseBoolean(implementation.getAttribute(ATTR_HIDDEN));
    }

    public void setName(String name) {
        String oldName = implementation.hasAttribute(ATTR_NAME) ? getName()
                : null;
        DOMUtils.setAttribute(implementation, ATTR_NAME, name);
        String newName = implementation.hasAttribute(ATTR_NAME) ? getName()
                : null;
        fireValueChange(Core.Name, oldName, newName);
    }

    public void setSingleton(boolean singleton) {
        DOMUtils.setAttribute(implementation, ATTR_SINGLETON,
                Boolean.toString(singleton));
    }

    public void setHidden(boolean hidden) {
        DOMUtils.setAttribute(implementation, ATTR_HIDDEN,
                Boolean.toString(hidden));
    }

    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    public List<IMarker> getMarkers() {
        return DOMUtils.getChildList(implementation, TAG_MARKER,
                ownedSheet.getElementAdapterProvider());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.marker.IMarkerGroup#isEmpty()
     */
    public boolean isEmpty() {
        return !implementation.hasChildNodes();
    }

    public void addMarker(IMarker marker) {
        Element m = ((MarkerImpl) marker).getImplementation();
        Node n = implementation.appendChild(m);
        if (n != null) {
            int index = DOMUtils.getElementIndex(implementation, TAG_MARKER, m);
            if (index >= 0) {
                if (getParent() != null)
                    fireIndexedTargetChange(Core.MarkerAdd, marker, index);
            }
        }
    }

    public void removeMarker(IMarker marker) {
        Element m = ((MarkerImpl) marker).getImplementation();
        if (m != null && m.getParentNode() == implementation) {
            int index = DOMUtils.getElementIndex(implementation, TAG_MARKER, m);
            if (index >= 0) {
                Node n = implementation.removeChild(m);
                if (n != null) {
                    if (getParent() != null)
                        fireIndexedTargetChange(Core.MarkerRemove, marker,
                                index);
                }
            }
        }
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    private void fireValueChange(String type, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, type, oldValue,
                newValue);
    }

    private void fireIndexedTargetChange(String type, Object target,
            int index) {
        getCoreEventSupport().dispatchIndexedTargetChange(this, type, target,
                index);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedSheet.getCoreEventSupport();
    }
}
