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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_REVISION_NUMBER;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TIMESTAMP;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.Revision;
import org.xmind.core.util.DOMUtils;

/**
 * @author Frank Shaka
 */
public class RevisionImpl extends Revision {

    private Element implementation;

    private RevisionManagerImpl parent;

    private IAdaptable content = null;

    public RevisionImpl(Element implementation, RevisionManagerImpl parent) {
        this.implementation = implementation;
        this.parent = parent;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof RevisionImpl))
            return false;
        RevisionImpl that = (RevisionImpl) obj;
        return implementation.equals(that.implementation);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImplementation());
        return super.getAdapter(adapter);
    }

    public Element getImplementation() {
        return implementation;
    }

    public IRevisionManager getOwnedManager() {
        return parent;
    }

    public String getContentType() {
        return parent.getContentType();
    }

    public String getResourceId() {
        return parent.getResourceId();
    }

    public int getRevisionNumber() {
        String num = getImplementation().getAttribute(ATTR_REVISION_NUMBER);
        return NumberUtils.safeParseInt(num, 0);
    }

    public long getTimestamp() {
        String num = getImplementation().getAttribute(ATTR_TIMESTAMP);
        return NumberUtils.safeParseLong(num, 0);
    }

    public IAdaptable getContent() {
        if (content == null) {
            content = loadContent();
        }
        return content;
    }

    public String getResourcePath() {
        return getImplementation().getAttribute(ATTR_RESOURCE);
    }

    private IAdaptable loadContent() {
        String path = getResourcePath();
        Document doc = loadDocument(path);
        if (doc != null) {
            Element docEle = doc.getDocumentElement();
            if (docEle != null) {
                Node ele = docEle.getFirstChild();
                if (ele != null) {
                    return parent.getAdaptableFactory().createAdaptable(ele);
                }
            }
        }
        return null;
    }

    private Document loadDocument(String path) {
        IFileEntry entry = getOwnedWorkbook().getManifest().getFileEntry(path);
        if (entry != null) {
            try {
                InputStream stream = entry.openInputStream();
                try {
                    return DOMUtils.loadDocument(stream);
                } finally {
                    stream.close();
                }
            } catch (Throwable e) {
                Core.getLogger().log(e, "Failed to load content from " + path); //$NON-NLS-1$
            }
        }
        return null;
    }

    public IWorkbook getOwnedWorkbook() {
        return parent.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(getImplementation());
    }

    protected void addNotify(WorkbookImpl workbook) {
        increaseFileEntryReference();
        if (SHEET.equals(getContentType())
                && getContent() instanceof SheetImpl) {
            ((SheetImpl) getContent()).addNotify(workbook);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        if (SHEET.equals(getContentType())
                && getContent() instanceof SheetImpl) {
            ((SheetImpl) getContent()).removeNotify(workbook);
        }
        decreaseFileEntryReference();
    }

    /**
     * 
     */
    private void increaseFileEntryReference() {
        String path = getResourcePath();
        if (path == null || "".equals(path)) //$NON-NLS-1$
            return;

        IManifest manifest = parent.getOwnedWorkbook().getManifest();
        IFileEntry entry = manifest.getFileEntry(path);
        if (entry != null) {
            entry.increaseReference();
        }
    }

    /**
     * 
     */
    private void decreaseFileEntryReference() {
        String path = getResourcePath();
        if (path == null || "".equals(path)) //$NON-NLS-1$
            return;

        IManifest manifest = parent.getOwnedWorkbook().getManifest();
        IFileEntry entry = manifest.getFileEntry(path);
        if (entry != null) {
            entry.decreaseReference();
        }
    }

}
