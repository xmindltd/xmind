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
package org.xmind.core.internal.security;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALGORITHM_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ITERATION_COUNT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_DERIVATION_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_IV;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_SIZE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SALT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_ALGORITHM;
import static org.xmind.core.internal.dom.DOMConstants.TAG_KEY_DERIVATION;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
    private static final String OLD37_KEY_DERIVATION_ALGORITHM_NAME = "PKCS12"; //$NON-NLS-1$
    private static final String KEY_DERIVATION_ALGORITHM_NAME = "PBKDF2WithHmacSHA512"; //$NON-NLS-1$
    private static final String KEY_DERIVATION_ITERATION_COUNT = "1024"; //$NON-NLS-1$
    private static final String CHECKSUM_TYPE = "MD5"; //$NON-NLS-1$
    private static final String KEY_DERIVATION_SIZE = "128"; //$NON-NLS-1$

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
        encData.setAttribute(KEY_DERIVATION_SIZE, TAG_KEY_DERIVATION,
                ATTR_KEY_SIZE);
        encData.setAttribute(generateIV(), TAG_KEY_DERIVATION, ATTR_KEY_IV);
        encData.setChecksumType(CHECKSUM_TYPE);

        boolean oldEncrptWay = beforeEncrpt37(encData);
        Cipher cipher = createCipher(true, oldEncrptWay, encData, password);
        OutputStream out = new CipherOutputStream(stream, cipher);
        if (encData.getChecksumType() != null) {
            out = new ChecksumTrackingOutputStream(encData,
                    new ChecksumOutputStream(out));
        }
        return out;
    }

    private boolean beforeEncrpt37(IEncryptionData encData) {
        String keyAlgoName = encData.getAttribute(TAG_KEY_DERIVATION,
                ATTR_KEY_DERIVATION_NAME);
        return OLD37_KEY_DERIVATION_ALGORITHM_NAME.equals(keyAlgoName);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEntryStreamNormalizer#normalizeInputStream(java.io.
     * InputStream, org.xmind.core.IFileEntry)
     */
    public InputStream normalizeInputStream(InputStream stream,
            IFileEntry fileEntry) throws IOException, CoreException {
        IEncryptionData encData = fileEntry.getEncryptionData();
        if (encData == null)
            return stream;

        boolean oldEncrptWay = beforeEncrpt37(encData);
        Cipher oldCipher = createCipher(false, oldEncrptWay, encData, password);
        InputStream in = new CipherInputStream(stream, oldCipher);
        if (encData.getChecksumType() != null) {
            in = new ChecksumVerifiedInputStream(new ChecksumInputStream(in),
                    encData.getChecksum());
        }
        return in;
    }

    private Cipher createCipher(boolean encrypt, boolean oldWay,
            IEncryptionData encData, String password) throws CoreException {
        checkEncryptionData(encData);
        Key aesKey = createKey(oldWay, encData, password);
        byte[] iv = getIV(encData);
        IvParameterSpec ivParameter = new IvParameterSpec(iv);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM, e);
        } catch (NoSuchPaddingException e) {
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM, e);
        }
        try {
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                    aesKey, ivParameter);
        } catch (InvalidKeyException e) {
            throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM, e);
        }
        return cipher;
    }

    private Key createKey(boolean old, IEncryptionData encData, String password)
            throws CoreException {
        byte[] key = old ? getOldKeyByte(encData, password)
                : getKeyByte(encData, password);
        return new SecretKeySpec(key, "AES"); //$NON-NLS-1$
    }

    private byte[] getKeyByte(IEncryptionData encData, String password)
            throws CoreException {
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory
                    .getInstance(KEY_DERIVATION_ALGORITHM_NAME);
        } catch (NoSuchAlgorithmException e) {
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM, e);
        }

        KeySpec keySpec = new PBEKeySpec(password.toCharArray(),
                getSalt(encData), getIterationCount(encData),
                getKeySize(encData));
        try {
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new CoreException(Core.ERROR_WRONG_PASSWORD, e);
        }
    }

    private byte[] getOldKeyByte(IEncryptionData encData, String password)
            throws CoreException {
        PKCS12KeyGenerator keyGen = null;
        try {
            keyGen = new PKCS12KeyGenerator(MessageDigest.getInstance("MD5")); //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM, e);
        }
        byte[] pwBytes = password == null ? new byte[0]
                : PKCS12KeyGenerator
                        .PKCS12PasswordToBytes(password.toCharArray());
        keyGen.init(pwBytes, getSalt(encData), getIterationCount(encData));
        byte[] key = keyGen.generateDerivedKey(getKeySize(encData));
        return key;
    }

    private void checkEncryptionData(IEncryptionData encData)
            throws CoreException {
        String algoName = encData.getAttribute(TAG_ALGORITHM,
                ATTR_ALGORITHM_NAME);
        if (algoName == null || !ALGORITHM_NAME.equals(algoName))
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);

        String keyAlgoName = encData.getAttribute(TAG_KEY_DERIVATION,
                ATTR_KEY_DERIVATION_NAME);
        if (keyAlgoName == null || !(KEY_DERIVATION_ALGORITHM_NAME
                .equals(keyAlgoName)
                || OLD37_KEY_DERIVATION_ALGORITHM_NAME.equals(keyAlgoName)))
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

    private byte[] getIV(IEncryptionData encData) throws CoreException {
        String ivString = encData.getAttribute(TAG_KEY_DERIVATION, ATTR_KEY_IV);
        if (ivString == null) {
            return new byte[16];
        }
        return Base64.base64ToByteArray(ivString);
    }

    private int getKeySize(IEncryptionData encData) throws CoreException {
        String keySizeString = encData.getAttribute(TAG_KEY_DERIVATION,
                ATTR_KEY_SIZE);
        if (keySizeString != null) {
            try {
                return Integer.parseInt(keySizeString);
            } catch (NumberFormatException ignore) {
            }
        }
        return Integer.parseInt(KEY_DERIVATION_SIZE);
    }

    /*
     * (non-Javadoc)
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

    private static String generateIV() {
        return Base64.byteArrayToBase64(generateIVBytes());
    }

    private static byte[] generateSaltBytes() {
        byte[] bytes = new byte[8];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    private static byte[] generateIVBytes() {
        byte[] bytes = new byte[16];
        getRandom().nextBytes(bytes);
        return bytes;
    }

}
