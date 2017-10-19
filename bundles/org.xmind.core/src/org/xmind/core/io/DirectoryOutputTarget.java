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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmind.core.Core;

public class DirectoryOutputTarget implements IOutputTarget {

    private class TimedFileOutputStream extends FileOutputStream {

        private String entryName;

        private File file;

        public TimedFileOutputStream(String entryName, File file)
                throws FileNotFoundException {
            super(file);
            this.entryName = entryName;
            this.file = file;
        }

        public void close() throws IOException {
            super.close();
            Long time = timeTable.remove(entryName);
            if (time != null) {
                file.setLastModified(time.longValue());
            }
        }

    }

    private File dir;

    private Map<String, Long> timeTable = new HashMap<String, Long>();

    public DirectoryOutputTarget(String path) {
        this.dir = new File(path);
    }

    public DirectoryOutputTarget(File file) {
        this.dir = file;
    }

    public OutputStream getEntryStream(String entryName) {
        if (!isAvailable())
            return null;

        try {
            return openEntryStream(entryName);
        } catch (IOException e) {
            Core.getLogger().log(e,
                    "Failed to get entry output stream for file: " //$NON-NLS-1$
                            + new File(dir, entryName).getPath());
            return null;
        }
    }

    public OutputStream openEntryStream(String entryName) throws IOException {
        File file = new File(dir, entryName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return new TimedFileOutputStream(entryName, file);
    }

    public boolean isEntryAvaialble(String entryName) {
        return isAvailable() && !new File(dir, entryName).isDirectory();
    }

    public boolean isAvailable() {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.exists() && dir.isDirectory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.io.IOutputTarget#setEntryTime(java.lang.String, long)
     */
    public void setEntryTime(String entryName, long time) {
        timeTable.put(entryName, Long.valueOf(time));
        File f = new File(dir, entryName);
        if (f.exists()) {
            f.setLastModified(time);
        }
    }

}