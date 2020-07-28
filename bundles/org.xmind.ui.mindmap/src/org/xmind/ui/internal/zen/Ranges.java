/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.zen;

import org.xmind.core.internal.dom.NumberUtils;

public class Ranges {

    public static final String RANGE_MASTER = "master"; //$NON-NLS-1$

    public static int parseStartIndex(String range) {
        if (range != null && range.startsWith("(") //$NON-NLS-1$
                && range.endsWith(")")) { //$NON-NLS-1$
            int sep = range.indexOf(',');
            if (sep > 0) {
                String startIndexValue = range.substring(1, sep).trim();
                int index = NumberUtils.safeParseInt(startIndexValue, -1);
                return index < 0 ? -1 : index;
            }
        }
        return -1;
    }

    public static int parseEndIndex(String range) {
        if (range != null && range.startsWith("(") //$NON-NLS-1$
                && range.endsWith(")")) { //$NON-NLS-1$
            int sep = range.lastIndexOf(',');
            if (sep > 0) {
                String endIndexValue = range
                        .substring(sep + 1, range.length() - 1).trim();
                int index = NumberUtils.safeParseInt(endIndexValue, -1);
                return index < 0 ? -1 : index;
            }
        }
        return -1;
    }

}
