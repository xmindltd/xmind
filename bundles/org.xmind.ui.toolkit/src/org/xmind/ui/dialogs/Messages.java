/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

package org.xmind.ui.dialogs;

import org.eclipse.osgi.util.NLS;

/**
 * @author Frank Shaka
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.dialogs.messages"; //$NON-NLS-1$

    public static String ErrorDetailsDialog_Copied_message;
    public static String ErrorDetailsDialog_CopyButton_text;

    public static String Notification_closeButton_text;

    public static String NotificationDialog_MoreLink_defaultText;

    public static String ViewModelPart_MenuBar_toolTip;

    public static String FullRichTextAction_FontViewer_toolTip;
    public static String FullRichTextAction_FontSizeViewer_toolTip;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
