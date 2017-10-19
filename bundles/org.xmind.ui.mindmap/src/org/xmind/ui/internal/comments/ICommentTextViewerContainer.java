package org.xmind.ui.internal.comments;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.IComment;

public interface ICommentTextViewerContainer {

    void moveToPreviousTextViewer(CommentTextViewer implementation);

    void moveToNextTextViewer(CommentTextViewer implementation);

    Composite getContentComposite();

    ScrolledComposite getScrolledComposite();

    void setLatestCreatedComment(IComment latestCreatedComment);

    IComment getLatestCreatedComment();

    void setSelectedComment(IComment selectedComment);

    IComment getSelectedComment();

    void createComment(String objectId);

    void cancelCreateComment();

    void setEditingComment(IComment comment);

    IComment getEditingComment();

    void setModified(boolean modified);

    boolean isModified();

}
