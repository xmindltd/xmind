package org.xmind.ui.internal.comments;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.IComment;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public abstract class CommentAction extends Action implements ICommentAction {

    private IGraphicalEditor targetEditor;

    protected Control control;

    public CommentAction(IGraphicalEditor targetEditor) {
        this.targetEditor = targetEditor;
    }

    @Override
    public void run() {
        if (control != null) {
            control.forceFocus();
        }
        super.run();
    }

    public void selectionChanged(Object selection) {
    }

    public void selectedCommentChanged(IComment comment) {
    }

    public void setTargetEditor(IGraphicalEditor targetEditor) {
        this.targetEditor = targetEditor;
    }

    protected IGraphicalEditor getTargetEditor() {
        return targetEditor;
    }

    protected ICommandStack getCommandStack() {
        return targetEditor == null ? null : targetEditor.getCommandStack();
    }

}
