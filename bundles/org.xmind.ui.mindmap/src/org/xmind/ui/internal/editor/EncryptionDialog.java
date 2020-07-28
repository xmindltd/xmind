package org.xmind.ui.internal.editor;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class EncryptionDialog extends TitleAreaDialog {

    private static final String DEFAULT_BUTTON_TRIGGER_EVENT_ID = "DEFAULT_BUTTON_TRIGGER_EVENT_ID"; //$NON-NLS-1$

    private int defaultButtonId = -1;

    private Map<Integer, Button> buttons;

    private Text oldPasswordInputBox;

    private Text newPasswordInputBox;

    private Text verifyNewPasswordInputBox;

    private Text hintPasswordInputBox;

    private Label oldPasswordVerificationLabel;

    private Label newPasswordVerificationLabel;

    private Label warningLabel;

    private Composite container;

    private Image doneIcon;

    private String password;

    private String hintMessage;

    private Listener defaultButtonListener;

    protected EncryptionDialog(Shell parentShell) {
        super(parentShell);
    }

    private Image getErrorIcon() {
        if (getContainer() == null || getContainer().isDisposed())
            return null;
        if (doneIcon == null || doneIcon.isDisposed()) {
            ImageDescriptor img = MindMapUI.getImages()
                    .get(IMindMapImages.WARNING_ICON);
            if (img != null) {
                doneIcon = img.createImage(getContainer().getDisplay());
            }
        }
        return doneIcon;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(400, 300);
    }

    @Override
    public void create() {
        super.create();

        setTitle(hasPassword()
                ? MindMapMessages.EncrptionDialog_ChangePassword_title
                : MindMapMessages.EncryptionDialog_SetPassword_title);

        setTitleImage(null);

        setMessage(hasPassword()
                ? MindMapMessages.EncryptionDialog_ChangePassword_message
                : MindMapMessages.EncryptionDialog_SetPassword_message);
    }

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

        this.container = composite;

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 20;
        gridLayout.marginHeight = 20;
        gridLayout.verticalSpacing = 20;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createPasswordArea(composite);

        checkSetButton();

        return composite;
    }

    private Composite createPasswordArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setBackground(parent.getBackground());

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        area.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 3;
        area.setLayout(gridLayout);

        if (hasPassword()) {
            createOldPasswordInputBox(area);
        }

        createNewPasswordInputBox(area);
        createVerifyPasswordInputBox(area);
        createHintPasswordInputBox(area);

        createErrorMessage(area);

        Listener verifyListener = new Listener() {
            public void handleEvent(Event event) {
                checkSetButton();
            }
        };
        if (oldPasswordInputBox != null) {
            oldPasswordInputBox.addListener(SWT.Modify, verifyListener);
        }
        newPasswordInputBox.addListener(SWT.Modify, verifyListener);
        verifyNewPasswordInputBox.addListener(SWT.Modify, verifyListener);

        return area;
    }

    private void createErrorMessage(Composite parent) {
        new Label(parent, SWT.NONE);

        warningLabel = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = 200;
        gridData.heightHint = SWT.DEFAULT;
        warningLabel.setLayoutData(gridData);
        warningLabel.setForeground(
                Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        new Label(parent, SWT.NONE);
    }

    private void createOldPasswordInputBox(Composite parent) {
        createInputLabel(parent,
                MindMapMessages.EncryptDialogPane_oldpassword_text);

        oldPasswordInputBox = createInput(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE, SWT.DEFAULT);

        hookText(oldPasswordInputBox);
        addRefreshDefaultButtonListener(oldPasswordInputBox);
        addTriggerDefaultButtonListener(oldPasswordInputBox,
                SWT.DefaultSelection);

        oldPasswordVerificationLabel = new Label(parent, SWT.NONE);
        oldPasswordVerificationLabel.setBackground(parent.getBackground());
        oldPasswordVerificationLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        oldPasswordVerificationLabel.setImage(getErrorIcon());
        oldPasswordVerificationLabel.setVisible(false);
    }

    private void createNewPasswordInputBox(Composite parent) {
        String text;
        if (oldPasswordInputBox == null) {
            text = MindMapMessages.EncryptDialogPane_password_text;
        } else {
            text = MindMapMessages.EncryptDialogPane_newpassword_text;
        }
        createInputLabel(parent, text);

        newPasswordInputBox = createInput(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE, SWT.DEFAULT);

        hookText(newPasswordInputBox);
        addRefreshDefaultButtonListener(newPasswordInputBox);
        addTriggerDefaultButtonListener(newPasswordInputBox,
                SWT.DefaultSelection);

        new Label(parent, SWT.NONE);
    }

    private void createVerifyPasswordInputBox(Composite parent) {
        createInputLabel(parent,
                MindMapMessages.EncryptDialogPane_confirm_text);

        verifyNewPasswordInputBox = createInput(parent,
                SWT.BORDER | SWT.PASSWORD | SWT.SINGLE, SWT.DEFAULT);

        hookText(verifyNewPasswordInputBox);
        addRefreshDefaultButtonListener(verifyNewPasswordInputBox);
        addTriggerDefaultButtonListener(verifyNewPasswordInputBox,
                SWT.DefaultSelection);

        newPasswordVerificationLabel = new Label(parent, SWT.NONE);
        newPasswordVerificationLabel.setBackground(parent.getBackground());
        newPasswordVerificationLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        newPasswordVerificationLabel.setImage(getErrorIcon());
        newPasswordVerificationLabel.setVisible(false);
    }

    private void createHintPasswordInputBox(Composite parent) {
        createInputLabel(parent, NLS.bind(
                MindMapMessages.EncryptionDialog_HintInput_label, " \n ")); //$NON-NLS-1$

        hintPasswordInputBox = createInput(parent, SWT.BORDER | SWT.WRAP, 50);

        new Label(parent, SWT.NONE);

        hookText(hintPasswordInputBox);
    }

    private Label createInputLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        label.setBackground(parent.getBackground());
        return label;
    }

    private Text createInput(Composite parent, int style, int height) {
        Text input = new Text(parent, style);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.minimumWidth = 200;
        gridData.widthHint = 200;
        gridData.heightHint = height;
        input.setLayoutData(gridData);

        return input;
    }

    private Composite getContainer() {
        return container;
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(MindMapMessages.EncryptionDialog_SetPassword_title);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                MindMapMessages.EncryptionDialog_ButtonBar_Set_button, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        setSetButtonEnabled(false);
    }

    private void setSetButtonEnabled(boolean enabled) {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(enabled);
        }
    }

    private void checkSetButton() {
        if (oldPasswordInputBox == null) {
            setSetButtonEnabled(!"".equals(newPasswordInputBox.getText()) //$NON-NLS-1$
                    && !"".equals(verifyNewPasswordInputBox.getText())); //$NON-NLS-1$
        } else {
            setSetButtonEnabled(!"".equals(oldPasswordInputBox.getText())); //$NON-NLS-1$
        }
    }

    protected void addRefreshDefaultButtonListener(final Control focusControl) {
        focusControl.addListener(SWT.FocusIn, getDefaultButtonListener());
        focusControl.addListener(SWT.FocusOut, getDefaultButtonListener());
    }

    protected void addTriggerDefaultButtonListener(Control control,
            int triggerEvent) {
        control.addListener(triggerEvent, getDefaultButtonListener());
        control.setData(DEFAULT_BUTTON_TRIGGER_EVENT_ID,
                Integer.valueOf(triggerEvent));
    }

    private Listener getDefaultButtonListener() {
        if (defaultButtonListener == null) {
            defaultButtonListener = new Listener() {

                private Button savedDefaultButton = null;

                public void handleEvent(Event event) {
                    Object triggerEvent = event.widget
                            .getData(DEFAULT_BUTTON_TRIGGER_EVENT_ID);
                    if (triggerEvent instanceof Integer
                            && event.type == ((Integer) triggerEvent)
                                    .intValue()) {
                        triggerDefaultButton();
                        return;
                    }

                    if (event.type == SWT.FocusIn) {
                        changeDefaultButton();
                    } else if (event.type == SWT.FocusOut) {
                        restoreDefaultButton();
                    }
                }

                private void restoreDefaultButton() {
                    if (defaultButtonId >= 0) {
                        Shell shell = container.getShell();
                        if (savedDefaultButton != null
                                && savedDefaultButton.isDisposed()) {
                            savedDefaultButton = null;
                        }
                        shell.setDefaultButton(savedDefaultButton);
                    }
                }

                private void changeDefaultButton() {
                    if (defaultButtonId >= 0) {
                        final Shell shell = container.getShell();
                        savedDefaultButton = shell.getDefaultButton();
                        shell.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                Button button = getButton(defaultButtonId);
                                if (button != null && !button.isDisposed()) {
                                    shell.setDefaultButton(button);
                                }
                            }
                        });
                    }
                }
            };
        }
        return defaultButtonListener;
    }

    protected Button getDefaultButton() {
        if (buttons != null && defaultButtonId >= 0) {
            return getButton(defaultButtonId);
        }
        return null;
    }

    protected void triggerDefaultButton() {
        triggerButton(defaultButtonId);
    }

    protected void triggerButton(int buttonId) {
        if (buttonId >= 0) {
            Button button = getButton(buttonId);
            if (button != null && !button.isDisposed() && button.isEnabled()) {
                buttonPressed(buttonId);
            }
        }
    }

    protected void okPressed() {
        if (!verify()) {
            warningLabel.setText(verifyOldPassword()
                    ? MindMapMessages.EncryptionDialog_Warning_NotMatch_label
                    : MindMapMessages.EncryptionDialog_Warning_NotCorrect_label);
            return;
        }
        setPassword(newPasswordInputBox.getText());
        setHintMessage(hintPasswordInputBox.getText());
        super.okPressed();
    }

    private boolean verifyOldPassword() {
        boolean oldPasswordVerified = false;
        if (!hasPassword()) {
            oldPasswordVerified = !"".equals(newPasswordInputBox.getText()); //$NON-NLS-1$
        } else if (oldPasswordInputBox != null) {
            oldPasswordVerified = testsPassword(oldPasswordInputBox.getText());
            oldPasswordVerificationLabel.setVisible(!oldPasswordVerified);
        }
        newPasswordVerificationLabel.setVisible(oldPasswordVerified);
        return oldPasswordVerified;
    }

    private boolean verifyNewPassword() {
        boolean newPasswordVerified = ((oldPasswordInputBox != null //
                || !"".equals(newPasswordInputBox.getText()))) //$NON-NLS-1$
                && newPasswordInputBox.getText()
                        .equals(verifyNewPasswordInputBox.getText());
        newPasswordVerificationLabel.setVisible(!newPasswordVerified);
        return newPasswordVerified;
    }

    protected boolean verify() {
        return verifyOldPassword() && verifyNewPassword();
    }

    protected void setPassword(String newPassword) {
        if (verify()) {
            if ("".equals(newPassword)) { //$NON-NLS-1$
                newPassword = null;
            }
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_WORKBOOK,
                    UserDataConstants.SET_PASSWORD);
            this.password = newPassword;
        }
    }

    protected String getPassword() {
        return this.password;
    }

    protected boolean hasPassword() {
        return false;
    }

    protected boolean testsPassword(String password) {
        return false;
    }

    protected void setHintMessage(String hintMessage) {
        if (verify()) {
            if ("".equals(hintMessage)) //$NON-NLS-1$
                hintMessage = null;

            this.hintMessage = hintMessage;
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_WORKBOOK,
                    UserDataConstants.SET_PASSWORD_HINT);
        }
    }

    protected String getHintMessage() {
        return hintMessage;
    }

    private void hookText(final Text text) {
        text.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event event) {
                text.selectAll();
            }
        });
    }

}
