/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.util.Logger;

public class ShowPropertiesAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public ShowPropertiesAction(IWorkbenchWindow window) {
        setId(ActionFactory.PROPERTIES.getId());
        setActionDefinitionId(ActionFactory.PROPERTIES.getCommandId());
        this.window = window;
    }

    public void run() {
        if (window == null)
            return;

        try {
            window.getActivePage()
                    .showView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
        } catch (PartInitException e) {
            Logger.log(e);
        }
    }

    public void dispose() {
        window = null;
    }

}