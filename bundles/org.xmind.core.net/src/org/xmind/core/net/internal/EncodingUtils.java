package org.xmind.core.net.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingUtils {

    public static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    public static final String LATIN1 = "ISO-8859-1"; //$NON-NLS-1$

    public static String urlEncode(Object object) {
        String text = object == null ? "" : String.valueOf(object); //$NON-NLS-1$
        try {
            return URLEncoder.encode(text, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(DEFAULT_ENCODING, e);
        }
    }

    public static String urlDecode(String text) {
        if (text == null)
            return ""; //$NON-NLS-1$
        try {
            return URLDecoder.decode(text, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(DEFAULT_ENCODING, e);
        }
    }

    public static String format(String pattern, Object... values) {
        Object[] encodedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            encodedValues[i] = EncodingUtils.urlEncode(values[i]);
        }
        return String.format(pattern, encodedValues);
    }

    public static byte[] toDefaultBytes(String str) {
        try {
            return str.getBytes(DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(DEFAULT_ENCODING, e);
        }
    }

    public static String toDefaultString(byte[] bytes) {
        try {
            return new String(bytes, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(DEFAULT_ENCODING, e);
        }
    }

    public static byte[] toAsciiBytes(String str) {
        try {
            return str.getBytes(LATIN1);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(LATIN1, e);
        }
    }

    public static String toAsciiString(byte[] bytes) {
        try {
            return new String(bytes, LATIN1);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingError(LATIN1, e);
        }
    }

}
