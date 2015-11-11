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

import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IChecksumStream;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IEncryptionHandler;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.comment.ICommentManager;
import org.xmind.core.internal.compatibility.Compatibility;
import org.xmind.core.internal.security.Crypto;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;

/**
 * @author frankshaka
 * 
 */
public class WorkbookLoader extends XMLLoader {

    /**
     * 
     */
    private WorkbookBuilderImpl builder;

    /**
     * 
     */
    private IInputSource source;

    /**
     * 
     */
    private IStorage storage;

    /**
     * 
     */
    private IEncryptionHandler encryptionHandler;

    /**
     * 
     */
    private WorkbookImpl workbook = null;

    /**
     * 
     */
    private Set<String> loadedEntries = new HashSet<String>();

    /**
     * 
     */
    private ManifestImpl manifest = null;

    /**
     * 
     */
    private boolean ignoreCopy;

    /**
     * 
     */
    private String password = null;

    /**
     * Byte buffer for transfering entries.
     */
    private byte[] byteBuffer = new byte[4096];

    /**
     * 
     * @param builder
     * @param source
     * @param storage
     * @param encryptionHandler
     */
    public WorkbookLoader(WorkbookBuilderImpl builder, IInputSource source,
            IStorage storage, IEncryptionHandler encryptionHandler)
                    throws CoreException {
        super();
        this.builder = builder;
        if (source == null && storage != null) {
            source = storage.getInputSource();
            this.ignoreCopy = true;
        } else {
            this.ignoreCopy = false;
        }
        this.source = source;
        this.storage = storage;
        this.encryptionHandler = encryptionHandler;

    }

    public IWorkbook load() throws IOException, CoreException {
        password = null;
        manifest = null;
        try {
            doLoad();
        } finally {
            manifest = null;
            password = null;
        }
        return workbook;
    }

    /**
     * The main loading process.
     * 
     * @throws IOException
     * @throws CoreException
     */
    private void doLoad() throws IOException, CoreException {
        loadManifest();

        if (loadOldFormat())
            return;

        loadContents();
        loadMeta();
        loadStyleSheet();
        loadMarkerSheet();
        loadComments();

        copyOtherStaff();

        initWorkbook();
        clearEncryptionData();
    }

    private void loadManifest() throws IOException, CoreException {
        Document doc = forceLoadXML(MANIFEST_XML);
        this.manifest = new ManifestImpl(doc);
    }

    private boolean loadOldFormat() throws IOException, CoreException {
        IWorkbook compatible = Compatibility.loadCompatibleWorkbook(source,
                this, storage);
        if (compatible != null) {
            workbook = (WorkbookImpl) compatible;
            return true;
        }
        return false;
    }

    private void loadContents() throws IOException, CoreException {
        Document doc = loadXMLFile(source, ArchiveConstants.CONTENT_XML);
        workbook = new WorkbookImpl(doc);
        workbook.setManifest(manifest);
    }

    private void loadMeta() throws IOException, CoreException {
        Document doc = forceLoadXML(ArchiveConstants.META_XML);
        MetaImpl meta = new MetaImpl(doc);
        workbook.setMeta(meta);
        if (meta.getValue(IMeta.CREATED_TIME) == null)
            meta.setValue(IMeta.CREATED_TIME,
                    NumberUtils.formatDate(System.currentTimeMillis()));

        workbook.setCreator(builder.creatorName, builder.creatorversion);
    }

    private void loadStyleSheet() throws IOException, CoreException {
        try {
            IStyleSheet styleSheet = ((StyleSheetBuilderImpl) Core
                    .getStyleSheetBuilder()).loadFromInputSource(source, this);
            ((StyleSheetImpl) styleSheet).setManifest(manifest);
            workbook.setStyleSheet((StyleSheetImpl) styleSheet);
        } catch (IOException e) {
            throw e;
        } catch (CoreException e) {
            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
                throw e;
        }
    }

    private void loadMarkerSheet() throws IOException, CoreException {
        try {
            IMarkerSheet markerSheet = ((MarkerSheetBuilderImpl) Core
                    .getMarkerSheetBuilder()).loadFromInputSource(source, this,
                            new WorkbookMarkerResourceProvider(workbook));
            workbook.setMarkerSheet((MarkerSheetImpl) markerSheet);
        } catch (IOException e) {
            throw e;
        } catch (CoreException e) {
            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
                throw e;
        }
    }

    private void loadComments() throws IOException, CoreException {
        try {
            ICommentManager commentManager = ((CommentManagerBuilderImpl) Core
                    .getCommentManagerBuilder()).loadFromInputSource(source,
                            this);
            workbook.setCommentManager((CommentManagerImpl) commentManager);
        } catch (IOException e) {
            throw e;
        } catch (CoreException e) {
            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
                throw e;
        }
    }

    private void copyOtherStaff() throws IOException, CoreException {
        if (ignoreCopy || source == null || storage == null)
            return;

        if (!source.equals(storage.getInputSource())) {
            IOutputTarget target = storage.getOutputTarget();
            IManifest manifest = workbook.getManifest();
            for (IFileEntry entry : manifest.getFileEntries()) {
                if (!entry.isDirectory()) {
                    String entryPath = entry.getPath();
                    if (shouldLoadEntry(entryPath)) {
                        copyEntry(source, target, entryPath);
                        markLoaded(entryPath);
                    }
                }
            }
        } else {
            // Prefetch all file entries:
            workbook.getManifest().getFileEntries();
        }
    }

    private boolean shouldLoadEntry(String entryPath) {
        return entryPath != null && !"".equals(entryPath) //$NON-NLS-1$
                && !hasBeenLoaded(entryPath);
    }

    private void copyEntry(IInputSource source, IOutputTarget target,
            String entryPath) throws IOException, CoreException {
        InputStream in = getInputStream(source, entryPath);
        if (in == null)
            return;

        try {
            long time = source.getEntryTime(entryPath);
            if (time >= 0)
                target.setEntryTime(entryPath, time);
            OutputStream out = getOutputStream(target, entryPath);
            try {
                int numBytes;
                while ((numBytes = in.read(byteBuffer)) > 0) {
                    out.write(byteBuffer, 0, numBytes);
                }
                if (!verifyChecksum(source, entryPath, in))
                    throw new CoreException(Core.ERROR_WRONG_PASSWORD);
            } finally {
                out.close();
            }

        } finally {
            in.close();
        }
    }

    private OutputStream getOutputStream(IOutputTarget target, String entryPath)
            throws IOException {
        if (!target.isEntryAvaialble(entryPath))
            return null;

        return target.openEntryStream(entryPath);
    }

    private void markLoaded(String entryPath) {
        loadedEntries.add(entryPath);
    }

    private boolean hasBeenLoaded(String entryPath) {
        return loadedEntries.contains(entryPath);
    }

    private void initWorkbook() throws IOException, CoreException {
        if (storage != null) {
            workbook.setTempStorage(storage);
        }
        initWorkbookContents(workbook);
        workbook.setPassword(password);
    }

    private void initWorkbookContents(WorkbookImpl workbook) {
        for (ISheet s : workbook.getSheets()) {
            initSheet(s, workbook);
        }
    }

    private void initSheet(ISheet sheet, WorkbookImpl wb) {
        ((SheetImpl) sheet).addNotify(wb);

        // Prefetch all revisions of this sheet.
        workbook.getRevisionRepository().getRevisionManager(sheet.getId(),
                IRevision.SHEET);
    }

    private void clearEncryptionData() {
        for (IFileEntry entry : manifest.getFileEntries()) {
            entry.deleteEncryptionData();
        }
    }

    private Document forceLoadXML(String entryPath)
            throws IOException, CoreException {
        try {
            return loadXMLFile(source, entryPath);
        } catch (Throwable e) {
            if (e instanceof CoreException) {
                CoreException coreEx = (CoreException) e;
                if (coreEx.getType() == Core.ERROR_WRONG_PASSWORD
                        || coreEx.getType() == Core.ERROR_CANCELLATION) {
                    throw coreEx;
                }
            }
            //in case the file is damaged, 
            //try continue loading
            Core.getLogger().log(e, "Faild to load " + entryPath); //$NON-NLS-1$
            return createDocument();
        }
    }

    private InputStream getInputStream(IInputSource source, String entryPath)
            throws CoreException {
        if (!source.hasEntry(entryPath))
            return null;

        InputStream in = source.getEntryStream(entryPath);
        if (in == null)
            return null;

        if (manifest != null) {
            IEncryptionData encData = manifest.getEncryptionData(entryPath);
            if (encData != null) {
                in = createDecryptedStream(in, encData);
            }
        }

        return in;
    }

    private InputStream createDecryptedStream(InputStream in,
            IEncryptionData encData) throws CoreException {
        String password = getPassword();
        if (password == null)
            throw new CoreException(Core.ERROR_CANCELLATION);
        return Crypto.createInputStream(in, false, encData, password);
    }

    private String getPassword() throws CoreException {
        if (password == null) {
            if (encryptionHandler != null) {
                password = encryptionHandler.retrievePassword();
            }
        }
        return password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.dom.XMLLoader#doLoadXMLFile(org.xmind.core.io
     * .IInputSource, java.lang.String)
     */
    protected Document doLoadXMLFile(IInputSource source, String entryPath)
            throws IOException, CoreException {
        InputStream stream = getInputStream(source, entryPath);
        if (stream == null)
            throw new CoreException(Core.ERROR_NO_SUCH_ENTRY, entryPath);

        Document doc;
        try {
            doc = builder.getDocumentLoader().parse(stream);
        } catch (Throwable error) {
            if (!verifyChecksum(source, entryPath, stream))
                throw new CoreException(Core.ERROR_WRONG_PASSWORD, error);
            if (error instanceof IOException)
                throw (IOException) error;
            if (error instanceof CoreException)
                throw (CoreException) error;
            throw new CoreException(Core.ERROR_FAIL_PARSING_XML, error);
        } finally {
            stream.close();
        }

        if (!verifyChecksum(source, entryPath, stream))
            throw new CoreException(Core.ERROR_WRONG_PASSWORD);

        markLoaded(entryPath);

        return doc;
    }

    private boolean verifyChecksum(IInputSource source, String entryName,
            InputStream stream) throws IOException, CoreException {
        if (stream instanceof IChecksumStream) {
            if (manifest == null) {
                throw new IllegalStateException(
                        "Manifest should not be encrypted"); //$NON-NLS-1$
            }
            IEncryptionData encData = manifest.getEncryptionData(entryName);
            if (encData != null) {
                String expectedChecksum = encData.getChecksum();
                if (expectedChecksum != null) {
                    String actualChecksum;
                    actualChecksum = ((IChecksumStream) stream).getChecksum();
                    if (actualChecksum == null
                            || !expectedChecksum.equals(actualChecksum)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Document createDocument() {
        return builder.createDocument();
    }

}
