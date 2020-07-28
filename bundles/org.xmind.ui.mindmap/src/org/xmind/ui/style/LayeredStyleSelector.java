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
package org.xmind.ui.style;

import static org.xmind.ui.style.Styles.LAYER_AFTER_ALL_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_DEFAULT_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_THEME_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_USER_VALUE;

import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;

@Deprecated
public abstract class LayeredStyleSelector extends MindMapStyleSelectorBase {

    protected String getThemeStyleValue(IGraphicalPart part, String familyName,
            String key) {
        String value = null;
        value = getLayeredProperty(part, LAYER_BEFORE_THEME_VALUE, familyName,
                key);
        if (isValidValue(part, key, value))
            return getCheckedValue(value);
        return super.getThemeStyleValue(part, familyName, key);
    }

    protected String getDefaultStyleValue(IGraphicalPart part,
            String familyName, String key,
            IStyleValueProvider defaultValueProvider) {
        String value = null;
        value = getLayeredProperty(part, LAYER_BEFORE_DEFAULT_VALUE, familyName,
                key);
        if (isValidValue(part, key, value))
            return getCheckedValue(value);

        value = super.getDefaultStyleValue(part, familyName, key,
                defaultValueProvider);
        if (isValidValue(part, key, value))
            return getCheckedValue(value);

        value = getLayeredProperty(part, LAYER_AFTER_ALL_VALUE, familyName,
                key);
        return value;
    }

    public String getStyleValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        String familyName = getFamilyName(part);
        if (familyName != null) {
            String value = getLayeredProperty(part, LAYER_BEFORE_USER_VALUE,
                    familyName, key);
            if (value != null)
                return getCheckedValue(value);
        }
        String value = super.getStyleValue(part, key, defaultValueProvider);
        if (value == null && !ignoresAutoValue(part, key))
            value = getAutoValue(part, key, defaultValueProvider);
        return value;
    }

    protected String getCheckedValue(String value) {
        if (Styles.NULL.equals(value))
            return null;
        return value;
    }

    protected abstract String getLayeredProperty(IGraphicalPart part,
            String layerName, String familyName, String key);

}