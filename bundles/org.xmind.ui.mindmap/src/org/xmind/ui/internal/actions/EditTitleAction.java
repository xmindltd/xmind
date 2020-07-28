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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.xmind.core.ISheet;
import org.xmind.core.ITitled;
import org.xmind.gef.GEF;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.editor.MindMapPageTitleEditor;

public class EditTitleAction extends RequestAction implements ISelectionAction {

    private boolean isSheet = false;

    public EditTitleAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.EDIT_TITLE.getId(), page, GEF.REQ_EDIT);
    }

    public void run() {
        if (isSheet) {
            MindMapPageTitleEditor titleEditor = (MindMapPageTitleEditor) getEditor()
                    .getAdapter(MindMapPageTitleEditor.class);
            if (titleEditor != null) {
                titleEditor.startEditing(getPage().getIndex());
                return;
            }
        }
        super.run();
    }

    public void setSelection(ISelection selection) {
        setEnabled(isTitledSelected(selection));
    }

    private boolean isTitledSelected(ISelection selection) {
        isSheet = false;
        if (selection instanceof IStructuredSelection) {
            Object[] elements = ((IStructuredSelection) selection).toArray();
            for (Object o : elements) {
                if (o instanceof ISheet)
                    isSheet = true;
                if (o instanceof ITitled)
                    return true;
            }
        }
        return false;
    }
}
