package org.xmind.ui.internal.actions;

import org.xmind.core.ISheet;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DuplicateSheetCommand;

public class DuplicateSheetAction extends EditorAction {

    public DuplicateSheetAction(IGraphicalEditor editor) {
        super(MindMapActionFactory.DUPLICATE_SHEET.getId(), editor);
    }

    @Override
    public void run() {
        if (isDisposed())
            return;

        IGraphicalEditorPage page = getActivePage();
        if (page != null) {
            ISheet sheet = (ISheet) page.getAdapter(ISheet.class);

            if (sheet == null) {
                Object input = page.getInput();
                if (input instanceof ISheet) {
                    sheet = (ISheet) input;
                }
            }

            if (sheet != null) {
                saveAndRunDuplicateSheetCommand(sheet);
            }
        }
    }

    protected void saveAndRunDuplicateSheetCommand(ISheet sheet) {
        DuplicateSheetCommand command = new DuplicateSheetCommand(sheet);
        command.setLabel(CommandMessages.Command_DuplicateSheet);
        saveAndRun(command);
    }
}
