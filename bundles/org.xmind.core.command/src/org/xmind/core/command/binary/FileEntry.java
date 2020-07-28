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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmind.core.internal.command.BinaryUtil;

public class FileEntry implements IBinaryEntry {

    private File file;

    public FileEntry(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public InputStream openInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    public void dispose() {
        BinaryUtil.delete(this.file);
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + " (" + file.length() + " bytes)"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
