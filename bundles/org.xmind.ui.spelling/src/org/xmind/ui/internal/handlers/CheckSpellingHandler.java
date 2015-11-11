package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.IWordContextProvider;
import org.xmind.ui.internal.spelling.SpellingCheckView;
import org.xmind.ui.internal.spelling.SpellingPlugin;

public class CheckSpellingHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchPart editor = HandlerUtil.getActivePart(event);
        if (editor != null && (editor instanceof IWordContextProvider
                || editor.getAdapter(IWordContextProvider.class) != null)) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    IViewPart view = editor.getSite().getPage()
                            .showView(SpellingPlugin.SPELLING_CHECK_VIEW_ID);
                    if (view instanceof SpellingCheckView) {
                        ((SpellingCheckView) view).scanWorkbook();
                    }
                }
            });
        }
        return null;
    }

}
