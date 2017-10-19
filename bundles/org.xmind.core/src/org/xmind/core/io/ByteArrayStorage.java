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
package org.xmind.core.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author frankshaka
 * 
 */
public class ByteArrayStorage implements IStorage {

    private static Collection<String> NO_ENTRIES = Collections.emptyList();

    protected class ByteArrayInputSource implements IInputSource {

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntries()
         */
        public Iterator<String> getEntries() {
            return dataTable == null ? NO_ENTRIES.iterator()
                    : dataTable.keySet().iterator();
        }

        public boolean isEntryAvailable(String entryName) {
            return dataTable != null && dataTable.get(entryName) != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntryStream(java.lang.String)
         */
        public InputStream getEntryStream(String entryName) {
            if (dataTable != null && entryName != null) {
                byte[] bs = dataTable.get(entryName);
                if (bs != null) {
                    return new ByteArrayInputStream(bs);
                }
            }
            return null;
        }

        public InputStream openEntryStream(String entryName)
                throws IOException {
            InputStream stream = getEntryStream(entryName);
            if (stream == null)
                throw new FileNotFoundException(entryName);
            return stream;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntrySize(java.lang.String)
         */
        public long getEntrySize(String entryName) {
            if (dataTable != null && entryName != null) {
                byte[] bs = dataTable.get(entryName);
                if (bs != null) {
                    return bs.length;
                }
            }
            return -1;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#getEntryTime(java.lang.String)
         */
        public long getEntryTime(String entryName) {
            if (timeTable != null && entryName != null) {
                Long time = timeTable.get(entryName);
                if (time != null)
                    return time.longValue();
            }
            return 0;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IInputSource#hasEntry(java.lang.String)
         */
        public boolean hasEntry(String entryName) {
            return dataTable != null && dataTable.containsKey(entryName);
        }

    }

    protected class ByteArrayOutputTarget implements IOutputTarget {

        private class EntryByteArrayOutputStream extends ByteArrayOutputStream {

            private String entryName;

            /**
             * 
             */
            public EntryByteArrayOutputStream(String entryName) {
                this.entryName = entryName;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.OutputStream#flush()
             */
            @Override
            public void flush() throws IOException {
                super.flush();
                pushBytes();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.ByteArrayOutputStream#close()
             */
            @Override
            public void close() throws IOException {
                super.close();
                pushBytes();
                synchronized (ByteArrayOutputTarget.this) {
                    if (timeTable == null
                            || !timeTable.containsKey(entryName)) {
                        setEntryTime(entryName, System.currentTimeMillis());
                    }
                }
            }

            /**
             * 
             */
            private void pushBytes() {
                putEntryData(entryName, toByteArray());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IOutputTarget#getEntryStream(java.lang.String)
         */
        public OutputStream getEntryStream(String entryName) {
            return new EntryByteArrayOutputStream(entryName);
        }

        public OutputStream openEntryStream(String entryName)
                throws IOException {
            return new EntryByteArrayOutputStream(entryName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xmind.core.io.IOutputTarget#isEntryAvaialble(java.lang.String)
         */
        public boolean isEntryAvaialble(String entryName) {
            return entryName != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xmind.core.io.IOutputTarget#setEntryTime(long)
         */
        public void setEntryTime(String entryName, long time) {
            synchronized (this) {
                if (timeTable == null) {
                    timeTable = new HashMap<String, Long>();
                }
                timeTable.put(entryName, time);
            }
        }

    }

    private Map<String, byte[]> dataTable;

    private Map<String, Long> timeTable;

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getFullPath()
     */
    public String getFullPath() {
        return getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getInputSource()
     */
    public IInputSource getInputSource() {
        return new ByteArrayInputSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getName()
     */
    public String getName() {
        return toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IArchive#getOutputTarget()
     */
    public IOutputTarget getOutputTarget() {
        return new ByteArrayOutputTarget();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IStorage#clear()
     */
    public void clear() {
        dataTable = null;
        timeTable = null;
    }

    public void deleteEntry(String entryName) {
        if (dataTable != null) {
            dataTable.remove(entryName);
        }
        if (timeTable != null) {
            timeTable.remove(entryName);
        }
    }

    public void renameEntry(String entryName, String newName) {
        if (dataTable != null) {
            byte[] data = dataTable.remove(entryName);
            if (data != null) {
                dataTable.put(newName, data);
            } else {
                dataTable.remove(newName);
            }
        }
        if (timeTable != null) {
            Long time = timeTable.remove(entryName);
            if (time != null) {
                timeTable.put(newName, time);
            } else {
                timeTable.remove(newName);
            }
        }
    }

    public byte[] getEntryData(String entryPath) {
        return dataTable == null ? null : dataTable.get(entryPath);
    }

    public void putEntryData(String entryPath, byte[] data) {
        if (dataTable == null) {
            dataTable = new HashMap<String, byte[]>();
        }
        dataTable.put(entryPath, data);
    }

}
