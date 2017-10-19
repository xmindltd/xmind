package org.xmind.ui.internal.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;

class ThemeOverrideDialog extends Dialog {

    private Button rememberCheck;

    private ResourceManager resources;

    protected ThemeOverrideDialog(Shell parentShell) {
        super(parentShell);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parentShell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.ThemesView_Dialog_title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.ThemesView_Dialog_message);

        createRememberCheck(composite);

        return composite;
    }

    private void createRememberCheck(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginTop = 25;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        rememberCheck = new Button(composite, SWT.CHECK);
        rememberCheck.setText(Messages.ThemesView_Dialog_Check);
        rememberCheck
                .setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
    }

    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_MARGIN);
        gridLayout.marginHeight = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_MARGIN);
        gridLayout.marginBottom = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_MARGIN);
        gridLayout.verticalSpacing = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_SPACING);
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(gridLayout);

        createPrefLink(composite);

        Composite buttonBar = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(
                IDialogConstants.VERTICAL_SPACING);
        buttonBar.setLayout(layout);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, true));
        buttonBar.setFont(parent.getFont());

        createButtonsForButtonBar(buttonBar);
        return buttonBar;
    }

    private void createPrefLink(Composite parent) {
        Hyperlink prefLink = new Hyperlink(parent, SWT.SINGLE);
        prefLink.setText(Messages.ThemesView_Dialog_PrefLink);
        prefLink.setUnderlined(false);
        prefLink.setForeground(
                resources.createColor(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$

        prefLink.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                PreferencesUtil
                        .createPreferenceDialogOn(null,
                                "org.xmind.ui.ThemePrefPage", null, null) //$NON-NLS-1$
                        .open();
            }
        });
        prefLink.getParent().setFocus();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                Messages.ThemesView_OverrideButton, true);
        createButton(parent, IDialogConstants.NO_ID,
                Messages.ThemesView_KeepButton, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (IDialogConstants.NO_ID == buttonId)
            noPressed();
    }

    @Override
    protected void okPressed() {
        IPreferenceStore pref = MindMapUIPlugin.getDefault()
                .getPreferenceStore();
        if (rememberCheck.getSelection())
            pref.setValue(PrefConstants.THEME_APPLY,
                    PrefConstants.THEME_OVERRIDE);
        else
            pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.ASK_USER);
        super.okPressed();
    }

    private void noPressed() {
        IPreferenceStore pref = MindMapUIPlugin.getDefault()
                .getPreferenceStore();
        if (rememberCheck.getSelection())
            pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.THEME_KEEP);
        else
            pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.ASK_USER);
        setReturnCode(IDialogConstants.NO_ID);
        close();
    }

}
