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

import static org.xmind.core.net.internal.EncodingUtils.toAsciiBytes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.xmind.core.net.Field;
import org.xmind.core.net.FieldSet;
import org.xmind.core.net.internal.EncodingUtils;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class MultipartEntity extends HttpEntity {

    private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary="; //$NON-NLS-1$

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private static final byte[] BOUNDARY_CHARS = toAsciiBytes(
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); //$NON-NLS-1$

    /** Carriage return/linefeed as a byte array */
    private static final byte[] CRLF = toAsciiBytes("\r\n"); //$NON-NLS-1$

    /** Content dispostion as a byte array */
    private static final byte[] QUOTE = toAsciiBytes("\""); //$NON-NLS-1$

    /** Extra characters as a byte array */
    private static final byte[] EXTRA = toAsciiBytes("--"); //$NON-NLS-1$

    /** Content dispostion as a byte array */
    private static final byte[] CONTENT_DISPOSITION = toAsciiBytes(
            "Content-Disposition: form-data; name="); //$NON-NLS-1$

    /** Content type header as a byte array */
    private static final byte[] CONTENT_TYPE = toAsciiBytes("Content-Type: "); //$NON-NLS-1$

    /** Content type header as a byte array */
    private static final byte[] CONTENT_TRANSFER_ENCODING = toAsciiBytes(
            "Content-Transfer-Encoding: "); //$NON-NLS-1$

    /** Attachment's file name as a byte array */
    private static final byte[] FILE_NAME = toAsciiBytes("; filename="); //$NON-NLS-1$

    private static final byte[] TEXT_CONTENT_TYPE = toAsciiBytes(
            "text/plain; charset=utf-8"); //$NON-NLS-1$

    private static final byte[] FILE_TRANSFER_ENCODING = toAsciiBytes("binary"); //$NON-NLS-1$

    private static final byte[] TEXT_TRANSFER_ENCODING = toAsciiBytes("8bit"); //$NON-NLS-1$

    private FieldSet parts;

    private byte[] boundary = null;

    /**
     * 
     */
    public MultipartEntity(FieldSet parts) {
        this.parts = parts;
    }

    /**
     * @return the parts
     */
    public FieldSet getParts() {
        return parts;
    }

    private byte[] getBoundary() {
        if (boundary != null)
            return boundary;
        Random rand = new Random();
        byte[] bytes = new byte[rand.nextInt(11) + 30]; // a random size
                                                        // from 30 to 40
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = BOUNDARY_CHARS[rand.nextInt(BOUNDARY_CHARS.length)];
        }
        boundary = bytes;
        return boundary;
    }

    public String getContentType() {
        StringBuffer typeBuffer = new StringBuffer(MULTIPART_CONTENT_TYPE);
        typeBuffer.append(EncodingUtils.toAsciiString(getBoundary()));
        return typeBuffer.toString();
    }

    private static String toSafeName(String name) {
        return name.replaceAll("\"", "%22"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public long getContentLength() {
        if (parts.isEmpty())
            return 0;
        long length = 0;
        for (Field part : parts) {
            length += EXTRA.length;
            length += getBoundary().length;
            length += CRLF.length;

            length += CONTENT_DISPOSITION.length;
            length += QUOTE.length;
            length += toAsciiBytes(toSafeName(part.name)).length;
            length += QUOTE.length;
            length += CRLF.length;

            if (part.value instanceof HttpEntity) {
                String fileName = ((HttpEntity) part.value).getFileName();
                if (fileName == null)
                    fileName = part.name;
                length += FILE_NAME.length;
                length += QUOTE.length;
                length += toAsciiBytes(toSafeName(fileName)).length;
                length += QUOTE.length;
            }

            length += CONTENT_TYPE.length;
            length += getContentType(part.value).length;
            length += CRLF.length;

            length += CONTENT_TRANSFER_ENCODING.length;
            length += getTransferEncoding(part.value).length;
            length += CRLF.length;
            length += CRLF.length;

            length += getPartDataLength(part);

            length += CRLF.length;

        }

        length += EXTRA.length;
        length += getBoundary().length;
        length += EXTRA.length;
        length += CRLF.length;
        return length;
    }

    public void writeTo(OutputStream stream) throws IOException {
        if (parts.isEmpty())
            return;

        for (Field part : parts) {
            stream.write(EXTRA);
            stream.write(getBoundary());
            stream.write(CRLF);

            stream.write(CONTENT_DISPOSITION);
            stream.write(QUOTE);
            stream.write(toAsciiBytes(toSafeName(part.name)));
            stream.write(QUOTE);
            if (part.value instanceof HttpEntity) {
                String fileName = ((HttpEntity) part.value).getFileName();
                if (fileName == null)
                    fileName = part.name;
                stream.write(FILE_NAME);
                stream.write(QUOTE);
                stream.write(toAsciiBytes(toSafeName(fileName)));
                stream.write(QUOTE);
            }
            stream.write(CRLF);

            stream.write(CONTENT_TYPE);
            stream.write(getContentType(part.value));
            stream.write(CRLF);

            stream.write(CONTENT_TRANSFER_ENCODING);
            stream.write(getTransferEncoding(part.value));
            stream.write(CRLF);
            stream.write(CRLF);

            writePartData(stream, part);

            stream.write(CRLF);
        }

        stream.write(EXTRA);
        stream.write(getBoundary());
        stream.write(EXTRA);
        stream.write(CRLF);
    }

    private static byte[] getContentType(Object value) {
        if (value instanceof HttpEntity)
            return EncodingUtils
                    .toAsciiBytes(((HttpEntity) value).getContentType());
        return TEXT_CONTENT_TYPE;
    }

    private static byte[] getTransferEncoding(Object value) {
        if (value instanceof HttpEntity)
            return FILE_TRANSFER_ENCODING;
        return TEXT_TRANSFER_ENCODING;
    }

    private static long getPartDataLength(Field part) {
        if (part.value instanceof HttpEntity) {
            return ((HttpEntity) part.value).getContentLength();
        }
        return toAsciiBytes(part.getValue()).length;
    }

    private static void writePartData(OutputStream stream, Field part)
            throws IOException {
        if (part.value instanceof HttpEntity) {
            ((HttpEntity) part.value).writeTo(stream);
        } else {
            writeFromText(stream, part.getValue());
        }
    }

    private static void writeFromText(OutputStream writeStream,
            String encodedText) throws IOException {
        writeStream.write(encodedText.getBytes("UTF-8")); //$NON-NLS-1$
    }

}