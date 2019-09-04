package org.xmind.ui.internal.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.prefs.PrefConstants;

public class AllowManualLayoutAction extends BooleanPrefAction {

    public AllowManualLayoutAction(IPreferenceStore prefStore) {
        super(prefStore, PrefConstants.MANUAL_LAYOUT_ALLOWED);
        setId("org.xmind.ui.allowManualLayout"); //$NON-NLS-1$
        setText(MindMapMessages.AllowManualLayout_text);
        setToolTipText(MindMapMessages.AllowManualLayout_toolTip);
    }

    @Override
    public void run() {
        super.run();
    }

}
