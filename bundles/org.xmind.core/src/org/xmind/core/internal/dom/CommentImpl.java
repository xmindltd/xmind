package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_AUTHOR;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_OBJECT_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TIME;
import static org.xmind.core.internal.dom.DOMConstants.TAG_CONTENT;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IIdentifiable;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.internal.Comment;
import org.xmind.core.util.DOMUtils;

public class CommentImpl extends Comment {

    private Element implementation;

    private IWorkbook ownedWorkbook;

    public CommentImpl(Element implementation, IWorkbook ownedWorkbook) {
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedWorkbook = ownedWorkbook;
    }

    public void initAttributes() {
        String osUser = System.getProperty("user.name"); //$NON-NLS-1$
        setAuthor(osUser);
        String timeMillis = Long.toString(System.currentTimeMillis());
        setTime(timeMillis);
    }

    public Element getImplementation() {
        return implementation;
    }

    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    public String getAuthor() {
        return DOMUtils.getAttribute(implementation, ATTR_AUTHOR);
    }

    public String getTime() {
        return DOMUtils.getAttribute(implementation, ATTR_TIME);
    }

    public String getContent() {
        return DOMUtils.getTextContentByTag(implementation, TAG_CONTENT);
    }

    public String getObjectId() {
        return DOMUtils.getAttribute(implementation, ATTR_OBJECT_ID);
    }

    public IIdentifiable getTarget() {
        return (IIdentifiable) getOwnedWorkbook().getElementById(getObjectId());
    }

    public void setContent(String content) {
        String oldValue = getContent();
        DOMUtils.setText(implementation, TAG_CONTENT, content);
        String newValue = getContent();
        fireTargetValueChange(getTarget(), oldValue, newValue);
    }

    private void fireTargetValueChange(Object target, String oldValue,
            String newValue) {
        if (target instanceof ITopic) {
            TopicImpl topic = (TopicImpl) target;
            topic.getCoreEventSupport().dispatchValueChange(topic,
                    Core.TopicComments, oldValue, newValue);
        } else if (target instanceof ISheet) {
            SheetImpl sheet = (SheetImpl) target;
            sheet.getCoreEventSupport().dispatchValueChange(sheet,
                    Core.TopicComments, oldValue, newValue);
        }
    }

    public void setAuthor(String author) {
        DOMUtils.setAttribute(implementation, ATTR_AUTHOR, author);
    }

    public void setTime(String time) {
        DOMUtils.setAttribute(implementation, ATTR_TIME, time);
    }

    public void setTarget(IIdentifiable target) {
        setObjectId(target.getId());
    }

    private void setObjectId(String objectId) {
        DOMUtils.setAttribute(implementation, ATTR_OBJECT_ID, objectId);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == Node.class || adapter == Element.class) {
            return getImplementation();
        }
        return super.getAdapter(adapter);
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        IIdentifiable target = getTarget();
        if (target instanceof IWorkbookComponent) {
            if (!((IWorkbookComponent) target).isOrphan()) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "COMMENT#" + getId() + "(" + getContent() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
