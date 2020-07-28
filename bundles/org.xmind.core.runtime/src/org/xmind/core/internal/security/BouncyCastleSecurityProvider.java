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

package org.xmind.core.internal.security;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALGORITHM_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ITERATION_COUNT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_KEY_DERIVATION_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SALT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_ALGORITHM;
import static org.xmind.core.internal.dom.DOMConstants.TAG_KEY_DERIVATION;

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
import org.xmind.core.IFileEntry;
import org.xmind.core.internal.zip.ArchiveConstants;

/**
 * @author Frank Shaka
 * @deprecated Use {@link PasswordProtectedNormalizer}
 */
public final class BouncyCastleSecurityProvider implements ISecurityProvider {

    /**
     * 
     */
    private static final String ALGORITHM_NAME = "AES/CBC/PKCS5Padding"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String KEY_DERIVATION_ALGORITHM_NAME = "PKCS12"; //$NON-NLS-1$

    /**
     * The ramdomizer
     */
    private static Random random = null;

    public BouncyCastleSecurityProvider() {
    }

    private boolean needChecksum(IEncryptionData encData) {
        IFileEntry entry = encData.getFileEntry();
        if (entry != null) {
            String path = entry.getPath();
            return needChecksum(path);
        }
        return false;
    }

    /**
     * @param entryPath
     * @return
     */
    private boolean needChecksum(String entryPath) {
        return ArchiveConstants.CONTENT_XML.equals(entryPath) || ArchiveConstants.META_XML.equals(entryPath)
                || ArchiveConstants.STYLES_XML.equals(entryPath)
                || ArchiveConstants.PATH_MARKER_SHEET.equals(entryPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.internal.security.ISecurityProvider#initEncryptionData
     * (org.xmind.core.IEncryptionData)
     */
    public void initializeEncryptionData(IEncryptionData encData) {
        if (needChecksum(encData)) {
            encData.setChecksumType("MD5"); //$NON-NLS-1$
        }

        encData.setAttribute(ALGORITHM_NAME, TAG_ALGORITHM, ATTR_ALGORITHM_NAME);
        encData.setAttribute(KEY_DERIVATION_ALGORITHM_NAME, TAG_KEY_DERIVATION, ATTR_KEY_DERIVATION_NAME);
        encData.setAttribute(generateSalt(), TAG_KEY_DERIVATION, ATTR_SALT);
        encData.setAttribute("1024", TAG_KEY_DERIVATION, ATTR_ITERATION_COUNT); //$NON-NLS-1$
    }

    private void checkEncryptionData(IEncryptionData encData) throws CoreException {
        String algoName = encData.getAttribute(TAG_ALGORITHM, ATTR_ALGORITHM_NAME);
        if (algoName == null || !ALGORITHM_NAME.equals(algoName))
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);

        String keyAlgoName = encData.getAttribute(TAG_KEY_DERIVATION, ATTR_KEY_DERIVATION_NAME);
        if (keyAlgoName == null || !KEY_DERIVATION_ALGORITHM_NAME.equals(keyAlgoName))
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);
    }

    private int getIterationCount(IEncryptionData encData) {
        return encData.getIntAttribute(1024, TAG_KEY_DERIVATION, ATTR_ITERATION_COUNT);
    }

    private byte[] getSalt(IEncryptionData encData) throws CoreException {
        String saltString = encData.getAttribute(TAG_KEY_DERIVATION, ATTR_SALT);
        if (saltString == null)
            throw new CoreException(Core.ERROR_FAIL_INIT_CRYPTOGRAM);
        return Base64.base64ToByteArray(saltString);
    }

    public OutputStream createPasswordProtectedOutputStream(OutputStream output, boolean encrypt,
            IEncryptionData encData, String password) throws CoreException {
        BufferedBlockCipher cipher = createCipher(encrypt, encData, password);
        OutputStream out = new BlockCipherOutputStream(output, cipher);
        if (encData.getChecksumType() != null)
            out = new ChecksumOutputStream(out);
        return out;
    }

    public InputStream createPasswordProtectedInputStream(InputStream input, boolean encrypt, IEncryptionData encData,
            String password) throws CoreException {
        BufferedBlockCipher cipher = createCipher(encrypt, encData, password);
        InputStream in = new BlockCipherInputStream(input, cipher);
        if (encData.getChecksumType() != null)
            in = new ChecksumInputStream(in);
        return in;
    }

    private BufferedBlockCipher createCipher(boolean encrypt, IEncryptionData encData, String password)
            throws CoreException {
        checkEncryptionData(encData);

        // Create a parameter generator
        PKCS12ParametersGenerator paramGen = new PKCS12ParametersGenerator(new MD5Digest());

        // Get the password bytes
        byte[] pwBytes = password == null ? new byte[0]
                : PBEParametersGenerator.PKCS12PasswordToBytes(password.toCharArray());

        // Initialize the parameter generator with password bytes, 
        // salt and iteration counts
        paramGen.init(pwBytes, getSalt(encData), getIterationCount(encData));

        // Generate a parameter
        CipherParameters param = paramGen.generateDerivedParameters(128);

        // Create a block cipher
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));

        // Initialize the block cipher
        cipher.init(encrypt, param);
        return cipher;
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
