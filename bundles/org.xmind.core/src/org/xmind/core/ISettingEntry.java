package org.xmind.core;

import java.util.Set;

public interface ISettingEntry extends IAdaptable, ISheetComponent {

    String getPath();

    /**
     * 
     * @param key
     * @return
     */
    String getAttribute(String key);

    void setAttribute(String key, String value);

    Set<String> getAttributeKeys();

}
