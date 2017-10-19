package org.xmind.core.internal;

import org.xmind.core.ISettingEntry;

/**
 * @author Jason Wong
 * @since 3.6.50
 */
public abstract class SettingEntry implements ISettingEntry {

    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

}
