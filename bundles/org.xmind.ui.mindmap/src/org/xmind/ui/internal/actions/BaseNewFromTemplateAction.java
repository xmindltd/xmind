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

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public abstract class BaseNewFromTemplateAction extends Action implements
        IWorkbenchAction {

    private IWorkbenchWindow window;

    private final IEditorPart[] editorPart = new IEditorPart[1];

    protected BaseNewFromTemplateAction(IWorkbenchWindow window) {
        if (window == null)
            throw new IllegalArgumentException();
        this.window = window;
    }

    public void run() {
        if (window == null)
            return;

        final IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;

        final InputStream templateStream;
        try {
            templateStream = getTemplateStream(window.getShell());
        } catch (Exception e) {
            notifyTemplateMissing(window.getShell());
            return;
        }

        if (templateStream == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IEditorInput input = MME
                        .createTemplatedEditorInput(templateStream);
                editorPart[0] = page.openEditor(input,
                        MindMapUI.MINDMAP_EDITOR_ID);
            }
        });
    }

    public IEditorPart getEditorPart() {
        return editorPart[0];
    }

    /**
     * @param shell
     */
    protected void notifyTemplateMissing(Shell shell) {
        MessageDialog.openError(shell, DialogMessages.CommonDialogTitle, NLS
                .bind(DialogMessages.TemplateMissing_message, getText()));
    }

    protected abstract InputStream getTemplateStream(Shell shell)
            throws Exception;

    protected IWorkbook createWorkbookFromTemplate(InputStream is)
            throws Exception {
        String tempLocation = Core.getWorkspace().getTempFile(
                Core.getIdFactory().createId() + MindMapUI.FILE_EXT_XMIND);
        FileUtils.ensureDirectory(new File(tempLocation));
        return Core.getWorkbookBuilder().loadFromStream(is, tempLocation);
    }

    public void dispose() {
        window = null;
    }

}