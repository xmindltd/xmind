package org.xmind.cathy.internal;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class GeneralPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private FieldEditor autoSaveInterval;

    private String[][] saveIntervals = new String[][] { { "5", "5" }, //$NON-NLS-1$//$NON-NLS-2$
            { "10", "10" }, //$NON-NLS-1$//$NON-NLS-2$
            { "30", "30" }, { "60", "60" } }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$

    private String[][] filesList = new String[][] { { "4", "4" }, { "5", "5" }, //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            { "10", "10" }, { "20", "20" }, //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            { "50", "50" } }; //$NON-NLS-1$//$NON-NLS-2$

    private boolean autoBackup = true;

    private BooleanFieldEditor autoBackupField;

    private Composite autoSaveIntervalsParent;

    private Button startupActionButton;

    private Composite container;

    public void init(IWorkbench workbench) {
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected void createFieldEditors() {

        addStartupGroup(container);
        new Label(container, SWT.NONE);

        addRecentFileCountSection(container);
        addAutoSaveGroup(container);

        this.initialize();
    }

    private void addStartupGroup(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(WorkbenchMessages.Startup_title);

        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(container);
        GridData data = new GridData();
        data.horizontalIndent = 25;
        data.minimumHeight = 0;
        container.setLayoutData(data);

        startupActionButton = new Button(container, SWT.CHECK);
        startupActionButton.setText(WorkbenchMessages.RestoreLastSession_label);
        addField(new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
                WorkbenchMessages.CheckUpdates_label, container));
    }

    private void addRecentFileCountSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(
                WorkbenchMessages.GeneralPrefPageSection_RecentFileCountSection_title);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 15)
                .applyTo(composite);

        Composite container = new Composite(composite, SWT.NONE);
        GridData data = new GridData();
        data.horizontalIndent = 25;
        container.setLayoutData(data);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

        addField(new ComboFieldEditor(IPreferenceConstants.RECENT_FILES,
                WorkbenchMessages.RecentFiles_label, filesList, container));
    }

    private void addAutoSaveGroup(Composite parent) {
        String message = WorkbenchMessages.AutoSave_label2;
        int index = message.indexOf("{0}"); //$NON-NLS-1$
        String label1, label2;
        label1 = message.substring(0, index);
        label2 = message.substring(index + 3, index + 7);
        if (null != saveIntervals) {
            for (String[] interval : saveIntervals) {
                interval[0] += " " + label2; //$NON-NLS-1$
            }
        }

        Label label = new Label(parent, SWT.NONE);
        label.setText(
                WorkbenchMessages.GeneralPrefPageSection_AutoSaveGroup_title);
        Composite container = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().extendedMargins(23, 0, 0, 0)
                .numColumns(1).applyTo(container);

        Composite saveParent = createContainer(container, 2);
        Composite enableParent = createContainer(saveParent, 1);
        addField(new BooleanFieldEditor(CathyPlugin.AUTO_SAVE_ENABLED, label1,
                enableParent));

        autoSaveIntervalsParent = createContainer(saveParent, 1);
        autoSaveInterval = new ComboFieldEditor(CathyPlugin.AUTO_SAVE_INTERVALS,
                "", saveIntervals, autoSaveIntervalsParent); //$NON-NLS-1$
        addField(autoSaveInterval);

        autoSaveInterval.setEnabled(
                getPreferenceStore().getBoolean(CathyPlugin.AUTO_SAVE_ENABLED),
                autoSaveIntervalsParent);

        Composite boolParent = createContainer(container, 1);
        autoBackupField = new BooleanFieldEditor(
                PrefConstants.AUTO_BACKUP_ENABLE,
                WorkbenchMessages.AutoBackup_label, boolParent);
        autoBackupField.setPropertyChangeListener(this);

    }

    private Composite createContainer(Composite parent, int cols) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(cols).applyTo(container);

        return container;
    }

    @Override
    protected Control createContents(Composite parent) {
        if (null == container)
            this.container = parent;
        return super.createContents(parent);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof FieldEditor) {
            FieldEditor fe = (FieldEditor) event.getSource();
            if (event.getProperty().equals(FieldEditor.VALUE)) {
                String prefName = fe.getPreferenceName();
                if (CathyPlugin.AUTO_SAVE_ENABLED.equals(prefName)) {
                    autoSaveInterval.setEnabled(
                            ((Boolean) event.getNewValue()).booleanValue(),
                            autoSaveIntervalsParent);
                } else if (PrefConstants.AUTO_BACKUP_ENABLE.equals(prefName)) {
                    autoBackup = ((Boolean) event.getNewValue()).booleanValue();
                }
            }
        }
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

        MindMapUIPlugin.getDefault().getPreferenceStore()
                .setValue(PrefConstants.AUTO_BACKUP_ENABLE, autoBackup);
        return true;
    }

    @Override
    protected void initialize() {
        super.initialize();
        int startupAction = getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION);
        startupActionButton
                .setSelection(startupAction == CathyPlugin.STARTUP_ACTION_LAST);
        autoBackupField.setPreferenceStore(
                MindMapUIPlugin.getDefault().getPreferenceStore());
        autoBackupField.load();
    }

    public void apply() {
        this.performApply();
    }

    public boolean ok() {
        return this.performOk();
    }

    public void excuteDefault() {
        this.performDefaults();
    }

    public boolean cancel() {
        return this.performCancel();
    }

}
