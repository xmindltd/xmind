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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_REVISIONS;
import static org.xmind.core.internal.zip.ArchiveConstants.REVISIONS_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IChecksumStream;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.internal.InternalCore;
import org.xmind.core.internal.security.Crypto;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.ICloseableOutputTarget;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;

/**
 * @author Frank Shaka
 * @deprecated Use <code>Core.getSerializerFactory().newSerializer()</code>
 */
@Deprecated
public class WorkbookSaver {

    private static class WorkbookSaveSession {

        /**
         * The workbook to save.
         */
        private final WorkbookImpl workbook;

        /**
         * The multi-entry output target to save to.
         */
        private final IOutputTarget target;

        /**
         * Entry paths that have been saved.
         */
        private Set<String> savedEntries = new HashSet<String>();

        /**
         * DOM transformer factory, lazy created.
         */
        private TransformerFactory transformerFactory = null;

        /**
         * Constructs a new WorkbookSaveSession.
         * 
         * @param workbook
         * @param target
         */
        public WorkbookSaveSession(WorkbookImpl workbook,
                IOutputTarget target) {
            this.workbook = workbook;
            this.target = target;
        }

        /**
         * The main saving process.
         * 
         * @throws IOException
         * @throws CoreException
         */
        public synchronized void save() throws IOException, CoreException {
            try {
                try {
                    try {
                        saveMeta();
                    } finally {
                        try {
                            saveContent();
                        } finally {
                            try {
                                saveMarkerSheet();
                            } finally {
                                try {
                                    saveStyleSheet();
                                } finally {
                                    try {
                                        saveComments();
                                    } finally {
                                        try {
                                            if (!workbook
                                                    .isSkipRevisionsWhenSaving()) {
                                                saveRevisions();
                                            }
                                        } finally {
                                            try {
                                                copyOtherStaff();
                                            } finally {
                                                saveManifest();
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                } finally {
                    clearEncryptionData();
                }
            } finally {
                if (target instanceof ICloseableOutputTarget) {
                    ((ICloseableOutputTarget) target).close();
                }
            }
        }

        private void saveManifest() throws IOException, CoreException {
            saveDOM(workbook.getManifest(), target, MANIFEST_XML);
        }

        private void saveStyleSheet() throws IOException, CoreException {
            IStyleSheet styleSheet = workbook.getStyleSheet();
            if (!styleSheet.isEmpty()) {
                saveDOM(styleSheet, target, STYLES_XML);
            }
        }

        private void saveMarkerSheet() throws IOException, CoreException {
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (!markerSheet.isEmpty()) {
                saveDOM(markerSheet, target, PATH_MARKER_SHEET);
            }
        }

        private void saveComments() throws IOException, CoreException {
        }

        private void saveRevisions() throws IOException, CoreException {
            Iterator<Element> sheets = DOMUtils.childElementIterByTag(
                    workbook.getWorkbookElement(), TAG_SHEET);
            while (sheets.hasNext()) {
                Element sheetEle = sheets.next();
                String sheetId = sheetEle.getAttribute(ATTR_ID);
                IRevisionManager manager = workbook.getRevisionRepository()
                        .getRevisionManager(sheetId, IRevision.SHEET);
                String path = PATH_REVISIONS + sheetId + "/" + REVISIONS_XML; //$NON-NLS-1$
                saveDOM(manager, target, path);
            }
        }

        private void saveContent() throws IOException, CoreException {
            saveDOM(workbook, target, CONTENT_XML);
        }

        private void saveMeta() throws IOException, CoreException {
            IMeta meta = workbook.getMeta();
//            meta.setValue(IMeta.CREATOR_NAME, workbook.getCurrentCreatorName());
//            meta.setValue(IMeta.CREATOR_VERSION, workbook.getCurrentCreatorVersion());
            saveDOM(meta, target, META_XML);
        }

        private void copyOtherStaff() throws IOException, CoreException {
            IInputSource source = workbook.getTempStorage().getInputSource();
            IManifest manifest = workbook.getManifest();
            for (IFileEntry entry : manifest.getFileEntries()) {
                if (!entry.isDirectory()) {
                    String entryPath = entry.getPath();
                    if (shouldSaveEntry(entryPath)) {
                        copyEntry(source, target, entryPath);
                        markSaved(entryPath);
                    }
                }
            }
        }

        private synchronized void copyEntry(IInputSource source,
                IOutputTarget target, String entryPath)
                        throws IOException, CoreException {
            InputStream in = getInputStream(source, entryPath);
            if (in == null) {
                Core.getLogger().log(
                        "Save workbook: failed to copy entry, input stream not avaiable: " //$NON-NLS-1$
                                + entryPath);
                return; // Entry source not found.
            }

            try {
                long time = source.getEntryTime(entryPath);
                if (time >= 0) {
                    target.setEntryTime(entryPath, time);
                }
                OutputStream out = getOutputStream(target, entryPath);
                try {
                    int numBytes;
                    byte[] byteBuffer = new byte[4096];
                    while ((numBytes = in.read(byteBuffer)) > 0) {
                        out.write(byteBuffer, 0, numBytes);
                    }
                    recordChecksum(entryPath, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }

        private boolean shouldSaveEntry(String entryPath) {
            return entryPath != null && !"".equals(entryPath) //$NON-NLS-1$
                    && !MANIFEST_XML.equals(entryPath)
                    && !hasBeenSaved(entryPath)
                    && !(workbook.isSkipRevisionsWhenSaving() && entryPath
                            .startsWith(ArchiveConstants.PATH_REVISIONS));
        }

        private void clearEncryptionData() {
            for (IFileEntry entry : workbook.getManifest().getFileEntries()) {
                entry.deleteEncryptionData();
            }
        }

        private void saveDOM(IAdaptable domAdapter, IOutputTarget target,
                String entryPath) throws IOException, CoreException {
            Node node = (Node) domAdapter.getAdapter(Node.class);
            if (node == null) {
                Core.getLogger()
                        .log("SaveWorkbook: No DOM node available for entry: " //$NON-NLS-1$
                                + entryPath);
                return;
            }

            if (transformerFactory == null) {
                try {
                    transformerFactory = TransformerFactory.newInstance();
                } catch (TransformerFactoryConfigurationError error) {
                    throw new CoreException(
                            Core.ERROR_FAIL_ACCESS_XML_TRANSFORMER,
                            "Failed to obtain XML transformer factory.", error); //$NON-NLS-1$
                }
            }

            Transformer transformer;
            try {
                transformer = transformerFactory.newTransformer();
            } catch (TransformerConfigurationException error) {
                throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_TRANSFORMER,
                        "Failed to create XML transformer for DOM entry '" //$NON-NLS-1$
                                + entryPath + "'.", //$NON-NLS-1$
                        error);
            }

            OutputStream out = getOutputStream(target, entryPath);
            try {
                transformer.transform(new DOMSource(node),
                        new StreamResult(out));
            } catch (TransformerException e) {
                throw new IOException(e.getLocalizedMessage(), e);
            } finally {
                out.close();
            }
            recordChecksum(entryPath, out);
            markSaved(entryPath);
        }

        private void recordChecksum(String entryPath, Object checksumProvider)
                throws IOException {
            if (checksumProvider instanceof IChecksumStream) {
                IEncryptionData encData = workbook.getManifest()
                        .getEncryptionData(entryPath);
                if (encData != null && encData.getChecksumType() != null) {
                    String checksum = ((IChecksumStream) checksumProvider)
                            .getChecksum();
                    if (checksum != null) {
                        encData.setAttribute(checksum,
                                DOMConstants.ATTR_CHECKSUM);
                    }
                }
            }
        }

        private InputStream getInputStream(IInputSource source,
                String entryPath) {
            if (source.hasEntry(entryPath)) {
                return source.getEntryStream(entryPath);
            }
            return null;
        }

        private OutputStream getOutputStream(IOutputTarget target,
                String entryPath) throws IOException, CoreException {
            OutputStream out = target.openEntryStream(entryPath);

            String password = workbook.getPassword();
            if (password == null)
                return out;

            IFileEntry entry = workbook.getManifest().getFileEntry(entryPath);
            if (entry == null)
                return out;

            if (ignoresEncryption(entry, entryPath))
                return out;

            IEncryptionData encData = entry.createEncryptionData();
            return Crypto.creatOutputStream(out, true, encData, password);
        }

        private boolean ignoresEncryption(IFileEntry entry, String entryPath) {
            return MANIFEST_XML.equals(entryPath)
                    || ((FileEntryImpl) entry).isIgnoreEncryption();
        }

        private boolean hasBeenSaved(String entryPath) {
            return savedEntries.contains(entryPath);
        }

        private void markSaved(String entryPath) {
            savedEntries.add(entryPath);
        }

    }

    /**
     * The workbook to save.
     */
    private final WorkbookImpl workbook;

    /**
     * The last target saved to.
     */
    private IOutputTarget lastTarget;

    /**
     * (Optional) The absolute path representing a ZIP file target
     */
    private String file;

    /**
     * Whether to skip revisions when saving.
     */
    private boolean skipRevisions = false;

    /**
     * @param workbook
     * @param file
     */
    public WorkbookSaver(WorkbookImpl workbook, String file) {
        super();
        this.workbook = workbook;
        this.file = file;
    }

    /**
     * @return the file path
     */
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @param skipRevisions
     *            the skipRevisions to set
     */
    public void setSkipRevisions(boolean skipRevisions) {
        this.skipRevisions = skipRevisions;
    }

    public boolean isSkipRevisions() {
        return skipRevisions;
    }

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    public synchronized void save() throws IOException, CoreException {
        doSave(this.lastTarget);
    }

    /**
     * 
     * @param output
     * @throws IOException
     * @throws CoreException
     */
    public synchronized void save(OutputStream output)
            throws IOException, CoreException {
        doSave(new ZipStreamOutputTarget(new ZipOutputStream(output)));
    }

    public synchronized void save(String file)
            throws IOException, CoreException {
        if (new File(file).isDirectory()) {
            doSave(new DirectoryOutputTarget(file));
        } else {
            FileOutputStream fout = new FileOutputStream(file);
            try {
                ZipOutputStream stream = new ZipOutputStream(fout);
                try {
                    doSave(new ZipStreamOutputTarget(stream));
                } finally {
                    stream.close();
                }
            } finally {
                fout.close();
            }
        }
        this.file = file;
    }

    public synchronized void save(IOutputTarget target)
            throws IOException, CoreException {
        doSave(target);
        this.lastTarget = target;
    }

    /**
     * 
     * @param target
     * @throws IOException
     * @throws CoreException
     */
    private synchronized void doSave(IOutputTarget target)
            throws IOException, CoreException {
        if (target == null)
            throw new FileNotFoundException("No target to save."); //$NON-NLS-1$

        if (InternalCore.DEBUG_WORKBOOK_SAVE)
            Core.getLogger().log(
                    "WorkbookSaver: About to save workbook to output target " //$NON-NLS-1$
                            + target.toString());
        new WorkbookSaveSession(workbook, target).save();
        if (InternalCore.DEBUG_WORKBOOK_SAVE)
            Core.getLogger().log(
                    "WorkbookSaver: Finished saving workbook to output target " //$NON-NLS-1$
                            + target.toString());
    }

}
