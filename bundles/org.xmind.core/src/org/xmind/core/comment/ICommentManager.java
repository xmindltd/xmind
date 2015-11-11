package org.xmind.core.comment;

import java.util.List;

import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.IWorkbook;

public interface ICommentManager extends IAdaptable {

    IWorkbook getOwnedWorkbook();

    boolean isEmpty();

    IComment createComment();

    void addComment(IComment comment, IIdentifiable target);

    void addComment(IComment comment, int index);

    void removeComment(IComment comment);

    List<IComment> getComments(IIdentifiable source);

    List<IComment> getAllComments();

}
