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
package org.xmind.core.internal;

import java.util.Collection;
import java.util.Collections;

import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

public abstract class Style implements IStyle {

    protected static final Collection<String> NO_VALUES = Collections
            .emptySet();

    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public IStyle getDefaultStyle(String styleFamily) {
        if (styleFamily == null)
            return null;
        String styleId = getDefaultStyleId(styleFamily);
        return getDefaultStyleById(styleId);
    }

    public IStyle getDefaultStyleById(String styleId) {
        if (styleId == null)
            return null;
        IStyleSheet sheet = getOwnedStyleSheet();
        if (sheet == null)
            return null;

        return sheet.findStyle(styleId);
    }

}