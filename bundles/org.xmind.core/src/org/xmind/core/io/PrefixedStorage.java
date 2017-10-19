package org.xmind.core.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class PrefixedStorage implements IStorage {

    private class PrefixedInputSource implements IInputSource, Closeable {

        private IInputSource source;

        public PrefixedInputSource(IInputSource source) {
            this.source = source;
        }

        public boolean hasEntry(String entryName) {
            return source.hasEntry(prefix + entryName);
        }

        public Iterator<String> getEntries() {
            return new Iterator<String>() {
                Iterator<String> it = source.getEntries();
                String next = findNext();

                private String findNext() {
                    while (it.hasNext()) {
                        String n = it.next();
                        if (n != null && n.startsWith(prefix)) {
                            return n.substring(prefix.length());
                        }
                    }
                    return null;
                }

                public void remove() {
                }

                public String next() {
                    String n = next;
                    next = findNext();
                    return n;
                }

                public boolean hasNext() {
                    return next != null;
                }
            };
        }

        public boolean isEntryAvailable(String entryName) {
            return source.isEntryAvailable(prefix + entryName);
        }

        @Deprecated
        public InputStream getEntryStream(String entryName) {
            return source.getEntryStream(prefix + entryName);
        }

        public InputStream openEntryStream(String entryName)
                throws IOException {
            return source.openEntryStream(prefix + entryName);
        }

        public long getEntrySize(String entryName) {
            return source.getEntrySize(prefix + entryName);
        }

        public long getEntryTime(String entryName) {
            return source.getEntryTime(prefix + entryName);
        }

        public void close() throws IOException {
            if (source instanceof Closeable) {
                ((Closeable) source).close();
            }
        }

    }

    private class PrefixedOutputTarget implements IOutputTarget, Closeable {

        private IOutputTarget target;

        public PrefixedOutputTarget(IOutputTarget target) {
            this.target = target;
        }

        public boolean isEntryAvaialble(String entryName) {
            return target.isEntryAvaialble(prefix + entryName);
        }

        @Deprecated
        public OutputStream getEntryStream(String entryName) {
            return target.getEntryStream(prefix + entryName);
        }

        public OutputStream openEntryStream(String entryName)
                throws IOException {
            return target.openEntryStream(prefix + entryName);
        }

        public void setEntryTime(String entryName, long time) {
            target.setEntryTime(prefix + entryName, time);
        }

        public void close() throws IOException {
            if (target instanceof Closeable) {
                ((Closeable) target).close();
            }
        }

    }

    private IStorage storage;

    private String prefix;

    public PrefixedStorage(IStorage storage, String prefix) {
        this.storage = storage;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public IStorage getStorage() {
        return storage;
    }

    public IInputSource getInputSource() {
        return new PrefixedInputSource(storage.getInputSource());
    }

    public IOutputTarget getOutputTarget() {
        return new PrefixedOutputTarget(storage.getOutputTarget());
    }

    public String getName() {
        return storage.getName();
    }

    public String getFullPath() {
        return storage.getFullPath();
    }

    public void clear() {
        Set<String> entriesToDelete = new HashSet<String>();
        Iterator<String> it = storage.getInputSource().getEntries();
        while (it.hasNext()) {
            String entry = it.next();
            while (entry.startsWith(prefix)) {
                entriesToDelete.add(entry);
                int slashIndex = entry.lastIndexOf('/');
                if (slashIndex <= 0)
                    break;
                entry = entry.substring(0, slashIndex);
            }
        }
        List<String> entryList = new ArrayList<String>(entriesToDelete);
        Collections.sort(entryList, Collections.reverseOrder());
        for (String entry : entryList) {
            storage.deleteEntry(entry);
        }
        storage.deleteEntry(prefix);
    }

    public void deleteEntry(String entryName) {
        storage.deleteEntry(prefix + entryName);
    }

    public void renameEntry(String entryName, String newName) {
        storage.renameEntry(prefix + entryName, prefix + newName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return storage.hashCode() ^ prefix.hashCode();
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
        if (obj == null || !(obj instanceof PrefixedStorage))
            return false;
        PrefixedStorage that = (PrefixedStorage) obj;
        return this.storage.equals(that.storage)
                && this.prefix.equals(that.prefix);
    }
}
