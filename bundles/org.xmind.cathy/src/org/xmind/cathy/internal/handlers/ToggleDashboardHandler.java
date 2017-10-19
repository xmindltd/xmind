package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleDashboardHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow wbw = HandlerUtil
                .getActiveWorkbenchWindowChecked(event);
        new org.xmind.ui.internal.e4handlers.ToggleDashboardHandler()
                .execute(wbw.getService(MWindow.class));
        return null;
    }

}
