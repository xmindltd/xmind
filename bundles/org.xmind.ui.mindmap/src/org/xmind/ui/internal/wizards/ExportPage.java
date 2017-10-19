package org.xmind.ui.internal.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardNode;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.xmind.ui.internal.MindMapMessages;

/**
 * Wizard page class from which an export wizard is selected.
 *
 * @since 3.2
 */
public class ExportPage extends ImportExportPage {

    private static final String STORE_SELECTED_EXPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
            + "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$

    private static final String STORE_EXPANDED_EXPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
            + "STORE_EXPANDED_EXPORT_CATEGORIES";   //$NON-NLS-1$

    private static final String STORE_RECENTLY_USED_WIZARD_IDS = DIALOG_SETTING_SECTION_NAME
            + "store_recently_used_wizard_ids";   //$NON-NLS-1$

    private static final String WIZARD_ID_SPLIT = ";"; //$NON-NLS-1$

    private static final int RECENTLY_USED_WIZARD_ID_SIZE = 5;

    private CategorizedWizardSelectionTree exportTree;

    /**
     * Constructor for export wizard selection page.
     *
     * @param aWorkbench
     * @param currentSelection
     */
    public ExportPage(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        super(aWorkbench, currentSelection);
        wizardsOrder = ImportExportPage.getWizardsOrder("export-wizards"); //$NON-NLS-1$
    }

    @Override
    protected void initialize() {
        workbench.getHelpSystem().setHelp(getControl(),
                IWorkbenchHelpContextIds.EXPORT_WIZARD_SELECTION_WIZARD_PAGE);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        final IPageChangingListener listener = new IPageChangingListener() {

            @Override
            public void handlePageChanging(PageChangingEvent event) {
                if (event.getCurrentPage() == ExportPage.this) {
                    IWizardDescriptor wizardDescriptor = ((WorkbenchWizardNode) getSelectedNode())
                            .getWizardElement();
                    String selectedId = wizardDescriptor.getId();

                    List<String> recentWizardIds = getRecentWizardIds();
                    if (recentWizardIds.contains(selectedId)) {
                        recentWizardIds.remove(selectedId);
                    }
                    if (recentWizardIds.size() > RECENTLY_USED_WIZARD_ID_SIZE
                            - 1) {
                        recentWizardIds
                                .remove(RECENTLY_USED_WIZARD_ID_SIZE - 1);
                    }
                    recentWizardIds.add(0, selectedId);

                    putRecentWizardIds(recentWizardIds);
                }
            }
        };
        ((WizardDialog) getContainer()).addPageChangingListener(listener);

        getControl().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (getContainer() != null) {
                    ((WizardDialog) getContainer())
                            .removePageChangingListener(listener);
                }
            }
        });
    }

    private List<String> getRecentWizardIds() {
        String recentIds = getDialogSettings()
                .get(STORE_RECENTLY_USED_WIZARD_IDS);
        if (recentIds != null && !recentIds.equals("")) { //$NON-NLS-1$
            String[] ids = recentIds.split(WIZARD_ID_SPLIT);
            return new ArrayList<String>(Arrays.asList(ids));
        }
        return new ArrayList<String>();
    }

    private void putRecentWizardIds(List<String> recentWizardIds) {
        String recentIds = ""; //$NON-NLS-1$
        if (recentWizardIds.size() >= 1) {
            recentIds += recentWizardIds.get(0);
        }
        if (recentWizardIds.size() >= 2) {
            for (int i = 1; i < recentWizardIds.size(); i++) {
                recentIds += WIZARD_ID_SPLIT + recentWizardIds.get(i);
            }
        }
        getDialogSettings().put(STORE_RECENTLY_USED_WIZARD_IDS, recentIds);
    }

    @Override
    protected Composite createTreeViewer(Composite parent) {
        IWizardCategory root = WorkbenchPlugin.getDefault()
                .getExportWizardRegistry().getRootCategory();
        root = insertRecentWizards(root);

        exportTree = new CategorizedWizardSelectionTree(root,
                WorkbenchMessages.ExportWizard_selectWizard);
        Composite exportComp = exportTree.createControl(parent);
        exportTree.getViewer()
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        listSelectionChanged(event.getSelection());
                    }
                });
        exportTree.getViewer()
                .addDoubleClickListener(new IDoubleClickListener() {
                    @Override
                    public void doubleClick(DoubleClickEvent event) {
                        treeDoubleClicked(event);
                    }
                });
        setTreeViewer(exportTree.getViewer());
        return exportComp;
    }

    private IWizardCategory insertRecentWizards(IWizardCategory root) {
        List<String> recentWizardIds = getRecentWizardIds();
        if (recentWizardIds.size() == 0) {
            return root;
        }

        List<IWizardDescriptor> wizardDescriptors = new ArrayList<IWizardDescriptor>();
        collectWizardDescriptors(wizardDescriptors, root);

        List<IWizardDescriptor> recentWizards = new ArrayList<IWizardDescriptor>();
        for (String wizardId : recentWizardIds) {
            for (IWizardDescriptor wizardDescriptor : wizardDescriptors) {
                if (wizardId.contains(wizardDescriptor.getId())) {
                    recentWizards.add(wizardDescriptor);
                    break;
                }
            }
        }

        if (recentWizards.size() == 0) {
            return root;
        }

        //create new root.
        WizardCollectionElement newRoot = new WizardCollectionElement("root", //$NON-NLS-1$
                null, "root", null);  //$NON-NLS-1$

        //add recent use
        WizardCollectionElement recentCategory = new WizardCollectionElement(
                RECENTLY_USED_CATEGORY_ID, null,
                MindMapMessages.ExportPage_Categore_Recent_name, newRoot);
        for (IWizardDescriptor wizardDescriptor : recentWizards) {
            WorkbenchWizardElement newDescriptor = new WorkbenchWizardElement(
                    ((WorkbenchWizardElement) wizardDescriptor)
                            .getConfigurationElement());
            newDescriptor.setParent(recentCategory);
            recentCategory.add(newDescriptor);
        }
        newRoot.add(recentCategory);

        //add old
        for (IWizardCategory wizardCategory : root.getCategories()) {
            newRoot.add((WizardCollectionElement) wizardCategory);
        }
        for (IWizardDescriptor wizardDescriptor : root.getWizards()) {
            newRoot.add(wizardDescriptor);
        }

        return newRoot;
    }

    private void collectWizardDescriptors(List<IWizardDescriptor> descriptors,
            IWizardCategory wizardCategory) {
        if (wizardCategory != null) {
            descriptors.addAll(Arrays.asList(wizardCategory.getWizards()));
            for (IWizardCategory category : wizardCategory.getCategories()) {
                collectWizardDescriptors(descriptors, category);
            }
        }
    }

    @Override
    public void saveWidgetValues() {
        storeExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES,
                exportTree.getViewer());
        storeSelectedCategoryAndWizard(STORE_SELECTED_EXPORT_WIZARD_ID,
                exportTree.getViewer());
        super.saveWidgetValues();
    }

    @Override
    protected void restoreWidgetValues() {
        IWizardCategory exportRoot = WorkbenchPlugin.getDefault()
                .getExportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES,
                exportRoot, exportTree.getViewer());
        selectPreviouslySelected(STORE_SELECTED_EXPORT_WIZARD_ID, exportRoot,
                exportTree.getViewer());
        super.restoreWidgetValues();
    }

    @Override
    protected ITriggerPoint getTriggerPoint() {
        return getWorkbench().getActivitySupport().getTriggerPointManager()
                .getTriggerPoint(WorkbenchTriggerPoints.EXPORT_WIZARDS);
    }

    @Override
    protected void updateMessage() {
        setMessage(WorkbenchMessages.ImportExportPage_chooseExportWizard);
        super.updateMessage();
    }

    @Override
    protected void expandPreviouslyExpandedCategories(String setting,
            IWizardCategory wizardCategories, TreeViewer viewer) {
        String[] expandedCategoryPaths = getDialogSettings().getArray(setting);
        if (expandedCategoryPaths == null
                || expandedCategoryPaths.length == 0) {
            return;
        }

        List<String> idList = Arrays.asList(expandedCategoryPaths);
        for (IWizardCategory category : wizardCategories.getCategories()) {
            if (!idList.contains(category.getId())) {
                viewer.setExpandedState(wizardCategories
                        .findCategory(new Path(category.getId())), false);
            }
        }
    }
}
