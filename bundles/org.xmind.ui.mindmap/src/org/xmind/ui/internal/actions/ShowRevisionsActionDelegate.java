package org.xmind.ui.internal.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.mindmap.MindMapUI;

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
                editor.getSite().getPage().showView(MindMapUI.VIEW_REVISIONS);
            }
        });

    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }

}
