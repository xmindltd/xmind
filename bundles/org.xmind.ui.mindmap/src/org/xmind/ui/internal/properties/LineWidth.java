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

public enum LineWidth {

    None("0pt", PropertyMessages.LineWidth_None, IMindMapImages.LINE_NONE), //$NON-NLS-1$

    Thinnest("1pt", PropertyMessages.LineWidth_Thinnest, //$NON-NLS-1$
            IMindMapImages.LINE_THINNEST), // 
    Thin("2pt", PropertyMessages.LineWidth_Thin, //$NON-NLS-1$
            IMindMapImages.LINE_THIN), // 
    Medium("3pt", PropertyMessages.LineWidth_Medium, //$NON-NLS-1$
            IMindMapImages.LINE_MEDIUM), // 
    Fat("4pt", PropertyMessages.LineWdith_Fat, //$NON-NLS-1$
            IMindMapImages.LINE_FAT), // 
    Fattest("5pt", //$NON-NLS-1$
            PropertyMessages.LineWidth_Fattest, IMindMapImages.LINE_FATTEST);

    private String value;

    private String name;

    private String iconName;

    private LineWidth(String value, String name, String iconName) {
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
        if (iconName == null)
            return null;
        return MindMapUI.getImages().get(iconName);
    }

    public static String[] getValues() {
        LineWidth[] instances = values();
        String[] values = new String[instances.length];
        for (int i = 0; i < instances.length; i++) {
            values[i] = instances[i].getValue();
        }
        return values;
    }

    public static LineWidth findByValue(String value) {
        if (value == null)
            return null;
        for (LineWidth lineWidth : values()) {
            if (lineWidth.getValue().startsWith(value))
                return lineWidth;
        }
        return null;
    }
}
