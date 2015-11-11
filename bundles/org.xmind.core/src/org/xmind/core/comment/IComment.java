package org.xmind.core.comment;

import org.w3c.dom.Element;
import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.IWorkbookComponent;

public interface IComment extends IAdaptable, IWorkbookComponent, IIdentifiable {

    Element getImplementation();

    void setContent(String content);

    void setTarget(IIdentifiable sheet);

    String getAuthor();

    String getTime();

    String getContent();

    String getObjectId();

    IIdentifiable getTarget();

}
