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
package org.xmind.core.net.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Frank Shaka
 * @since 3.6.51
 */
public class TeeInputStream extends FilterInputStream {

    private OutputStream out;

    /**
     * @param in
     */
    public TeeInputStream(InputStream in, OutputStream out) {
        super(in);
        this.out = out;
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#read()
     */
    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            out.write(b);
        }
        return b;
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        int n = in.read(b);
        if (n != -1) {
            out.write(b, 0, n);
        }
        return n;
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterInputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        if (n != -1) {
            out.write(b, off, n);
        }
        return n;
    }

}
