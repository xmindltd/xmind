package org.xmind.ui.internal.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;
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
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.resources.ColorUtils;

public class CommentsView extends ViewPart implements IContributedContentsView,
        IPartListener, ISelectionChangedListener, ICoreEventListener,
        ICommentTextViewerContainer {

    public static final Color BG_COLOR = ColorUtils.getColor("#ffffff"); //$NON-NLS-1$

    private static final String COMMENTS_EDIT_CONTEXT_ID = "org.xmind.ui.context.comments.edit"; //$NON-NLS-1$

    private static final Color INVALID_COLER = ColorUtils.getColor("#f0f0f0"); //$NON-NLS-1$

    private class ContextActivator implements IPartListener {
        IContextActivation context;
        IContextService service;

        public ContextActivator() {
            getSite().getPage().addPartListener(this);
        }

        private void activateContext() {
            if (service == null)
                service = (IContextService) getSite()
                        .getService(IContextService.class);
            if (service != null) {
                context = service.activateContext(COMMENTS_EDIT_CONTEXT_ID);
            }
        }

        private void deactivateContext() {
            if (service != null && context != null)
                service.deactivateContext(context);
            context = null;
        }

        public void partActivated(IWorkbenchPart part) {
            if (part instanceof CommentsView) {
                activateContext();
            }
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
            getSite().getPage().removePartListener(this);
        }

        public void partDeactivated(IWorkbenchPart part) {
            if (part instanceof CommentsView) {
                deactivateContext();
            }
        }

        public void partOpened(IWorkbenchPart part) {
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

    private class CommitCommentHandler extends AbstractHandler {

        public Object execute(ExecutionEvent event) throws ExecutionException {
            saveComment();
            return null;
        }
    }

    private CommentsPartActionBarContributor contributor;

    private ISelectionProvider selectionProvider = new CommentsSelectionProvider();

    private Map<String, IWorkbenchAction> workbenchActions = new HashMap<String, IWorkbenchAction>(
            7);

    private List<TextAction> textActions = new ArrayList<TextAction>(7);

    private IGraphicalEditor contributingEditor;

    private ITopicPart currentTopicPart;

    private ICoreEventRegister commentEventRegister;

    private ICoreEventRegister globalEventRegister;

    private ScrolledComposite sc;

    private Composite contentComposite;

    private SheetCommentsViewer contentViewer;

    private ISheet sheet;

    private Control control;

    private ControlListener controlListener;

    private IHandlerActivation commitHandlerActivation;

    private IComment latestCreatedComment;

    private IComment selectedComment;

    public void createPartControl(Composite parent) {
        contributor = new CommentsPartActionBarContributor(null,
                contributingEditor);
        control = createControl(parent);
        setInitialInput();

        activateHandlers();
        IActionBars actionBars = getViewSite().getActionBars();
        createActions(actionBars);
        new ContextActivator();

        getSite().getPage().addPartListener(this);
    }

    private void activateHandlers() {
        IHandlerService handlerService = (IHandlerService) getSite()
                .getService(IHandlerService.class);
        if (handlerService != null) {
            commitHandlerActivation = handlerService.activateHandler(
                    "org.xmind.ui.command.commitComments", //$NON-NLS-1$
                    new CommitCommentHandler());
        }
    }

    private void deactivateHandlers() {
        if (commitHandlerActivation != null) {
            commitHandlerActivation.getHandlerService()
                    .deactivateHandler(commitHandlerActivation);
            commitHandlerActivation = null;
        }
    }

    private void saveComment() {
        if (contentViewer != null) {
            contentViewer.save();
        }
    }

    private void setInitialInput() {
        IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (activeEditor instanceof IGraphicalEditor) {
            setContributingEditor((IGraphicalEditor) activeEditor);
        } else {
            setInput(null);
        }
    }

    private Composite createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(BG_COLOR);

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
        gridLayout.marginBottom = 29;
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
                commentEventRegister = new CoreEventRegister(
                        sheet.getAdapter(ICoreEventSupport.class), this);
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
    }

    private void resetSelectedComment() {
        contributor.selectedCommentChanged(null);
    }

    private void updateCompositeEnabled() {
        sc.setEnabled(sheet != null);
        if (sheet == null) {
            contentComposite.getParent().setBackground(INVALID_COLER);
            contentComposite.setBackground(INVALID_COLER);
        } else {
            contentComposite.getParent().setBackground(control.getBackground());
            contentComposite.setBackground(control.getBackground());
        }
    }

    private void updateComments() {
        contentComposite.setRedraw(false);
        clearContent();
        selectionProvider.setSelection(null);

        if (sheet != null) {
            contentViewer = new SheetCommentsViewer(sheet, contributor,
                    selectionProvider, this, contributingEditor);
            contentViewer.create(contentComposite);
        }
        contentComposite.pack();
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

    private void createActions(IActionBars actionBars) {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        addGlobalTextAction(actionBars, window, ActionFactory.UNDO,
                ITextOperationTarget.UNDO);
        addGlobalTextAction(actionBars, window, ActionFactory.REDO,
                ITextOperationTarget.REDO);
        addGlobalTextAction(actionBars, window, ActionFactory.CUT,
                ITextOperationTarget.CUT);
        addGlobalTextAction(actionBars, window, ActionFactory.COPY,
                ITextOperationTarget.COPY);
        addGlobalTextAction(actionBars, window, ActionFactory.PASTE,
                ITextOperationTarget.PASTE);
        addGlobalTextAction(actionBars, window, ActionFactory.SELECT_ALL,
                ITextOperationTarget.SELECT_ALL);
    }

    private void addGlobalTextAction(IActionBars actionBars,
            IWorkbenchWindow window, ActionFactory actionFactory, int textOp) {
        IWorkbenchAction action = actionFactory.create(window);
        workbenchActions.put(action.getId(), action);
        TextAction textAction = new TextAction(textOp);
        textActions.add(textAction);
        actionBars.setGlobalActionHandler(action.getId(), textAction);
    }

    public IWorkbenchAction getGlobalAction(String actionId) {
        return workbenchActions == null ? null : workbenchActions.get(actionId);
    }

    public void updateTextActions(TextViewer textViewer) {
        if (textViewer != null) {
            for (TextAction action : textActions) {
                action.update(textViewer);
            }
        }
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
        getSite().getPage().removePartListener(this);
        setContributingEditor(null);

        super.dispose();

        if (workbenchActions != null) {
            for (IWorkbenchAction action : workbenchActions.values()) {
                action.dispose();
            }
            workbenchActions = null;
        }
        textActions = null;
    }

    public void setFocus() {
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IContributedContentsView.class) {
            return this;
        } else if (adapter == ITextViewer.class) {
            return getImplementation() == null ? null
                    : getImplementation().getTextViewer();
        } else if (adapter == ITopicPart.class) {
            return currentTopicPart;
        } else if (adapter == ITopic.class) {
            return currentTopicPart == null ? null
                    : currentTopicPart.getTopic();
        }
        return super.getAdapter(adapter);
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

    private void setContributingEditor(IGraphicalEditor editor) {
        if (editor == contributingEditor) {
            return;
        }

        if (contributingEditor != null) {
            ISelectionProvider selectionProvider = contributingEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null)
                selectionProvider.removeSelectionChangedListener(this);
        }

        contributingEditor = editor;

        if (contributingEditor != null) {
            ISelectionProvider selectionProvider = contributingEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null) {
                selectionProvider.addSelectionChangedListener(this);
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

    public IWorkbenchPart getContributingPart() {
        return contributingEditor;
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

    @Override
    public void setEditingComment(IComment comment) {
    }

    @Override
    public IComment getEditingComment() {
        return null;
    }

    @Override
    public void setModified(boolean modified) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

}
