package org.xmind.ui.commands;

import static org.xmind.core.ISheetSettings.INFO_ITEM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetSettings;
import org.xmind.gef.command.ModifyCommand;
import org.xmind.ui.internal.InfoItemContributorManager;
import org.xmind.ui.mindmap.IInfoItemContributor;

/**
 * @author Jason Wong
 */
public class ModifyInfoItemVisibilityCommand extends ModifyCommand {

    private Map<String, String> defaultModes;

    private String type;

    public ModifyInfoItemVisibilityCommand(ISheet sheet, boolean visible,
            String type) {
        super(sheet, visible);
        this.type = type;
        init();
    }

    private void init() {
        defaultModes = new HashMap<String, String>();

        List<IInfoItemContributor> cs = InfoItemContributorManager.getInstance()
                .getBothContributors();
        for (IInfoItemContributor c : cs)
            defaultModes.put(c.getId(), c.getDefaultMode());
    }

    @Override
    protected Object getValue(Object source) {
        if (source instanceof ISheet)
            return Boolean.valueOf(isVisible((ISheet) source, type));
        return null;
    }

    @Override
    protected void setValue(Object source, Object value) {
        if (source instanceof ISheet && value instanceof Boolean)
            setVisible((ISheet) source, (Boolean) value, type);
    }

    private void setVisible(ISheet sheet, boolean visible, String type) {
        if (type == null || "".equals(type)) //$NON-NLS-1$
            return;

        ISettingEntry entry = findEntry(sheet, type);
        if (visible != isVisible(entry, type)) {
            String modeValue = visible ? ISheetSettings.MODE_CARD
                    : ISheetSettings.MODE_ICON;
            if (entry == null) {
                entry = sheet.getSettings().createEntry(INFO_ITEM);
                entry.setAttribute(ISheetSettings.ATTR_TYPE, type);
                sheet.getSettings().addEntry(entry);
            }
            entry.setAttribute(ISheetSettings.ATTR_MODE, modeValue);
        }
    }

    private boolean isVisible(ISheet sheet, String type) {
        ISettingEntry entry = findEntry(sheet, type);
        return isVisible(entry, type);
    }

    private boolean isVisible(ISettingEntry entry, String type) {
        if (entry != null)
            return ISheetSettings.MODE_CARD
                    .equals(entry.getAttribute(ISheetSettings.ATTR_MODE));
        return ISheetSettings.MODE_CARD.equals(defaultModes.get(type));
    }

    private ISettingEntry findEntry(ISheet sheet, String type) {
        List<ISettingEntry> entries = sheet.getSettings().getEntries(INFO_ITEM);
        for (ISettingEntry entry : entries) {
            String t = entry.getAttribute(ISheetSettings.ATTR_TYPE);
            if (type.equals(t))
                return entry;
        }
        return null;
    }

}
