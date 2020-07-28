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
package org.xmind.core.internal.runtime;

import static org.xmind.core.internal.XmindCore.PLUGIN_ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.util.FileUtils;

public class WorkspaceSession {

    private File tempRoot;

    private File stampFile;

    private RandomAccessFile stampFileWrapper;

    private FileLock stampFileLock;

    public WorkspaceSession(File tempRoot, File stampFile,
            RandomAccessFile stampFileWrapper, FileLock stampFileLock) {
        this.tempRoot = tempRoot;
        this.stampFile = stampFile;
        this.stampFileWrapper = stampFileWrapper;
        this.stampFileLock = stampFileLock;
    }

    /**
     * Create a 'Workspace Stamp File' in the temp dir and lock it.
     * 
     * @throws CoreException
     *             when we fail to the create or lock of the 'Workspace Stamp
     *             File'
     */
    public static WorkspaceSession openSessionIn(File tempRoot)
            throws CoreException {
        if (!tempRoot.isDirectory()) {
            if (!tempRoot.mkdirs()) {
                throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
                        "Failed to create temporary directory at " //$NON-NLS-1$
                                + tempRoot.getAbsolutePath()));
            }
        }

        File stampFile;
        try {
            stampFile = File.createTempFile("xmind", ".core", tempRoot); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
                    "Failed to create workspace stamp file in " //$NON-NLS-1$
                            + tempRoot.getAbsolutePath(), e));
        }

        RandomAccessFile stampFileWrapper;
        try {
            stampFileWrapper = new RandomAccessFile(stampFile, "rw"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            stampFile.delete();
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
                    "Failed to find created workspace stamp file at " //$NON-NLS-1$
                            + stampFile.getAbsolutePath(), e));
        }

        FileLock stampFileLock = null;

        try {
            for (int err = 0; err < 10; err++) {
                try {
                    stampFileLock = stampFileWrapper.getChannel().tryLock();
                } catch (IOException e) {
                }
                if (stampFileLock != null)
                    break;
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
        }

        if (stampFileLock == null) {
            try {
                stampFileWrapper.close();
            } catch (IOException e) {
            }
            stampFile.delete();
            throw new CoreException(
                    new Status(
                            IStatus.ERROR,
                            PLUGIN_ID,
                            "Failed to lock workspace stamp file. Please make sure you have r/w permission at " //$NON-NLS-1$
                                    + tempRoot.getAbsolutePath()));
        }

        return new WorkspaceSession(tempRoot, stampFile, stampFileWrapper,
                stampFileLock);
    }

    public void close() {
        // Release stamp file lock and delete stamp file.
        deleteStampFile();

        // The last session in a temp dir is responsible for clearing the temp dir.
        if (!hasOtherSession()) {
            clearTempRoot();
        }
    }

    private void deleteStampFile() {
        if (stampFileLock != null) {
            try {
                stampFileLock.release();
            } catch (IOException e) {
            }
            stampFileLock = null;
        }
        if (stampFileWrapper != null) {
            try {
                stampFileWrapper.close();
            } catch (IOException e) {
            }
            stampFileWrapper = null;
        }
        if (stampFile != null) {
            stampFile.delete();
            stampFile = null;
        }
    }

    private boolean hasOtherSession() {
        if (!tempRoot.exists())
            return false;

        String[] stampFileNames = tempRoot.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".core"); //$NON-NLS-1$
            }
        });

        if (stampFileNames != null && stampFileNames.length > 0) {
            for (int i = 0; i < stampFileNames.length; i++) {
                File otherStampFile = new File(tempRoot, stampFileNames[i]);
                if (isLocked(otherStampFile))
                    return true;
            }
        }

        return false;
    }

    private boolean isLocked(File file) {
        try {
            RandomAccessFile s = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
            try {
                FileLock l = s.getChannel().tryLock();
                if (l == null)
                    return true;

                try {
                    l.release();
                } catch (IOException e2) {
                }
                return false;
            } finally {
                s.close();
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void clearTempRoot() {
        FileUtils.delete(tempRoot);
    }

}
