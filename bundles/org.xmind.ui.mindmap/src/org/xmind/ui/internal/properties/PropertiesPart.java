
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.style.IStyled;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.gef.ui.properties.IPropertyPartContainer;
import org.xmind.gef.ui.properties.IPropertySectionPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.e4models.ViewModelFolderRenderer;
import org.xmind.ui.internal.e4models.ViewModelPart;
import org.xmind.ui.mindmap.ICategoryAnalyzation;
import org.xmind.ui.mindmap.ICategoryManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;

public class PropertiesPart extends ViewModelPart
        implements ISelectionChangedListener, IPropertyPartContainer,
        IContributedContentsView {

    private static class SectionRecord {

        String id;

        IPropertySectionPart section;

        Section control;

        boolean visible;

        public SectionRecord(String id, IPropertySectionPart section) {
            this.id = id;
            this.section = section;
        }

    }

    private static final int DEFAULT_SECTION_WIDTH = 200;

    private IGraphicalEditor sourceEditor;

    private static PropertySectionContributorManager manager = PropertySectionContributorManager
            .getInstance();

    private List<SectionRecord> sections = new ArrayList<SectionRecord>();

    private CTabItem ti;

    private String modeLabel;

    private Composite composite;

    private PageBook viewerStack;

    private Control defaultPage;

    private Composite contentComposite;

    private WidgetFactory widgetFactory;

    private ScrolledForm form;

    private String title;

    private Hyperlink resetStyleControl;

    private ResourceManager resources;

    protected void init() {
        super.init();

        EModelService modelService = getAdapter(EModelService.class);
        MApplication application = getAdapter(MApplication.class);
        ArrayList<String> tags = new ArrayList<String>();
        tags.add(IModelConstants.TAG_EDITOR);
        tags.add(IModelConstants.TAG_ACTIVE);
        List<MPart> editors = modelService.findElements(application,
                IModelConstants.PART_ID_COMPATIBILITY_EDITOR, MPart.class,
                tags);
        if (editors != null && !editors.isEmpty()) {
            MPart editor = editors.get(0);
            handlePartActivated(editor);
        }

        initContent();
    }

    private void initContent() {
        for (SectionRecord rec : sections) {
            rec.section.init(this, getContributedEditor());
        }

        if (sourceEditor != null) {
            if (this.sourceEditor != null) {
                final DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) sourceEditor
                        .getSite().getSelectionProvider();
                if (selectionProvider != null) {
                    selectionProvider.addAsyncSelectionChangedListener(this);

                    final ISelection selection = selectionProvider
                            .getSelection();
                    if (selection != null && !selection.isEmpty()) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            public void run() {
                                selectionChanged(new SelectionChangedEvent(
                                        selectionProvider, selection));
                            }
                        });
                    }
                }
            }
        }
    }

    protected void createContent(Composite parent) {
        MindMapUIPlugin.getDefault().getUsageDataCollector()
                .trackView(UserDataConstants.VIEW_FORMAT);

        CTabFolder ctf = new CTabFolder(parent, SWT.BORDER);
        ctf.setRenderer(new ViewModelFolderRenderer(ctf));
        ctf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ti = new CTabItem(ctf, SWT.NONE);
        MPart partModel = getAdapter(MPart.class);
        modeLabel = partModel.getLocalizedLabel();
//        ti.setToolTipText(getToolTip(partModel.getLocalizedTooltip()));
        ti.setText(modeLabel);
        ctf.setSelection(ti);

        Composite contentContainer = new Composite(ctf, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        contentContainer.setLayout(layout);

        ti.setControl(contentContainer);

        Control content = doCreateContent(contentContainer);
        content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        addTopRight(ctf, true);
        adjustViewMenuBar(true);
    }

    protected Control doCreateContent(Composite parent) {
        resources = new LocalResourceManager(JFaceResources.getResources(),
                parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewerStack = new PageBook(composite, SWT.NONE);
        viewerStack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        defaultPage = createDefaultPage(viewerStack);

        contentComposite = new Composite(viewerStack, SWT.NONE);
        GridLayout contantLayout = new GridLayout();
        contantLayout.marginWidth = 0;
        contantLayout.marginHeight = 0;
        contantLayout.verticalSpacing = 0;
        contantLayout.horizontalSpacing = 0;
        contentComposite.setLayout(contantLayout);
        contentComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.widgetFactory = new WidgetFactory(contentComposite.getDisplay());

        form = widgetFactory.createScrolledForm(contentComposite);
        addHorizontalScrollSupport(form);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));
        form.setMinWidth(DEFAULT_SECTION_WIDTH);
        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (widgetFactory != null) {
                    widgetFactory.dispose();
                    widgetFactory = null;
                }
            }
        });

        createSectionControls(form, form.getBody());

        Composite internalComposite = new Composite(form.getBody(), SWT.NONE);
        internalComposite.setBackground(form.getBody().getBackground());
        internalComposite.setLayout(new GridLayout(1, false));
        internalComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
        createExtendSectionControls(widgetFactory, internalComposite);
        form.reflow(true);

        viewerStack.showPage(
                sourceEditor == null ? defaultPage : contentComposite);

        this.composite = composite;

        return composite;
    }

    // add horizontal scroll support for windows
    private void addHorizontalScrollSupport(final ScrolledForm form) {
        if (Util.isWindows()) {
            form.addListener(SWT.MouseHorizontalWheel, new Listener() {

                public void handleEvent(Event event) {
                    if (!form.isDisposed()) {
                        int offset = event.count;
                        offset = -(int) (Math.sqrt(Math.abs(offset)) * offset);

                        Point origin = form.getOrigin();
                        form.setOrigin(origin.x + offset, origin.y);
                    }
                }
            });
        }
    }

    private Composite createDefaultPage(Composite parent) {
        Composite page = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        page.setLayout(gridLayout);

        Label label = new Label(page, SWT.LEFT | SWT.WRAP);
        label.setText(MindMapMessages.PropertiesPart_DefaultPage_message);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return page;
    }

    @SuppressWarnings("restriction")
    protected boolean postConfiguration(IWorkbenchPart workbenchPart,
            MPart part) {
        super.postConfiguration(workbenchPart, part);
        IWorkbenchPartSite site = workbenchPart.getSite();
        IGraphicalEditor editor = getContributedEditor();
        if (site instanceof IViewSite && editor != null) {
            IActionBars sourceActionBars = editor.getEditorSite()
                    .getActionBars();
            IActionBars targetActionBars = ((IViewSite) site).getActionBars();
            if (sourceActionBars == null || targetActionBars == null)
                return false;

            IServiceLocator serviceLocator = targetActionBars
                    .getServiceLocator();
            if (serviceLocator == null)
                return false;
            IEclipseContext eclipseContext = serviceLocator
                    .getService(IEclipseContext.class);
            eclipseContext.set(ECommandService.class,
                    serviceLocator.getService(ECommandService.class));
            eclipseContext.set(EHandlerService.class,
                    serviceLocator.getService(EHandlerService.class));

            retargetAction(sourceActionBars, targetActionBars,
                    ActionFactory.UNDO.getId());
            retargetAction(sourceActionBars, targetActionBars,
                    ActionFactory.REDO.getId());
            return true;
        }
        return false;
    }

    private void retargetAction(IActionBars sourceActionBars,
            IActionBars targetActionBars, String actionId) {
        IAction handler = sourceActionBars.getGlobalActionHandler(actionId);
        if (handler != null) {
            targetActionBars.setGlobalActionHandler(actionId, handler);
        }
    }

    private void setEditor(IGraphicalEditor editor) {
        if (this.sourceEditor == editor)
            return;

        if (this.sourceEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) sourceEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.removeAsyncSelectionChangedListener(this);
            }
        }

        this.sourceEditor = editor;

        if (viewerStack != null && !viewerStack.isDisposed()) {
            if (sourceEditor != null) {
                if (contentComposite != null
                        && !contentComposite.isDisposed()) {

                    initContent();
                    viewerStack.showPage(contentComposite);
                }
            } else {
                if (defaultPage != null && !defaultPage.isDisposed())
                    viewerStack.showPage(defaultPage);
            }
        }

    }

    public IGraphicalEditor getContributedEditor() {
        return sourceEditor;
    }

    private void addSection(String id, IPropertySectionPart section) {
        Assert.isNotNull(id);
        Assert.isNotNull(section);
        removeSection(id);
        SectionRecord rec = new SectionRecord(id, section);
        sections.add(rec);
        section.init(this, sourceEditor);
        if (form != null && !form.isDisposed()) {
            createSectionControl(form.getBody(), rec);
        }
    }

    private void removeSection(String id) {
        SectionRecord rec = getRec(id);
        if (rec == null)
            return;

        if (sections.remove(rec)) {
            rec.section.dispose();
            if (rec.control != null && !rec.control.isDisposed()) {
                rec.control.dispose();
            }
        }
    }

    private List<String> getSectionIds() {
        ArrayList<String> list = new ArrayList<String>(sections.size());
        for (SectionRecord rec : sections) {
            list.add(rec.id);
        }
        return list;
    }

    private List<String> getVisibleSectionIds() {
        ArrayList<String> list = new ArrayList<String>(sections.size());
        for (SectionRecord rec : sections) {
            if (rec.visible)
                list.add(rec.id);
        }
        return list;
    }

    private void setSectionVisible(String id, boolean visible) {
        SectionRecord rec = getRec(id);
        if (rec == null || rec.visible == visible)
            return;

        rec.visible = visible;
        if (rec.control != null && !rec.control.isDisposed()) {
            GridData gd = (GridData) rec.control.getLayoutData();
            gd.exclude = !visible;
            rec.control.setVisible(visible);
        }
    }

    private void reflow() {
        if (form != null && !form.isDisposed()) {
            form.reflow(true);
            form.getParent().layout();
        }
    }

    private void moveSectionFirst(SectionRecord rec) {
        if (rec.control != null && !rec.control.isDisposed()) {
            rec.control.moveAbove(null);
            rec.control.getParent().layout();
        }
    }

    private void moveSectionAfter(String id, String lastId) {
        SectionRecord rec = getRec(id);
        if (rec == null)
            return;
        SectionRecord lastRec = getRec(lastId);
        if (lastRec == null) {
            moveSectionFirst(rec);
        } else {
            if (rec.control != null && !rec.control.isDisposed()
                    && lastRec.control != null
                    && !lastRec.control.isDisposed()) {
                rec.control.moveBelow(lastRec.control);
                rec.control.getParent().layout();
            }
        }
    }

    private SectionRecord getRec(String id) {
        if (id == null)
            return null;

        for (SectionRecord rec : sections) {
            if (id.equals(rec.id))
                return rec;
        }
        return null;
    }

    private void createExtendSectionControls(WidgetFactory widgetFactory,
            Composite parent) {
        createResetStyleControl(widgetFactory, parent);
    }

    private void createResetStyleControl(WidgetFactory widgetFactory,
            Composite parent) {
        resetStyleControl = widgetFactory.createHyperlink(parent,
                MindMapMessages.MindMapPropertySheetPage_ResetStyle_text,
                SWT.NONE);
        resetStyleControl.setUnderlined(false);
        resetStyleControl.setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        resetStyleControl.addHyperlinkListener(new IHyperlinkListener() {

            public void linkExited(HyperlinkEvent e) {
                resetStyleControl.setUnderlined(false);
            }

            public void linkEntered(HyperlinkEvent e) {
                resetStyleControl.setUnderlined(true);
            }

            public void linkActivated(HyperlinkEvent e) {
                resetStyles();
            }
        });

        resetStyleControl.setFont((Font) resources.get(
                JFaceResources.getDefaultFontDescriptor().increaseHeight(-1)));
        resetStyleControl.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$
    }

    private boolean shouldHasResetStyleControl(ISelection selection) {
        if (!(selection instanceof StructuredSelection))
            return false;

        boolean result = true;

        Object[] resetedStyleds = ((StructuredSelection) selection).toArray();
        if (resetedStyleds != null) {
            for (Object styled : resetedStyleds) {
                if (!(styled instanceof IStyled)) {
                    result = false;
                }
            }
        }
        return result;
    }

    private void resetStyles() {
        IGraphicalEditor editor = getContributedEditor();
        if (editor == null)
            return;

        IGraphicalEditorPage activePageInstance = editor
                .getActivePageInstance();
        if (activePageInstance == null)
            return;

        ISelectionProvider selectionProvider = activePageInstance
                .getSelectionProvider();
        if (selectionProvider == null)
            return;

        ISelection selection = selectionProvider.getSelection();
        if (!(selection instanceof StructuredSelection))
            return;

        Object[] resetedStyleds = ((StructuredSelection) selection).toArray();
        if (resetedStyleds != null) {
            for (Object styled : resetedStyleds) {
                if (styled instanceof IStyled) {
                    IStyled resetedStyled = (IStyled) styled;
                    ModifyStyleCommand modifyStyleCommand = new ModifyStyleCommand(
                            resetedStyled, (String) null);
                    modifyStyleCommand
                            .setLabel(CommandMessages.Command_ModifyStyle);
                    editor.getCommandStack().execute(modifyStyleCommand);
                }
            }
        }
    }

    private void createSectionControls(final ScrolledForm form,
            final Composite formBody) {
        GridLayout layout = new GridLayout(1, true);
        formBody.setLayout(layout);
        for (SectionRecord rec : sections) {
            createSectionControl(formBody, rec);
        }
        form.addControlListener(new ControlListener() {
            public void controlResized(ControlEvent e) {
                relayout(form, formBody);
            }

            public void controlMoved(ControlEvent e) {
            }
        });
    }

    private void relayout(ScrolledForm form, Composite formBody) {
        Rectangle area = form.getClientArea();
        GridLayout layout = (GridLayout) formBody.getLayout();
        int newNumColumns = Math.max(1, area.width / DEFAULT_SECTION_WIDTH);
        boolean change = newNumColumns != layout.numColumns
                && newNumColumns >= 0
                && newNumColumns <= formBody.getChildren().length;
        if (change) {
            layout.numColumns = newNumColumns;
            formBody.layout();
        }
    }

    private void createSectionControl(Composite parent, SectionRecord rec) {
        rec.control = widgetFactory.createSection(parent,
                Section.TITLE_BAR | SWT.BORDER);
        Composite client = widgetFactory.createComposite(rec.control,
                SWT.NO_FOCUS | SWT.WRAP);
        rec.control.setClient(client);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = GridData.BEGINNING;
        data.widthHint = DEFAULT_SECTION_WIDTH;
        rec.control.setLayoutData(data);
        rec.section.createControl(client);
        rec.visible = true;
        updateSectionTitle(rec);
    }

    public void updateSectionTitle(IPropertySectionPart section) {
        SectionRecord rec = findRecord(section);
        if (rec != null) {
            updateSectionTitle(rec);
        }
    }

    private SectionRecord findRecord(IPropertySectionPart section) {
        for (SectionRecord rec : sections) {
            if (rec.section == section)
                return rec;
        }
        return null;
    }

    private void updateSectionTitle(SectionRecord rec) {
        if (rec.control == null || rec.control.isDisposed())
            return;

        String title = rec.section.getTitle();
        if (title == null) {
            title = ""; //$NON-NLS-1$
        }
        rec.control.setText(title);
    }

    public Control getControl() {
        return composite;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();

        selectionChanged(selection);

        if (composite != null && !composite.isDisposed())
            composite.setRedraw(false);
        setSelectionToSections(selection);
        if (form != null && !form.isDisposed()) {
            refresh();
        }
        if (composite != null && !composite.isDisposed())
            composite.setRedraw(true);
    }

    private void selectionChanged(ISelection selection) {
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(false);

        if (resetStyleControl != null && !resetStyleControl.isDisposed()) {
            boolean resetStyleControlVisible = shouldHasResetStyleControl(
                    selection);
            GridData gd = (GridData) resetStyleControl.getLayoutData();
            gd.exclude = !resetStyleControlVisible;
            resetStyleControl.setVisible(resetStyleControlVisible);
        }

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            List<String> newVisibleSectionIds = manager
                    .getApplicableSectionIds(ss.toArray());
            List<String> oldVisibleSectionIds = getVisibleSectionIds();
            if (!equalsList(oldVisibleSectionIds, newVisibleSectionIds)) {
                List<String> oldSectionIds = getSectionIds();
                List<String> toAdd = new ArrayList<String>(
                        newVisibleSectionIds);
                toAdd.removeAll(oldSectionIds);
                for (String id : toAdd) {
                    addSection(id, newVisibleSectionIds, oldSectionIds);
                    oldSectionIds = getSectionIds();
                }

                List<String> toHide = new ArrayList<String>(oldSectionIds);
                toHide.removeAll(newVisibleSectionIds);
                for (String id : oldSectionIds) {
                    setSectionVisible(id, !toHide.contains(id));
                }

                reflow();
            }
            if (ti != null && !ti.isDisposed()) {
                ti.setText(calcTitle(ss.toArray()) + " " + modeLabel); //$NON-NLS-1$
            }
        } else {
            if (ti != null && !ti.isDisposed()) {
                ti.setText(modeLabel);
            }
        }
        if (getControl() != null && !getControl().isDisposed())
            getControl().setRedraw(true);
    }

    private void addSection(String id, List<String> newVisibleSectionIds,
            List<String> oldSectionIds) {
        addSection(id, manager.createSection(id));
        String aboveId = findAboveId(id, oldSectionIds, newVisibleSectionIds);
        moveSectionAfter(id, aboveId);
    }

    private String findAboveId(String id, List<String> oldSectionIds,
            List<String> newSectionIds) {
        int index = newSectionIds.indexOf(id);
        for (int i = index - 1; i >= 0; i--) {
            String aboveId = newSectionIds.get(i);
            if (oldSectionIds.contains(aboveId))
                return aboveId;
        }
        return null;
    }

    private static boolean equalsList(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size())
            return false;
        for (int i = 0; i < list1.size(); i++) {
            String s1 = list1.get(i);
            String s2 = list2.get(i);
            if (!s1.equals(s2))
                return false;
        }
        return true;
    }

    private String calcTitle(Object[] objects) {
        if (objects == null || objects.length == 0)
            return null;
        String category = getCategoryName(objects);
        return category;
    }

    private String getCategoryName(Object[] objects) {
        ICategoryManager typeManager = MindMapUI.getCategoryManager();
        ICategoryAnalyzation result = typeManager.analyze(objects);
        return typeManager.getCategoryName(result.getMainCategory());
    }

    private void setSelectionToSections(ISelection selection) {
        for (SectionRecord rec : sections) {
            if (rec.visible) {
                rec.section.setSelection(selection);
                updateSectionTitle(rec);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void refresh() {
        for (SectionRecord rec : sections) {
            if (rec.visible) {
                rec.section.refresh();
            }
        }
        if (form != null && !form.isDisposed()) {
            form.reflow(true);
        }
    }

    public void dispose() {
        if (sourceEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) sourceEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.removeAsyncSelectionChangedListener(this);
            }
        }

        for (SectionRecord rec : sections) {
            rec.section.dispose();
            rec.visible = false;
            rec.control = null;
        }
        if (composite != null) {
            composite.dispose();
            composite = null;
        }
        form = null;
        title = null;
        super.dispose();
    }

    @Override
    protected void handlePartActivated(MPart part) {

        super.handlePartActivated(part);
        Object partObject = part.getObject();
        if (partObject instanceof CompatibilityPart) {
            IWorkbenchPart editorPart = ((CompatibilityPart) partObject)
                    .getPart();
            if (editorPart instanceof IGraphicalEditor) {
                setEditor((IGraphicalEditor) editorPart);
            }

            MPart partModel = getAdapter(MPart.class);
            Object wp = partModel.getTransientData()
                    .get(E4PartWrapper.E4_WRAPPER_KEY);
            if (wp instanceof E4PartWrapper) {
                postConfiguration((IWorkbenchPart) wp, partModel);
            }
        } else if (sourceEditor == null) {
            IWorkbenchWindow window = getAdapter(IWorkbenchWindow.class);
            IEditorPart editorPart = window.getActivePage().getActiveEditor();
            if (editorPart instanceof IGraphicalEditor) {
                setEditor((IGraphicalEditor) editorPart);
            }
        }
    }

    public IPageSite getContainerSite() {
        return null;
    }

    public IWorkbenchPart getContributingPart() {
        return sourceEditor;
    }

}
