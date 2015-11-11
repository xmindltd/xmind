package org.xmind.ui.internal.e4handlers;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.MindMapUI;

public class NewDefaultWorkbookHandler {

    public void execute(final IWorkbenchWindow window) {
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

}
