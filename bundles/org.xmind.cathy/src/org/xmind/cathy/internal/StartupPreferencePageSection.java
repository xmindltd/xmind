package org.xmind.cathy.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;

public class StartupPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private Composite container;

    private Button startupActionButton;

    @Override
    protected Control createContents(Composite parent) {
        if (null == container)
            this.container = parent;
        return super.createContents(parent);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected void initialize() {
        super.initialize();
        int startupAction = getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION);
        startupActionButton
                .setSelection(startupAction == CathyPlugin.STARTUP_ACTION_LAST);
    }

    @Override
    protected void createFieldEditors() {
        addStartupGroup(container);
        this.initialize();
    }

    private void addStartupGroup(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(container);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(container);

        startupActionButton = new Button(container, SWT.CHECK);
        startupActionButton.setText(WorkbenchMessages.RestoreLastSession_label);

        if (isShowUploadDataCheck()) {
            addField(
                    new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
                            WorkbenchMessages.CheckUpdates_label, container));
        }
    }

    private boolean isShowUploadDataCheck() {
        return !Boolean.getBoolean(CathyPlugin.KEY_NOT_SHOW_UPLOAD_DATA_CHECK);
    }

    @Override
    public boolean performOk() {
        if (!super.performOk())
            return false;

        if (startupActionButton.getSelection()) {
            getPreferenceStore().setValue(CathyPlugin.STARTUP_ACTION,
                    CathyPlugin.STARTUP_ACTION_LAST);
        } else {
            getPreferenceStore().setValue(CathyPlugin.STARTUP_ACTION,
                    CathyPlugin.STARTUP_ACTION_WIZARD);
        }

        return true;
    }

}
