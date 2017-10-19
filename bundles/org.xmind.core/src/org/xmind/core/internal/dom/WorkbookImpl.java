/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_VERSION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUMMARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;
import static org.xmind.core.internal.dom.DOMConstants.TAG_WORKBOOK;
import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.FileOutputStream;
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
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.IResourceRef;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISerializer;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbookComponentRefManager;
import org.xmind.core.IWorkbookExtensionManager;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Workbook;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.IMarkerRefCounter;
import org.xmind.core.util.IStyleRefCounter;

/**
 * @author briansun
 */
public class WorkbookImpl extends Workbook
        implements ICoreEventSource, ICoreEventSource2, INodeAdaptableFactory {

    private Document implementation;

    private ManifestImpl manifest;

    private NodeAdaptableRegistry adaptableRegistry;

    private CoreEventSupport coreEventSupport = null;

    private StyleSheetImpl styleSheet = null;

    private MarkerSheetImpl markerSheet = null;

    private MetaImpl meta = null;

    private CommentManagerImpl commentManager = null;

    private WorkbookMarkerRefCounter markerRefCounter = null;

    private WorkbookStyleRefCounter styleRefCounter = null;

    private WorkbookComponentRefCounter elementRefCounter = null;

    private RevisionRepositoryImpl revisionRepository = null;

    private WorkbookExtensionManagerImpl extensionManager;

    /**
     * @param implementation
     */
    public WorkbookImpl(Document implementation, IStorage storage) {
        this(implementation,
                new ManifestImpl(DOMUtils.createDocument(), storage));
    }

    /**
     * @param implementation
     * @param manifest
     */
    public WorkbookImpl(Document implementation, ManifestImpl manifest) {
        this.implementation = implementation;
        this.manifest = manifest;
        this.adaptableRegistry = new NodeAdaptableRegistry(implementation,
                this);
        init();
    }

    private void init() {
        manifest.setWorkbook(this);
        manifest.createFileEntry(CONTENT_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();
        manifest.createFileEntry(META_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();

        Element w = DOMUtils.ensureChildElement(implementation, TAG_WORKBOOK);
        NS.setNS(NS.XMAP, w, NS.Xhtml, NS.Xlink, NS.SVG, NS.Fo);
        if (!DOMUtils.childElementIterByTag(w, TAG_SHEET).hasNext())
            addSheet(createSheet());
        InternalDOMUtils.addVersion(implementation);

        ICoreEventListener eventHook = new ICoreEventListener() {
            public void handleCoreEvent(CoreEvent event) {
                handleMarkerSheetEvent(event);
            }
        };
        ICoreEventSupport eventSupport = getCoreEventSupport();
        eventSupport.registerGlobalListener(Core.MarkerAdd, eventHook);
        eventSupport.registerGlobalListener(Core.MarkerRemove, eventHook);
        eventSupport.registerGlobalListener(Core.MarkerGroupAdd, eventHook);
        eventSupport.registerGlobalListener(Core.MarkerGroupRemove, eventHook);
    }

    /**
     * <b>NOTE</b>: This is not a public API.
     * 
     * @return the implementation
     */
    public Document getImplementation() {
        return implementation;
    }

    /**
     * <b>NOTE</b>: This is not a public API.
     * 
     * @return the storage
     */
    public IStorage getStorage() {
        return manifest.getStorage();
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
        if (getStorage() != null) {
            return "Workbook{" + hashCode() + ";path:" //$NON-NLS-1$//$NON-NLS-2$
                    + getStorage().getFullPath() + "}"; //$NON-NLS-1$
        }
        return "Workbook{" + hashCode() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (IStorage.class.equals(adapter))
            return adapter.cast(getStorage());
        if (IEntryStreamNormalizer.class.equals(adapter))
            return adapter.cast(manifest.getStreamNormalizer());
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Document.class))
            return adapter.cast(implementation);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getWorkbookElement());
        if (IMarkerSheet.class.equals(adapter))
            return adapter.cast(getMarkerSheet());
        if (IManifest.class.equals(adapter))
            return adapter.cast(getManifest());
        if (ICoreEventSupport.class.equals(adapter))
            return adapter.cast(getCoreEventSupport());
        if (INodeAdaptableFactory.class.equals(adapter))
            return adapter.cast(this);
        if (INodeAdaptableProvider.class.equals(adapter))
            return adapter.cast(getAdaptableRegistry());
        if (IMarkerRefCounter.class.equals(adapter))
            return adapter.cast(getMarkerRefCounter());
        if (IStyleRefCounter.class.equals(adapter))
            return adapter.cast(getStyleRefCounter());
        if (IWorkbookComponentRefManager.class.equals(adapter))
            return adapter.cast(getElementRefCounter());
        if (IRevisionRepository.class.equals(adapter))
            return adapter.cast(getRevisionRepository());
        if (IWorkbookExtensionManager.class.equals(adapter))
            return adapter.cast(getWorkbookExtensionManager());
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

    /**
     * @see org.xmind.core.IWorkbook#getSheets()
     */
    public List<ISheet> getSheets() {
        return DOMUtils.getChildList(getWorkbookElement(), TAG_SHEET,
                getAdaptableRegistry());
    }

    public SheetImpl getPrimarySheet() {
        Element e = DOMUtils.getFirstChildElementByTag(getWorkbookElement(),
                TAG_SHEET);
        if (e != null)
            return (SheetImpl) getAdaptableRegistry().getAdaptable(e);
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

    public StyleSheetImpl getStyleSheet() {
        if (styleSheet == null)
            setStyleSheet(createStyleSheet());
        return styleSheet;
    }

    public void setStyleSheet(StyleSheetImpl styleSheet) {
        StyleSheetImpl oldStyleSheet = this.styleSheet;
        if (styleSheet == oldStyleSheet)
            return;

        if (oldStyleSheet != null) {
            oldStyleSheet.getCoreEventSupport().setParent(null);
        }
        this.styleSheet = styleSheet;
        if (styleSheet != null) {
            styleSheet.getCoreEventSupport().setParent(getCoreEventSupport());
        }
    }

    protected StyleSheetImpl createStyleSheet() {
        StyleSheetImpl ss = (StyleSheetImpl) Core.getStyleSheetBuilder()
                .createStyleSheet();
        getManifest().createFileEntry(STYLES_XML, Core.MEDIA_TYPE_TEXT_XML)
                .increaseReference();
        ss.setManifest(getManifest());
        return ss;
    }

    public ManifestImpl getManifest() {
        return manifest;
    }

    public MarkerSheetImpl getMarkerSheet() {
        if (markerSheet == null)
            setMarkerSheet(createMarkerSheet());
        return markerSheet;
    }

    protected MarkerSheetImpl createMarkerSheet() {
        MarkerSheetImpl ms = (MarkerSheetImpl) Core.getMarkerSheetBuilder()
                .createMarkerSheet(new WorkbookMarkerResourceProvider(this));
        ms.setManifest(getManifest());
        return ms;
    }

    public void setMarkerSheet(MarkerSheetImpl markerSheet) {
        MarkerSheetImpl oldMarkerSheet = this.markerSheet;
        if (markerSheet == oldMarkerSheet)
            return;

        if (oldMarkerSheet != null) {
            oldMarkerSheet.getCoreEventSupport().setParent(null);
            for (IMarkerGroup oldMarkerGroup : oldMarkerSheet
                    .getMarkerGroups()) {
                handleMarkerGroupManagement(oldMarkerGroup, false);
            }
        }
        this.markerSheet = markerSheet;
        if (markerSheet != null) {
            for (IMarkerGroup newMarkerGroup : markerSheet.getMarkerGroups()) {
                handleMarkerGroupManagement(newMarkerGroup, true);
            }
            markerSheet.getCoreEventSupport().setParent(getCoreEventSupport());
        }
    }

    public MetaImpl getMeta() {
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
            markerRefCounter = new WorkbookMarkerRefCounter(this);
        return markerRefCounter;
    }

    protected WorkbookStyleRefCounter getStyleRefCounter() {
        if (styleRefCounter == null) {
            styleRefCounter = new WorkbookStyleRefCounter(
                    (StyleSheetImpl) getStyleSheet(), manifest);
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
     * @deprecated
     */
    @Deprecated
    public synchronized void save() throws IOException, CoreException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public synchronized void save(final String file)
            throws IOException, CoreException {
        FileOutputStream stream = new FileOutputStream(file);
        try {
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(this);
            serializer.setOutputStream(stream);
            serializer.serialize(null);
        } finally {
            stream.close();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public synchronized void save(final IOutputTarget target)
            throws IOException, CoreException {
        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(this);
        serializer.setOutputTarget(target);
        serializer.serialize(null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public synchronized void save(final OutputStream output)
            throws IOException, CoreException {
        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(this);
        serializer.setOutputStream(output);
        serializer.serialize(null);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setTempStorage(IStorage storage) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public IStorage getTempStorage() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getFile() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setFile(String file) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isSkipRevisionsWhenSaving() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getTempLocation() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setTempLocation(String tempLocation) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbook#setPassword(java.lang.String)
     */
    @Deprecated
    public void setPassword(String password) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbook#getPassword()
     */
    @Deprecated
    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void saveTemp() throws IOException, CoreException {
        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(this);
        serializer.setWorkbookStorageAsOutputTarget();
        serializer.serialize(null);
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

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbook#getCommentManager()
     */
    public org.xmind.core.ICommentManager getCommentManager() {
        if (commentManager == null) {
            commentManager = createCommentManager();
        }
        return commentManager;
    }

    /**
     * @return
     */
    private CommentManagerImpl createCommentManager() {
        return new CommentManagerImpl(this, DOMUtils.createDocument());
    }

    public void setCommentManager(CommentManagerImpl commentManager) {
        this.commentManager = commentManager;
    }

    private void handleMarkerSheetEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.MarkerAdd.equals(type)) {
            handleMarkerManagement((IMarker) event.getTarget(), true);
        } else if (Core.MarkerRemove.equals(type)) {
            handleMarkerManagement((IMarker) event.getTarget(), false);
        } else if (Core.MarkerGroupAdd.equals(type)) {
            handleMarkerGroupManagement((IMarkerGroup) event.getTarget(), true);
        } else if (Core.MarkerGroupRemove.equals(type)) {
            handleMarkerGroupManagement((IMarkerGroup) event.getTarget(),
                    false);
        }
    }

    /**
     * @param marker
     */
    private void handleMarkerManagement(IMarker marker, boolean added) {
        String path = marker.getResourcePath();
        if (path == null || "".equals(path)) //$NON-NLS-1$
            return;

        IFileEntry entry = getManifest()
                .getFileEntry(ArchiveConstants.PATH_MARKERS + path);
        if (entry == null)
            return;

        if (added) {
            entry.increaseReference();
        } else {
            entry.decreaseReference();
        }
    }

    private void handleMarkerGroupManagement(IMarkerGroup group,
            boolean added) {
        for (IMarker marker : group.getMarkers()) {
            handleMarkerManagement(marker, added);
        }
    }

    private IWorkbookExtensionManager getWorkbookExtensionManager() {
        if (extensionManager == null) {
            extensionManager = new WorkbookExtensionManagerImpl();
            extensionManager.setOwnedWorkbook(this);
        }
        return extensionManager;
    }

}
