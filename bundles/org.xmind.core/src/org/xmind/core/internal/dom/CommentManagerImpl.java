/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_AUTHOR;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_OBJECT_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TIME;
import static org.xmind.core.internal.dom.DOMConstants.TAG_COMMENT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_COMMENTS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IComment;
import org.xmind.core.ICommentManager;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.util.DOMUtils;

/**
 * @author Frank Shaka
 */
public class CommentManagerImpl
        implements ICommentManager, INodeAdaptableFactory {

    private final WorkbookImpl ownedWorkbook;

    private final Document implementation;

    private final NodeAdaptableRegistry registry;

    private final Set<CommentImpl> pendingComments;

    /**
     * 
     */
    public CommentManagerImpl(WorkbookImpl ownerWorkbook,
            Document implementation) {
        this.ownedWorkbook = ownerWorkbook;
        this.implementation = implementation;
        this.registry = new NodeAdaptableRegistry(implementation, this);
        this.pendingComments = new HashSet<CommentImpl>();
        init();
    }

    private void init() {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_COMMENTS);
        NS.setNS(NS.Comments, m);
        InternalDOMUtils.addVersion(implementation);
    }

    private Element getCommentsElement() {
        return implementation.getDocumentElement();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IAdaptable#getAdapter(java.lang.Class)
     */
    public <T> T getAdapter(Class<T> adapter) {
        if (IWorkbook.class.equals(adapter))
            return adapter.cast(getOwnedWorkbook());
        if (Node.class.equals(adapter) || Document.class.equals(adapter))
            return adapter.cast(implementation);
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#getOwnedWorkbook()
     */
    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return false;
    }

    protected void objectAddNotify(String objectId, Object obj) {
        Element p = getCommentsElement();
        Iterator<CommentImpl> it = pendingComments.iterator();
        while (it.hasNext()) {
            CommentImpl c = it.next();
            if (objectId.equals(c.getObjectId())) {
                it.remove();
                Element ele = c.getImplementation();
                if (ele.getParentNode() != p) {
                    p.appendChild(ele);
                    if (obj instanceof ICoreEventSource) {
                        ownedWorkbook.getCoreEventSupport()
                                .dispatchTargetChange((ICoreEventSource) obj,
                                        Core.CommentAdd, c);
                    }
                }
            }
        }
    }

    protected void objectRemoveNotify(String objectId, Object obj) {
        Element p = getCommentsElement();
        int i = 0;
        while (i < p.getChildNodes().getLength()) {
            Node n = p.getChildNodes().item(i);
            if (n instanceof Element
                    && TAG_COMMENT.equals(((Element) n).getTagName())) {
                Element ele = (Element) n;
                if (objectId
                        .equals(DOMUtils.getAttribute(ele, ATTR_OBJECT_ID))) {
                    p.removeChild(ele);
                    IAdaptable a = registry.getAdaptable(ele);
                    if (a instanceof CommentImpl) {
                        CommentImpl c = (CommentImpl) a;
                        pendingComments.add(c);
                        if (obj instanceof ICoreEventSource) {
                            ownedWorkbook.getCoreEventSupport()
                                    .dispatchTargetChange(
                                            (ICoreEventSource) obj,
                                            Core.CommentRemove, c);
                        }
                    }
                    continue;
                }
            }
            i++;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#createComment(java.lang.String, long,
     * java.lang.String)
     */
    public IComment createComment(String author, long time, String objectId) {
        if (author == null)
            throw new IllegalArgumentException();
        if (time <= 0)
            throw new IllegalArgumentException();
        if (objectId == null)
            throw new IllegalArgumentException();

        Element ele = implementation.createElement(TAG_COMMENT);
        DOMUtils.setAttribute(ele, ATTR_AUTHOR, author);
        DOMUtils.setAttribute(ele, ATTR_TIME, Long.toString(time));
        DOMUtils.setAttribute(ele, ATTR_OBJECT_ID, objectId);

        CommentImpl c = new CommentImpl(ownedWorkbook, this, ele);
        registry.register(c, ele);
        return c;
    }

    private static boolean isObjectOrphan(Object obj) {
        return !(obj instanceof IWorkbookComponent)
                || ((IWorkbookComponent) obj).isOrphan();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#addCommand(org.xmind.core.IComment)
     */
    public void addComment(IComment comment) {
        if (!(comment instanceof CommentImpl)
                || comment.getOwnedWorkbook() != getOwnedWorkbook())
            throw new IllegalArgumentException();

        CommentImpl c = (CommentImpl) comment;
        Element ele = c.getImplementation();
        Element p = getCommentsElement();
        if (ele.getParentNode() == p)
            return;

        Object obj = ownedWorkbook.getElementById(c.getObjectId());
        if (obj == null || isObjectOrphan(obj)) {
            /// associated object not exist,
            /// add comment to pending set
            pendingComments.add(c);
            return;
        }

        p.appendChild(ele);
        if (obj instanceof ICoreEventSource) {
            ownedWorkbook.getCoreEventSupport().dispatchTargetChange(
                    (ICoreEventSource) obj, Core.CommentAdd, c);
        }
        updateModificationInfo();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.ICommentManager#removeComment(org.xmind.core.IComment)
     */
    public void removeComment(IComment comment) {
        if (!(comment instanceof CommentImpl)
                || comment.getOwnedWorkbook() != getOwnedWorkbook())
            throw new IllegalArgumentException();

        CommentImpl c = (CommentImpl) comment;
        Element ele = c.getImplementation();
        Element p = getCommentsElement();

        pendingComments.remove(c);

        if (ele.getParentNode() != p)
            return;

        p.removeChild(ele);

        Object obj = ownedWorkbook.getElementById(c.getObjectId());
        if (obj instanceof ICoreEventSource) {
            ownedWorkbook.getCoreEventSupport().dispatchTargetChange(
                    (ICoreEventSource) obj, Core.CommentRemove, c);
        }
        updateModificationInfo();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#getComments(java.lang.String)
     */
    public Set<IComment> getComments(String objectId) {
        if (objectId == null)
            throw new IllegalArgumentException();

        Object o = getOwnedWorkbook().getElementById(objectId);
        if (o == null || isObjectOrphan(o)) {
            return Collections.emptySet();
        }

        Set<IComment> set = new HashSet<IComment>();
        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getCommentsElement(), TAG_COMMENT);
        while (it.hasNext()) {
            Element ele = it.next();
            if (objectId.equals(DOMUtils.getAttribute(ele, ATTR_OBJECT_ID))) {
                IAdaptable a = registry.getAdaptable(ele);
                if (a instanceof CommentImpl) {
                    set.add((CommentImpl) a);
                }
            }
        }
        return set;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#hasComments(java.lang.String)
     */
    public boolean hasComments(String objectId) {
        if (objectId == null)
            throw new IllegalArgumentException();

        Object o = getOwnedWorkbook().getElementById(objectId);
        if (o == null || isObjectOrphan(o)) {
            return false;
        }

        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getCommentsElement(), TAG_COMMENT);
        while (it.hasNext()) {
            Element ele = it.next();
            if (objectId.equals(DOMUtils.getAttribute(ele, ATTR_OBJECT_ID))) {
                IAdaptable a = registry.getAdaptable(ele);
                if (a instanceof CommentImpl) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#getAllComments()
     */
    public Set<IComment> getAllComments() {
        Set<IComment> set = new HashSet<IComment>();
        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getCommentsElement(), TAG_COMMENT);
        while (it.hasNext()) {
            Element ele = it.next();
            IAdaptable a = registry.getAdaptable(ele);
            if (a instanceof CommentImpl) {
                CommentImpl c = (CommentImpl) a;
                Object o = getOwnedWorkbook().getElementById(c.getObjectId());
                if (o != null && !isObjectOrphan(o)) {
                    set.add(c);
                }
            }
        }
        return set;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.ICommentManager#isEmpty()
     */
    public boolean isEmpty() {
        Iterator<Element> it = DOMUtils
                .childElementIterByTag(getCommentsElement(), TAG_COMMENT);
        while (it.hasNext()) {
            Element ele = it.next();
            IAdaptable a = registry.getAdaptable(ele);
            if (a instanceof CommentImpl) {
                CommentImpl c = (CommentImpl) a;
                Object o = getOwnedWorkbook().getElementById(c.getObjectId());
                if (o != null && !isObjectOrphan(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.internal.dom.INodeAdaptableFactory#createAdaptable(org.w3c
     * .dom.Node)
     */
    public IAdaptable createAdaptable(Node node) {
        if (node instanceof Element) {
            Element ele = (Element) node;
            String tag = ele.getTagName();
            if (TAG_COMMENT.equals(tag)) {
                return new CommentImpl(ownedWorkbook, this, ele);
            }
        }
        return null;
    }

    protected void updateModificationInfo() {
        if (ownedWorkbook != null) {
            ownedWorkbook.updateModificationInfo();
        }
    }

}
