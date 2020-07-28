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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;

public abstract class StyleSheet implements IStyleSheet {

    protected static final Set<IStyle> NO_STYLES = Collections.emptySet();

    private IStyleSheet parent = null;

    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public Set<IStyle> getAllStyles() {
        Set<IStyle> normalStyles = getStyles(NORMAL_STYLES);
        Set<IStyle> masterStyles = getStyles(MASTER_STYLES);
        Set<IStyle> automaticStyles = getStyles(AUTOMATIC_STYLES);
        int size = normalStyles.size() + masterStyles.size()
                + automaticStyles.size();
        if (size == 0)
            return NO_STYLES;
        List<IStyle> list = new ArrayList<IStyle>(size);
        list.addAll(automaticStyles);
        list.addAll(masterStyles);
        list.addAll(normalStyles);
        return DOMUtils.unmodifiableSet(list);
    }

    public boolean isEmpty() {
        return getAllStyles().isEmpty();
    }

    public IStyleSheet getParentSheet() {
        return parent;
    }

    public void setParentSheet(IStyleSheet parent) {
        this.parent = parent;
    }

    public IStyle findStyle(String styleId) {
        if (styleId == null)
            return null;
        IStyle style = getLocalStyle(styleId);
        if (style != null)
            return style;
        if (parent != null)
            style = parent.findStyle(styleId);
        return style;
    }

    protected abstract IStyle getLocalStyle(String styleId);

}