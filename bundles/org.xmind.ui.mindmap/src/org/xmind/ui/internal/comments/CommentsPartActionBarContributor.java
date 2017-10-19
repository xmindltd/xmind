package org.xmind.ui.internal.comments;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextViewer;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.e4models.CommentsPart;

public class CommentsPartActionBarContributor
        extends CommentsActionBarContributor {

    private CommentsPart part;

    public CommentsPartActionBarContributor(CommentsPart part,
            IGraphicalEditor targetEditor) {
        super(targetEditor);
        this.part = part;
        makeActions();
    }

    protected IAction getContextAction(String actionId) {
        return part == null ? null : part.getGlobalAction(actionId);
    }

    public void update(TextViewer textViewer) {
        part.updateTextActions(textViewer);
    }

}
