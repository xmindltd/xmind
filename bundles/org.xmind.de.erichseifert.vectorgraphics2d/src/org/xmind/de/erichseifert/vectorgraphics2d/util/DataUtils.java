/* ******************************************************************************
 * Copyright (c) 2006-2013 XMind Ltd. and others.
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
package org.xmind.de.erichseifert.vectorgraphics2d.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that contains utility functions for working with data
 * collections like maps or lists.
 *
 * @author Jason Wong
 */
public abstract class DataUtils {
    /**
     * Default constructor that prevents creation of class.
     */
    protected DataUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a mapping from two arrays, one with keys, one with values.
     *
     * @param <K>
     *            Data type of the keys.
     * @param <V>
     *            Data type of the values.
     * @param keys
     *            Array containing the keys.
     * @param values
     *            Array containing the values.
     * @return Map with keys and values from the specified arrays.
     */
    public static <K, V> Map<K, V> map(K[] keys, V[] values) {
        // Check for valid parameters
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Cannot create a Map: " //$NON-NLS-1$
                    + "The number of keys and values differs."); //$NON-NLS-1$
        }
        // Fill map with keys and values
        Map<K, V> map = new LinkedHashMap<K, V>();
        for (int i = 0; i < keys.length; i++) {
            K key = keys[i];
            V value = values[i];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Returns a string containing all elements concatenated by a specified
     * separator.
     *
     * @param separator
     *            Separator string.
     * @param elements
     *            List of elements that should be concatenated.
     * @return a concatenated string.
     */
    public static String join(String separator, List<?> elements) {
        if (elements == null || elements.size() == 0) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer sb = new StringBuffer(elements.size() * 3);
        int i = 0;
        for (Object elem : elements) {
            if (i++ > 0) {
                sb.append(separator);
            }
            sb.append(format(elem));
        }
        return sb.toString();
    }

    /**
     * Returns a string containing all elements concatenated by a specified
     * separator.
     *
     * @param separator
     *            Separator string.
     * @param elements
     *            List of elements that should be concatenated.
     * @return a concatenated string.
     */
    public static String join(String separator, Object... elements) {
        if (elements == null || elements.length == 0) {
            return ""; //$NON-NLS-1$
        }
        return join(separator, Arrays.asList(elements));
    }

    /**
     * Returns a string with all float values concatenated by a specified
     * separator.
     *
     * @param separator
     *            Separator string.
     * @param elements
     *            Float array.
     * @return a concatenated string.
     */
    public static String join(String separator, float... elements) {
        if (elements == null || elements.length == 0) {
            return ""; //$NON-NLS-1$
        }
        List<Number> list = new ArrayList<Number>(elements.length);
        for (Float elem : elements) {
            list.add(elem);
        }
        return join(separator, list);
    }

    /**
     * Returns a string with all double values concatenated by a specified
     * separator.
     *
     * @param separator
     *            Separator string.
     * @param elements
     *            Double array.
     * @return a concatenated string.
     */
    public static String join(String separator, double... elements) {
        if (elements == null || elements.length == 0) {
            return ""; //$NON-NLS-1$
        }
        List<Number> list = new ArrayList<Number>(elements.length);
        for (Double elem : elements) {
            list.add(elem);
        }
        return join(separator, list);
    }

    /**
     * Returns the largest of all specified values.
     *
     * @param values
     *            Several integer values.
     * @return largest value.
     */
    public static int max(int... values) {
        int max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * Copies data from an input stream to an output stream using a buffer of
     * specified size.
     *
     * @param in
     *            Input stream.
     * @param out
     *            Output stream.
     * @param bufferSize
     *            Size of the copy buffer.
     * @throws IOException
     *             when an error occurs while copying.
     */
    public static void transfer(InputStream in, OutputStream out, int bufferSize)
            throws IOException {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Returns a formatted string of the specified number. All trailing zeroes
     * or decimal points will be stripped.
     *
     * @param number
     *            Number to convert to a string.
     * @return A formatted string.
     */
    public static String format(Number number) {
        String formatted = Double.toString(number.doubleValue())
                .replaceAll("\\.0+$", "") //$NON-NLS-1$ //$NON-NLS-2$
                .replaceAll("(\\.[0-9]*[1-9])0+$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
        return formatted;
    }

    /**
     * Returns a formatted string of the specified object.
     *
     * @param number
     *            Object to convert to a string.
     * @return A formatted string.
     */
    public static String format(Object obj) {
        if (obj instanceof Number) {
            return format((Number) obj);
        } else {
            return obj.toString();
        }
    }
}
