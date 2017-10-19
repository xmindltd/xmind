package org.xmind.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.commands.DeleteSheetCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;

public class RevertToRevisionHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        revertToRevision(HandlerUtil.getCurrentSelection(event),
                HandlerUtil.getActiveEditor(event));
        return null;
    }

    private static void revertToRevision(ISelection selection,
            IEditorPart editor) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (!(obj instanceof IRevision))
            return;

        IRevision revision = (IRevision) obj;
        if (!IRevision.SHEET.equals(revision.getContentType()))
            return;

        IWorkbook workbook = revision.getOwnedWorkbook();
        final ISheet sourceSheet = (ISheet) workbook
                .findElement(revision.getResourceId(), null);

        final ISheet targetSheet = (ISheet) workbook
                .importElement(revision.getContent());
        if (targetSheet == null)
            return;

        // Force update modification info
        String title = targetSheet.getTitleText();
        targetSheet.setTitleText("#" + title); //$NON-NLS-1$
        targetSheet.setTitleText(title);

        final int sheetIndex = sourceSheet.getIndex();

        List<Command> commands = new ArrayList<Command>();
        ISheet placeholderSheet = workbook.createSheet();
        commands.add(new AddSheetCommand(placeholderSheet, workbook));
        commands.add(new DeleteSheetCommand(sourceSheet));
        commands.add(new AddSheetCommand(targetSheet, workbook, sheetIndex));
        commands.add(new DeleteSheetCommand(placeholderSheet, workbook));

        final Command command = new CompoundCommand(
                MindMapMessages.RevertToRevisionCommand_label, commands);
        final ICommandStack commandStack = editor == null ? null
                : MindMapUIPlugin.getAdapter(editor, ICommandStack.class);

        final IRevisionManager manager = revision.getOwnedManager();
        final IRevision latestRevision = manager.getLatestRevision();

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                if (latestRevision == null || sourceSheet
                        .getModifiedTime() > latestRevision.getTimestamp()) {
                    manager.addRevision(sourceSheet);
                }
                if (commandStack != null) {
                    commandStack.execute(command);
                } else {
                    command.execute();
                }
            }
        });

    }

}
