package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.cathy.internal.ICathyConstants;

public class UnpinRecentFileHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell != null) {
            Object data = shell.getData(ICathyConstants.HELPER_RECENTFILE_PIN);
            if (data instanceof Runnable) {
                ((Runnable) data).run();
            }
        }
        return null;
    }

}
