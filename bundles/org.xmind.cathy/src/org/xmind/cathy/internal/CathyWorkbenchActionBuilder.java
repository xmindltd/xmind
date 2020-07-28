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
package org.xmind.cathy.internal;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Make and contribute basic actions to menus and toolbars.
 * 
 * <p>
 * Main Menu Bar:
 * <ul>
 * <li>File (file)
 * <ul>
 * <li>(fileStart)</li>
 * <li>(new.ext)</li>
 * <li>---- (open.group)</li>
 * <li>(open.ext)</li>
 * <li>---- (close.group)</li>
 * <li>(close.ext)</li>
 * <li>---- (save.group)</li>
 * <li>---- (save.ext)</li>
 * <li>---- (print.group)</li>
 * <li>(print.ext)</li>
 * <li>---- (import.group)</li>
 * <li>(import.ext)</li>
 * <li>---- (share.group)</li>
 * <li>---- (additions)</li>
 * <li>(fileEnd)</li>
 * </ul>
 * </li>
 * <li>Edit (edit)
 * <ul>
 * <li>(editStart)</li>
 * <li>(undo.ext)</li>
 * <li>---- (cut.group)</li>
 * <li>(cut.ext)</li>
 * <li>---- (delete.group)</li>
 * <li>---- (select.group)</li>
 * <li>---- (additions)</li>
 * <li>---- (find.group)</li>
 * <li>---- (find.ext)</li>
 * <li>(editEnd)</li>
 * </ul>
 * </li>
 * <li>(additions)</li>
 * <li>Help (help)
 * <ul>
 * <li>---- (group.intro)</li>
 * <li>(group.intro.ext)</li>
 * <li>---- (group.main)</li>
 * <li>(group.assist)</li>
 * <li>---- (helpStart)</li>
 * <li>(group.main.ext)</li>
 * <li>---- (group.tutorials)</li>
 * <li>---- (group.tools)</li>
 * <li>---- (group.updates)</li>
 * <li>---- (group.xmindnet)</li>
 * <li>(helpEnd)</li>
 * <li>---- (additions)</li>
 * <li>---- (group.about)</li>
 * <li>(about.ext)</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Main Tool Bar and other trim bars are defined in 'Application.e4xmi'.
 * </p>
 * 
 * <p>
 * NOTE: Up to now (4.5.0), we can't use Application.e4xmi to establish our main
 * menu model because workbench will delete main menu models from each window
 * model when persisting the workbench model. So we have to keep this class just
 * to fill in the main menu with our group markers each time the workbench
 * starts up.
 * </p>
 * 
 * @author Frank Shaka
 */
public class CathyWorkbenchActionBuilder extends ActionBarAdvisor {

    public CathyWorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
    }

    /**
     * Fill basic actions in the main menu.
     */
    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(createHelpMenu());
    }

    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.File_menu_text,
                IWorkbenchActionConstants.M_FILE);

        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator("open.group")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator("close.group")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.SAVE_GROUP));
        menu.add(new Separator(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator("print.group")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator("import.group")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator("share.group")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

        return menu;
    }

    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.Edit_menu_text,
                IWorkbenchActionConstants.M_EDIT);

        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator("cut.group")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator("delete.group")); //$NON-NLS-1$
        menu.add(new Separator("select.group")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator("find.group")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));

        return menu;
    }

    private MenuManager createHelpMenu() {
        MenuManager menu = new MenuManager(WorkbenchMessages.Help_menu_text,
                IWorkbenchActionConstants.M_HELP);

        menu.add(new Separator("group.intro")); //$NON-NLS-1$
        menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
        menu.add(new Separator("group.main")); //$NON-NLS-1$
        menu.add(new GroupMarker("group.assist")); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.HELP_START));
        menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
        menu.add(new Separator("group.tutorials")); //$NON-NLS-1$
        menu.add(new Separator("group.tools")); //$NON-NLS-1$
        menu.add(new Separator("group.updates")); //$NON-NLS-1$
        menu.add(new Separator("group.xmindnet")); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator("group.about")); //$NON-NLS-1$
        menu.add(new GroupMarker("about.ext")); //$NON-NLS-1$

        return menu;
    }

}