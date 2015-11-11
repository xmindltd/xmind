package org.xmind.ui.internal.spelling;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

@Deprecated
public class CheckSpellingAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
        window = null;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void run(IAction action) {
        if (window == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                IViewPart view = window.getActivePage()
                        .showView(SpellingPlugin.SPELLING_CHECK_VIEW_ID);
                if (view instanceof SpellingCheckView) {
                    ((SpellingCheckView) view).scanWorkbook();
                }
            }
        });

    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
