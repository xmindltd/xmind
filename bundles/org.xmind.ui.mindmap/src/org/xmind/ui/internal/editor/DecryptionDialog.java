package org.xmind.ui.internal.editor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;

public class DecryptionDialog extends TitleAreaDialog {

    private String workbookName;

    private String hintMessage;

    private int times;

    private Text passwordInputBox;

    private String password;

    public DecryptionDialog(Shell parent) {
        super(parent);
    }

    public DecryptionDialog(Shell parent, String workbookName,
            String hintMessage) {
        this(parent);
        this.workbookName = workbookName;
        this.hintMessage = hintMessage;
    }

    public DecryptionDialog(Shell parent, String workbookMessage,
            String hintMessage, int times) {
        this(parent, workbookMessage, hintMessage);
        this.times = times;
    }

    @Override
    public void create() {
        super.create();

        setTitle(MindMapMessages.DecryptionDialog_title);

        setTitleImage(null);

        setMessage(MindMapMessages.DecryptionDialog_message);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parent.getFont());

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 20;
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createContentArea(composite);

        checkOkButton();

        return composite;
    }

    private void createContentArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setBackground(parent.getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        area.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 3;
        area.setLayout(gridLayout);

        createFileNamePart(area);
        createPasswordPart(area);

        if ("".equals(hintMessage)) //$NON-NLS-1$
            hintMessage = null;
        if ((times > 0 && times < 4) || (times >= 4 && hintMessage == null))
            createErrorMessagePart(area);

        if (times >= 4 && hintMessage != null)
            createHintMessagePart(area);
    }

    private void createFileNamePart(Composite area) {
        Label label = new Label(area, SWT.NONE);
        label.setText(MindMapMessages.DecryptionDialog_FileName_label);
        label.setLayoutData(
                new GridData(SWT.CLIP_CHILDREN, SWT.CENTER, false, false));

        Label fileName = new Label(area, SWT.NONE);
        fileName.setText(workbookName == null
                ? MindMapMessages.DecryptionDialog_FileName_untitled
                : workbookName);
    }

    private void createPasswordPart(Composite area) {
        Label label = new Label(area, SWT.NONE);
        label.setText(MindMapMessages.DecryptionDialog_Password_label);
        label.setLayoutData(
                new GridData(SWT.CLIP_CHILDREN, SWT.CENTER, false, false));

        createPasswordInputBox(area);
    }

    private void createErrorMessagePart(Composite area) {
        new Label(area, SWT.NONE);

        Label warningLabel = new Label(area, SWT.NONE);
        warningLabel
                .setText(MindMapMessages.DecryptionDialog_WarningLabel_text);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 320;
        gridData.heightHint = SWT.DEFAULT;
        warningLabel.setLayoutData(gridData);
        warningLabel.setForeground(
                Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    }

    private void createHintMessagePart(Composite parent) {
        new Label(parent, SWT.NONE);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        Label hintLabel = new Label(composite, SWT.NONE);
        hintLabel.setText(MindMapMessages.DecryptionDialog_Hint_label);
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_WORKBOOK,
                UserDataConstants.GET_PASSWORD_HINT);

        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.FILL)
                .applyTo(hintLabel);

        Label hintMessageLabel = new Label(composite, SWT.WRAP);
        hintMessageLabel.setText(hintMessage);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 320;
        gridData.heightHint = SWT.DEFAULT;
        hintMessageLabel.setLayoutData(gridData);
    }

    protected void createButtonsForButtonBar(Composite buttonBar) {
        createOkButton(buttonBar);
        createCloseButton(buttonBar);
    }

    private void createOkButton(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
        setOkButtonEnabled(false);
    }

    private void createCloseButton(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    private void createPasswordInputBox(Composite parent) {
        passwordInputBox = new Text(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.minimumWidth = 320;
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        passwordInputBox.setLayoutData(gridData);

        hookText(passwordInputBox);

        Listener inputChangedListener = new Listener() {
            public void handleEvent(Event event) {
                checkOkButton();
            }
        };
        passwordInputBox.addListener(SWT.Modify, inputChangedListener);
    }

    protected void okPressed() {
        this.password = passwordInputBox.getText();
        setReturnCode(OK);
        close();
    }

    private void hookText(final Text text) {
        text.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                text.selectAll();
            }
        });
    }

    protected String getPassword() {
        return password;
    }

    private void checkOkButton() {
        setOkButtonEnabled(passwordInputBox != null
                && !"".equals(passwordInputBox.getText())); //$NON-NLS-1$
    }

    private void setOkButtonEnabled(boolean enabled) {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed())
            button.setEnabled(enabled);
    }

}
