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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_CREATOR_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_CREATOR_VERSION;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MEDIA_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_NEXT_REVISION_NUMBER;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_REVISION_NUMBER;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TIMESTAMP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_REVISION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_REVISION_CONTENT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IFileEntry;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.RevisionManager;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.util.DOMUtils;

/**
 * @author Frank Shaka
 */
public class RevisionManagerImpl extends RevisionManager
        implements ICoreEventSource, INodeAdaptableFactory {

    private Document implementation;

    private WorkbookImpl ownedWorkbook;

    private NodeAdaptableRegistry registry;

    private ICoreEventSupport coreEventSupport;

    private String dirPath;

    private boolean registered = false;

    /**
     * 
     */
    public RevisionManagerImpl(Document implementation,
            WorkbookImpl ownedWorkbook, String dirPath) {
        this.implementation = implementation;
        this.ownedWorkbook = ownedWorkbook;
        this.dirPath = dirPath;
        this.registry = new NodeAdaptableRegistry(implementation, this);
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof RevisionManagerImpl))
            return false;
        RevisionManagerImpl that = (RevisionManagerImpl) obj;
        return implementation.equals(that.implementation);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Document.class))
            return adapter.cast(getImplementation());
        return super.getAdapter(adapter);
    }

    public Document getImplementation() {
        return implementation;
    }

    protected Element getRevisionsElement() {
        return implementation.getDocumentElement();
    }

    protected INodeAdaptableFactory getAdaptableFactory() {
        return ownedWorkbook;
    }

    public IAdaptable createAdaptable(Node node) {
        return new RevisionImpl((Element) node, this);
    }

    protected String getDirPath() {
        return dirPath;
    }

    public String getResourceId() {
        return getRevisionsElement().getAttribute(ATTR_RESOURCE_ID);
    }

    public String getContentType() {
        return getRevisionsElement().getAttribute(ATTR_MEDIA_TYPE);
    }

    public Iterator<IRevision> iterRevisions() {
        return new DOMUtils.AdaptableIterator<IRevision>(getRevisionsElement(),
                TAG_REVISION, registry, false);
    }

    public Iterator<IRevision> iterRevisionsReversed() {
        return new DOMUtils.AdaptableIterator<IRevision>(getRevisionsElement(),
                TAG_REVISION, registry, true);
    }

    public IRevision getLatestRevision() {
        Node node = getLastRevisionNode();
        return node == null ? null : (IRevision) registry.getAdaptable(node);
    }

    protected Node getLastRevisionNode() {
        Node node = getRevisionsElement().getLastChild();
        while (node != null && !DOMUtils.isElementByTag(node, TAG_REVISION)) {
            node = node.getPreviousSibling();
        }
        return node;
    }

    public int getNextRevisionNumber() {
        String num = getRevisionsElement()
                .getAttribute(ATTR_NEXT_REVISION_NUMBER);
        return NumberUtils.safeParseInt(num, 1);
    }

    public IRevision addRevision(IAdaptable content)
            throws IOException, CoreException {
        Node node = (Node) content.getAdapter(Node.class);
        if (node == null)
            throw new CoreException(Core.ERROR_INVALID_ARGUMENT,
                    "Invalid content for content type: " + getContentType()); //$NON-NLS-1$

        IRevision latest = getLatestRevision();
        if (latest != null) {
            IAdaptable latestContent = latest.getContent();
            if (latestContent != null) {
                Object latestNode = latestContent.getAdapter(Node.class);
                if (latestNode != null && latestNode.equals(node))
                    return null;
            }
        }

        int revNum = getNextRevisionNumber();
        long timestamp = System.currentTimeMillis();
        String path = takeSnapshot(node, revNum, timestamp);
        RevisionImpl revision = createRevision(revNum, timestamp, path);
        setNextRevisionNumber(revNum + 1);
        if (!isOrphan()) {
            revision.addNotify(ownedWorkbook);
            fireTargetEvent(Core.RevisionAdd, revision);
        }
        return revision;
    }

    private void setNextRevisionNumber(int nextRevNum) {
        getRevisionsElement().setAttribute(ATTR_NEXT_REVISION_NUMBER,
                String.valueOf(nextRevNum));
    }

    private RevisionImpl createRevision(int revNum, long timestamp,
            String path) {
        Element ele = implementation.createElement(TAG_REVISION);
        ele.setAttribute(ATTR_REVISION_NUMBER, String.valueOf(revNum));
        ele.setAttribute(ATTR_TIMESTAMP, String.valueOf(timestamp));
        ele.setAttribute(ATTR_RESOURCE, path);

        ele.setAttribute(ATTR_CREATOR_NAME,
                getOwnedWorkbook().getMeta().getValue(IMeta.CREATOR_NAME));
        ele.setAttribute(ATTR_CREATOR_VERSION,
                getOwnedWorkbook().getMeta().getValue(IMeta.CREATOR_VERSION));
        getRevisionsElement().appendChild(ele);
        return new RevisionImpl(ele, this);
    }

    private String takeSnapshot(Node source, int revNum, long timestamp)
            throws IOException, CoreException {
        Document doc = DOMUtils.createDocument(TAG_REVISION_CONTENT);
        Element docEle = doc.getDocumentElement();
        NS.setNS(NS.Revision, docEle, NS.Xhtml, NS.Xlink, NS.SVG, NS.Fo);
        Node snapshot = doc.importNode(source, true);
        docEle.appendChild(snapshot);

        String path = getDirPath() + getFileName(revNum, timestamp, "xml"); //$NON-NLS-1$
        IFileEntry entry = ownedWorkbook.getManifest().createFileEntry(path);
        OutputStream stream = entry.openOutputStream();
        DOMUtils.save(doc, stream, true);
        return path;
    }

    private String getFileName(int revNum, long timestamp, String extName) {
        return String.format("rev-%s-%s.%s", revNum, timestamp, extName); //$NON-NLS-1$
    }

    public Object removeRevision(IRevision revision) {
        RevisionImpl rev = (RevisionImpl) revision;
        Element ele = rev.getImplementation();
        Element parentEle = getRevisionsElement();
        int index = DOMUtils.getNodeIndex(parentEle, ele);
        if (index < 0)
            return null;
        if (!isOrphan())
            rev.removeNotify(ownedWorkbook);
        parentEle.removeChild(ele);
        if (!isOrphan())
            fireTargetEvent(Core.RevisionRemove, revision);
        return Integer.valueOf(index);
    }

    public void restoreRevision(IRevision revision, Object removal) {
        if (!(removal instanceof Integer))
            throw new IllegalArgumentException("Invalid removal object"); //$NON-NLS-1$
        int index = ((Integer) removal).intValue();
        RevisionImpl rev = (RevisionImpl) revision;
        Element ele = rev.getImplementation();
        Element parentEle = getRevisionsElement();
        NodeList childNodes = parentEle.getChildNodes();
        if (index < childNodes.getLength()) {
            parentEle.insertBefore(ele, childNodes.item(index));
        } else {
            parentEle.appendChild(ele);
        }
        if (!isOrphan()) {
            ((RevisionImpl) revision).addNotify(ownedWorkbook);
            fireTargetEvent(Core.RevisionAdd, revision);
        }
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return !registered;
    }

    protected void addNotify(WorkbookImpl workbook) {
        registered = true;
        increaseFileEntryReference();
        Iterator<IRevision> it = iterRevisions();
        while (it.hasNext()) {
            RevisionImpl rev = (RevisionImpl) it.next();
            rev.addNotify(workbook);
            fireTargetEvent(Core.RevisionAdd, rev);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        Iterator<IRevision> it = iterRevisionsReversed();
        while (it.hasNext()) {
            RevisionImpl rev = (RevisionImpl) it.next();
            rev.removeNotify(workbook);
            fireTargetEvent(Core.RevisionRemove, rev);
        }
        decreaseFileEntryReference();
        registered = false;
    }

    private void increaseFileEntryReference() {
        IFileEntry entry = getMetaFileEntry();
        if (entry != null) {
            entry.increaseReference();
        }
    }

    private void decreaseFileEntryReference() {
        IFileEntry entry = getMetaFileEntry();
        if (entry != null) {
            entry.decreaseReference();
        }
    }

    private IFileEntry getMetaFileEntry() {
        String path = dirPath + ArchiveConstants.REVISIONS_XML;
        return ownedWorkbook.getManifest().getFileEntry(path);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.event.ICoreEventSource#getCoreEventSupport()
     */
    public ICoreEventSupport getCoreEventSupport() {
        if (coreEventSupport == null) {
            coreEventSupport = ownedWorkbook.getCoreEventSupport();
        }
        return coreEventSupport;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.event.ICoreEventSource#registerCoreEventListener(java.
     * lang.String, org.xmind.core.event.ICoreEventListener)
     */
    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerGlobalListener(type, listener);
    }

    private void fireTargetEvent(String eventType, IRevision revision) {
        getCoreEventSupport().dispatchTargetChange(this, eventType, revision);
    }

}
