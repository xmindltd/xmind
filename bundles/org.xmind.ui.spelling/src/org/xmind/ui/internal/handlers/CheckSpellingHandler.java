package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.internal.spelling.SpellingCheckDialog;

public class CheckSpellingHandler extends AbstractHandler {
    private boolean dialogOpened = false;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchPart editor = HandlerUtil.getActivePart(event);
        if (editor != null && (editor instanceof IWordContextProvider
                || editor.getAdapter(IWordContextProvider.class) != null)) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    SpellingCheckDialog dialog = new SpellingCheckDialog(
                            editor.getSite().getShell()) {
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
