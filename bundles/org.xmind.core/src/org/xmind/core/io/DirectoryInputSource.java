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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmind.core.Core;

public class DirectoryInputSource implements IInputSource {

    private File dir;

    private FileFilter filter;

    public DirectoryInputSource(File file) {
        this(file, null);
    }

    public DirectoryInputSource(String path) {
        this(new File(path), null);
    }

    /**
     * 
     */
    public DirectoryInputSource(File file, FileFilter filter) {
        this.dir = file;
        this.filter = filter;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return dir;
    }

    public FileFilter getFilter() {
        return filter;
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    public String getName() {
        return dir.getName();
    }

    public Iterator<String> getEntries() {
        List<String> list = new ArrayList<String>();
        getSubFiles("", dir, list); //$NON-NLS-1$
        return list.iterator();
    }

    private void getSubFiles(String parentEntry, File parentFile,
            List<String> list) {
        if (!parentFile.isDirectory())
            return;

        for (File file : parentFile.listFiles()) {
            if (filter == null || filter.accept(file)) {
                String entryName;
                if ("".equals(parentEntry)) { //$NON-NLS-1$
                    entryName = file.getName();
                } else {
                    entryName = parentEntry + "/" + file.getName(); //$NON-NLS-1$
                }
                if (!file.isDirectory()) {
                    list.add(entryName);
                }
                getSubFiles(entryName, file, list);
            }
        }
    }

    public boolean isEntryAvailable(String entryName) {
        return isAvailable() && !new File(dir, entryName).isDirectory();
    }

    protected boolean isAvailable() {
        return dir.exists() && dir.isDirectory();
    }

    public boolean hasEntry(String entryName) {
        File f = new File(dir, entryName);
        return f.exists() && f.canRead()
                && (filter == null || filter.accept(f));
    }

    public InputStream getEntryStream(String entryName) {
        if (!isAvailable())
            return null;

        File file = new File(dir, entryName);
        if (file.isFile() && file.canRead()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Core.getLogger().log(e,
                        "Failed to get entry input stream: " + entryName); //$NON-NLS-1$
            }
        }
        return null;
    }

    public InputStream openEntryStream(String entryName) throws IOException {
        File file = new File(dir, entryName);
        if (!file.exists())
            throw new FileNotFoundException(file.toString());
        return new FileInputStream(file);
    }

    public boolean closeEntryStream(String entryPath, InputStream stream) {
        try {
            stream.close();
            return true;
        } catch (IOException e) {
            Core.getLogger().log(e,
                    "Failed to close entry input stream: " + entryPath); //$NON-NLS-1$
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IInputSource#getEntrySize(java.lang.String)
     */
    public long getEntrySize(String entryName) {
        File f = new File(dir, entryName);
        if (f.exists())
            return f.length();
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IInputSource#getEntryTime(java.lang.String)
     */
    public long getEntryTime(String entryName) {
        File f = new File(dir, entryName);
        if (f.exists())
            return f.lastModified();
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
        if (obj == null || !(obj instanceof DirectoryInputSource))
            return false;
        DirectoryInputSource that = (DirectoryInputSource) obj;
        return this.dir.equals(that.dir) && (this.filter == that.filter
                || (this.filter != null && this.filter.equals(that.filter)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.dir.hashCode()
                ^ (this.filter == null ? 1 : this.filter.hashCode());
    }

}