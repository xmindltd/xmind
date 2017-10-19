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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.Core;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.dialogs.HyperlinkDialog;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class ModifyHyperlinkAction extends PageAction implements
        ISelectionAction {

    public ModifyHyperlinkAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.MODIFY_HYPERLINK.getId(), page);
    }

    @Override
    public void run() {
        if (isDisposed())
            return;

        Shell parentShell = getEditor().getSite().getShell();
        ISelection selection = getPage().getSelectionProvider().getSelection();
        if (selection instanceof IStructuredSelection) {
            HyperlinkDialog dialog = new HyperlinkDialog(parentShell,
                    getEditor(), (IStructuredSelection) selection);
            int retCode = dialog.open();
            if (retCode == HyperlinkDialog.OK) {
                modifyHyperlink(dialog.getValue());
            } else if (retCode == HyperlinkDialog.REMOVE) {
                modifyHyperlink(null);
            }
        }
    }

    private void modifyHyperlink(String newHyperlink) {
        sendRequest(new Request(MindMapUI.REQ_MODIFY_HYPERLINK).setParameter(
                GEF.PARAM_TEXT, newHyperlink));
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isPropertyModifiable(selection,
                Core.TopicHyperlink, getViewer()));
    }

}