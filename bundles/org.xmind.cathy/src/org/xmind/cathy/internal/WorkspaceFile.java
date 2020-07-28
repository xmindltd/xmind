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
package org.xmind.cathy.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorkspaceFile {

    private File file;

    private FileLock fileLock;

    private RandomAccessFile raFile;

    public WorkspaceFile(File file) {
        this.file = file;
    }

    public List<String> readLines() {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) { //$NON-NLS-1$
                    list.add(line);
                }
            }
        } catch (IOException e) {
            CathyPlugin.log(e, null);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
        return list;
    }

    public void writeLines(List<String> lines) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            Iterator<String> it = lines.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
                if (it.hasNext())
                    writer.newLine();
            }
        } catch (IOException e) {
            CathyPlugin.log(e, null);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public boolean lock() {
        try {
            raFile = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
        } catch (FileNotFoundException ignore) {
            return false;
        }
        try {
            fileLock = raFile.getChannel().tryLock();
        } catch (IOException ignore) {
        }
        if (fileLock != null)
            return true;
        try {
            raFile.close();
        } catch (IOException ignore) {
        }
        raFile = null;
        return false;
    }

    public void unlock() {
        if (fileLock != null) {
            try {
                fileLock.release();
            } catch (IOException ignore) {
            }
            fileLock = null;
        }
        if (raFile != null) {
            try {
                raFile.close();
            } catch (IOException ignore) {
            }
            raFile = null;
        }
    }

    public synchronized static boolean isLocked(File f) {
        WorkspaceFile fl = new WorkspaceFile(f);
        if (!fl.lock())
            return true;
        fl.unlock();
        return false;
    }

}