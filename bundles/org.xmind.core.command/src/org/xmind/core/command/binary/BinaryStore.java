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
package org.xmind.core.command.binary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.xmind.core.internal.command.BinaryUtil;
import org.xmind.core.internal.command.XMindCommandPlugin;

public class BinaryStore implements IBinaryStore {

    private Map<String, IBinaryEntry> entries = new HashMap<String, IBinaryEntry>();

    private List<String> entryNames = new ArrayList<String>();

    private File root;

    private byte[] buffer = new byte[4096];

    private int randomIndex = 1;

    public BinaryStore(File root) {
        this.root = root;
    }

    public BinaryStore(boolean useLocalFileCache) {
        if (useLocalFileCache) {
            File cacheDir = XMindCommandPlugin.getDefault()
                    .getBinaryCacheLocation();
            String rootName = String.format("%s-%s", //$NON-NLS-1$
                    String.valueOf(System.currentTimeMillis()), String
                            .valueOf(new Random(System.currentTimeMillis())
                                    .nextInt()));
            this.root = new File(cacheDir, rootName);
        } else {
            this.root = null;
        }
    }

    public BinaryStore() {
        this(true);
    }

    public synchronized Iterator<String> entryNames() {
        return this.entryNames.iterator();
    }

    public synchronized boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public synchronized int size() {
        return this.entries.size();
    }

    public synchronized IBinaryEntry getEntry(String entryName) {
        return this.entries.get(entryName);
    }

    public synchronized boolean hasEntry(String entryName) {
        return this.entries.containsKey(entryName);
    }

    public synchronized void clear() {
        Object[] oldEntries = this.entries.values().toArray();
        this.entries.clear();
        this.entryNames.clear();
        for (int i = 0; i < oldEntries.length; i++) {
            ((IBinaryEntry) oldEntries[i]).dispose();
        }
        if (this.root != null) {
            BinaryUtil.delete(this.root);
        }
    }

    public synchronized boolean removeEntry(String entryName) {
        IBinaryEntry entry = this.entries.remove(entryName);
        if (entry != null) {
            this.entryNames.remove(entryName);
            entry.dispose();
        }
        return entry != null;
    }

    public synchronized INamedEntry addEntry(IProgressMonitor monitor,
            InputStream source) throws IOException, InterruptedException {
        String entryName = generateRandomEntryName();
        IBinaryEntry realEntry = createEntry(monitor, entryName, source);
        NamedEntry entry = new NamedEntry(entryName, realEntry);
        addEntry(entryName, entry);
        return entry;
    }

    protected String generateRandomEntryName() {
        String entryName = Long.toHexString(System.currentTimeMillis()) + "-" //$NON-NLS-1$
                + (randomIndex++) + "-" //$NON-NLS-1$
                + Integer.toHexString(new Random().nextInt()) + ".tmp"; //$NON-NLS-1$
        return entryName;
    }

    public synchronized IBinaryEntry addEntry(IProgressMonitor monitor,
            String entryName, InputStream source) throws IOException,
            InterruptedException {
        IBinaryEntry entry = createEntry(monitor, entryName, source);
        addEntry(entryName, entry);
        return entry;
    }

    public synchronized void addEntry(String entryName, IBinaryEntry entry) {
        removeEntry(entryName);
        this.entries.put(entryName, entry);
        this.entryNames.add(entryName);
    }

    protected IBinaryEntry createEntry(IProgressMonitor monitor,
            String entryName, InputStream source) throws IOException,
            InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        if (monitor.isCanceled())
            throw new InterruptedException();
        if (this.root != null) {
            this.root.mkdirs();
        }
        if (monitor.isCanceled())
            throw new InterruptedException();

        if (this.root != null && this.root.isDirectory()) {
            if (entryName.endsWith("/")) { //$NON-NLS-1$
                File file = new File(this.root, entryName.substring(0,
                        entryName.length() - 1));
                if (monitor.isCanceled())
                    throw new InterruptedException();
                file.mkdirs();
                return IBinaryEntry.NULL;
            } else {
                File file = new File(this.root, entryName);
                file.getParentFile().mkdirs();
                if (monitor.isCanceled())
                    throw new InterruptedException();
                try {
                    OutputStream fout = new FileOutputStream(file);
                    try {
                        int read;
                        while ((read = source.read(buffer)) > 0) {
                            if (monitor.isCanceled())
                                throw new InterruptedException();
                            fout.write(buffer, 0, read);
                        }
                    } finally {
                        fout.close();
                    }
                } finally {
                    source.close();
                }
                if (monitor.isCanceled())
                    throw new InterruptedException();
                return new FileEntry(file);
            }
        } else {
            if (entryName.endsWith("/")) { //$NON-NLS-1$
                return IBinaryEntry.NULL;
            } else {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
                    try {
                        int read;
                        while ((read = source.read(buffer)) > 0) {
                            if (monitor.isCanceled())
                                throw new InterruptedException();
                            out.write(buffer, 0, read);
                        }
                        if (monitor.isCanceled())
                            throw new InterruptedException();
                        return new ByteArrayEntry(out.toByteArray());
                    } finally {
                        out.close();
                    }
                } finally {
                    source.close();
                }
            }
        }
    }

    @Override
    public String toString() {
        return entries.toString();
    }

}
