package org.xmind.ui.internal.dashboard.pages;

import org.eclipse.jface.dialogs.DialogPage;

public abstract class DashboardPage extends DialogPage
        implements IDashboardPage {

    private IDashboardContext context = null;

    public void setContext(IDashboardContext container) {
        this.context = container;
    }

    protected IDashboardContext getContext() {
        return context;
    }

}
