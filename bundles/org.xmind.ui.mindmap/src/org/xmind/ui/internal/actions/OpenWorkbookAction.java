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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dialogs.OpenWorkbookDialog;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

@Deprecated
public class OpenWorkbookAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public OpenWorkbookAction(IWorkbenchWindow window) {
        super(MindMapMessages.OpenWorkbook_text);
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
        setId("org.xmind.ui.open"); //$NON-NLS-1$
        setImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, true));
        setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.OPEN, false));
        setToolTipText(MindMapMessages.OpenWorkbook_toolTip);
        setActionDefinitionId("org.xmind.ui.command.openWorkbook"); //$NON-NLS-1$
    }

    public void run() {
        if (window == null)
            return;

        new OpenWorkbookDialog(window).open();
    }

    public void dispose() {
        window = null;
    }

}