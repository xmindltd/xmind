/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.share;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "net.xmind.share.messages"; //$NON-NLS-1$

    public static String Uploader_openAction_text;

    public static String UploaderDialog_containUnupload_text;

    public static String UploaderDialog_CreateError_Text;

    public static String UploaderDialog_windowTitle;
    public static String UploaderDialog_title;
    public static String UploaderDialog_message;

    public static String UploaderDialog_GeneralPage_title;
    public static String UploaderDialog_Upload_text;
    public static String UploaderDialog_Title_text;
    public static String UploaderDialog_Description_text;
    public static String UploaderDialog_Privacy_title;
    public static String UploaderDialog_Public_label;
    public static String UploaderDialog_PublicView_label;
    public static String UploaderDialog_Private_label;
    public static String UploaderDialog_LanguageChannel_label;
    public static String UploaderDialog_Privacy_text;
    public static String UploaderDialog_Download_text;
    public static String UploaderDialog_Privacy_link;

    public static String UploaderDialog_OKPressedError_Text;

    public static String UploaderDialog_Privacy_Public_title;
    public static String UploaderDialog_Privacy_Public_description;
    public static String UploaderDialog_Privacy_Private_title;
    public static String UploaderDialog_Privacy_Private_description;
    public static String UploaderDialog_Privacy_Unlisted_title;
    public static String UploaderDialog_Privacy_Unlisted_description;
    public static String UploaderDialog_Privacy_AllowDownload_text;
    public static String UploaderDialog_Privacy_DownloadAllowed;
    public static String UploaderDialog_Privacy_DownloadForbidden;

    public static String UploaderDialog_ThumbnailPage_title;
    public static String UploaderDialog_Thumbnail_description;

    public static String UploaderDialog_uploadOneSheet_message;

    public static String UploadJob_name;
    public static String UploadJob_Task_Prepare;
    public static String UploadJob_Task_TransferFile;
    public static String UploadJob_Task_Cancel;
    public static String UploadJob_Failure_message;
    public static String UploadJob_Success_message;
    public static String UploadJob_Canceled_message;
    public static String UploadJob_OpenMap_title;
    public static String UploadJob_OpenMap_message;
    public static String UploadJob_View_text;
    public static String UploadJob_Close_text;

    public static String TransferFileJob_ErrorCode_message;

    public static String failedToGenerateThumbnail;
    public static String failedToGenerateUploadFile;

    public static String ErrorDialog_title;
    public static String ErrorDialog_message;
    public static String ErrorDialog_Unauthorized_message;
    public static String ErrorDialog_OutOfMemory_message;
    public static String ErrorDialog_UnexpectedError_message;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}