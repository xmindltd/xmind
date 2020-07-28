/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EncodingUtils {

    private static final Map<String, String> ENTITIES = new HashMap<String, String>(
            5);

    static {
        ENTITIES.put("lt", "<"); //$NON-NLS-1$ //$NON-NLS-2$
        ENTITIES.put("gt", ">"); //$NON-NLS-1$ //$NON-NLS-2$
        ENTITIES.put("amp", "&"); //$NON-NLS-1$ //$NON-NLS-2$
        ENTITIES.put("apos", "'"); //$NON-NLS-1$ //$NON-NLS-2$
        ENTITIES.put("quot", "\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final Pattern ESCAPER = Pattern.compile("&([^;]+);"); //$NON-NLS-1$

    private EncodingUtils() {
    }

    public static String unescape(String text) {
        StringBuffer buffer = new StringBuffer(text.length());
        Matcher matcher = ESCAPER.matcher(text);
        String unescaped, entity;
        int charCode;
        while (matcher.find()) {
            entity = matcher.group(1);
            if (entity.length() > 1 && entity.charAt(0) == '#') {
                if (entity.length() > 2 && entity.charAt(1) == 'x') {
                    charCode = Integer.parseInt(entity.substring(2), 16);
                } else {
                    charCode = Integer.parseInt(entity.substring(1), 10);
                }
                unescaped = Character.toString((char) charCode);
            } else {
                unescaped = ENTITIES.get(entity);
                if (unescaped == null) {
                    unescaped = "&" + entity + ";"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            matcher.appendReplacement(buffer, unescaped);
        }
        matcher.appendTail(buffer);
        return buffer.toString().replaceAll("\\s+", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
