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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Invisible characters are encoded using
 * <a href="https://en.wikipedia.org/wiki/Quoted-printable">Quoted-Printable
 * codec</a>.
 * 
 * @author Frank Shaka
 * @since 3.6.51
 */
public class LoggingOutputStream extends FilterOutputStream {

    private static final byte MIN_VISIBLE_CHAR = (byte) 32;
    private static final byte MAX_VISIBLE_CHAR = (byte) 126;

    private static final byte PREFIX = (byte) '=';
    private static final byte LF = (byte) '\n';
    private static final byte CR = (byte) '\r';
    private static final byte START1 = (byte) '0';
    private static final byte START2 = (byte) ('A' - 10);

    private byte[] buffer;

    /**
     * @param out
     */
    public LoggingOutputStream(OutputStream out) {
        super(out);
        this.buffer = new byte[1024];
    }

    private int quote(byte[] b, int off, int len) {
        int buffered = 0;
        int n;
        for (int i = 0; i < len; i++) {
            n = quote(b[i], buffered);
            buffered += n;
        }
        return buffered;
    }

    private int quote(byte b, int bufferOffset) {
        if (b != LF && b != CR && (b == PREFIX || b < MIN_VISIBLE_CHAR
                || b > MAX_VISIBLE_CHAR)) {
            ensureBufferSize(bufferOffset + 2);
            buffer[bufferOffset] = PREFIX;
            buffer[bufferOffset + 1] = toHex((b & 0xF0) >> 4);
            buffer[bufferOffset + 2] = toHex(b & 0x0F);
            return 3;
        } else {
            ensureBufferSize(bufferOffset);
            buffer[bufferOffset] = b;
            return 1;
        }
    }

    private static final byte toHex(int x) {
        if (x < 10) {
            return (byte) (START1 + x);
        }
        return (byte) (START2 + x);
    }

    /**
     * @param expectedSize
     */
    private void ensureBufferSize(int expectedSize) {
        if (expectedSize >= buffer.length) {
            byte[] newBuffer = new byte[(int) (buffer.length * 1.4)];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        int n = quote(b, 0, b.length);
        try {
            out.write(buffer, 0, n);
            out.flush();
        } catch (IOException e) {
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int n = quote(b, off, len);
        try {
            out.write(buffer, 0, n);
            out.flush();
        } catch (IOException e) {
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.FilterOutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        int n = quote((byte) b, 0);
        try {
            out.write(buffer, 0, n);
            out.flush();
        } catch (IOException e) {
        }
    }

}
