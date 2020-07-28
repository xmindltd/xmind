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
package org.xmind.ui.internal.prefs;

import org.eclipse.osgi.util.NLS;

public class PrefMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.internal.prefs.messages"; //$NON-NLS-1$

    public static String DnDPrefPage_AlwaysRequestButton;

    public static String DnDPrefPage_CopyButton;

    public static String DnDPrefPage_DnDGroup_text;

    public static String DnDPrefPage_DnDLabel_Text;

    public static String DnDPrefPage_LinkButton;

    public static String EditorPage_title;
    public static String EditorPage_UndoLimit_title;
    public static String EditorPage_UndoLimit_label;
    public static String EditorPage_UndoRedo_description;
    public static String EditorPage_TopicPositioning_title;
    public static String EditorPage_TopicPositioning_AllowOverlaps;
    public static String EditorPage_TopicPositioning_AllowManualLayout;
    public static String EditorPage_TopicPositioning_AllowFreePosition;
    public static String EditorPage_TopicPositioning_FreePositioning_description;
    public static String EditorPage_Preview_text;
    public static String EditorPage_EnableAnimation_text;
    public static String EditorPage_EnableShadow_text;
    public static String EditorPage_Zoom_Scale_text;

    public static String EditorPage_UndoRedo_gradientColor;
    public static String EditorPage_UndoRedo_tips;
    public static String EditorPage_UndoRedo_tips_fade_delay;
//    public static String EditorPage_UndoRedo_tipsFadeDelay_label;

    public static String MarkersPage_title;
    public static String MarkersPage_Groups_label;
    public static String MarkersPage_Markers_label;
    public static String MarkersPage_AddGroup_text;
    public static String MarkersPage_RemoveGroup_text;
    public static String MarkersPage_RenameGroup_text;
    public static String MarkersPage_ExportGroup_text;
    public static String MarkersPage_AddMarker_text;
    public static String MarkersPage_RemoveMarker_text;
    public static String MarkersPage_RenameMarker_text;
    public static String MarkersPage_DefaultGroupName;

    public static String AuthorInfoPage_Message;
    public static String AuthorInfoPage_Name_label;
    public static String AuthorInfoPage_Email_label;
    public static String AuthorInfoPage_Organization_label;

    public static String PreferencePage_EmptyName_errorMessage;

    public static String ThemePrefPage_title;
    public static String ThemePrefPage_Group_text;
    public static String ThemePrefPage_OverrideButton;
    public static String ThemePrefPage_KeepButton;
    public static String ThemePrefPage_AskButton;

    static {
        NLS.initializeMessages(BUNDLE_NAME, PrefMessages.class);
    }

    private PrefMessages() {
    }

}