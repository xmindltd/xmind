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
/**
 * 
 */
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.zip.ArchiveConstants.COMMENTS_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_EXTENSIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IMeta;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtensionManager;
import org.xmind.core.internal.AbstractSerializingBase;
import org.xmind.core.internal.compatibility.Compatibility;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.CoreIOException;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.io.InvalidChecksumException;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IProgressReporter;
import org.xml.sax.SAXException;

/**
 * @author Frank Shaka
 *
 */
public class DeserializerImpl extends AbstractSerializingBase
        implements IDeserializer {

    private WorkbookImpl workbook;

    private IStorage storage;

    private IInputSource inputSource;

    private InputStream inputStream;

    private boolean usesWorkbookStorageAsInputSource;

    private ManifestImpl manifest;

    private final Map<String, Document> loadedDocuments;

    /**
     * 
     */
    public DeserializerImpl() {
        super();
        this.workbook = null;
        this.storage = new ByteArrayStorage();
        this.inputSource = null;
        this.inputStream = null;
        this.usesWorkbookStorageAsInputSource = false;
        this.manifest = null;
        this.loadedDocuments = new HashMap<String, Document>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#getWorkbook()
     */
    public IWorkbook getWorkbook() {
        return workbook;
    }

    protected void setWorkbook(WorkbookImpl workbook) {
        this.workbook = workbook;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#setWorkbookStorage(org.xmind.core.io.
     * IStorage)
     */
    public void setWorkbookStorage(IStorage storage) {
        if (storage == null)
            throw new IllegalArgumentException("storage is null"); //$NON-NLS-1$
        this.storage = storage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#getWorkbookStorage()
     */
    public IStorage getWorkbookStorage() {
        return this.storage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#setInputSource(org.xmind.core.io.
     * IInputSource)
     */
    public void setInputSource(IInputSource source) {
        if (source == null)
            throw new IllegalArgumentException("input source is null"); //$NON-NLS-1$
        this.inputSource = source;
        this.inputStream = null;
        this.usesWorkbookStorageAsInputSource = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#setInputStream(java.io.InputStream)
     */
    public void setInputStream(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("input stream is null"); //$NON-NLS-1$
        this.inputStream = stream;
        this.inputSource = null;
        this.usesWorkbookStorageAsInputSource = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#setWorkbookStorageAsInputSource()
     */
    public void setWorkbookStorageAsInputSource() {
        this.usesWorkbookStorageAsInputSource = true;
        this.inputSource = null;
        this.inputStream = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IDeserializer#hasInputSource()
     */
    public boolean hasInputSource() {
        return inputSource != null || inputStream != null
                || usesWorkbookStorageAsInputSource;
    }

    public void deserializeManifest(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException {
        if (inputStream != null) {
            ZipInputStream zin = new ZipInputStream(inputStream);
            try {
                FileUtils.extractZipFile(zin, storage.getOutputTarget());
            } finally {
                zin.close();
            }
        } else if (inputSource != null) {
            FileUtils.transfer(inputSource, storage.getOutputTarget());
        } else if (!usesWorkbookStorageAsInputSource) {
            throw new IllegalStateException("no input source available"); //$NON-NLS-1$
        }

        /// load manifest.xml first to provide file entry info
        Document manifestDoc = forceLoadDocumentFromEntry(
                ArchiveConstants.MANIFEST_XML);
        manifest = new ManifestImpl(manifestDoc, storage);
        manifest.setStreamNormalizer(getEntryStreamNormalizer());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xmind.core.IDeserializer#deserialize(org.xmind.core.util.
     * IProgressReporter)
     */
    public void deserialize(IProgressReporter reporter)
            throws IOException, CoreException, IllegalStateException {
        if (manifest == null)
            deserializeManifest(reporter);

        /// Check if it's in old format
        WorkbookImpl compatibleWorkbook = Compatibility
                .loadCompatibleWorkbook(this);
        if (compatibleWorkbook != null) {
            setWorkbook(compatibleWorkbook);
            return;
        }

        try {
            setWorkbook(loadWorkbook());
        } catch (InvalidChecksumException e) {
            throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
        } catch (CoreIOException e) {
            CoreException ce = e.getCoreException();
            throw new CoreException(ce.getType(), ce.getCodeInfo(), e);
        }

    }

    private WorkbookImpl loadWorkbook() throws IOException, CoreException {
        /// load content.xml
        Document workbookDoc = forceLoadDocumentFromEntry(
                ArchiveConstants.CONTENT_XML);
        WorkbookImpl workbook = new WorkbookImpl(workbookDoc, manifest);

        Document metaDoc = forceLoadDocumentFromEntry(META_XML);
        MetaImpl meta = new MetaImpl(metaDoc);
        workbook.setMeta(meta);
        if (meta.getValue(IMeta.CREATED_TIME) == null) {
            meta.setValue(IMeta.CREATED_TIME,
                    NumberUtils.formatDate(System.currentTimeMillis()));
        }
        meta.setValue(IMeta.CREATOR_NAME, getCreatorName());
        meta.setValue(IMeta.CREATOR_VERSION, getCreatorVersion());

        /// load styles.xml
        Document styleSheetDoc = loadDocumentFromEntry(STYLES_XML);
        if (styleSheetDoc != null) {
            StyleSheetImpl styleSheet = ((StyleSheetBuilderImpl) Core
                    .getStyleSheetBuilder()).createStyleSheet(styleSheetDoc);
            styleSheet.setManifest(manifest);
            workbook.setStyleSheet(styleSheet);
        }

        /// load markers/markerSheet.xml
        Document markerSheetDoc = loadDocumentFromEntry(PATH_MARKER_SHEET);
        if (markerSheetDoc != null) {
            MarkerSheetImpl markerSheet = ((MarkerSheetBuilderImpl) Core
                    .getMarkerSheetBuilder()).createMarkerSheet(markerSheetDoc,
                            new WorkbookMarkerResourceProvider(workbook));
            markerSheet.setManifest(manifest);
            workbook.setMarkerSheet(markerSheet);
        }

        /// load extensions
        IWorkbookExtensionManager m = ((IWorkbook) workbook)
                .getAdapter(IWorkbookExtensionManager.class);
        if (m instanceof WorkbookExtensionManagerImpl) {
            WorkbookExtensionManagerImpl extManager = (WorkbookExtensionManagerImpl) m;
            for (String provider : extManager.getProviders()) {
                Document extDoc = loadDocumentFromEntry(PATH_EXTENSIONS + //
                        provider + ".xml"); //$NON-NLS-1$
                if (extDoc != null) {
                    extManager.createExtension(provider, extDoc);
                }
            }
        }

        /// load comments.xml
        Document commentManagerDoc = loadDocumentFromEntry(COMMENTS_XML);
        if (commentManagerDoc != null) {
            CommentManagerImpl commentManager = new CommentManagerImpl(workbook,
                    commentManagerDoc);
            workbook.setCommentManager(commentManager);
        }

        /// initialize workbook content
        for (ISheet sheet : workbook.getSheets()) {
            ((SheetImpl) sheet).addNotify(workbook);
        }

        IRevisionRepository revisionRepository = workbook
                .getRevisionRepository();
        for (String resourceId : revisionRepository
                .getRegisteredResourceIds()) {
            revisionRepository.getRegisteredRevisionManager(resourceId);
        }

        /// check all file entries for integrity
        /// TODO FIXME do we really need this?
        Iterator<IFileEntry> it = manifest.iterFileEntries();
        while (it.hasNext()) {
            IFileEntry e = it.next();
            if (e.isDirectory() || !e.canRead())
                continue;
            InputStream in = e.openInputStream();
            byte[] b = new byte[1024];
            while (in.read(b) != -1) {
                /// do nothing
            }
        }

        return workbook;
    }

    protected InputStream openEntryInputStream(String entryPath)
            throws IOException, CoreException {
        if (manifest == null && storage == null)
            throw new IllegalStateException(
                    "No manifest or input source available"); //$NON-NLS-1$

        if (manifest != null) {
            IFileEntry entry = manifest.getFileEntry(entryPath);
            if (entry != null) {
                if (!entry.canRead())
                    return null;
                return entry.openInputStream();
            }
        }

        if (storage != null) {
            IInputSource source = storage.getInputSource();
            if (source != null && source.hasEntry(entryPath)
                    && source.isEntryAvailable(entryPath)) {
                return source.openEntryStream(entryPath);
            }
        }

        return null;
    }

    protected Document loadDocumentFromEntry(String entryPath)
            throws IOException, CoreException {
        Document cache = loadedDocuments.get(entryPath);
        if (cache != null)
            return cache;

        InputStream stream = openEntryInputStream(entryPath);
        if (stream == null)
            return null;

        Document document;
        try {
            document = loadDocumentFromStream(stream);
        } catch (CoreIOException e) {
            CoreException ce = e.getCoreException();
            throw new CoreException(ce.getType(), ce.getCodeInfo(), e);
        } catch (IOException e) {
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw e;
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (RuntimeException e) {
            /// catching any runtime exception during xml parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } catch (Error e) {
            /// catching any error during xml parsing
            if (hasEncryptionData(entryPath)) {
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
            }
            throw e;
        } finally {
            if (stream != null)
                stream.close();
        }

        if (document != null) {
            loadedDocuments.put(entryPath, document);
        }
        return document;
    }

    protected Document loadDocumentFromStream(InputStream stream)
            throws IOException, CoreException {
        try {
            DocumentBuilder builder;
            try {
                builder = DOMUtils.getDefaultDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
            }
            return builder.parse(stream);
        } catch (SAXException e) {
            throw new CoreException(Core.ERROR_FAIL_PARSING_XML, e);
        } finally {
            stream.close();
        }
    }

    protected Document createDocument() throws CoreException {
        DocumentBuilder builder;
        try {
            builder = DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_PARSER, e);
        }

        return builder.newDocument();
    }

    protected Document forceLoadDocumentFromEntry(String entryPath)
            throws CoreException {
        Document document;
        try {
            document = loadDocumentFromEntry(entryPath);
        } catch (IOException e) {
            document = null;
        }

        if (document == null) {
            document = createDocument();
            loadedDocuments.put(entryPath, document);
        }
        return document;
    }

    public ManifestImpl getManifest() {
        return manifest;
    }

    private boolean hasEncryptionData(String entryPath) {
        if (manifest != null) {
            IFileEntry entry = manifest.getFileEntry(entryPath);
            return entry != null && entry.getEncryptionData() != null;
        }
        return false;
    }

}
