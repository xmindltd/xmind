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
package org.xmind.ui.internal.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;

public class DropDownInsertImageAction extends Action
        implements IWorkbenchAction, IPropertyChangeListener {

    private class InsertImageMenuCreator implements IMenuCreator {

        private Menu menu;

        public Menu getMenu(Control parent) {
            if (menu != null) {
                menu.dispose();
            }

            menu = new Menu(parent);
            fillMenu(menu);
            return menu;
        }

        public Menu getMenu(Menu parent) {
            if (menu != null) {
                menu.dispose();
            }
            menu = new Menu(parent);
            fillMenu(menu);
            return menu;
        }

        private void fillMenu(Menu menu) {
            for (IWorkbenchAction action : imageActionExtensions) {
                ActionContributionItem item = new ActionContributionItem(
                        action);
                item.fill(menu, -1);
            }
        }

        public void dispose() {
            if (menu != null) {
                menu.dispose();
                menu = null;
            }
        }

    }

    private IAction sourceAction;

    private List<IWorkbenchAction> imageActionExtensions;

    public DropDownInsertImageAction(IAction sourceAction,
            List<IWorkbenchAction> imageActionExtensions) {
        super(MindMapMessages.InsertImage_text, AS_DROP_DOWN_MENU);
        setId("org.xmind.ui.insertImageDropDown"); //$NON-NLS-1$
        this.sourceAction = sourceAction;
        this.imageActionExtensions = imageActionExtensions;
        setMenuCreator(new InsertImageMenuCreator());
        if (sourceAction != null)
            sourceAction.addPropertyChangeListener(this);
        setEnabled(sourceAction != null && sourceAction.isEnabled());
    }

    public void run() {
        if (sourceAction != null) {
            sourceAction.run();
        }
    }

    public void dispose() {
        if (sourceAction != null) {
            sourceAction.removePropertyChangeListener(this);
            sourceAction = null;
        }
        if (imageActionExtensions != null) {
            imageActionExtensions.clear();
            imageActionExtensions = null;
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (ENABLED.equals(event.getProperty())) {
            this.setEnabled(sourceAction != null && sourceAction.isEnabled());
        }
    }

}
