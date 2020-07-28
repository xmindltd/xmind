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
package org.xmind.ui.internal.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;

public class BrowserPref {

    protected static final String PREF_INTERNAL_WEB_BROWSER_HISTORY = "internalWebBrowserHistory"; //$NON-NLS-1$

    protected static final String PREF_BROWSER_CHOICE = "browserChoice"; //$NON-NLS-1$

    public static final int INTERNAL = 1;

    public static final int EXTERNAL = 0;

    private BrowserPref() {
    }

    /**
     * Returns the preference store.
     * 
     * @return the preference store
     */
    protected static IPreferenceStore getPreferenceStore() {
        return BrowserPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Returns the Web browser history list.
     * 
     * @return java.util.List
     */
    public static List<String> getInternalWebBrowserHistory() {
        String temp = getPreferenceStore().getString(
                PREF_INTERNAL_WEB_BROWSER_HISTORY);
        StringTokenizer st = new StringTokenizer(temp, "|*|"); //$NON-NLS-1$
        List<String> l = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            l.add(s);
        }
        return l;
    }

    /**
     * Sets the Web browser history.
     * 
     * @param list
     *            the history
     */
    public static void setInternalWebBrowserHistory(List<String> list) {
        StringBuffer sb = new StringBuffer();
        if (list != null) {
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String s = iterator.next();
                sb.append(s);
                sb.append("|*|"); //$NON-NLS-1$
            }
        }
        getPreferenceStore().setValue(PREF_INTERNAL_WEB_BROWSER_HISTORY,
                sb.toString());
//        BrowserPlugin.getDefault().savePluginPreferences();
//        InstanceScope instanceScope = new InstanceScope();
//        String bundId = String.valueOf(BrowserPlugin.getDefault().getBundle()
//                .getBundleId());
//        try {
//            instanceScope.getNode(bundId).flush();
//        } catch (BackingStoreException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Returns whether the internal browser is used by default
     * 
     * @return true if the internal browser is used by default
     */
    public static boolean isDefaultUseInternalBrowser() {
        return BrowserUtil.canUseInternalWebBrowser();
    }

    /**
     * Returns whether the system browser is used by default
     * 
     * @return true if the system browser is used by default
     */
    public static boolean isDefaultUseSystemBrowser() {
        return BrowserUtil.canUseSystemBrowser();
    }

    /**
     * Returns whether the internal or external browser is being used
     * 
     * @return one of <code>INTERNAL</code> or <code>EXTERNAL</code>.
     */
    public static int getBrowserChoice() {
        int choice = getPreferenceStore().getInt(PREF_BROWSER_CHOICE);
        if (choice == 2)
            return EXTERNAL;
        if (choice == INTERNAL && !BrowserUtil.canUseInternalWebBrowser())
            return EXTERNAL;
        return choice;
    }

    /**
     * Sets whether the internal, system and external browser is used
     * 
     * @param choice
     *            </code>INTERNAL</code>, <code>SYSTEM</code> and
     *            <code>EXTERNAL</code>
     */
    public static void setBrowserChoice(int choice) {
        getPreferenceStore().setValue(PREF_BROWSER_CHOICE, choice);
//        BrowserPlugin.getDefault().savePluginPreferences();
    }

}