package org.xmind.ui.internal.e4handlers;

import java.util.List;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.ICathyConstants;

public class ShowDashboardPageHandler {

    @Execute
    public void execute(@Optional MWindow window,
            @Optional ParameterizedCommand command) {
        Object pageId = command == null ? null
                : command.getParameterMap()
                        .get(ICathyConstants.PARAMETER_DASHBOARD_PAGE_ID);
        showDashboardPage(window,
                pageId instanceof String ? (String) pageId : null);
    }

    public static void showDashboardPage(MWindow window, String pageId) {
        if (window == null) {
            CathyPlugin.log(
                    "Failed to find active window in ShowDashboardPageHandler."); //$NON-NLS-1$
            return;
        }

        List<String> tags = window.getTags();
        if (!tags.contains(ICathyConstants.TAG_SHOW_DASHBOARD)) {
            tags.add(ICathyConstants.TAG_SHOW_DASHBOARD);
        }

        if (pageId == null)
            return;

        EModelService modelService = window.getContext()
                .get(EModelService.class);
        if (modelService == null) {
            CathyPlugin.log(
                    "Failed to find EModelService in ShowDashboardPageHandler."); //$NON-NLS-1$
            return;
        }

        List<MPart> dashboardParts = modelService.findElements(window,
                ICathyConstants.ID_DASHBOARD_PART, MPart.class, null);
        if (dashboardParts.isEmpty()) {
            CathyPlugin.log(
                    "Failed to find Dashboard part in ShowDashboardPageHandler."); //$NON-NLS-1$
            return;
        }

        MPart dashboardPart = dashboardParts.get(0);

        String oldPageId = (String) dashboardPart.getTransientData()
                .get(ICathyConstants.DATA_DASHBOARD_SELECTED_PAGE_ID);
        if (pageId.equals(oldPageId)) {
            ////todo
            return;
        }

        dashboardPart.getTransientData()
                .remove(ICathyConstants.DATA_DASHBOARD_SELECTED_PAGE_ID);
        dashboardPart.getTransientData()
                .put(ICathyConstants.DATA_DASHBOARD_SELECTED_PAGE_ID, pageId);
    }

}
