package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.ReduceFileSizeDialog;

public class ReduceFileSizeHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        if (part == null || !(part instanceof IEditorPart))
            return null;

        reduceFileSize((IEditorPart) part);
        return null;
    }

    private void reduceFileSize(final IEditorPart editor) {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_MODIFY,
                UserDataConstants.REDUCE_FILE_SIZE);
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                ReduceFileSizeDialog dialog = new ReduceFileSizeDialog(editor);
                dialog.open();
            }
        });
    }

}
