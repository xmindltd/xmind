/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class EditorPrefPage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

//    private Composite tipsFadeDelayParent;
//    private IntegerFieldEditor tipsFadeDelayField;

    public EditorPrefPage() {
        super(PrefMessages.EditorPage_title, FLAT);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return MindMapUIPlugin.getDefault().getPreferenceStore();
    }

    protected void createFieldEditors() {
        addPreviewField();
        addUndoRedoField();
        addTopicPositioningGroup();
        addAnimationField();
        addShadowField();
        addZoomField();
//        addGradientColorField();
    }

    private void addUndoRedoField() {
        Composite parent = createGroup(PrefMessages.EditorPage_UndoLimit_title);

        addField(new IntegerFieldEditor(PrefConstants.UNDO_LIMIT,
                PrefMessages.EditorPage_UndoLimit_label,
                createFieldContainer(parent)));

        Label descriptionLabel = new Label(parent, SWT.WRAP);
        descriptionLabel.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false));
        ((GridData) descriptionLabel.getLayoutData()).widthHint = 400;
        descriptionLabel.setText(PrefMessages.EditorPage_UndoRedo_description);

//        Label blank = new Label(parent, SWT.NONE);
//        blank.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
//                false));
//        ((GridData) blank.getLayoutData()).heightHint = 5;
//        blank.setText(""); //$NON-NLS-1$
//
//        addTipsField(createFieldContainer(parent));
//        tipsFadeDelayParent = createFieldContainer(parent);
//        addTipsFadeDelayField(tipsFadeDelayParent);

    }

    private void addTopicPositioningGroup() {
        Composite parent = createGroup(
                PrefMessages.EditorPage_TopicPositioning_title);
        addAllowOverlapsField(createFieldContainer(parent));
        addAllowManualLayoutField(createFieldContainer(parent));
        addAllowFreePositionField(createFieldContainer(parent));

        Label descriptionLabel = new Label(parent, SWT.WRAP);
        descriptionLabel.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, false, false));
        ((GridData) descriptionLabel.getLayoutData()).widthHint = 400;
        descriptionLabel.setText(
                PrefMessages.EditorPage_TopicPositioning_FreePositioning_description);
    }

    private void addAnimationField() {
        addField(new BooleanFieldEditor(PrefConstants.ANIMATION_ENABLED,
                PrefMessages.EditorPage_EnableAnimation_text,
                getFieldEditorParent()));
    }

    private void addShadowField() {
        addField(new BooleanFieldEditor(PrefConstants.SHADOW_ENABLED,
                PrefMessages.EditorPage_EnableShadow_text,
                getFieldEditorParent()));
    }

//    private void addGradientColorField() {
//        addField(new BooleanFieldEditor(PrefConstants.GRADIENT_COLOR,
//                PrefMessages.EditorPage_UndoRedo_gradientColor,
//                getFieldEditorParent()));
//    }

    private void addPreviewField() {
        addField(new BooleanFieldEditor(PrefConstants.PREVIEW_SKIPPED,
                PrefMessages.EditorPage_Preview_text, getFieldEditorParent()));
    }

    private Composite createGroup(String groupTitle) {
        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);

        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new GridLayout(1, false));
        group.setText(groupTitle);
        return group;
    }

    private Composite createFieldContainer(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(1, false));
        return composite;
    }

//    private void addTipsField(Composite parent) {
//        addField(new BooleanFieldEditor(PrefConstants.UNDO_REDO_TIPS_ENABLED,
//                PrefMessages.EditorPage_UndoRedo_tips, parent));
//    }
//
//    private void addTipsFadeDelayField(Composite parent) {
//        //tips fade delay
//        tipsFadeDelayField = new IntegerFieldEditor(
//                PrefConstants.UNDO_REDO_TIPS_FADE_DELAY,
//                PrefMessages.EditorPage_UndoRedo_tips_fade_delay, parent);
//        addField(tipsFadeDelayField);
//
//        tipsFadeDelayField.setEnabled(MindMapUIPlugin.getDefault()
//                .getPreferenceStore().getBoolean(
//                        PrefConstants.UNDO_REDO_TIPS_ENABLED),
//                tipsFadeDelayParent);
//    }

    // allow  overlap 
    private void addAllowOverlapsField(Composite parent) {
        addField(new BooleanFieldEditor(PrefConstants.OVERLAPS_ALLOWED,
                PrefMessages.EditorPage_TopicPositioning_AllowOverlaps,
                parent));
    }

    private void addAllowManualLayoutField(Composite parent) {
        addField(new BooleanFieldEditor(PrefConstants.MANUAL_LAYOUT_ALLOWED,
                PrefMessages.EditorPage_TopicPositioning_AllowManualLayout,
                parent));
    }

    private void addAllowFreePositionField(Composite parent) {
        addField(new BooleanFieldEditor(PrefConstants.FREE_POSITION_ALLOWED,
                PrefMessages.EditorPage_TopicPositioning_AllowFreePosition,
                parent));
    }

    private void addZoomField() {
        if (getPreferenceStore().getInt(PrefConstants.ZOOM_VALUE) == 0) {
//            int width = Display.getCurrent().getBounds().width;
//            if (width < 1366)
            getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 100);
//            else if (width <= 1920)
//                getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 120);
//            else
//                getPreferenceStore().setValue(PrefConstants.ZOOM_VALUE, 150);
        }

        addField(new IntegerFieldEditor(PrefConstants.ZOOM_VALUE,
                PrefMessages.EditorPage_Zoom_Scale_text,
                getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

//    public void propertyChange(PropertyChangeEvent event) {
//        super.propertyChange(event);
//        if (event.getSource() instanceof FieldEditor) {
//            FieldEditor fe = (FieldEditor) event.getSource();
//            if (event.getProperty().equals(FieldEditor.VALUE)) {
//                String prefName = fe.getPreferenceName();
//                if (PrefConstants.UNDO_REDO_TIPS_ENABLED.equals(prefName)) {
//                    tipsFadeDelayField.setEnabled(
//                            (Boolean) event.getNewValue(), tipsFadeDelayParent);
//                }
//            }
//        }
//    }
}
