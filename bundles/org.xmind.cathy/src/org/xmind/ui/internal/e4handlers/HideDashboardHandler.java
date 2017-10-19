package org.xmind.ui.internal.e4handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.xmind.cathy.internal.ICathyConstants;

public class HideDashboardHandler {

    @Execute
    public void execute(@Optional MWindow window) {
        if (window == null)
            return;

        if (window.getTags().contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
            window.getTags().remove(ICathyConstants.TAG_SHOW_DASHBOARD);
        }

    }

}
