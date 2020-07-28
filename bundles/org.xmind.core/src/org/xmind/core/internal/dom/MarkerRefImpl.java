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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_MARKER_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REFS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.MarkerRef;
import org.xmind.core.marker.IMarker;
import org.xmind.core.util.DOMUtils;

public class MarkerRefImpl extends MarkerRef {

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    public MarkerRefImpl(Element implementation, WorkbookImpl ownedWorkbook) {
        this.implementation = implementation;
        this.ownedWorkbook = ownedWorkbook;
    }

    public Element getImplementation() {
        return implementation;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MarkerRefImpl))
            return false;
        MarkerRefImpl that = (MarkerRefImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "MKRRef#" + getMarkerId(); //$NON-NLS-1$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public IMarker getMarker() {
        return ownedWorkbook.getMarkerSheet().findMarker(getMarkerId());
    }

    public String getMarkerId() {
        return DOMUtils.getAttribute(implementation, ATTR_MARKER_ID);
    }

    public void setMarkerId(String markerId) {
        DOMUtils.setAttribute(implementation, ATTR_MARKER_ID, markerId);
    }

    public ITopic getParent() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_MARKER_REFS)) {
            p = p.getParentNode();
            if (DOMUtils.isElementByTag(p, TAG_TOPIC)) {
                return (ITopic) ownedWorkbook.getAdaptableRegistry()
                        .getAdaptable(p);
            }
        }
        return null;
    }

    public ISheet getOwnedSheet() {
        ITopic parent = getParent();
        if (parent != null)
            return parent.getOwnedSheet();
        return null;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

}
