package org.xmind.ui.internal.comments;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.TextViewer;
import org.xmind.core.IComment;
import org.xmind.ui.texteditor.ISpellingActivation;

public interface ICommentsActionBarContributor {

    void setSpellingActivation(ISpellingActivation spellingActivation);

    void fillToolBar(IToolBarManager toolbar);

    void fillContextMenu(IMenuManager menu);

    CommentAction getAction(String id);

    void selectionChanged(Object selection);

    void selectedCommentChanged(IComment comment);

    void dispose();

    void update(TextViewer textViewer);

}
