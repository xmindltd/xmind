package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class AuthorInfoPrefPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private Text nameEditor = null;

    private Text emailEditor = null;

    private Text organizationEditor = null;

    private static final String EMPTY = ""; //$NON-NLS-1$

    public AuthorInfoPrefPage() {
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return MindMapUIPlugin.getDefault().getPreferenceStore();
    }

    public void init(IWorkbench workbench) {
        String name = doGetPreferenceStore().getString(
                PrefConstants.AUTHOR_INFO_NAME);
        if (name == null || "".equals(name)) { //$NON-NLS-1$
            doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_NAME,
                    System.getProperty("user.name")); //$NON-NLS-1$
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout compositeLayout = new GridLayout(1, false);
        compositeLayout.marginWidth = 0;
        compositeLayout.marginHeight = 0;
        compositeLayout.verticalSpacing = 5;
        compositeLayout.horizontalSpacing = 5;
        composite.setLayout(compositeLayout);

        Label descriptionLabel = new Label(composite, SWT.WRAP);
        descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
                false));
        ((GridData) descriptionLabel.getLayoutData()).widthHint = 240;
        descriptionLabel.setText(PrefMessages.AuthorInfoPage_Message);

        createNameItem(composite);
        createEmailItem(composite);
        createOrganizationItem(composite);

        return composite;
    }

    private Composite createNameItem(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginHeight = 0;
        inputLayout.marginWidth = 0;
        inputLayout.verticalSpacing = 0;
        composite.setLayout(inputLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createLabel(composite, PrefMessages.AuthorInfoPage_Name_label);
        nameEditor = createEditor(composite,
                doGetPreferenceStore()
                        .getString(PrefConstants.AUTHOR_INFO_NAME), false);

        return composite;
    }

    private Composite createEmailItem(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginHeight = 0;
        inputLayout.marginWidth = 0;
        inputLayout.verticalSpacing = 0;
        composite.setLayout(inputLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createLabel(composite, PrefMessages.AuthorInfoPage_Email_label);
        emailEditor = createEditor(
                composite,
                doGetPreferenceStore().getString(
                        PrefConstants.AUTHOR_INFO_EMAIL), true);
        return composite;
    }

    private Composite createOrganizationItem(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginHeight = 0;
        inputLayout.marginWidth = 0;
        inputLayout.verticalSpacing = 0;
        composite.setLayout(inputLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createLabel(composite, PrefMessages.AuthorInfoPage_Organization_label);
        organizationEditor = createEditor(composite, doGetPreferenceStore()
                .getString(PrefConstants.AUTHOR_INFO_ORG), true);

        return composite;
    }

    private void createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
    }

    private Text createEditor(Composite parent, String content,
            boolean canBeEmpty) {
        final Text editor = new Text(parent, SWT.BORDER | SWT.SINGLE);
        editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        if (content != null && !EMPTY.equals(content))
            editor.setText(content);

        if (!canBeEmpty) {
            editor.addFocusListener(new FocusListener() {

                public void focusLost(FocusEvent e) {
                    validateLibraryName(editor);
                }

                public void focusGained(FocusEvent e) {
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            if (editor.isDisposed())
                                return;
                            editor.setSelection(0, editor.getCharCount());
                        }
                    });
                }
            });

            editor.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validateLibraryName(editor);
                }
            });
        }

        return editor;
    }

    protected boolean validateLibraryName(Text editor) {
        if (EMPTY.equals(editor.getText())) {
            setErrorMessage(PrefMessages.PreferencePage_EmptyName_errorMessage);
            return false;
        } else {
            setErrorMessage(null);
            return true;
        }
    }

    @Override
    public boolean performOk() {
        if (!saveLibraryName())
            return false;
        return true;
    }

    @Override
    protected void performDefaults() {

        System.setProperty(DOMConstants.AUTHOR_NAME,
                System.getProperty("user.name")); //$NON-NLS-1$
        System.setProperty(DOMConstants.AUTHOR_EMAIL, EMPTY);
        System.setProperty(DOMConstants.AUTHOR_ORG, EMPTY);

        if (nameEditor != null && !nameEditor.isDisposed())
            nameEditor.setText(System.getProperty("user.name")); //$NON-NLS-1$
        if (emailEditor != null && !emailEditor.isDisposed())
            emailEditor.setText(EMPTY);
        if (organizationEditor != null && !organizationEditor.isDisposed())
            organizationEditor.setText(EMPTY);

        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_NAME,
                System.getProperty("user.name")); //$NON-NLS-1$
        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_EMAIL, EMPTY);
        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_ORG, EMPTY);

        super.performDefaults();
    }

    private boolean saveLibraryName() {
        if (nameEditor == null || nameEditor.isDisposed())
            return true;
        if (!validateLibraryName(nameEditor))
            return false;

        System.setProperty(DOMConstants.AUTHOR_NAME, nameEditor.getText());
        System.setProperty(DOMConstants.AUTHOR_EMAIL, emailEditor.getText());
        System.setProperty(DOMConstants.AUTHOR_ORG,
                organizationEditor.getText());

        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_NAME,
                nameEditor.getText());
        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_EMAIL,
                emailEditor.getText());
        doGetPreferenceStore().setValue(PrefConstants.AUTHOR_INFO_ORG,
                organizationEditor.getText());

        return true;
    }

}
