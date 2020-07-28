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

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

@Deprecated
public class NewWorkbookAction extends Action implements IWorkbenchAction {

    private IWorkbenchWindow window;

    public NewWorkbookAction(IWorkbenchWindow window) {
        super(MindMapMessages.NewWorkbook_text);
        if (window == null)
            throw new IllegalArgumentException();

        this.window = window;
        setId("org.xmind.ui.newWorkbook"); //$NON-NLS-1$
        setImageDescriptor(MindMapUI.getImages().get(IMindMapImages.NEW, true));
        setDisabledImageDescriptor(
                MindMapUI.getImages().get(IMindMapImages.NEW, false));
        setToolTipText(MindMapMessages.NewWorkbook_toolTip);
        setActionDefinitionId("org.xmind.ui.command.newWorkbook"); //$NON-NLS-1$
    }

    public void run() {
        if (window == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                window.getActivePage().openEditor(
                        MME.createNonExistingEditorInput(),
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    public void dispose() {
        window = null;
    }

}