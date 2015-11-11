package org.xmind.ui.internal.comments;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.comment.IComment;

public interface ICommentTextViewerContainer {

//    void moveToPreviousTextViewer(Control currControl);
//
//    void moveToNextTextViewer(Control currControl);

    void moveToPreviousTextViewer(CommentTextViewer implementation);

    void moveToNextTextViewer(CommentTextViewer implementation);

    Composite getContentComposite();

    ScrolledComposite getScrolledComposite();

    void setLatestCreatedComment(IComment latestCreatedComment);

    IComment getLatestCreatedComment();

    void setSelectedComment(IComment selectedComment);

    IComment getSelectedComment();

}
