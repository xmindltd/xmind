/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.wizards.AbstractMindMapImportPage;
import org.xmind.ui.wizards.AbstractMindMapImportWizard;
import org.xmind.ui.wizards.MindMapImporter;

public class XMind2008ImportWizard extends AbstractMindMapImportWizard {

    private static final String SETTINGS_ID = "org.xmind.ui.import.xmind2008"; //$NON-NLS-1$

    private static final String PAGE_ID = "importXMind2008"; //$NON-NLS-1$

    private static final String EXT = "*.xmap"; //$NON-NLS-1$

    private class XMind2008ImportPage extends AbstractMindMapImportPage {

        protected XMind2008ImportPage() {
            super(PAGE_ID, WizardMessages.XMind2008ImportPage_title);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 15;
            composite.setLayout(layout);
            setControl(composite);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));

            Control destinationControl = createDestinationControl(composite);
            destinationControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                    true, true));

            updateStatus();

            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    openBrowseDialog();
                }
            });
        }

        protected FileDialog createBrowseDialog() {
            FileDialog dialog = super.createBrowseDialog();
            dialog.setFilterExtensions(new String[] { EXT });
            dialog.setFilterNames(new String[] { NLS.bind(
                    WizardMessages.XMind2008MindImportPage_FilterName, EXT) });
            return dialog;
        }
    }

    private XMind2008ImportPage page;

    public XMind2008ImportWizard() {
        IDialogSettings settings = MindMapUIPlugin.getDefault()
                .getDialogSettings(SETTINGS_ID);
        setDialogSettings(settings);
        setWindowTitle(WizardMessages.XMind2008ImportWizard_windowTitle);
    }

    public void addPages() {
        addPage(page = new XMind2008ImportPage());
    }

    protected MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook) {
        return new XMind2008Importer(sourcePath, targetWorkbook);
    }

    protected String getApplicationId() {
        return "XMind2008"; //$NON-NLS-1$
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

}
