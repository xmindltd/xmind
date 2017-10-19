package org.xmind.ui.preference;

import org.eclipse.ui.IWorkbenchPreferencePage;

public interface IPreferenceSection extends IWorkbenchPreferencePage {

    public abstract void apply();

    public abstract boolean ok();

    public abstract void excuteDefault();

    public abstract boolean cancel();
}
