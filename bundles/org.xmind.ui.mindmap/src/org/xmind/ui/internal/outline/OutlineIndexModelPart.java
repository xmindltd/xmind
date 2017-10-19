package org.xmind.ui.internal.outline;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.PageBook;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IRevealService;
import org.xmind.gef.service.IRevealServiceListener;
import org.xmind.gef.service.RevealEvent;
import org.xmind.gef.service.ZoomingAndPanningRevealService;
import org.xmind.gef.tree.ITreeViewer;
import org.xmind.gef.tree.TreeRootPart;
import org.xmind.gef.tree.TreeSelectTool;
import org.xmind.gef.tree.TreeViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.dnd.MindMapElementTransfer;
import org.xmind.ui.internal.e4models.ViewModelPart;
import org.xmind.ui.internal.editpolicies.ModifiablePolicy;
import org.xmind.ui.internal.outline.resource.OutlineResources;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.tabfolder.IPageClosedListener;
import org.xmind.ui.util.MindMapUtils;

public class OutlineIndexModelPart extends ViewModelPart
        implements IPartListener, IPageChangedListener,
        IContributedContentsView, IPageClosedListener, ICoreEventListener {

    private class CenteredRevealHelper implements IRevealServiceListener {

        private ZoomingAndPanningRevealService service;

        private boolean oldCentered;

        /**
         *
         */
        public CenteredRevealHelper(IViewer viewer) {
            Object service = viewer.getService(IRevealService.class);
            if (service != null
                    && service instanceof ZoomingAndPanningRevealService) {
                this.service = (ZoomingAndPanningRevealService) service;
                this.oldCentered = this.service.isCentered();
            } else {
                this.service = null;
                this.oldCentered = false;
            }
        }

        public void start(IGraphicalPart part) {
            if (this.service != null) {
                this.service.setCentered(true);
                this.service.reveal(new StructuredSelection(part));
                this.service.addRevealServiceListener(this);
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingStarted(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingStarted(RevealEvent event) {
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingCanceled(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingCanceled(RevealEvent event) {
            restore();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xmind.gef.service.IRevealServiceListener#revealingFinished(org
         * .xmind.gef.service.RevealEvent)
         */
        public void revealingFinished(RevealEvent event) {
            restore();
        }

        void restore() {
            this.service.removeRevealServiceListener(this);
            this.service.setCentered(this.oldCentered);
        }

    }

    private class ShowWorkbookAction extends Action {

        public ShowWorkbookAction() {
            super(MindMapMessages.OutlineIndexPart_ShowWorkbookAction_text,
                    AS_RADIO_BUTTON);
            setId("org.xmind.ui.showWorkbook"); //$NON-NLS-1$
            setToolTipText(
                    MindMapMessages.OutlineIndexPart_ShowWorkbookAction_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.WORKBOOK, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.WORKBOOK, false));
        }

        public void run() {

            showCurrentPage = false;
            showCurrentSheetAction.setChecked(showCurrentPage);
            showWorkbookAction.setChecked(!showCurrentPage);

            if (sourceEditor == null)
                return;

            IWorkbookRef workbookRef = sourceEditor
                    .getAdapter(IWorkbookRef.class);
            if (workbookRef == null)
                return;

            if (outlineType == OUTLINE_TYPE_NONE) {
                showEditorTreeViewer();
            } else {
                OutlineViewer currentViewer = getOutlineViewer(sourceEditor,
                        parseOutlineTypeToViewerType(outlineType));
                if (currentViewer != null
                        && !currentViewer.getControl().isDisposed()) {
                    currentViewer.setInput(
                            outlineResources.getResourceForWorkbook(workbookRef,
                                    outlineType, false));
                    viewerStack.showPage(currentViewer.getControl());
                }
            }

        }
    }

    private class ShowCurrentSheetAction extends Action {

        public ShowCurrentSheetAction() {
            super(MindMapMessages.OutlineIndexPart_ShowCurrentSheetAction_text);
            setId("org.xmind.ui.showCurrentSheet"); //$NON-NLS-1$
            setToolTipText(
                    MindMapMessages.OutlineIndexPart_ShowCurrentSheetAction_toolTip);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SHEET, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.SHEET, false));
        }

        public void run() {

            showCurrentPage = true;
            showCurrentSheetAction.setChecked(showCurrentPage);
            showWorkbookAction.setChecked(!showCurrentPage);

            if (sourcePage == null)
                return;

            ISheet sheet = (ISheet) sourcePage.getAdapter(ISheet.class);
            if (sheet == null)
                return;

            if (outlineType == OUTLINE_TYPE_NONE) {
                showCurrentPageViewer();
            } else {
                OutlineViewer currentViewer = getOutlineViewer(sourceEditor,
                        parseOutlineTypeToViewerType(outlineType));
                if (currentViewer != null
                        && !currentViewer.getControl().isDisposed()) {
                    currentViewer.setInput(outlineResources
                            .getResourceForSheet(sheet, outlineType, false));
                    viewerStack.showPage(currentViewer.getControl());
                }
            }

        }
    }

    private class IndexTypeAction extends Action {

        IndexTypeAction(String name) {
            super(name, IAction.AS_CHECK_BOX);
        }

        @Override
        public void run() {
            if (indexTypeToolItem != null && !indexTypeToolItem.isDisposed()) {
                setOutlineType(OutlineType.findByName(getText()).getType());
                indexTypeToolItem.setText(getText());
                if (topComposite != null && !topComposite.isDisposed()) {
                    topComposite.pack(true);
                    topComposite.getParent().layout(true, true);
                }
            }
        }
    }

    public static final int OUTLINE_TYPE_NONE = 0;
    public static final int OUTLINE_TYPE_BY_MARKERS = 1;
    public static final int OUTLINE_TYPE_BY_LABELS = 2;
    public static final int OUTLINE_TYPE_BY_STARTDATE = 3;
    public static final int OUTLINE_TYPE_BY_ENDDATE = 4;
    public static final int OUTLINE_TYPE_BY_ASSIGNEE = 5;
    public static final int OUTLINE_TYPE_BY_AZ = 6;
    public static final int OUTLINE_TYPE_BY_ZA = 7;

    private static final String VIEWMENU_ID_FOR_OUTLINEPART = "org.eclipse.ui.views.ContentOutline"; //$NON-NLS-1$

    private CoreEventRegister coreEventRegister = new CoreEventRegister(this);

    private Composite parentComposite;

    private IGraphicalEditor sourceEditor;

    private OutlineResources outlineResources;

    private IGraphicalEditorPage sourcePage;

    private PageBook viewerStack;

    private Control defaultPage;

    private ITreeViewer editorTreeViewer;

    private Map<Object, ITreeViewer> pageViewers = new HashMap<Object, ITreeViewer>();

    private Map<IGraphicalEditor, Collection<IGraphicalEditorPage>> pages = new HashMap<IGraphicalEditor, Collection<IGraphicalEditorPage>>();

    private boolean showCurrentPage = false;

    private int outlineType = OUTLINE_TYPE_NONE;

    private Map<IGraphicalEditor, Map<String, OutlineViewer>> outlineViewers = new HashMap<IGraphicalEditor, Map<String, OutlineViewer>>();

    private EditDomain domain;

    private IAction showWorkbookAction;

    private IAction showCurrentSheetAction;

    private MenuManager dropDownMenuManager;

    private Composite topComposite;

    private ToolItem indexTypeToolItem;

    protected void init() {
        super.init();
        registerViewMenu(VIEWMENU_ID_FOR_OUTLINEPART);
    }

    protected Control doCreateContent(Composite parent) {
        this.parentComposite = parent;

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        topComposite = createTopComposite(composite);

        new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite contentComposite = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        contentComposite.setLayout(gridLayout);
        contentComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewerStack = new PageBook(contentComposite, SWT.NONE);
        viewerStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        defaultPage = createDefaultPage(viewerStack);
        viewerStack.showPage(defaultPage);

        IWorkbenchWindow window = getAdapter(IWorkbenchWindow.class);
        if (window != null) {
            IWorkbenchPage activePage = window.getActivePage();
            if (activePage != null) {
                partActivated(activePage.getActiveEditor());
                activePage.addPartListener(this);
            }
        }
        return composite;
    }

    private Composite createTopComposite(Composite composite) {
        Composite topComposite = new Composite(composite, SWT.RIGHT);
        topComposite.setBackground(
                topComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 2;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        topComposite.setLayout(layout);
        topComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        showWorkbookAction = new ShowWorkbookAction();
        showCurrentSheetAction = new ShowCurrentSheetAction();

        showWorkbookAction.setChecked(!isShowCurrentPage());
        showCurrentSheetAction.setChecked(isShowCurrentPage());

        ToolBarManager toolBarManager = new ToolBarManager();
        toolBarManager.add(showWorkbookAction);
        toolBarManager.add(showCurrentSheetAction);

        ToolBar leftToolBar = toolBarManager.createControl(topComposite);
        leftToolBar.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        ToolBar rightToolBar = new ToolBar(topComposite, SWT.NONE);
        rightToolBar
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

        indexTypeToolItem = new ToolItem(rightToolBar, SWT.DROP_DOWN);
        indexTypeToolItem.setText(OutlineType.None.getName());

        indexTypeToolItem.addListener(SWT.Dispose, getToolItemListener());
        indexTypeToolItem.addListener(SWT.Selection, getToolItemListener());

        return topComposite;
    }

    private Listener getToolItemListener() {
        return new Listener() {

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Dispose:
                    handleWidgetDispose(event);
                    break;
                case SWT.Selection:
                    Widget ew = event.widget;
                    if (ew != null) {
                        handleWidgetSelection(event,
                                ((ToolItem) ew).getSelection());
                    }
                    break;
                }
            }
        };
    }

    private void handleWidgetSelection(Event event, boolean selection) {
        if (event.widget instanceof ToolItem) {
            ToolItem item = (ToolItem) event.widget;
            MenuManager menuMan = getDropDownMenuManager();
            Menu menu = menuMan.createContextMenu(item.getParent());
            if (menu != null) {
                Rectangle b = item.getBounds();
                Point p = item.getParent().toDisplay(b.x, b.y + b.height);
                menu.setLocation(p.x, p.y);
                menu.setVisible(true);
            }
        }

    }

    private MenuManager getDropDownMenuManager() {
        if (dropDownMenuManager == null) {
            dropDownMenuManager = new MenuManager();
            dropDownMenuManager.setRemoveAllWhenShown(true);
            dropDownMenuManager.addMenuListener(new IMenuListener() {

                public void menuAboutToShow(IMenuManager manager) {
                    for (String name : OutlineType.getNames()) {
                        manager.add(new IndexTypeAction(name));
                    }
                }
            });
        }
        return dropDownMenuManager;
    }

    private void handleWidgetDispose(Event event) {
        if (event.widget instanceof ToolItem) {
            if (dropDownMenuManager != null) {
                dropDownMenuManager.dispose();
                dropDownMenuManager = null;
            }
        }
    }

    private OutlineViewer getOutlineViewer(IGraphicalEditor editor,
            String viewerType) {
        Map<String, OutlineViewer> typeToViewers = outlineViewers.get(editor);
        if (typeToViewers == null)
            return null;
        return typeToViewers.get(viewerType);
    }

    private String parseOutlineTypeToViewerType(int outlineType) {
        if (outlineType == OUTLINE_TYPE_BY_STARTDATE
                || outlineType == OUTLINE_TYPE_BY_ENDDATE)
            return OutlineViewer.VIEWERTYPE_MULITCOLUMN;
        if (outlineType == OUTLINE_TYPE_BY_MARKERS
                || outlineType == OUTLINE_TYPE_BY_LABELS
                || outlineType == OUTLINE_TYPE_BY_ASSIGNEE
                || outlineType == OUTLINE_TYPE_BY_AZ
                || outlineType == OUTLINE_TYPE_BY_ZA)
            return OutlineViewer.VIEWERTYPE_SINGLECOLUMN;
        return null;
    }

    private void registerCoreEventForEditor() {
        if (sourceEditor != null) {
            IWorkbook workbook = (IWorkbook) sourceEditor
                    .getAdapter(IWorkbook.class);
            if (workbook instanceof ICoreEventSource) {
                coreEventRegister = new CoreEventRegister(
                        (ICoreEventSource) workbook, this);
                coreEventRegister.register(Core.ModifyTime);
                coreEventRegister.register(Core.WorkbookSave);
            }
        }
    }

    public OutlineResources getOutlineResources() {
        return outlineResources;
    }

    public boolean isShowCurrentPage() {
        return showCurrentPage;
    }

    public void setOutlineType(int type) {
        if (type == OUTLINE_TYPE_NONE) {
            outlineType = type;
            if (isShowCurrentPage()) {
                showCurrentPageViewer();
            } else {
                showEditorTreeViewer();
            }
        } else {
            String oldViewerType = parseOutlineTypeToViewerType(outlineType);
            OutlineViewer oldViewer = getOutlineViewer(sourceEditor,
                    oldViewerType);

            this.outlineType = type;

            String viewerType = parseOutlineTypeToViewerType(type);
            OutlineViewer newViewer = getOutlineViewer(sourceEditor,
                    viewerType);
            if (newViewer == null) {
                newViewer = createOutlineViewer(viewerStack, viewerType);
                Map<String, OutlineViewer> typeToViewer = outlineViewers
                        .get(sourceEditor);
                typeToViewer.put(viewerType, newViewer);
            }
            outlineViewerChanged(oldViewer, newViewer);
        }
    }

    protected void dispose() {
        setEditor(null);
        IWorkbenchWindow window = getAdapter(IWorkbenchWindow.class);
        if (window != null) {
            window.getActivePage().removePartListener(this);
        }
        for (Object editor : pages.keySet().toArray()) {
            partClosed((IGraphicalEditor) editor);
        }
        coreEventRegister.unregisterAll();
        super.dispose();
    }

    private Composite createDefaultPage(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        page.setLayout(new GridLayout(1, false));

        Label label = new Label(page, SWT.LEFT | SWT.WRAP);
        label.setText(MindMapMessages.OutlineIndexPart_DefaultPage_message);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return page;
    }

    protected void setFocus() {
        super.setFocus();
        OutlineViewer outlineViewer = getOutlineViewer(sourceEditor,
                parseOutlineTypeToViewerType(outlineType));
        if (outlineViewer != null && !outlineViewer.getControl().isDisposed()) {
            outlineViewer.setFocus();
        } else if (viewerStack != null && !viewerStack.isDisposed()) {
            viewerStack.setFocus();
        } else if (parentComposite != null && !parentComposite.isDisposed()) {
            parentComposite.setFocus();
        }
    }

    public IWorkbenchPart getContributingPart() {
        return sourceEditor;
    }

    public void pageClosed(Object pageObject) {
        if (pageObject instanceof IGraphicalEditorPage) {
            IGraphicalEditorPage page = (IGraphicalEditorPage) pageObject;
            unregisterSourcePage(page);
        }
    }

    public void pageChanged(PageChangedEvent event) {
        final IGraphicalEditorPage page = (IGraphicalEditorPage) event
                .getSelectedPage();
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                if (page.isDisposed() || page.getControl() == null
                        || page.getControl().isDisposed())
                    return;
                setSourcePage(page);
            }
        });
    }

    public void partActivated(final IWorkbenchPart part) {
        if (!(part instanceof IGraphicalEditor))
            return;

        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                setEditor((IGraphicalEditor) part);
            }
        });
    }

    private void setEditor(IGraphicalEditor editor) {
        if (editor == this.sourceEditor)
            return;

        if (this.sourceEditor != null) {
            this.sourceEditor.removePageChangedListener(this);
        }

        this.sourceEditor = editor;
        this.outlineResources = editor == null ? null : new OutlineResources();
        registerCoreEventForEditor();

        if (editor != null && domain == null) {
            domain = new EditDomain();
            domain.installTool(GEF.TOOL_SELECT, new TreeSelectTool());
            domain.setCommandStack(sourceEditor.getCommandStack());
            domain.installEditPolicy(MindMapUI.POLICY_MODIFIABLE,
                    new ModifiablePolicy());
        }

        if (this.sourceEditor != null) {
            if (!outlineViewers.containsKey(sourceEditor)) {
                outlineViewers.put(sourceEditor,
                        new HashMap<String, OutlineViewer>());
            }
            this.sourceEditor.addPageChangedListener(this);
            setSourcePage(this.sourceEditor.getActivePageInstance());
        } else {
            setSourcePage(null);
            viewerStack.showPage(defaultPage);
        }
    }

    private void setSourcePage(IGraphicalEditorPage page) {
        if (page == this.sourcePage)
            return;

        this.sourcePage = page;

        if (viewerStack != null && !viewerStack.isDisposed()) {
            Control pageToShow = defaultPage;
            if (this.sourcePage != null) {
                if (outlineType == OUTLINE_TYPE_NONE) {
                    if (isShowCurrentPage())
                        showCurrentPageViewer();
                    else
                        showEditorTreeViewer();
                } else {
                    OutlineViewer viewer = ensureOutlineViewer(this.sourcePage);
                    if (viewer != null && !viewer.getControl().isDisposed()) {
                        pageToShow = viewer.getControl();
                    }
                    viewerStack.showPage(pageToShow);
                }
            }
        }
    }

    private void showEditorTreeViewer() {
        if (editorTreeViewer == null || editorTreeViewer.getControl() == null
                || editorTreeViewer.getControl().isDisposed()) {
            editorTreeViewer = createEditorTreeViewer();
            if (editorTreeViewer != null) {
                configureEditorTreeViewer(editorTreeViewer);
                createEditorTreeViewerControl(editorTreeViewer, viewerStack);
                editorTreeViewer
                        .setInput(sourceEditor.getAdapter(IWorkbook.class));
            }
        }
        if (editorTreeViewer != null) {
            Object newInput = sourceEditor.getAdapter(IWorkbook.class);
            if (newInput != editorTreeViewer.getInput()) {
                editorTreeViewer.setInput(newInput);
            }
            viewerStack.showPage(editorTreeViewer.getControl());
        }

    }

    private void showCurrentPageViewer() {
        IGraphicalEditorPage page = sourceEditor.getActivePageInstance();
        if (page == null) {
            viewerStack.showPage(defaultPage);
            return;
        }

        Object pageInput = page.getInput();
        ITreeViewer pageTreeViewer = pageViewers.get(pageInput);
        if (pageTreeViewer == null || pageTreeViewer.getControl() == null
                || pageTreeViewer.getControl().isDisposed()) {
            pageTreeViewer = createPageTreeViewer();
            if (pageTreeViewer != null) {
                configurePageTreeViewer(pageTreeViewer);
                createPageTreeViewerControl(pageTreeViewer, viewerStack);
                pageTreeViewer.setInput(pageInput);
            }
        }
        if (pageTreeViewer != null) {
            Object newInput = pageInput;
            if (newInput != pageTreeViewer.getInput()) {
                pageTreeViewer.setInput(newInput);
            }
            viewerStack.showPage(pageTreeViewer.getControl());
        }
    }

    private TreeViewer createEditorTreeViewer() {
        MindMapTreeViewer viewer = new MindMapTreeViewer();
        viewer.getProperties().set(ITreeViewer.PROP_HEADER_VISIBLE, false);
        return viewer;
    }

    private ITreeViewer createPageTreeViewer() {
        MindMapTreeViewer viewer = new MindMapTreeViewer();
        viewer.getProperties().set(ITreeViewer.PROP_HEADER_VISIBLE, false);
        return viewer;
    }

    private Control createEditorTreeViewerControl(ITreeViewer viewer,
            Composite parent) {
        Control control = ((TreeViewer) viewer).createControl(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        hookViewerControl(viewer, control);
        return control;
    }

    private Control createPageTreeViewerControl(ITreeViewer viewer,
            Composite parent) {
        Control control = ((TreeViewer) viewer).createControl(parent,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        hookViewerControl(viewer, control);
        return control;
    }

    private void configureEditorTreeViewer(ITreeViewer viewer) {
        configureTreeViewer(viewer);
    }

    private void configurePageTreeViewer(ITreeViewer viewer) {
        configureTreeViewer(viewer);
    }

    protected void configureTreeViewer(ITreeViewer viewer) {
//        viewer.addSelectionChangedListener(this);
        viewer.setEditDomain(domain);
        viewer.setPartFactory(MindMapUI.getMindMapTreePartFactory());
        viewer.setRootPart(new TreeRootPart());
    }

    private void outlineViewerChanged(OutlineViewer oldViewer,
            OutlineViewer newViewer) {
        if (newViewer != null && !newViewer.getControl().isDisposed()
                && sourceEditor != null && sourcePage != null) {
            IWorkbookRef workbookRef = sourceEditor
                    .getAdapter(IWorkbookRef.class);
            Object resourceForWorkbook = outlineResources
                    .getResourceForWorkbook(workbookRef, outlineType, true);

            ISheet sheet = (ISheet) sourcePage.getAdapter(ISheet.class);
            Object resourceForSheet = outlineResources
                    .getResourceForSheet(sheet, outlineType, true);
            if (showCurrentPage) {
                newViewer.setInput(resourceForSheet);
            } else {
                newViewer.setInput(resourceForWorkbook);

            }
            if (oldViewer != newViewer) {
//                IWorkbenchPartSite site = getViewSite();
//                if (site != null)
//                    site.setSelectionProvider(newViewer);
                if (viewerStack != null && !viewerStack.isDisposed()) {
                    viewerStack.showPage(newViewer.getControl());
                }
            }
        }
    }

    private OutlineViewer ensureOutlineViewer(IGraphicalEditorPage page) {
        String viewerType = parseOutlineTypeToViewerType(outlineType);
        OutlineViewer viewer = getOutlineViewer(sourceEditor, viewerType);
        if (viewer == null) {
            if (sourceEditor != null) {
                viewer = createOutlineViewer(viewerStack, viewerType);
                registerSourcePage(page);
                Map<String, OutlineViewer> typeToViewer = outlineViewers
                        .get(sourceEditor);
                typeToViewer.put(viewerType, viewer);
            }
        }

        if (viewer != null) {
            if (showCurrentPage) {
                ISheet sheet = (ISheet) page.getAdapter(ISheet.class);
                if (sheet != null && viewer.getInput() != outlineResources
                        .getResourceForSheet(sheet, outlineType, true))
                    viewer.setInput(outlineResources.getResourceForSheet(sheet,
                            outlineType, true));
            } else {
                IWorkbookRef workbookRef = page.getParentEditor()
                        .getAdapter(IWorkbookRef.class);
                Object newInput = outlineResources
                        .getResourceForWorkbook(workbookRef, outlineType, true);
                if (workbookRef != null && viewer.getInput() != newInput)
                    viewer.setInput(newInput);
            }
        }
        return viewer;
    }

    private void registerSourcePage(IGraphicalEditorPage page) {
        IGraphicalEditor editor = page.getParentEditor();
        Collection<IGraphicalEditorPage> list = pages.get(editor);
        if (list == null) {
            list = new HashSet<IGraphicalEditorPage>();
            pages.put(editor, list);
        }
        list.add(page);
    }

    private void unregisterSourcePage(IGraphicalEditorPage page) {
        IGraphicalEditor editor = page.getParentEditor();
        Collection<IGraphicalEditorPage> list = pages.get(editor);
        if (list != null) {
            list.remove(page);
            if (list.isEmpty()) {
                pages.remove(editor);
            }
        }
    }

    private OutlineViewer createOutlineViewer(PageBook parent,
            String viewerType) {
        final OutlineViewer viewer = new OutlineViewer(parent, viewerType);
        viewer.setAutoExpandLevel(4);
        viewer.getTree().setLinesVisible(true);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object element = ss.getFirstElement();
                    if (element instanceof ITopic) {
                        reveal((ITopic) element);
                    }
                }
            }
        });

        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
//                handleDoubleClick(viewer.getTree());
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object element = ss.getFirstElement();
                    if (element instanceof ITopic) {
                        handleDoubleClick(viewer.getTree(),
                                MindMapUtils.findTopicPart(
                                        sourceEditor.getAdapter(
                                                IGraphicalViewer.class),
                                        (ITopic) element));
                    }
                }
            }
        });
        viewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

    private void reveal(ITopic topic) {
        if (sourceEditor == null)
            return;

        sourceEditor.getSite().getPage().activate(sourceEditor);
        if (topic != null) {
            ISelectionProvider selectionProvider = sourceEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.setSelection(new StructuredSelection(topic));
            }

            Object selectedPage = sourceEditor.getSelectedPage();
            if (selectedPage instanceof IGraphicalEditorPage) {
                IGraphicalEditorPage page = (IGraphicalEditorPage) selectedPage;
                IGraphicalViewer viewer = page.getViewer();
                if (viewer == null)
                    return;

                IPart selectedPart = viewer.getFocusedPart();
                if (selectedPart instanceof IGraphicalPart) {
                    IGraphicalPart part = (IGraphicalPart) selectedPart;
                    new CenteredRevealHelper(viewer).start(part);
                }
            }

        }
    }

    private void disposeOutlineViewer(OutlineViewer viewer) {
        if (viewer != null && viewer.getControl() != null) {
            viewer.getControl().dispose();
            viewer = null;
        }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (!(part instanceof IGraphicalEditor))
            return;

        Collection<IGraphicalEditorPage> oldPages = pages.remove(part);
        if (part == this.sourceEditor) {
            setEditor(null);

        }
        if (oldPages != null) {
            for (Object page : oldPages.toArray()) {
                pageClosed(page);
            }
        }

        Map<String, OutlineViewer> typeToViewer = outlineViewers.remove(part);
        if (typeToViewer != null)
            for (OutlineViewer viewer : typeToViewer.values()) {
                disposeOutlineViewer(viewer);
            }
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void handleCoreEvent(CoreEvent event) {
        if (sourceEditor == null)
            return;

        final String type = event.getType();
        if (viewerStack != null && !viewerStack.isDisposed())
            viewerStack.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    if (Core.WorkbookSave.equals(type)) {
                        OutlineViewer currentViewer = getOutlineViewer(
                                sourceEditor,
                                parseOutlineTypeToViewerType(outlineType));
                        if (currentViewer != null) {
                            currentViewer.refresh();
                        }
                    } else if (Core.ModifyTime.equals(type)) {
                        if (sourceEditor != null) {
                            IWorkbookRef workbookRef = sourceEditor
                                    .getAdapter(IWorkbookRef.class);
                            Object resourceForWorkbook = outlineResources
                                    .getResourceForWorkbook(workbookRef,
                                            outlineType, true);
                            Object resourceForSheet = null;
                            if (sourcePage != null) {
                                ISheet sheet = (ISheet) sourcePage
                                        .getAdapter(ISheet.class);
                                resourceForSheet = outlineResources
                                        .getResourceForSheet(sheet, outlineType,
                                                true);
                            }
                            OutlineViewer currentViewer = getOutlineViewer(
                                    sourceEditor,
                                    parseOutlineTypeToViewerType(outlineType));
                            if (currentViewer != null) {
                                Control control = currentViewer.getControl();
                                if (control != null && !control.isDisposed()) {
                                    if (!showCurrentPage) {
                                        if (resourceForWorkbook != null)
                                            currentViewer.setInput(
                                                    resourceForWorkbook);
                                    } else {
                                        if (resourceForSheet != null)
                                            currentViewer
                                                    .setInput(resourceForSheet);
                                    }
                                }
                            }
                        }
                    }
                }
            });
    }

    private void hookViewerControl(final IViewer viewer,
            final Control control) {
        final DragSource dragSource = new DragSource(control,
                DND.DROP_COPY | DND.DROP_MOVE);
        dragSource.setTransfer(
                new Transfer[] { MindMapElementTransfer.getInstance(),
                        TextTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {

            Object[] elements;

            String text;

            public void dragStart(DragSourceEvent event) {
                elements = getElements();
                if (elements == null || elements.length == 0)
                    event.doit = false;
                else
                    text = createText(elements);
            }

            private Object[] getElements() {
                if (control instanceof Tree) {
                    TreeItem[] selection = ((Tree) control).getSelection();
                    if (selection.length > 0) {
                        Object[] elements = new Object[selection.length];
                        for (int i = 0; i < selection.length; i++) {
                            TreeItem item = selection[i];
                            Object data = item.getData();
                            if (data instanceof IPart) {
                                data = ((IPart) data).getModel();
                            }
                            elements[i] = data;
                        }
                        return elements;
                    }
                }
                return null;
            }

            public void dragSetData(DragSourceEvent event) {
                if (MindMapElementTransfer.getInstance()
                        .isSupportedType(event.dataType)) {
                    event.data = elements;
                } else if (TextTransfer.getInstance()
                        .isSupportedType(event.dataType)) {
                    event.data = text;
                }
            }

            private String createText(Object[] elements) {
                IDndClient textClient = MindMapUI.getMindMapDndSupport()
                        .getDndClient(MindMapUI.DND_TEXT);
                if (textClient == null)
                    return null;

                Object data = textClient.toTransferData(elements, viewer);
                return data instanceof String ? (String) data : null;
            }

            public void dragFinished(DragSourceEvent event) {
            }

        });
        control.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                dragSource.dispose();
            }
        });

        control.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Tree tree = (Tree) control;
                TreeItem[] selection = tree.getSelection();
                if (selection.length == 0)
                    return;

                TreeItem item = selection[0];
                if (!(item.getData() instanceof TopicTreePart))
                    return;

                TopicTreePart part = (TopicTreePart) item.getData();
                reveal(part.getTopic());
            }
        });

        control.addListener(SWT.MouseDoubleClick, new Listener() {

            public void handleEvent(Event event) {
                handleDoubleClick(control, null);
                event.doit = false;
            }

        });
    }

    private void handleDoubleClick(Control control, ITopicPart topicPart) {
        startEditing(control, topicPart);
    }

    private void startEditing(Control control, final ITopicPart topicPart) {
        Tree tree = (Tree) control;
        TreeItem[] selection = tree.getSelection();

        if (selection.length == 0)
            return;
        final TreeItem item = selection[0];

        if (!(item.getData() instanceof TopicTreePart) && topicPart == null) {
            return;
        }

        TreeEditor editor = new TreeEditor(tree) {
            @Override
            public void layout() {
                super.layout();
                Control editor = getEditor();
                if (editor == null || editor.isDisposed())
                    return;
                Rectangle bounds = editor.getBounds();
                Point prefSize = editor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                if (prefSize.y > bounds.height) {
                    bounds.y += (bounds.height - prefSize.y - 1) / 2;
                    bounds.height = prefSize.y;
                }
                editor.setBounds(bounds);
            }
        };
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;

        final Text text = new Text(tree, SWT.SINGLE | SWT.BORDER);
        text.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_IBEAM));
        editor.setEditor(text, item);

        final String oldValue = item.getText();
        text.setText(oldValue);
        text.setFocus();
        text.selectAll();

        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (item != null && !item.isDisposed()) {
                    item.setText(
                            text.getText() == null ? oldValue : text.getText());
                    modifyTreeItem(item, topicPart);
                }

                // This async process fixes a bug on Leopard:
                // Whole workbench crashes
                e.display.asyncExec(new Runnable() {
                    public void run() {
                        text.dispose();
                    }
                });

            }
        });

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.character) {
                case SWT.ESC:
                    item.setText(oldValue);

                    // This async process fixes a bug on Leopard:
                    // Whole workbench crashes
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            text.dispose();
                        }
                    });

                    break;
                case SWT.CR:
                    if (item != null && !item.isDisposed()) {
                        item.setText(text.getText() == null ? oldValue
                                : text.getText());
                        modifyTreeItem(item, topicPart);
                    }

                    // This async process fixes a bug on Leopard:
                    // Whole workbench crashes
                    e.display.asyncExec(new Runnable() {
                        public void run() {
                            text.dispose();
                        }
                    });

                    break;
                }
            }
        });

        item.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (text != null && !text.isDisposed())
                    text.dispose();
            }
        });

    }

    private void modifyTreeItem(TreeItem item, ITopicPart topicPart) {
        if (topicPart != null) {
            topicPart.handleRequest(
                    new Request(GEF.REQ_MODIFY)
                            .setViewer(topicPart.getSite().getViewer())
                            .setParameter(GEF.PARAM_TEXT, item.getText())
                            .setPrimaryTarget(topicPart),
                    GEF.ROLE_MODIFIABLE);
        }

        Object o = item.getData();
        if (o instanceof IPart) {
            IPart part = (IPart) o;
            part.handleRequest(new Request(GEF.REQ_MODIFY)
                    .setViewer(part.getSite().getViewer())
                    .setParameter(GEF.PARAM_TEXT, item.getText())
                    .setPrimaryTarget(part), GEF.ROLE_MODIFIABLE);
        }
    }

}
