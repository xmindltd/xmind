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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_VERSION;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.util.DOMUtils;

/**
 * @author briansun
 * 
 */
public class InternalDOMUtils {

    public static void addVersion(Document document) {
        Element element = document.getDocumentElement();
        if (element != null && !element.hasAttribute(ATTR_VERSION)) {
            DOMUtils.setAttribute(element, ATTR_VERSION,
                    Core.getCurrentVersion());
        }
    }

    public static void replaceVersion(Document document) {
        Element element = document.getDocumentElement();
        if (element != null) {
            DOMUtils.setAttribute(element, ATTR_VERSION,
                    Core.getCurrentVersion());
        }
    }

    public static String getParentPath(String path) {
        int i;
        if (path.endsWith("/")) { //$NON-NLS-1$
            i = path.lastIndexOf('/', path.length() - 2);
        } else {
            i = path.lastIndexOf('/');
        }
        if (i < 0)
            return null;
        return path.substring(0, i + 1);
    }

    public static String getLastName(String path) {
        String parent = getParentPath(path);
        if (parent != null) {
            return path.substring(parent.length());
        }
        return path;
    }

    public static boolean isParentPath(String path, String parentPath) {
        return path.startsWith(parentPath);
    }

    public static String trim(String text) {
        return text == null ? null : text.trim();
    }

    public static String trimElementContent(String text, String tagName) {
        int start = text.indexOf("<" + tagName + " "); //$NON-NLS-1$ //$NON-NLS-2$
        if (start < 0) {
            start = text.indexOf("<" + tagName + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (start >= 0) {
            start = text.indexOf('>', start + 1);
            if (start > 0) {
                int i = start - 1;
                // prevent element like '<tag />'
                while (text.charAt(i) != '<') {
                    if (text.charAt(i) == '/')
                        return null;
                    i--;
                }
            }
            start++;
        }
        int end = text.lastIndexOf("</" + tagName + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        if (start > 0 || (end >= 0 && end < text.length()))
            return text.substring(start, end);
        return text;
    }

    public static String makeElementText(String text, NS defaultNS,
            String tagName, NS... nss) {
        if (tagName != null) {
            if (text.indexOf("<" + tagName) < 0) { //$NON-NLS-1$
                StringBuffer sb = new StringBuffer(
                        tagName.length()
                                + (defaultNS != null
                                        ? defaultNS.getURI().length() : 0)
                                + nss.length * 20 + 2);
                sb.append('<');
                sb.append(tagName);
                if (defaultNS != null) {
                    sb.append(" xmlns=\""); //$NON-NLS-1$
                    sb.append(defaultNS.getURI());
                    sb.append('\"');
                }
                for (NS ns : nss) {
                    sb.append(" xmlns:"); //$NON-NLS-1$
                    sb.append(ns.getPrefix());
                    sb.append('=');
                    sb.append('\"');
                    sb.append(ns.getURI());
                    sb.append('\"');
                }
                sb.append('>');
                sb.append(text);
                sb.append('<');
                sb.append('/');
                sb.append(tagName);
                sb.append('>');
                return sb.toString();
            }
        }
        return text;
    }

    public static String toRangeValue(int startIndex, int endIndex) {
        if (startIndex >= 0 || endIndex >= 0) {
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            if (startIndex >= 0)
                sb.append(startIndex);
            sb.append(',');
            if (endIndex >= 0)
                sb.append(endIndex);
            sb.append(')');
            return sb.toString();
        }
        return null;
    }

    public static int getStartIndex(String rangeValue) {
        if (rangeValue != null && rangeValue.startsWith("(") //$NON-NLS-1$
                && rangeValue.endsWith(")")) { //$NON-NLS-1$
            int sep = rangeValue.indexOf(',');
            if (sep > 0) {
                String startIndexValue = rangeValue.substring(1, sep).trim();
                int index = NumberUtils.safeParseInt(startIndexValue, -1);
                return index < 0 ? -1 : index;
            }
        }
        return -1;
    }

    public static int getEndIndex(String rangeValue) {
        if (rangeValue != null && rangeValue.startsWith("(") //$NON-NLS-1$
                && rangeValue.endsWith(")")) { //$NON-NLS-1$
            int sep = rangeValue.lastIndexOf(',');
            if (sep > 0) {
                String endIndexValue = rangeValue
                        .substring(sep + 1, rangeValue.length() - 1).trim();
                int index = NumberUtils.safeParseInt(endIndexValue, -1);
                return index < 0 ? -1 : index;
            }
        }
        return -1;
    }

    public static void updateModificationInfo(IAdaptable obj) {
        Element ele = (Element) obj.getAdapter(Element.class);
        if (ele == null)
            return;

        long oldTime = getModifiedTime(obj, ele);
        long newTime = System.currentTimeMillis();
        DOMUtils.setAttribute(ele, DOMConstants.ATTR_TIMESTAMP,
                Long.toString(newTime));

        DOMUtils.setAttribute(ele, DOMConstants.ATTR_MODIFYBY, null);
        DOMUtils.setAttribute(ele, DOMConstants.ATTR_MODIFIED_BY,
                getDefaultModifierName());

        ICoreEventSupport eventSupport = (ICoreEventSupport) obj
                .getAdapter(ICoreEventSupport.class);
        if (eventSupport != null && obj instanceof ICoreEventSource) {
            eventSupport.dispatchValueChange((ICoreEventSource) obj,
                    Core.ModifyTime, Long.valueOf(oldTime),
                    Long.valueOf(newTime));
        }
    }

    public static long getModifiedTime(IAdaptable obj, Element ele) {
        String time = DOMUtils.getAttribute(ele, DOMConstants.ATTR_TIMESTAMP);
        return NumberUtils.safeParseLong(time, 0);
    }

    public static String getModifiedBy(IAdaptable obj, Element ele) {
        String name = DOMUtils.getAttribute(ele, DOMConstants.ATTR_MODIFIED_BY);
        if (name != null)
            return name;
        return DOMUtils.getAttribute(ele, DOMConstants.ATTR_MODIFYBY);
    }

    public static String getDefaultModifierName() {
        String name = System.getProperty(DOMConstants.AUTHOR_NAME);
        return name != null ? name : System.getProperty("user.name"); //$NON-NLS-1$
    }

}