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
 */
public class TempSaver {

//    private class Session {
//        IOutputTarget target;
//        Set<String> savedEntryPaths = new HashSet<String>();
//
//        void serialize(IAdaptable domAdaptable, String entryPath) {
//
//        }
//    }
//
//    private WorkbookImpl workbook;
//
//    private ManifestImpl manifest;
//
//    /**
//     * @param workbook
//     */
//    public TempSaver(WorkbookImpl workbook, ManifestImpl manifest) {
//        super();
//        this.workbook = workbook;
//        this.manifest = manifest;
//    }
//
//    /**
//     * @return the storage
//     */
//    public IStorage getStorage() {
//        return manifest.getStorage();
//    }
//
//    public IStorage getPrexiedStorage() {
//        return manifest.getPrefixedStorage();
//    }
//
//    public void save(String oldPassword, String newPassword)
//            throws IOException, CoreException {
//        String oldPrefix = prefixedStorage.getPrefix();
//        String newPrefix = newPassword == null ? "" //$NON-NLS-1$
//                : ".encrypted/" + digest(newPassword) + "/"; //$NON-NLS-1$ //$NON-NLS-2$
//
//        IOutputTarget target = storage.getOutputTarget();
//
//        IMeta meta = workbook.getMeta();
//        meta.setValue(IMeta.CREATOR_NAME,
//                Core.getWorkbookBuilder().getCreatorName());
//        meta.setValue(IMeta.CREATOR_VERSION,
//                Core.getWorkbookBuilder().getCreatorVersion());
//        serialize(target, meta, META_XML);
//
//        serialize(target, workbook, CONTENT_XML);
//
//        IMarkerSheet markerSheet = workbook.getMarkerSheet();
//        if (!markerSheet.isEmpty()) {
//            serialize(target, markerSheet, PATH_MARKER_SHEET);
//        }
//
//        IStyleSheet styleSheet = workbook.getStyleSheet();
//        if (!styleSheet.isEmpty()) {
//            serialize(target, styleSheet, STYLES_XML);
//        }
//
//        ICommentManager commentManager = workbook.getCommentManager();
//        if (!commentManager.isEmpty()) {
//            serialize(target, commentManager, COMMENTS_XML);
//        }
//
//        IRevisionRepository revisionRepository = workbook
//                .getRevisionRepository();
//        for (String resourceId : revisionRepository
//                .getRegisteredResourceIds()) {
//            IRevisionManager manager = revisionRepository
//                    .getRegisteredRevisionManager(resourceId);
//            String path = PATH_REVISIONS + resourceId + "/" //$NON-NLS-1$
//                    + REVISIONS_XML;
//            serialize(target, manager, path);
//        }
//
//        IManifest manifest = workbook.getManifest();
//        serialize(target, manifest, MANIFEST_XML);
//
//    }
//
////    private void saveStorage(IStorage sourceStorage, IOutputTarget target)
////            throws CoreException, IOException {
////        IInputSource source = storage.getInputSource();
////        Iterator<String> entries = source.getEntries();
////        while (entries.hasNext()) {
////            String entryPath = entries.next();
////            if (entryPath != null && !"".equals(entryPath) //$NON-NLS-1$
////                    && !hasBeenSaved(entryPath)) {
////                saveStorageEntry(source, target, entryPath);
////                markSaved(entryPath);
////            }
////        }
////    }
////
////    private void clearEncryptionData() {
////        for (IFileEntry entry : workbook.getManifest().getFileEntries()) {
////            entry.deleteEncryptionData();
////        }
////    }
////
////    /**
////     * @param source
////     * @param target
////     * @param entryPath
////     */
////    private void saveStorageEntry(IInputSource source, IOutputTarget target,
////            String entryPath) {
////        try {
////            InputStream in = getInputStream(source, entryPath);
////            if (in != null) {
////                try {
////                    long time = source.getEntryTime(entryPath);
////                    if (time >= 0) {
////                        target.setEntryTime(entryPath, time);
////                    }
////                    OutputStream out = getOutputStream(target, entryPath);
////                    if (out != null) {
////                        try {
////                            byte[] byteBuffer = new byte[1024];
////                            int numBytes;
////                            while ((numBytes = in.read(byteBuffer)) > 0) {
////                                out.write(byteBuffer, 0, numBytes);
////                            }
////                        } finally {
////                            out.close();
////                        }
////                    }
////                } finally {
////                    in.close();
////                }
////            }
////        } catch (IOException e) {
////            Core.getLogger().log(e);
////        } catch (CoreException e) {
////            Core.getLogger().log(e);
////        }
////    }
////
////    private InputStream getInputStream(IInputSource source, String entryPath)
////            throws IOException, CoreException {
////        if (source.hasEntry(entryPath)) {
////            return source.getEntryStream(entryPath);
////        }
////        return null;
////    }
//
//    private void serialize(IOutputTarget target, IAdaptable domAdapter,
//            String entryPath) throws IOException, CoreException {
//        OutputStream out = getOutputStream(target, entryPath);
//        if (out != null) {
////            try {
//            DOMUtils.save(domAdapter, out, true);
////            } finally {
////                markSaved(entryPath);
////            }
//        }
//    }
//
//    private OutputStream getOutputStream(IOutputTarget target, String entryPath)
//            throws IOException, CoreException {
////        if (!target.isEntryAvaialble(entryPath))
////            return null;
////
////        return target.openEntryStream(entryPath);
//
//        OutputStream out = target.openEntryStream(entryPath);
//
//        String password = workbook.getPassword();
//        if (password == null)
//            return out;
//
//        IFileEntry entry = workbook.getManifest().getFileEntry(entryPath);
//        if (entry == null)
//            return out;
//
//        if (ignoresEncryption(entry, entryPath))
//            return out;
//
//        IEncryptionData encData = entry.createEncryptionData();
//        return Crypto.creatOutputStream(out, true, encData, password);
//    }
//
//    private boolean ignoresEncryption(IFileEntry entry, String entryPath) {
//        return MANIFEST_XML.equals(entryPath)
//                || ((FileEntryImpl) entry).isIgnoreEncryption();
//    }
//
////    private boolean hasBeenSaved(String entryPath) {
////        return savedEntries != null && savedEntries.contains(entryPath);
////    }
////
////    /**
////     * @param entryPath
////     */
////    private void markSaved(String entryPath) {
////        if (savedEntries == null)
////            savedEntries = new HashSet<String>();
////        savedEntries.add(entryPath);
////    }
//
//    private static String digest(String str) {
//        return UUID.randomUUID().toString();
//    }
//
}
