package org.xmind.ui.internal.biggerplate.utils;

public class UrlUtils {

    public static String getParameter(String url, String parameterKey) {
        if (url == null || parameterKey == null || url.indexOf('?') == -1
                || url.indexOf('?') == url.length() - 1) {
            return null;
        }
        String paramatersString = url.substring(url.indexOf('?') + 1);
        String[] parameterEntrys = paramatersString.split("&"); //$NON-NLS-1$
        for (String parameterEntry : parameterEntrys) {
            if (parameterEntry.startsWith(parameterKey)) {
                if (parameterEntry.indexOf('=') == -1
                        || parameterEntry.indexOf('=') == parameterEntry
                                .length() - 1) {
                    return null;
                } else {
                    return parameterEntry
                            .substring(parameterEntry.indexOf('=') + 1);
                }
            }
        }
        return null;
    }

    private UrlUtils() {
    }

}
