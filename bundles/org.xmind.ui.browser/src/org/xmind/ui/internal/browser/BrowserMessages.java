/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.ui.internal.browser;

import org.eclipse.osgi.util.NLS;

public class BrowserMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.internal.browser.messages"; //$NON-NLS-1$

    public static String BrowserText_Details;
    public static String BrowserText_EmbeddedBrowserUnavailableInfo;
    public static String BrowserText_OpenFileLinkToolTip;
    public static String BrowserText_ProblemDetails;
    public static String BrowserText_ProblemDetailsTitle;
    public static String BrowserViewer_NextPage_toolTip;
    public static String BrowserViewer_PrevPage_toolTip;
    public static String BrowserViewer_Refresh_toolTip;
    public static String BrowserViewer_Stop_toolTip;
    public static String ExternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message;
    public static String InternalWebBrowser_ErrorCouldNotLaunchWebBrowser_message;
    public static String BrowserPrefPage_title;
    public static String BrowserPrefPage_description;
    public static String BrowserPrefPage_InternalBrowser_text;
    public static String BrowserPrefPage_ExternalBrowser_text;
    public static String BrowserEditor_title;
    public static String BrowserEditor_ErrorInvalidEditorInput_message;
    public static String BrowserView_OpenInExternalBrowser_text;
    public static String BrowserView_OpenInExternalBrowser_toolTip;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, BrowserMessages.class);
    }

    private BrowserMessages() {
    }
}