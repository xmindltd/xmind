package org.xmind.ui.internal.imports.novamind;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.imports.ImportMessages;
import org.xmind.ui.internal.imports.ImportPlugin;
import org.xmind.ui.wizards.AbstractMindMapImportPage;
import org.xmind.ui.wizards.AbstractMindMapImportWizard;
import org.xmind.ui.wizards.MindMapImporter;

/**
 * @author lyn
 */

public class NovaMindImportWizard extends AbstractMindMapImportWizard {

    private static final String SETTINGS_ID = "org.xmind.ui.imports.NovaMind"; //$NON-NLS-1$

    private static final String PAGE_ID = "importNovaMind"; //$NON-NLS-1$

    private static final String EXT = "*" + NMConstants.FILE_EXTENSION; //$NON-NLS-1$

    private class NovaMindImportPage extends AbstractMindMapImportPage {

        protected NovaMindImportPage() {
            super(PAGE_ID, ImportMessages.NovaMindImportPage_title);
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

            Control destinationControl = createDestinationControl(composite);
            destinationControl.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));

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
                    ImportMessages.NovaMindImportPage_FileDialog_Filter_name,
                    EXT) });
            return dialog;
        }

    }

    private NovaMindImportPage page;

    public NovaMindImportWizard() {
        IDialogSettings settings = ImportPlugin.getDefault().getDialogSettings()
                .getSection(SETTINGS_ID);
        if (settings == null) {
            settings = ImportPlugin.getDefault().getDialogSettings()
                    .addNewSection(SETTINGS_ID);
        }
        setDialogSettings(settings);
        setWindowTitle(ImportMessages.NovaMindImportWizard_windowTitle);
    }

    public void addPages() {
        addPage(page = new NovaMindImportPage());
    }

    protected MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook) {
        return new NovaMindImporter(sourcePath, targetWorkbook);
    }

    protected String getApplicationId() {
        return "NovaMind"; //$NON-NLS-1$
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

}
