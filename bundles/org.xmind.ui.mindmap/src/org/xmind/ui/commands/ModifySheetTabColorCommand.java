package org.xmind.ui.commands;

import java.util.List;

import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.MindMapMessages;

public class ModifySheetTabColorCommand extends ModifyCommand {

    public ModifySheetTabColorCommand(ISheet source, String rgb) {
        super(source, rgb);
        setLabel(MindMapMessages.ModifySheetTabColorCommand_label);
    }

    @Override
    protected Object getValue(Object source) {
        if (source instanceof ISheet) {
            return getRgb((ISheet) source);
        }
        return null;
    }

    @Override
    protected void setValue(Object source, Object value) {
        if (source instanceof ISheet && value instanceof String) {
            String oldValue = getRgb((ISheet) source);
            if (value.equals(oldValue)) {
                return;
            }
            setRgb((ISheet) source, (String) value);
        }
    }

    private String getRgb(ISheet sheet) {
        ISettingEntry entry = findEntry(sheet);
        return entry == null ? null
                : entry.getAttribute(ISheetSettings.ATTR_RGB);
    }

    private void setRgb(ISheet sheet, String rgb) {
        ISettingEntry entry = findEntry(sheet);
        if (entry == null) {
            entry = sheet.getSettings().createEntry(ISheetSettings.TAB_COLOR);
            sheet.getSettings().addEntry(entry);
        }
        entry.setAttribute(ISheetSettings.ATTR_RGB, rgb);
    }

    private ISettingEntry findEntry(ISheet sheet) {
        List<ISettingEntry> entries = sheet.getSettings()
                .getEntries(ISheetSettings.TAB_COLOR);
        return entries.size() == 0 ? null : entries.get(0);
    }

}
