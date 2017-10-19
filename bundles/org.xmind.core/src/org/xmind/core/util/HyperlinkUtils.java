/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmind.core.IIdentifiable;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;

public class HyperlinkUtils {

    private HyperlinkUtils() {
    }

    public static String getProtocolName(String uri) {
        int i = uri.indexOf(':');
        return i < 0 ? null : uri.substring(0, i);
    }

    public static String trimURLContent(String uri) {
        int i = uri.indexOf(':');
        if (i >= 0) {
            uri = uri.substring(i + 1);
            while (uri.startsWith("/")) { //$NON-NLS-1$
                uri = uri.substring(1);
            }
        }
        return uri;
    }

    public static String trimFileUrlContent(String uri) {
        if (uri.startsWith("file://")) //$NON-NLS-1$
            return uri.substring("file://".length()); //$NON-NLS-1$
        return uri;
    }

    public static String getAttachmentProtocolName() {
        return "xap"; //$NON-NLS-1$
    }

    public static String getFileProtocolName() {
        return "file"; //$NON-NLS-1$
    }

    public static boolean isFileUrl(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        return getFileProtocolName().equals(getProtocolName(url));
    }

    public static boolean isAttachmentURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        String protocol = getProtocolName(url);
        return getAttachmentProtocolName().equals(protocol);
    }

    public static String toAttachmentURL(String path) {
        return getAttachmentProtocolName() + ":" + path; //$NON-NLS-1$
    }

    public static String toAttachmentPath(String url) {
        return trimURLContent(url);
    }

    public static boolean isInternalAttachmentURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        return url.startsWith("platform:/plugin"); //$NON-NLS-1$
    }

    public static String getInternalProtocolName() {
        return "xmind"; //$NON-NLS-1$
    }

    public static boolean isInternalURL(String url) {
        if (url == null || "".equals(url)) //$NON-NLS-1$
            return false;
        return getInternalProtocolName().equals(getProtocolName(url));
    }

    public static String toInternalURL(String elementId) {
        return getInternalProtocolName() + ":#" + elementId; //$NON-NLS-1$
    }

    public static String toInternalURL(Object element) {
        return toInternalURL(element, null);
    }

    public static String toInternalURL(Object element, IWorkbook workbook) {
        if (element instanceof IIdentifiable) {
            String id = ((IIdentifiable) element).getId();
            //String mainPath = getMainPath(element, workbook);
            return getInternalProtocolName() + ":#" + id; //$NON-NLS-1$
        }
        return null;
    }

    public static String toElementID(String uri) {
        if (isInternalURL(uri)) {
            int index = uri.indexOf("#"); //$NON-NLS-1$
            if (index >= 0) {
                return uri.substring(index + 1);
            }
        }
        return null;
    }

//    /**
//     * @param element
//     * @param workbook
//     * @return
//     */
//    private static String getMainPath(Object element, IWorkbook workbook) {
//        if (workbook != null) {
//            String file = workbook.getFile();
//            if (file != null) {
//                return file;
//            }
//        }
//        return ""; //$NON-NLS-1$
//    }

    public static Object findElement(String uri, IWorkbook workbook) {
        String id = toElementID(uri);
        if (id != null) {
            Object element = workbook.getElementById(id);
            if (element instanceof ITopic) {
                if (!isAttach((ITopic) element)) {
                    element = null;
                }
            }
            return element;
        }
        return null;
    }

    private static boolean isAttach(ITopic topic) {
        return topic.getPath().getWorkbook() == topic.getOwnedWorkbook();
    }

    @SuppressWarnings("nls")
    public static boolean isLinkToWeb(String urlOrBookmark) {
        if (urlOrBookmark.contains("www.") || urlOrBookmark.contains(".com")
                || urlOrBookmark.contains(".cn")
                || urlOrBookmark.contains(".org")
                || urlOrBookmark.contains(".cc")
                || urlOrBookmark.contains(".net")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("nls")
    public static boolean isUrlAddress(String text) {
        final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher;
        if (!text.contains("http://") && !text.contains("https://")
                && !text.contains("file://")) {
            matcher = pattern.matcher("http://" + text);
        } else {
            matcher = pattern.matcher(text);
        }
        return matcher.matches() && isLinkToWeb(text);
    }

    public static boolean isEmailAddress(String text) {
        final String EMAIL_REGEX = "^([\\w_\\-\\.]+)@((\\[[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.)|(([\\w\\-]+\\.)+))([a-zA-Z]{2,4}|[\\d]{1,3})(\\]?)$"; //$NON-NLS-1$
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }

}