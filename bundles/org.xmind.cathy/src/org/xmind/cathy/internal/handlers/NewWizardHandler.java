package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.cathy.internal.ICathyConstants;

public class NewWizardHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow wbWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        if (wbWindow != null) {
            MWindow window = wbWindow.getService(MWindow.class);
            if (window != null) {
                org.xmind.ui.internal.e4handlers.ShowDashboardPageHandler
                        .showDashboardPage(window,
                                ICathyConstants.DASHBOARD_PAGE_NEW);
            }
        }

        return null;
    }

}
