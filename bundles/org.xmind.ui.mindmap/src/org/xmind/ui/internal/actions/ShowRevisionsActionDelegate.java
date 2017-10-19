package org.xmind.ui.internal.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.internal.utils.CommandUtils;

@Deprecated
public class ShowRevisionsActionDelegate implements IEditorActionDelegate {

    private IEditorPart editor;

    public ShowRevisionsActionDelegate() {
    }

    public void run(IAction action) {
        if (editor == null)
            return;
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                CommandUtils.executeCommand(
                        "org.xmind.ui.command.editingHistory", //$NON-NLS-1$
                        editor.getSite().getWorkbenchWindow());
            }
        });

    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }

}
