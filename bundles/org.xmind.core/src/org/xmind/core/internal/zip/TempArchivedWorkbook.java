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

import static org.xmind.core.internal.zip.ArchiveConstants.CONTENT_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.MANIFEST_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.META_XML;
import static org.xmind.core.internal.zip.ArchiveConstants.PATH_MARKER_SHEET;
import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.xmind.core.CoreException;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;

/**
 * 
 * @author frankshaka
 * @deprecated
 */
public class TempArchivedWorkbook extends AbstractArchivedWorkbook {

    private static final List<String> IGNORE_LIST = Arrays.asList(CONTENT_XML,
            PATH_MARKER_SHEET, STYLES_XML, MANIFEST_XML, META_XML);

    public TempArchivedWorkbook(IWorkbook workbook, String file) {
        super(workbook, file);
    }

    public InputStream getEntryInputStream(String entryPath) {
        File entryFile = new File(getFile(), entryPath);
        try {
            return new FileInputStream(entryFile);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public OutputStream getEntryOutputStream(String entryPath) {
        File entryFile = FileUtils.ensureFileParent(new File(getFile(),
                entryPath));
        try {
            return new FileOutputStream(entryFile);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public long getTime(String entryPath) {
        File entryFile = new File(getFile(), entryPath);
        if (entryFile.exists())
            return entryFile.lastModified();
        return -1;
    }

    public void setTime(String entryPath, long time) {
        if (time < 0)
            return;
        File entryFile = new File(getFile(), entryPath);
        if (entryFile.exists())
            entryFile.setLastModified(time);
    }

    public long getSize(String entryPath) {
        File entryFile = new File(getFile(), entryPath);
        if (entryFile.exists()) {
            long length = entryFile.length();
            if (length > 0)
                return length;
        }
        return -1;
    }

    public void save(IArchivedWorkbook source) throws IOException,
            CoreException {
        DOMUtils.save(workbook, getEntryOutputStream(CONTENT_XML), true);

        IMarkerSheet markerSheet = workbook.getMarkerSheet();
        if (!markerSheet.isEmpty()) {
            DOMUtils.save(markerSheet, getEntryOutputStream(PATH_MARKER_SHEET),
                    true);
        }

        IStyleSheet styleSheet = workbook.getStyleSheet();
        if (!styleSheet.isEmpty()) {
            DOMUtils.save(styleSheet, getEntryOutputStream(STYLES_XML), true);
        }

        IMeta meta = workbook.getMeta();
        DOMUtils.save(meta, getEntryOutputStream(META_XML), true);

        IManifest manifest = workbook.getManifest();
        DOMUtils.save(manifest, getEntryOutputStream(MANIFEST_XML), true);

        if (source != null && !equals(source)) {
            for (IFileEntry entry : manifest.getFileEntries()) {
                String path = entry.getPath();
                if (!IGNORE_LIST.contains(path) && !entry.isDirectory()) {
                    InputStream is = source.getEntryInputStream(path);
                    if (is != null) {
                        OutputStream os = getEntryOutputStream(path);
                        if (os != null) {
                            try {
                                FileUtils.transfer(is, os);
                            } catch (IOException e) {
                            }
                            setTime(path, source.getTime(path));
                        }
                    }
                }
            }
        }
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TempArchivedWorkbook))
            return false;
        TempArchivedWorkbook that = (TempArchivedWorkbook) obj;
        return new File(this.getFile()).equals(new File(that.getFile()));
    }

}