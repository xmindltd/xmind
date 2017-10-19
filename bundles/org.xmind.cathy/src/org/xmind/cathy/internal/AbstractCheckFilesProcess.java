package org.xmind.cathy.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.mindmap.MindMapUI;

public class AbstractCheckFilesProcess {

    private final IWorkbench workbench;

    private List<IEditorInput> editorsToOpen;

    public AbstractCheckFilesProcess(IWorkbench workbench) {
        this.workbench = workbench;
    }

    public IWorkbench getWorkbench() {
        return workbench;
    }

    protected void addEditorToOpen(IEditorInput input) {
        if (editorsToOpen == null)
            editorsToOpen = new ArrayList<IEditorInput>();
        editorsToOpen.add(input);
    }

    protected void openEditors(boolean activate) {
        if (editorsToOpen != null && !editorsToOpen.isEmpty()) {
            openEditors(editorsToOpen, activate);
        }
    }

    protected void openEditors(List<IEditorInput> editorInputs,
            boolean activate) {
        for (final IEditorInput input : editorInputs) {
            IEditorPart editor = openEditor(input, activate);
            if (editor != null)
                activate = false;
        }
    }

    protected IEditorPart openEditor(final IEditorInput input,
            final boolean activate) {
        if (input == null)
            return null;

        Display display = workbench.getDisplay();
        if (display == null)
            return null;

        final IEditorPart[] result = new IEditorPart[1];
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = getPrimaryWindow();
                if (window == null)
                    return;
                final IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    SafeRunner.run(new SafeRunnable(NLS.bind(
                            WorkbenchMessages.CheckOpenFilesJob_FailsToOpen_message,
                            input.getName())) {
                        public void run() throws Exception {
                            result[0] = page.openEditor(input,
                                    MindMapUI.MINDMAP_EDITOR_ID, activate);
                        }
                    });
                }
            }

        });
        return result[0];
    }

    private IWorkbenchWindow getPrimaryWindow() {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            if (windows != null && windows.length > 0) {
                window = windows[0];
            }
        }
        return window;
    }

}
