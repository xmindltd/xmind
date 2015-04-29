package org.xmind.ui.internal.evernote;

import org.eclipse.osgi.util.NLS;

/**
 * @author Jason Wong
 */
public class EvernoteMessages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.internal.evernote.messages"; //$NON-NLS-1$

    public static String EvernoteSignInDialog_EvernoteTab_text;
    public static String EvernoteSignInDialog_YinxiangTab_text;
    public static String EvernoteSignInDialog_Evernote_title;
    public static String EvernoteSignInDialog_Yinxiang_title;
    public static String EvernoteSignInDialog_Connecting_label;
    public static String EvernoteSignInDialog_BrowserCrash_label;

    public static String EvernoteExportDialog_title;
    public static String EvernoteExportDialog_IncludeImage;
    public static String EvernoteExportDialog_IncludeFile;
    public static String EvernoteExportDialog_IncludeText;
    public static String EvernoteExportDialog_SaveButton_label;
    public static String EvernoteExportDialog_Content_label;
    public static String EvernoteExportDialog_Notebook_label;

    public static String PreferencePage_FeatureDescription_message;
    public static String PreferencePage_LinkEvernote_text;
    public static String PreferencePage_UnlinkEvernote_text;
    public static String PreferencePage_AccountInfo_label;

    public static String EvernoteExportJob_UnknownError_message;
    public static String EvernoteExportJob_AuthExpired_message;
    public static String EvernoteExportJob_InvalidAuth_message;
    public static String EvernoteExportJob_jobName_withCentralTopicTitle;
    public static String EvernoteExportJob_QuotaReached_message_withMonthlyQuota;
    public static String EvernoteExportJob_LimitReached_message_withMaximumNoteSize;
    public static String EvernoteExportJob_networkErrorText_withErrorMessage;
    public static String EvernoteExportJob_OtherException_message_withErrorMessage;
    public static String EvernoteExportJob_systemErrorText_withErrorMessage;

    public static String EvernoteExportHandler_okActionLabel;
    public static String EvernoteExportHandler_successfullySaveText;

    public static String EvernoteOAuthJob_title;

    public static String EvernotePrefPage_unlinkAccountText;
    public static String EvernotePrefPage_unlinkAccountTitle;

    public static String EvernoteExporter_Note_untitled;

    static {
        NLS.initializeMessages(BUNDLE_NAME, EvernoteMessages.class);
    }

    private EvernoteMessages() {
    }
}
