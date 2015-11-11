package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_COMMENT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_COMMENTS;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IIdentifiable;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.comment.IComment;
import org.xmind.core.internal.CommentManager;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.util.DOMUtils;

public class CommentManagerImpl extends CommentManager
        implements INodeAdaptableFactory {

    private Document implementation;

    private WorkbookImpl ownedWorkbook;

    private NodeAdaptableRegistry adaptableRegistry;

    public CommentManagerImpl(Document implementation) {
        this.implementation = implementation;
        this.adaptableRegistry = new NodeAdaptableRegistry(implementation,
                this);
        init();
    }

    private void init() {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_COMMENTS);
        NS.setNS(NS.Comments, m);
        InternalDOMUtils.addVersion(implementation);
    }

    public IAdaptable createAdaptable(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            String tagName = e.getNodeName();
            if (TAG_COMMENT.equals(tagName)) {
                return new CommentImpl(e, ownedWorkbook);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return !getCommentsElement().hasChildNodes();
    }

    public Element getCommentsElement() {
        return implementation.getDocumentElement();
    }

    protected void setOwnedWorkbook(WorkbookImpl ownedWorkbook) {
        this.ownedWorkbook = ownedWorkbook;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return ownedWorkbook == null;
    }

    public Object getAdapter(Class adapter) {
//        if (adapter == ICoreEventSource.class)
//            return this;
        if (adapter == Node.class || adapter == Document.class)
            return implementation;
        if (adapter == ElementRegistry.class)
            return getAdaptableRegistry();
        return super.getAdapter(adapter);
    }

    public NodeAdaptableRegistry getAdaptableRegistry() {
        return adaptableRegistry;
    }

    public IComment createComment() {
        CommentImpl comment = new CommentImpl(
                implementation.createElement(TAG_COMMENT), ownedWorkbook);
        comment.initAttributes();
        return comment;
    }

    public void addComment(IComment comment, IIdentifiable target) {
        comment.setTarget(target);
        Element t = comment.getImplementation();
        Element c = DOMUtils.ensureChildElement(implementation, TAG_COMMENTS);
        c.appendChild(t);
        getAdaptableRegistry().registerByNode(comment,
                comment.getImplementation());
        fireTargetValueChange(comment.getTarget(), null, ""); //$NON-NLS-1$
    }

    public void addComment(IComment comment, int index) {
        Element t = comment.getImplementation();
        if (t != null && t.getOwnerDocument() == implementation) {
            Element c = DOMUtils.ensureChildElement(implementation,
                    TAG_COMMENTS);
            Element[] es = DOMUtils.getChildElementsByTag(c, TAG_COMMENT);
            if (index >= 0 && index < es.length) {
                c.insertBefore(t, es[index]);
            } else {
                c.appendChild(t);
            }
            getAdaptableRegistry().registerByNode(comment,
                    comment.getImplementation());
            fireTargetValueChange(comment.getTarget(), null, ""); //$NON-NLS-1$
        }
    }

    public void removeComment(IComment comment) {
        Element t = comment.getImplementation();
        Element c = DOMUtils.ensureChildElement(implementation, TAG_COMMENTS);
        c.removeChild(t);
        getAdaptableRegistry().unregisterByNode(comment,
                comment.getImplementation());
        fireTargetValueChange(comment.getTarget(), comment.getContent(), ""); //$NON-NLS-1$
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

    /**
     * 
     * @param source
     *            instanceOf {@link ITopic} or {@link ISheet}
     * @return
     */
    public List<IComment> getComments(IIdentifiable source) {
        List<IComment> comments = new ArrayList<IComment>();
        List<IComment> allComments = getAllComments();
        for (IComment comment : allComments) {
            if (comment.getObjectId().equals(source.getId())) {
                comments.add(comment);
            }
        }
        return comments;
    }

    public List<IComment> getAllComments() {
        return DOMUtils.getChildList(getCommentsElement(), TAG_COMMENT,
                getAdaptableRegistry());
    }

}
