package org.xmind.core.net.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingUtils {

    public static String urlEncode(Object object) {
        String text = object == null ? "" : String.valueOf(object); //$NON-NLS-1$
        try {
            return URLEncoder.encode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    public static String urlDecode(String text) {
        if (text == null)
            return ""; //$NON-NLS-1$
        try {
            return URLDecoder.decode(text, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    public static String format(String pattern, Object... values) {
        Object[] encodedValues = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            encodedValues[i] = EncodingUtils.urlEncode(values[i]);
        }
        return String.format(pattern, encodedValues);
    }

    public static byte[] toAsciiBytes(String str) {
        try {
            return str.getBytes("US-ASCII"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    public static String toAsciiString(byte[] bytes) {
        try {
            return new String(bytes, "US-ASCII"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

}
