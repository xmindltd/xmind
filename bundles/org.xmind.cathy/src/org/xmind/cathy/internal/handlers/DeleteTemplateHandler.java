package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.MindMapUI;

public class DeleteTemplateHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof ITemplate) {
                ITemplate template = (ITemplate) obj;
                Shell activeShell = HandlerUtil.getActiveShell(event);

                if (!MessageDialog.openConfirm(activeShell,
                        WorkbenchMessages.ConfirmDeleteTemplateDialog_title,
                        NLS.bind(
                                WorkbenchMessages.ConfirmDeleteTemplateDialog_message_withTemplateName,
                                template.getName()))) {
                    return null;
                }
                MindMapUI.getResourceManager().removeUserTemplate(template);
            }
        }

        return null;
    }

}
