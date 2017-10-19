package org.xmind.ui.internal.spelling;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenSpellingCheckDialogHandler extends AbstractHandler {
    private boolean dialogOpened = false;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return null;

        SpellingCheckDialog dialog = new SpellingCheckDialog(shell) {

            public int open() {
                dialogOpened = true;
                return super.open();
            }

            public boolean close() {
                dialogOpened = false;
                return super.close();
            }
        };

        if (!dialogOpened)
            dialog.open();

        return null;
    }

}
