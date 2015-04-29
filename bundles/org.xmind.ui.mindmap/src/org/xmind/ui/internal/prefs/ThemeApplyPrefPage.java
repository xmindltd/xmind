package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class ThemeApplyPrefPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private IPreferenceStore pref = MindMapUIPlugin.getDefault()
            .getPreferenceStore();

    private Button override;

    private Button keep;

    private Button ask;

    public ThemeApplyPrefPage() {
        super(PrefMessages.ThemePrefPage_title);
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);

        Composite group = createGroup(parent);

        override = new Button(group, SWT.RADIO);
        override.setText(PrefMessages.ThemePrefPage_OverrideButton);
        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 3;
        override.setLayoutData(data);

        keep = new Button(group, SWT.RADIO);
        keep.setText(PrefMessages.ThemePrefPage_KeepButton);
        data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 3;
        keep.setLayoutData(data);

        ask = new Button(group, SWT.RADIO);
        ask.setText(PrefMessages.ThemePrefPage_AskButton);
        data = new GridData(SWT.FILL, SWT.NONE, true, true);
        data.horizontalSpan = 3;
        ask.setLayoutData(data);

        updateStatus();

        return parent;
    }

    private void updateStatus() {
        String themeOverride = pref.getString(PrefConstants.THEME_APPLY);

        override.setSelection(PrefConstants.THEME_OVERRIDE
                .equals(themeOverride));
        keep.setSelection(PrefConstants.THEME_KEEP.equals(themeOverride));
        ask.setSelection(PrefConstants.ASK_USER.equals(themeOverride)
                || IPreferenceStore.STRING_DEFAULT_DEFAULT
                        .equals(themeOverride));
    }

    private Composite createGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(gridLayout);

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setLayout(new GridLayout(1, false));
        group.setText(PrefMessages.ThemePrefPage_Group_text);
        return group;
    }

    @Override
    protected void performDefaults() {
        override.setSelection(false);
        keep.setSelection(false);
        ask.setSelection(true);
        pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.ASK_USER);
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        if (override.getSelection())
            pref.setValue(PrefConstants.THEME_APPLY,
                    PrefConstants.THEME_OVERRIDE);
        else if (keep.getSelection())
            pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.THEME_KEEP);
        else if (ask.getSelection())
            pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.ASK_USER);

        updateStatus();

        return true;
    }

}
