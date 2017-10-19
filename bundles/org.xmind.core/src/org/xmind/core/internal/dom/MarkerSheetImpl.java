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
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SINGLETON;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_GROUP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_SHEET;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IManifest;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.internal.MarkerSheet;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipFileInputSource;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.IInputSource;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceAllocator;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IPropertiesProvider;

public class MarkerSheetImpl extends MarkerSheet implements
        INodeAdaptableFactory, ICoreEventSource, IPropertiesProvider {

    private Document implementation;

    private IMarkerResourceProvider realResourceProvider;

    private ElementRegistry elementRegistry = null;

    private NodeAdaptableProvider elementAdaptableProvider = null;

    private CoreEventSupport coreEventSupport = null;

    private Properties properties = null;

    private ManifestImpl manifest = null;

    public MarkerSheetImpl(Document implementation,
            IMarkerResourceProvider resourceProvider) {
        this.implementation = implementation;
        this.realResourceProvider = resourceProvider;
        init();
    }

    private void init() {
        Element m = DOMUtils.ensureChildElement(implementation,
                TAG_MARKER_SHEET);
        NS.setNS(NS.Marker, m);
        InternalDOMUtils.addVersion(implementation);
    }

    public Document getImplementation() {
        return implementation;
    }

    public Element getSheetElement() {
        return implementation.getDocumentElement();
    }

    /**
     * @param manifest
     *            the manifest to set
     */
    public void setManifest(ManifestImpl manifest) {
        this.manifest = manifest;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (IManifest.class.equals(adapter))
            return adapter.cast(manifest);
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (IMarkerResourceProvider.class.equals(adapter))
            return adapter.cast(realResourceProvider);
        if (ICoreEventSupport.class.equals(adapter))
            return adapter.cast(getCoreEventSupport());
        if (IPropertiesProvider.class.equals(adapter))
            return adapter.cast(this);
        if (Properties.class.equals(adapter))
            return adapter.cast(getProperties());
        if (ElementRegistry.class.equals(adapter))
            return adapter.cast(getElementRegistry());
        if (INodeAdaptableFactory.class.equals(adapter))
            return adapter.cast(this);
        if (INodeAdaptableProvider.class.equals(adapter))
            return adapter.cast(getElementAdapterProvider());
        if (adapter.isAssignableFrom(Document.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    protected IMarkerResource getMarkerResource(IMarker marker) {
        if (realResourceProvider != null)
            return realResourceProvider.getMarkerResource(marker);
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MarkerSheetImpl))
            return false;
        MarkerSheetImpl that = (MarkerSheetImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(implementation);
    }

    public boolean isPermanent() {
        return realResourceProvider == null
                || realResourceProvider.isPermanent();
    }

    public IMarker createMarker(String resourcePath) {
        Element markerImpl = implementation.createElement(TAG_MARKER);
        DOMUtils.setAttribute(markerImpl, ATTR_RESOURCE, resourcePath);
        MarkerImpl marker = new MarkerImpl(markerImpl, this);
        getElementRegistry().register(marker);
        return marker;
    }

    public IMarker createMarkerById(String markerId, String resourcePath) {
        Element markerImpl = implementation.createElement(TAG_MARKER);
        DOMUtils.setAttribute(markerImpl, ATTR_RESOURCE, resourcePath);
        MarkerImpl marker = new MarkerImpl(markerImpl, this);
        replaceId(marker, markerId);
        getElementRegistry().register(marker);
        return marker;
    }

    public IMarkerGroup createMarkerGroupById(String groupId) {
        Element groupImpl = implementation.createElement(TAG_MARKER_GROUP);
        MarkerGroupImpl group = new MarkerGroupImpl(groupImpl, this);
        replaceId(group, groupId);
        getElementRegistry().register(group);
        return group;
    }

    public IMarkerGroup createMarkerGroup(boolean singleton) {
        Element groupImpl = implementation.createElement(TAG_MARKER_GROUP);
        if (singleton)
            groupImpl.setAttribute(ATTR_SINGLETON, Boolean.toString(singleton));
        MarkerGroupImpl group = new MarkerGroupImpl(groupImpl, this);
        getElementRegistry().register(group);
        return group;
    }

    protected IMarker getLocalMarker(String markerId) {
        Object element = getElementById(markerId);
        if (element != null && element instanceof IMarker)
            return (IMarker) element;
        return null;
    }

    protected IMarkerGroup getLocalMarkerGroup(String groupId) {
        Object element = getElementById(groupId);
        if (element != null && element instanceof IMarkerGroup)
            return (IMarkerGroup) element;
        return null;
    }

    public List<IMarkerGroup> getMarkerGroups() {
        return DOMUtils.getChildList(getSheetElement(), TAG_MARKER_GROUP,
                getElementAdapterProvider());
    }

    public void addMarkerGroup(IMarkerGroup group) {
        Element g = ((MarkerGroupImpl) group).getImplementation();
        Element s = getSheetElement();
        Node n = s.appendChild(g);
        if (n != null) {
            int index = DOMUtils.getElementIndex(s, TAG_MARKER_GROUP, g);
            if (index >= 0) {
                fireIndexedTargetChange(Core.MarkerGroupAdd, group, index);
            }
        }
    }

    public void removeMarkerGroup(IMarkerGroup group) {
        Element g = ((MarkerGroupImpl) group).getImplementation();
        Element s = getSheetElement();
        if (g.getParentNode() == s) {
            int index = DOMUtils.getElementIndex(s, TAG_MARKER_GROUP, g);
            if (index >= 0) {
                Node n = s.removeChild(g);
                if (n != null) {
                    fireIndexedTargetChange(Core.MarkerGroupRemove, group,
                            index);
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return !getSheetElement().hasChildNodes();
    }

    protected Object getElementById(String id) {
        Object element = getElementRegistry().getElement(id);
        if (element == null) {
            Element domElement = implementation.getElementById(id);
            if (domElement != null) {
                element = getElementAdapter(domElement);
            }
        }
        return element;
    }

    public ElementRegistry getElementRegistry() {
        if (elementRegistry == null)
            elementRegistry = new ElementRegistry();
        return elementRegistry;
    }

    protected NodeAdaptableProvider getElementAdapterProvider() {
        if (elementAdaptableProvider == null)
            elementAdaptableProvider = new NodeAdaptableProvider(
                    getElementRegistry(), this, implementation);
        return elementAdaptableProvider;
    }

    protected IAdaptable getElementAdapter(Node node) {
        return getElementAdapterProvider().getAdaptable(node);
    }

    public IAdaptable createAdaptable(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            String tagName = e.getTagName();
            if (TAG_MARKER_GROUP.equals(tagName)) {
                return new MarkerGroupImpl(e, this);
            } else if (TAG_MARKER.equals(tagName)) {
                return new MarkerImpl(e, this);
            }
        }
        return null;
    }

    @Deprecated
    public void save(OutputStream out) throws IOException, CoreException {
        DOMUtils.save(implementation, out, false);
    }

    private static final String OLD_MARKERLISTS_XML = "markerlists.xml"; //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static final String OLD_TAG_MARKER_LISTS = "markerLists"; //$NON-NLS-1$
    private static final String OLD_TAG_MARKER_LIST = "markerList"; //$NON-NLS-1$
    private static final String OLD_TAG_MARKER = "marker"; //$NON-NLS-1$
    private static final String OLD_ATT_NAME = "name"; //$NON-NLS-1$
    private static final String OLD_ATT_ID = "id"; //$NON-NLS-1$
    private static final String OLD_ATT_FILE = "file"; //$NON-NLS-1$

    @Deprecated
    public void importFrom(String sourcePath)
            throws IOException, CoreException {
        File sourceFile = new File(sourcePath);
        IInputSource source;
        if (sourceFile.isDirectory()) {
            source = new DirectoryInputSource(sourceFile);
        } else {
            source = new ZipFileInputSource(new ZipFile(sourceFile));
        }
        try {
            importFrom(source, new File(sourcePath).getName());
        } finally {
            if (source instanceof ZipFileInputSource) {
                ((ZipFileInputSource) source).closeZipFile();
            }
        }
    }

    @Deprecated
    public void importFrom(IInputSource source)
            throws IOException, CoreException {
        importFrom(source, null);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.marker.IMarkerSheet#importFrom(org.xmind.core.io.
     * IInputSource , java.lang.String)
     */
    @Deprecated
    public void importFrom(IInputSource source, String groupName)
            throws IOException, CoreException {
        if (source.hasEntry(ArchiveConstants.MARKER_SHEET_XML)) {
            InputStream is = source
                    .openEntryStream(ArchiveConstants.MARKER_SHEET_XML);
            IMarkerSheet sourceMarkerSheet = Core.getMarkerSheetBuilder()
                    .loadFromStream(is,
                            new MarkerResourceProvider(source, null));
            importFrom(sourceMarkerSheet);
        } else if (source.hasEntry(OLD_MARKERLISTS_XML)) {
            importFromOldMarkerSheet(source);
        } else {
            importAllAsNewGroup(source, groupName);
        }
    }

    @Deprecated
    private void importAllAsNewGroup(IInputSource source, String groupName)
            throws IOException {
        IMarkerGroup group = createMarkerGroup(false);
        if (groupName == null) {
            groupName = createGroupName();
        }
        group.setName(groupName);
        Iterator<String> entries = source.getEntries();
        while (entries.hasNext()) {
            String entry = entries.next();
            InputStream is = source.openEntryStream(entry);
            if (is != null) {
                IMarker marker = createMarker(entry);
                marker.setName(FileUtils.getFileName(entry));
                group.addMarker(marker);
                IMarkerResource resource = marker.getResource();
                if (resource != null) {
                    OutputStream os = resource.openOutputStream();
                    if (os != null) {
                        try {
                            FileUtils.transfer(is, os, true);
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        }
        addMarkerGroup(group);
    }

    private String createGroupName() {
        return "Group " + (getMarkerGroups().size() + 1); //$NON-NLS-1$
        //return Core.getIdFactory().createId();
    }

    @Deprecated
    private void importFromOldMarkerSheet(IInputSource source)
            throws IOException {
        InputStream is = source.openEntryStream(OLD_MARKERLISTS_XML);
        if (is == null)
            throw new FileNotFoundException();
        Document document = DOMUtils.loadDocument(is);
        Iterator<Element> listIt = DOMUtils.childElementIterByTag(
                document.getDocumentElement(), OLD_TAG_MARKER_LIST);
        while (listIt.hasNext()) {
            Element listEle = listIt.next();
            String listId = listEle.getAttribute(OLD_ATT_ID);
            IMarkerGroup existingGroup = findMarkerGroup(listId);
            if (existingGroup != null) {
                importGroupFromOld(source, listEle, existingGroup);
            } else {
                IMarkerGroup newGroup = createMarkerGroup(false);
                replaceId(newGroup, listId);
                addMarkerGroup(newGroup);
                importGroupFromOld(source, listEle, newGroup);
            }
        }

    }

    private void replaceId(IAdaptable adaptable, String newId) {
        Element ele = (Element) adaptable.getAdapter(Element.class);
        if (ele == null)
            return;
        String oldId = ele.getAttribute(DOMConstants.ATTR_ID);
        getElementRegistry().unregisterByKey(oldId);
        ele.setAttribute(DOMConstants.ATTR_ID, newId);
        getElementRegistry().registerByKey(newId, adaptable);
    }

    @Deprecated
    private void importGroupFromOld(IInputSource source, Element listEle,
            IMarkerGroup targetGroup) throws IOException {
        targetGroup.setName(listEle.getAttribute(OLD_ATT_NAME));
        Iterator<Element> markerIt = DOMUtils.childElementIterByTag(listEle,
                OLD_TAG_MARKER);
        while (markerIt.hasNext()) {
            Element markerEle = markerIt.next();
            String file = DOMUtils.getAttribute(markerEle, OLD_ATT_FILE);
            if (file != null) {
                String markerId = markerEle.getAttribute(OLD_ATT_ID);
                IMarker targetMarker = findMarker(markerId);
                if (targetMarker == null) {
                    targetMarker = targetGroup.getOwnedSheet().createMarker(
                            createGroupName() + FileUtils.getExtension(file));
                    replaceId(targetMarker, markerId);
                    targetGroup.addMarker(targetMarker);
                }
                targetMarker.setName(markerEle.getAttribute(OLD_ATT_NAME));
                String oldEntryName = listEle.getAttribute(OLD_ATT_ID) + "/" //$NON-NLS-1$
                        + file;
                IMarkerResource newRes = targetMarker.getResource();
                if (newRes != null) {
                    OutputStream os = newRes.openOutputStream();
                    if (os != null) {
                        if (source.hasEntry(oldEntryName)) {
                            InputStream mis = source
                                    .openEntryStream(oldEntryName);
                            FileUtils.transfer(mis, os, true);
                        }
                    }
                }
            }
        }
    }

    @Deprecated
    public void importFrom(IMarkerSheet sheet) {
        try {
            importFromChecked(sheet);
        } catch (Exception e) {
            Core.getLogger().log(e);
        }
    }

    @Deprecated
    public void importFromChecked(IMarkerSheet sheet)
            throws IOException, CoreException {
        for (IMarkerGroup group : sheet.getMarkerGroups()) {
            importGroupChecked(group);
        }
    }

    @Deprecated
    public IMarkerGroup importGroup(IMarkerGroup group) {
        try {
            return importGroupChecked(group);
        } catch (Exception e) {
            Core.getLogger().log(e);
            return null;
        }
    }

    @Deprecated
    public IMarkerGroup importGroupChecked(IMarkerGroup group)
            throws IOException, CoreException {
        String id = group.getId();
        IMarkerGroup existingGroup = findMarkerGroup(id);
        if (existingGroup != null) {
            existingGroup.setName(group.getName());
            importGroup(group, existingGroup);
            if (existingGroup.getParent() == null)
                addMarkerGroup(existingGroup);
            return existingGroup;
        }

        IMarkerGroup targetGroup = createMarkerGroup(group.isSingleton());
        replaceId(targetGroup, group.getId());
        targetGroup.setName(group.getName());
        importGroup(group, targetGroup);
        addMarkerGroup(targetGroup);
        return targetGroup;
    }

    @Deprecated
    private void importGroup(IMarkerGroup sourceGroup, IMarkerGroup targetGroup)
            throws IOException {
        for (IMarker sourceMarker : sourceGroup.getMarkers()) {
            String id = sourceMarker.getId();
            IMarker targetMarker = getLocalMarker(id);
            if (targetMarker == null
                    || !targetGroup.equals(targetMarker.getParent())) {
                targetMarker = targetGroup.getOwnedSheet()
                        .createMarker(sourceMarker.getResourcePath());
                replaceId(targetMarker, sourceMarker.getId());
                targetGroup.addMarker(targetMarker);
            }
            targetMarker.setName(sourceMarker.getName());
            IMarkerResource sourceRes = sourceMarker.getResource();
            if (sourceRes != null) {
                InputStream is = sourceRes.openInputStream();
                IMarkerResource targetRes = targetMarker.getResource();
                if (targetRes != null) {
                    OutputStream os = targetRes.openOutputStream();
                    FileUtils.transfer(is, os, true);
                }
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    public CoreEventSupport getCoreEventSupport() {
        if (coreEventSupport != null)
            return coreEventSupport;

        coreEventSupport = new CoreEventSupport();
        return coreEventSupport;
    }

    private void fireIndexedTargetChange(String type, Object target,
            int index) {
        getCoreEventSupport().dispatchIndexedTargetChange(this, type, target,
                index);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.marker.IMarkerSheet#createMarkerResourcePath(java.io.
     * InputStream, java.lang.String)
     */
    public String allocateMarkerResource(InputStream source,
            String suggestedPath) throws IOException {
        if (realResourceProvider != null
                && realResourceProvider instanceof IMarkerResourceAllocator) {
            return ((IMarkerResourceAllocator) realResourceProvider)
                    .allocateMarkerResource(source, suggestedPath);
        }
        return null;
    }

}
