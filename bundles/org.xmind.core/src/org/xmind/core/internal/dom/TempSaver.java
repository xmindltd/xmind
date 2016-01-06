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

import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.internal.security.Crypto;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;

/**
 * @author frankshaka
 * 
 */
public class TempSaver {

    private WorkbookImpl workbook;

    private IStorage storage;

    private IStorage oldStorage;

    private Set<String> savedEntries;

    /**
     * @param workbook
     */
    public TempSaver(WorkbookImpl workbook) {
        super();
        this.workbook = workbook;
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        if (storage == null) {
            storage = new ByteArrayStorage();
        }
        return storage;
    }

    /**
     * @param storage
     *            the storage to set
     */
    public void setStorage(IStorage storage) {
        if (storage == this.storage)
            return;

        if (oldStorage != null) {
            oldStorage = this.storage;
        }
        this.storage = storage;
    }

    public void save() throws IOException, CoreException {
        savedEntries = new HashSet<String>();
        try {
            saveAll();
        } finally {
            savedEntries = null;
            oldStorage = null;
        }
    }

    private void saveAll() throws IOException, CoreException {
        if (storage == null)
            storage = createStorage();
        IOutputTarget target = storage.getOutputTarget();

        saveDOM(workbook.getMeta(), target, META_XML);
        saveDOM(workbook, target, CONTENT_XML);

        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        if (!markerSheet.isEmpty()) {
            saveDOM(markerSheet, target, PATH_MARKER_SHEET);
        }

        IStyleSheet styleSheet = workbook.getStyleSheet();
        if (!styleSheet.isEmpty()) {
            saveDOM(styleSheet, target, STYLES_XML);
        }

        if (oldStorage != null) {
            saveStorage(oldStorage, target);
        }
        IManifest manifest = workbook.getManifest();
        saveDOM(manifest, target, MANIFEST_XML);
        clearEncryptionData();

    }

    private IStorage createStorage() {
        return new ByteArrayStorage();
    }

    private void saveStorage(IStorage sourceStorage, IOutputTarget target)
            throws CoreException, IOException {
        IInputSource source = storage.getInputSource();
        Iterator<String> entries = source.getEntries();
        while (entries.hasNext()) {
            String entryPath = entries.next();
            if (entryPath != null && !"".equals(entryPath) //$NON-NLS-1$
                    && !hasBeenSaved(entryPath)) {
                saveStorageEntry(source, target, entryPath);
                markSaved(entryPath);
            }
        }
    }

    private void clearEncryptionData() {
        for (IFileEntry entry : workbook.getManifest().getFileEntries()) {
            entry.deleteEncryptionData();
        }
    }

    /**
     * @param source
     * @param target
     * @param entryPath
     */
    private void saveStorageEntry(IInputSource source, IOutputTarget target,
            String entryPath) {
        try {
            InputStream in = getInputStream(source, entryPath);
            if (in != null) {
                try {
                    long time = source.getEntryTime(entryPath);
                    if (time >= 0) {
                        target.setEntryTime(entryPath, time);
                    }
                    OutputStream out = getOutputStream(target, entryPath);
                    if (out != null) {
                        try {
                            byte[] byteBuffer = new byte[1024];
                            int numBytes;
                            while ((numBytes = in.read(byteBuffer)) > 0) {
                                out.write(byteBuffer, 0, numBytes);
                            }
                        } finally {
                            out.close();
                        }
                    }
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            Core.getLogger().log(e);
        } catch (CoreException e) {
            Core.getLogger().log(e);
        }
    }

    private InputStream getInputStream(IInputSource source, String entryPath)
            throws IOException, CoreException {
        if (source.hasEntry(entryPath)) {
            return source.getEntryStream(entryPath);
        }
        return null;
    }

    private void saveDOM(IAdaptable domAdapter, IOutputTarget target,
            String entryPath) throws IOException, CoreException {
        OutputStream out = getOutputStream(target, entryPath);
        if (out != null) {
            try {
                DOMUtils.save(domAdapter, out, true);
            } finally {
                markSaved(entryPath);
            }
        }
    }

    private OutputStream getOutputStream(IOutputTarget target, String entryPath)
            throws IOException, CoreException {
//        if (!target.isEntryAvaialble(entryPath))
//            return null;
//
//        return target.openEntryStream(entryPath);

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
        return savedEntries != null && savedEntries.contains(entryPath);
    }

    /**
     * @param entryPath
     */
    private void markSaved(String entryPath) {
        if (savedEntries == null)
            savedEntries = new HashSet<String>();
        savedEntries.add(entryPath);
    }

}
