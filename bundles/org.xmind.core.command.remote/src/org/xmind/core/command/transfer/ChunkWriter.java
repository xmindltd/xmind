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
package org.xmind.core.command.transfer;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A helper class that writes data to an output stream in chunks.
 * 
 * @author Frank Shaka
 * @see ChunkReader
 */
public class ChunkWriter implements Closeable, Flushable {

    private class ChunkOutputStream extends FilterOutputStream {

        private boolean closed = false;

        public ChunkOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            flushBinaryBuffer(true);
            flushBuffer();
            if (!closed) {
                out.write(separator);
                out.flush();
            }
            closed = true;
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (closed)
                throw new IOException("Stream already closed."); //$NON-NLS-1$
            for (int i = 0; i < len; i++) {
                appendToBuffer(b[off + i]);
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (closed)
                throw new IOException("Stream already closed."); //$NON-NLS-1$
            appendToBuffer((byte) (b & 0xff));
        }

        @Override
        public void flush() throws IOException {
            flushBinaryBuffer(false);
            flushBuffer();
            out.flush();
        }

    }

    private static final byte DEFAULT_CHUNK_SEPARATOR = (byte) '\n';

    private static final String DEFAULT_TEXT_ENCODING = "UTF-8"; //$NON-NLS-1$

    private static final int DEFAULT_BUFFER_SIZE = 16;

    private static final int DEFAULT_BINARY_BUFFER_SIZE = DEFAULT_BUFFER_SIZE / 4 * 3;

    /**
     * This array is a lookup table that translates 6-bit positive integer index
     * values into their "Base64 Alphabet" equivalents as specified in Table 1
     * of RFC 2045.
     */
    private static final byte intToBase64[] = { 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/' };

    private OutputStream out;

    private String textEncoding;

    private byte separator;

    private ChunkOutputStream binaryStream = null;

    private byte[] binaryBuffer = new byte[DEFAULT_BINARY_BUFFER_SIZE];

    private int binaryBuffered = 0;

    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    private int buffered = 0;

    public ChunkWriter(OutputStream out) {
        this(out, DEFAULT_CHUNK_SEPARATOR, DEFAULT_TEXT_ENCODING);
    }

    public ChunkWriter(OutputStream out, byte chunkSeparator,
            String textEncoding) {
        this.out = out;
        this.separator = chunkSeparator;
        this.textEncoding = textEncoding;
    }

    public synchronized void writeText(String text) throws IOException {
        closeBinaryStream();
        if (text == null)
            text = ""; //$NON-NLS-1$
        byte[] bytes = text.getBytes(textEncoding);
        out.write(bytes);
        out.write(separator);
        out.flush();
    }

    public synchronized OutputStream openNextChunkAsStream() throws IOException {
        closeBinaryStream();
        return binaryStream = new ChunkOutputStream(out);
    }

    public synchronized void close() throws IOException {
        closeBinaryStream();
        try {
            out.flush();
        } finally {
            out.close();
        }
    }

    public synchronized void flush() throws IOException {
        flushBinaryBuffer(false);
        flushBuffer();
        out.flush();
    }

    private synchronized void closeBinaryStream() throws IOException {
        if (binaryStream != null && !binaryStream.closed) {
            binaryStream.close();
            binaryStream = null;
        }
    }

    private synchronized void appendToBuffer(byte b) throws IOException {
        binaryBuffer[binaryBuffered++] = b;
        if (binaryBuffered >= binaryBuffer.length) {
            int off = 0;
            while (off < binaryBuffered) {
                int b0 = binaryBuffer[off++] & 0xff;
                int b1 = off >= binaryBuffered ? -1
                        : binaryBuffer[off++] & 0xff;
                int b2 = off >= binaryBuffered ? -1
                        : binaryBuffer[off++] & 0xff;
                writeBinary(b0, b1, b2);
            }
            binaryBuffered = 0;
        }
    }

    private synchronized void writeBinary(int b0, int b1, int b2)
            throws IOException {
        buffer[buffered++] = intToBase64[b0 >> 2];
        if (b1 < 0) {
            buffer[buffered++] = intToBase64[(b0 << 4) & 0x3f];
            buffer[buffered++] = '=';
            buffer[buffered++] = '=';
        } else {
            buffer[buffered++] = intToBase64[(b0 << 4) & 0x3f | (b1 >> 4)];
            if (b2 < 0) {
                buffer[buffered++] = intToBase64[(b1 << 2) & 0x3f];
                buffer[buffered++] = '=';
            } else {
                buffer[buffered++] = intToBase64[(b1 << 2) & 0x3f | (b2 >> 6)];
                buffer[buffered++] = intToBase64[b2 & 0x3f];
            }
        }
        if (buffered >= buffer.length || b2 < 0) {
            try {
                out.write(buffer, 0, buffered);
            } finally {
                buffered = 0;
            }
        }
    }

    private synchronized void flushBinaryBuffer(boolean all) throws IOException {
        if (binaryBuffered > 0) {
            int off = 0;
            int max = all ? binaryBuffered : binaryBuffered - 2;
            while (off < max) {
                int b0 = binaryBuffer[off++] & 0xff;
                int b1 = off >= binaryBuffered ? -1
                        : binaryBuffer[off++] & 0xff;
                int b2 = off >= binaryBuffered ? -1
                        : binaryBuffer[off++] & 0xff;
                writeBinary(b0, b1, b2);
            }
            int newCount = binaryBuffered - off;
            if (newCount > 0) {
                System.arraycopy(binaryBuffer, off, binaryBuffer, 0, newCount);
            }
            binaryBuffered = newCount;
        }
    }

    private synchronized void flushBuffer() throws IOException {
        if (buffered > 0) {
            try {
                out.write(buffer, 0, buffered);
            } finally {
                buffered = 0;
            }
        }
    }

}
