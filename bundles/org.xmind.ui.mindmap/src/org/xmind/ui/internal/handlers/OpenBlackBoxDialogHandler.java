package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.dialogs.BlackBoxDialog;

public class OpenBlackBoxDialogHandler extends AbstractHandler {

    public static final String BLACK_BOX_DIALOG_DATA_KEY = "blackBoxDialog"; //$NON-NLS-1$

    private boolean dialogOpened = false;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        Shell shell = window.getShell();
        if (shell == null || shell.isDisposed())
            return null;

        BlackBoxDialog dialog = new BlackBoxDialog(shell) {
            public int open() {
                dialogOpened = true;
                int returnCode = super.open();

                getShell().setData(BLACK_BOX_DIALOG_DATA_KEY, this);
                return returnCode;
            }

            public boolean close() {
                dialogOpened = false;
                getShell().setData(BLACK_BOX_DIALOG_DATA_KEY, null);

                return super.close();
            }
        };

        if (!dialogOpened)
            dialog.open();

        return null;
    }
}
