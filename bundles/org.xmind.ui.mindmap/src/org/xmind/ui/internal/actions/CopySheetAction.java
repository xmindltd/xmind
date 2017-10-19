package org.xmind.ui.internal.actions;

import org.xmind.core.ISheet;
import org.xmind.gef.ui.actions.EditorAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;

public class CopySheetAction extends EditorAction {

    public CopySheetAction(IGraphicalEditor editor) {
        super(MindMapActionFactory.COPY_SHEET.getId(), editor);
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
                CopiedSheetStorageSupport.getInstance().setCopiedSheet(sheet);
            }
        }
    }
}
