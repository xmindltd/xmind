package org.xmind.ui.internal.comments;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import org.xmind.core.IComment;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;

public abstract class CommentsActionBarContributor
        implements ICommentsActionBarContributor {

    protected IGraphicalEditor targetEditor;

    private Map<String, CommentAction> actions = new HashMap<String, CommentAction>();

    private ISpellingActivation spellingActivation;

    public CommentsActionBarContributor(IGraphicalEditor targetEditor) {
        this.targetEditor = targetEditor;
    }

    protected void makeActions() {
    }

    protected void addAction(CommentAction action) {
        if (action != null && action.getId() != null)
            actions.put(action.getId(), action);
    }

    public void setSpellingActivation(ISpellingActivation spellingActivation) {
        this.spellingActivation = spellingActivation;
    }

    public void fillToolBar(IToolBarManager toolbar) {
    }

    public void fillContextMenu(IMenuManager menu) {
        menu.add(getContextAction(ActionFactory.UNDO.getId()));
        menu.add(getContextAction(ActionFactory.REDO.getId()));
        menu.add(new Separator());
        menu.add(getContextAction(ActionFactory.CUT.getId()));
        menu.add(getContextAction(ActionFactory.COPY.getId()));
        menu.add(getContextAction(ActionFactory.PASTE.getId()));
        menu.add(new Separator());
        menu.add(getContextAction(ActionFactory.SELECT_ALL.getId()));

        if (spellingActivation != null) {
            IMenuContributor contributor = (IMenuContributor) spellingActivation
                    .getAdapter(IMenuContributor.class);
            if (contributor != null) {
                menu.add(new Separator());
                contributor.fillMenu(menu);
            }
        }
    }

    protected abstract IAction getContextAction(String actionId);

    public CommentAction getAction(String id) {
        return actions.get(id);
    }

    public void selectionChanged(Object selection) {
        for (CommentAction action : actions.values()) {
            action.selectionChanged(selection);
        }
    }

    public void selectedCommentChanged(IComment comment) {
        for (CommentAction action : actions.values()) {
            action.selectedCommentChanged(comment);
        }
    }

    public void dispose() {
        actions.clear();
    }

    public void setTargetEditor(IGraphicalEditor targetEditor) {
        if (this.targetEditor != targetEditor) {
            this.targetEditor = targetEditor;
            for (Entry<String, CommentAction> entry : actions.entrySet()) {
                entry.getValue().setTargetEditor(targetEditor);
            }
        }
    }

}