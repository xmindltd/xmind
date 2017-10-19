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
package org.xmind.core.util;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class Codec {

    public static final String HASH_ALGORITHM = "SHA-256"; //$NON-NLS-1$

    private Codec() {
    }

    public static char[] hexEncode(byte[] bytes, boolean upperCase) {
        int bytesLength = bytes.length;
        char[] chars = new char[bytesLength << 1];
        int byteIndex = 0;
        int charIndex = 0;
        int value;
        char c;
        while (byteIndex < bytesLength) {
            value = bytes[byteIndex++] & 0xFF;

            c = Character.forDigit(value >> 4, 16);
            if (upperCase)
                c = Character.toUpperCase(c);
            chars[charIndex++] = c;

            c = Character.forDigit(value & 0x0F, 16);
            if (upperCase)
                c = Character.toUpperCase(c);
            chars[charIndex++] = c;
        }

        return chars;
    }

    public static byte[] hexDecode(char[] chars) {
        int charsLength = chars.length;
        if ((charsLength & 0x01) != 0)
            throw new IllegalArgumentException(
                    "Invalid hex length: " + charsLength); //$NON-NLS-1$

        byte[] bytes = new byte[charsLength >> 1];

        int charIndex = 0;
        int byteIndex = 0;
        int value1;
        int value2;
        while (charIndex < charsLength) {
            value1 = Character.digit(chars[charIndex], 16);
            if (value1 < 0)
                throw new IllegalArgumentException("Invalid character '" //$NON-NLS-1$
                        + chars[charIndex] + "' at index " + charIndex); //$NON-NLS-1$
            charIndex++;
            value2 = Character.digit(chars[charIndex], 16);
            if (value2 < 0)
                throw new IllegalArgumentException("Invalid character '" //$NON-NLS-1$
                        + chars[charIndex] + "' at index " + charIndex); //$NON-NLS-1$
            charIndex++;
            bytes[byteIndex++] = (byte) (((value1 << 4) | value2) & 0xFF);
        }

        return bytes;
    }

}
