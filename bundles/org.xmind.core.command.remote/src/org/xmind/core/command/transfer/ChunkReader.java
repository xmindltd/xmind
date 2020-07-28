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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;

/**
 * A helper class that reads data from an input stream in chunks.
 * 
 * @author Frank Shaka
 * @see ChunkWriter
 */
public class ChunkReader implements Closeable {

    private class ChunkInputStream extends FilterInputStream {

        private boolean closed = false;

        public ChunkInputStream(InputStream in) {
            super(in);
            chunkEnded = false;
        }

        @Override
        public int read() throws IOException {
            if (closed)
                throw new IOException("Stream already closed."); //$NON-NLS-1$
            if (chunkEnded && binaryBuffered <= 0)
                return -1;
            if (binaryBuffered <= 0) {
                refillBinary();
            }
            if (chunkEnded && binaryBuffered <= 0)
                return -1;
            byte b = binaryBuffer[0];
            useBinary(1);
            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (closed)
                throw new IOException("Stream already closed."); //$NON-NLS-1$
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed)
                throw new IOException("Stream already closed."); //$NON-NLS-1$
            if (off < 0 || off + len > b.length)
                throw new IndexOutOfBoundsException(
                        "Offset or length out of bounds."); //$NON-NLS-1$
            int read = 0;
            while (len > 0) {
                if (chunkEnded && binaryBuffered <= 0)
                    break;
                if (binaryBuffered <= 0) {
                    refillBinary();
                }
                if (chunkEnded && binaryBuffered <= 0)
                    break;
                int size = Math.min(len, binaryBuffered);
                System.arraycopy(binaryBuffer, 0, b, off, size);
                read += size;
                off += size;
                len -= size;
                useBinary(size);
            }
            if (read == 0)
                read = -1;
            return read;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            binaryBuffered = 0;
        }

    }

    private static final byte DEFAULT_CHUNK_SEPARATOR = (byte) '\n';

    private static final String DEFAULT_TEXT_ENCODING = "UTF-8"; //$NON-NLS-1$

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final int DEFAULT_TEXT_BUFFER_SIZE = 1024;

    private static final int DEFAULT_BINARY_BUFFER_SIZE = DEFAULT_BUFFER_SIZE / 4 * 3;

    private static final String EMPTY_TEXT = ""; //$NON-NLS-1$

    /**
     * This array is a lookup table that translates unicode characters drawn
     * from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into
     * their 6-bit positive integer equivalents. Characters that are not in the
     * Base64 alphabet but fall within the bounds of the array are translated to
     * -1.
     */
    private static final byte base64ToInt[] = { -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1,
            -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1,
            -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

    private InputStream in;

    private String textEncoding;

    private byte separator;

    private boolean eof = false;

    private byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    private int buffered = 0;

    private byte[] textBuffer = new byte[DEFAULT_TEXT_BUFFER_SIZE];

    private ChunkInputStream binaryStream = null;

    private byte[] binaryBuffer = new byte[DEFAULT_BINARY_BUFFER_SIZE];

    private int binaryBuffered = 0;

    private boolean chunkEnded = false;

    public ChunkReader(InputStream in) {
        this(in, DEFAULT_CHUNK_SEPARATOR, DEFAULT_TEXT_ENCODING);
    }

    public ChunkReader(InputStream in, byte chunkSeparator, String textEncoding) {
        this.in = in;
        this.separator = chunkSeparator;
        this.textEncoding = textEncoding;
    }

    private boolean isStreamEnded() {
        return eof && buffered <= 0;
    }

    private boolean isBinaryChunkEnded() {
        return chunkEnded && binaryBuffered <= 0;
    }

    /**
     * Read a line of text and return it.
     * 
     * @return a line of text, or <code>null</code> if the main stream has
     *         reached the end
     * @throws IOException
     */
    public String readText() throws IOException {
        closeBinaryStream(true);

        // The number of bytes to return as text.
        int size = 0;

        // The index of the separator char in the byte buffer.
        int sep;

        // A new number of bytes that we need to enlarge the text buffer.
        int newSize;

        // The number of bytes in buffer.
        int numBuffered;

        // The total number of bytes that we consumed from the stream.
        // This number may be one more than 'size' as we consume the separator 
        // char as well.
        int consumed = 0;

        // Run until the end of stream is reached.
        while (!isStreamEnded()) {

            // Find the next separator char in the byte buffer,
            // or the end of the byte buffer.
            sep = findSep(0);

            // Calculate new text buffer size.
            newSize = size + sep;

            // Enlarge the text buffer to receive new bytes.
            if (newSize > textBuffer.length) {
                textBuffer = Arrays.copyOf(textBuffer,
                        Math.max(textBuffer.length << 1, newSize));
            }

            // Copy required bytes from byte buffer to text buffer.
            if (sep > 0) {
                System.arraycopy(buffer, 0, textBuffer, size, sep);
            }

            // Now we have 'size' bytes in the text buffer.
            size = newSize;

            // Consume bytes from the byte buffer.
            numBuffered = buffered;
            if (sep == numBuffered) {
                use(numBuffered);
                consumed += numBuffered;
            } else {
                use(sep + 1);
                consumed += sep + 1;
                break;
            }

            // Reload byte buffer from the stream.
            reload();
        }

        // No bytes consumed from stream, this must be caused by the end of stream,
        // otherwise there must have been at least one separator char.
        if (consumed == 0)
            return null;

        // Return the cached empty string to prevent creating a new String instance.
        if (size == 0)
            return EMPTY_TEXT;

        // Construct a new String instance from the text buffer.
        return new String(textBuffer, 0, size, textEncoding);
    }

    /**
     * Create a stream for reading the next chunk of data.
     * 
     * @return an input stream for reading the next chunk of data, or
     *         <code>null</code> if the main stream has reached the end
     * @throws IOException
     */
    public InputStream openNextChunkAsStream() throws IOException {
        closeBinaryStream(true);
        if (isStreamEnded())
            return null;
        return binaryStream = new ChunkInputStream(in);
    }

    private void closeBinaryStream(boolean drain) throws IOException {
        if (binaryStream != null && !binaryStream.closed
                && !isBinaryChunkEnded()) {
            if (drain) {
                byte[] buf = new byte[4096];
                while (binaryStream.read(buf) > 0) {
                }
            }
            binaryStream.close();
            binaryStream = null;
        }
    }

    public void close() throws IOException {
        closeBinaryStream(false);
        in.close();
    }

    private void reload() throws IOException {
        if (eof)
            return;
        if (!eof && buffered < buffer.length) {
            int len = buffer.length - buffered;
            int read = in.read(buffer, buffered, len);
            if (read < 0) {
                eof = true;
                read = 0;
            }
            buffered += read;
        }
    }

    private static int base64toInt(byte c) throws IOException {
        int b = base64ToInt[c];
        if (b < 0)
            throw new UnmappableCharacterException(1);
        return b;
    }

    private int findSep(int off) {
        for (int i = off; i < buffered; i++) {
            if (buffer[i] == separator)
                return i;
        }
        return buffered;
    }

    private void use(int used) {
        if (used >= buffered) {
            buffered = 0;
        } else {
            int newCount = buffered - used;
            System.arraycopy(buffer, used, buffer, 0, newCount);
            buffered = newCount;
        }
    }

    private void refillBinary() throws IOException {
        reload();
        if (eof) {
            chunkEnded = true;
        }
        int used = 0;
        int max = buffered - 3;
        int bmax = binaryBuffer.length - 2;
        while (used < max && binaryBuffered < bmax) {
            byte b0 = buffer[used++];
            if (b0 == separator) {
                chunkEnded = true;
                break;
            }
            byte b1 = buffer[used++];
            if (b1 == separator) {
                chunkEnded = true;
                break;
            }
            byte b2 = buffer[used++];
            if (b2 == separator) {
                chunkEnded = true;
                break;
            }
            byte b3 = buffer[used++];
            if (b3 == separator) {
                chunkEnded = true;
                break;
            }

            int c0 = base64toInt(b0);
            int c1 = base64toInt(b1);
            binaryBuffer[binaryBuffered++] = (byte) ((c0 << 2) | (c1 >> 4));

            if (b2 != '=') {
                int c2 = base64toInt(b2);
                binaryBuffer[binaryBuffered++] = (byte) ((c1 << 4) | (c2 >> 2));
                if (b3 != '=') {
                    int c3 = base64toInt(b3);
                    binaryBuffer[binaryBuffered++] = (byte) ((c2 << 6) | c3);
                }
            }
        }
        use(used);
    }

    private void useBinary(int used) {
        if (used >= binaryBuffered) {
            binaryBuffered = 0;
        } else {
            int newCount = binaryBuffered - used;
            System.arraycopy(binaryBuffer, used, binaryBuffer, 0, newCount);
            binaryBuffered = newCount;
        }
    }

}
