package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;

public class ShowResourceManagerDialogHanlder extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window != null) {
            E4Utils.showPart(IModelConstants.COMMAND_SHOW_DIALOG_PART, window,
                    IModelConstants.PART_ID_RESOURCE_MANAGER, null, null);
        }
        return null;
    }

}
