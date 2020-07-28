package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.prefs.PrefConstants;

public class TopicPositionPreferenceSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private Composite container;

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        GridLayout layout = (GridLayout) container.getLayout();
        layout.marginLeft = 25;
        addAllowOverlapsField();
        addAllowManualLayoutField();
//        addAllowFreePositionField();

//        Label descriptionLabel = new Label(container, SWT.WRAP);
//        descriptionLabel
//                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//        ((GridData) descriptionLabel.getLayoutData()).widthHint = 450;
//        descriptionLabel.setText(
//                PrefMessages.EditorPage_TopicPositioning_FreePositioning_description);
    }

    @Override
    protected Control createContents(Composite parent) {
        if (null == container)
            this.container = parent;
        return super.createContents(parent);
    }

    // allow  overlap 
    private void addAllowOverlapsField() {
        BooleanFieldEditor allowOverlapsField = new BooleanFieldEditor(
                PrefConstants.OVERLAPS_ALLOWED,
                PrefMessages.EditorPage_TopicPositioning_AllowOverlaps,
                getFieldEditorParent()) {

            @Override
            protected void doStore() {
                boolean oldValue = getPreferenceStore()
                        .getBoolean(getPreferenceName());
                super.doStore();
                boolean newValue = getPreferenceStore()
                        .getBoolean(getPreferenceName());
                if (oldValue != newValue) {
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .trackEvent(UserDataConstants.CATEGORY_PREFERENCE,
                                    UserDataConstants.ALLOW_OVERLAP);
                }
            }
        };
        addField(allowOverlapsField);
    }

//    private void addAllowFreePositionField() {
//        addField(new BooleanFieldEditor(PrefConstants.FREE_POSITION_ALLOWED,
//                PrefMessages.EditorPage_TopicPositioning_AllowFreePosition,
//                getFieldEditorParent()));
//    }

    //allow manual layout
    private void addAllowManualLayoutField() {
        BooleanFieldEditor allowManualLayoutField = new BooleanFieldEditor(
                PrefConstants.MANUAL_LAYOUT_ALLOWED,
                PrefMessages.EditorPage_TopicPositioning_AllowManualLayout,
                getFieldEditorParent()) {

            @Override
            protected void doStore() {
                boolean oldValue = getPreferenceStore()
                        .getBoolean(getPreferenceName());
                super.doStore();
                boolean newValue = getPreferenceStore()
                        .getBoolean(getPreferenceName());
                if (oldValue != newValue) {
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .trackEvent(UserDataConstants.CATEGORY_PREFERENCE,
                                    UserDataConstants.ALLOW_FREE_POSITION);
                }
            }
        };
        addField(allowManualLayoutField);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return MindMapUIPlugin.getDefault().getPreferenceStore();
    }

}
