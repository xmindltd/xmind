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
package org.xmind.ui.datepicker;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.datepicker.messages"; //$NON-NLS-1$

    // Full name of each month
    public static String January;
    public static String Feburary;
    public static String March;
    public static String April;
    public static String May;
    public static String June;
    public static String July;
    public static String August;
    public static String September;
    public static String October;
    public static String November;
    public static String December;

    // Abbr name of each week day
    public static String Monday;
    public static String Tuesday;
    public static String Wednesday;
    public static String Thursday;
    public static String Friday;
    public static String Saturday;
    public static String Sunday;

    public static String TodayPattern;
    public static String None;
    public static String Illegal;

    public static String TimeCheckInvalid_label;
    public static String TimeCheckInvalidSmall_message;
    public static String TimeCheckInvalidBig_message;

    public static String DatePicker_Time_AM_text;
    public static String DatePicker_Time_PM_text;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
