package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.spelling.SpellingCheckView;
import org.xmind.ui.internal.spelling.SpellingPlugin;

public class CheckSpellingForAllHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil
                .getActiveWorkbenchWindow(event);
        if (window != null && window.getActivePage() != null) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    IViewPart view = window.getActivePage()
                            .showView(SpellingPlugin.SPELLING_CHECK_VIEW_ID);
                    if (view instanceof SpellingCheckView) {
                        ((SpellingCheckView) view).scanAll();
                    }
                }
            });
        }
        return null;
    }

}
