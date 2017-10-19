package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class ThemePreferencePageSection extends PreferenceFieldEditorPageSection
        implements IWorkbenchPreferencePage {

    private IPreferenceStore pref = MindMapUIPlugin.getDefault()
            .getPreferenceStore();

    private ComboFieldEditor themeCombo;

    private String[][] themes = new String[][] {
            { PrefMessages.ThemePrefPage_OverrideButton,
                    PrefConstants.THEME_OVERRIDE },
            { PrefMessages.ThemePrefPage_KeepButton, PrefConstants.THEME_KEEP },
            { PrefMessages.ThemePrefPage_AskButton, PrefConstants.ASK_USER } };

    @Override
    public void init(IWorkbench workbench) {
        this.setPreferenceStore(pref);
    }

    @Override
    protected void createFieldEditors() {
        themeCombo = new ComboFieldEditor(PrefConstants.THEME_APPLY,
                MindMapMessages.ThemePrefPage_ThemeEditor_label, themes,
                getDecratorParentComposite());
        addField(themeCombo);
    }

    @Override
    protected void performDefaults() {
        pref.setValue(PrefConstants.THEME_APPLY, PrefConstants.ASK_USER);
        super.performDefaults();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }
}
