package org.xmind.ui.internal.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.ui.internal.views.messages"; //$NON-NLS-1$
    public static String BlackBoxView_DeleteBackups;
    public static String BlackBoxView_Description_text;
    public static String BlackBoxView_Info;
    public static String BlackBoxView_OpenVersion;
    public static String BlackBoxView_Versions;

    // Inspector
    public static String AuthorInfoInspectorSection_title;
    public static String AuthorInfoInspectorSection_Name;
    public static String AuthorInfoInspectorSection_Email;
    public static String AuthorInfoInspectorSection_Organization;
    public static String AuthorInfoInspectorSection_Enter_Name;
    public static String AuthorInfoInspectorSection_Enter_Email;
    public static String AuthorInfoInspectorSection_Enter_Organization;
    public static String AttachmentsInspectorSection_title;
    public static String ExternalFilesInspectorSection_title;
    public static String HyperlinkInspectorSection_title;
    public static String ImageInspectorSection_title;
    public static String FileInfoInspectorSection_title;
    public static String NoContent_message;
    public static String FileInfoEstimateSize_label;
    public static String FileInfoWords_label;
    public static String FileInfoTopics_label;
    public static String FileInfoRevisions_label;
    public static String FileInfoModifiedTime_label;
    public static String FileInfoModifiedBy_label;
    public static String FileInfoCreatedTime_label;
    public static String RemoveAllRevisionsDialog_title;
    public static String RemoveAllRevisionsDialog_message;

    public static String ThemesView_Dialog_title;
    public static String ThemesView_Dialog_message;
    public static String ThemesView_Dialog_Check;
    public static String ThemesView_Dialog_PrefLink;
    public static String ThemesView_OverrideButton;
    public static String ThemesView_KeepButton;

    public static String ThemeUICore_group_default_name;
    public static String ThemeUICore_group_user_name;

    public static String ThemesPopover_MoreTheme_label;
    public static String ThemesPopover_ManagerTheme_label;
    public static String ThemesPopover_Extract_Current_Theme_label;

    public static String MarkersPopover_ManageMarkers_label;
    public static String MarkersPopover_ImportMarkers_label;
    public static String MarkersPopover_ExportMarkers_label;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
