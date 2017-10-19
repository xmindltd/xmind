package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.keys.IBindingService;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.CommentsPart;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class CommentsPopup extends PopupDialog
        implements ICoreEventListener, ICommentTextViewerContainer {

    public static final Color BG_COLOR = ColorUtils.getColor("#f5f5f5"); //$NON-NLS-1$

    private static final String CONTEXT_ID = "org.xmind.ui.context.commentsPopup"; //$NON-NLS-1$

    private static final String CMD_GOTO_COMMENTS_VIEW = "org.xmind.ui.command.gotoCommentsView"; //$NON-NLS-1$

    private static final String CMD_COMMIT_COMMENTS = "org.xmind.ui.command.commitComments"; //$NON-NLS-1$

    private class PopupKeyboardListener implements Listener {

        private List<TriggerSequence> currentSequences = null;

        private DisposeListener disposeListener = new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (!e.display.isDisposed()) {
                    e.display.removeFilter(SWT.KeyDown,
                            PopupKeyboardListener.this);
                }
            }
        };

        private int nextKeyIndex = -1;

        public void hook(Control control) {
            control.getDisplay().addFilter(SWT.KeyDown, this);
            control.getShell().addDisposeListener(disposeListener);
        }

        public void handleEvent(Event event) {
            if (event.type == SWT.KeyDown) {
                handleKeyDown(event);
            }
        }

        private void handleKeyDown(Event event) {
            if (triggerableCommands.isEmpty())
                return;

            List<KeyStroke> keys = generateKeyStrokes(event);
            if (currentSequences == null) {
                nextKeyIndex = -1;
                for (TriggerSequence ts : triggerableCommands.keySet()) {
                    if (matches(keys, ts.getTriggers()[0])) {
                        if (currentSequences == null)
                            currentSequences = new ArrayList<TriggerSequence>(
                                    triggerableCommands.size());
                        currentSequences.add(ts);
                    }
                }
                if (currentSequences == null)
                    return;
            }

            if (nextKeyIndex < 0)
                nextKeyIndex = 0;
            Iterator<TriggerSequence> it = currentSequences.iterator();
            while (it.hasNext()) {
                TriggerSequence ts = it.next();
                Trigger[] triggers = ts.getTriggers();
                if (nextKeyIndex >= triggers.length) {
                    it.remove();
                } else {
                    if (matches(keys, triggers[nextKeyIndex])) {
                        if (nextKeyIndex == triggers.length - 1) {
                            if (triggerFound(ts)) {
                                event.doit = false;
                            }
                            return;
                        }
                    } else {
                        it.remove();
                    }
                }
            }
            if (currentSequences != null && currentSequences.isEmpty()) {
                nextKeyIndex++;
            } else {
                currentSequences = null;
                nextKeyIndex = -1;
            }
        }

        private boolean triggerFound(TriggerSequence triggerSequence) {
            currentSequences = null;
            nextKeyIndex = -1;
            String commandId = triggerableCommands.get(triggerSequence);
            if (commandId != null) {
                return handleCommand(commandId);
            }
            return false;
        }

        private boolean matches(List<KeyStroke> keys, Trigger expected) {
            for (KeyStroke key : keys) {
                if (key.equals(expected))
                    return true;
            }
            return false;
        }

        private List<KeyStroke> generateKeyStrokes(Event event) {
            final List<KeyStroke> keyStrokes = new ArrayList<KeyStroke>(3);

            /*
             * If this is not a keyboard event, then there are no key strokes.
             * This can happen if we are listening to focus traversal events.
             */
            if ((event.stateMask == 0) && (event.keyCode == 0)
                    && (event.character == 0)) {
                return keyStrokes;
            }

            // Add each unique key stroke to the list for consideration.
            final int firstAccelerator = SWTKeySupport
                    .convertEventToUnmodifiedAccelerator(event);
            keyStrokes.add(SWTKeySupport
                    .convertAcceleratorToKeyStroke(firstAccelerator));

            // We shouldn't allow delete to undergo shift resolution.
            if (event.character == SWT.DEL) {
                return keyStrokes;
            }

            final int secondAccelerator = SWTKeySupport
                    .convertEventToUnshiftedModifiedAccelerator(event);
            if (secondAccelerator != firstAccelerator) {
                keyStrokes.add(SWTKeySupport
                        .convertAcceleratorToKeyStroke(secondAccelerator));
            }

            final int thirdAccelerator = SWTKeySupport
                    .convertEventToModifiedAccelerator(event);
            if ((thirdAccelerator != secondAccelerator)
                    && (thirdAccelerator != firstAccelerator)) {
                keyStrokes.add(SWTKeySupport
                        .convertAcceleratorToKeyStroke(thirdAccelerator));
            }

            return keyStrokes;
        }
    }

    @Inject
    private EPartService partService;

    private IWorkbenchWindow window;

    private ITopicPart topicPart;

    private ITopic topic;

    private boolean showExtraActions;

    private Control control;

    private CommentsPopupActionBarContributor contributor;

    private ISelectionProvider selectionProvider = new CommentsSelectionProvider();

    private PopupKeyboardListener popupKeyBoardListener;

    private IBindingService bindingService;

    private IContextService contextService;

    private IContextActivation contextActivation;

    private Map<TriggerSequence, String> triggerableCommands = new HashMap<TriggerSequence, String>(
            3);

    private ScrolledComposite sc;

    private Composite contentComposite;

    private TopicCommentsViewer contentViewer;

    private ToolBarManager toolBarManager;

    private ICoreEventRegister eventRegister;

    private ICoreEventRegister globalEventRegister;

    private ControlListener controlListener;

    private IComment latestCreatedComment;

    private IComment selectedComment;

    private IComment editingComment;

    private boolean modified;

    public CommentsPopup(IWorkbenchWindow window, ITopicPart topicPart,
            boolean showExtraActions) {
        super(window.getShell(), SWT.RESIZE, true, true, true, false, false,
                null, null);
        this.window = window;
        this.topicPart = topicPart;
        this.showExtraActions = showExtraActions;
        this.topic = topicPart.getTopic();
    }

    @Override
    protected Point getDefaultSize() {
        return new Point(350, 250);
    }

    @Override
    protected Color getBackground() {
        return BG_COLOR;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        IViewer viewer = topicPart.getSite().getViewer();
        Rectangle bounds = topicPart.getFigure().getBounds().getCopy();
        return calcInitialLocation((IGraphicalViewer) viewer, bounds);
    }

    private Point calcInitialLocation(IGraphicalViewer viewer,
            Rectangle bounds) {
        ZoomManager zoom = viewer.getZoomManager();
        bounds = bounds.scale(zoom.getScale()).expand(1, 1)
                .translate(viewer.getScrollPosition().getNegated());
        return viewer.getControl().toDisplay(bounds.x,
                bounds.y + bounds.height);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List getForegroundColorExclusions() {
        List list = super.getForegroundColorExclusions();
        collectColorExclusions(control, list);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List getBackgroundColorExclusions() {
        List list = super.getBackgroundColorExclusions();
        collectColorExclusions(control, list);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void collectColorExclusions(Control control, List list) {
        list.add(control);
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) {
                collectColorExclusions(child, list);
            }
        }
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return MindMapUIPlugin.getDefault()
                .getDialogSettings(MindMapUI.POPUP_DIALOG_SETTINGS_ID);
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        this.control = composite;
        composite.setBackground(getBackground());
        composite.setForeground(getForeground());

        contributor = new CommentsPopupActionBarContributor(this,
                getTargetEditor());
        Control control = createControl(composite);

        update();
        popupKeyBoardListener = new PopupKeyboardListener();
        popupKeyBoardListener.hook(control);
        setInfoText(null);

        hookTopic();
        registerGlobalEvent();
        initActions();

        return composite;
    }

    private IGraphicalEditor getTargetEditor() {
        if (window != null) {
            IEditorPart editorPart = window.getActivePage().getActiveEditor();
            if (editorPart instanceof IGraphicalEditor) {
                return (IGraphicalEditor) editorPart;
            }
        }
        return null;
    }

    private void registerGlobalEvent() {
        globalEventRegister = new CoreEventRegister(
                topic.getOwnedWorkbook().getAdapter(ICoreEventSupport.class),
                this);
        globalEventRegister.register(Core.CommentContent);
    }

    private void unRegisterGlobalEvent() {
        if (globalEventRegister != null) {
            globalEventRegister.unregisterAll();
            globalEventRegister = null;
        }
    }

    private void hookTopic() {
        if (eventRegister == null) {
            eventRegister = new CoreEventRegister(topic, this);
        }
        eventRegister.register(Core.CommentAdd);
        eventRegister.register(Core.CommentRemove);
    }

    private void unhookTopic() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
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
                        if (comment.getOwnedWorkbook().getElementById(
                                comment.getObjectId()) == topic) {
                            update();
                        }
                    }
                }
            }
        });
    }

    private void initActions() {
        contributor.selectionChanged(topic);
    }

    @Override
    public boolean close() {
        unhookTopic();
        unRegisterGlobalEvent();
        if (contextActivation != null && contextService != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
        if (contributor != null) {
            contributor.dispose();
        }
        if (sc != null && !sc.isDisposed()) {
            sc.removeControlListener(getControlListener());
        }
        if (getReturnCode() == OK) {
            saveComment();
        }
        //mark with CommentsView
        if (partService != null) {
            MPart part = partService.findPart(CommentsPart.PART_ID);
            if (part.isVisible()) {
                Object object = part.getObject();
                if (object instanceof CommentsPart) {
                    Control control = ((CommentsPart) object).getControl();
                    if (control != null && !control.isDisposed()) {
                        control.setData(CommentsConstants.COMMENTS_POPUP_SHOWN,
                                false);
                    }
                }
            }
        }
        return super.close();
    }

    private Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createToolbar(composite);
        contentComposite = createContentComposite(composite);

        return composite;
    }

    private void createToolbar(Composite parent) {
        if (contributor == null) {
            return;
        }

        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.heightHint = 30;
        composite.setLayoutData(gridData);
        composite.setBackground(ColorUtils.getColor("#e0e0e0")); //$NON-NLS-1$

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setBackground(label.getParent().getBackground());
        GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, true);
        layoutData.horizontalIndent = 10;
        label.setLayoutData(layoutData);
        label.setText(MindMapMessages.Comments_lable);
        label.setFont(FontUtils.getBold(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 1)));

        toolBarManager = new ToolBarManager(SWT.FLAT);
        contributor.fillToolBar(toolBarManager);
        composite.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                toolBarManager.update(true);
            }
        });

        ToolBar toolBar = toolBarManager.createControl(composite);
        toolBar.setBackground(toolBar.getParent().getBackground());
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
    }

    private Composite createContentComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        sc = new ScrolledComposite(composite, SWT.V_SCROLL);
        sc.setBackground(parent.getBackground());
        sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sc.setExpandHorizontal(true);

        final Composite contentComposite = new Composite(sc, SWT.NONE);
        contentComposite.setBackground(parent.getBackground());

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginBottom = 29;
        contentComposite.setLayout(gridLayout);

        sc.setContent(contentComposite);
        sc.getVerticalBar().setIncrement(17);

        sc.addControlListener(getControlListener());

        return contentComposite;
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

    private void update() {
        resetSelectedComment();
        updateComments();
        setModified(false);
        setEditingComment(null);
    }

    private void resetSelectedComment() {
        contributor.selectedCommentChanged(null);
    }

    private void updateComments() {
        selectionProvider.setSelection(null);
        contentComposite.setRedraw(false);
        resetContent();

        contentViewer = new TopicCommentsViewer(topic, contributor,
                selectionProvider, this, false, getTargetEditor());
        contentViewer.create(contentComposite);
        contentComposite.pack();
        contentComposite.setRedraw(true);
    }

    private void resetContent() {
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

    public int open() {
        IWorkbench workbench = window.getWorkbench();
        bindingService = (IBindingService) workbench
                .getAdapter(IBindingService.class);
        contextService = (IContextService) workbench
                .getAdapter(IContextService.class);
        if (bindingService != null) {
            registerWorkbenchCommands();
        }
        int ret = super.open();
        if (ret == OK) {
            if (contextService != null) {
                contextActivation = contextService.activateContext(CONTEXT_ID);
            }
            if (bindingService != null) {
                registerDialogCommands();
            }
        }
        //mark with CommentsView
        if (partService != null) {
            MPart part = partService.findPart(CommentsPart.PART_ID);
            if (part.isVisible()) {
                Object object = part.getObject();
                if (object instanceof CommentsPart) {
                    Control control = ((CommentsPart) object).getControl();
                    if (control != null && !control.isDisposed()) {
                        control.setData(CommentsConstants.COMMENTS_POPUP_SHOWN,
                                true);
                    }
                }
            }
        }

        return ret;
    }

    private void registerWorkbenchCommands() {
        registerCommand(IWorkbenchCommandConstants.FILE_SAVE);
        registerCommand(IWorkbenchCommandConstants.EDIT_UNDO);
        registerCommand(IWorkbenchCommandConstants.EDIT_REDO);
        registerCommand(IWorkbenchCommandConstants.EDIT_CUT);
        registerCommand(IWorkbenchCommandConstants.EDIT_COPY);
        registerCommand(IWorkbenchCommandConstants.EDIT_PASTE);
        registerCommand(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
    }

    private TriggerSequence registerCommand(String commandId) {
        if (bindingService == null)
            return null;
        TriggerSequence key = bindingService.getBestActiveBindingFor(commandId);
        if (key != null) {
            triggerableCommands.put(key, commandId);
        }
        return key;
    }

    @Override
    protected Control getFocusControl() {
        return contentComposite;
    }

    private void registerDialogCommands() {
        if (showExtraActions) {
            registerCommand(CMD_GOTO_COMMENTS_VIEW);
        }
        registerCommand(CMD_COMMIT_COMMENTS);
        for (String commandId : contributor.getTextCommandIds()) {
            registerCommand(commandId);
        }
    }

    private void saveComment() {
        if (contentViewer != null) {
            contentViewer.save();
        }
    }

    private boolean handleCommand(String commandId) {
        if (CMD_GOTO_COMMENTS_VIEW.equals(commandId)) {
            if (showExtraActions) {
                gotoCommentsView();
            }
            return true;
        } else if (CMD_COMMIT_COMMENTS.equals(commandId)) {
            saveComment();
            return true;
        } else if (IWorkbenchCommandConstants.FILE_SAVE.equals(commandId)) {
            saveComment();
            return true;
        }
        IAction action = contributor.getActionHandler(commandId);
        if (action != null && action.isEnabled()) {
            if (action.getStyle() == IAction.AS_CHECK_BOX) {
                action.setChecked(!action.isChecked());
            }
            action.run();
            return true;
        }
        return false;
    }

    public void gotoCommentsView() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                if (window == null) {
                    return;
                }
                close();

                E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                        window, IModelConstants.PART_ID_COMMENTS, null,
                        IModelConstants.PART_STACK_ID_RIGHT);
            }
        });
    }

    public IWorkbenchWindow getWorkbenchWindow() {
        return window;
    }

    public ITopic getTopic() {
        return topic;
    }

    public boolean isShowExtraActions() {
        return showExtraActions;
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

    @Override
    public void createComment(String objectId) {
        contentViewer.createNewComment();
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

}
