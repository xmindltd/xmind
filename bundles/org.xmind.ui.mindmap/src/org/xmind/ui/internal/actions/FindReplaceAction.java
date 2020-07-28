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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.findreplace.FindReplaceDialog;

public class FindReplaceAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public FindReplaceAction(IWorkbenchWindow window) {
        super();
        this.window = window;
        setId(ActionFactory.FIND.getId());
        setActionDefinitionId(ActionFactory.FIND.getCommandId());
    }

    public void run() {
        if (window == null)
            return;

        FindReplaceDialog dialog = FindReplaceDialog.getInstance(window);
        if (dialog != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof ITextSelection) {
                dialog.setInitialFindText(
                        ((ITextSelection) selection).getText());
            }
            dialog.open();
        }
    }

    public void dispose() {
        window = null;
    }

}