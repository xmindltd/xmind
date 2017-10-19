package org.xmind.core;

import java.util.List;
import java.util.Set;

/**
 * @author Jason Wong
 */
public interface ISheetSettings extends IAdaptable, ISheetComponent {

    String SEP = "/"; //$NON-NLS-1$

    String INFO_ITEM = "info-items/info-item"; //$NON-NLS-1$
    String ATTR_TYPE = "type"; //$NON-NLS-1$
    String ATTR_MODE = "mode"; //$NON-NLS-1$
    String MODE_CARD = "card"; //$NON-NLS-1$
    String MODE_ICON = "icon"; //$NON-NLS-1$

    String TAB_COLOR = "tab-color"; //$NON-NLS-1$
    String ATTR_RGB = "rgb"; //$NON-NLS-1$

    /**
     * 
     * @param path
     *            "info-items/info-item"
     * @return
     */
    List<ISettingEntry> getEntries(String path);

    ISettingEntry createEntry(String path);

    void addEntry(ISettingEntry entry);

    void removeEntry(ISettingEntry entry);

    Set<String> getPaths();

}
