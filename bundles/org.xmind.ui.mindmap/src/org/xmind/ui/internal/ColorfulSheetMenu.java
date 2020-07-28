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
package org.xmind.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.mindmap.MindMapUI;

public class ColorfulSheetMenu extends CompoundContributionItem
        implements IWorkbenchContribution {

    private static final String SHEET_ICON_PATH = "icons/sheet/"; //$NON-NLS-1$

    private static final List<ColorEntry> TAB_COLORS_WINDOWS = Arrays.asList( //
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_blue,
                    "#2188e2", "windows_blue.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_purple,
                    "#a497fd", "windows_purple.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_red,
                    "#f58868", "windows_red.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_yellow,
                    "#fabd65", "windows_yellow.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_green,
                    "#80df98", "windows_green.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_brown,
                    "#9e8273", "windows_brown.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_gray,
                    "#6b7288", "windows_gray.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_windows_none, "", //$NON-NLS-1$
                    "windows_none.png")); //$NON-NLS-1$

    private static final List<ColorEntry> TAB_COLORS_OTHERS = Arrays.asList( //
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_red, "#ff625c", //$NON-NLS-1$
                    "others_red.png"), //$NON-NLS-1$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_orange,
                    "#f9a646", "others_orange.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_yellow,
                    "#f5cf4a", "others_yellow.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_green,
                    "#6dcc50", "others_green.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_blue,
                    "#4bb8f3", "others_blue.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_purple,
                    "#d089e1", "others_purple.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_gray,
                    "#a4a4a8", "others_gray.png"), //$NON-NLS-1$//$NON-NLS-2$
            new ColorEntry(MindMapMessages.ColorfulSheetMenu_mac_none, "", //$NON-NLS-1$
                    "others_none.png")); //$NON-NLS-1$

    private static class ColorEntry {

        private String name;

        private String rgb;

        private String iconPath;

        public ColorEntry(String name, String rgb, String iconPath) {
            this.name = name;
            this.rgb = rgb;
            this.iconPath = iconPath;
        }

        public String getName() {
            return name;
        }

        public String getRgb() {
            return rgb;
        }

        public String getIconPath() {
            return iconPath;
        }
    }

    private IServiceLocator serviceLocator;

    public ColorfulSheetMenu() {
    }

    public void initialize(IServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        List<IContributionItem> items = new ArrayList<IContributionItem>();

        if (serviceLocator != null) {
            fillItems(items, getSheetTabColors());
        }

        return items.toArray(new IContributionItem[items.size()]);
    }

    private void fillItems(List<IContributionItem> items,
            List<ColorEntry> colors) {
        for (ColorEntry color : colors) {
            items.add(makeColorCommandItem(color));
        }
    }

    private IContributionItem makeColorCommandItem(final ColorEntry color) {
        String id = "colorfulSheet." + color.getName(); //$NON-NLS-1$
        CommandContributionItemParameter parameter = new CommandContributionItemParameter(
                serviceLocator, id, MindMapCommandConstants.COLORFUL_SHEET,
                CommandContributionItem.STYLE_PUSH);

        parameter.label = color.getName();
        parameter.icon = MindMapUI.getImages().get(color.getIconPath(),
                SHEET_ICON_PATH);

        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put(MindMapCommandConstants.COLORFUL_SHEET_PARAM_RGB,
                color.getRgb());
        parameter.parameters = params;

        return new CommandContributionItem(parameter);
    }

    private static List<ColorEntry> getSheetTabColors() {
        return Util.isWindows() ? TAB_COLORS_WINDOWS : TAB_COLORS_OTHERS;
    }

}