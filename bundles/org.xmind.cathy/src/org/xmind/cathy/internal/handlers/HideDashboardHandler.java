package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class HideDashboardHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow wbWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        if (wbWindow != null) {
            MWindow window = wbWindow.getService(MWindow.class);
            if (window != null) {
                new org.xmind.ui.internal.e4handlers.HideDashboardHandler()
                        .execute(window);
            }
        }
        return null;
    }

}
