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
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.commands.ModifyThemeCommand;
import org.xmind.ui.mindmap.MindMapUI;

public class ModifyThemeCommandBuilder extends CommandBuilder {

    private static final String NULL = "$NULL$"; //$NON-NLS-1$

    private IStyle sourceTheme;

    private Map<IWorkbook, IStyle> appliedThemes = null;

    private List<IStyled> sources = new ArrayList<IStyled>();

    public ModifyThemeCommandBuilder(IViewer viewer,
            ICommandStack commandStack, IStyle theme) {
        super(viewer, commandStack);
        this.sourceTheme = theme;
    }

    public ModifyThemeCommandBuilder(IViewer viewer, CommandBuilder delegate,
            IStyle theme) {
        super(viewer, delegate);
        this.sourceTheme = theme;
    }

    public IStyle getTheme() {
        return sourceTheme;
    }

    public void removeStyle(IStyled source) {
        if (isSourceModified(source))
            return;

        modifyStyle(source, null);
        sources.add(source);
    }

    public List<IStyled> getModifiedSources() {
        return sources;
    }

    public boolean isSourceModified(IStyled source) {
        return sources.contains(source);
    }

    private void modifyStyle(IStyled source, String newStyleId) {
        if (NULL.equals(newStyleId))
            newStyleId = null;
        add(new ModifyStyleCommand(source, newStyleId), true);
    }

    public void modify(ISheet sheet) {
        IStyle appliedTheme = getAppliedTheme(sheet);
        if (appliedTheme == null) {
            add(new ModifyThemeCommand(sheet, null), true);
        } else {
            add(new ModifyThemeCommand(sheet, appliedTheme.getId()), true);
        }
//        ModifyStyleCommandBuilder modifyStyleBuilder = new ModifyStyleCommandBuilder(
//                getViewer(), this, createSheetStyleRequest(appliedTheme));
//        modifyStyleBuilder.modify(sheet);
    }

//    private Request createSheetStyleRequest(IStyle appliedTheme) {
//        Request request = new Request(MindMapUI.REQ_MODIFY_STYLE)
//                .setViewer(getViewer());
//        request.setParameter(MindMapUI.PARAM_STYLE_PREFIX
//                + Styles.MultiLineColors,
//                getMapStyleValue(appliedTheme, Styles.MultiLineColors));
//        request.setParameter(MindMapUI.PARAM_STYLE_PREFIX + Styles.LineTapered,
//                getMapStyleValue(appliedTheme, Styles.LineTapered));
//        return request;
//    }
//
//    private String getMapStyleValue(IStyle theme, String key) {
//        if (theme == null)
//            return null;
//        IStyle mapStyle = theme.getDefaultStyle(Styles.FAMILY_MAP);
//        if (mapStyle == null)
//            return null;
//        return mapStyle.getProperty(key);
//    }

    private IStyle getAppliedTheme(ISheet sheet) {
        if (sourceTheme == null
                || sourceTheme.isEmpty()
                || MindMapUI.getResourceManager().getBlankTheme()
                        .equals(sourceTheme))
            return null;

        IWorkbook workbook = sheet.getOwnedWorkbook();
        if (workbook == null)
            return sourceTheme;

        if (appliedThemes == null)
            appliedThemes = new HashMap<IWorkbook, IStyle>();
        if (!appliedThemes.containsKey(workbook)) {
            IStyle appliedTheme = createAppliedTheme(workbook);
            appliedThemes.put(workbook, appliedTheme);
        }
        return appliedThemes.get(workbook);
    }

    private IStyle createAppliedTheme(IWorkbook workbook) {
        IStyleSheet ss = workbook.getStyleSheet();
        return ss.importStyle(sourceTheme);
    }

}