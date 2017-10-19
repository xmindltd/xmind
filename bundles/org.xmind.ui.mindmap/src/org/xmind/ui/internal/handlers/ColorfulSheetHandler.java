package org.xmind.ui.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.commands.ModifySheetTabColorCommand;

public class ColorfulSheetHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        changeTabColor(event);
        return null;
    }

    private void changeTabColor(ExecutionEvent event) {
        IGraphicalEditorPage page = getActivePage(event);
        if (page == null) {
            return;
        }

        ISheet sheet = page.getAdapter(ISheet.class);
        String rgb = (String) event
                .getParameter(MindMapCommandConstants.COLORFUL_SHEET_PARAM_RGB);

        String oldRgb = getRgb(sheet);
        if (((oldRgb == null || oldRgb.equals("")) //$NON-NLS-1$
                && (rgb == null || rgb.equals(""))) //$NON-NLS-1$
                || (oldRgb != null && oldRgb.equals(rgb))) {
            return;
        }

        ModifySheetTabColorCommand command = new ModifySheetTabColorCommand(
                sheet, rgb);

        ICommandStack cs = page.getEditDomain().getCommandStack();
        if (cs != null)
            cs.execute(command);
    }

    private IGraphicalEditorPage getActivePage(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof IGraphicalEditor) {
            return ((IGraphicalEditor) editor).getActivePageInstance();
        }

        return null;
    }

    private String getRgb(ISheet sheet) {
        ISettingEntry entry = findEntry(sheet);
        return entry == null ? null
                : entry.getAttribute(ISheetSettings.ATTR_RGB);
    }

    private ISettingEntry findEntry(ISheet sheet) {
        List<ISettingEntry> entries = sheet.getSettings()
                .getEntries(ISheetSettings.TAB_COLOR);
        return entries.size() == 0 ? null : entries.get(0);
    }

}
