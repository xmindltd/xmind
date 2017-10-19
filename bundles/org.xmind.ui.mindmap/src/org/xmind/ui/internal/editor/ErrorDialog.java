package org.xmind.ui.internal.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.statushandlers.StatusDetails;
import org.xmind.ui.internal.statushandlers.StatusHandlerMessages;

public class ErrorDialog extends Dialog {

    private StatusDetails details;

    private Runnable closeCallback;

    private final ErrorSupportProvider supportProvider;

    public ErrorDialog(Shell parentShell, StatusAdapter error,
            ErrorSupportProvider supportProvider) {
        super(parentShell);
        this.details = new StatusDetails(error);
        this.closeCallback = null;
        this.supportProvider = supportProvider;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, 0);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        composite.setBackground(parent.getBackground());

        Control titleArea = createTitleArea(composite);
        titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) titleArea.getLayoutData()).widthHint = 280;

        applyDialogFont(composite);
        // initialize the dialog units
        initializeDialogUnits(composite);
        // create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        buttonBar = createButtonBar(composite);

        return composite;
    }

    private Control createTitleArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        area.setLayout(layout);
        area.setBackground(parent.getBackground());

        Label titleImageLabel = new Label(area, SWT.NONE);
        titleImageLabel.setImage(details.getImage());
        titleImageLabel.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        titleImageLabel.setBackground(parent.getBackground());

        Composite messageParent = new Composite(area, SWT.NONE);
        messageParent.setLayout(new GridLayout());
        messageParent.setBackground(parent.getBackground());

        Label messageLabel = new Label(messageParent, SWT.WRAP);
        messageLabel.setText(details.getMessage());
        messageLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        messageLabel.setBackground(parent.getBackground());

        if (supportProvider != null) {
            if (supportProvider instanceof AbstractStatusAreaProvider
                    && ((AbstractStatusAreaProvider) supportProvider)
                            .validFor(details.getStatusAdapter())) {
                ((AbstractStatusAreaProvider) supportProvider)
                        .createSupportArea(messageParent,
                                details.getStatusAdapter());
            } else if (supportProvider
                    .validFor(details.getStatusAdapter().getStatus())) {
                supportProvider.createSupportArea(messageParent,
                        details.getStatusAdapter().getStatus());
            }
        }

        return area;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Control detailsArea = createDetailsArea(composite);
        detailsArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData) detailsArea.getLayoutData()).widthHint = 400;
        ((GridData) detailsArea.getLayoutData()).heightHint = 80;

        return composite;
    }

    private Control createDetailsArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        area.setLayout(layout);

        Text detailsText = new Text(area,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        detailsText.setEditable(false);
        detailsText.setText(details.getFullText());
        detailsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return area;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        createBlankArea(parent);
        createButton(parent, IDialogConstants.CANCEL_ID,
                StatusHandlerMessages.RuntimeErrorDialog_CloseButton_Text,
                true);
    }

    private void createBlankArea(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        composite.setBackground(parent.getBackground());

        Hyperlink report = new Hyperlink(composite, SWT.LEFT);
        report.setText(
                StatusHandlerMessages.RuntimeErrorDialog_ReportHyperlink_Text);
        report.setUnderlined(true);
        report.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                reportPressed();
            }
        });
        report.setBackground(composite.getBackground());

    }

    private void reportPressed() {
        try {
            MindMapUIPlugin.getDefault().getErrorReporter().report(details);
        } catch (InterruptedException e) {
            return;
        }
        close();
    }

    /**
     * @param closeCallback
     *            the closeCallback to set
     */
    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    @Override
    public boolean close() {
        boolean returnValue = super.close();

        if (closeCallback != null) {
            Display.getCurrent().asyncExec(closeCallback);
        }

        return returnValue;
    }

}
