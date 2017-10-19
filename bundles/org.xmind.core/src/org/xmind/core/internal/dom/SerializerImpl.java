/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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

import static org.xmind.core.internal.zip.ArchiveConstants.COMMENTS_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_EXTENSIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_REVISIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.REVISIONS_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.ICommentManager;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IMeta;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtension;
import org.xmind.core.IWorkbookExtensionManager;
import org.xmind.core.internal.AbstractSerializingBase;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IProgressReporter;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class SerializerImpl extends AbstractSerializingBase
        implements ISerializer {

    private IWorkbook workbook;
    private IOutputTarget outputTarget;

    private final Set<String> encryptionIgnoredEntries;
    private String[] preferredEncryptionIgnoredEntries;

    private ZipOutputStream intermediateOutputStream;

    private boolean compressed;

    private boolean usesWorkbookStorageAsOutputTarget;

    private ManifestImpl manifest;

    private ManifestImpl tempManifest;

    private final Set<String> serializedEntryPaths;

    public SerializerImpl() {
        super();
        this.workbook = null;
        this.outputTarget = null;
        this.encryptionIgnoredEntries = new HashSet<String>();
        this.preferredEncryptionIgnoredEntries = null;
        this.intermediateOutputStream = null;
        this.compressed = false;
        this.usesWorkbookStorageAsOutputTarget = false;
        this.manifest = null;
        this.tempManifest = null;
        this.serializedEntryPaths = new HashSet<String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#getWorkbook()
     */
    public IWorkbook getWorkbook() {
        return workbook;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#setWorkbook(org.xmind.core.IWorkbook)
     */
    public void setWorkbook(IWorkbook workbook) {
        if (workbook == null)
            throw new IllegalArgumentException("Workbook is null"); //$NON-NLS-1$
        if (!(workbook instanceof WorkbookImpl))
            throw new IllegalArgumentException("Can't serialize this workbook"); //$NON-NLS-1$
        IStorage storage = null;
        if (usesWorkbookStorageAsOutputTarget) {
            storage = workbook.getAdapter(IStorage.class);
            if (storage == null)
                throw new IllegalArgumentException(
                        "No workbook storage available"); //$NON-NLS-1$
        }
        this.workbook = workbook;
        if (storage != null) {
            doSetOutputTarget(storage.getOutputTarget());
        }
    }

    protected IOutputTarget getOutputTarget() {
        return this.outputTarget;
    }

    protected void doSetOutputTarget(IOutputTarget target) {
        this.outputTarget = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#hasOutputTarget()
     */
    public boolean hasOutputTarget() {
        return outputTarget != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#setOutputTarget(org.xmind.core.io.
     * IOutputTarget)
     */
    public void setOutputTarget(IOutputTarget target) {
        if (target == null)
            throw new IllegalArgumentException("output target is null"); //$NON-NLS-1$
        doSetOutputTarget(target);
        this.intermediateOutputStream = null;
        this.usesWorkbookStorageAsOutputTarget = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("stream is null"); //$NON-NLS-1$
        this.intermediateOutputStream = new ZipOutputStream(stream);
        doSetOutputTarget(new ZipStreamOutputTarget(intermediateOutputStream,
                compressed));
        this.usesWorkbookStorageAsOutputTarget = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#setWorkbookStorageAsOutputTarget()
     */
    public void setWorkbookStorageAsOutputTarget() {
        if (getWorkbook() != null) {
            IStorage storage = getWorkbook().getAdapter(IStorage.class);
            if (storage == null)
                throw new IllegalArgumentException(
                        "no workbook storage available"); //$NON-NLS-1$
            doSetOutputTarget(storage.getOutputTarget());
        } else {
            /// sets a fake output target that will be substituted when
            /// workbook is set
            doSetOutputTarget(new ByteArrayStorage().getOutputTarget());
        }
        this.usesWorkbookStorageAsOutputTarget = true;
        this.intermediateOutputStream = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#getEncryptionIgnoredEntries()
     */
    public String[] getEncryptionIgnoredEntries() {
        return preferredEncryptionIgnoredEntries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.ISerializer#setEncryptionIgnoredEntries(java.lang.String[]
     * )
     */
    public void setEncryptionIgnoredEntries(String[] entryPaths) {
        this.preferredEncryptionIgnoredEntries = entryPaths;
        encryptionIgnoredEntries.clear();
        collectDefaultEncryptionIgnoredEntries(encryptionIgnoredEntries);
        if (entryPaths != null) {
            encryptionIgnoredEntries.addAll(Arrays.asList(entryPaths));
        }
    }

    protected boolean isEntryEncryptionIgnored(String entryPath) {
        return encryptionIgnoredEntries.contains(entryPath);
    }

    protected void collectDefaultEncryptionIgnoredEntries(
            Set<String> entryPaths) {
        entryPaths.add(ArchiveConstants.MANIFEST_XML);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.ISerializer#serialize(org.xmind.core.util.
     * IProgressReporter)
     */
    public void serialize(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException {
        WorkbookImpl workbook = (WorkbookImpl) getWorkbook();
        if (workbook == null)
            throw new IllegalStateException("no workbook to serialize"); //$NON-NLS-1$

        if (!hasOutputTarget())
            throw new IllegalStateException("no output target specified"); //$NON-NLS-1$

        manifest = workbook.getManifest();

        IEntryStreamNormalizer oldNormalizer = manifest.getStreamNormalizer();
        IEntryStreamNormalizer newNormalizer = getEntryStreamNormalizer();
        boolean normalizerChanged = newNormalizer != null
                && !newNormalizer.equals(oldNormalizer);

        if (usesWorkbookStorageAsOutputTarget) {
            tempManifest = manifest;
            if (normalizerChanged) {
                /// use new normalizer to save XML files
                tempManifest.setStreamNormalizer(newNormalizer);
            }
        } else {
            Document tempImplementation = cloneDocument(
                    manifest.getImplementation(),
                    ArchiveConstants.MANIFEST_XML);
            tempManifest = new ManifestImpl(tempImplementation,
                    new WriteOnlyStorage(getOutputTarget()));
            if (newNormalizer != null) {
                tempManifest.setStreamNormalizer(newNormalizer);
            } else {
                tempManifest.setStreamNormalizer(oldNormalizer);
            }

            /// Give this manifest a temp owner workbook to prevent null 
            /// pointer exception when file entry events are triggered.
            new WorkbookImpl(DOMUtils.createDocument(), tempManifest);
        }

        /// save meta.xml
        IMeta meta = workbook.getMeta();
        String creatorName = getCreatorName();
        if (creatorName != null)
            meta.setValue(IMeta.CREATOR_NAME, creatorName);
        String creatorVersion = getCreatorVersion();
        if (creatorVersion != null)
            meta.setValue(IMeta.CREATOR_VERSION, creatorVersion);
        serializeXML(meta, META_XML);

        /// save content.xml
        serializeXML(workbook, CONTENT_XML);

        /// NOTE: XML files should always serialized when saving to the 
        /// workbook's temp storage, otherwise recovered workbooks may contain
        /// invalid data.

        /// save markers/markerSheet.xml
        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        if (usesWorkbookStorageAsOutputTarget || !markerSheet.isEmpty()) {
            serializeXML(markerSheet, PATH_MARKER_SHEET);
        } else {
            tempManifest.deleteFileEntry(PATH_MARKER_SHEET);
            serializedEntryPaths.add(PATH_MARKER_SHEET);
        }

        /// save styles.xml
        IStyleSheet styleSheet = workbook.getStyleSheet();
        if (usesWorkbookStorageAsOutputTarget || !styleSheet.isEmpty()) {
            serializeXML(styleSheet, STYLES_XML);
        } else {
            tempManifest.deleteFileEntry(STYLES_XML);
            serializedEntryPaths.add(STYLES_XML);
        }

        /// save comments.xml
        ICommentManager commentManager = workbook.getCommentManager();
        if (usesWorkbookStorageAsOutputTarget || !commentManager.isEmpty()) {
            serializeXML(commentManager, COMMENTS_XML);
        } else {
            tempManifest.deleteFileEntry(COMMENTS_XML);
            serializedEntryPaths.add(COMMENTS_XML);
        }

        /// save extensions
        IWorkbookExtensionManager extensionManager = ((IWorkbook) workbook)
                .getAdapter(IWorkbookExtensionManager.class);
        List<IWorkbookExtension> exts = extensionManager.getExtensions();
        for (IWorkbookExtension ext : exts) {
            String providerName = ext.getProviderName();
            String path = PATH_EXTENSIONS + providerName + ".xml"; //$NON-NLS-1$
            serializeXML(ext, path);
        }

        /// save revisions
        IRevisionRepository revisionRepository = workbook
                .getRevisionRepository();
        for (String resourceId : revisionRepository
                .getRegisteredResourceIds()) {
            IRevisionManager manager = revisionRepository
                    .getRegisteredRevisionManager(resourceId);
            if (manager != null) {
                String path = PATH_REVISIONS + resourceId + "/" //$NON-NLS-1$
                        + REVISIONS_XML;
                serializeXML(manager, path);
            }
        }

        /// copy remaining file entries, e.g. attachments, etc.
        Iterator<IFileEntry> sourceEntryIter;
        if (!usesWorkbookStorageAsOutputTarget) {
            /// saving to external location,
            /// write only referenced file entries
            sourceEntryIter = manifest.iterFileEntries();
        } else if (normalizerChanged) {
            /// saving to internal storage when encryption is changed,
            /// re-encrypt all file entries
            sourceEntryIter = manifest.getAllRegisteredEntries().iterator();
        } else {
            /// saving to internal storage when encryption is not changed,
            /// touch no file entries
            sourceEntryIter = null;
        }

        while (sourceEntryIter != null && sourceEntryIter.hasNext()) {
            IFileEntry sourceEntry = sourceEntryIter.next();
            if (sourceEntry.isDirectory() || !sourceEntry.canRead())
                continue;

            String entryPath = sourceEntry.getPath();
            if (MANIFEST_XML.equals(entryPath)
                    || serializedEntryPaths.contains(entryPath))
                continue;

            IFileEntry targetEntry = tempManifest.getFileEntry(entryPath);
            if (targetEntry == null)
                // TODO missing entry, need log?
                continue;

            if (usesWorkbookStorageAsOutputTarget) {
                /// saving to internal storage,
                /// write to a temporary entry first to protect original entry
                String tempEntryPath = makeTempPath(entryPath);

                /// make sure we use the old normalizer to decrypt the file entry
                manifest.setStreamNormalizer(oldNormalizer);
                InputStream entryInput = sourceEntry.openInputStream();
                try {
                    OutputStream tempOutput = tempManifest.getStorage()
                            .getOutputTarget().openEntryStream(tempEntryPath);
                    try {
                        FileUtils.transfer(entryInput, tempOutput, false);
                    } finally {
                        tempOutput.close();
                    }
                } finally {
                    entryInput.close();
                }

                /// make sure we use the new normalizer to encrypt the file entry
                tempManifest.setStreamNormalizer(newNormalizer);
                InputStream tempInput = tempManifest.getStorage()
                        .getInputSource().openEntryStream(tempEntryPath);
                try {
                    OutputStream entryOutput = openEntryOutputStream(entryPath);
                    try {
                        FileUtils.transfer(tempInput, entryOutput, false);
                    } finally {
                        entryOutput.close();
                    }
                } finally {
                    tempInput.close();
                }

            } else {
                /// saving to external location,
                /// just copy the entry directly
                InputStream entryInput = sourceEntry.openInputStream();
                try {
                    OutputStream entryOutput = openEntryOutputStream(entryPath);
                    try {
                        FileUtils.transfer(entryInput, entryOutput, false);
                    } finally {
                        entryOutput.close();
                    }
                } finally {
                    entryInput.close();
                }
            }
        }

        if (usesWorkbookStorageAsOutputTarget && normalizerChanged) {
            /// keep the new normalizer in the original manifest
            /// to decrypt data in the internal storage afterwards
            manifest.setStreamNormalizer(newNormalizer);
        }

        /// save manifest.xml
        serializeXML(tempManifest, MANIFEST_XML);

        /// only upon success should we close zip stream
        if (intermediateOutputStream != null) {
            intermediateOutputStream.finish();
            intermediateOutputStream.flush();
            intermediateOutputStream.close();
        }

    }

    private static Document cloneDocument(Document document, String xmlName)
            throws CoreException {
        try {
            Transformer transformer = createXMLSerializer();
            DOMResult result = new DOMResult();
            transformer.transform(new DOMSource(document), result);
            return (Document) result.getNode();
        } catch (TransformerException e) {
            throw new CoreException(Core.ERROR_FAIL_SERIALIZING_XML, xmlName,
                    e);
        }
    }

    private static Transformer createXMLSerializer() throws CoreException {
        /// create a new transformer instance each time
        return DOMUtils.getDefaultTransformer();
    }

    private String makeTempPath(String path) {
        int sepIndex = path.lastIndexOf('/');
        if (sepIndex >= 0) {
            return path.substring(0, sepIndex + 1) + "._." //$NON-NLS-1$
                    + path.substring(sepIndex + 1);
        }
        // no separator
        return "._." + path; //$NON-NLS-1$
    }

    private void serializeXML(IAdaptable domAdaptable, String entryPath)
            throws IOException, CoreException {
        Node node = (Node) domAdaptable.getAdapter(Node.class);
        if (node == null)
            throw new CoreException(Core.ERROR_INVALID_ARGUMENT,
                    "Object has no DOM node"); //$NON-NLS-1$

        Transformer transformer = createXMLSerializer();
        OutputStream out = openEntryOutputStream(entryPath);
        try {
            transformer.transform(new DOMSource(node), new StreamResult(out));
        } catch (TransformerException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new CoreException(Core.ERROR_FAIL_SERIALIZING_XML, entryPath,
                    e);
        } finally {
            out.close();
        }

        serializedEntryPaths.add(entryPath);
    }

    private OutputStream openEntryOutputStream(String entryPath)
            throws IOException, CoreException {
        IFileEntry entry = tempManifest.getFileEntry(entryPath);

        if (isEntryEncryptionIgnored(entryPath)) {
            entry.deleteEncryptionData();
            return tempManifest.getStorage().getOutputTarget()
                    .openEntryStream(entryPath);
        }

        if (entry == null) {
            entry = tempManifest.createFileEntry(entryPath);
            entry.increaseReference();
        }
        return entry.openOutputStream();
    }

//    String calcChecksum(Object checksumProvider) {
//        if (checksumProvider instanceof IChecksumStream) {
//            return ((IChecksumStream) checksumProvider).getChecksum();
//        }
//        return null;
//    }

//    void recordChecksum(String entryPath, Object checksumProvider) {
//        String checksum = calcChecksum(checksumProvider);
//        if (checksum == null)
//            return;
//
//        IFileEntry entry = tempManifest.getFileEntry(entryPath);
//        if (entry == null)
//            return;
//
//        IEncryptionData encData = entry.getEncryptionData();
//        if (encData == null || encData.getChecksumType() == null)
//            return;
//
//        encData.setAttribute(checksum, DOMConstants.ATTR_CHECKSUM);
//    }

    private static class WriteOnlyStorage implements IStorage {

        private IOutputTarget target;

        public WriteOnlyStorage(IOutputTarget target) {
            this.target = target;
        }

        public IInputSource getInputSource() {
            throw new UnsupportedOperationException();
        }

        public IOutputTarget getOutputTarget() {
            return target;
        }

        public String getName() {
            return toString();
        }

        public String getFullPath() {
            return getName();
        }

        public void clear() {
        }

        public void deleteEntry(String entryName) {
        }

        public void renameEntry(String entryName, String newName) {
        }

    }

}
