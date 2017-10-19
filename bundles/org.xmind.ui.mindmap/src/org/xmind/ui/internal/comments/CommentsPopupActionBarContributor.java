package org.xmind.ui.internal.comments;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;

public class CommentsPopupActionBarContributor
        extends CommentsActionBarContributor {

    private class TextAction extends Action {

        private int op;

        private TextViewer textViewer;

        public TextAction(int op) {
            this.op = op;
        }

        public void run() {
            if (textViewer != null && textViewer.canDoOperation(op)) {
                textViewer.doOperation(op);
                Composite contentComposite = (Composite) commentsPopup
                        .getContentComposite();
                contentComposite.pack();
            }
        }

        public void update(TextViewer textViewer) {
            this.textViewer = textViewer;
            setEnabled(textViewer != null && textViewer.canDoOperation(op));
        }
    }

    private class GotoCommentsPartAction extends CommentAction {

        public GotoCommentsPartAction(IGraphicalEditor editor) {
            super(editor);
            setId("org.xmind.ui.action.gotoCommentsView"); //$NON-NLS-1$
            setText(MindMapMessages.EditInCommentsView_text);
            setToolTipText(MindMapMessages.EditInCommentsView_tooltip);
            setImageDescriptor(
                    MindMapUI.getImages().get("comments-view-edit.png", true)); //$NON-NLS-1$
        }

        public void run() {
            control = commentsPopup.getContentComposite();
            super.run();

            Display.getCurrent().timerExec(50, new Runnable() {
                public void run() {

                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            commentsPopup.gotoCommentsView();
                        }
                    });
                }
            });
        }
    }

    private CommentsPopup commentsPopup;

    private CommentAction showPreTopicCommentsAction;

    private CommentAction showNextTopicCommentsAction;

    private CommentAction gotoCommentsViewAction;

    private Map<String, TextAction> textActions = new HashMap<String, TextAction>(
            10);

    private Map<String, IAction> actionHandlers = new HashMap<String, IAction>(
            10);

    private Collection<String> textCommandIds = new HashSet<String>(10);

    public CommentsPopupActionBarContributor(CommentsPopup commentsPopup,
            IGraphicalEditor targetEditor) {
        super(targetEditor);
        this.commentsPopup = commentsPopup;
        makeActions();
    }

    protected void makeActions() {
        if (commentsPopup.isShowExtraActions()) {
            showPreTopicCommentsAction = new ShowPreTopicCommentsAction(
                    targetEditor, commentsPopup);
            addAction(showPreTopicCommentsAction);

            showNextTopicCommentsAction = new ShowNextTopicCommentsAction(
                    targetEditor, commentsPopup);
            addAction(showNextTopicCommentsAction);

            gotoCommentsViewAction = new GotoCommentsPartAction(targetEditor);
            addAction(gotoCommentsViewAction);
        }

        addWorkbenchAction(ActionFactory.UNDO, ITextOperationTarget.UNDO);
        addWorkbenchAction(ActionFactory.REDO, ITextOperationTarget.REDO);
        addWorkbenchAction(ActionFactory.CUT, ITextOperationTarget.CUT);
        addWorkbenchAction(ActionFactory.COPY, ITextOperationTarget.COPY);
        addWorkbenchAction(ActionFactory.PASTE, ITextOperationTarget.PASTE);
        addWorkbenchAction(ActionFactory.SELECT_ALL,
                ITextOperationTarget.SELECT_ALL);
    }

    public void fillToolBar(IToolBarManager toolbar) {
        if (commentsPopup.isShowExtraActions()) {
            toolbar.add(showPreTopicCommentsAction);
            toolbar.add(showNextTopicCommentsAction);
            toolbar.add(gotoCommentsViewAction);
        }
    }

    private void addWorkbenchAction(ActionFactory factory, int textOp) {
        IWorkbenchAction action = factory
                .create(commentsPopup.getWorkbenchWindow());
        TextAction textAction = new TextAction(textOp);
        textAction.setId(action.getId());
        textAction.setActionDefinitionId(action.getActionDefinitionId());
        textAction.setText(action.getText());
        textAction.setToolTipText(action.getToolTipText());
        textAction.setDescription(action.getDescription());
        textAction.setImageDescriptor(action.getImageDescriptor());
        textAction.setDisabledImageDescriptor(
                action.getDisabledImageDescriptor());
        textAction.setHoverImageDescriptor(action.getHoverImageDescriptor());
        action.dispose();
        actionHandlers.put(action.getActionDefinitionId(), textAction);
        textActions.put(textAction.getId(), textAction);
    }

    public IAction getActionHandler(String commandId) {
        return actionHandlers.get(commandId);
    }

    public void update(TextViewer textViewer) {
        for (TextAction action : textActions.values()) {
            action.update(textViewer);
        }
    }

    public IAction getTextAction(String actionId) {
        return textActions.get(actionId);
    }

    public Collection<String> getTextCommandIds() {
        return textCommandIds;
    }

    protected IAction getContextAction(String actionId) {
        return getTextAction(actionId);
    }

}