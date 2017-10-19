package org.xmind.ui.internal.imports.lighten;

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

public class LightenImportWizard extends AbstractMindMapImportWizard {

    private static final String SETTINGS_ID = "org.xmind.ui.imports.Lighten"; //$NON-NLS-1$

    private final static String PAGE_ID = "importLighten"; //$NON-NLS-1$

    private final static String FILE_FORMAT = "*.lighten"; //$NON-NLS-1$

    public LightenImportWizard() {
        IDialogSettings settings = ImportPlugin.getDefault().getDialogSettings()
                .getSection(SETTINGS_ID);
        if (settings == null) {
            settings = ImportPlugin.getDefault().getDialogSettings()
                    .addNewSection(SETTINGS_ID);
        }
        setDialogSettings(settings);
    }

    @Override
    protected MindMapImporter createImporter(String sourcePath,
            IWorkbook targetWorkbook) {
        return new LightenImporter(sourcePath, targetWorkbook);
    }

    @Override
    protected String getApplicationId() {
        return "Lighten"; //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        addPage(new LightenImportPage());
    }

    private class LightenImportPage extends AbstractMindMapImportPage {

        protected LightenImportPage() {
            super(PAGE_ID, ImportMessages.LightenImportPage_title);
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

        @Override
        protected FileDialog createBrowseDialog() {
            FileDialog dialog = super.createBrowseDialog();
            dialog.setFilterExtensions(new String[] { FILE_FORMAT });
            dialog.setFilterNames(new String[] {
                    NLS.bind(ImportMessages.LightenImportPage_FilterName,
                            FILE_FORMAT) });
            return dialog;
        }
    }

}
