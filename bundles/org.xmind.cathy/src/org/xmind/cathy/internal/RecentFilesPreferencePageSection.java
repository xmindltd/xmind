package org.xmind.cathy.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;

public class RecentFilesPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private String[][] filesList = new String[][] { { "10", "10" }, //$NON-NLS-1$//$NON-NLS-2$
            { "20", "20" },  //$NON-NLS-1$ //$NON-NLS-2$
            { "50", "50" } }; //$NON-NLS-1$//$NON-NLS-2$

    private Composite container;
    private ComboFieldEditor recentFilesField;

    @Override
    protected Control createContents(Composite parent) {
        if (null == container)
            this.container = parent;
        return super.createContents(parent);
    }

    @Override
    protected void createFieldEditors() {
        addRecentFileCountSection(container);
        this.initialize();
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    private void addRecentFileCountSection(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(container);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

        addField(recentFilesField = new ComboFieldEditor(
                IPreferenceConstants.RECENT_FILES,
                WorkbenchMessages.RecentFiles_label, filesList, container));
    }

    @Override
    protected void initialize() {
        recentFilesField.setPreferenceStore(
                WorkbenchPlugin.getDefault().getPreferenceStore());
        recentFilesField.load();
    }

    @Override
    public void init(IWorkbench workbench) {
//        WorkbenchPlugin.getDefault().getPreferenceStore().setDefault(
//                IPreferenceConstants.RECENT_FILES, DEFAULT_RECENT_VALUE);
        super.init(workbench);
    }

}
