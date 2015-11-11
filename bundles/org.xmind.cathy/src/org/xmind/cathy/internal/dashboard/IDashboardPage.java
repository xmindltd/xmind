package org.xmind.cathy.internal.dashboard;

import org.eclipse.jface.dialogs.IDialogPage;

public interface IDashboardPage extends IDialogPage {

    void setContainer(IDashboardPageContainer container);

    void setFocus();

}
