package org.xmind.ui.internal.e4models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.comments.CommentTextViewer;
import org.xmind.ui.internal.comments.CommentsPartActionBarContributor;
import org.xmind.ui.internal.comments.CommentsSelectionProvider;
import org.xmind.ui.internal.comments.ICommentTextViewerContainer;
import org.xmind.ui.internal.comments.ICommentsActionBarContributor;
import org.xmind.ui.internal.comments.SheetCommentsViewer;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;

@SuppressWarnings("restriction")
public class CommentsPart extends ViewModelPart implements
        IContributedContentsView, IPartListener, ISelectionChangedListener,
        ICoreEventListener, ICommentTextViewerContainer {

    public static final String PART_ID = "org.xmind.ui.modelPart.comment"; //$NON-NLS-1$

    public static final String BG_COLOR = "#ffffff"; //$NON-NLS-1$

    private static final String COMMENTS_EDIT_CONTEXT_ID = "org.xmind.ui.context.comments.edit"; //$NON-NLS-1$

    private static final String INVALID_COLER = "#f0f0f0"; //$NON-NLS-1$

    private static class EActionHandler {

        private IAction action;

        public EActionHandler(IAction action) {
            this.action = action;
        }

        @Execute
        public void execute() {
            if (action.getStyle() == IAction.AS_CHECK_BOX
                    || action.getStyle() == IAction.AS_RADIO_BUTTON) {
                action.setChecked(!action.isChecked());
            }
            action.run();
        }

        @CanExecute
        public boolean canExecute() {
            return action.isEnabled();
        }
    }

    private class ContextActivator implements IPartListener {

        public ContextActivator() {
            getAdapter(IWorkbenchWindow.class).getActivePage()
                    .addPartListener(this);
        }

        public void partActivated(IWorkbenchPart part) {
            MPart modelPart = CommentsPart.this.getAdapter(MPart.class);
            Object e4Wrapper = modelPart.getTransientData()
                    .get(E4PartWrapper.E4_WRAPPER_KEY);
            if (part == e4Wrapper) {
                activateContext();
            }
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
            getAdapter(IWorkbenchWindow.class).getActivePage()
                    .removePartListener(this);
        }

        public void partDeactivated(IWorkbenchPart part) {
            MPart modelPart = CommentsPart.this.getAdapter(MPart.class);
            Object e4Wrapper = modelPart.getTransientData()
                    .get(E4PartWrapper.E4_WRAPPER_KEY);
            if (part == e4Wrapper) {
                deactivateContext();
            }
        }

        public void partOpened(IWorkbenchPart part) {
        }

        private void activateContext() {
            contextService.activateContext(COMMENTS_EDIT_CONTEXT_ID);
        }

        private void deactivateContext() {
            contextService.deactivateContext(COMMENTS_EDIT_CONTEXT_ID);
        }
    }

    private class TextAction extends Action {

        private TextViewer textViewer;

        private int op;

        public TextAction(int op) {
            this.op = op;
        }

        public void run() {
            if (textViewer != null && textViewer.canDoOperation(op)) {
                textViewer.doOperation(op);
                contentComposite.pack();
            }
        }

        public void update(TextViewer textViewer) {
            this.textViewer = textViewer;
            setEnabled(textViewer != null && textViewer.canDoOperation(op));
        }
    }

    private class CommitCommentHandler {

        @Execute
        public void execute() {
            saveComment();
        }
    }

    @Inject
    private EHandlerService handlerService;

    @Inject
    private EContextService contextService;

    private CommentsPartActionBarContributor contributor;

    private ISelectionProvider selectionProvider = new CommentsSelectionProvider();

    private List<TextAction> textActions = new ArrayList<TextAction>(7);

    private IGraphicalEditor contributingEditor;

    private ICoreEventRegister commentEventRegister;

    private ICoreEventRegister globalEventRegister;

    private Control control;

    private ScrolledComposite sc;

    private Composite contentComposite;

    private SheetCommentsViewer contentViewer;

    private ISheet sheet;

    private ControlListener controlListener;

    private CommitCommentHandler commitCommentHandler = new CommitCommentHandler();

    private IComment latestCreatedComment;

    private IComment selectedComment;

    private IComment editingComment;

    private boolean modified;

    private String insertTarget;

    private Map<String, Object> handlers = new HashMap<String, Object>();

    private Map<String, IAction> globalActions = new HashMap<String, IAction>(
            7);

    @Override
    protected Control doCreateContent(Composite parent) {
        contributor = new CommentsPartActionBarContributor(this,
                contributingEditor);

        control = createControl(parent);
        activateHandlers();
        new ContextActivator();
        getAdapter(IWorkbenchWindow.class).getActivePage()
                .addPartListener(this);
        return control;
    }

    private Composite createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor(BG_COLOR)));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        contentComposite = createContentComposite(composite);

        return composite;
    }

    private Composite createContentComposite(Composite parent) {
        sc = new ScrolledComposite(parent, SWT.V_SCROLL);
        sc.setBackground(parent.getBackground());
        sc.setLayoutData(new GridData(GridData.FILL_BOTH));
        sc.setExpandHorizontal(true);

        final Composite composite = new Composite(sc, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 0;
        composite.setLayout(gridLayout);

        sc.setContent(composite);
        sc.getVerticalBar().setIncrement(17);

        sc.addControlListener(getControlListener());

        return composite;
    }

    private ControlListener getControlListener() {
        if (controlListener == null) {
            controlListener = new ControlListener() {

                public void controlMoved(ControlEvent e) {
                }

                public void controlResized(ControlEvent e) {
                    e.widget.getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            if (contentComposite != null
                                    && !contentComposite.isDisposed()) {
                                contentComposite.pack();
                            }
                        }
                    });
                }
            };
        }

        return controlListener;
    }

    private void setInitialInput() {
        IEditorPart activeEditor = getAdapter(IWorkbenchWindow.class)
                .getActivePage().getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) activeEditor);
        } else {
            setInput(null);
        }
    }

    private void setInput(ISheet sheet) {
        if (this.sheet == sheet) {
            return;
        }
        this.sheet = sheet;
        unhookSheet();
        update();
        hookSheet();
    }

    private void hookSheet() {
        if (sheet != null) {
            if (commentEventRegister == null)
                commentEventRegister = new CoreEventRegister(sheet
                        .getOwnedWorkbook().getAdapter(ICoreEventSupport.class),
                        this);
            commentEventRegister.register(Core.CommentAdd);
            commentEventRegister.register(Core.CommentRemove);

            registerGlobalEvent();
        }
    }

    private void unhookSheet() {
        if (commentEventRegister != null) {
            commentEventRegister.unregisterAll();
            commentEventRegister = null;
        }
        unRegisterGlobalEvent();
    }

    private void registerGlobalEvent() {
        globalEventRegister = new CoreEventRegister(
                sheet.getOwnedWorkbook().getAdapter(ICoreEventSupport.class),
                this);
        globalEventRegister.register(Core.CommentContent);
    }

    private void unRegisterGlobalEvent() {
        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
            globalEventRegister = null;
        }
    }

    private void update() {
        resetSelectedComment();
        updateCompositeEnabled();
        updateComments();
        setModified(false);
        setEditingComment(null);
        setInsertTarget(null);
    }

    private void resetSelectedComment() {
        contributor.selectedCommentChanged(null);
    }

    private void updateCompositeEnabled() {
        sc.setEnabled(sheet != null);
        if (sheet == null) {
            contentComposite.getParent().setBackground((Color) resources
                    .get(ColorUtils.toDescriptor(INVALID_COLER)));
            contentComposite.setBackground((Color) resources
                    .get(ColorUtils.toDescriptor(INVALID_COLER)));
        } else {
            contentComposite.getParent().setBackground(control.getBackground());
            contentComposite.setBackground(control.getBackground());
        }
    }

    private void updateComments() {
        contentComposite.setRedraw(false);
        clearContent();
        selectionProvider.setSelection(null);

        if (contentViewer != null) {
            contentViewer.dispose();
            contentViewer = null;
        }
        if (sheet != null) {
            contentViewer = new SheetCommentsViewer(sheet, contributor,
                    selectionProvider, this, contributingEditor);
            contentViewer.create(contentComposite);
        }
        contentComposite.pack(true);
        contentComposite.layout(true, true);
        contentComposite.setRedraw(true);
    }

    private void clearContent() {
        Control[] controls = contentComposite.getChildren();
        if (controls != null) {
            for (Control control : controls) {
                if (control != null && !control.isDisposed()) {
                    control.dispose();
                    control = null;
                }
            }
        }
    }

    protected boolean postConfiguration(IWorkbenchPart workbenchPart,
            MPart part) {
        // prevent PartRenderingEngine's limbo shell.
        setInitialInput();

        super.postConfiguration(workbenchPart, part);
        IWorkbenchPartSite site = workbenchPart.getSite();
        if (site instanceof IViewSite) {
            IActionBars actionBars = ((IViewSite) site).getActionBars();
            if (actionBars == null) {
                return false;
            }

            IServiceLocator serviceLocator = actionBars.getServiceLocator();
            if (serviceLocator == null)
                return false;
            IEclipseContext eclipseContext = serviceLocator
                    .getService(IEclipseContext.class);
            eclipseContext.set(ECommandService.class,
                    serviceLocator.getService(ECommandService.class));
            eclipseContext.set(EHandlerService.class,
                    serviceLocator.getService(EHandlerService.class));

            registerGlobalTextActionHandlers();
            return true;
        }
        return false;
    }

    private void registerGlobalTextActionHandlers() {
        activateGlobalTextHandler(ActionFactory.UNDO,
                ITextOperationTarget.UNDO);
        activateGlobalTextHandler(ActionFactory.REDO,
                ITextOperationTarget.REDO);
        activateGlobalTextHandler(ActionFactory.CUT, ITextOperationTarget.CUT);
        activateGlobalTextHandler(ActionFactory.COPY,
                ITextOperationTarget.COPY);
        activateGlobalTextHandler(ActionFactory.PASTE,
                ITextOperationTarget.PASTE);
        activateGlobalTextHandler(ActionFactory.SELECT_ALL,
                ITextOperationTarget.SELECT_ALL);
    }

    private void activateGlobalTextHandler(ActionFactory actionFactory,
            int textOp) {
        TextAction textAction = new TextAction(textOp);
        String commandId = actionFactory.getCommandId();
        textAction.setActionDefinitionId(commandId);
        Object handler = new EActionHandler(textAction);
        handlerService.activateHandler(commandId, handler);
        handlers.put(commandId, handler);

        textAction.setId(actionFactory.getId());
        IWorkbenchAction workbenchAction = actionFactory
                .create(getAdapter(IWorkbenchWindow.class));
        textAction.setText(workbenchAction.getText());
        workbenchAction.dispose();
        textActions.add(textAction);
        globalActions.put(actionFactory.getId(), textAction);
    }

    private void unregisterGlobalTextActionHandlers() {
        if (handlerService != null) {
            for (Entry<String, Object> entry : handlers.entrySet()) {
                handlerService.deactivateHandler(entry.getKey(),
                        entry.getValue());
            }
            handlers.clear();
        }
    }

    public IAction getGlobalAction(String actionId) {
        return globalActions.get(actionId);
    }

    public void updateTextActions(TextViewer textViewer) {
        if (textViewer != null) {
            for (TextAction action : textActions) {
                action.update(textViewer);
            }
        }
    }

    private void activateHandlers() {
        handlerService.activateHandler("org.xmind.ui.command.commitComments", //$NON-NLS-1$
                commitCommentHandler);
    }

    private void deactivateHandlers() {
        handlerService.deactivateHandler("org.xmind.ui.command.commitComments", //$NON-NLS-1$
                commitCommentHandler);
    }

    private void saveComment() {
        if (contentViewer != null) {
            contentViewer.save();
        }
    }

    protected void setFocus() {
        super.setFocus();
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    private void setContributingEditor(IGraphicalEditor editor) {
        if (editor == contributingEditor) {
            return;
        }

        if (contributingEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) contributingEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null)
                selectionProvider.removeAsyncSelectionChangedListener(this);
        }

        contributingEditor = editor;

        if (contributingEditor != null) {
            DelegatedSelectionProvider selectionProvider = (DelegatedSelectionProvider) contributingEditor
                    .getSite().getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.addAsyncSelectionChangedListener(this);
            }
        }

        if (getControl().isDisposed()) {
            return;
        }
        if (contentViewer != null) {
            contentViewer.setTargetEditor(contributingEditor);
        }
        if (contributor != null) {
            contributor.setTargetEditor(contributingEditor);
        }

        ISheet sheet = getSheet(contributingEditor);
        setInput(sheet);
    }

    public void partActivated(IWorkbenchPart part) {
        if (part instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) part);
        }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
        if (part == this.contributingEditor) {
            setContributingEditor(null);
        }
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void selectionChanged(SelectionChangedEvent event) {
        ISheet sheet = getSheet(contributingEditor);
        setInput(sheet);
    }

    private ISheet getSheet(IGraphicalEditor editor) {
        if (editor != null && editor.getActivePageInstance() != null) {
            return (ISheet) editor.getActivePageInstance()
                    .getAdapter(ISheet.class);
        } else {
            return null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        final String type = event.getType();
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                if (!contentComposite.isDisposed()) {
                    if (Core.CommentAdd.equals(type)
                            || Core.CommentRemove.equals(type)) {
                        update();
                    } else if (Core.CommentContent.equals(type)) {
                        IComment comment = (IComment) event.getSource();
                        if (comment.isOrphan()) {
                            return;
                        }
                        Object source = comment.getOwnedWorkbook()
                                .getElementById(comment.getObjectId());
                        if ((source instanceof ITopic
                                && ((ITopic) source).getOwnedSheet() == sheet)
                                || source instanceof ISheet
                                        && source == sheet) {
                            update();
                        }
                    }
                }
            }
        });
    }

    public void moveToPreviousTextViewer(CommentTextViewer implementation) {
        List<CommentTextViewer> implementations = contentViewer
                .getImplementations();
        int index = implementations.indexOf(implementation);
        if (index <= 0 || index > implementations.size() - 1) {
            return;
        }

        setSelection(new StructuredSelection(implementations.get(index - 1)));
    }

    public void moveToNextTextViewer(CommentTextViewer implementation) {
        List<CommentTextViewer> implementations = contentViewer
                .getImplementations();
        int index = implementations.indexOf(implementation);
        if (index < 0 || index >= implementations.size() - 1) {
            return;
        }

        setSelection(new StructuredSelection(implementations.get(index + 1)));
    }

    private void setSelection(ISelection selection) {
        selectionProvider.setSelection(selection);
    }

    //It can be only used in getAdapter() for findReplaceAction.
    private CommentTextViewer getImplementation() {
        if (selectionProvider instanceof CommentsSelectionProvider) {
            ISelection selection = selectionProvider.getSelection();
            if (selection instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection) selection)
                        .getFirstElement();
                if (obj instanceof CommentTextViewer) {
                    return (CommentTextViewer) obj;
                }
            }

            ISelection oldSelection = ((CommentsSelectionProvider) selectionProvider)
                    .getOldSelection();
            if (oldSelection instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection) oldSelection)
                        .getFirstElement();
                if (obj instanceof CommentTextViewer) {
                    return (CommentTextViewer) obj;
                }
            }
        }

        return null;
    }

    public Composite getContentComposite() {
        return contentComposite;
    }

    public ScrolledComposite getScrolledComposite() {
        return sc;
    }

    public ICommentsActionBarContributor getContributor() {
        return contributor;
    }

    public void setLatestCreatedComment(IComment latestCreatedComment) {
        this.latestCreatedComment = latestCreatedComment;
    }

    public IComment getLatestCreatedComment() {
        return latestCreatedComment;
    }

    public void setSelectedComment(IComment selectedComment) {
        this.selectedComment = selectedComment;
    }

    public IComment getSelectedComment() {
        return selectedComment;
    }

    public Control getControl() {
        return control;
    }

    @Override
    public void createComment(String objectId) {
        contentViewer.createNewComment(objectId);
    }

    @Override
    public void cancelCreateComment() {
        contentViewer.cancelCreateNewComment();
    }

    public void setEditingComment(IComment editingComment) {
        this.editingComment = editingComment;
    }

    public IComment getEditingComment() {
        return editingComment;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

    public void setInsertTarget(String objectId) {
        this.insertTarget = objectId;
    }

    public String getInsertTarget() {
        return insertTarget;
    }

    public IWorkbenchPart getContributingPart() {
        return contributingEditor;
    }

    public void dispose() {
        unhookSheet();
        deactivateHandlers();
        if (contributor != null) {
            contributor.dispose();
        }
        if (sc != null && !sc.isDisposed()) {
            sc.removeControlListener(getControlListener());
        }
        getAdapter(IWorkbenchWindow.class).getActivePage()
                .removePartListener(this);
        setContributingEditor(null);
        unregisterGlobalTextActionHandlers();
        if (contentViewer != null) {
            contentViewer.dispose();
            contentViewer = null;
        }

        super.dispose();

        textActions = null;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IContributedContentsView.class) {
            return adapter.cast(this);
        } else if (adapter == ITextViewer.class) {
            return adapter.cast(getImplementation() == null ? null
                    : getImplementation().getTextViewer());
        }
        return super.getAdapter(adapter);
    }

}
