/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
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
import org.xmind.ui.dialogs.IDialogConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.AbstractMindMapImportPage;
import org.xmind.ui.wizards.AbstractMindMapImportWizard;
import org.xmind.ui.wizards.MindMapImporter;

/**
 * @deprecated
 * @author Frank Shaka
 */
public class WorkbookImportWizard extends AbstractMindMapImportWizard {

    private static final String SETTINGS_ID = "org.xmind.ui.imports.workbook"; //$NON-NLS-1$

    private static final String PAGE_ID = "importWorkbook"; //$NON-NLS-1$

    private class WorkbookImportPage extends AbstractMindMapImportPage {

        protected WorkbookImportPage() {
            super(PAGE_ID,
                    MindMapMessages.WorkbookImportWizard_ImportXmindMap_Title);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 15;
            composite.setLayout(layout);
            setControl(composite);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));

//            Control destinationControl = createDestinationControl(composite);
//            destinationControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
//                    true, true));

            updateStatus();

            if (getCastedWizard().hasTargetWorkbook()) {
                parent.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        openBrowseDialog();
                    }
                });
            } else {
                disableControls(fileGroup);
                setMessage(
                        MindMapMessages.WorkbookImportWizard_NoTargetWorkbookMessage,
                        WARNING);
            }
        }

        private void disableControls(Control control) {
            control.setEnabled(false);
            if (control instanceof Composite) {
                Control[] controls = ((Composite) control).getChildren();
                for (int i = 0; i < controls.length; i++) {
                    disableControls(controls[i]);
                }
            }
        }

        protected FileDialog createBrowseDialog() {
            FileDialog dialog = super.createBrowseDialog();
            dialog.setFilterExtensions(new String[] { "*" //$NON-NLS-1$
                    + MindMapUI.FILE_EXT_XMIND });
            dialog.setFilterNames(new String[] { NLS.bind("{0} (*{1})", //$NON-NLS-1$
                    IDialogConstants.FILE_DIALOG_FILTER_WORKBOOK,
                    MindMapUI.FILE_EXT_XMIND) });
            return dialog;
        }

        @Override
        protected boolean isPageCompletable() {
            return super.isPageCompletable()
                    && getCastedWizard().hasTargetWorkbook();
        }
    }

    private WorkbookImportPage page;

    public WorkbookImportWizard() {
        IDialogSettings settings = MindMapUIPlugin.getDefault()
                .getDialogSettings().getSection(SETTINGS_ID);
        if (settings == null) {
            settings = MindMapUIPlugin.getDefault().getDialogSettings()
                    .addNewSection(SETTINGS_ID);
        }
        setDialogSettings(settings);
        setWindowTitle(
                MindMapMessages.WorkbookImportWizard_ImportWorkbook_Title);
    }

    public void addPages() {
        addPage(page = new WorkbookImportPage());
    }

    protected MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook) {
        return new WorkbookImporter(sourcePath, targetWorkbook);
    }

    protected String getApplicationId() {
        return "XMind Workbook"; //$NON-NLS-1$
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

    @Override
    public boolean isToNewWorkbook() {
        return false;
    }

}