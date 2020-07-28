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
package org.xmind.ui.util;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class NumberUtils {

    private NumberUtils() {
    }

    public static long safeParseLong(String s, long defaultLong) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return defaultLong;
        }
    }

    public static int safeParseInt(String s, int defaultInt) {
        if (s == null || "".equals(s)) //$NON-NLS-1$
            return defaultInt;
        int result = 0;
        int radix = 10;
        int i = 0, max = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin = limit / radix;
        int digit;

        if (max > 0) {
            if (i < max) {
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    return defaultInt;
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    return -result;
                }
                if (result < multmin) {
                    return -result;
                }
                result *= radix;
                if (result < limit + digit) {
                    return -result;
                }
                result -= digit;
            }
            return -result;
        }
        return defaultInt;
    }

    /**
     * Parses the specified string argument to an unsigned decimal integer. If
     * the string starts with non-digital characters ( '-' included ), -1 is
     * returned. If the string starts with digital characters but ends with
     * non-digital ones, the value of the digital part is returned.
     * 
     * @param s
     *            a <code>String</code> containing the <code>int</code>
     *            representation to be parsed.
     * @return the integer value represented by the argument in decimal.
     */
    public static int safeParseInt(String s) {
        return safeParseInt(s, -1);
    }

    public static double safeParseDouble(String s) {
        return safeParseDouble(s, 0);
    }

    public static double safeParseDouble(String s, double defaultDouble) {
        if (s == null || "".equals(s)) //$NON-NLS-1$
            return defaultDouble;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
        }
        return defaultDouble;
    }

    public static String toLetter(int digit, char start, int total) {
        if (digit <= total) {
            return String.valueOf((char) (digit + start - 1));
        }
        int x = digit / total;
        int y = digit - x * total;
        if (y == 0) {
            y = total;
            x--;
        }
        return toLetter(x, start, total) + toLetter(y, start, total);
    }

    public static final int DEFAULT_MAX_ROUND = 10000;
    public static final double DEFAULT_EPSILONG = 0.00000001;

    public static double newton(double[] coefs, double x0) {
        return newton(coefs, x0, DEFAULT_MAX_ROUND, DEFAULT_EPSILONG);
    }

    /**
     * Find a solution to the following equation:<br>
     * <blockquote>
     * <code>a<sub>0</sub> X<sup>n</sup> + a<sub>1</sub> X<sup>n-1</sup> + ...
     * + a<sub>n-1</sub> X + a<sub>n</sub> = 0</code></blockquote>
     * 
     * @param coefs
     *            [ a<sub>0</sub>, a<sub>1</sub>, ..., a<sub>n</sub> ]
     * @param x0
     *            an initial value
     * @param max
     *            the maximum rounds the iteration goes
     * @param eps
     *            precision controller
     * @return a solution to the equation near the initial value
     */
    public static double newton(double[] coefs, double x0, int max,
            double eps) {
        if (coefs == null)
            throw new IllegalArgumentException("coefficient list is null"); //$NON-NLS-1$
        if (coefs.length <= 1)
            throw new IllegalArgumentException(
                    "coefficients are too few to calculate"); //$NON-NLS-1$
        eps = abs(eps);
        double current = x0;
        double last = current;
        double sig = 1;
        int i = 0;
        while (abs(sig) > eps) {
            i++;
            if (i >= max) {
                //Logger.log("Out of max: " + Arrays.toString( coefs ) + ", x = " + current); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            }
            current = last - f(coefs, last) / df(coefs, last);
            sig = current - last;
            last = current;
        }
        return current;
    }

    /**
     * Calculate the value of polynomial.
     * 
     * @param coefs
     *            coefficients of the polynomial
     * @param x
     *            variable value
     * @return the value of the polynomial
     */
    static double f(double[] coefs, double x) {
        double y = 0;
        for (int i = 0; i < coefs.length; i++) {
            y += coefs[i] * pow(x, coefs.length - 1 - i);
        }
        return y;
    }

    /**
     * Calculate the value of differentiated polynomial
     * 
     * @param coefs
     *            coefficients of the polynomial
     * @param x
     *            variable value
     * @return the value of differentiated polynomial
     */
    static double df(double[] coefs, double x) {
        double y = 0;
        for (int i = 0; i < coefs.length - 1; i++) {
            y += coefs[i] * (coefs.length - 1 - i)
                    * pow(x, coefs.length - 2 - i);
        }
        return y;
    }

    public static short bytesToShort16(byte highByte, byte lowByte) {
        return (short) ((highByte << 8) | (lowByte & 0xFF));
    }

    public static enum RomanSymbols {
        M(1000), D(500), C(100), L(50), X(10), V(5), I(1);

        public final long value;

        private RomanSymbols(int value) {
            this.value = value;
        }
    }

    public static String toRoman(long n) {
        int i;
        StringBuilder sb = new StringBuilder(10);
        RomanSymbols[] symbols = RomanSymbols.values();
        while (n > 0) {
            for (i = 0; i < symbols.length; i++) {
                if (symbols[i].value <= n) {
                    int shift = i + (i % 2);
                    if (i > 0 && shift < symbols.length && (symbols[i - 1].value
                            - symbols[shift].value) <= n) {
                        sb.append(symbols[shift].name());
                        sb.append(symbols[i - 1].name());
                        n = n - symbols[i - 1].value + symbols[shift].value;
                        i = -1;
                    } else {
                        sb.append(symbols[i].name());
                        n -= symbols[i].value;
                        i = -1;
                    }
                }
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("nls")
    public static String toSimpleChinese(int n) {
        String num[] = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
        String xx[] = { "十", "百", "千" };
        StringBuilder sb = new StringBuilder();
        int bs = 0;
        while ((int) (n / ((long) Math.pow(10, bs))) != 0) {
            bs++;
        }
        if (bs > 4)
            return "";
        for (int i = bs - 1; i >= 0; i--) {
            int index = (int) (n / Math.pow(10, i));
            sb.append(num[index]);
            if (index != 0 && i > 0)
                sb.append(xx[i - 1]);
            n -= index * Math.pow(10, i);
        }
        return sb.toString();
    }

    @SuppressWarnings("nls")
    public static String toTraditionalChinese(int n) {
        String num[] = { "零", "壹", "貳", "參", "肆", "伍", "陸", "柒", "捌", "玖" };
        String xx[] = { "拾", "佰", "仟" };
        StringBuilder sb = new StringBuilder();
        int bs = 0;
        while ((int) (n / ((long) Math.pow(10, bs))) != 0) {
            bs++;
        }
        if (bs > 4)
            return "";
        for (int i = bs - 1; i >= 0; i--) {
            int index = (int) (n / Math.pow(10, i));
            sb.append(num[index]);
            if (index != 0 && i > 0)
                sb.append(xx[i - 1]);
            n -= index * Math.pow(10, i);
        }
        return sb.toString();
    }

}