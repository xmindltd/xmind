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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmind.core.Core;
import org.xmind.core.io.IInputSource;

public class ZipFileInputSource implements IInputSource {

    private static class EntryIterAdapter implements Iterator<String> {

        private Enumeration<? extends ZipEntry> entries;

        /**
         * @param entries
         */
        public EntryIterAdapter(Enumeration<? extends ZipEntry> entries) {
            super();
            this.entries = entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return entries.hasMoreElements();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        public String next() {
            ZipEntry entry = entries.nextElement();
            if (entry == null)
                return null;
            return entry.getName();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private ZipFile zipFile;

    public ZipFileInputSource(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    public Iterator<String> getEntries() {
        return new EntryIterAdapter(zipFile.entries());
    }

    public boolean hasEntry(String entryName) {
        return zipFile.getEntry(entryName) != null;
    }

    public InputStream getEntryStream(String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null)
            return null;
        try {
            return zipFile.getInputStream(entry);
        } catch (IOException e) {
            Core.getLogger().log(e);
        }
        return null;
    }

    public InputStream openEntryStream(String entryName) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null)
            throw new FileNotFoundException(entryName);
        return zipFile.getInputStream(entry);
    }

    public boolean isEntryAvailable(String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        return entry != null && !entry.isDirectory();
    }

    public void closeZipFile() {
        try {
            zipFile.close();
        } catch (IOException e) {
            Core.getLogger().log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IInputSource#getEntrySize(java.lang.String)
     */
    public long getEntrySize(String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) {
            return entry.getSize();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IInputSource#getEntryTime(java.lang.String)
     */
    public long getEntryTime(String entryName) {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) {
            return entry.getTime();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ZipFileInputSource))
            return false;
        ZipFileInputSource that = (ZipFileInputSource) obj;
        return this.zipFile.equals(that.zipFile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.zipFile.hashCode();
    }

}