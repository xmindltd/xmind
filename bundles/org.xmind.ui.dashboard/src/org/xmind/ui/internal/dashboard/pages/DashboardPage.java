package org.xmind.ui.internal.dashboard.pages;

import org.eclipse.jface.dialogs.DialogPage;

public abstract class DashboardPage extends DialogPage
        implements IDashboardPage {

    private IDashboardPageContainer container = null;

    public void setContainer(IDashboardPageContainer container) {
        this.container = container;
    }

    protected IDashboardPageContainer getContainer() {
        return container;
    }

    protected void hideDashboard() {
        if (container == null)
            return;
        container.hideDashboard();
    }

}
