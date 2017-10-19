package org.xmind.ui.internal.imports.opml;

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

public class OpmlImportWizard extends AbstractMindMapImportWizard {

    private static final String SETTINGS_ID = "org.xmind.ui.imports.Opml"; //$NON-NLS-1$
    private static final String PAGE_ID = "importOPML"; //$NON-NLS-1$
    private static final String DOCUMENT_FILTER = "*.opml"; //$NON-NLS-1$
    private static final String DOCUMENT_EXT = "*.opml"; //$NON-NLS-1$

    private class OpmlImportPage extends AbstractMindMapImportPage {

        protected OpmlImportPage() {
            super(PAGE_ID, ImportMessages.OpmlImportWizard_windowTitle);
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
            dialog.setFilterExtensions(new String[] { DOCUMENT_FILTER });
            dialog.setFilterNames(new String[] { NLS.bind(
                    ImportMessages.OpmlImportPage_FilterName, DOCUMENT_EXT) });
            return dialog;
        }
    }

    public OpmlImportWizard() {
        IDialogSettings settings = ImportPlugin.getDefault().getDialogSettings()
                .getSection(SETTINGS_ID);
        if (settings == null) {
            settings = ImportPlugin.getDefault().getDialogSettings()
                    .addNewSection(SETTINGS_ID);
        }
        setDialogSettings(settings);
    }

    public void addPages() {
        addPage(new OpmlImportPage());
    }

    @Override
    protected MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook) {
        return new OpmlImporter(sourcePath, targetWorkbook);
    }

    @Override
    protected String getApplicationId() {
        return "Opml"; //$NON-NLS-1$
    }
}
