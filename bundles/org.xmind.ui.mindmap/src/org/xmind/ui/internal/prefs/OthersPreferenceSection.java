package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class OthersPreferenceSection extends PreferenceFieldEditorPageSection
        implements IWorkbenchPreferencePage {

    @Override
    protected void createFieldEditors() {
        addAnimationField();
        addShadowField();
        addZoomField();
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return MindMapUIPlugin.getDefault().getPreferenceStore();
    }

    private void addAnimationField() {
        addField(new BooleanFieldEditor(PrefConstants.ANIMATION_ENABLED,
                PrefMessages.EditorPage_EnableAnimation_text,
                getDecratorParentComposite()));
    }

    private void addShadowField() {
        addField(new BooleanFieldEditor(PrefConstants.SHADOW_ENABLED,
                PrefMessages.EditorPage_EnableShadow_text,
                getDecratorParentComposite()));
    }

    private void addZoomField() {
        if (getPreferenceStore().getInt(PrefConstants.ZOOM_VALUE) == 0) {
//            int width = Display.getCurrent().getBounds().width;
//            if (width <= 1370)
            getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 100);
//            else if (width > 1370 && width <= 1930)
//                getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 120);
//            else
//                getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 150);
        }

        String[][] zoom = new String[][] { { "50%", "50" }, { "75%", "75" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                { "100%", "100" }, { "120%", "120" }, { "150%", "150" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                { "200%", "200" }, { "300%", "300" } };  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        addField(new ComboFieldEditor(PrefConstants.ZOOM_VALUE,
                PrefMessages.EditorPage_Zoom_Scale_text, zoom,
                getDecratorParentComposite()));
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
