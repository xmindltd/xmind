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
package org.xmind.core.internal.zip;


/**
 * @author briansun
 * @deprecated
 */
public class ArchivedWorkbook {
//extends AbstractArchivedWorkbook {
//
//    private static class ZipEntryInputStream extends InputStream {
//
//        private ZipFile zipFile;
//
//        private InputStream delegate;
//
//        public ZipEntryInputStream(ZipFile zipFile, InputStream delegate) {
//            this.zipFile = zipFile;
//            this.delegate = delegate;
//        }
//
//        public int available() throws IOException {
//            return delegate.available();
//        }
//
//        public void close() throws IOException {
//            try {
//                delegate.close();
//            } finally {
//                zipFile.close();
//            }
//        }
//
//        public int hashCode() {
//            return delegate.hashCode();
//        }
//
//        public synchronized void mark(int readlimit) {
//            delegate.mark(readlimit);
//        }
//
//        public boolean markSupported() {
//            return delegate.markSupported();
//        }
//
//        public int read() throws IOException {
//            return delegate.read();
//        }
//
//        public int read(byte[] b) throws IOException {
//            return delegate.read(b);
//        }
//
//        public int read(byte[] b, int off, int len) throws IOException {
//            return delegate.read(b, off, len);
//        }
//
//        public synchronized void reset() throws IOException {
//            delegate.reset();
//        }
//
//        public long skip(long n) throws IOException {
//            return delegate.skip(n);
//        }
//
//        public String toString() {
//            return delegate.toString();
//        }
//
//    }
//
//    private static final List<String> XML_PATHS = Arrays.asList(CONTENT_XML,
//            PATH_MARKER_SHEET, STYLES_XML, MANIFEST_XML, META_XML);
//
//    /**
//     * @param file
//     */
//    public ArchivedWorkbook(IWorkbook workbook, String file) {
//        super(workbook, file);
//    }
//
//    public OutputStream getEntryOutputStream(String entryPath) {
//        //ZipOutputStream os = new ZipOutputStream(
//        //        new FileOutputStream(getFile()));
//        //os.putNextEntry(new ZipEntry(entryPath));
//        //return os;
//
//        /*
//         * We can't provide an output stream for those who wants to insert,
//         * replace or delete some entry within a ZIP archive, for the
//         * ZipOutputStream will ignore all other existing contents in the
//         * archive and leave only the given entry.
//         */
//        return null;
//    }
//
//    public InputStream getEntryInputStream(String entryPath) {
//        InputStream stream = getZippedStream(entryPath);
//        if (stream != null) {
//            String password = workbook.getPassword();
//            if (password != null) {
//                if (map != null) {
//                    EncryptionData encData = map.get(entryPath);
//                    if (encData != null) {
//                        try {
////                            BufferedBlockCipher cipher = Crypto
////                                    .createBlockCipher(false, password, encData);
////                            stream = new BlockCipherInputStream(stream, cipher);
//                            stream = Crypto.createInputStream(stream,
//                                    false, encData, password);
//                        } catch (CoreException e) {
//                            Core.getLogger().log(e);
//                            return null;
//                        }
//                    }
//                }
//            }
//        }
//        return stream;
//    }
//
//    /**
//     * @param entryPath
//     * @return
//     */
//    private InputStream getZippedStream(String entryPath) {
//        try {
//            ZipFile zipFile = new ZipFile(getFile());
//            try {
//                ZipEntry entry = zipFile.getEntry(entryPath);
//                if (entry != null) {
//                    InputStream realInputStream = zipFile.getInputStream(entry);
//                    if (realInputStream != null)
//                        return new ZipEntryInputStream(zipFile, realInputStream);
//                }
//            } catch (Exception e) {
//            }
//            zipFile.close();
//        } catch (Exception e) {
//        }
//        return null;
//    }
//
//    public long getTime(String entryPath) {
//        try {
//            ZipFile zipFile = new ZipFile(getFile());
//            try {
//                ZipEntry e = zipFile.getEntry(entryPath);
//                if (e != null) {
//                    return e.getTime();
//                }
//            } finally {
//                zipFile.close();
//            }
//        } catch (Exception e) {
//        }
//        return -1;
//    }
//
//    public void setTime(String entryPath, long time) {
//    }
//
//    public long getSize(String entryPath) {
//        try {
//            ZipFile zipFile = new ZipFile(getFile());
//            try {
//                ZipEntry e = zipFile.getEntry(entryPath);
//                if (e != null) {
//                    return e.getSize();
//                }
//            } finally {
//                zipFile.close();
//            }
//        } catch (Exception e) {
//        }
//        return -1;
//    }
//
//    public void save(IArchivedWorkbook source) throws IOException,
//            CoreException {
//        File tempSource = null;
//        if (!isValidSource(source)) {
//            String tempDir = Core.getWorkspace().getAbsolutePath(
//                    IWorkspace.DIR_TEMP);
//            tempSource = new File(tempDir, Core.getIdFactory().createId());
//            extractTo(tempSource);
//        }
//
//        FileUtils.ensureFileParent(new File(getFile()));
//
//        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
//                getFile()));
//
//        try {
//            IManifest manifest = workbook.getManifest();
//
//            if (map != null) {
//                map.clear();
//            }
//
//            OutputStream out_CONTENT = getOutputStream(zos, CONTENT_XML,
//                    manifest);
//            DOMUtils.save(workbook, out_CONTENT, true);
//            if (out_CONTENT instanceof IChecksumStream)
//                saveChecksum(CONTENT_XML, ((IChecksumStream) out_CONTENT)
//                        .getChecksum());
//
//            IMarkerSheet markerSheet = workbook.getMarkerSheet();
//            if (!markerSheet.isEmpty()) {
//                DOMUtils.save(markerSheet, getOutputStream(zos,
//                        PATH_MARKER_SHEET, manifest), true);
//            }
//
//            IStyleSheet styleSheet = workbook.getStyleSheet();
//            if (!styleSheet.isEmpty()) {
//                OutputStream out_STYLES = getOutputStream(zos, STYLES_XML,
//                        manifest);
//                DOMUtils.save(styleSheet, out_STYLES, true);
//                if (out_STYLES instanceof IChecksumStream)
//                    saveChecksum(STYLES_XML, ((IChecksumStream) out_STYLES)
//                            .getChecksum());
//            }
//
//            IMeta meta = workbook.getMeta();
//            OutputStream out_META = getOutputStream(zos, META_XML, manifest);
//            DOMUtils.save(meta, out_META, true);
//            if (out_META instanceof IChecksumStream)
//                saveChecksum(META_XML, ((IChecksumStream) out_META)
//                        .getChecksum());
//
//            encapsulate(manifest, source, tempSource, zos);
//
//            //zos.putNextEntry(new ZipEntry(MANIFEST_XML));
//            OutputStream manifestOutput = getOutputStream(zos, MANIFEST_XML,
//                    manifest);
//            DOMUtils.save(manifest, manifestOutput, true);
//
//            clearEncryptionData(manifest);
//
//        } finally {
//            zos.close();
//            if (tempSource != null) {
//                FileUtils.delete(tempSource);
//                tempSource = null;
//            }
//        }
//    }
//
//    /**
//     * @param entryPath
//     * @param checksum
//     */
//    private void saveChecksum(String entryPath, String checksum) {
//        if (map != null) {
//            EncryptionData encData = map.get(entryPath);
//            if (encData != null) {
//                encData.setChecksum(checksum);
//            }
//        }
//    }
//
//    /**
//     * @param zippedOutput
//     * @param entryName
//     * @param manifest
//     * @return
//     * @throws IOException
//     */
//    private OutputStream getOutputStream(ZipOutputStream zippedOutput,
//            String entryName, IManifest manifest) throws CoreException,
//            IOException {
//
//        zippedOutput.putNextEntry(new ZipEntry(entryName));
//        OutputStream out = new NonCloseOutputStream(zippedOutput);
//
//        if (ignoresEncryption(entryName))
//            return out;
//
//        String password = workbook.getPassword();
//
//        if (password == null) {
//            return out;
//        }
//        Document mfDoc = (Document) manifest.getAdapter(Document.class);
//        if (mfDoc == null)
//            return out;
//
//        EncryptionData encData = Crypto.generateEncryptionData(mfDoc);
//        if (encData == null)
//            return out;
//
//        if (map == null)
//            map = new HashMap<String, EncryptionData>();
//        map.put(entryName, encData);
//
//        return Crypto.creatOutputStream(out, true, encData, password);
//
////        BufferedBlockCipher cipher = Crypto.createBlockCipher(true, password,
////                encData);
////        return new ChecksumOutputStream(
////                new BlockCipherOutputStream(out, cipher));
//    }
//
//    private static class NonCloseOutputStream extends FilterOutputStream {
//
//        public NonCloseOutputStream(OutputStream out) {
//            super(out);
//        }
//
//        @Override
//        public void close() throws IOException {
//            // don't close the underlying stream
//        }
//
//    }
//
//    /**
//     * @param entryName
//     * @return
//     */
//    private boolean ignoresEncryption(String entryName) {
//        return ArchiveConstants.MANIFEST_XML.equals(entryName);
//    }
//
//    private Map<String, EncryptionData> map = null;
//
//    /**
//     * @param map
//     *            the map to set
//     */
//    public void setEncryptionDataMap(Map<String, EncryptionData> map) {
//        this.map = map;
//    }
//
//    /**
//     * Encapsulate all preserved files from the directory back to the archive.
//     * 
//     * @param manifest
//     * @param source
//     * @param sourcePath
//     * @param target
//     */
//    private void encapsulate(IManifest manifest, IArchivedWorkbook source,
//            File sourcePath, ZipOutputStream target) {
//
//        for (IFileEntry entry : manifest.getFileEntries()) {
//            if (entry.isDirectory())
//                continue;
//
//            String path = entry.getPath();
//
//            if (!isXmlFile(path)) {
//                InputStream is = getInputStream(path, source, sourcePath);
//                if (is != null) {
////                    ZipEntry ze = new ZipEntry(path);
////                    ze.setTime(getTime(path, source, sourcePath));
//                    try {
//                        //target.putNextEntry(ze);
//                        FileUtils.transfer(is, getOutputStream(target, path,
//                                manifest), true);
//                    } catch (IOException e) {
//                    } catch (CoreException e) {
//                    } finally {
//                        try {
//                            is.close();
//                        } catch (IOException e) {
//                        }
//                    }
//                }
//            }
//            insertEncryptionData(entry, path);
//        }
//    }
//
//    /**
//     * @param manifest
//     */
//    private void clearEncryptionData(IManifest manifest) {
//        if (map != null) {
//            for (EncryptionData encData : map.values()) {
//                deleteEncryptionData(encData, manifest);
//            }
//        }
//    }
//
//    /**
//     * @param encData
//     * @param manifest
//     */
//    private void deleteEncryptionData(EncryptionData encData, IManifest manifest) {
//        Element encDataEle = (Element) encData.getAdapter(Element.class);
//        if (encDataEle != null) {
//            Node p = encDataEle.getParentNode();
//            if (p != null) {
//                p.removeChild(encDataEle);
//            }
//        }
//    }
//
//    /**
//     * @param entry
//     * @param path
//     */
//    private void insertEncryptionData(IFileEntry entry, String path) {
//        if (map == null || map.isEmpty())
//            return;
//
//        EncryptionData encData = map.get(path);
//        if (encData != null) {
//            Element entryEle = (Element) entry.getAdapter(Element.class);
//            Element encDataEle = (Element) encData.getAdapter(Element.class);
//            if (entryEle != null && encDataEle != null) {
//                entryEle.appendChild(encDataEle);
//            }
//        }
//    }
//
//    /**
//     * Extract all preserved files to directory.
//     * 
//     * @param destPath
//     */
//    private void extractTo(File destPath) {
//        try {
//            ZipFile zipFile = new ZipFile(getFile());
//            try {
//                extract(zipFile, destPath);
//            } finally {
//                zipFile.close();
//            }
//        } catch (IOException e) {
//        }
//    }
//
//    private void extract(ZipFile zipFile, File destPath) {
//        Enumeration<? extends ZipEntry> entries = zipFile.entries();
//        while (entries.hasMoreElements()) {
//            ZipEntry entry = entries.nextElement();
//            String path = entry.getName();
//            if (!isXmlFile(path)) {
//                try {
//                    InputStream is = zipFile.getInputStream(entry);
//                    if (is != null) {
//                        File f = FileUtils.ensureFileParent(new File(destPath,
//                                path));
//                        OutputStream os = new FileOutputStream(f);
//                        FileUtils.transfer(is, os);
//                        f.setLastModified(entry.getTime());
//                    }
//                } catch (IOException e) {
//                }
//            }
//        }
//    }
//
//    /**
//     * Checks whether a directory is needed for a re-encapsulation action.
//     * 
//     * @param source
//     * @return Whether to create a temporary directory.
//     */
//    private boolean isValidSource(IArchivedWorkbook source) {
//        return source != null && !equals(source);
//    }
//
//    private InputStream getInputStream(String entryPath,
//            IArchivedWorkbook source, File tempSource) {
//
//        if (isValidSource(source)) {
//            return source.getEntryInputStream(entryPath);
//        } else if (tempSource != null) {
//            try {
//                return new FileInputStream(new File(tempSource, entryPath));
//            } catch (FileNotFoundException e) {
//            }
//        }
//        return null;
//    }
//
////    private long getTime(String entryPath, IArchivedWorkbook source,
////            File tempSource) {
////        if (isValidSource(source)) {
////            return source.getTime(entryPath);
////        } else if (tempSource != null) {
////            File f = new File(tempSource, entryPath);
////            if (f.exists())
////                return f.lastModified();
////        }
////        return System.currentTimeMillis();
////    }
//
//    private static boolean isXmlFile(String path) {
//        return XML_PATHS.contains(path);
//    }
//
//    public boolean equals(Object obj) {
//        if (obj == this)
//            return true;
//        if (obj == null || !(obj instanceof ArchivedWorkbook))
//            return false;
//        ArchivedWorkbook that = (ArchivedWorkbook) obj;
//        return new File(getFile()).equals(new File(that.getFile()));
//    }
}