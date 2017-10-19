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

/**
 * @author Frank Shaka
 * @deprecated
 */
@Deprecated
public class WorkbookLoader {

//    private WorkbookBuilderImpl builder;
//    private IStorage storage;
//    private IEncryptionHandler encryptionHandler;
//
//    private IInputSource source = null;
//    private WorkbookImpl workbook = null;
//
//    private ManifestImpl manifest = null;
//    private String password = null;
//
//    /**
//     * 
//     * @param builder
//     * @param source
//     * @param storage
//     * @param encryptionHandler
//     */
//    public WorkbookLoader(WorkbookBuilderImpl builder, IStorage storage,
//            IEncryptionHandler encryptionHandler) throws CoreException {
//        super();
//        this.builder = builder;
//        this.storage = storage;
//        this.encryptionHandler = encryptionHandler;
//    }
//
//    public IWorkbook load() throws IOException, CoreException {
//        source = storage.getInputSource();
//        password = null;
//        manifest = null;
//        try {
//            doLoad();
//        } finally {
//            manifest = null;
//            password = null;
//            source = null;
//        }
//        return workbook;
//    }
//
//    /**
//     * The main loading process.
//     * 
//     * @throws IOException
//     * @throws CoreException
//     */
//    private void doLoad() throws IOException, CoreException {
//        loadManifest();
//
//        if (loadOldFormat())
//            return;
//
//        loadContents();
//        loadMeta();
//        loadStyleSheet();
//        loadMarkerSheet();
//        loadComments();
//
//        initWorkbook();
//    }
//
//    private void loadManifest() throws IOException, CoreException {
//        Document doc = forceLoadXML(MANIFEST_XML);
//        ManifestImpl manifest = new ManifestImpl(doc, null, storage);
//        manifest.setEncryptionDelegate(encryptionHandler);
//        this.manifest = manifest;
//    }
//
//    private boolean loadOldFormat() throws IOException, CoreException {
//        IWorkbook compatible = Compatibility.loadCompatibleWorkbook(source,
//                this, storage);
//        if (compatible != null) {
//            workbook = (WorkbookImpl) compatible;
//            return true;
//        }
//        return false;
//    }
//
//    private void loadContents() throws IOException, CoreException {
//        Document doc = loadXMLFile(source, ArchiveConstants.CONTENT_XML);
//        workbook = new WorkbookImpl(doc, manifest);
//        workbook.setInitialPassword(password);
//    }
//
//    private void loadMeta() throws IOException, CoreException {
//        Document doc = forceLoadXML(ArchiveConstants.META_XML);
//        MetaImpl meta = new MetaImpl(doc);
//        workbook.setMeta(meta);
//        if (meta.getValue(IMeta.CREATED_TIME) == null)
//            meta.setValue(IMeta.CREATED_TIME,
//                    NumberUtils.formatDate(System.currentTimeMillis()));
//    }
//
//    private void loadStyleSheet() throws IOException, CoreException {
//        try {
//            IStyleSheet styleSheet = ((StyleSheetBuilderImpl) Core
//                    .getStyleSheetBuilder()).loadFromInputSource(source, this);
//            ((StyleSheetImpl) styleSheet).setManifest(manifest);
//            workbook.setStyleSheet((StyleSheetImpl) styleSheet);
//        } catch (IOException e) {
//            throw e;
//        } catch (CoreException e) {
//            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
//                throw e;
//        }
//    }
//
//    private void loadMarkerSheet() throws IOException, CoreException {
//        try {
//            IMarkerSheet markerSheet = ((MarkerSheetBuilderImpl) Core
//                    .getMarkerSheetBuilder()).loadFromInputSource(source, this,
//                            new WorkbookMarkerResourceProvider(workbook));
//            workbook.setMarkerSheet((MarkerSheetImpl) markerSheet);
//        } catch (IOException e) {
//            throw e;
//        } catch (CoreException e) {
//            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
//                throw e;
//        }
//    }
//
//    private void loadComments() throws IOException, CoreException {
//        try {
//            ICommentManager commentManager = ((CommentManagerBuilderImpl) Core
//                    .getCommentManagerBuilder()).loadFromInputSource(source,
//                            this);
//            workbook.setCommentManager((CommentManagerImpl) commentManager);
//        } catch (IOException e) {
//            throw e;
//        } catch (CoreException e) {
//            if (e.getType() != Core.ERROR_NO_SUCH_ENTRY)
//                throw e;
//        }
//    }
//
//    private void initWorkbook() throws IOException, CoreException {
//        // Prefetch all file entries:
//        workbook.getManifest().getFileEntries();
//        initWorkbookContents(workbook);
//        workbook.setInitialPassword(password);
//    }
//
//    private void initWorkbookContents(WorkbookImpl workbook) {
//        for (ISheet s : workbook.getSheets()) {
//            initSheet(s, workbook);
//        }
//    }
//
//    private void initSheet(ISheet sheet, WorkbookImpl wb) {
//        ((SheetImpl) sheet).addNotify(wb);
//
//        // Prefetch all revisions of this sheet.
//        workbook.getRevisionRepository().getRevisionManager(sheet.getId(),
//                IRevision.SHEET);
//    }
//
//    private Document forceLoadXML(String entryPath)
//            throws IOException, CoreException {
//        try {
//            return loadXMLFile(source, entryPath);
//        } catch (Throwable e) {
//            if (e instanceof CoreException) {
//                CoreException coreEx = (CoreException) e;
//                if (coreEx.getType() == Core.ERROR_WRONG_PASSWORD
//                        || coreEx.getType() == Core.ERROR_CANCELLATION) {
//                    throw coreEx;
//                }
//            }
//            //in case the file is damaged, 
//            //try continue loading
//            Core.getLogger().log(e, "Faild to load " + entryPath); //$NON-NLS-1$
//            return createDocument();
//        }
//    }
//
//    private InputStream getInputStream(IInputSource source, String entryPath)
//            throws IOException, CoreException {
//        if (manifest != null) {
//            IFileEntry entry = manifest.getFileEntry(entryPath);
//            if (entry != null) {
//                return entry.openInputStream();
//            }
//        }
//        if (!source.hasEntry(entryPath))
//            return null;
//
//        InputStream in = source.openEntryStream(entryPath);
//        IEncryptionData encData = manifest.getEncryptionData(entryPath);
//        if (encData != null) {
//            in = createDecryptedStream(in, encData);
//        }
//        return in;
//    }
//
//    private InputStream createDecryptedStream(InputStream in,
//            IEncryptionData encData) throws CoreException {
//        String password = getPassword();
//        if (password == null)
//            throw new CoreException(Core.ERROR_CANCELLATION);
//        return Crypto.createInputStream(in, false, encData, password);
//    }
//
//    private String getPassword() throws CoreException {
//        if (password == null) {
//            if (encryptionHandler != null) {
//                password = encryptionHandler.retrievePassword();
//            }
//        }
//        return password;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * org.xmind.core.internal.dom.XMLLoader#doLoadXMLFile(org.xmind.core.io
//     * .IInputSource, java.lang.String)
//     */
//    protected Document doLoadXMLFile(IInputSource source, String entryPath)
//            throws IOException, CoreException {
//        InputStream stream = getInputStream(source, entryPath);
//        if (stream == null)
//            throw new CoreException(Core.ERROR_NO_SUCH_ENTRY, entryPath);
//
//        Document doc;
//        try {
//            doc = builder.getDocumentLoader().parse(stream);
//        } catch (Throwable error) {
//            if (!verifyChecksum(source, entryPath, stream))
//                throw new CoreException(Core.ERROR_WRONG_PASSWORD, error);
//            if (error instanceof IOException)
//                throw (IOException) error;
//            if (error instanceof CoreException)
//                throw (CoreException) error;
//            throw new CoreException(Core.ERROR_FAIL_PARSING_XML, error);
//        } finally {
//            stream.close();
//        }
//
//        if (!verifyChecksum(source, entryPath, stream))
//            throw new CoreException(Core.ERROR_WRONG_PASSWORD);
//
//        return doc;
//    }
//
//    private boolean verifyChecksum(IInputSource source, String entryName,
//            InputStream stream) throws IOException, CoreException {
//        if (stream instanceof IChecksumStream) {
//            if (manifest == null) {
//                throw new IllegalStateException(
//                        "Manifest should not be encrypted"); //$NON-NLS-1$
//            }
//            IEncryptionData encData = manifest.getEncryptionData(entryName);
//            if (encData != null) {
//                String expectedChecksum = encData.getChecksum();
//                if (expectedChecksum != null) {
//                    String actualChecksum;
//                    actualChecksum = ((IChecksumStream) stream).getChecksum();
//                    if (actualChecksum == null
//                            || !expectedChecksum.equals(actualChecksum)) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    public Document createDocument() {
//        return builder.createDocument();
//    }
//
}
