package org.xmind.ui.internal.actions;

import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.PasteSheetCommand;

public class PasteSheetAction extends EditorAction {

    public PasteSheetAction(IGraphicalEditor editor) {
        super(MindMapActionFactory.PASTE_SHEET.getId(), editor);
    }

    @Override
    public void run() {
        if (isDisposed())
            return;

        IGraphicalEditorPage page = getActivePage();
        if (page != null) {
            IWorkbook workbook = ((ISheet) page.getAdapter(ISheet.class))
                    .getOwnedWorkbook();
            ISheet copiedSheet = CopiedSheetStorageSupport.getInstance()
                    .getCopiedSheet(workbook);
            if (workbook != null) {
                saveAndRunPasteSheetCommand(workbook, copiedSheet);
            }
        }
    }

    protected void saveAndRunPasteSheetCommand(IWorkbook workbook,
            ISheet copiedSheet) {
        PasteSheetCommand command = new PasteSheetCommand(workbook, copiedSheet);
        command.setLabel(CommandMessages.Command_PasteSheet);
        saveAndRun(command);
    }
}
