package org.xmind.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.SaveWizardManager.SaveWizardDescriptor;
import org.xmind.ui.wizards.SaveOptions;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class SaveWizardDialog extends Dialog {

    private List<SaveWizardDescriptor> wizards;

    private SaveWizardDescriptor targetWizard;

    private SaveOptions targetOptions;

    private boolean prepareForSpace = false;

    private Button defaultButton;

    public SaveWizardDialog(Shell parentShell,
            List<SaveWizardDescriptor> wizards,
            SaveWizardDescriptor targetWizard, SaveOptions targetOptions) {
        super(parentShell);
        this.wizards = wizards;
        this.targetWizard = targetWizard;
        this.targetOptions = targetOptions == null ? SaveOptions.getDefault()
                : targetOptions;
    }

    public SaveWizardDescriptor getTargetWizard() {
        return targetWizard;
    }

    public SaveOptions getTargetOptions() {
        return targetOptions;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(MindMapMessages.SaveWizardDialog_shell_title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.widthHint = 360;
        layoutData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(layoutData);

        createNameField(composite);
        createWizardChoiceField(composite);

        return composite;
    }

    private void createNameField(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        layoutData.widthHint = SWT.DEFAULT;
        layoutData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(MindMapMessages.SaveWizardDialog_name_text);

        Text text = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.setText(targetOptions.proposalName());

        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String content = ((Text) e.widget).getText();
                if (content.contains("\r")) { //$NON-NLS-1$
                    content = content.replaceAll("\n\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    content = content.replaceAll("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
                }
                targetOptions = targetOptions.proposalName(content);
            }
        });

        text.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.SHIFT) {
                    prepareForSpace = false;
                    while (getShell().getDefaultButton() != defaultButton) {
                        getShell().setDefaultButton(defaultButton);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.SHIFT) {
                    prepareForSpace = true;
                    while (getShell().getDefaultButton() != null)
                        getShell().setDefaultButton(null);
                    getShell().update();
                } else if (e.keyCode == SWT.CR) {
                    if (prepareForSpace) {
                        if (e.widget instanceof Text) {
                            ((Text) e.widget).insert(" "); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
    }

    private void createWizardChoiceField(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.widthHint = SWT.DEFAULT;
        layoutData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 5;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        label.setText(MindMapMessages.SaveWizardDialog_location_text);

        Composite radioGroup = new Composite(composite, SWT.NONE);
        GridLayout radioLayout = new GridLayout(1, false);
        radioLayout.marginWidth = 0;
        radioLayout.marginHeight = 0;
        radioLayout.verticalSpacing = 5;
        radioLayout.horizontalSpacing = 5;
        radioGroup.setLayout(radioLayout);

        for (final SaveWizardDescriptor wizard : wizards) {
            Button wizardRadio = new Button(radioGroup, SWT.RADIO);
            wizardRadio.setText(wizard.getName());
            wizardRadio.setData(wizard);
            wizardRadio.setSelection(wizard.equals(targetWizard));
            wizardRadio.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    targetWizard = (SaveWizardDescriptor) e.widget.getData();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                    okPressed();
                }
            });
        }

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        defaultButton = createButton(parent, IDialogConstants.OK_ID,
                MindMapMessages.SaveWizardDialog_okButton_text, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

}
