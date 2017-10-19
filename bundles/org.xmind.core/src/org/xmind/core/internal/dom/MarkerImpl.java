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
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SVG;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_GROUP;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Marker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.DOMUtils;

public class MarkerImpl extends Marker implements ICoreEventSource {

    private Element implementation;

    private MarkerSheetImpl ownedSheet;

    public MarkerImpl(Element implementation, MarkerSheetImpl ownedSheet) {
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedSheet = ownedSheet;
    }

    public IMarkerSheet getOwnedSheet() {
        return ownedSheet;
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
        if (obj == null || !(obj instanceof MarkerImpl))
            return false;
        MarkerImpl that = (MarkerImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "MKR#" + getId() + "(" + getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

    public IMarkerGroup getParent() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_MARKER_GROUP)) {
            return (IMarkerGroup) ownedSheet.getElementAdapter((Element) p);
        }
        return null;
    }

    public IMarkerResource getResource() {
        return ownedSheet.getMarkerResource(this);
    }

    public String getResourcePath() {
        return implementation.getAttribute(ATTR_RESOURCE);
    }

    public void setResourcePath(String resourcePath) {
        DOMUtils.setAttribute(implementation, ATTR_RESOURCE, resourcePath);
    }

    public String getSVGPath() {
        return DOMUtils.getAttribute(implementation, ATTR_SVG);
    }

    public void setName(String name) {
        String oldName = DOMUtils.getAttribute(implementation, ATTR_NAME);
        DOMUtils.setAttribute(implementation, ATTR_NAME, name);
        String newName = DOMUtils.getAttribute(implementation, ATTR_NAME);
        fireValueChange(Core.Name, oldName, newName);
    }

    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    public boolean isHidden() {
        return Boolean.parseBoolean(implementation.getAttribute(ATTR_HIDDEN));
    }

    public void setHidden(boolean hidden) {
        DOMUtils.setAttribute(implementation, ATTR_HIDDEN,
                Boolean.toString(hidden));
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

    public ICoreEventSupport getCoreEventSupport() {
        return ownedSheet.getCoreEventSupport();
    }
}
