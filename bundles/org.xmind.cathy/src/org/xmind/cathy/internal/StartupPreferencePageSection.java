package org.xmind.cathy.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.core.usagedata.IUsageDataSampler;
import org.xmind.core.usagedata.IUsageDataUploader;
import org.xmind.ui.preference.PreferenceFieldEditorPageSection;
import org.xmind.ui.resources.ColorUtils;

public class StartupPreferencePageSection extends
        PreferenceFieldEditorPageSection implements IWorkbenchPreferencePage {

    private Composite container;
    private Button startupActionButton;

    private LocalResourceManager resources;

    @Override
    protected Control createContents(Composite parent) {
        if (null == container)
            this.container = parent;
        return super.createContents(parent);
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return CathyPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected void initialize() {
        super.initialize();
        int startupAction = getPreferenceStore()
                .getInt(CathyPlugin.STARTUP_ACTION);
        startupActionButton
                .setSelection(startupAction == CathyPlugin.STARTUP_ACTION_LAST);
    }

    @Override
    public void createControl(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);
        super.createControl(parent);
    }

    @Override
    protected void createFieldEditors() {
        addStartupGroup(container);
        if (isShowUploadDataCheck()) {
            addSendUsageDataGroup(container);
        }
        this.initialize();
    }

    private void addStartupGroup(Composite parent) {

        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(container);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(container);

        startupActionButton = new Button(container, SWT.CHECK);
        startupActionButton.setText(WorkbenchMessages.RestoreLastSession_label);

        if (isShowUploadDataCheck()) {
            addField(
                    new BooleanFieldEditor(CathyPlugin.CHECK_UPDATES_ON_STARTUP,
                            WorkbenchMessages.CheckUpdates_label, container));
        }
    }

    private boolean isShowUploadDataCheck() {
        return !Boolean.getBoolean(CathyPlugin.KEY_NOT_SHOW_UPLOAD_DATA_CHECK);
    }

    private void addSendUsageDataGroup(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().indent(25, 0).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(2).spacing(5, 0)
                .applyTo(composite);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));
        GridLayoutFactory.fillDefaults().applyTo(composite2);

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
                (Color) resources.get(ColorUtils.toDescriptor("#006CF9"))); //$NON-NLS-1$
        privacyHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                Program.launch(LinkUtils.getLinkByLanguage(true, false,
                        "/privacy/usage/")); //$NON-NLS-1$
            }
        });

        composite.setFocus();

        if (CathyPlugin.getDefault()
                .isDebugging("/debug/udc/showUploadButton")) { //$NON-NLS-1$
            Button uploadButton = new Button(composite, SWT.PUSH);
            GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
                    .span(2, 0).indent(10, 0).minSize(100, 0)
                    .applyTo(uploadButton);
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

        return true;
    }
}
