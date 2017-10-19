package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class DnDPreferencePageSection extends PreferenceFieldEditorPageSection
        implements IWorkbenchPreferencePage {

    IPreferenceStore pre = MindMapUIPlugin.getDefault().getPreferenceStore();

    private String[][] dndString = new String[][] {
            { PrefMessages.DnDPrefPage_LinkButton,
                    PrefConstants.CREATE_HYPERLINK },
            { PrefMessages.DnDPrefPage_CopyButton,
                    PrefConstants.CREATE_ATTACHMENT },
            { PrefMessages.DnDPrefPage_AlwaysRequestButton,
                    PrefConstants.ASK_USER } };

    @Override
    public void init(IWorkbench workbench) {
        this.setPreferenceStore(pre);
    }

    @Override
    protected void createFieldEditors() {
        addField(new ComboFieldEditor(PrefConstants.ADD_EXTERNAL_FILE,
                PrefMessages.DnDPrefPage_DnDLabel_Text, dndString,
                getDecratorParentComposite()));
    }

    @Override
    protected void performDefaults() {
        pre.setValue(PrefConstants.ADD_EXTERNAL_FILE, PrefConstants.ASK_USER);
        super.performDefaults();
    }

}
