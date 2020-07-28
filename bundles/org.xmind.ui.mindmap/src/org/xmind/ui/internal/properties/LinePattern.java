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
package org.xmind.ui.internal.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;

public enum LinePattern {

    Solid(Styles.LINE_PATTERN_SOLID, PropertyMessages.LinePattern_Solid,
            IMindMapImages.PATTERN_SOLID), //
    Dash(Styles.LINE_PATTERN_DASH, PropertyMessages.LinePattern_Dash,
            IMindMapImages.PATTERN_DASH), //
    Dot(Styles.LINE_PATTERN_DOT, PropertyMessages.LinePattern_Dot,
            IMindMapImages.PATTERN_DOT), //
    DashDot(Styles.LINE_PATTERN_DASH_DOT, PropertyMessages.LinePattern_DashDot,
            IMindMapImages.PATTERN_DASHDOT), //
    DashDotDot(Styles.LINE_PATTERN_DASH_DOT_DOT,
            PropertyMessages.LinePattern_DashDotDot,
            IMindMapImages.PATTERN_DASHDOTDOT);

    private String value;

    private String name;

    private String iconName;

    private LinePattern(String value, String name, String iconName) {
        this.value = value;
        this.name = name;
        this.iconName = iconName;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public ImageDescriptor getIcon() {
        return MindMapUI.getImages().get(iconName);
    }

    public static String[] getValues() {
        LinePattern[] instances = values();
        String[] values = new String[instances.length];
        for (int i = 0; i < instances.length; i++) {
            values[i] = instances[i].getValue();
        }
        return values;
    }

    public static LinePattern findByValue(String value) {
        if (value == null)
            return null;
        for (LinePattern linePattern : values()) {
            if (linePattern.getValue().equals(value))
                return linePattern;
        }
        return null;
    }

}