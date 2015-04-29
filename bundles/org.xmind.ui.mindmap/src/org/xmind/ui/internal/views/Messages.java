package org.xmind.ui.internal.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.xmind.ui.internal.views.messages"; //$NON-NLS-1$
    public static String BlackBoxView_DeleteBackups;
    public static String BlackBoxView_Description_text;
    public static String BlackBoxView_Info;
    public static String BlackBoxView_OpenVersion;
    public static String BlackBoxView_Versions;

    public static String ThemesView_Dialog_title;
    public static String ThemesView_Dialog_message;
    public static String ThemesView_Dialog_Check;
    public static String ThemesView_Dialog_PrefLink;
    public static String ThemesView_OverrideButton;
    public static String ThemesView_KeepButton;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
