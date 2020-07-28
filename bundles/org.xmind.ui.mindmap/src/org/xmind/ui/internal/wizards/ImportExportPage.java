package org.xmind.ui.internal.wizards;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.dialogs.WizardActivityFilter;
import org.eclipse.ui.internal.dialogs.WizardContentProvider;
import org.eclipse.ui.internal.dialogs.WizardPatternFilter;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardNode;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardSelectionPage;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.io.BundleResource;
import org.xmind.core.util.DOMUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.XMLUtils;

/**
 * Abstract wizard page class from which an import or export wizard can be
 * chosen.
 *
 * @since 3.2
 */
public abstract class ImportExportPage extends WorkbenchWizardSelectionPage {

    public static final String RECENTLY_USED_CATEGORY_ID = "recentlyUsed"; //$NON-NLS-1$

    protected static final String DIALOG_SETTING_SECTION_NAME = "ImportExportPage."; //$NON-NLS-1$

    private static final String WIZARDS_ORDER_XML_PATH = "src/org/xmind/ui/internal/wizards/" //$NON-NLS-1$
            + "wizards_order.xml"; //$NON-NLS-1$

    protected static class Category {

        private String id;

        private List<String> descriptorIds;

        public Category(String id, List<String> descriptorIds) {
            this.id = id;
            this.descriptorIds = descriptorIds;
        }

        public String getId() {
            return id;
        }

        public List<String> getDescriptorIds() {
            return descriptorIds;
        }
    }

    private static class DataTransferWizardCollectionComparator
            extends ViewerComparator {

        public final static DataTransferWizardCollectionComparator INSTANCE = new DataTransferWizardCollectionComparator();

        private List<Object> wizardsOrder;

        private DataTransferWizardCollectionComparator() {
            super();
        }

        public void setWizardsOrder(List<Object> wizardsOrder) {
            this.wizardsOrder = wizardsOrder;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 instanceof IWizardCategory
                    && e2 instanceof IWizardCategory) {
                return indexOf((IWizardCategory) e1)
                        - indexOf((IWizardCategory) e2);

            } else if (e1 instanceof IWizardDescriptor
                    && e2 instanceof IWizardDescriptor) {
                //sort recently used
                if (RECENTLY_USED_CATEGORY_ID
                        .equals(((IWizardDescriptor) e1).getCategory().getId())
                        && RECENTLY_USED_CATEGORY_ID
                                .equals(((IWizardDescriptor) e2).getCategory()
                                        .getId())) {
                    return 1;
                } else {
                    return indexOf((IWizardDescriptor) e1)
                            - indexOf((IWizardDescriptor) e2);
                }
            }

            return super.compare(viewer, e1, e2);
        }

        private int indexOf(Object object) {
            if (object instanceof IWizardCategory) {
                IWizardCategory wizardCategory = (IWizardCategory) object;

                for (Object category : wizardsOrder) {
                    if (((Category) category).getId()
                            .equals(wizardCategory.getId())) {
                        return wizardsOrder.indexOf(category);
                    }
                }

            } else if (object instanceof IWizardDescriptor) {
                IWizardCategory wizardCategory = ((IWizardDescriptor) object)
                        .getCategory();

                for (Object category : wizardsOrder) {
                    if (((Category) category).getId()
                            .equals(wizardCategory.getId())) {

                        return ((Category) category).getDescriptorIds()
                                .indexOf(((IWizardDescriptor) object).getId());
                    }
                }

            }

            return -1;
        }
    }

    private static class InternalFilteredTree extends FilteredTree {

        private ResourceManager resources;

        private Label messageLabel;

        public InternalFilteredTree(Composite parent, int treeStyle,
                PatternFilter filter, String message) {
            super(parent, treeStyle, filter, true);
            messageLabel.setText(message);
        }

        protected void createControl(Composite parent, int treeStyle) {
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.verticalSpacing = 10;
            setLayout(layout);

            if (parent.getLayout() instanceof GridLayout) {
                setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            }

            if (showFilterControls) {
                Composite composite = new Composite(this, SWT.NONE);
                resources = new LocalResourceManager(
                        JFaceResources.getResources(), composite);

                composite.setBackground(parent.getBackground());
                composite.setLayoutData(
                        new GridData(SWT.FILL, SWT.TOP, true, false));
                composite.setFont(parent.getFont());
                GridLayout layout2 = new GridLayout(2, false);
                layout2.marginWidth = 0;
                layout2.marginHeight = 0;
                layout2.horizontalSpacing = 32;
                composite.setLayout(layout2);

                messageLabel = new Label(composite, SWT.NONE);
                messageLabel.setLayoutData(
                        new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
                messageLabel.setFont(composite.getFont());
                messageLabel.setBackground(composite.getBackground());
                messageLabel.setForeground((Color) resources
                        .get(ColorUtils.toDescriptor("#323232"))); //$NON-NLS-1$

                filterComposite = new Composite(composite, SWT.BORDER);
                filterComposite.setBackground(
                        getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                GridLayout filterLayout = new GridLayout(2, false);
                filterLayout.marginHeight = 0;
                filterLayout.marginWidth = 0;
                filterComposite.setLayout(filterLayout);
                filterComposite.setFont(composite.getFont());

                createFilterControls(filterComposite);
                filterComposite.setLayoutData(
                        new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            }

            treeComposite = new Composite(this, SWT.NONE);
            GridLayout treeCompositeLayout = new GridLayout();
            treeCompositeLayout.marginHeight = 0;
            treeCompositeLayout.marginWidth = 0;
            treeComposite.setLayout(treeCompositeLayout);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            treeComposite.setLayoutData(data);
            createTreeControl(treeComposite, treeStyle);
        }

        @Override
        protected void createFilterText(Composite parent) {
            super.createFilterText(parent);
            filterText.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#b2b2b2"))); //$NON-NLS-1$
        }

        @Override
        protected void textChanged() {
            super.textChanged();
            if (getFilterString() == null || getFilterString().equals("")) { //$NON-NLS-1$
                Display.getCurrent().timerExec(
                        (int) getRefreshJobDelay() * 3 / 2, new Runnable() {

                            @Override
                            public void run() {
                                getViewer().expandAll();
                            }
                        });
            }
        }
    }

    private static class ImportExportLabelProvider extends LabelProvider
            implements IColorProvider {

        private ResourceManager resources;

        public ImportExportLabelProvider(ResourceManager resources) {
            this.resources = resources;
        }

        @Override
        public Color getForeground(Object element) {
            return null;
        }

        @Override
        public Color getBackground(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            //query the element for its label
            IWorkbenchAdapter adapter = getAdapter(element);
            if (adapter == null) {
                return ""; //$NON-NLS-1$
            }
            String label = adapter.getLabel(element);

            //return the decorated label
            return decorateText(label, element);
        }

        private IWorkbenchAdapter getAdapter(Object o) {
            return Adapters.adapt(o, IWorkbenchAdapter.class);
        }

        private String decorateText(String input, Object element) {
            return input;
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof IWizardCategory) {
                return (Image) resources.get(
                        MindMapUI.getImages().get("icons/impexp/folder.png")); //$NON-NLS-1$
            }

            //obtain the base image by querying the element
            IWorkbenchAdapter adapter = getAdapter(element);
            if (adapter == null) {
                return null;
            }
            ImageDescriptor descriptor = adapter.getImageDescriptor(element);
            if (descriptor == null) {
                return null;
            }

            //add any annotations to the image descriptor
            descriptor = decorateImage(descriptor, element);

            return (Image) resources.get(descriptor);
        }

        private ImageDescriptor decorateImage(ImageDescriptor input,
                Object element) {
            return input;
        }
    }

    /*
     * Class to create a control that shows a categorized tree of wizard types.
     */
    protected class CategorizedWizardSelectionTree {
        private final static int SIZING_LISTS_HEIGHT = 200;

        private IWizardCategory wizardCategories;
        private String message;
        private TreeViewer viewer;

        /**
         * Constructor for CategorizedWizardSelectionTree
         *
         * @param categories
         *            root wizard category for the wizard type
         * @param msg
         *            message describing what the user should choose from the
         *            tree.
         */
        protected CategorizedWizardSelectionTree(IWizardCategory categories,
                String msg) {
            this.wizardCategories = categories;
            this.message = msg;
        }

        /**
         * Create the tree viewer and a message describing what the user should
         * choose from the tree.
         *
         * @param parent
         *            Composite on which the tree viewer is to be created
         * @return Comoposite with all widgets
         */
        protected Composite createControl(Composite parent) {
            Font font = parent.getFont();

            // create composite for page.
            Composite outerContainer = new Composite(parent, SWT.NONE);
            outerContainer.setLayout(new GridLayout());
            outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
            outerContainer.setFont(font);

            createFilteredTree(outerContainer);
            layoutTopControl(viewer.getControl());

            return outerContainer;
        }

        /**
         * Create the categorized tree viewer.
         *
         * @param parent
         */
        @SuppressWarnings("unchecked")
        private void createFilteredTree(Composite parent) {
            // Create a FilteredTree for the categories and wizards
            InternalFilteredTree filteredTree = new InternalFilteredTree(parent,
                    SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
                            | SWT.FULL_SELECTION,
                    new WizardPatternFilter(), message);
            viewer = filteredTree.getViewer();
            filteredTree.setFont(parent.getFont());
            filteredTree.setQuickSelectionMode(true);

            viewer.setContentProvider(new WizardContentProvider());
            viewer.setLabelProvider(new ImportExportLabelProvider(resources));
            DataTransferWizardCollectionComparator comparator = DataTransferWizardCollectionComparator.INSTANCE;
            comparator.setWizardsOrder(wizardsOrder);
            viewer.setComparator(comparator);

            ArrayList inputArray = new ArrayList();

            if (wizardCategories != null) {
                if (wizardCategories.getParent() == null) {
                    IWizardCategory[] children = wizardCategories
                            .getCategories();
                    for (int i = 0; i < children.length; i++) {
                        inputArray.add(children[i]);
                    }
                } else {
                    inputArray.add(wizardCategories);
                }
            }

            viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

            AdaptableList input = new AdaptableList(inputArray);

            // filter wizard list according to capabilities that are enabled
            viewer.addFilter(new WizardActivityFilter());

            viewer.setInput(input);
        }

        /**
         * @return the categorized tree viewer
         */
        protected TreeViewer getViewer() {
            return viewer;
        }

        /**
         * Layout for the given control.
         *
         * @param control
         */
        private void layoutTopControl(Control control) {
            GridData data = new GridData(GridData.FILL_BOTH);

            int availableRows = DialogUtil.availableRows(control.getParent());

            //Only give a height hint if the dialog is going to be too small
            if (availableRows > 50) {
                data.heightHint = SIZING_LISTS_HEIGHT;
            } else {
                data.heightHint = availableRows * 3;
            }

            control.setLayoutData(data);
        }
    }

    protected ResourceManager resources;

    private TreeViewer treeViewer;

    protected List<Object> wizardsOrder;

    /**
     * Constructor for import/export wizard page.
     *
     * @param aWorkbench
     *            current workbench
     * @param currentSelection
     *            current selection
     */
    protected ImportExportPage(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        super("importExportPage", aWorkbench, currentSelection, null, null);    //$NON-NLS-1$
        setTitle(WorkbenchMessages.Select);
    }

    @Override
    public void createControl(Composite parent) {
        Font font = parent.getFont();

        // create composite for page.
        Composite outerContainer = new Composite(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                outerContainer);

        outerContainer.setLayout(new GridLayout());
        outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        outerContainer.setFont(font);

        Composite comp = createTreeViewer(outerContainer);

        Dialog.applyDialogFont(comp);

        restoreWidgetValues();

        setControl(outerContainer);

        initialize();
    }

    /**
     * Create the tree viewer from which a wizard is selected.
     */
    protected abstract Composite createTreeViewer(Composite parent);

    /**
     * Method to call when an item in one of the lists is double-clicked. Shows
     * the first page of the selected wizard or expands a collapsed tree.
     * 
     * @param event
     */
    protected void treeDoubleClicked(DoubleClickEvent event) {
        ISelection selection = event.getViewer().getSelection();
        IStructuredSelection ss = (IStructuredSelection) selection;
        listSelectionChanged(ss);

        Object element = ss.getFirstElement();
        TreeViewer v = (TreeViewer) event.getViewer();
        if (v.isExpandable(element)) {
            v.setExpandedState(element, !v.getExpandedState(element));
        } else if (element instanceof WorkbenchWizardElement) {
            if (canFlipToNextPage()) {
                getContainer().showPage(getNextPage());
                return;
            }
        }
        getContainer().showPage(getNextPage());
    }

    /*
     * Update the wizard's message based on the given (selected) wizard element.
     */
    private void updateSelectedNode(WorkbenchWizardElement wizardElement) {
        setErrorMessage(null);
        if (wizardElement == null) {
            updateMessage();
            setSelectedNode(null);
            return;
        }

        setSelectedNode(createWizardNode(wizardElement));
        setMessage(wizardElement.getDescription());
    }

    /*
     * Update the wizard's message based on the currently selected tab and the
     * selected wizard on that tab.
     */
    protected void updateMessage() {
        TreeViewer viewer = getTreeViewer();
        if (viewer != null) {
            ISelection selection = viewer.getSelection();
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object sel = ss.getFirstElement();
            if (sel instanceof WorkbenchWizardElement) {
                updateSelectedNode((WorkbenchWizardElement) sel);
            } else {
                setSelectedNode(null);
            }
        } else {
            setMessage(null);
        }
    }

    /*
     * Method to call whenever the selection in one of the lists has changed.
     * Updates the wizard's message to relect the description of the currently
     * selected wizard.
     */
    protected void listSelectionChanged(ISelection selection) {
        setErrorMessage(null);
        IStructuredSelection ss = (IStructuredSelection) selection;
        Object sel = ss.getFirstElement();
        if (sel instanceof WorkbenchWizardElement) {
            WorkbenchWizardElement currentWizardSelection = (WorkbenchWizardElement) sel;
            updateSelectedNode(currentWizardSelection);
        } else {
            updateSelectedNode(null);
        }
    }

    /*
     * Create a wizard node given a wizard's descriptor.
     */
    private IWizardNode createWizardNode(IWizardDescriptor element) {
        return new WorkbenchWizardNode(this, element) {
            @Override
            public IWorkbenchWizard createWizard() throws CoreException {
                return wizardElement.createWizard();
            }
        };
    }

    /**
     * Uses the dialog store to restore widget values to the values that they
     * held last time this wizard was used to completion.
     */
    protected void restoreWidgetValues() {
        updateMessage();
    }

    /**
     * Expands the wizard categories in this page's category viewer that were
     * expanded last time this page was used. If a category that was previously
     * expanded no longer exists then it is ignored.
     */
    @SuppressWarnings("unchecked")
    protected void expandPreviouslyExpandedCategories(String setting,
            IWizardCategory wizardCategories, TreeViewer viewer) {
        String[] expandedCategoryPaths = getDialogSettings().getArray(setting);
        if (expandedCategoryPaths == null
                || expandedCategoryPaths.length == 0) {
            return;
        }

        List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);

        if (wizardCategories != null) {
            for (int i = 0; i < expandedCategoryPaths.length; i++) {
                IWizardCategory category = wizardCategories
                        .findCategory(new Path(expandedCategoryPaths[i]));
                if (category != null) {
                    categoriesToExpand.add(category);
                }
            }
        }

        if (!categoriesToExpand.isEmpty()) {
            viewer.setExpandedElements(categoriesToExpand.toArray());
        }

    }

    /**
     * Selects the wizard category and wizard in this page that were selected
     * last time this page was used. If a category or wizard that was previously
     * selected no longer exists then it is ignored.
     */
    protected void selectPreviouslySelected(String setting,
            IWizardCategory wizardCategories, final TreeViewer viewer) {
        String selectedId = getDialogSettings().get(setting);
        if (selectedId == null) {
            return;
        }

        if (wizardCategories == null) {
            return;
        }

        Object selected = wizardCategories.findCategory(new Path(selectedId));

        if (selected == null) {
            selected = wizardCategories.findWizard(selectedId);

            if (selected == null) {
                // if we cant find either a category or a wizard, abort.
                return;
            }
        }

        viewer.setSelection(new StructuredSelection(selected), true);
    }

    /**
     * Stores the collection of currently-expanded categories in this page's
     * dialog store, in order to recreate this page's state in the next instance
     * of this page.
     */
    @SuppressWarnings("unchecked")
    protected void storeExpandedCategories(String setting, TreeViewer viewer) {
        Object[] expandedElements = viewer.getExpandedElements();
        List expandedElementPaths = new ArrayList(expandedElements.length);
        for (int i = 0; i < expandedElements.length; ++i) {
            if (expandedElements[i] instanceof IWizardCategory) {
                expandedElementPaths.add(((IWizardCategory) expandedElements[i])
                        .getPath().toString());
            }
        }
        getDialogSettings().put(setting, (String[]) expandedElementPaths
                .toArray(new String[expandedElementPaths.size()]));
    }

    /**
     * Stores the currently-selected element in this page's dialog store, in
     * order to recreate this page's state in the next instance of this page.
     */
    protected void storeSelectedCategoryAndWizard(String setting,
            TreeViewer viewer) {
        Object selected = ((IStructuredSelection) viewer.getSelection())
                .getFirstElement();

        if (selected != null) {
            if (selected instanceof IWizardCategory) {
                getDialogSettings().put(setting,
                        ((IWizardCategory) selected).getPath().toString());
            } else {
                // else its a wizard
                getDialogSettings().put(setting,
                        ((IWizardDescriptor) selected).getId());
            }
        }
    }

    /**
     * When Finish is pressed, write widget values to the dialog store so that
     * they will persist into the next invocation of the wizard page.
     */
    public void saveWidgetValues() {
        // do nothing by default - subclasses should override
    }

    @Override
    public IWizardPage getNextPage() {
        ITriggerPoint triggerPoint = getTriggerPoint();

        if (triggerPoint == null || WorkbenchActivityHelper
                .allowUseOf(triggerPoint, getSelectedNode())) {
            return super.getNextPage();
        }
        return null;
    }

    /**
     * Get the trigger point for the wizard type, if one exists.
     *
     * @return the wizard's trigger point
     */
    protected ITriggerPoint getTriggerPoint() {
        return null;    // default implementation
    }

    /**
     * Set the tree viewer that is used for this wizard selection page.
     *
     * @param viewer
     */
    protected void setTreeViewer(TreeViewer viewer) {
        treeViewer = viewer;
    }

    /**
     * Get the tree viewer that is used for this wizard selection page.
     *
     * @return tree viewer used for this wizard's selection page
     */
    protected TreeViewer getTreeViewer() {
        return treeViewer;
    }

    /**
     * Perform any initialization of the wizard page that needs to be done after
     * widgets are created and main control is set.
     */
    protected void initialize() {
        // do nothing by default
    }

    protected static List<Object> getWizardsOrder(String tag) {
        List<Object> wizardsOrder = new ArrayList<Object>();
        loadWizardsOrder(wizardsOrder, tag);
        return wizardsOrder;
    }

    private static void loadWizardsOrder(List<Object> wizardsOrder,
            String tag) {
        Bundle bundle = Platform.getBundle(MindMapUIPlugin.PLUGIN_ID);
        if (bundle == null) {
            return;
        }

        BundleResource xmLResource = new BundleResource(bundle,
                new Path(WIZARDS_ORDER_XML_PATH)).resolve();
        if (xmLResource == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to locate wizards order xml: " //$NON-NLS-1$
                                    + bundle.getSymbolicName() + "/" //$NON-NLS-1$
                                    + WIZARDS_ORDER_XML_PATH));
            return;
        }

        URL listXMLURL = xmLResource.toPlatformURL();
        Element element = getWizardsOrderElement(listXMLURL);
        if (element == null) {
            return;
        }

        Element orderEle = DOMUtils.getFirstChildElementByTag(element, tag);

        Iterator<Element> it = DOMUtils.childElementIterByTag(orderEle,
                "category"); //$NON-NLS-1$
        while (it.hasNext()) {
            Element categoryEle = it.next();

            String categoryId = categoryEle.getAttribute("id"); //$NON-NLS-1$

            List<String> wizardIds = new ArrayList<String>();
            for (Element wizardEle : DOMUtils.getChildElementsByTag(categoryEle,
                    "wizard")) { //$NON-NLS-1$
                wizardIds.add(wizardEle.getAttribute("id")); //$NON-NLS-1$
            }
            wizardsOrder.add(new Category(categoryId, wizardIds));
        }
    }

    private static Element getWizardsOrderElement(URL xmlURL) {
        xmlURL = FileLocator.find(xmlURL);
        try {
            InputStream is = xmlURL.openStream();
            if (is != null) {
                try {
                    Document doc = XMLUtils.getDefaultDocumentBuilder()
                            .parse(is);
                    if (doc != null)
                        return doc.getDocumentElement();
                } finally {
                    is.close();
                }
            }
        } catch (Throwable e) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to load wizards order list from " //$NON-NLS-1$
                                    + xmlURL.toExternalForm(),
                            e));
        }
        return null;
    }

}
