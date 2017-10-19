package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.spelling.SpellingCheckDialog;

public class CheckSpellingForAllHandler extends AbstractHandler {
    private boolean dialogOpened = false;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil
                .getActiveWorkbenchWindow(event);
        if (window != null && window.getActivePage() != null) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    SpellingCheckDialog dialog = new SpellingCheckDialog(
                            window.getShell()) {
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
                }
            });
        }
        return null;
    }

}
