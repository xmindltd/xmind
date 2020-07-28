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

import org.w3c.dom.Element;
import org.xmind.core.IFileEntry;
import org.xmind.core.IResourceRef;
import org.xmind.core.internal.AbstractWorkbookComponent;
import org.xmind.core.marker.IMarker;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.DOMUtils;

public class ResourceRefImpl extends AbstractWorkbookComponent
        implements IResourceRef {

    private Element source;

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    public ResourceRefImpl(Element implementation, WorkbookImpl ownedWorkbook) {
        this.source = implementation;
        this.implementation = implementation;
        this.ownedWorkbook = ownedWorkbook;
    }

    protected void setImplementation(Element implementation) {
        this.implementation = implementation;
    }

    public Element getImplementation() {
        return implementation;
    }

    public WorkbookImpl getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ResourceRefImpl))
            return false;
        ResourceRefImpl that = (ResourceRefImpl) obj;
        return this.source == that.source;
    }

    public int hashCode() {
        return source.hashCode();
    }

    public String toString() {
        return "ResRef#" + getResourceId(); //$NON-NLS-1$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public Object getResource() {
        String type = getType();
        if (FILE_ENTRY.equals(type))
            return getFileEntry();
//        else if (MARKER.equals(type))
//            return getMarker();
//        else if (STYLE.equals(type))
//            return getStyle();
        return null;
    }

    protected void addNotify(WorkbookImpl workbook) {
        if (workbook != null) {
            String type = getType();
            if (FILE_ENTRY.equals(type)) {
                InternalHyperlinkUtils.increaseFileEntryRef(workbook,
                        getResourceId());
            }
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        if (workbook != null) {
            String type = getType();
            if (FILE_ENTRY.equals(type)) {
                InternalHyperlinkUtils.decreaseFileEntryRef(workbook,
                        getResourceId());
            }
        }
    }

    protected IFileEntry getFileEntry() {
        String entryPath = getResourceId();
        return getOwnedWorkbook().getManifest().getFileEntry(entryPath);
    }

    protected IMarker getMarker() {
        String markerId = getResourceId();
        return getOwnedWorkbook().getMarkerSheet().findMarker(markerId);
    }

    protected IStyle getStyle() {
        String styleId = getResourceId();
        return getOwnedWorkbook().getStyleSheet().findStyle(styleId);
    }

    public String getResourceId() {
        return DOMUtils.getAttribute(implementation,
                DOMConstants.ATTR_RESOURCE_ID);
    }

    public String getType() {
        return DOMUtils.getAttribute(implementation, DOMConstants.ATTR_TYPE);
    }

}
