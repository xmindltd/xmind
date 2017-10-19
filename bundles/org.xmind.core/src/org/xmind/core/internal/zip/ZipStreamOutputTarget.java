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

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.xmind.core.Core;
import org.xmind.core.io.IOutputTarget;

/**
 * @author frankshaka
 * 
 */
public class ZipStreamOutputTarget implements IOutputTarget, Closeable {

    private static final boolean DEFAULT_COMPRESSED = Boolean
            .getBoolean("org.xmind.core.workbook.compressed"); //$NON-NLS-1$

    private static class ZipEntryOutputStream extends FilterOutputStream {

        /**
         * @param out
         */
        public ZipEntryOutputStream(ZipOutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilterOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            out.flush();
            ((ZipOutputStream) out).closeEntry();
            // Don't call out.close() to close the ZIP output stream.
        }

    }

    private ZipOutputStream zip;

    private Map<String, Long> timeTable = new HashMap<String, Long>();

    /**
     * 
     */
    public ZipStreamOutputTarget(ZipOutputStream zip) {
        this(zip, DEFAULT_COMPRESSED);
    }

    public ZipStreamOutputTarget(ZipOutputStream zip, boolean compressed) {
        this.zip = zip;
        if (compressed) {
            zip.setLevel(Deflater.DEFAULT_COMPRESSION);
        } else {
            zip.setLevel(Deflater.NO_COMPRESSION);
        }
    }

    public void setEntryTime(String entryName, long time) {
        timeTable.put(entryName, Long.valueOf(time));
    }

    public OutputStream getEntryStream(String entryName) {
        try {
            return openEntryStream(entryName);
        } catch (IOException e) {
            Core.getLogger().log(e);
            return null;
        }
    }

    public OutputStream openEntryStream(String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);

        Long time = timeTable.remove(entryName);
        if (time != null) {
            entry.setTime(time.longValue());
        }

        zip.putNextEntry(entry);
        return new ZipEntryOutputStream(zip);
    }

    public boolean isEntryAvaialble(String entryName) {
        return zip != null;
    }

    public void close() throws IOException {
        zip.finish();
        zip.flush();
        zip.close();
    }

}
