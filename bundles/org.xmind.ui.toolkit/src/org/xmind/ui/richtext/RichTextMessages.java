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
package org.xmind.ui.richtext;

import org.eclipse.osgi.util.NLS;

public class RichTextMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.richtext.messages"; //$NON-NLS-1$

    public static String AlignCenterAction_text;
    public static String AlignCenterAction_toolTip;
    public static String AlignLeftAction_text;
    public static String AlignLeftAction_toolTip;
    public static String AlignRightAction_text;
    public static String AlignRightAction_toolTip;
    public static String BackgroundAction_text;
    public static String BoldAction_text;
    public static String BoldAction_toolTip;
    public static String BulletAction_Text;
    public static String BulletAction_toolTip;
    public static String FontAction_text;
    public static String FontAction_toolTip;
    public static String ForegroundAction_text;
    public static String IndentAction_text;
    public static String IndentAction_toolTip;
    public static String ItalicAction_text;
    public static String ItalicAction_toolTip;
    public static String OutdentAction_text;
    public static String OutdentAction_toolTip;
    public static String StrikeoutAction_text;
    public static String StrikeoutAction_toolTip;
    public static String UnderlineAction;
    public static String UnderlineAction_toolTip;

    public static String ACTIONBAR_FONT_MENU_TEXT;
    public static String ACTIONBAR_ALIGN_MENU_TEXT;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, RichTextMessages.class);
    }

    private RichTextMessages() {
    }
}