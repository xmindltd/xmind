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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.Assert;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class StreamedEntity extends HttpEntity {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private InputStream input;

    private long length;

    private int bufferSize;

    /**
     * 
     */
    public StreamedEntity(InputStream input, long length, int bufferSize) {
        super();
        Assert.isLegal(input != null);
        Assert.isLegal(bufferSize > 0);
        this.input = input;
        this.length = length;
        this.bufferSize = bufferSize;
    }

    /**
     * 
     */
    public StreamedEntity(InputStream input, long length) {
        this(input, length, DEFAULT_BUFFER_SIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.internal.Entity#writeTo(java.io.OutputStream)
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        transfer(input, output, bufferSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.net.http.HttpEntity#getContentLength()
     */
    @Override
    public long getContentLength() {
        return length;
    }

    public static void transfer(InputStream input, OutputStream output,
            int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int numRead;
        while ((numRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, numRead);
        }
    }

}
