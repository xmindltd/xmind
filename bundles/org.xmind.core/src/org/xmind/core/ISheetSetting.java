package org.xmind.core;

public interface ISheetSetting {

    boolean isInfoItemVisible(String type, String key, String defaultMode);

    void setInfoItemVisible(String type, String key, String defaultMode,
            boolean visible);

    String getInfoItemMode(String type, String key);

    void setInfoItemMode(String type, String key, String value);

}
