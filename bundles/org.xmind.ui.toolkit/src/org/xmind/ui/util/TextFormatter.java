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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class TextFormatter {

    private static final long K = 1 << 10;

    private static final long M = 1 << 20;

    private static final long G = 1 << 30;

    /**
     * 
     * @param size
     * @return
     */
    public static String toFileSize(long size) {
        if (size <= K) {
            return String.format("%d B", size); //$NON-NLS-1$
        } else if (size <= M) {
            return String.format("%.1f K", size * 1.0 / K); //$NON-NLS-1$
        } else if (size <= G) {
            return String.format("%.1f M", size * 1.0 / M); //$NON-NLS-1$
        }
        return String.format("%.1f G", size * 1.0 / G); //$NON-NLS-1$
    }

    /**
     * Convert the milliseconds to hour-minute-seconds.
     * 
     * @param milliseconds
     * @return
     */
    public static Object[] toHMS(long milliseconds) {
        long s = milliseconds / 1000;
        long m = s / 60;
        long h = m / 60;
        return new Object[] { h, m % 60, s % 60 };
    }

    public static Object[] toPrecisionHMS(long milliseconds) {
        double s = milliseconds / 1000.0;
        double m = s / 60.0;
        double h = m / 60.0;
        return new Object[] { h, rem(m, 60), rem(s, 60) };
    }

    private static double rem(double dividend, double divisor) {
        return dividend - ((int) (dividend / divisor) * divisor);
    }

    /**
     * 
     * @param milliseconds
     * @return
     */
    public static String toTime(long milliseconds) {
        return toTime(milliseconds, 0, false);
    }

    public static String toTime(long milliseconds, int secondPrecision,
            boolean autoCompact) {
        if (secondPrecision > 0) {
            Object[] hms = toPrecisionHMS(milliseconds);
            if (autoCompact && ((Double) hms[0]).doubleValue() == 0) {
                return String.format("%.0f:%." + secondPrecision + "f", hms); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return String.format("%.0f:%.0f:%." + secondPrecision + "f", hms); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            Object[] hms = toHMS(milliseconds);
            if (autoCompact && ((Long) hms[0]).longValue() == 0) {
                return String.format("%02d:%02d", hms); //$NON-NLS-1$
            }
            return String.format("%02d:%02d:%02d", hms); //$NON-NLS-1$
        }
    }

    public static String removeNewLineCharacter(String text) {
        return text.replaceAll(" ?(\\r\\n|\\n|\\r)+", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String formatTimeMillis(long timeMillis, String pattern) {
        if (timeMillis <= 0 || pattern == null) {
            return null;
        }
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateFormat formatter = new SimpleDateFormat(pattern,
                Locale.getDefault());
        return formatter.format(calendar.getTime());
    }

}
