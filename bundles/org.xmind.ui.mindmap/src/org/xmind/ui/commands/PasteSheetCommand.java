package org.xmind.ui.commands;

import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.gef.command.Command;

public class PasteSheetCommand extends Command {

    private IWorkbook workbook;

    private ISheet copiedSheet;

    public PasteSheetCommand(IWorkbook workbook, ISheet copiedSheet) {
        this.workbook = workbook;
        this.copiedSheet = copiedSheet;
    }

    @Override
    public void redo() {
        workbook.addSheet(copiedSheet);
        super.redo();
    }

    @Override
    public void undo() {
        workbook.removeSheet(copiedSheet);
        super.undo();
    }
}
