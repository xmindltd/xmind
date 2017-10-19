package org.xmind.ui.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.xmind.ui.util.messages"; //$NON-NLS-1$

    public static String BMPFile;
    public static String JPEGFile;
    public static String GIFFile;
    public static String PNGFile;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
