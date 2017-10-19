package org.xmind.ui.internal.wizards;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.wizards.IWizardCategory;

/**
 * Wizard page class from which an import wizard is selected.
 *
 * @since 3.2
 */
public class ImportPage extends ImportExportPage {
    private static final String STORE_SELECTED_IMPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
            + "STORE_SELECTED_IMPORT_WIZARD_ID"; //$NON-NLS-1$

    private static final String STORE_EXPANDED_IMPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
            + "STORE_EXPANDED_IMPORT_CATEGORIES";   //$NON-NLS-1$

    protected CategorizedWizardSelectionTree importTree;

    /**
     * Constructor for import wizard selection page.
     *
     * @param aWorkbench
     * @param currentSelection
     */
    public ImportPage(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        super(aWorkbench, currentSelection);
        wizardsOrder = ImportExportPage.getWizardsOrder("import-wizards"); //$NON-NLS-1$
    }

    @Override
    protected void initialize() {
        workbench.getHelpSystem().setHelp(getControl(),
                IWorkbenchHelpContextIds.IMPORT_WIZARD_SELECTION_WIZARD_PAGE);
    }

    @Override
    protected Composite createTreeViewer(Composite parent) {
        IWizardCategory root = WorkbenchPlugin.getDefault()
                .getImportWizardRegistry().getRootCategory();
        importTree = new CategorizedWizardSelectionTree(root,
                WorkbenchMessages.ImportWizard_selectWizard);
        Composite importComp = importTree.createControl(parent);
        importTree.getViewer()
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        listSelectionChanged(event.getSelection());
                    }
                });
        importTree.getViewer()
                .addDoubleClickListener(new IDoubleClickListener() {
                    @Override
                    public void doubleClick(DoubleClickEvent event) {
                        treeDoubleClicked(event);
                    }
                });
        setTreeViewer(importTree.getViewer());
        return importComp;
    }

    @Override
    public void saveWidgetValues() {
        storeExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES,
                importTree.getViewer());
        storeSelectedCategoryAndWizard(STORE_SELECTED_IMPORT_WIZARD_ID,
                importTree.getViewer());
        super.saveWidgetValues();
    }

    @Override
    protected void restoreWidgetValues() {
        IWizardCategory importRoot = WorkbenchPlugin.getDefault()
                .getImportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES,
                importRoot, importTree.getViewer());
        selectPreviouslySelected(STORE_SELECTED_IMPORT_WIZARD_ID, importRoot,
                importTree.getViewer());
        super.restoreWidgetValues();
    }

    @Override
    protected ITriggerPoint getTriggerPoint() {
        return getWorkbench().getActivitySupport().getTriggerPointManager()
                .getTriggerPoint(WorkbenchTriggerPoints.IMPORT_WIZARDS);
    }

    @Override
    protected void updateMessage() {
        setMessage(WorkbenchMessages.ImportExportPage_chooseImportWizard);
        super.updateMessage();
    }

}
