package org.xmind.ui.internal.comments;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.actions.AddCommentAction;
import org.xmind.ui.internal.views.CommentsView;

public class CommentsViewActionBarContributor
        extends CommentsActionBarContributor {

    private CommentsView view;

    public CommentsViewActionBarContributor(CommentsView view,
            IGraphicalEditor targetEditor) {
        super(targetEditor);
        this.view = view;
        makeActions();
    }

    protected void makeActions() {
        addCommentAction = new AddCommentAction(targetEditor, null, view);
        addAction(addCommentAction);
    }

    protected IAction getContextAction(String actionId) {
        return view == null ? null : view.getGlobalAction(actionId);
    }

    public void update(TextViewer textViewer) {
        view.updateTextActions(textViewer);
    }

}