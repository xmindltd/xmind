/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.net.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class FileEntity extends HttpEntity {

    private File file;

    /**
     * 
     */
    public FileEntity(File file) {
        this.file = file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentLength()
     */
    @Override
    public long getContentLength() {
        return file.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        InputStream input = new FileInputStream(file);
        StreamedEntity.transfer(input, output,
                StreamedEntity.DEFAULT_BUFFER_SIZE);
    }

}
