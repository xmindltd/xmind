package org.xmind.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IEvaluationService;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.commands.AddSheetCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.DeleteSheetCommand;
import org.xmind.ui.commands.ModifyMetadataCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.TextFormatter;
import org.xmind.ui.viewers.SWTUtils;

public class WorkbookRevisionDialog extends Dialog {

    private class CurrentSelectionProviderWrap
            implements ISelectionChangedListener {

        private ISelectionProvider selectionProvider = null;

        public void dispose() {
            if (selectionProvider != null) {
                selectionProvider.removeSelectionChangedListener(this);
                selectionProvider = null;
            }
        }

        public void notifySelectionChanges() {
            IWorkbenchWindow parentWindow = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            IEclipseContext context = ((WorkbenchWindow) parentWindow)
                    .getModel().getContext();
            context.remove(ISources.ACTIVE_CURRENT_SELECTION_NAME);

            if (selectionProvider != null) {
                IEvaluationService es = (IEvaluationService) parentWindow
                        .getWorkbench().getService(IEvaluationService.class);
                es.getCurrentState().addVariable(
                        ISources.ACTIVE_CURRENT_SELECTION_NAME,
                        selectionProvider.getSelection());
            }
        }

        public void setSelectionProvider(ISelectionProvider selectionProvider) {
            if (selectionProvider == this.selectionProvider)
                return;

            ISelectionProvider oldSelectionProvider = this.selectionProvider;
            this.selectionProvider = selectionProvider;

            if (oldSelectionProvider != null) {
                oldSelectionProvider.removeSelectionChangedListener(this);
            }
            if (selectionProvider != null) {
                selectionProvider.addSelectionChangedListener(this);
            }

            notifySelectionChanges();
        }

        public void selectionChanged(SelectionChangedEvent event) {
            notifySelectionChanges();
        }

    }

    public static class RevisionContentProvider
            implements IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            return ((IRevisionManager) inputElement).getRevisions().toArray();
        }

    }

    private static class RevisionNumberLabelProvider
            extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.valueOf(revision.getRevisionNumber());
        }

    }

    private static class RevisionDateTimeLabelProvider
            extends ColumnLabelProvider {

        @Override
        public String getText(Object element) {
            IRevision revision = (IRevision) element;
            return String.format("%tF", revision.getTimestamp()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
                    + String.format("%tT", revision.getTimestamp()); //$NON-NLS-1$
        }

    }

    private static class VersionCloumnSorter extends ViewerSorter {
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            IRevision revision1 = (IRevision) e1;
            IRevision revision2 = (IRevision) e2;

            if (revision1.getRevisionNumber() > revision2.getRevisionNumber())
                return -1;
            if (revision1.getRevisionNumber() == revision2.getRevisionNumber())
                return 0;
            return 1;
        }
    }

    private class RevisionOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            handleOpen(event.getSelection());
        }

    }

    private static final String KEY_SELECTION_PROVIDER = "org.xmind.ui.WorkbookRevisionDialog.selectionProvider"; //$NON-NLS-1$

    private static final int PREVIEW_ID = IDialogConstants.CLIENT_ID + 1;

    private static final int RESTORE_ID = IDialogConstants.CLIENT_ID + 2;

    private static final String K_AUTO_SAVE = IMeta.CONFIG_AUTO_REVISION_GENERATION;

    private static final String V_NO = IMeta.V_NO;

    private Shell shell = null;

    private CurrentSelectionProviderWrap currentSelectionProviderWrap = null;

    private IGraphicalEditor sourceEditor;

    private TableViewer viewer;

    private ISheet sheet;

    private MenuManager popupMenuManager;

    private IRevisionManager revisionManager;

    private Button previewBt;

    private boolean previewDialogOpened = false;

    private ResourceManager resources;

    private IPartListener partListenerHandler = new IPartListener() {

        public void partOpened(IWorkbenchPart part) {

        }

        public void partDeactivated(IWorkbenchPart part) {

        }

        public void partClosed(IWorkbenchPart part) {
        }

        public void partBroughtToTop(IWorkbenchPart part) {

        }

        public void partActivated(IWorkbenchPart part) {
            if (!(part instanceof IGraphicalEditor))
                return;
            if (getSourceEditor() != (IGraphicalEditor) part) {
                if (part != null) {
                    setSourceEditor((IGraphicalEditor) part);
                }
                if (coreEventRegister != null) {
                    coreEventRegister.unregisterAll();
                }
                if (topicEventRegister != null) {
                    topicEventRegister.unregisterAll();
                }
                if (getSheet() != null)
                    registerCoreEvents();
                update();
            }
        }
    };

    private ICoreEventListener coreEventHandler = new ICoreEventListener() {

        public void handleCoreEvent(CoreEvent event) {
            String type = event.getType();
            if (Core.RevisionAdd.equals(type)
                    || Core.RevisionRemove.equals(type)) {
                updateTabelViewer();
            } else if (Core.TitleText.equals(type)) {
                updateShellTitle();
            } else if (Core.RootTopic.equals(type)) {
                topicEventRegister.unregisterAll();
                ITopic rootTopic = sheet.getRootTopic();
                topicEventRegister.setNextSourceFrom(rootTopic);
                topicEventRegister.register(Core.TitleText);
            }
        }

    };

    private IPageChangedListener pageChangedHandler = new IPageChangedListener() {

        public void pageChanged(PageChangedEvent event) {
            IViewer viewer = MindMapUIPlugin.getAdapter(getSourceEditor(),
                    IViewer.class);

            if (viewer != null && viewer instanceof IMindMapViewer) {
                ISheet newSheet = ((IMindMapViewer) viewer).getSheet();
                if (getSheet() != newSheet) {
                    setSheet(newSheet);
                }
                if (coreEventRegister != null) {
                    coreEventRegister.unregisterAll();
                }
                if (topicEventRegister != null) {
                    topicEventRegister.unregisterAll();
                }
                registerCoreEvents();
                update();
            }
        }
    };

    private ICoreEventRegister coreEventRegister = new CoreEventRegister(
            coreEventHandler);

    private ICoreEventRegister topicEventRegister = new CoreEventRegister(
            coreEventHandler);

    public WorkbookRevisionDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public WorkbookRevisionDialog(Shell shell, IGraphicalEditor sourceEditor) {
        super(shell);
        setSourceEditor(sourceEditor);
        IViewer viewer = MindMapUIPlugin.getAdapter(sourceEditor,
                IViewer.class);
        if (viewer != null && viewer instanceof IMindMapViewer) {
            setSheet(((IMindMapViewer) viewer).getSheet());
        }
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        shell = newShell;
        newShell.setText(MindMapMessages.WorkbookRevisionDialog_title);
        newShell.setSize(520, 500);
        newShell.setLocation(
                Display.getCurrent().getClientArea().width / 2
                        - newShell.getShell().getSize().x / 2,
                Display.getCurrent().getClientArea().height / 2
                        - newShell.getSize().y / 2);
    }

    @Override
    public void create() {
        super.create();
        registerSourceProvider();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 14;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createDescriptionArea(composite);

        Control viewerControl = createViewer(composite);
        GridData viewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewerControl.setLayoutData(viewerData);

        registerCoreEvents();
        viewerControl.setData(KEY_SELECTION_PROVIDER, viewer);
        createPopupMenu(viewerControl);
        viewerControl.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleViewerDispose();
            }
        });
        composite.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleDisposed();
            }
        });

        return composite;
    }

    private void createDescriptionArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 21;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);

        Label discriptionLabel = new Label(composite, SWT.WRAP);
        GridData discriptionLabelData = new GridData(SWT.FILL, SWT.CENTER, true,
                true);
        discriptionLabel.setLayoutData(discriptionLabelData);
        discriptionLabel.setAlignment(SWT.LEFT);
        discriptionLabel.setText(
                DialogMessages.workbookRevisionDialog_Description_Label_text);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 13;
        layout.marginHeight = 23;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        //create hyperlink area
        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        composite2.setLayout(gridLayout);

        createDisableHyperlink(composite2);

        //create buttonBar
        Composite buttonBar = new Composite(composite, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 0; // this is incremented by createButton
        if (Util.isMac()) {
            layout2.makeColumnsEqualWidth = false;
            layout2.horizontalSpacing = 0;
        } else {
            layout2.makeColumnsEqualWidth = true;
            layout2.horizontalSpacing = 8;
        }
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.verticalSpacing = 0;
        buttonBar.setLayout(layout2);

        GridData data2 = new GridData(
                GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        buttonBar.setLayoutData(data2);
        buttonBar.setFont(composite.getFont());

        createButtonsForButtonBar(buttonBar);

        return composite2;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        previewBt = createButton(parent, PREVIEW_ID,
                DialogMessages.WorkbookRevisionDialog_Preview_Button_label,
                false);

        previewBt.setEnabled(viewer != null
                && !StructuredSelection.EMPTY.equals(viewer.getSelection()));

        createButton(parent, RESTORE_ID,
                DialogMessages.WorkbookRevisionDialog_Restore_Button_label,
                false);
        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (PREVIEW_ID == buttonId)
            preview();
        if (RESTORE_ID == buttonId)
            restore();
        if (IDialogConstants.CLOSE_ID == buttonId)
            close();
    }

    private void asyncExec(Runnable runnable) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
    }

    private void setSourceEditor(IGraphicalEditor editor) {
        if (editor == this.sourceEditor)
            return;
        if (this.sourceEditor != null) {
            this.sourceEditor.removePageChangedListener(pageChangedHandler);
        }
        this.sourceEditor = editor;
        if (this.sourceEditor != null) {
            this.sourceEditor.addPageChangedListener(pageChangedHandler);
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .addPartListener(partListenerHandler);
            IGraphicalEditorPage page = this.sourceEditor
                    .getActivePageInstance();
            if (page != null) {
                IGraphicalViewer viewer = page.getViewer();
                if (viewer instanceof IMindMapViewer) {
                    IMindMapViewer mmv = (IMindMapViewer) viewer;
                    IMindMap mindMap = mmv.getMindMap();
                    setSheet(mindMap.getSheet());
                }
            }
        }
    }

    private IGraphicalEditor getSourceEditor() {
        return this.sourceEditor;
    }

    private ISheet getSheet() {
        return this.sheet;
    }

    private void setSheet(ISheet sheet) {
        if (this.sheet == sheet)
            return;

        this.sheet = sheet;
        if (this.sheet != null) {
            revisionManager = this.sheet.getOwnedWorkbook()
                    .getRevisionRepository()
                    .getRevisionManager(this.sheet.getId(), IRevision.SHEET);
        } else {
            revisionManager = null;
        }
        if (viewer != null) {
            viewer.setInput(revisionManager);
        }
    }

    private String getTitleText() {
        String text = null;
        if (getSheet() == null) {
            if (getSourceEditor() != null) {
                IGraphicalEditorPage page = getSourceEditor()
                        .getActivePageInstance();
                if (page != null) {
                    ISheet sheet2 = (ISheet) page.getInput();
                    if (sheet2 != null)
                        setSheet(sheet2);
                }
            }
        }
        if (getSheet() != null)
            text = String.format("%s - %s", getSheet().getTitleText(), //$NON-NLS-1$
                    getSheet().getRootTopic().getTitleText());
        return TextFormatter.removeNewLineCharacter(text);
    }

    private void registerSourceProvider() {
        currentSelectionProviderWrap = new CurrentSelectionProviderWrap();
        currentSelectionProviderWrap.notifySelectionChanges();

        final Listener focusListener = new Listener() {
            public void handleEvent(Event event) {
                if (currentSelectionProviderWrap == null)
                    return;

                Widget w = event.widget;
                ISelectionProvider selectionProvider = null;
                while (w != null) {
                    selectionProvider = (ISelectionProvider) w
                            .getData(KEY_SELECTION_PROVIDER);
                    if (selectionProvider != null)
                        break;

                    if (w instanceof Control) {
                        w = ((Control) w).getParent();
                    }
                }
                currentSelectionProviderWrap
                        .setSelectionProvider(selectionProvider);
            }
        };

        final Display display = Display.getCurrent();
        display.addFilter(SWT.FocusIn, focusListener);
        getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                display.removeFilter(SWT.FocusIn, focusListener);
            }
        });

    }

    private void createPopupMenu(Control viewerControl) {
        popupMenuManager = new MenuManager("#popup"); //$NON-NLS-1$
        popupMenuManager.add(new GroupMarker("start")); //$NON-NLS-1$
        popupMenuManager
                .add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        popupMenuManager.add(new GroupMarker("end")); //$NON-NLS-1$
        IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
                .getService(IMenuService.class);
        menuService.populateContributionManager(popupMenuManager,
                "popup:org.xmind.ui.RevisionsView"); //$NON-NLS-1$
        final Menu popupMenu = popupMenuManager
                .createContextMenu(viewerControl);
        viewerControl.setMenu(popupMenu);
    }

    private void handleDisposed() {
        if (currentSelectionProviderWrap != null) {
            currentSelectionProviderWrap.dispose();
            currentSelectionProviderWrap = null;
        }
        coreEventRegister.unregisterAll();
        topicEventRegister.unregisterAll();
        viewer = null;
        revisionManager = null;
        sheet = null;
        setSourceEditor((IGraphicalEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor());
    }

    private void handleViewerDispose() {
        if (currentSelectionProviderWrap != null) {
            currentSelectionProviderWrap.dispose();
            currentSelectionProviderWrap = null;
        }
    }

    private void registerCoreEvents() {
        coreEventRegister.setNextSourceFrom(revisionManager);
        coreEventRegister.register(Core.RevisionAdd);
        coreEventRegister.register(Core.RevisionRemove);
        coreEventRegister.setNextSourceFrom(sheet);
        coreEventRegister.register(Core.TitleText);
        coreEventRegister.register(Core.RootTopic);
        ITopic rootTopic = getSheet().getRootTopic();
        topicEventRegister.setNextSourceFrom(rootTopic);
        topicEventRegister.register(Core.TitleText);
    }

    private Control createViewer(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new RevisionContentProvider());

        viewer.getControl().setLayoutData(new GridData(GridData.CENTER));

        TableViewerColumn col0 = new TableViewerColumn(viewer, SWT.LEFT);
        col0.getColumn()
                .setText(MindMapMessages.RevisionView_VersionColumn_text);
        col0.getColumn().setWidth(200);
        col0.setLabelProvider(new RevisionNumberLabelProvider());

        TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.LEFT);
        col1.getColumn().setText(MindMapMessages.RevisionsView_DateColumn_text);
        col1.getColumn().setWidth(282);
        col1.setLabelProvider(new RevisionDateTimeLabelProvider());

        viewer.setInput(revisionManager);
        viewer.setSorter(new VersionCloumnSorter());
        viewer.addOpenListener(new RevisionOpenListener());
        viewer.getTable().addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (SWTUtils.matchKey(e.stateMask, e.keyCode, 0, SWT.SPACE)) {
                    handleOpen(viewer.getSelection());
                }
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (previewBt != null && !previewBt.isDisposed())
                    previewBt.setEnabled(!StructuredSelection.EMPTY
                            .equals(event.getSelection()));
            }
        });

        viewer.getControl().setToolTipText(
                MindMapMessages.RevisionPage_ShowDetails_message);

        return viewer.getControl();
    }

    private void handleOpen(ISelection selection) {
        if (selection.isEmpty())
            return;
        IRevision revision = (IRevision) ((IStructuredSelection) selection)
                .getFirstElement();
        viewRevision(revision);
    }

    private void viewRevision(IRevision revision) {
        if (previewDialogOpened)
            return;
        List<IRevision> revisions = revisionManager.getRevisions();
        int index = revisions.indexOf(revision);
        RevisionPreviewDialog dialog = new RevisionPreviewDialog(shell, sheet,
                revisions, index) {
            public int open() {
                previewDialogOpened = true;
                return super.open();
            }

            public boolean close() {
                previewDialogOpened = false;
                return super.close();
            }
        };
        dialog.open();
    }

    private void update() {
        updateTabelViewer();
        updateShellTitle();
    }

    private void updateTabelViewer() {
        if (viewer != null) {
            asyncExec(new Runnable() {
                public void run() {
                    if (viewer != null) {
                        viewer.setInput(revisionManager);
                        viewer.refresh();
                        createPopupMenu(viewer.getControl());
                    }
                }
            });
        }
    }

    private void updateShellTitle() {
        asyncExec(new Runnable() {
            public void run() {
                if (shell != null && !shell.isDisposed()) {
                    shell.setText(getTitleText());
                }
            }
        });
    }

    private void createDisableHyperlink(Composite parent) {
        final Hyperlink disableLink = new Hyperlink(parent, SWT.SINGLE);
        disableLink.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        disableLink.setText(
                MindMapMessages.WorkbookRevisionDialog_Disable_hyperlink);
        disableLink.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$
        boolean isAutoSave = !V_NO.equals(
                getSheet().getOwnedWorkbook().getMeta().getValue(K_AUTO_SAVE));
        if (!isAutoSave) {
            disableLink.setEnabled(false);
        }
        disableLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {

                Boolean isOk = MessageDialog.openConfirm(shell,
                        DialogMessages.DisableRevisonDialog_Title_text,
                        DialogMessages.DisableRevisonDialog_Comfirm_message);
                if (isOk) {
                    disableRevision();
                    disableLink.setEnabled(false);
                    close();
                }

            }
        });
    }

    private void disableRevision() {
        IWorkbook workbook = getSheet().getOwnedWorkbook();
        Command command = new ModifyMetadataCommand(workbook, K_AUTO_SAVE,
                V_NO);
        command.setLabel(CommandMessages.Command_TurnOffAutoRevisionSaving);
        ICommandStack commandStack = getSourceEditor().getCommandStack();
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    private void restore() {
        revertToRevision(viewer.getSelection(), getSourceEditor());
    }

    private void preview() {
        ISelection selection = viewer.getSelection();
        if (selection.isEmpty())
            return;
        IRevision revision = (IRevision) ((IStructuredSelection) selection)
                .getFirstElement();
        viewRevision(revision);
    }

    private void revertToRevision(ISelection selection, IEditorPart editor) {
        if (selection == null || selection.isEmpty()
                || !(selection instanceof IStructuredSelection))
            return;

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (!(obj instanceof IRevision))
            return;

        IRevision revision = (IRevision) obj;
        if (!IRevision.SHEET.equals(revision.getContentType()))
            return;

        IWorkbook workbook = revision.getOwnedWorkbook();
        final ISheet sourceSheet = (ISheet) workbook
                .findElement(revision.getResourceId(), null);

        final ISheet targetSheet = (ISheet) workbook
                .importElement(revision.getContent());
        if (targetSheet == null)
            return;

        // Force update modification info
        String title = targetSheet.getTitleText();
        targetSheet.setTitleText("#" + title); //$NON-NLS-1$
        targetSheet.setTitleText(title);

        final int sheetIndex = sourceSheet.getIndex();

        List<Command> commands = new ArrayList<Command>();
        ISheet placeholderSheet = workbook.createSheet();
        commands.add(new AddSheetCommand(placeholderSheet, workbook));
        commands.add(new DeleteSheetCommand(sourceSheet));
        commands.add(new AddSheetCommand(targetSheet, workbook, sheetIndex));
        commands.add(new DeleteSheetCommand(placeholderSheet, workbook));

        // TODO comments delete
//        List<IComment> comments = CommentsUtils
//                .getAllCommentsOfSheetAndChildren(sourceSheet);
//        for (IComment comment : comments) {
//            if (comment.getTarget() instanceof ITopic
//                    && !containsTopicById(targetSheet.getRootTopic(),
//                            comment.getTarget().getId())) {
//                commands.add(new DeleteCommentCommand(comment));
//            }
//        }

        final Command command = new CompoundCommand(
                MindMapMessages.RevertToRevisionCommand_label, commands);
        final ICommandStack commandStack = editor == null ? null
                : MindMapUIPlugin.getAdapter(editor, ICommandStack.class);

        final IRevisionManager manager = revision.getOwnedManager();
        final IRevision latestRevision = manager.getLatestRevision();

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                if (latestRevision == null || sourceSheet
                        .getModifiedTime() > latestRevision.getTimestamp()) {
                    manager.addRevision(sourceSheet);
                }
                if (commandStack != null) {
                    commandStack.execute(command);
                } else {
                    command.execute();
                }
            }
        });
    }

}
