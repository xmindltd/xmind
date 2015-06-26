package org.xmind.ui.biggerplate;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.biggerplate.BiggerplateOauth;
import org.xmind.ui.internal.biggerplate.Info;

public class BiggerplatePrefPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private Composite composite;

    private Label accountLabel;

    private Label usernameLabel;

    private Button changeStatusButton;

    public BiggerplatePrefPage() {
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(BiggerplatePlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        Label descriptionLabel = new Label(composite, SWT.WRAP);
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
                false));
        descriptionLabel
                .setText(BiggerplateMessages.PreferencePage_FeatureDescription_message);

        createAccountInfoArea(composite);
        updateStatusSection(false);

        return composite;
    }

    private void updateStatusSection(boolean async) {
        if (accountLabel == null || accountLabel.isDisposed()
                || usernameLabel == null || usernameLabel.isDisposed()
                || changeStatusButton == null
                || changeStatusButton.isDisposed())
            return;

        if (async) {
            changeStatusButton.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateStatusSection(false);
                }
            });
        } else {
            String refreshToken = getPreferenceStore().getString(
                    BiggerplateOauth.PREFERENCE_STORE_REFRESH_TOKEN_KEY);
            boolean biggerplateSignedin = refreshToken != null
                    && !refreshToken.equals(""); //$NON-NLS-1$

            if (biggerplateSignedin) {
                setEnabled(accountLabel, true);
                setEnabled(usernameLabel, true);
                usernameLabel.setText(getPreferenceStore().getString(
                        BiggerplateOauth.PREFERENCE_STORE_USERNAME_KEY));

                changeStatusButton
                        .setText(BiggerplateMessages.PreferencePage_UnlinkEvernote_text);
                changeStatusButton.getParent().layout();
                Point p = changeStatusButton.getParent().computeSize(
                        SWT.DEFAULT, SWT.DEFAULT);
                changeStatusButton.getParent().setSize(p);
            } else {
                setEnabled(accountLabel, false);
                setEnabled(usernameLabel, false);
                changeStatusButton
                        .setText(BiggerplateMessages.PreferencePage_LinkEvernote_text);
                changeStatusButton.getParent().layout();
            }
            composite.layout(true, true);
        }
    }

    private void createAccountInfoArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);

        accountLabel = new Label(composite, SWT.NONE);
        accountLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        accountLabel.setFont(JFaceResources.getDefaultFont());
        accountLabel
                .setText(BiggerplateMessages.PreferencePage_AccountInfo_label);

        usernameLabel = new Label(composite, SWT.NONE);
        usernameLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        usernameLabel.setFont(JFaceResources.getDefaultFont());
        usernameLabel.setText(""); //$NON-NLS-1$

        changeStatusButton = new Button(composite, SWT.PUSH);
        changeStatusButton.setText(""); //$NON-NLS-1$
        changeStatusButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String refreshToken = getPreferenceStore().getString(
                        BiggerplateOauth.PREFERENCE_STORE_REFRESH_TOKEN_KEY);
                boolean biggerplateSignedin = refreshToken != null
                        && !refreshToken.equals(""); //$NON-NLS-1$
                if (biggerplateSignedin
                        && !MessageDialog
                                .openConfirm(
                                        Display.getDefault().getActiveShell(),
                                        BiggerplateMessages.PreferencePage_UnlinkAccount_title,
                                        BiggerplateMessages.PreferencePage_UnlinkAccount_text))
                    return;

                changeServiceStatus();
            }
        });
    }

    private void changeServiceStatus() {
        final Display display = Display.getDefault();
        changeStatusButton.setEnabled(false);
        Thread t = new Thread(new Runnable() {
            public void run() {
                String refreshToken = getPreferenceStore().getString(
                        BiggerplateOauth.PREFERENCE_STORE_REFRESH_TOKEN_KEY);
                boolean biggerplateSignedin = refreshToken != null
                        && !refreshToken.equals(""); //$NON-NLS-1$

                if (biggerplateSignedin) {
                    signOut();
                } else {
                    signIn();
                }

                display.asyncExec(new Runnable() {
                    public void run() {
                        if (changeStatusButton != null
                                && !changeStatusButton.isDisposed()) {
                            changeStatusButton.setEnabled(true);
                        }
                    }
                });
                updateStatusSection(true);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void setEnabled(Control control, boolean enabled) {
        control.setVisible(enabled);
        ((GridData) control.getLayoutData()).exclude = !enabled;
    }

    private void signIn() {
        Info info = new Info();
        String accessToken = BiggerplateOauth.generateAccessToken(info);
        if (accessToken == null || accessToken.equals("")) { //$NON-NLS-1$
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    MessageDialog
                            .openWarning(
                                    Display.getCurrent().getActiveShell(),
                                    BiggerplateMessages.PreferencePage_LinkFailed_title,
                                    BiggerplateMessages.PreferencePage_LinkFailed_text);
                }
            });
        }
    }

    private void signOut() {
        getPreferenceStore().setValue(
                BiggerplateOauth.PREFERENCE_STORE_REFRESH_TOKEN_KEY, ""); //$NON-NLS-1$
        getPreferenceStore().setValue(
                BiggerplateOauth.PREFERENCE_STORE_USERNAME_KEY, ""); //$NON-NLS-1$
    }

}
