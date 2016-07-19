package org.xmind.core.internal.security;

/*
 * CipherOutputStream.java -- Filters output through a cipher. Copyright (C)
 * 2004 Free Software Foundation, Inc.
 * 
 * This file is part of GNU Classpath.
 * 
 * GNU Classpath is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2, or (at your option) any later version.
 * 
 * GNU Classpath is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * GNU Classpath; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.crypto.BufferedBlockCipher;

public class BlockCipherOutputStream extends FilterOutputStream {
    private BufferedBlockCipher cipher;
    private int lastOutputSize = -1;

    public BlockCipherOutputStream(OutputStream out,
            BufferedBlockCipher cipher) {
        super(out);
        this.cipher = cipher;
    }

    public void close() throws IOException {
        try {
            if (lastOutputSize >= 0) {
                byte[] buf = new byte[lastOutputSize];
                int num = cipher.doFinal(buf, 0);
                if (num > 0) {
                    out.write(buf, 0, num);
                }
            }
            out.flush();
            out.close();
            lastOutputSize = -1;
        } catch (Exception cause) {
            IOException ioex = new IOException(String.valueOf(cause));
            ioex.initCause(cause);
            throw ioex;
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

    /**
     * Write a byte array to the output stream.
     * 
     * @param buf
     *            The next bytes.
     * @throws IOException
     *             If an I/O error occurs, or if the underlying cipher is not in
     *             the correct state to transform data.
     */
    public void write(byte[] buf) throws IOException {
        write(buf, 0, buf.length);
    }

    public void write(byte[] b, int off, int le) throws IOException {
        lastOutputSize = cipher.getOutputSize(le);
        byte[] buf = new byte[lastOutputSize];
        int num = cipher.processBytes(b, off, le, buf, 0);
        if (num > 0) {
            out.write(buf, 0, num);
        }
    }
}
