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

import java.security.MessageDigest;

/**
 * Generator for PBE derived keys and ivs as defined by PKCS 12 V1.0.
 * <p>
 * The document this implementation is based on can be found at <a
 * href=http://www.rsasecurity.com/rsalabs/pkcs/pkcs-12/index.html> RSA's PKCS12
 * Page</a>
 * <p>
 * NOTE: This algorithm in this class is copied from Bouncycastle's
 * PKCS12ParametersGenerator in order to decrypt legacy xmind files.
 * 
 * @author Frank Shaka
 */
public class PKCS12KeyGenerator {

    public static final int KEY_MATERIAL = 1;
    public static final int IV_MATERIAL = 2;
    public static final int MAC_MATERIAL = 3;

    protected byte[] password;
    protected byte[] salt;
    protected int iterationCount;

    private MessageDigest digest;

    private int u;
    private int v;

    /**
     * Construct a PKCS 12 Parameters generator. This constructor will accept
     * any digest which also implements ExtendedDigest.
     *
     * @param digest
     *            the digest to be used as the source of derived keys.
     * @exception IllegalArgumentException
     *                if an unknown digest is passed in.
     */
    public PKCS12KeyGenerator(MessageDigest digest) {
        this.digest = digest;
        u = digest.getDigestLength();
        v = 64; //((ExtendedDigest)digest).getByteLength();
    }

    /**
     * initialise the PBE generator.
     *
     * @param password
     *            the password converted into bytes (see below).
     * @param salt
     *            the salt to be mixed with the password.
     * @param iterationCount
     *            the number of iterations the "mixing" function is to be
     *            applied for.
     */
    public void init(byte[] password, byte[] salt, int iterationCount) {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    /**
     * return the password byte array.
     *
     * @return the password byte array.
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * return the salt byte array.
     *
     * @return the salt byte array.
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * return the iteration count.
     *
     * @return the iteration count.
     */
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * converts a password to a byte array according to the scheme in PKCS12
     * (unicode, big endian, 2 zero pad bytes at the end).
     *
     * @param password
     *            a character array representing the password.
     * @return a byte array representing the password.
     */
    public static byte[] PKCS12PasswordToBytes(char[] password) {
        if (password != null && password.length > 0) {
            // +1 for extra 2 pad bytes.
            byte[] bytes = new byte[(password.length + 1) * 2];

            for (int i = 0; i != password.length; i++) {
                bytes[i * 2] = (byte) (password[i] >>> 8);
                bytes[i * 2 + 1] = (byte) password[i];
            }

            return bytes;
        } else {
            return new byte[0];
        }
    }

    /**
     * add a + b + 1, returning the result in a. The a value is treated as a
     * BigInteger of length (b.length * 8) bits. The result is modulo 2^b.length
     * in case of overflow.
     */
    private void adjust(byte[] a, int aOff, byte[] b) {
        int x = (b[b.length - 1] & 0xff) + (a[aOff + b.length - 1] & 0xff) + 1;

        a[aOff + b.length - 1] = (byte) x;
        x >>>= 8;

        for (int i = b.length - 2; i >= 0; i--) {
            x += (b[i] & 0xff) + (a[aOff + i] & 0xff);
            a[aOff + i] = (byte) x;
            x >>>= 8;
        }
    }

    /**
     * generation of a derived key ala PKCS12 V1.0.
     */
    private byte[] generateDerivedKey(int idByte, int n) {
        byte[] D = new byte[v];
        byte[] dKey = new byte[n];

        for (int i = 0; i != D.length; i++) {
            D[i] = (byte) idByte;
        }

        byte[] S;

        if ((salt != null) && (salt.length != 0)) {
            S = new byte[v * ((salt.length + v - 1) / v)];

            for (int i = 0; i != S.length; i++) {
                S[i] = salt[i % salt.length];
            }
        } else {
            S = new byte[0];
        }

        byte[] P;

        if ((password != null) && (password.length != 0)) {
            P = new byte[v * ((password.length + v - 1) / v)];

            for (int i = 0; i != P.length; i++) {
                P[i] = password[i % password.length];
            }
        } else {
            P = new byte[0];
        }

        byte[] I = new byte[S.length + P.length];

        System.arraycopy(S, 0, I, 0, S.length);
        System.arraycopy(P, 0, I, S.length, P.length);

        byte[] B = new byte[v];
        int c = (n + u - 1) / u;
        byte[] A;

        for (int i = 1; i <= c; i++) {
            digest.update(D, 0, D.length);
            digest.update(I, 0, I.length);
            A = digest.digest();
            for (int j = 1; j < iterationCount; j++) {
                digest.update(A, 0, A.length);
                A = digest.digest();
            }

            for (int j = 0; j != B.length; j++) {
                B[j] = A[j % A.length];
            }

            for (int j = 0; j != I.length / v; j++) {
                adjust(I, j * v, B);
            }

            if (i == c) {
                System.arraycopy(A, 0, dKey, (i - 1) * u,
                        dKey.length - ((i - 1) * u));
            } else {
                System.arraycopy(A, 0, dKey, (i - 1) * u, A.length);
            }
        }

        return dKey;
    }

    /**
     * Generate a key parameter derived from the password, salt, and iteration
     * count we are currently initialised with.
     *
     * @param keySize
     *            the size of the key we want (in bits)
     * @return a KeyParameter object.
     */
    public byte[] generateDerivedKey(int keySize) {
        keySize = keySize / 8;

        byte[] dKey = generateDerivedKey(KEY_MATERIAL, keySize);
        byte[] key = new byte[keySize];
        System.arraycopy(dKey, 0, key, 0, keySize);
        return key;
    }

}
