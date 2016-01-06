package org.xmind.ui.internal.dashboard.pages;

import org.eclipse.jface.dialogs.IDialogPage;

public interface IDashboardPage extends IDialogPage {

    void setContainer(IDashboardPageContainer container);

    void setFocus();

}
