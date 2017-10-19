package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ShowCloudPageHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow wbWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        if (wbWindow != null) {
            MWindow window = wbWindow.getService(MWindow.class);
            if (window != null) {
                String pageId = "org.xmind.ui.part.dashboard.cloud"; //$NON-NLS-1$
                org.xmind.ui.internal.e4handlers.ShowDashboardPageHandler
                        .showDashboardPage(window, pageId);
            }
        }
        return null;
    }

}
