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
package org.xmind.cathy.internal;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.core.usagedata.IUsageDataSampler;
import org.xmind.core.usagedata.IUsageDataUploader;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.NumberUtils;

public class GeneralPrefPage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, Listener {

//    private static class SoftCheckFileFieldEditor
//            extends StringButtonFieldEditor {
//
//        private String[] extensions = null;
//
//        private String[] extensionNames = null;
//
//        public SoftCheckFileFieldEditor(String name, String labelText,
//                Composite parent) {
//            init(name, labelText);
//            setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
//            createControl(parent);
//        }
//
//        @Override
//        public void setEmptyStringAllowed(boolean b) {
//            super.setEmptyStringAllowed(b);
//            refreshValidState();
//        }
//
//        @Override
//        protected boolean checkState() {
//            Text text = getTextControl();
//            if (text == null)
//                return false;
//            boolean validFile;
//            String path = text.getText();
//            if ("".equals(path)) { //$NON-NLS-1$
//                validFile = isEmptyStringAllowed();
//            } else {
//                validFile = new File(path).isFile();
//            }
//            if (validFile) {
//                getPage().setMessage(null);
//            } else {
//                getPage().setMessage(getErrorMessage(),
//                        IMessageProvider.WARNING);
//            }
//            return validFile;
//        }
//
//        @Override
//        protected String changePressed() {
//            File f = new File(getTextControl().getText());
//            if (!f.exists()) {
//                f = null;
//            }
//            File d = getFile(f);
//            if (d == null) {
//                return null;
//            }
//            return d.getAbsolutePath();
//        }
//
//        /**
//         * Helper to open the file chooser dialog.
//         * 
//         * @param startingDirectory
//         *            the directory to open the dialog on.
//         * @return File The File the user selected or <code>null</code> if they
//         *         do not.
//         */
//        private File getFile(File startingDirectory) {
//            FileDialog dialog = new FileDialog(getShell(),
//                    SWT.OPEN | SWT.SHEET);
//            if (extensions != null) {
//                dialog.setFilterExtensions(extensions);
//                if (extensionNames != null) {
//                    dialog.setFilterNames(extensionNames);
//                }
//            }
//            if (startingDirectory != null) {
//                dialog.setFileName(startingDirectory.getPath());
//            }
//            String file = dialog.open();
//            if (file != null) {
//                file = file.trim();
//                if (file.length() > 0) {
//                    return new File(file);
//                }
//            }
//
//            return null;
//        }
//
//        public void setExtensions(String[] extensions) {
//            this.extensions = extensions;
//        }
//
//        public void setExtensionNames(String[] extensionNames) {
//            this.extensionNames = extensionNames;
//        }
//
//    }

//    private IntegerFieldEditor autoSaveIntervalsField;
//
//    private Composite autoSaveIntervalsParent;

    private Text autoSaveIntervalsInput;

    private boolean autoBackup = true;

    private BooleanFieldEditor autoBackupField;

    private IntegerFieldEditor recentFilesField;

//    private Combo startupActionCombo;
//
//    private SoftCheckFileFieldEditor homeMapField;
//
//    private Control homeMapControl;

    private Control recentFilesControl;

    private Button startupActionButton;

    private ResourceManager resources;

    public GeneralPrefPage() {
        super(WorkbenchMessages.GeneralPrefPage_title, FLAT);
    }

    @Override
    public void applyData(Object data) {
        if (IPreferenceConstants.RECENT_FILES.equals(data)) {
            if (recentFilesControl != null
                    && !recentFilesControl.isDisposed()) {
                recentFilesControl.setFocus();
                highlight(recentFilesControl.getParent());
            }
        }
    }

    private void highlight(final Control control) {
        final Display display = control.getDisplay();
        final Color oldBackground = control.getBackground();
        final Color c1 = oldBackground == null
                ? display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
                : oldBackground;
        final int r1 = c1.getRed(), g1 = c1.getGreen(), b1 = c1.getBlue();
        final int r0 = 255, g0 = 240, b0 = 180;
        final int total = 30;
        final int[] step = new int[] { 0 };
        final Color[] c = new Color[1];
        display.timerExec(500, new Runnable() {
            public void run() {
                if (control.isDisposed())
                    return;
                c[0] = new Color(display, r0, g0, b0);
                control.setBackground(c[0]);
                display.timerExec(20, new Runnable() {
                    public void run() {
                        c[0].dispose();

                        if (control.isDisposed())
                            return;

                        ++step[0];
                        if (step[0] > total) {
                            control.setBackground(null);
                            return;
                        }

                        int x = step[0], y = total - step[0];
                        int r = (r0 * y + r1 * x) / total;
                        int g = (g0 * y + g1 * x) / total;
                        int b = (b0 * y + b1 * x) / total;
                        c[0] = new Color(display, r, g, b);
                        control.setBackground(c[0]);
                        display.timerExec(20, this);
                    }
                });
            }
        });
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    @Override
    public void createControl(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);
        super.createControl(parent);
    }

    protected Control createContents(Composite parent) {
        Composite composite = (Composite) super.createContents(parent);
        ((GridLayout) composite.getLayout()).verticalSpacing = 15;
        return composite;
    }

    protected void createFieldEditors() {
        addStartupGroup();
        addRecentFileCountField();
        addAutoSaveGroup();
        addAutoBackupGroup();
        //addRememberLastSessionField();
        //addCheckUpdatesField();
        addSendUsageDataGroup();
    }

    private void addStartupGroup() {
        Composite parent = createGroup(WorkbenchMessages.Startup_title);
        addStartupActionField(parent);
        addCheckUpdatesField(parent);
    }

    private void addStartupActionField(Composite parent) {
        startupActionButton = new Button(parent, SWT.CHECK);
        startupActionButton.setText(WorkbenchMessages.RestoreLastSession_label);
//        addField(new BooleanFieldEditor(CathyPlugin.RESTORE_LAST_SESSION,
//                WorkbenchMessages.StartupAction_LastSession,
//                createFieldContainer(parent, false)));
//        Composite line = new Composite(parent, SWT.NONE);
//        line.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//        GridLayout layout = new GridLayout(2, false);
//        layout.marginHeight = 0;
//        layout.marginWidth = 0;
//        line.setLayout(layout);
//        fillStartupActionFields(line);
    }

//    private void fillStartupActionFields(Composite parent) {
//        Label label = new Label(parent, SWT.NONE);
//        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
//        label.setText(WorkbenchMessages.StartupAction_label);
//
//        startupActionCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
//        startupActionCombo
//                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        startupActionCombo.add(WorkbenchMessages.StartupAction_OpenDialog);
//        startupActionCombo.add(WorkbenchMessages.StartupAction_BlankMap);
//        startupActionCombo.add(WorkbenchMessages.StartupAction_HomeMap);
//        startupActionCombo.add(WorkbenchMessages.StartupAction_LastSession);
//        startupActionCombo.addListener(SWT.Selection, this);
//    }

//    private void addHomeMapField(Composite parent) {
//        Composite line = new Composite(parent, SWT.NONE);
//        line.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//        GridLayout layout = new GridLayout(2, false);
//        layout.marginHeight = 0;
//        layout.marginWidth = 0;
//        line.setLayout(layout);
//        fillHomeMapFields(line);
//    }

//    private void fillHomeMapFields(Composite parent) {
//        Composite container = createFieldContainer(parent, true);
//        addField(homeMapField = new SoftCheckFileFieldEditor(
//                PrefConstants.HOME_MAP_LOCATION,
//                WorkbenchMessages.HomeMap_label, container));
//        homeMapControl = homeMapField.getTextControl(container);
//        homeMapField.setErrorMessage(WorkbenchMessages.HomeMap_NotFound_error);
//        String xmindExt = "*" + MindMapUI.FILE_EXT_XMIND; //$NON-NLS-1$
//        homeMapField.setExtensions(new String[] { xmindExt });
//        homeMapField.setExtensionNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
//                DialogMessages.WorkbookFilterName, xmindExt) });
//    }

    private void addCheckUpdatesField(Composite parent) {
        addField(new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
                WorkbenchMessages.CheckUpdates_label,
                createFieldContainer(parent, true)));
    }

    private void addRecentFileCountField() {
        Composite container = getFieldEditorParent();
        addField(recentFilesField = new IntegerFieldEditor(
                IPreferenceConstants.RECENT_FILES,
                WorkbenchMessages.RecentFiles_label, container));
        recentFilesControl = recentFilesField.getTextControl(container);
    }

//    private void addRememberLastSessionField() {
//        addField(new BooleanFieldEditor(CathyPlugin.RESTORE_LAST_SESSION,
//                WorkbenchMessages.RestoreLastSession_label,
//                getFieldEditorParent()));
//    }
//
//    private void addCheckUpdatesField() {
//        addField(new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
//                WorkbenchMessages.CheckUpdates_label, getFieldEditorParent()));
//    }

    private void addAutoSaveGroup() {
        String message = WorkbenchMessages.AutoSave_label2;
        int index = message.indexOf("{0}"); //$NON-NLS-1$
        int cols = 3;
        String label1, label2;
        if (index >= 0) {
            label1 = message.substring(0, index);
            label2 = message.substring(index + 3);
            if ("".equals(label2)) { //$NON-NLS-1$
                label2 = null;
                cols--;
            }
        } else {
            label1 = message;
            label2 = null;
            cols--;
            if ("".equals(label1)) {//$NON-NLS-1$
                label1 = null;
                cols--;
            }
        }

        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(cols, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);
//        parent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
//                ((GridData) composite.getLayoutData()).horizontalIndent = 10;

        Composite booleanParent = new Composite(parent, SWT.NONE);
        booleanParent
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
        addField(new BooleanFieldEditor(CathyPlugin.AUTO_SAVE_ENABLED, label1,
                booleanParent));

        autoSaveIntervalsInput = new Text(parent,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        autoSaveIntervalsInput
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
        ((GridData) autoSaveIntervalsInput.getLayoutData()).widthHint = 40;
        autoSaveIntervalsInput.setEnabled(
                getPreferenceStore().getBoolean(CathyPlugin.AUTO_SAVE_ENABLED));

        if (label2 != null) {
            Label label = new Label(parent, SWT.NONE);
            label.setText(label2);
            label.setLayoutData(
                    new GridData(SWT.FILL, SWT.CENTER, false, true));
        }
    }

//    private void addAutoSaveGroup() {
//        Composite parent = getFieldEditorParent();
//        GridLayout gridLayout = new GridLayout(2, false);
//        gridLayout.marginWidth = 0;
//        gridLayout.marginHeight = 0;
//        gridLayout.verticalSpacing = 0;
//        gridLayout.horizontalSpacing = 0;
//        parent.setLayout(gridLayout);
//
////        PreferenceLinkArea link = new PreferenceLinkArea(parent,
////                SWT.NONE,
////                "org.xmind.ui.BackupPrefPage", //$NON-NLS-1$
////                "See <a>''{0}''</a> for auto saving and backup options.",
////                (IWorkbenchPreferenceContainer) getContainer(), null);
////        link.getControl().setLayoutData(
////                new GridData(SWT.FILL, SWT.FILL, true, false));
//
//
//        addField(new BooleanFieldEditor(CathyPlugin.AUTO_SAVE_ENABLED,
//                WorkbenchMessages.AutoSave_label, createFieldContainer(parent,
//                        false)));
//
//        autoSaveIntervalsParent = createFieldContainer(parent, true);
//
//        addField(autoSaveIntervalsField = new IntegerFieldEditor(
//                CathyPlugin.AUTO_SAVE_INTERVALS, "", //$NON-NLS-1$
//                autoSaveIntervalsParent));
//
//        autoSaveIntervalsField.setEnabled(
//                getPreferenceStore().getBoolean(CathyPlugin.AUTO_SAVE_ENABLED),
//                autoSaveIntervalsParent);
//
//        Label label = new Label(parent, SWT.NONE);
//        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
//                false));
//        label.setText(WorkbenchMessages.AutoSave_Minutes);
//
//    }

    private void addAutoBackupGroup() {
        Composite parent = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        parent.setLayout(gridLayout);
        addField(autoBackupField = new BooleanFieldEditor(
                PrefConstants.AUTO_BACKUP_ENABLE,
                WorkbenchMessages.AutoBackup_label,
                createFieldContainer(parent, true)));
    }

    private void addSendUsageDataGroup() {
        Composite composite = getFieldEditorParent();
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));

        GridLayout gridLayout2 = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite2.setLayout(gridLayout2);

        addField(new BooleanFieldEditor(
                CathyPlugin.USAGE_DATA_UPLOADING_ENABLED,
                WorkbenchMessages.GeneralPrefPage_usageData_text, composite2));

        //
        Hyperlink privacyHyperlink = new Hyperlink(composite, SWT.NONE);
        privacyHyperlink.setBackground(composite.getBackground());
        privacyHyperlink.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));
        privacyHyperlink
                .setText(WorkbenchMessages.GeneralPrefPage_seePolicy_link);
        privacyHyperlink.setUnderlined(true);
        privacyHyperlink.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#77afe0"))); //$NON-NLS-1$
        privacyHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                Program.launch(
                        LinkUtils.getLinkByLanguage(true, true, "/privacy/")); //$NON-NLS-1$
            }
        });

        if (CathyPlugin.getDefault()
                .isDebugging("/debug/udc/showUploadButton")) { //$NON-NLS-1$
            Button uploadButton = new Button(composite, SWT.PUSH);
            GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false);
            layoutData.horizontalSpan = 2;
            layoutData.horizontalIndent = 10;
            layoutData.minimumWidth = 100;
            uploadButton.setLayoutData(layoutData);
            uploadButton.setBackground(composite.getBackground());
            uploadButton.setText("Upload Now"); //$NON-NLS-1$
            uploadButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    IUsageDataSampler sampler = CathyPlugin.getDefault()
                            .getUsageDataCollector();
                    if (sampler instanceof IUsageDataUploader) {
                        ((IUsageDataUploader) sampler).forceUpload();
                    }
                }
            });
        }
    }

    private Composite createFieldContainer(Composite parent,
            boolean grabHorizontal) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, grabHorizontal, true));
        composite.setLayout(new GridLayout(1, false));
        return composite;
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

    @Override
    protected void initialize() {
        super.initialize();
        int startupAction = getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION);
        startupActionButton
                .setSelection(startupAction == CathyPlugin.STARTUP_ACTION_LAST);
//        startupActionCombo.select(startupAction);

//        homeMapField.setPreferenceStore(
//                MindMapUIPlugin.getDefault().getPreferenceStore());
//        homeMapField.setEmptyStringAllowed(
//                startupAction != CathyPlugin.STARTUP_ACTION_HOME);
//        homeMapField.load();

        recentFilesField.setPreferenceStore(
                WorkbenchPlugin.getDefault().getPreferenceStore());
        recentFilesField.load();

        autoSaveIntervalsInput.setText(String.valueOf(
                getPreferenceStore().getInt(CathyPlugin.AUTO_SAVE_INTERVALS)));
        autoBackupField.setPreferenceStore(
                MindMapUIPlugin.getDefault().getPreferenceStore());
        autoBackupField.load();
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    public boolean performOk() {
        if (!super.performOk())
            return false;

        if (startupActionButton.getSelection()) {
            getPreferenceStore().setValue(CathyPlugin.STARTUP_ACTION,
                    CathyPlugin.STARTUP_ACTION_LAST);
        } else {
            getPreferenceStore().setValue(CathyPlugin.STARTUP_ACTION,
                    CathyPlugin.STARTUP_ACTION_WIZARD);
        }

        int autoSaveIntervals = NumberUtils
                .safeParseInt(autoSaveIntervalsInput.getText(), 0);
        getPreferenceStore().setValue(CathyPlugin.AUTO_SAVE_INTERVALS,
                autoSaveIntervals);
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .setValue(PrefConstants.AUTO_BACKUP_ENABLE, autoBackup);
        return true;
    }

    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getSource() instanceof FieldEditor) {
            FieldEditor fe = (FieldEditor) event.getSource();
            if (event.getProperty().equals(FieldEditor.VALUE)) {
                String prefName = fe.getPreferenceName();
                if (CathyPlugin.AUTO_SAVE_ENABLED.equals(prefName)) {
                    autoSaveIntervalsInput.setEnabled(
                            ((Boolean) event.getNewValue()).booleanValue());
//                    autoSaveIntervalsField.setEnabled(
//                            (Boolean) event.getNewValue(),
//                            autoSaveIntervalsParent);
                } else if (PrefConstants.AUTO_BACKUP_ENABLE.equals(prefName)) {
                    autoBackup = ((Boolean) event.getNewValue()).booleanValue();
                }
            }
        }
    }

    public void handleEvent(Event event) {
//        if (event.widget == startupActionCombo) {
//            int startupAction = startupActionCombo.getSelectionIndex();
//            homeMapField.setEmptyStringAllowed(
//                    startupAction != CathyPlugin.STARTUP_ACTION_HOME);
//            checkState();
//        }
    }
}
