package org.xmind.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.gef.command.Command;

public class DuplicateSheetCommand extends Command {

    private ISheet sheet;
    private ISheet clonedSheet;

    public DuplicateSheetCommand(ISheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public void redo() {
        if (clonedSheet == null) {
            cloneSheet();
        }
        sheet.getOwnedWorkbook().addSheet(clonedSheet, sheet.getIndex() + 1);
        super.redo();
    }

    @Override
    public void undo() {
        clonedSheet.getOwnedWorkbook().removeSheet(clonedSheet);
        super.undo();
    }

    private void cloneSheet() {
        List<ISheet> sheetList = new ArrayList<ISheet>();
        sheetList.add(sheet);
        clonedSheet = (ISheet) sheet.getOwnedWorkbook().clone(sheetList)
                .getCloneds().iterator().next();
        clonedSheet.setTitleText(clonedSheet.getTitleText() + " copy"); //$NON-NLS-1$
    }
}
