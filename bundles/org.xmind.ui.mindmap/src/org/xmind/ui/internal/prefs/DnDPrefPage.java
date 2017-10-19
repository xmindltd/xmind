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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.prefs.PrefConstants;

public class DnDPrefPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    IPreferenceStore pre = MindMapUIPlugin.getDefault().getPreferenceStore();

    private Button link;

    private Button copy;

    private Button alwaysRequest;

    public DnDPrefPage() {
        super();
    }

    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = createGroup(parent);

        Label label = new Label(composite, SWT.WRAP);
        label.setText(PrefMessages.DnDPrefPage_DnDLabel_Text);
        GridData data = new GridData(SWT.FILL, SWT.NONE, false, false);
        data.horizontalSpan = 1;
        label.setLayoutData(data);

        link = new Button(composite, SWT.RADIO);
        link.setText(PrefMessages.DnDPrefPage_LinkButton);
        data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 1;
        link.setLayoutData(data);

        copy = new Button(composite, SWT.RADIO);
        copy.setText(PrefMessages.DnDPrefPage_CopyButton);
        data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 1;
        copy.setLayoutData(data);

        alwaysRequest = new Button(composite, SWT.RADIO);
        alwaysRequest.setText(PrefMessages.DnDPrefPage_AlwaysRequestButton);
        data = new GridData(SWT.FILL, SWT.NONE, true, true);
        data.horizontalSpan = 1;
        alwaysRequest.setLayoutData(data);

        String externalFile = pre.getString(PrefConstants.ADD_EXTERNAL_FILE);
        link.setSelection(PrefConstants.CREATE_HYPERLINK.equals(externalFile));
        copy.setSelection(PrefConstants.CREATE_ATTACHMENT.equals(externalFile));
        alwaysRequest
                .setSelection(PrefConstants.ASK_USER.equals(externalFile)
                        || IPreferenceStore.STRING_DEFAULT_DEFAULT
                                .equals(externalFile));
        return parent;
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
        group.setText(PrefMessages.DnDPrefPage_DnDGroup_text);
        return group;
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected void performDefaults() {
        link.setSelection(false);
        copy.setSelection(false);
        alwaysRequest.setSelection(true);
        pre.setValue(PrefConstants.ADD_EXTERNAL_FILE, PrefConstants.ASK_USER);
        super.performDefaults();
    }

    public boolean performOk() {

        if (link.getSelection()) {
            pre.setValue(PrefConstants.ADD_EXTERNAL_FILE,
                    PrefConstants.CREATE_HYPERLINK);
        } else if (copy.getSelection()) {
            pre.setValue(PrefConstants.ADD_EXTERNAL_FILE,
                    PrefConstants.CREATE_ATTACHMENT);
        } else {
            pre.setValue(PrefConstants.ADD_EXTERNAL_FILE,
                    PrefConstants.ASK_USER);

        }
        String externalFile = pre.getString(PrefConstants.ADD_EXTERNAL_FILE);
        link.setSelection(PrefConstants.CREATE_HYPERLINK.equals(externalFile));
        copy.setSelection(PrefConstants.CREATE_ATTACHMENT.equals(externalFile));
        alwaysRequest
                .setSelection(PrefConstants.ASK_USER.equals(externalFile)
                        || IPreferenceStore.STRING_DEFAULT_DEFAULT
                                .equals(externalFile));

        return true;
    }
}