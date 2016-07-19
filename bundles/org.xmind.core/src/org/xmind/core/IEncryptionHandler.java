package org.xmind.core;

/**
 * A encryption handler provides key encryption information for
 * encrypting/decrypting file entry streams.
 * 
 * @author Frank Shaka
 * @deprecated Use {@link IEntryStreamNormalizer}
 */
public interface IEncryptionHandler {

    /**
     * Returns the password that locks the content.
     * 
     * @return a {@link String} as the password
     * @throws CoreException
     *             <ul>
     *             <li><code>Core.ERROR_CANCELLATION</code> - the operation is
     *             cancelled</li>
     *             </ul>
     */
    String retrievePassword() throws CoreException;

}
