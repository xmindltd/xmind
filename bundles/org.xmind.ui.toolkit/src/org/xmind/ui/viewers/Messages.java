package org.xmind.ui.viewers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.viewers.messages"; //$NON-NLS-1$

    public static String CategorizedViewer_UnknownCategory;

    public static String ZoomOut_toolTip;

    public static String ZoomIn_toolTip;

    public static String ContentsView_NoContent;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
