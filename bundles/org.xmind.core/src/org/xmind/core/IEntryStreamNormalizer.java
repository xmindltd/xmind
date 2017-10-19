package org.xmind.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface converts input/output streams that may be encoded/encrypted
 * into normalized ones that are decoded/decrypted and suitable for
 * reading/writing actual entry content directly. A file entry provides
 * encoding/encryption information when decoding/decrypting and receives
 * encoding/encryption information when encoding/encrypting.
 * 
 * @author Ren Siu
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IEntryStreamNormalizer {

    IEntryStreamNormalizer NULL = new IEntryStreamNormalizer() {

        public OutputStream normalizeOutputStream(OutputStream stream,
                IFileEntry fileEntry) throws IOException, CoreException {
            fileEntry.deleteEncryptionData();
            return stream;
        }

        public InputStream normalizeInputStream(InputStream stream,
                IFileEntry fileEntry) throws IOException, CoreException {
            return stream;
        }

    };

    /**
     * Converts the specified output stream into an encoded/encrypted one that
     * can be used for writing normal entry content. The specified file entry's
     * original encoding/encryption data will be cleared and new ones will be
     * created on it to store encoding/encryption information used for writing
     * operations.
     * 
     * @param stream
     *            the {@link OutputStream} that writes data to the target
     *            storage
     * @param fileEntry
     *            the {@link IFileEntry} that receives encoding/encryption data
     * @return a new {@link OutputStream} that receives content data and writes
     *         them to the given stream
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             <ul>
     *             <li><code>Core.ERROR_WRONG_PASSWORD</code> - if
     *             encoding/encryption fails;</li>
     *             <li><code>Core.ERROR_CANCELLATION</code> - if the operation
     *             is cancelled</li>
     *             </ul>
     * @throws IllegalArgumentException
     *             if the stream or file entry is <code>null</code>
     */
    OutputStream normalizeOutputStream(OutputStream stream,
            IFileEntry fileEntry) throws IOException, CoreException;

    /**
     * Converts the specified input stream into an decoded/decrypted one that
     * can be used for reading normal entry content. The specified file entry's
     * encoding/encryption data will be used during converting and reading
     * operations.
     * 
     * @param stream
     *            the {@link InputStream} that reads data from the target
     *            storage
     * @param fileEntry
     *            the {@link IFileEntry} that provides encoding/encryption data
     * @return a new {@link InputStream} that reads encoded/encrypted data from
     *         the given stream and decodes/decrypts them
     * @throws IOException
     *             if any I/O error occurs
     * @throws CoreException
     *             if decoding/decryption fails
     * @throws IllegalArgumentException
     *             if the stream or file entry is <code>null</code>
     */
    InputStream normalizeInputStream(InputStream stream, IFileEntry fileEntry)
            throws IOException, CoreException;

}
