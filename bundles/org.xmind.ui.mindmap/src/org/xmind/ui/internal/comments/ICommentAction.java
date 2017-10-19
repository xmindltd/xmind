package org.xmind.ui.internal.comments;

import org.xmind.core.IComment;

public interface ICommentAction {

    void selectionChanged(Object selection);

    void selectedCommentChanged(IComment comment);

}
