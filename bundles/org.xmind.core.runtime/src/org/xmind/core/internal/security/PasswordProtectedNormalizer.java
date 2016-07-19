/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.xmind.core.internal.security;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALGORITHM_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ITERATION_COUNT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_DERIVATION_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SALT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_ALGORITHM;
import static org.xmind.core.internal.dom.DOMConstants.TAG_KEY_DERIVATION;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IEncryptionData;
import org.xmind.core.IEntryStreamNormalizer;
import org.xmind.core.IFileEntry;
import org.xmind.core.io.ChecksumTrackingOutputStream;
import org.xmind.core.io.ChecksumVerifiedInputStream;

/**
 * This class provides file entry encryption/decryption based on a password.
 * Instances of this class that have the same password are considered equal to
 * each other.
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class PasswordProtectedNormalizer implements IEntryStreamNormalizer {

    private static final String ALGORITHM_NAME = "AES/CBC/PKCS5Padding"; //$NON-NLS-1$
    private static final String KEY_DERIVATION_ALGORITHM_NAME = "PKCS12"; //$NON-NLS-1$
    private static final String KEY_DERIVATION_ITERATION_COUNT = "1024"; //$NON-NLS-1$
    private static final String CHECKSUM_TYPE = "MD5"; //$NON-NLS-1$

    /**
     * The randomizer
     */
    private static Random random = null;

    /**
     * The password
     */
    private final String password;

    /**
     * 
     */
    public PasswordProtectedNormalizer(String password) {
        if (password == null)
            throw new IllegalArgumentException("password is null"); //$NON-NLS-1$
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IEntryStreamNormalizer#normalizeOutputStream(java.io.
     * OutputStream, org.xmind.core.IFileEntry)
     */
    public OutputStream normalizeOutputStream(OutputStream stream,
            IFileEntry fileEntry) throws IOException, CoreException {
        fileEntry.deleteEncryptionData();

        IEncryptionData encData = fileEntry.createEncryptionData();
        encData.setAttribute(ALGORITHM_NAME, TAG_ALGORITHM,
                ATTR_ALGORITHM_NAME);
        encData.setAttribute(KEY_DERIVATION_ALGORITHM_NAME, TAG_KEY_DERIVATION,
                ATTR_KEY_DERIVATION_NAME);
        encData.setAttribute(generateSalt(), TAG_KEY_DERIVATION, ATTR_SALT);
        encData.setAttribute(KEY_DERIVATION_ITERATION_COUNT, TAG_KEY_DERIVATION,
                ATTR_ITERATION_COUNT);
        encData.setChecksumType(CHECKSUM_TYPE);

        BufferedBlockCipher cipher = createCipher(true, encData, password);
        OutputStream out = new BlockCipherOutputStream(stream, cipher);
        if (encData.getChecksumType() != null) {
            out = new ChecksumTrackingOutputStream(encData,
                    new ChecksumOutputStream(out));
        }
        return out;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IEntryStreamNormalizer#normalizeInputStream(java.io.
     * InputStream, org.xmind.core.IFileEntry)
     */
    public InputStream normalizeInputStream(InputStream stream,
            IFileEntry fileEntry) throws IOException, CoreException {
        IEncryptionData encData = fileEntry.getEncryptionData();
        if (encData == null)
            return stream;

        BufferedBlockCipher cipher = createCipher(false, encData, password);
        InputStream in = new BlockCipherInputStream(stream, cipher);
        if (encData.getChecksumType() != null) {
            in = new ChecksumVerifiedInputStream(new ChecksumInputStream(in),
                    encData.getChecksum());
        }
        return in;
    }

    private BufferedBlockCipher createCipher(boolean encrypt,
            IEncryptionData encData, String password) throws CoreException {
        checkEncryptionData(encData);

        // Create a parameter generator
        PKCS12ParametersGenerator paramGen = new PKCS12ParametersGenerator(
                new MD5Digest());

        // Get the password bytes
        byte[] pwBytes = password == null ? new byte[0]
                : PBEParametersGenerator
                        .PKCS12PasswordToBytes(password.toCharArray());

        // Initialize the parameter generator with password bytes, 
        // salt and iteration counts
        paramGen.init(pwBytes, getSalt(encData), getIterationCount(encData));

        // Generate a parameter
        CipherParameters param = paramGen.generateDerivedParameters(128);

        // Create a block cipher
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new AESEngine()));

        // Initialize the block cipher
        cipher.init(encrypt, param);
        return cipher;
    }

    private void checkEncryptionData(IEncryptionData encData)
            throws CoreException {
        String algoName = encData.getAttribute(TAG_ALGORITHM,
                ATTR_ALGORITHM_NAME);
        if (algoName == null || !ALGORITHM_NAME.equals(algoName))
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);

        String keyAlgoName = encData.getAttribute(TAG_KEY_DERIVATION,
                ATTR_KEY_DERIVATION_NAME);
        if (keyAlgoName == null
                || !KEY_DERIVATION_ALGORITHM_NAME.equals(keyAlgoName))
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);
    }

    private int getIterationCount(IEncryptionData encData) {
        return encData.getIntAttribute(1024, TAG_KEY_DERIVATION,
                ATTR_ITERATION_COUNT);
    }

    private byte[] getSalt(IEncryptionData encData) throws CoreException {
        String saltString = encData.getAttribute(TAG_KEY_DERIVATION, ATTR_SALT);
        if (saltString == null)
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);
        return Base64.base64ToByteArray(saltString);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof PasswordProtectedNormalizer))
            return false;
        PasswordProtectedNormalizer that = (PasswordProtectedNormalizer) obj;
        return this.password.equals(that.password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 37 ^ password.hashCode();
    }

    private static Random getRandom() {
        if (random == null)
            random = new Random();
        return random;
    }

    private static String generateSalt() {
        return Base64.byteArrayToBase64(generateSaltBytes());
    }

    private static byte[] generateSaltBytes() {
        byte[] bytes = new byte[8];
        getRandom().nextBytes(bytes);
        return bytes;
    }

}
