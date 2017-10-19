package org.xmind.core.net.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingUtils {

    public static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    public static final String LATIN1 = "ISO-8859-1"; //$NON-NLS-1$

    private EncodingUtils() {
    }

    public static String urlEncode(Object object) {
        String text = object == null ? "" : String.valueOf(object); //$NON-NLS-1$
        try {
            return URLEncoder.encode(text, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw wrapEncodingException(e, DEFAULT_ENCODING);
        }
    }

    public static String urlDecode(String text) {
        if (text == null)
            return ""; //$NON-NLS-1$
        try {
            return URLDecoder.decode(text, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw wrapEncodingException(e, DEFAULT_ENCODING);
        }
    }

    public static String format(String pattern, Object... values) {
        Object[] encodedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            encodedValues[i] = EncodingUtils.urlEncode(values[i]);
        }
        return String.format(pattern, encodedValues);
    }

    /**
     * 
     * @param str
     * @return
     * @throws AssertionError
     *             if the default encoding (UTF-8) is not supported
     */
    public static byte[] toDefaultBytes(String str) {
        return toBytes(str, DEFAULT_ENCODING);
    }

    /**
     * 
     * @param bytes
     * @return
     * @throws AssertionError
     *             if the default encoding (UTF-8) is not supported
     */
    public static String toDefaultString(byte[] bytes) {
        return toString(bytes, DEFAULT_ENCODING);
    }

    /**
     * 
     * @param str
     * @return
     * @throws AssertionError
     *             if the ASCII encoding (ISO-8859-1) is not supported
     */
    public static byte[] toAsciiBytes(String str) {
        return toBytes(str, LATIN1);
    }

    /**
     * 
     * @param bytes
     * @return
     * @throws AssertionError
     *             if the ASCII encoding (ISO-8859-1) is not supported
     */
    public static String toAsciiString(byte[] bytes) {
        return toString(bytes, LATIN1);
    }

    private static byte[] toBytes(String str, String charsetName) {
        try {
            return str.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw wrapEncodingException(e, charsetName);
        }
    }

    private static String toString(byte[] bytes, String charsetName) {
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw wrapEncodingException(e, charsetName);
        }
    }

    public static AssertionError wrapEncodingException(
            UnsupportedEncodingException e, String charsetName) {
        return new AssertionError(
                String.format("Encoding not supported: %s", charsetName), e); //$NON-NLS-1$
    }

    /**
     * This array is a lookup table that translates 6-bit positive integer index
     * values into their "Base64 Alphabet" equivalents as specified in Table 1
     * of RFC 2045.
     */
    private static final char[] intToBase64 = { 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
            'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '+', '/' };

    /**
     * Translates the specified byte array into a Base64 string as per
     * Preferences.put(byte[]).
     */
    public static char[] base64Encode(byte[] a) {
        int aLen = a.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - 3 * numFullGroups;
        int resultLen = 4 * ((aLen + 2) / 3);
        char[] result = new char[resultLen];
        char[] intToAlpha = intToBase64;

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        int outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result[outCursor++] = intToAlpha[byte0 >> 2];
            result[outCursor++] = intToAlpha[(byte0 << 4) & 0x3f
                    | (byte1 >> 4)];
            result[outCursor++] = intToAlpha[(byte1 << 2) & 0x3f
                    | (byte2 >> 6)];
            result[outCursor++] = intToAlpha[byte2 & 0x3f];
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result[outCursor++] = intToAlpha[byte0 >> 2];
            if (numBytesInPartialGroup == 1) {
                result[outCursor++] = intToAlpha[(byte0 << 4) & 0x3f];
                result[outCursor++] = '=';
                result[outCursor++] = '=';
            } else {
                // assert numBytesInPartialGroup == 2;
                int byte1 = a[inCursor++] & 0xff;
                result[outCursor++] = intToAlpha[(byte0 << 4) & 0x3f
                        | (byte1 >> 4)];
                result[outCursor++] = intToAlpha[(byte1 << 2) & 0x3f];
                result[outCursor++] = '=';
            }
        }
        return result;
    }

}
