package org.xmind.ui.internal.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

/**
 * The import/export wizard allows users to choose whether to show the import
 * wizard or the export wizard.
 *
 * @since 3.2
 */
public class ImportExportWizard extends Wizard {
    /**
     * Constant used to to specify to the import/export wizard which page should
     * initially be shown.
     */
    public static final String IMPORT = "import";   //$NON-NLS-1$
    /**
     * Constant used to to specify to the import/export wizard which page should
     * initially be shown.
     */
    public static final String EXPORT = "export";   //$NON-NLS-1$

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private ImportExportPage importExportPage;
    private String page = null;

    /**
     * Create an import/export wizard and show the page with the given id.
     *
     * @param pageId
     */
    public ImportExportWizard(String pageId) {
        page = pageId;
    }

    /**
     * Subclasses must implement this <code>IWizard</code> method to perform any
     * special finish processing for their wizard.
     */
    @Override
    public boolean performFinish() {
        importExportPage.saveWidgetValues();
        return true;
    }

    /**
     * Creates the wizard's pages lazily.
     */
    @Override
    public void addPages() {
        if (page.equals(IMPORT)) {
            importExportPage = new ImportPage(this.workbench, this.selection);
        } else if (page.equals(EXPORT)) {
            importExportPage = new ExportPage(this.workbench, this.selection);
        }
        if (importExportPage != null) {
            addPage(importExportPage);
        }
    }

    /**
     * Initializes the wizard.
     *
     * @param aWorkbench
     *            the workbench
     * @param currentSelection
     *            the current selection
     */
    public void init(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        this.workbench = aWorkbench;
        this.selection = currentSelection;

        ImageDescriptor wizardBannerImage = null;
        if (IMPORT.equals(page)) {
            wizardBannerImage = MindMapUI.getImages()
                    .getWizBan(IMindMapImages.WIZ_IMPORT);
            setWindowTitle(WorkbenchMessages.ImportWizard_title);
        } else if (EXPORT.equals(page)) {
            wizardBannerImage = MindMapUI.getImages()
                    .getWizBan(IMindMapImages.WIZ_EXPORT);
            setWindowTitle(WorkbenchMessages.ExportWizard_title);
        }
        if (wizardBannerImage != null) {
            setDefaultPageImageDescriptor(wizardBannerImage);
        }
        setNeedsProgressMonitor(true);
    }
}
