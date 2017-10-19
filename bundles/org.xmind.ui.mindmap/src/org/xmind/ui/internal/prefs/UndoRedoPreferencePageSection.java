package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class UndoRedoPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private Composite container;

    private String[][] undos = new String[][] { { "10", "10" }, { "20", "20" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            { "50", "50" }, { "100", "100" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    @Override
    protected Control createContents(Composite parent) {
        container = parent;
        return super.createContents(parent);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return MindMapUIPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected void createFieldEditors() {
        addUndoRedoField();
    }

    private void addUndoRedoField() {
        Label descriptionLabel = new Label(container, SWT.WRAP);
        GridData data = (GridData) descriptionLabel.getLayoutData();
        if (null == data) {
            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            data.widthHint = 450;
            data.horizontalIndent = 25;
            descriptionLabel.setLayoutData(data);
        }
        descriptionLabel.setText(PrefMessages.EditorPage_UndoRedo_description);
        addField(new ComboFieldEditor(PrefConstants.UNDO_LIMIT,
                PrefMessages.EditorPage_UndoLimit_label, undos,
                getDecratorParentComposite()));

    }

}
