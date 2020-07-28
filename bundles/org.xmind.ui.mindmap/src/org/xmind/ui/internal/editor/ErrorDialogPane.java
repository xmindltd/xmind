/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.internal.editor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.statushandlers.StatusDetails;

public class ErrorDialogPane extends DialogPane {

    private final StatusAdapter error;

    private final String summary;

    private Text summaryBoard;

    public ErrorDialogPane(StatusAdapter error) {
        this.error = error;
        Throwable cause = StatusDetails
                .getRootCause(error.getStatus().getException());
        if (cause == null)
            cause = new UnknownError();
        this.summary = NLS.bind(
                MindMapMessages.ErrorDialogPane_summaryBoard_text,
                new Object[] { error.getStatus().getMessage(),
                        cause.getClass().getName(),
                        cause.getLocalizedMessage() });
    }

    @Override
    protected Control createDialogContents(Composite parent) {
        Composite composite = (Composite) super.createDialogContents(parent);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createSummaryBoard(composite);
        return composite;
    }

    @Override
    protected int getPreferredWidth() {
        return 500;
    }

    private void createSummaryBoard(Composite parent) {
        Composite box = new Composite(parent, SWT.NONE);
        box.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        box.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 10;
        box.setLayout(gridLayout);

        createIcon(box);
        createSummaryBox(box);
    }

    private void createIcon(Composite parent) {
        Label iconLabel = new Label(parent, SWT.NONE);
        iconLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, true));
        iconLabel.setBackground(parent.getBackground());
        iconLabel.setImage(parent.getDisplay().getSystemImage(SWT.ICON_ERROR));
    }

    private void createSummaryBox(Composite parent) {
        summaryBoard = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        summaryBoard.setBackground(parent.getBackground());
        applyFont(summaryBoard);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        summaryBoard.setLayoutData(gridData);
        if (summary != null) {
            summaryBoard.setText(summary);
        }
    }

    public void dispose() {
        super.dispose();
        summaryBoard = null;
    }

    public void setFocus() {
        if (summaryBoard != null && !summaryBoard.isDisposed()) {
            summaryBoard.setFocus();
        }
    }

    protected void createButtonsForButtonBar(Composite buttonBar) {
        createButton(buttonBar, IDialogConstants.OK_ID,
                MindMapMessages.EncryptDialogPane_detailsButton_label, false);
        createButton(buttonBar, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, false);
        getButton(IDialogConstants.OK_ID).setEnabled(error != null);
    }

    @Override
    protected boolean closePressed() {
        setReturnCode(CANCEL);
        close();
        return true;
    }

    @Override
    protected boolean okPressed() {
        showDetails();
        return true;
    }

    private void showDetails() {
        StatusManager.getManager().handle(error, StatusManager.SHOW);
    }

    protected void escapeKeyPressed() {
        triggerButton(IDialogConstants.CLOSE_ID);
    }

}