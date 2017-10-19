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
import static org.xmind.core.internal.dom.DOMConstants.TAG_CONTENT;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.util.DOMUtils;

/**
 * @author Frank Shaka
 */
public class CommentImpl implements IComment, ICoreEventSource {

    private final WorkbookImpl ownerWorkbook;

    private final Element implementation;

    private CommentManagerImpl ownedCommentManager;

    /**
     * 
     */
    public CommentImpl(WorkbookImpl ownerWorkbook,
            CommentManagerImpl ownedCommentManager, Element implementation) {
        this.ownerWorkbook = ownerWorkbook;
        this.ownedCommentManager = ownedCommentManager;
        this.implementation = implementation;
    }

    /**
     * @return the implementation
     */
    public Element getImplementation() {
        return implementation;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IAdaptable#getAdapter(java.lang.Class)
     */
    public <T> T getAdapter(Class<T> adapter) {
        if (IWorkbook.class.equals(adapter))
            return adapter.cast(getOwnedWorkbook());
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        if (ICoreEventSupport.class.equals(adapter))
            return adapter.cast(getCoreEventSupport());
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#getOwnedWorkbook()
     */
    public IWorkbook getOwnedWorkbook() {
        return ownerWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IComment that) {
        return (int) (that.getTime() - this.getTime());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IComment#getObjectId()
     */
    public String getObjectId() {
        return DOMUtils.getAttribute(implementation, ATTR_OBJECT_ID);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IComment#getAuthor()
     */
    public String getAuthor() {
        return DOMUtils.getAttribute(implementation, ATTR_AUTHOR);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IComment#getTime()
     */
    public long getTime() {
        return NumberUtils.safeParseLong(
                DOMUtils.getAttribute(implementation, DOMConstants.ATTR_TIME),
                0);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IComment#getContent()
     */
    public String getContent() {
        return DOMUtils.getTextContentByTag(implementation, TAG_CONTENT);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IComment#setContent(java.lang.String)
     */
    public void setContent(String content) {
        String oldContent = getContent();
        if (content == oldContent
                || (content != null && content.equals(oldContent)))
            return;

        DOMUtils.setText(implementation, TAG_CONTENT, content);
        getCoreEventSupport().dispatchValueChange(this, Core.CommentContent,
                oldContent, content);
        updateModificationInfo();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.event.ICoreEventSource#registerCoreEventListener(java.lang
     * .String, org.xmind.core.event.ICoreEventListener)
     */
    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.event.ICoreEventSource#getCoreEventSupport()
     */
    public ICoreEventSupport getCoreEventSupport() {
        return ownerWorkbook.getCoreEventSupport();
    }

    protected void updateModificationInfo() {
        if (ownedCommentManager != null) {
            ownedCommentManager.updateModificationInfo();
        }
    }

}
