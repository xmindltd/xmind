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
package org.xmind.ui.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.xmind.ui.io.IDownloadTarget.IDownloadTarget2;

/**
 * A <code>FileDownloadTarget</code> describes a download location in the local
 * file system.
 * 
 * @author Frank Shaka
 */
public class FileDownloadTarget implements IDownloadTarget, IDownloadTarget2 {

    private String filePath;

    private String tempFilePath;

    public FileDownloadTarget(String filePath, boolean useTempPath) {
        this.filePath = filePath;
        this.tempFilePath = useTempPath ? filePath + ".tmp" : filePath; //$NON-NLS-1$
    }

    public String getPath() {
        return filePath;
    }

    public OutputStream openOutputStream() throws IOException {
        File dir = new File(tempFilePath).getParentFile();
        if (dir != null)
            dir.mkdirs();
        return new FileOutputStream(tempFilePath);
    }

    public void afterDownload(IStatus status) {
        if (!status.isOK()) {
            new File(tempFilePath).delete();
        } else if (!filePath.equals(tempFilePath)) {
            new File(tempFilePath).renameTo(new File(filePath));
        }
    }

}