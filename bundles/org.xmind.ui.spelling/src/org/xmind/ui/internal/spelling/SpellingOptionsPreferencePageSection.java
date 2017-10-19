package org.xmind.ui.internal.spelling;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;

import com.swabunga.spell.engine.Configuration;

public class SpellingOptionsPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private List<FieldEditor> settingFields = new ArrayList<FieldEditor>();

    protected Composite settingsParent;

    public void init(IWorkbench workbenche) {
        setPreferenceStore(SpellingPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(SpellingPlugin.SPELLING_CHECK_ENABLED,
                Messages.enableSpellCheck, getFieldEditorParent()));
        addSpellingSettings(getFieldEditorParent());
        updateOptions(SpellingPlugin.isSpellingCheckEnabled());
    }

    private void addSpellingSettings(Composite parent) {
        settingsParent = createSettingsParent(parent);
        addSettingField(Configuration.SPELL_IGNOREUPPERCASE,
                Messages.ignoreAllCapital);
        addSettingField(Configuration.SPELL_IGNOREMIXEDCASE,
                Messages.ignoreMultiCapital);
        addSettingField(Configuration.SPELL_IGNOREINTERNETADDRESSES,
                Messages.ignoreWebAddress);
        addSettingField(Configuration.SPELL_IGNOREDIGITWORDS,
                Messages.ignoreNumberousAppendix);
        addSettingField(Configuration.SPELL_IGNORESENTENCECAPITALIZATION,
                Messages.ignoreFirstLowercaseSentences);
    }

    private void addSettingField(String name, String label) {
        FieldEditor field = new BooleanFieldEditor(name, label, settingsParent);
        addField(field);
        settingFields.add(field);
    }

    private void updateOptions(boolean enabled) {
        settingsParent.setEnabled(enabled);
        for (FieldEditor field : settingFields) {
            field.setEnabled(enabled, settingsParent);
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        FieldEditor field = (FieldEditor) event.getSource();
        if (SpellingPlugin.SPELLING_CHECK_ENABLED
                .equals(field.getPreferenceName())) {
            updateOptions(((BooleanFieldEditor) field).getBooleanValue());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (settingsParent != null) {
            settingsParent.dispose();
            settingsParent = null;
        }
    }

    protected Composite createSettingsParent(Composite parent) {
        GridLayoutFactory.fillDefaults().extendedMargins(25, 0, 0, 0)
                .applyTo(parent);
        parent.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite itemContainer = new Composite(parent, SWT.NONE);
        itemContainer.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        GridLayoutFactory.fillDefaults().applyTo(itemContainer);

        return itemContainer;
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
