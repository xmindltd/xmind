package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.ShareOption;
import org.xmind.ui.internal.dialogs.ShareDialog;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class ShowShareOptionsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil
                .getActiveWorkbenchWindowChecked(event);
        share(window);
        return null;
    }

    private void share(IWorkbenchWindow window) {
        ShareDialog optionDialog = new ShareDialog(window.getShell(),
                MindMapUIPlugin.getDefault().getShareOptionRegistry());
        optionDialog.open();
        final ShareOption option = optionDialog.getSelectedOption();
        if (option == null)
            return;

        final IHandlerService handlerService = window
                .getService(IHandlerService.class);
        Assert.isNotNull(handlerService);

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                handlerService.executeCommand(option.getCommandId(), null);
            }
        });
    }

}
