package org.xmind.cathy.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class SaveBackupPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private String[][] saveIntervals = new String[][] { { "5", "5" }, //$NON-NLS-1$//$NON-NLS-2$
            { "10", "10" }, //$NON-NLS-1$//$NON-NLS-2$
            { "30", "30" }, { "60", "60" } }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$

    private Composite container;

    private FieldEditor autoSaveInterval;

    private boolean autoBackup = true;

    private BooleanFieldEditor autoBackupField;

    private Composite autoSaveIntervalsParent;

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
        autoBackupField.setPreferenceStore(
                MindMapUIPlugin.getDefault().getPreferenceStore());
        autoBackupField.load();
    }

    @Override
    protected void createFieldEditors() {
        addAutoSaveGroup(container);
        this.initialize();
    }

    private void addAutoSaveGroup(Composite parent) {
        String message = WorkbenchMessages.AutoSave_label2;
        int index = message.indexOf("{0}"); //$NON-NLS-1$
        String label1, label2;
        label1 = message.substring(0, index);
        label2 = message.substring(index + 3);
        if (null != saveIntervals) {
            for (String[] interval : saveIntervals) {
                interval[0] += " " + label2; //$NON-NLS-1$
            }
        }

        Composite container = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(container);
        GridLayoutFactory.fillDefaults().extendedMargins(23, 0, 0, 0)
                .applyTo(container);

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
        GridDataFactory.fillDefaults().applyTo(container);
        GridLayoutFactory.fillDefaults().numColumns(cols).applyTo(container);

        return container;
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
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .setValue(PrefConstants.AUTO_BACKUP_ENABLE, autoBackup);
        return true;
    }
}
