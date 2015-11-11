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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_VERSION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MANIFEST;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUMMARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;
import static org.xmind.core.internal.dom.DOMConstants.TAG_WORKBOOK;
import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IBoundary;
import org.xmind.core.ICloneData;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.IResourceRef;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbookComponentRefManager;
import org.xmind.core.comment.ICommentManager;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Workbook;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.IMarkerRefCounter;
import org.xmind.core.util.IStyleRefCounter;

/**
 * @author briansun
 * 
 */
public class WorkbookImpl extends Workbook
        implements ICoreEventSource, ICoreEventSource2, INodeAdaptableFactory {

    private static interface ISaveable {
        void run() throws IOException, CoreException;
    }

    private Document implementation;

    private WorkbookSaver saver;

    private TempSaver tempSaver;

    private NodeAdaptableRegistry adaptableRegistry;

    private CoreEventSupport coreEventSupport = null;

    private StyleSheetImpl styleSheet = null;

    private ManifestImpl manifest = null;

    private MarkerSheetImpl markerSheet = null;

    private MetaImpl meta = null;

    private CommentManagerImpl commentManager = null;

    private WorkbookMarkerRefCounter markerRefCounter = null;

    private WorkbookStyleRefCounter styleRefCounter = null;

    private WorkbookComponentRefCounter elementRefCounter = null;

    private String password = null;

    private RevisionRepositoryImpl revisionRepository = null;

    private String creatorName;

    private String creatorVersion;

    /**
     * @param implementation
     */
    public WorkbookImpl(Document implementation) {
        this(implementation, null, true);
    }

    /**
     * @param fileName
     *            The file name of the workbook.
     */
    public WorkbookImpl(Document implementation, String targetPath) {
        this(implementation, targetPath, true);
    }

    public WorkbookImpl(Document implementation, String targetPath,
            boolean needInit) {
        this.implementation = implementation;
        this.saver = new WorkbookSaver(this, targetPath);
        this.tempSaver = new TempSaver(this);
        this.adaptableRegistry = new NodeAdaptableRegistry(implementation,
                this);
        if (needInit)
            init();
    }

    private void init() {
        Element w = DOMUtils.ensureChildElement(implementation, TAG_WORKBOOK);
        NS.setNS(NS.XMAP, w, NS.Xhtml, NS.Xlink, NS.SVG, NS.Fo);
        if (!DOMUtils.childElementIterByTag(w, TAG_SHEET).hasNext())
            addSheet(createSheet());
        InternalDOMUtils.addVersion(implementation);
    }

    /**
     * @return the implementation
     */
    public Document getImplementation() {
        return implementation;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookImpl))
            return false;
        WorkbookImpl that = (WorkbookImpl) obj;
        return implementation == that.implementation;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        if (getFile() != null) {
            return "Workbook(" + getFile() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return "Workbook{" + hashCode() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ICoreEventSource.class)
            return this;
        if (adapter == Document.class || adapter == Node.class)
            return implementation;
        if (adapter == Element.class)
            return getWorkbookElement();
        if (adapter == IMarkerSheet.class)
            return getMarkerSheet();
        if (adapter == IManifest.class)
            return getManifest();
        if (adapter == ICoreEventSupport.class)
            return getCoreEventSupport();
        if (adapter == INodeAdaptableFactory.class)
            return this;
        if (adapter == INodeAdaptableProvider.class)
            return getAdaptableRegistry();
        if (adapter == IMarkerRefCounter.class)
            return getMarkerRefCounter();
        if (adapter == IStyleRefCounter.class)
            return getStyleRefCounter();
        if (adapter == IWorkbookComponentRefManager.class)
            return getElementRefCounter();
        if (adapter == IRevisionRepository.class)
            return getRevisionRepository();

        return super.getAdapter(adapter);
    }

    /**
     * @return
     */
    protected Element getWorkbookElement() {
        return implementation.getDocumentElement();
    }

    /**
     * @return the adaptableRegistry
     */
    public NodeAdaptableRegistry getAdaptableRegistry() {
        return adaptableRegistry;
    }

    /**
     * @see org.xmind.core.IWorkbook#createTopic()
     */
    public ITopic createTopic() {
        TopicImpl topic = new TopicImpl(implementation.createElement(TAG_TOPIC),
                this);
        getAdaptableRegistry().registerByNode(topic, topic.getImplementation());
        return topic;
    }

    /**
     * @see org.xmind.core.IWorkbook#createSheet()
     */
    public ISheet createSheet() {
        SheetImpl sheet = new SheetImpl(implementation.createElement(TAG_SHEET),
                this);
        getAdaptableRegistry().registerByNode(sheet, sheet.getImplementation());
        return sheet;
    }

    /**
     * @see org.xmind.core.IWorkbook#createRelationship()
     */
    public IRelationship createRelationship() {
        RelationshipImpl relationship = new RelationshipImpl(
                implementation.createElement(TAG_RELATIONSHIP), this);
        getAdaptableRegistry().registerByNode(relationship,
                relationship.getImplementation());
        return relationship;
    }

    /**
     * @see org.xmind.core.IWorkbook#createRelationship(org.xmind.core.ITopic,
     *      org.xmind.core.ITopic)
     */
    public IRelationship createRelationship(IRelationshipEnd end1,
            IRelationshipEnd end2) {
        ISheet sheet = end1.getOwnedSheet();
        IRelationship rel = createRelationship();
        rel.setEnd1Id(end1.getId());
        rel.setEnd2Id(end2.getId());
        sheet.addRelationship(rel);
        return rel;
    }

    public IBoundary createBoundary() {
        BoundaryImpl boundary = new BoundaryImpl(
                implementation.createElement(TAG_BOUNDARY), this);
        getAdaptableRegistry().registerByNode(boundary,
                boundary.getImplementation());
        return boundary;
    }

    public ISummary createSummary() {
        SummaryImpl summary = new SummaryImpl(
                implementation.createElement(TAG_SUMMARY), this);
        getAdaptableRegistry().registerByNode(summary,
                summary.getImplementation());
        return summary;
    }

    public INotesContent createNotesContent(String format) {
        Element e = implementation.createElement(format);
        INotesContent content;
        if (INotes.HTML.equals(format)) {
            content = new HtmlNotesContentImpl(e, this);
        } else {
            content = new PlainNotesContentImpl(e, this);
        }
        getAdaptableRegistry().registerByNode(content, e);
        return content;
    }

    public String getVersion() {
        return DOMUtils.getAttribute(getWorkbookElement(), ATTR_VERSION);
    }

    private void saveVersion() {
        InternalDOMUtils.replaceVersion(implementation);
        InternalDOMUtils
                .replaceVersion(((MetaImpl) getMeta()).getImplementation());
        if (styleSheet != null) {
            InternalDOMUtils.replaceVersion(styleSheet.getImplementation());
        }
        if (markerSheet != null) {
            InternalDOMUtils.replaceVersion(markerSheet.getImplementation());
        }
    }

    /**
     * @see org.xmind.core.IWorkbook#getSheets()
     */
    public List<ISheet> getSheets() {
        return DOMUtils.getChildList(getWorkbookElement(), TAG_SHEET,
                getAdaptableRegistry());
    }

    public ISheet getPrimarySheet() {
        Element e = DOMUtils.getFirstChildElementByTag(getWorkbookElement(),
                TAG_SHEET);
        if (e != null)
            return (ISheet) getAdaptableRegistry().getAdaptable(e);
        return null;
    }

    public void addSheet(ISheet sheet, int index) {
        Element s = ((SheetImpl) sheet).getImplementation();
        if (s != null && s.getOwnerDocument() == implementation) {
            Element w = getWorkbookElement();
            Node n = null;
            Element[] es = DOMUtils.getChildElementsByTag(w, TAG_SHEET);
            if (index >= 0 && index < es.length) {
                n = w.insertBefore(s, es[index]);
            } else {
                n = w.appendChild(s);
            }
            if (n != null) {
                ((SheetImpl) sheet).addNotify(this);
                fireIndexedTargetChange(Core.SheetAdd, sheet, sheet.getIndex());
                updateModificationInfo();
            }
        }
    }

    public void removeSheet(ISheet sheet) {
//        if (hasOnlyOneSheet())
//            return;

        Element s = ((SheetImpl) sheet).getImplementation();
        Element w = getWorkbookElement();
        if (s != null && s.getParentNode() == w) {
            int oldIndex = sheet.getIndex();
            ((SheetImpl) sheet).removeNotify(this);
            Node n = w.removeChild(s);
            if (n != null) {
                fireIndexedTargetChange(Core.SheetRemove, sheet, oldIndex);
                updateModificationInfo();
            }
        }
    }

//    private boolean hasOnlyOneSheet() {
//        Iterator<Element> it = DOMUtils.childElementIterByTag(
//                getWorkbookElement(), TAG_SHEET);
//        if (it.hasNext()) {
//            it.next();
//            if (!it.hasNext())
//                return true;
//        }
//        return false;
//    }

    public void moveSheet(int sourceIndex, int targetIndex) {
        if (sourceIndex < 0 || sourceIndex == targetIndex)
            return;
        Element w = getWorkbookElement();
        Element[] ss = DOMUtils.getChildElementsByTag(w, TAG_SHEET);
        if (sourceIndex >= ss.length)
            return;
        Element s = ss[sourceIndex];
        if (targetIndex >= 0 && targetIndex < ss.length - 1) {
            int realTargetIndex = sourceIndex < targetIndex ? targetIndex + 1
                    : targetIndex;
            Element target = ss[realTargetIndex];
            if (s != target) {
                w.removeChild(s);
                w.insertBefore(s, target);
            }
        } else {
            w.removeChild(s);
            w.appendChild(s);
            targetIndex = ss.length - 1;
        }
        if (sourceIndex != targetIndex) {
            fireIndexedTargetChange(Core.SheetMove,
                    getAdaptableRegistry().getAdaptable(s), sourceIndex);
            updateModificationInfo();
        }
    }

    public IStyleSheet getStyleSheet() {
        if (styleSheet == null)
            styleSheet = createStyleSheet();
        return styleSheet;
    }

    public void setStyleSheet(StyleSheetImpl styleSheet) {
        this.styleSheet = styleSheet;
    }

    protected StyleSheetImpl createStyleSheet() {
        StyleSheetImpl ss = (StyleSheetImpl) Core.getStyleSheetBuilder()
                .createStyleSheet();
        ss.setManifest(getManifest());
        return ss;
    }

    public IManifest getManifest() {
        if (manifest == null)
            manifest = createManifest();
        return manifest;
    }

    public void setManifest(ManifestImpl manifest) {
        if (manifest == null)
            throw new IllegalArgumentException("Manifest is null"); //$NON-NLS-1$
        ManifestImpl oldManifest = this.manifest;
        this.manifest = manifest;
        if (oldManifest != null) {
            oldManifest.setWorkbook(null);
        }
        manifest.setWorkbook(this);
    }

    protected ManifestImpl createManifest() {
        Document mfImpl = createManifestImplementation();

        ManifestImpl mf = new ManifestImpl(mfImpl);
        mf.setWorkbook(this);

        mf.createFileEntry(CONTENT_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();
        mf.createFileEntry(MANIFEST_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();
        mf.createFileEntry(META_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();

        return mf;
    }

    private Document createManifestImplementation() {
        return DOMUtils.createDocument(TAG_MANIFEST);
    }

    public IMarkerSheet getMarkerSheet() {
        if (markerSheet == null)
            markerSheet = createMarkerSheet();
        return markerSheet;
    }

    protected MarkerSheetImpl createMarkerSheet() {
        return (MarkerSheetImpl) Core.getMarkerSheetBuilder()
                .createMarkerSheet(new WorkbookMarkerResourceProvider(this));
    }

    public void setMarkerSheet(MarkerSheetImpl markerSheet) {
        this.markerSheet = markerSheet;
    }

    public IMeta getMeta() {
        if (meta == null) {
            meta = createMeta();
        }
        return meta;
    }

    private MetaImpl createMeta() {
        Document metaImpl = DOMUtils.createDocument();
        MetaImpl meta = new MetaImpl(metaImpl);
        meta.setOwnedWorkbook(this);
        return meta;
    }

    public void setMeta(MetaImpl meta) {
        if (meta == null)
            throw new IllegalArgumentException("Meta is null"); //$NON-NLS-1$
        MetaImpl oldMeta = this.meta;
        this.meta = meta;
        if (oldMeta != null) {
            oldMeta.setOwnedWorkbook(null);
        }
        meta.setOwnedWorkbook(this);
    }

    protected WorkbookMarkerRefCounter getMarkerRefCounter() {
        if (markerRefCounter == null)
            markerRefCounter = new WorkbookMarkerRefCounter(
                    (MarkerSheetImpl) getMarkerSheet(),
                    (ManifestImpl) getManifest());
        return markerRefCounter;
    }

    protected WorkbookStyleRefCounter getStyleRefCounter() {
        if (styleRefCounter == null) {
            styleRefCounter = new WorkbookStyleRefCounter(
                    (StyleSheetImpl) getStyleSheet(),
                    (ManifestImpl) getManifest());
        }
        return styleRefCounter;
    }

    protected WorkbookComponentRefCounter getElementRefCounter() {
        if (elementRefCounter == null) {
            elementRefCounter = new WorkbookComponentRefCounter(this);
        }
        return elementRefCounter;
    }

    public IRevisionRepository getRevisionRepository() {
        if (revisionRepository == null) {
            revisionRepository = new RevisionRepositoryImpl(this);
        }
        return revisionRepository;
    }

    public ICloneData clone(Collection<? extends Object> sources) {
        return WorkbookUtilsImpl.clone(this, sources, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#importElement(org.xmind.core.IAdaptable)
     */
    public IAdaptable importElement(IAdaptable source) {
        Node node = (Node) source.getAdapter(Node.class);
        if (node == null)
            return null;
        Node ele = getImplementation().importNode(node, true);
        return getAdaptableRegistry().getAdaptable(ele);
    }

    public IResourceRef createResourceRef(String resourceType,
            String resourceId) {
        Element ele = implementation.createElement(TAG_RESOURCE_REF);
        ele.setAttribute(ATTR_TYPE, resourceType);
        ele.setAttribute(ATTR_RESOURCE_ID, resourceId);
        ResourceRefImpl ref = new ResourceRefImpl(ele, this);
        getAdaptableRegistry().registerByNode(ref, ele);
        return ref;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#findElementById(java.lang.String,
     * org.xmind.core.IAdaptable)
     */
    public Object findElement(String id, IAdaptable source) {
        Node node = source == null ? null
                : (Node) source.getAdapter(Node.class);
        if (node == null)
            node = getImplementation();
        return getAdaptableRegistry().getAdaptable(id,
                DOMUtils.getOwnerDocument(node));
    }

    public IAdaptable createAdaptable(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            String tagName = e.getNodeName();
            if (TAG_SHEET.equals(tagName)) {
                return new SheetImpl(e, this);
            } else if (TAG_TOPIC.equals(tagName)) {
                return new TopicImpl(e, this);
            } else if (TAG_RELATIONSHIP.equals(tagName)) {
                return new RelationshipImpl(e, this);
            } else if (TAG_MARKER_REF.equals(tagName)) {
                return new MarkerRefImpl(e, this);
            } else if (TAG_BOUNDARY.equals(tagName)) {
                return new BoundaryImpl(e, this);
            } else if (TAG_SUMMARY.equals(tagName)) {
                return new SummaryImpl(e, this);
            } else if (TAG_RESOURCE_REF.equals(tagName)) {
                return new ResourceRefImpl(e, this);
            }
            Node p = node.getParentNode();
            if (p != null && p instanceof Element) {
                String parentName = p.getNodeName();
                if (DOMConstants.TAG_NOTES.equals(parentName)) {
                    String format = tagName;
                    if (INotes.HTML.equals(format)) {
                        return new HtmlNotesContentImpl(e, this);
                    } else if (INotes.PLAIN.equals(format)) {
                        return new PlainNotesContentImpl(e, this);
                    }
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        String oldPassword = this.password;
        this.password = password;
        fireValueChange(Core.PasswordChange, oldPassword, this.password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param type
     * @param oldValue
     * @param newValue
     */
    private void fireValueChange(String type, Object oldValue,
            Object newValue) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null)
            coreEventSupport.dispatchValueChange(this, type, oldValue,
                    newValue);
    }

    public ICoreEventSupport getCoreEventSupport() {
        if (coreEventSupport == null)
            coreEventSupport = new CoreEventSupport();
        return coreEventSupport;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    public ICoreEventRegistration registerOnceCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerOnceCoreEventListener(this, type,
                listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.event.ICoreEventSource2#hasOnceListeners(java.lang.String)
     */
    public boolean hasOnceListeners(String type) {
        return coreEventSupport != null
                && coreEventSupport.hasOnceListeners(this, type);
    }

    private void fireIndexedTargetChange(String type, Object target,
            int index) {
        if (coreEventSupport != null) {
            coreEventSupport.dispatchIndexedTargetChange(this, type, target,
                    index);
        }
    }

    /**
     * 
     */
    private void fireTargetChange(String type, Object target) {
        if (coreEventSupport != null) {
            coreEventSupport.dispatchTargetChange(this, type, target);
        }
    }

    private void save(ISaveable runnable) throws IOException, CoreException {
        saveVersion();
        fireTargetChange(Core.WorkbookPreSaveOnce, this);
        fireTargetChange(Core.WorkbookPreSave, this);
        runnable.run();
        fireTargetChange(Core.WorkbookSave, this);
    }

    /**
     * @see org.xmind.core.IWorkbook#save()
     */
    public synchronized void save() throws IOException, CoreException {
        save(new ISaveable() {
            public void run() throws IOException, CoreException {
                saver.save();
            }
        });
    }

    /**
     * @see org.xmind.core.IWorkbook#save(java.lang.String)
     */
    public synchronized void save(final String file)
            throws IOException, CoreException {
        if (file == null)
            throw new IllegalArgumentException();

        save(new ISaveable() {
            public void run() throws IOException, CoreException {
                saver.save(file);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#save(org.xmind.core.io.IOutputTarget)
     */
    public synchronized void save(final IOutputTarget target)
            throws IOException, CoreException {
        if (target == null)
            throw new IllegalArgumentException();

        save(new ISaveable() {
            public void run() throws IOException, CoreException {
                saver.save(target);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#save(java.io.OutputStream)
     */
    public synchronized void save(final OutputStream output)
            throws IOException, CoreException {
        if (output == null)
            throw new IllegalArgumentException();

        save(new ISaveable() {
            public void run() throws IOException, CoreException {
                saver.save(output);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#setTempArchive(org.xmind.core.io.IArchive)
     */
    public void setTempStorage(IStorage storage) {
        if (storage == null)
            throw new IllegalArgumentException();
        tempSaver.setStorage(storage);
    }

    /**
     * @return the tempArchive
     */
    public IStorage getTempStorage() {
        return tempSaver.getStorage();
    }

    public String getFile() {
        return saver.getFile();
    }

    public void setFile(String file) {
        saver.setFile(file);
    }

    public void setSkipRevisionsWhenSaving(boolean skipRevisions) {
        saver.setSkipRevisions(skipRevisions);
    }

    public boolean isSkipRevisionsWhenSaving() {
        return saver.isSkipRevisions();
    }

    public String getTempLocation() {
        IStorage storage = getTempStorage();
        return storage instanceof DirectoryStorage
                ? ((DirectoryStorage) storage).getFullPath() : null;
    }

    public void setTempLocation(String tempLocation) {
        if (tempLocation == null)
            throw new IllegalArgumentException();

        setTempStorage(new DirectoryStorage(new File(tempLocation)));
    }

    public void saveTemp() throws IOException, CoreException {
        tempSaver.save();
    }

    public long getModifiedTime() {
        return InternalDOMUtils.getModifiedTime(this, getWorkbookElement());
    }

    public String getModifiedBy() {
        return InternalDOMUtils.getModifiedBy(this, getWorkbookElement());
    }

    protected void updateModificationInfo() {
        InternalDOMUtils.updateModificationInfo(this);
    }

    public void setCreator(String name, String version) {
        this.creatorName = name;
        this.creatorVersion = version;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorVersion() {
        return creatorVersion;
    }

    public ICommentManager getCommentManager() {
        if (commentManager == null) {
            commentManager = createCommentManager();
        }
        return commentManager;
    }

    private CommentManagerImpl createCommentManager() {
        CommentManagerImpl commentManager = (CommentManagerImpl) Core
                .getCommentManagerBuilder().createCommentManager();
        commentManager.setOwnedWorkbook(this);
        return commentManager;
    }

    public void setCommentManager(CommentManagerImpl commentManager) {
        this.commentManager = commentManager;
        commentManager.setOwnedWorkbook(this);
    }

}
