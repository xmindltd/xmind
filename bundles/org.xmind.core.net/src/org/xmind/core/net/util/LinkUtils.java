package org.xmind.core.net.util;

public class LinkUtils {

    public static final String HOST_NET = "www.xmind.net"; //$NON-NLS-1$

    public static final String HOST_CN = "www.xmind.cn"; //$NON-NLS-1$

    private static final String LANGUAGE_OSGI_NL_KEY = "osgi.nl"; //$NON-NLS-1$

    private static final String LANGUAGE_KEY_CN = "zh_CN"; //$NON-NLS-1$

    public static boolean isCnLanguage() {
        String lang = System.getProperty(LANGUAGE_OSGI_NL_KEY);
        return LANGUAGE_KEY_CN.equals(lang);
    }

    public static String getHostByLanguage(boolean protocol, boolean https) {
        String host = isCnLanguage() ? HOST_CN : HOST_NET;
        if (protocol) {
            if (https) {
                host = "https://" + host; //$NON-NLS-1$
            } else {
                host = "http://" + host; //$NON-NLS-1$
            }
        }
        return host;
    }

    public static String getHostByUser(boolean isCnUser, boolean protocol,
            boolean https) {
        String host = isCnUser ? HOST_CN : HOST_NET;
        if (protocol) {
            if (https) {
                host = "https://" + host; //$NON-NLS-1$
            } else {
                host = "http://" + host; //$NON-NLS-1$
            }
        }
        return host;
    }

    /**
     * @param protocol
     * @param https
     * @param api
     *            should start with "/"
     * @return
     */
    public static String getLinkByLanguage(boolean protocol, boolean https,
            String api) {
        return getHostByLanguage(protocol, https) + (api == null ? "" : api); //$NON-NLS-1$
    }

    /**
     * @param isCnUser
     * @param protocol
     * @param https
     * @param api
     *            should start with "/"
     * @return
     */
    public static String getLinkByUser(boolean isCnUser, boolean protocol,
            boolean https, String api) {
        return getHostByUser(isCnUser, protocol, https)
                + (api == null ? "" : api); //$NON-NLS-1$
    }

    private LinkUtils() {
    }

}
