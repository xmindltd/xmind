package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.util.PrefUtils;

public class ShowAllPreferencesHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final String preferencePageId = event
                .getParameter(IWorkbenchCommandConstants.WINDOW_PREFERENCES_PARM_PAGEID);
        final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
                .getActiveWorkbenchWindow(event);

        final Shell shell;
        if (activeWorkbenchWindow == null) {
            shell = null;
        } else {
            shell = activeWorkbenchWindow.getShell();
        }

        PrefUtils.openPrefDialog(shell, preferencePageId);

        return null;
    }

}
