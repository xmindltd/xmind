package org.xmind.ui.internal.dashboard.pages;

import org.eclipse.jface.dialogs.IDialogPage;

public interface IDashboardPage extends IDialogPage {

    void setContext(IDashboardContext container);

    void setFocus();

}
