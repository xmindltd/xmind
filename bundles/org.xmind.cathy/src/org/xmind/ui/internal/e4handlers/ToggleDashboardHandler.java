
package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.ICathyConstants;

public class ToggleDashboardHandler {

    @Execute
    public void execute(@Optional MWindow window) {
        if (window == null) {
            CathyPlugin.log(
                    "Failed to locate active window when toggling the Dashboard."); //$NON-NLS-1$
            return;
        }

        if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
            window.getTags().remove(ICathyConstants.TAG_SHOW_DASHBOARD);
        } else {
            window.getTags().add(ICathyConstants.TAG_SHOW_DASHBOARD);
        }
    }

}
