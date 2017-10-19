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

import org.xmind.core.util.FileUtils;

/**
 * @author frankshaka
 * 
 */
public class DirectoryStorage implements IStorage {

    private File dir;

    private FileFilter filter;

    /**
     * 
     */
    public DirectoryStorage(File dir) {
        this(dir, null);
    }

    /**
     * 
     */
    public DirectoryStorage(File dir, FileFilter filter) {
        this.dir = dir;
        this.filter = filter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getFullPath()
     */
    public String getFullPath() {
        return dir.getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getInputSource()
     */
    public IInputSource getInputSource() {
        return new DirectoryInputSource(dir, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getName()
     */
    public String getName() {
        return dir.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IRandomAccessArchive#getOutputTarget()
     */
    public IOutputTarget getOutputTarget() {
        return new DirectoryOutputTarget(dir);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IStorage#clear()
     */
    public void clear() {
        FileUtils.delete(dir);
    }

    public void deleteEntry(String entryName) {
        File f = new File(dir, entryName);
        while (f != null && !f.equals(dir)) {
            if (f.isFile() || (f.isDirectory() && f.list().length == 0)) {
                f.delete();
            }
            f = f.getParentFile();
        }
        dir.delete();
    }

    public void renameEntry(String entryName, String newName) {
        File targetFile = new File(dir, newName);
        File targetParent = targetFile.getParentFile();
        if (targetParent != null) {
            targetParent.mkdirs();
        }
        new File(dir, entryName).renameTo(targetFile);
        deleteEntry(entryName);
    }

    @Override
    public String toString() {
        return dir.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return dir.hashCode() ^ (filter == null ? 37 : filter.hashCode());
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
        if (obj == null || !(obj instanceof DirectoryStorage))
            return false;
        DirectoryStorage that = (DirectoryStorage) obj;
        return this.dir.equals(that.dir) && (this.filter == that.filter
                || (this.filter != null && this.filter.equals(that.filter)));
    }

}
