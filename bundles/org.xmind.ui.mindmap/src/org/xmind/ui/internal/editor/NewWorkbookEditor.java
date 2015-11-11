package org.xmind.ui.internal.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.EditorPart;
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.gallery.GalleryLayout;
import org.xmind.ui.gallery.GalleryNavigablePolicy;
import org.xmind.ui.gallery.GallerySelectTool;
import org.xmind.ui.gallery.GalleryViewer;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.internal.ITemplateManagerListener;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapTemplateManager;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.actions.OpenHomeMapAction;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.OpenWorkbookDialog;
import org.xmind.ui.internal.editor.WorkbookHistory.IWorkbookHistoryListener;
import org.xmind.ui.internal.views.ThemesViewer;
import org.xmind.ui.internal.wizards.FileTemplateDescriptor;
import org.xmind.ui.internal.wizards.TemplateLabelProvider;
import org.xmind.ui.internal.wizards.ThemeTemplateDescriptor;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.PrefUtils;

@Deprecated
public class NewWorkbookEditor extends EditorPart
        implements ISelectionChangedListener, IOpenListener,
        ITemplateManagerListener, ICoreEventListener, IWorkbookHistoryListener {

    public static final String EDITOR_ID = "org.xmind.ui.NewWorkbookChooser"; //$NON-NLS-1$

    private static final class DefaultInput implements IEditorInput {

        static final DefaultInput singleton = new DefaultInput();

        private DefaultInput() {
        }

        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            return null;
        }

        public String getToolTipText() {
            return WizardMessages.NewWorkbookEditor_toolTip;
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public String getName() {
            return WizardMessages.NewWorkbookEditor_title;
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public boolean exists() {
            return false;
        }

        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public String toString() {
            return "Default Editor Input For NewWorkbookEditor"; //$NON-NLS-1$
        }

    }

    private static class TemplateThemesViewer extends ThemesViewer {

        public TemplateThemesViewer(Composite parent) {
            super(parent);
        }

        @Override
        protected void init() {
            super.init();
            EditDomain editDomain = getEditDomain();
            editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
            editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                    new GalleryNavigablePolicy());
            editDomain.uninstallEditPolicy(GEF.TOOL_EDIT);

            Properties properties = getProperties();
            properties.set(GalleryViewer.TitlePlacement,
                    GalleryViewer.TITLE_BOTTOM);
            properties.set(GalleryViewer.FlatFrames, true);
            properties.set(GalleryViewer.Layout,
                    new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                            GalleryLayout.ALIGN_TOPLEFT, 10, 10, 10, 10, 10,
                            10));
            properties.set(GalleryViewer.FrameContentSize,
                    new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        }

        @Override
        public Control createControl(Composite parent, int style) {
            return super.createControl(parent, style);
        }

    }

    private static final int FRAME_WIDTH = 200;
    private static final int FRAME_HEIGHT = 100;

    private static Map<IWorkbenchWindow, IEditorReference> openedInstances = new HashMap<IWorkbenchWindow, IEditorReference>();

    private ScrolledComposite scrollable;

    private Composite body;

    private Control chooseButton;

    private Control deleteTemplateButton;

    private IAction deleteTemplateAction;

    private IGraphicalViewer templatesViewer;

    private IGraphicalViewer themesViewer;

    private Composite recentFileGroup;

    private ITemplateDescriptor selectedTemplate;

    private boolean ignoreSelectionChanges = false;

    private boolean refreshingTemplatesViewer = false;

    private ITemplateDescriptor templateToSelectOnRefreshed = null;

    private ICoreEventRegister coreEventRegister = null;

    private IContextActivation contextActivation = null;

    private boolean reflowing = false;

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (input != DefaultInput.singleton)
            throw new PartInitException("Illegal editor input: " + input); //$NON-NLS-1$

        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite contentArea = createContentArea(parent);
        fillContentArea(contentArea);

        setPartName(getEditorInput().getName());
        setTitleToolTip(getEditorInput().getToolTipText());

        reflow();

        MindMapTemplateManager.getInstance().addTemplateManagerListener(this);
        WorkbookHistory.getInstance().addWorkbookHistoryListener(this);

        ICoreEventSupport ces = (ICoreEventSupport) MindMapUI
                .getResourceManager().getUserThemeSheet()
                .getAdapter(ICoreEventSupport.class);
        coreEventRegister = new CoreEventRegister(this);
        coreEventRegister.setNextSupport(ces);
        coreEventRegister.register(Core.StyleAdd);
        coreEventRegister.register(Core.StyleRemove);
        coreEventRegister.register(Core.Name);

        createActionHandlers();

        IContextService contextService = (IContextService) getSite()
                .getService(IContextService.class);
        if (contextService != null) {
            contextActivation = contextService
                    .activateContext("org.xmind.ui.context.home"); //$NON-NLS-1$
        }

        ITemplateDescriptor template = loadSelectedTemplate();
        if (template == null) {
            template = findTemplate(templatesViewer.getSelection());
        }
        setSelectedTemplate(template);

        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        final IEditorReference lastOpened = openedInstances.get(window);
        openedInstances.put(window,
                (IEditorReference) getSite().getPage().getReference(this));
        if (lastOpened != null) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    lastOpened.getPage().closeEditors(
                            new IEditorReference[] { lastOpened }, false);
                }
            });
        }
    }

    @Override
    public void dispose() {
//        restoreMaximizedState();

        if (contextActivation != null) {
            IContextService contextService = (IContextService) getSite()
                    .getService(IContextService.class);
            if (contextService != null) {
                contextService.deactivateContext(contextActivation);
            }
            contextActivation = null;
        }

        if (coreEventRegister != null) {
            coreEventRegister.unregisterAll();
            coreEventRegister = null;
        }

        WorkbookHistory.getInstance().removeWorkbookHistoryListener(this);
        MindMapTemplateManager.getInstance()
                .removeTemplateManagerListener(this);

        saveSelectedTemplate();

        super.dispose();

        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        IEditorReference lastOpened = openedInstances.get(window);
        if (lastOpened.getEditor(false) == this) {
            openedInstances.remove(window);
        }
    }

    private Composite createContentArea(Composite parent) {
        return createGridComposite(parent, 0, 0, 0, 0);
    }

    private void fillContentArea(Composite parent) {
        createMessageArea(parent);
        createMainSeparator(parent);

        Composite composite = createFrameBody(parent);
        body = composite;
        GridLayout bodyLayout = new GridLayout(3, false);
        bodyLayout.marginWidth = 0;
        bodyLayout.marginHeight = 0;
        bodyLayout.verticalSpacing = 0;
        bodyLayout.horizontalSpacing = 0;
        composite.setLayout(bodyLayout);

        Control mainContent = createMainContent(composite);
        mainContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control separator = createSideBarSeparator(composite);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        Control sideBar = createSideBar(composite);
        GridData gridData = new GridData(SWT.END, SWT.FILL, false, true);
        gridData.widthHint = 220;
        sideBar.setLayoutData(gridData);
    }

    private void createMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 15;
        gridLayout.horizontalSpacing = 15;
        composite.setLayout(gridLayout);

        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label label = new Label(composite, SWT.WRAP);
        label.setText(WizardMessages.NewWorkbookEditor_description);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Button button = new Button(composite, SWT.PUSH | SWT.CENTER);
        button.setText(WizardMessages.NewWorkbookEditor_ChooseButton_text);
        GridData buttonData = new GridData(SWT.END, SWT.CENTER, false, true);
        buttonData.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                true).x + 24;
        button.setLayoutData(buttonData);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                chooseAndCreate();
            }
        });
        chooseButton = button;
    }

    private void createMainSeparator(Composite parent) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    private Composite createFrameBody(Composite parent) {
        final ScrolledComposite composite = new ScrolledComposite(parent,
                SWT.V_SCROLL);
        composite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        ScrollBar vBar = composite.getVerticalBar();
        if (vBar != null && vBar.getIncrement() < 5) {
            vBar.setIncrement(5);
        }
        composite.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                reflow();
            }
        });
        scrollable = composite;

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);

        Composite content = new Composite(composite, SWT.NONE);
        content.setBackground(composite.getBackground());
        composite.setContent(content);

        return content;
    }

    private Control createMainContent(Composite parent) {
        Composite mainContent = createGridComposite(parent, 5, 5, 10, 10);

        Control content1 = createTemplatesGroup(mainContent);
        content1.setLayoutData(
                new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        Control content2 = createThemesGroup(mainContent);
        content2.setLayoutData(
                new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        return mainContent;
    }

    private Control createTemplatesGroup(Composite parent) {
        Composite composite = createTitledComposite(parent,
                WizardMessages.ChooseTemplateWizardPage_TemplatesGroup_text, 15,
                15, 5, 5);
        Control control = createTemplateChooser(composite);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createTemplateManagementBar(composite);
        return composite;
    }

    private Control createThemesGroup(Composite parent) {
        Composite composite = createTitledComposite(parent,
                WizardMessages.ChooseTemplateWizardPage_ThemesGroup_text, 15,
                15, 5, 5);
        Control control = createThemeChooser(composite);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return composite;
    }

    private Control createTemplateChooser(Composite parent) {
        GalleryViewer viewer = new GalleryViewer();

        EditDomain editDomain = new EditDomain();
        editDomain.installTool(GEF.TOOL_SELECT, new GallerySelectTool());
        editDomain.installEditPolicy(GalleryViewer.POLICY_NAVIGABLE,
                new GalleryNavigablePolicy());
        viewer.setEditDomain(editDomain);

        Properties properties = viewer.getProperties();
        properties.set(GalleryViewer.Wrap, true);
        properties.set(GalleryViewer.FlatFrames, true);
        properties.set(GalleryViewer.Horizontal, true);
        properties.set(GalleryViewer.TitlePlacement,
                GalleryViewer.TITLE_BOTTOM);
        properties.set(GalleryViewer.SolidFrames, true);
        properties.set(GalleryViewer.FrameContentSize,
                new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        properties.set(GalleryViewer.ImageConstrained, true);
        properties.set(GalleryViewer.ImageStretched, true);
        properties.set(GalleryViewer.Layout,
                new GalleryLayout(GalleryLayout.ALIGN_TOPLEFT,
                        GalleryLayout.ALIGN_TOPLEFT, 10, 10, 10, 10, 10, 10));

        Control control = viewer.createControl(parent, SWT.NONE);
        viewer.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);
        viewer.getCanvas().getVerticalBar().setEnabled(false);
        viewer.getCanvas().getHorizontalBar().setEnabled(false);

        createTemplateDndSupport(control, viewer);
        viewer.setLabelProvider(new TemplateLabelProvider());
        List<ITemplateDescriptor> templates = MindMapTemplateManager
                .getInstance().loadAllTemplates();
        viewer.setInput(templates);

        if (templates.size() > 0) {
            viewer.setSelection(new StructuredSelection(templates.get(0)),
                    true);
        }
        viewer.addSelectionChangedListener(this);
        viewer.addOpenListener(this);

        templatesViewer = viewer;
        return control;
    }

    private Control createThemeChooser(Composite parent) {
        TemplateThemesViewer viewer = new TemplateThemesViewer(parent);
        viewer.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);
        viewer.getCanvas().getVerticalBar().setEnabled(false);
        viewer.getCanvas().getHorizontalBar().setEnabled(false);

        IStyle theme = MindMapUI.getResourceManager().getDefaultTheme();
        if (theme != null)
            viewer.setDefaultTheme(theme);
        List<IStyle> themes = loadThemes();
        viewer.setInput(themes);
        if (themes.size() > 0) {
            viewer.setSelection(new StructuredSelection(themes.get(0)), true);
        }
        viewer.addSelectionChangedListener(this);
        viewer.addOpenListener(this);

        themesViewer = viewer;
        return viewer.getControl();
    }

    private void createTemplateDndSupport(final Control control,
            final IViewer viewer) {
        final Color normal = control.getBackground();
        final Color canDrop = ColorUtils.getColor(230, 230, 230);
        final boolean[] dragging = new boolean[1];
        dragging[0] = false;
        final DropTarget target = new DropTarget(control,
                DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK);
        target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                if (dragging[0]) {
                    event.detail = DND.DROP_NONE;
                    control.setBackground(normal);
                } else {
                    event.detail = DND.DROP_COPY;
                    control.setBackground(canDrop);
                }
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                control.setBackground(normal);
            }

            public void dragOperationChanged(DropTargetEvent event) {
                if (dragging[0]) {
                    event.detail = DND.DROP_NONE;
                    control.setBackground(normal);
                } else {
                    event.detail = DND.DROP_COPY;
                    control.setBackground(canDrop);
                }
            }

            public void drop(DropTargetEvent event) {
                if (dragging[0] || event.data == null) {
                    event.detail = DND.DROP_NONE;
                } else {
                    importTemplates((String[]) event.data);
                }
            }
        });

        final DragSource source = new DragSource(control, DND.DROP_COPY);
        source.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        source.addDragListener(new DragSourceAdapter() {
            List<String> fileNames = new ArrayList<String>();

            public void dragStart(DragSourceEvent event) {
                dragging[0] = true;
                fileNames.clear();
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                for (Object element : selection.toList()) {
                    if (element instanceof FileTemplateDescriptor) {
                        fileNames.add(((FileTemplateDescriptor) element)
                                .getFile().getAbsolutePath());
                    }
                }
                event.doit = !fileNames.isEmpty();
            }

            public void dragSetData(DragSourceEvent event) {
                event.data = fileNames.toArray(new String[fileNames.size()]);
            }

            public void dragFinished(DragSourceEvent event) {
                fileNames.clear();
                dragging[0] = false;
            }
        });

        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                target.dispose();
                source.dispose();
            }
        });
    }

    private void createTemplateManagementBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(5, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 20;
        composite.setLayout(gridLayout);

        createAddCustomTemplateButton(composite);
        createDeleteCustomTemplateButton(composite);
    }

    private void createAddCustomTemplateButton(Composite parent) {
        Control button = createLink(parent,
                WizardMessages.ChooseTemplateWizardPage_AddTemplate_text, null,
                new SafeRunnable() {
                    public void run() throws Exception {
                        addTemplateFromFile();
                    }
                });
        button.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
    }

    private void createDeleteCustomTemplateButton(Composite parent) {
        final Control button = createLink(parent,
                WizardMessages.ChooseTemplateWizardPage_DeleteTemplate_text,
                null, new SafeRunnable() {
                    public void run() throws Exception {
                        deleteSelectedTemplate();
                    }
                });
        button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        deleteTemplateButton = button;
    }

    private Control createSideBarSeparator(Composite parent) {
        Control separator = new Composite(parent, SWT.NONE) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return new Point(1, hHint < 0 ? 1 : hHint);
            }
        };
        separator.setBackground(separator.getParent().getBackground());
        final Color color = ColorUtils.getColor(230, 230, 230);
        separator.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                org.eclipse.swt.graphics.Rectangle r = ((Control) e.widget)
                        .getBounds();
                r.x = 0;
                r.y = 0;
                e.gc.setForeground(color);
                e.gc.setLineStyle(SWT.LINE_SOLID);
                e.gc.setLineWidth(1);
                e.gc.drawLine(r.x, r.y + 10, r.x, r.y + r.height - 10);
            }
        });
        return separator;
    }

    private Control createSideBar(Composite parent) {
        Composite sideBar = createGridComposite(parent, 5, 5, 0, 5);
        fillSideBarContent(sideBar);
        createOpenFileDropSupport(sideBar);
        return sideBar;
    }

    private void fillSideBarContent(Composite parent) {
        createRecentFilesGroup(parent);
        createOpenFileGroup(parent);
        createPreferencesGroup(parent);
    }

    private void createRecentFilesGroup(Composite parent) {
        Composite composite = createTitledComposite(parent,
                WizardMessages.NewWorkbookEditor_RecentFilesGroup_title, 10, 0,
                5, 5, 15, 0);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        recentFileGroup = createGridComposite(composite, 0, 5, 0, 5);
        recentFileGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fillRecentFiles(recentFileGroup);
    }

    private void fillRecentFiles(Composite parent) {
        int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getInt(IPreferenceConstants.RECENT_FILES);
        WorkbookHistoryItem[] items = WorkbookHistory.getInstance()
                .getTopItems(itemsToShow);

        if (items.length == 0) {
            Label label = new Label(parent, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            label.setText(
                    MindMapMessages.ReopenWorkbookMenu_NoItemsPlaceholder_text);
            label.setBackground(label.getParent().getBackground());
            label.setForeground(
                    label.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        } else {
            for (final WorkbookHistoryItem item : items) {
                String path = item.getPath();
                createLink(parent, new File(path).getName(), path,
                        new SafeRunnable() {
                            public void run() throws Exception {
                                item.reopen(getSite().getPage());
                                hideMe();
                            }
                        });
            }
        }
    }

    private void createOpenFileGroup(Composite parent) {
        Composite composite = createGridComposite(parent, 10, 0, 5, 5, 20, 0);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createLink(composite,
                WizardMessages.NewWorkbookEditor_OpenLocalFileLink_text, null,
                new SafeRunnable() {
                    public void run() throws Exception {
                        openFile();
                    }
                });

        createLink(composite,
                WizardMessages.NewWorkbookEditor_OpenHomeMapLink_text, null,
                new SafeRunnable() {
                    public void run() throws Exception {
                        IWorkbenchWindow window = getSite()
                                .getWorkbenchWindow();
                        final IWorkbenchPage page = window.getActivePage();
                        Shell shell = window.getShell();
                        OpenHomeMapAction.openHomeMap(shell, page);

                    }
                });
    }

    private void createPreferencesGroup(Composite parent) {
        Composite composite = createGridComposite(parent, 10, 0, 5, 5, 40, 0);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        createLink(composite,
                WizardMessages.NewWorkbookEditor_OpenPreferencesLink_text, null,
                new SafeRunnable() {
                    public void run() throws Exception {
                        PrefUtils.openPrefDialog(getSite().getShell(),
                                PrefUtils.GENERAL_PREF_PAGE_ID);
                    }
                });
    }

    private Hyperlink createLink(Composite parent, String text, String toolTip,
            final ISafeRunnable action) {
        final Hyperlink link = new Hyperlink(parent, SWT.NONE);
        link.setBackground(link.getParent().getBackground());
        link.setForeground(link.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        link.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, -1));
        link.setText(text);
        link.setToolTipText(toolTip);
        link.setUnderlined(false);
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkEntered(HyperlinkEvent e) {
                link.setUnderlined(true);
            }

            public void linkExited(HyperlinkEvent e) {
                link.setUnderlined(false);
            }

            public void linkActivated(HyperlinkEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        SafeRunner.run(action);
                    }
                });
            }
        });
        return link;
    }

    private Composite createTitledComposite(Composite parent, String title,
            int... spacings) {
        Composite composite = createGridComposite(parent, spacings);
        Label label = new Label(composite, SWT.NONE);
        label.setFont(FontUtils.getBold(JFaceResources.DEFAULT_FONT));
        label.setBackground(label.getParent().getBackground());
        label.setText(title);

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        separator.setBackground(separator.getParent().getBackground());
        return composite;
    }

    /**
     * 
     * @param parent
     * @param spacings
     *            [marginWidth, marginHeight, [horizontalSpacing,
     *            verticalSpacing, [marginTop, marginBottom, [marginLeft,
     *            marginRight]]]]
     * @return
     */
    private Composite createGridComposite(Composite parent, int... spacings) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridLayout layout = new GridLayout(1, false);
        if (spacings.length > 1) {
            layout.marginWidth = spacings[0];
            layout.marginHeight = spacings[1];
        }
        if (spacings.length > 3) {
            layout.verticalSpacing = spacings[2];
            layout.horizontalSpacing = spacings[3];
        }
        if (spacings.length > 5) {
            layout.marginTop = spacings[4];
            layout.marginBottom = spacings[5];
        }
        if (spacings.length > 7) {
            layout.marginLeft = spacings[6];
            layout.marginRight = spacings[7];
        }
        composite.setLayout(layout);
        return composite;
    }

    private void createOpenFileDropSupport(final Control control) {
        final DropTarget target = new DropTarget(control,
                DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK);
        target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }

            public void dragLeave(DropTargetEvent event) {
            }

            public void dragOperationChanged(DropTargetEvent event) {
                event.detail = DND.DROP_COPY;
            }

            public void drop(DropTargetEvent event) {
                openFiles((String[]) event.data);
            }
        });
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                target.dispose();
            }
        });
    }

    @Override
    public void setFocus() {
        if (body == null || body.isDisposed())
            return;

        if (selectedTemplate == null
                || !(selectedTemplate instanceof ThemeTemplateDescriptor)) {
            templatesViewer.getControl().setFocus();
        } else {
            themesViewer.getControl().setFocus();
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        if (ignoreSelectionChanges)
            return;

        setSelectedTemplate(findTemplate(event.getSelection()));
    }

    private void reflow() {
        if (body == null || body.isDisposed())
            return;

        Display display = body.getDisplay();
        if (display == null || display.isDisposed())
            return;

        if (reflowing)
            return;

        reflowing = true;
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    if (body != null && !body.isDisposed()) {
                        body.setSize(body.computeSize(
                                body.getParent().getClientArea().width,
                                SWT.DEFAULT, true));
                        body.layout(true, true);
                    }
                } finally {
                    reflowing = false;
                }
            }
        });
    }

    private void importTemplates(String[] fileNames) {
        MindMapTemplateManager.getInstance().importTemplates(fileNames);
    }

    private void openFiles(String[] fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            final String filePath = fileNames[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    IEditorInput editorInput = MME
                            .createFileEditorInput(filePath);
                    getSite().getPage().openEditor(editorInput,
                            MindMapUI.MINDMAP_EDITOR_ID, true);
                }
            });
        }
    }

    private List<IStyle> loadThemes() {
        IResourceManager resourceManager = MindMapUI.getResourceManager();
        IStyleSheet systemThemeSheets = resourceManager.getSystemThemeSheet();
        Set<IStyle> systemThemes = systemThemeSheets
                .getStyles(IStyleSheet.MASTER_STYLES);

        IStyleSheet userThemeSheets = resourceManager.getUserThemeSheet();
        Set<IStyle> userThemes = userThemeSheets
                .getStyles(IStyleSheet.MASTER_STYLES);

        List<IStyle> themes = new ArrayList<IStyle>();
        themes.add(resourceManager.getBlankTheme());
        themes.addAll(systemThemes);
        themes.addAll(userThemes);
        return themes;
    }

    private ITemplateDescriptor findTemplate(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (!ss.isEmpty()) {
                Object element = ss.toList().get(ss.size() - 1);
                if (element instanceof ITemplateDescriptor) {
                    return (ITemplateDescriptor) element;
                } else if (element instanceof IStyle) {
                    return new ThemeTemplateDescriptor((IStyle) element);
                }
            }
        }
        return null;
    }

    private void setSelectedTemplate(ITemplateDescriptor template) {
        this.selectedTemplate = template;
        ignoreSelectionChanges = true;
        try {
            if (template instanceof ThemeTemplateDescriptor) {
                IStyle selectedTheme = ((ThemeTemplateDescriptor) template)
                        .getTheme();
                themesViewer.setSelection(
                        new StructuredSelection(selectedTheme), true);
                templatesViewer.setSelection(StructuredSelection.EMPTY);
                reveal(selectedTheme, themesViewer);
            } else {
                templatesViewer
                        .setSelection(
                                template == null ? StructuredSelection.EMPTY
                                        : new StructuredSelection(template),
                                true);
                themesViewer.setSelection(StructuredSelection.EMPTY);
                reveal(template, templatesViewer);
            }
        } finally {
            ignoreSelectionChanges = false;
        }
        updateStatus();
    }

    public void open(OpenEvent event) {
        setSelectedTemplate(findTemplate(event.getSelection()));
        chooseAndCreate();
    }

    private void reveal(final Object element, final IGraphicalViewer viewer) {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (scrollable == null || scrollable.isDisposed()
                        || body == null || body.isDisposed()
                        || viewer.getControl() == null
                        || viewer.getControl().isDisposed())
                    return;
                IGraphicalPart part = viewer.findGraphicalPart(element);
                if (part == null)
                    return;
                Rectangle clientBounds = part.getFigure().getBounds();
                Point clientLocation = body.toControl(viewer
                        .computeToDisplay(clientBounds.getLocation(), false)
                        .getSWTPoint());
                Point hostLocation = scrollable.getOrigin();
                Rectangle hostBounds = new Rectangle(
                        scrollable.getClientArea());
                int top = clientLocation.y - hostLocation.y - 15;
                int bottom = clientLocation.y + clientBounds.height + 15
                        - hostLocation.y - hostBounds.height;
                int offset;
                if (top < 0) {
                    offset = top;
                } else if (bottom > 0) {
                    offset = bottom;
                } else {
                    offset = 0;
                }
                if (offset != 0) {
                    scrollable.setOrigin(hostLocation.x,
                            hostLocation.y + offset);
                }
            }
        });
    }

    private void chooseAndCreate() {
        if (selectedTemplate == null)
            return;

        final IEditorInput editorInput = selectedTemplate.createEditorInput();
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        getSite().getPage().openEditor(editorInput,
                                MindMapUI.MINDMAP_EDITOR_ID);
                        hideMe();
                    }
                });
            }
        });
    }

    private void hideMe() {
//        IWorkbenchPage page = getSite().getPage();
//        page.hideEditor((IEditorReference) page.getReference(this));
    }

    private void updateStatus() {
        boolean hasTemplateSelected = selectedTemplate != null;
        boolean hasFileTemplateSelected = hasTemplateSelected
                && selectedTemplate instanceof FileTemplateDescriptor;
        Control b = chooseButton;
        if (b != null && !b.isDisposed()) {
            b.setEnabled(hasTemplateSelected);
        }
        b = deleteTemplateButton;
        if (b != null && !b.isDisposed()) {
            b.setEnabled(hasFileTemplateSelected);
        }
        if (deleteTemplateAction != null) {
            deleteTemplateAction.setEnabled(hasFileTemplateSelected);
        }
    }

    private static final String SETTINGS_SECTION = "org.xmind.ui.newWizard"; //$NON-NLS-1$
    private static final String SELECTED_TEMPLATE = "selectedTemplate"; //$NON-NLS-1$

    private IDialogSettings getDialogSettings() {
        return MindMapUIPlugin.getDefault().getDialogSettings(SETTINGS_SECTION);
    }

    private void saveSelectedTemplate() {
        getDialogSettings().put(SELECTED_TEMPLATE,
                selectedTemplate == null ? "" : selectedTemplate //$NON-NLS-1$
                        .getSymbolicName());
    }

    private ITemplateDescriptor loadSelectedTemplate() {
        String symbolicName = getDialogSettings().get(SELECTED_TEMPLATE);
        return MindMapTemplateManager.getInstance().loadTemplate(symbolicName);
    }

    public void templateAdded(ITemplateDescriptor template) {
        refreshTemplatesViewer(template);
    }

    public void templateRemoved(ITemplateDescriptor template) {
        refreshTemplatesViewer(null);
    }

    private void refreshTemplatesViewer(final ITemplateDescriptor template) {
        final IGraphicalViewer viewer = templatesViewer;
        if (viewer == null)
            return;

        final Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;

        if (template != null)
            templateToSelectOnRefreshed = template;
        if (refreshingTemplatesViewer)
            return;

        refreshingTemplatesViewer = true;
        control.getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {
                    if (control.isDisposed())
                        return;

                    viewer.setInput(MindMapTemplateManager.getInstance()
                            .loadAllTemplates());
                    if (templateToSelectOnRefreshed != null && getSite()
                            .getPage()
                            .getActiveEditor() == NewWorkbookEditor.this) {
                        setSelectedTemplate(templateToSelectOnRefreshed);
                    }
                    reflow();
                } finally {
                    refreshingTemplatesViewer = false;
                }
            }
        });
    }

    public void handleCoreEvent(final CoreEvent event) {
        final IGraphicalViewer viewer = themesViewer;
        if (viewer == null)
            return;
        Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;
        control.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (Core.Name.equals(event.getType())) {
                    IPart part = viewer.findPart(event.getSource());
                    if (part != null) {
                        part.update();
                    }
                } else {
                    viewer.setInput(loadThemes());
                    viewer.setSelection(
                            new StructuredSelection(event.getSource()), true);
                }
            }
        });
    }

    private void createActionHandlers() {
        IActionBars actionBars = getEditorSite().getActionBars();
        deleteTemplateAction = new Action() {
            public void run() {
                deleteSelectedTemplate();
            }
        };
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
                deleteTemplateAction);
    }

    private void deleteSelectedTemplate() {
        ITemplateDescriptor template = selectedTemplate;
        if (template != null) {
            if (!MessageDialog.openConfirm(getSite().getShell(),
                    DialogMessages.ConfirmDeleteTemplateDialog_title,
                    NLS.bind(
                            DialogMessages.ConfirmDeleteTemplateDialog_message_with_templateName,
                            template.getName())))
                return;
            MindMapTemplateManager.getInstance().removeTemplate(template);
        }
    }

    private void addTemplateFromFile() {
        FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
        String ext = "*" + MindMapUI.FILE_EXT_TEMPLATE; //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] { ext });
        dialog.setFilterNames(new String[] { NLS.bind("{0} ({1})", //$NON-NLS-1$
                DialogMessages.TemplateFilterName, ext) });
        String path = dialog.open();
        if (path == null)
            return;

        MindMapTemplateManager.getInstance().importCustomTemplate(path);
    }

    private void openFile() throws Exception {
        IEditorPart[] openedEditors = new OpenWorkbookDialog(
                getSite().getWorkbenchWindow()).open();
        if (openedEditors != null && openedEditors.length > 0) {
            hideMe();
        }
    }

    public void workbookHistoryUpdated() {
        getSite().getWorkbenchWindow().getWorkbench().getDisplay()
                .syncExec(new Runnable() {
                    public void run() {
                        refreshRecentFileList();
                    }
                });
    }

    private void refreshRecentFileList() {
        if (recentFileGroup == null || recentFileGroup.isDisposed())
            return;

        Control[] oldChildren = recentFileGroup.getChildren();
        for (int i = 0; i < oldChildren.length; i++) {
            oldChildren[i].dispose();
        }
        fillRecentFiles(recentFileGroup);
        reflow();
    }

//    private void enterMaximizedState() {
//        getSite().getPage().setPartState(
//                getSite().getPage().getReference(this),
//                IStackPresentationSite.STATE_MAXIMIZED);
//    }
//
//    private void restoreMaximizedState() {
//        int state = getSite().getPage().getPartState(
//                getSite().getPage().getReference(this));
//        if (state == IStackPresentationSite.STATE_MAXIMIZED) {
//            getSite().getPage().setPartState(
//                    getSite().getPage().getReference(this),
//                    IStackPresentationSite.STATE_RESTORED);
//        }
//    }

    /**
     * 
     * @param window
     * @param maximized
     * @return
     * @deprecated Use {@link #showIn(IWorkbenchWindow, boolean)}.
     */
    public static boolean open(IWorkbenchWindow window, boolean maximized) {
        return showIn(window);
    }

    public static boolean showIn(IWorkbenchWindow window) {
//        IWorkbenchPage page = window.getActivePage();
//        if (page == null)
//            return false;
//        try {
//            final IEditorPart editor = page.openEditor(DefaultInput.singleton,
//                    EDITOR_ID);
//            if (editor instanceof NewWorkbookEditor) {
//                Display.getCurrent().asyncExec(new Runnable() {
//                    public void run() {
//                        ((NewWorkbookEditor) editor).reflow();
//                    }
//                });
//            }
//            return editor != null;
//        } catch (PartInitException e) {
//            Logger.log(e);
//            return false;
//        }
        return false;
    }

    public static void hideFrom(IWorkbenchWindow window) {
        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;
        IEditorReference opened = openedInstances.get(window);
        if (opened != null) {
            IWorkbenchPart editorPart = opened.getPart(false);
            page.closeEditor((IEditorPart) editorPart, false);
        }
    }

//    public static void closeAll(IWorkbenchWindow window) {
//        IWorkbenchPage[] pages = window.getPages();
//        for (int i = 0; i < pages.length; i++) {
//            IWorkbenchPage page = pages[i];
//            IEditorPart editor = page.findEditor(DEFAULT_INPUT);
//            while (editor != null) {
//                if (editor instanceof NewWorkbookEditor) {
//                    ((NewWorkbookEditor) editor).restoreMaximizedState();
//                }
//                page.closeEditor(editor, false);
//                editor = page.findEditor(DEFAULT_INPUT);
//            }
//        }
//    }

}
