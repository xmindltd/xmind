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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_FULL_PATH;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MEDIA_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.PASSWORD_HINT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_FILE_ENTRY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MANIFEST;
import static org.xmind.core.internal.dom.InternalDOMUtils.getParentPath;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IFileEntryFilter;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.internal.Manifest;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;

public class ManifestImpl extends Manifest implements ICoreEventSource {

    private Document implementation;

    private IStorage storage;

    private WorkbookImpl ownedWorkbook;

    private final Map<String, IFileEntry> entries;

    private IEntryStreamNormalizer normalizer;

    private CoreEventSupport coreEventSupport;

    public ManifestImpl(Document implementation, IStorage storage) {
        this.implementation = implementation;
        this.storage = storage == null ? new ByteArrayStorage() : storage;
        this.normalizer = IEntryStreamNormalizer.NULL;
        this.ownedWorkbook = null;
        this.entries = new HashMap<String, IFileEntry>();
        init();
    }

    private void init() {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_MANIFEST);
        NS.setNS(NS.Manifest, m);

        // Prefetch all file entries, which makes getAllRegisteredEntries() 
        // always returns correct value.
        for (Iterator<IFileEntry> it = iterFileEntries(); it.hasNext();) {
            it.next();
        }

        IFileEntry metaEntry = createFileEntry(MANIFEST_XML,
                Core.MEDIA_TYPE_TEXT_XML);
        insertFileEntryImpl(metaEntry.getAdapter(Element.class));
    }

    public Document getImplementation() {
        return implementation;
    }

    public Element getManifestElement() {
        return implementation.getDocumentElement();
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Document.class))
            return adapter.cast(implementation);
        if (IWorkbook.class.equals(adapter))
            return adapter.cast(ownedWorkbook);
        return super.getAdapter(adapter);
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return ownedWorkbook == null;
    }

    /**
     * @param workbook
     */
    protected void setWorkbook(WorkbookImpl workbook) {
        this.ownedWorkbook = workbook;
        if (workbook != null) {
            getCoreEventSupport().setParent(workbook.getCoreEventSupport());
        }
    }

    protected void setStorage(IStorage storage) {
        this.storage = storage == null ? new ByteArrayStorage() : storage;
    }

    protected IStorage getStorage() {
        return storage;
    }

    protected void setStreamNormalizer(IEntryStreamNormalizer normalizer) {
        this.normalizer = normalizer == null ? IEntryStreamNormalizer.NULL
                : normalizer;
    }

    protected IEntryStreamNormalizer getStreamNormalizer() {
        return this.normalizer;
    }

    protected Collection<IFileEntry> getAllRegisteredEntries() {
        return entries.values();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IManifest#iterFileEntries()
     */
    public Iterator<IFileEntry> iterFileEntries() {
        return iterFileEntries(null);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IManifest#iterFileEntries(org.xmind.core.IManifest.
     * IFileEntryFilter)
     */
    public Iterator<IFileEntry> iterFileEntries(final IFileEntryFilter filter) {
        final Iterator<Element> it = DOMUtils
                .childElementIterByTag(getManifestElement(), TAG_FILE_ENTRY);
        return new Iterator<IFileEntry>() {

            IFileEntry next = findNext();

            private IFileEntry findNext() {
                while (it.hasNext()) {
                    Element e = it.next();
                    if (e.hasAttribute(ATTR_FULL_PATH) && select(e)) {
                        return getFileEntry(e);
                    }
                }
                return null;
            }

            private boolean select(Element e) {
                if (filter == null)
                    return true;

                String path = e.getAttribute(ATTR_FULL_PATH);
                String mediaType = e.getAttribute(ATTR_MEDIA_TYPE);
                boolean isDirectory = path.endsWith("/"); //$NON-NLS-1$
                return filter.select(path, mediaType, isDirectory);
            }

            public void remove() {
            }

            public IFileEntry next() {
                IFileEntry n = next;
                next = findNext();
                return n;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

    public IFileEntry getFileEntry(String path) {
        if (path == null)
            return null;
        IFileEntry entry = findEntry(path);
        if (entry == null) {
            while (path.startsWith("/")) { //$NON-NLS-1$
                path = path.substring(1, path.length());
                entry = findEntry(path);
                if (entry != null)
                    break;
            }
        }
        if (entry == null) {
            entry = findEntry("/" + path); //$NON-NLS-1$
        }
        return entry;
    }

    private IFileEntry findEntry(String path) {
        IFileEntry entry = entries.get(path);
        if (entry == null) {
            Element e = findEntryElementByPath(path);
            if (e != null)
                entry = createFileEntry(path, e);
        }
        return entry;
    }

    private Element findEntryElementByPath(String path) {
        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getManifestElement(), TAG_FILE_ENTRY);
        while (it.hasNext()) {
            Element e = it.next();
            if (path.equals(e.getAttribute(ATTR_FULL_PATH)))
                return e;
        }
        return null;
    }

    public IFileEntry createFileEntry(String path) {
        return createFileEntry(path, ""); //$NON-NLS-1$
    }

    public IFileEntry createFileEntry(String path, String mediaType) {
        IFileEntry entry = getFileEntry(path);
        if (entry != null)
            return entry;

        String parent = InternalDOMUtils.getParentPath(path);
        if (parent != null) {
            IFileEntry parentFileEntry = createFileEntry(parent);
            insertFileEntryImpl(parentFileEntry.getAdapter(Element.class));
        }
        Element e = implementation.createElement(TAG_FILE_ENTRY);
        e.setAttribute(ATTR_FULL_PATH, path);
        e.setAttribute(ATTR_MEDIA_TYPE, mediaType);

        return createFileEntry(path, e);
    }

    private IFileEntry createFileEntry(String path, Element entryElement) {
        IFileEntry entry = new FileEntryImpl(entryElement, this);
        entries.put(path, entry);
        return entry;
    }

    private IFileEntry getFileEntry(Element element) {
        String path = element.getAttribute(ATTR_FULL_PATH);
        IFileEntry entry = entries.get(path);
        if (entry != null)
            return entry;
        return createFileEntry(path, element);
    }

    protected void insertFileEntry(IFileEntry entry) {
        Element e = (Element) entry.getAdapter(Element.class);
        if (e != null) {
            insertFileEntryImpl(e);
        }
        getCoreEventSupport().dispatchTargetChange(this, Core.FileEntryAdd,
                entry);
    }

    protected void removeFileEntry(IFileEntry entry) {
        Element e = (Element) entry.getAdapter(Element.class);
        if (e != null) {
            Element m = getManifestElement();
            if (m == e.getParentNode())
                m.removeChild(e);
        }
        getCoreEventSupport().dispatchTargetChange(this, Core.FileEntryRemove,
                entry);
    }

    private void insertFileEntryImpl(Element entryElement) {
        Element e = findInsertLocation(entryElement);
        if (e != null) {
            getManifestElement().insertBefore(entryElement, e);
        } else {
            getManifestElement().appendChild(entryElement);
        }
    }

    private Element findInsertLocation(Element entryElement) {
        if (entryElement.hasAttribute(ATTR_FULL_PATH))
            return findInsertLocation(entryElement,
                    entryElement.getAttribute(ATTR_FULL_PATH));
        return null;
    }

    private Element findInsertLocation(Element entryElement, String path) {
        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getManifestElement(), TAG_FILE_ENTRY);
        while (it.hasNext()) {
            Element e = it.next();
            if (e != entryElement && e.hasAttribute(ATTR_FULL_PATH)) {
                String p = e.getAttribute(ATTR_FULL_PATH);
                if (p != null && path.compareToIgnoreCase(p) < 0) {
                    return e;
                }
            }
        }
        return null;
    }

    public IFileEntry createAttachmentFromFilePath(String sourcePath)
            throws IOException {
        return createAttachmentFromFilePath(sourcePath, null);
    }

    public IFileEntry createAttachmentFromFilePath(String sourcePath,
            String mediaType) throws IOException {
        if (sourcePath == null)
            throw new IllegalArgumentException("Path is null!"); //$NON-NLS-1$
        File file = new File(sourcePath);
        if (!file.exists())
            throw new FileNotFoundException("Source path does not exists."); //$NON-NLS-1$

        if (file.isFile()) {
            IFileEntry entry = createAttachmentFromStream(
                    new FileInputStream(sourcePath), sourcePath, mediaType);
            if (entry != null) {
                entry.setTime(file.lastModified());
            }
            return entry;
        }

        if (file.isDirectory()) {
            String fileName = file.getName();
            String path = makeAttachmentPath(fileName, true);
            if (mediaType == null)
                mediaType = FileUtils.getMediaType(fileName);
            IFileEntry root = createFileEntry(path, mediaType);
            if (root != null) {
                importDirectory(path, file);
            }
            return root;
        }

        throw new IllegalArgumentException(
                "Unknown file type (neither a file nor a directory)"); //$NON-NLS-1$
    }

    protected void importDirectory(String parentPath, File dir)
            throws IOException {
        for (String sub : dir.list()) {
            File f = new File(dir, sub);
            if (f.isFile()) {
                String path = parentPath == null ? sub : parentPath + sub;
                String mediaType = FileUtils.getMediaType(sub);
                IFileEntry e = createFileEntry(path, mediaType);
                if (e != null) {
                    e.setTime(f.lastModified());
                    OutputStream os = e.openOutputStream();
                    try {
                        FileInputStream is = new FileInputStream(f);
                        try {
                            FileUtils.transfer(is, os);
                        } finally {
                            is.close();
                        }
                    } finally {
                        os.close();
                    }
                }
            } else if (f.isDirectory()) {
                String path = parentPath == null ? sub + "/" //$NON-NLS-1$
                        : parentPath + sub + "/"; //$NON-NLS-1$
                importDirectory(path, f);
            }
        }
    }

    public IFileEntry createAttachmentFromStream(InputStream stream,
            String sourceName) throws IOException {
        return createAttachmentFromStream(stream, sourceName, null);
    }

    public IFileEntry createAttachmentFromStream(InputStream stream,
            String sourceName, String mediaType) throws IOException {
        if (sourceName == null || stream == null)
            return null;

        String path = makeAttachmentPath(sourceName, false);
        if (mediaType == null)
            mediaType = FileUtils.getMediaType(sourceName);
        IFileEntry entry = createFileEntry(path, mediaType);
        if (entry != null) {
            OutputStream os = entry.openOutputStream();
            try {
                FileUtils.transfer(stream, os);
            } finally {
                os.close();
            }
        }
        return entry;
    }

    public IFileEntry cloneEntry(IFileEntry sourceEntry, String targetPath)
            throws IOException {
        if (sourceEntry == null || targetPath == null)
            return null;

        IFileEntry existingEntry = getFileEntry(targetPath);
        if (existingEntry != null) {
            return null;
        }

        if (sourceEntry.isDirectory()) {
            if (!targetPath.endsWith("/")) //$NON-NLS-1$
                targetPath = targetPath + "/"; //$NON-NLS-1$
            importDirectoryEntry(targetPath, sourceEntry);
        } else {
            importFileEntry(targetPath, sourceEntry);
        }
        return getFileEntry(targetPath);
    }

    private void importFileEntry(String path, IFileEntry sourceEntry)
            throws IOException {
        InputStream is = sourceEntry.openInputStream();
        try {
            IFileEntry entry = createFileEntry(path,
                    sourceEntry.getMediaType());
            entry.setTime(sourceEntry.getTime());
            OutputStream os = entry.openOutputStream();
            try {
                FileUtils.transfer(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    private void importDirectoryEntry(String parentPath, IFileEntry sourceEntry)
            throws IOException {
        String sourceParentPath = getParentPath(sourceEntry.getPath());
        for (IFileEntry sourceSubEntry : sourceEntry.getSubEntries()) {
            String sourceSubPath = sourceSubEntry.getPath();
            if (sourceSubPath != null) {
                String subPath = sourceSubPath
                        .substring(sourceParentPath.length());
                if (parentPath != null) {
                    subPath = parentPath + subPath;
                }
                if (!sourceSubEntry.isDirectory()) {
                    importFileEntry(subPath, sourceSubEntry);
                }
            }
        }
        if (getFileEntry(parentPath) == null) {
            createFileEntry(parentPath, sourceEntry.getMediaType());
        }
    }

    public IFileEntry cloneEntryAsAttachment(IFileEntry sourceEntry)
            throws IOException {
        String sourcePath = sourceEntry.getPath();
        String path = makeAttachmentPath(sourcePath, sourceEntry.isDirectory());
        IFileEntry cloneEntry = cloneEntry(sourceEntry, path);
        return cloneEntry;
    }

    public String makeAttachmentPath(String source) {
        return makeAttachmentPath(source, false);
    }

    public String makeAttachmentPath(String source, boolean directory) {
        String path = "attachments/" + Core.getIdFactory().createId() //$NON-NLS-1$
                + FileUtils.getExtension(source);
        if (directory)
            path += "/"; //$NON-NLS-1$
        return path;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IManifest#getEncryptionData(java.lang.String)
     */
    public IEncryptionData getEncryptionData(String entryPath) {
        IFileEntry entry = getFileEntry(entryPath);
        if (entry != null)
            return entry.getEncryptionData();
        return null;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return ownedWorkbook.getCoreEventSupport()
                .registerCoreEventListener(this, type, listener);
    }

    public CoreEventSupport getCoreEventSupport() {
        if (coreEventSupport != null)
            return coreEventSupport;

        coreEventSupport = new CoreEventSupport();
        return coreEventSupport;
    }

    public String getPasswordHint() {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_MANIFEST);
        return m.getAttribute(PASSWORD_HINT);
    }

    public void setPasswordHint(String hint) {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_MANIFEST);
        m.setAttribute(PASSWORD_HINT, hint);
    }

//    protected void saveTemp(String newPassword)
//            throws IOException, CoreException {
//        String oldPassword = getPassword();
//        SerializerImpl serializer = (SerializerImpl) Core.getSerializerFactory()
//                .newSerializer();
//        if (oldPassword == newPassword
//                || (oldPassword != null && oldPassword.equals(newPassword))) {
//            serializer.serializeAll(ownedWorkbook, this, this,
//                    FileEntrySelection.None, true);
//        } else {
//            Transformer transformer = DOMUtils.getDefaultTransformer();
//            DOMResult newDocResult = new DOMResult();
//            try {
//                transformer.transform(new DOMSource(implementation),
//                        newDocResult);
//            } catch (TransformerException e) {
//                throw new CoreException(Core.ERROR_FAIL_SERIALIZING_XML,
//                        MANIFEST_XML, e);
//            }
//
//            ManifestImpl tempManifest = new ManifestImpl(
//                    (Document) newDocResult.getNode(), storage);
//            this.password = newPassword;
//            for (IFileEntry entry : getAllRegisteredEntries()) {
//                if (entry.getEncryptionData() != null) {
//                    entry.deleteEncryptionData();
//                    entry.createEncryptionData();
//                }
//            }
//            serializer.serializeAll(ownedWorkbook, tempManifest, this,
//                    FileEntrySelection.All, true);
//        }
//    }

}
