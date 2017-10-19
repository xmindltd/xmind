package org.xmind.ui.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;

public abstract class PreferenceFieldEditorPageSection
        extends FieldEditorPreferencePage implements IPreferenceSection {

    @Override
    public void createControl(Composite parent) {
        Control body = createContents(parent);
        setControl(body);
    }

    protected Composite getDecratorParentComposite() {
        Composite composite = getFieldEditorParent();
        GridData data = (GridData) composite.getLayoutData();
        data.minimumHeight = 20;
        data.horizontalIndent = 25;
        return composite;
    }

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    public void apply() {
        this.performApply();
    }

    @Override
    public boolean ok() {
        return this.performOk();
    }

    @Override
    public void excuteDefault() {
        this.performDefaults();
    }

    @Override
    public boolean cancel() {
        return this.performCancel();
    }

}
