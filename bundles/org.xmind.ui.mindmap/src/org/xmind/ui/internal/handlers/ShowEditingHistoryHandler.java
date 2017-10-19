package org.xmind.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IMeta;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyMetadataCommand;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.WorkbookRevisionDialog;
import org.xmind.ui.mindmap.IMindMapViewer;

public class ShowEditingHistoryHandler extends AbstractHandler
        implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return null;
        if (!(editor instanceof IGraphicalEditor))
            return null;

        IViewer viewer = MindMapUIPlugin.getAdapter(editor, IViewer.class);
        if (viewer == null || !(viewer instanceof IMindMapViewer))
            return null;

        Control control = viewer.getControl();
        if (control == null)
            return null;
        Shell shell = control.getShell();

        WorkbookRevisionDialog dialog = new WorkbookRevisionDialog(shell,
                (IGraphicalEditor) editor);
        IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                .getActivePageInstance();
        ISheet sheet = (ISheet) page.getInput();
        IWorkbook workbook = sheet.getOwnedWorkbook();
        IMeta meta = workbook.getMeta();
        if (IMeta.V_NO
                .equals(meta.getValue(IMeta.CONFIG_AUTO_REVISION_GENERATION))) {
            boolean isOk = MessageDialog.openConfirm(shell,
                    DialogMessages.EnableRevisionDialog_Title_text,
                    DialogMessages.EnableRevisionDialog_Confirm_message);
            if (isOk) {
                enableRevision((IGraphicalEditor) editor, sheet);
            }
        }
        dialog.open();
        return null;
    }

    private void enableRevision(IGraphicalEditor editor, ISheet sheet) {
        IWorkbook workbook = sheet.getOwnedWorkbook();
        Command command = new ModifyMetadataCommand(workbook,
                IMeta.CONFIG_AUTO_REVISION_GENERATION, IMeta.V_YES);
        command.setLabel(CommandMessages.Command_TurnOffAutoRevisionSaving);
        ICommandStack commandStack = ((IGraphicalEditor) editor)
                .getCommandStack();
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }

    }

}
