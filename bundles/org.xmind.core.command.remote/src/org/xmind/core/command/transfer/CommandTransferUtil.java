/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.command.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.xmind.core.command.binary.BinaryStore;
import org.xmind.core.command.binary.IBinaryEntry;
import org.xmind.core.command.binary.IBinaryStore;

/**
 * This utility class provides constants and convenient methods to
 * encode/decode/read/write data from/to input/output streams.
 * 
 * @author Frank Shaka
 */
public class CommandTransferUtil {

    public static final String ENCODING = "UTF-8"; //$NON-NLS-1$

    public static final String MARKER_PROPERTIES = "[PROPERTIES]"; //$NON-NLS-1$

    public static final String MARKER_VALUES = "[VALUES]"; //$NON-NLS-1$

    public static final String MARKER_FILES = "[FILES]"; //$NON-NLS-1$

    private CommandTransferUtil() {
    }

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, ENCODING);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, ENCODING);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    public static IBinaryStore readFiles(IProgressMonitor monitor,
            ChunkReader reader) throws IOException {
        String numFilesStr = reader.readText();
        if (numFilesStr == null)
            // Empty files
            return null;

        int numFiles;
        try {
            numFiles = Integer.parseInt(numFilesStr, 10);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Invalid format of files number: " + numFilesStr); //$NON-NLS-1$
        }
        monitor.beginTask(null, numFiles);

        IBinaryStore store = null;
        String entryName;
        try {
            while ((entryName = reader.readText()) != null) {
                if ("".equals(entryName) || monitor.isCanceled()) //$NON-NLS-1$
                    break;

                InputStream chunkStream = reader.openNextChunkAsStream();
                if (chunkStream != null) {
                    IProgressMonitor fileMonitor = new SubProgressMonitor(
                            monitor, 1);
                    if (store == null) {
                        store = new BinaryStore();
                    }
                    store.addEntry(fileMonitor, entryName, chunkStream);
                    if (monitor.isCanceled())
                        return null;
                    fileMonitor.done();
                }
            }
        } catch (InterruptedException e) {
            return null;
        }
        return store;
    }

    public static void writeFiles(IProgressMonitor monitor, IBinaryStore files,
            ChunkWriter writer) throws IOException {
        monitor.beginTask(null, files.size());
        writer.writeText(String.valueOf(files.size()));
        Iterator<String> entryNames = files.entryNames();
        while (entryNames.hasNext()) {
            String entryName = entryNames.next();
            IBinaryEntry entry = files.getEntry(entryName);
            writer.writeText(entryName);
            if (monitor.isCanceled())
                return;
            if (entry != null && !entryName.endsWith("/")) { //$NON-NLS-1$
                InputStream in = entry.openInputStream();
                if (monitor.isCanceled())
                    return;
                try {
                    OutputStream out = writer.openNextChunkAsStream();
                    if (monitor.isCanceled())
                        return;
                    try {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = in.read(buffer)) > 0) {
                            if (monitor.isCanceled())
                                return;
                            out.write(buffer, 0, read);
                        }
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            }
            if (monitor.isCanceled())
                return;
            monitor.worked(1);
        }
        if (monitor.isCanceled())
            return;
        monitor.done();
    }
}
