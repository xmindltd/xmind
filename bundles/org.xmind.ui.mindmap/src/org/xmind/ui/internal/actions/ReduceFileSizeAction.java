package org.xmind.ui.internal.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.internal.dialogs.ReduceFileSizeDialog;

@Deprecated
public class ReduceFileSizeAction implements IEditorActionDelegate {

    private IEditorPart editor;

    public ReduceFileSizeAction() {
    }

    public void run(IAction action) {
        if (editor == null)
            return;
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                ReduceFileSizeDialog dialog = new ReduceFileSizeDialog(editor);
                dialog.open();
            }
        });
    }

    public void selectionChanged(IAction action, ISelection selection) {

    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }
}
