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
package org.xmind.ui.internal.browser;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BrowserPrefPage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private Button internal;

    private Button external;

    public BrowserPrefPage() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
     * .swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
        layout.verticalSpacing = convertVerticalDLUsToPixels(3);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(data);

        Label label = new Label(composite, SWT.WRAP);
        label.setText(BrowserMessages.BrowserPrefPage_description);
        data = new GridData(SWT.FILL, SWT.NONE, false, false);
        data.horizontalSpan = 2;
        data.widthHint = 275;
        label.setLayoutData(data);

        internal = new Button(composite, SWT.RADIO);
        internal.setText(BrowserMessages.BrowserPrefPage_InternalBrowser_text);
        data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 2;
        internal.setLayoutData(data);

        if (!BrowserUtil.canUseInternalWebBrowser())
            internal.setEnabled(false);

        external = new Button(composite, SWT.RADIO);
        external.setText(BrowserMessages.BrowserPrefPage_ExternalBrowser_text);
        data = new GridData(SWT.FILL, SWT.NONE, true, false);
        data.horizontalSpan = 2;
        external.setLayoutData(data);

        internal.setSelection(BrowserPref.getBrowserChoice() == BrowserPref.INTERNAL);
        external.setSelection(BrowserPref.getBrowserChoice() == BrowserPref.EXTERNAL);
        Dialog.applyDialogFont(composite);
        return composite;
    }

    public void init(IWorkbench workbench) {
        // do nothing
    }

    /**
     * 
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible)
            setTitle(BrowserMessages.BrowserPrefPage_title);
    }

    /**
     * Performs special processing when this page's Defaults button has been
     * pressed.
     */
    protected void performDefaults() {
        internal.setSelection(!BrowserPref.isDefaultUseSystemBrowser());
        external.setSelection(BrowserPref.isDefaultUseSystemBrowser());
        super.performDefaults();
    }

    /**
     * Method declared on IPreferencePage. Subclasses should override
     */
    public boolean performOk() {
        int choice;
        if (internal.getSelection())
            choice = BrowserPref.INTERNAL;
        else
            choice = BrowserPref.EXTERNAL;
        BrowserPref.setBrowserChoice(choice);
        return true;
    }
}