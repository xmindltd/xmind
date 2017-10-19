package org.xmind.ui.resources;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.resources.messages"; //$NON-NLS-1$

    public static String FetchFontList_jobName;

    public static String FetchFontNames;

    public static String FilterFontList;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
