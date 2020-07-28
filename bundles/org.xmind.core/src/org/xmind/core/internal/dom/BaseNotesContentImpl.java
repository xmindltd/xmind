/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_NOTES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookComponent;
import org.xmind.core.util.DOMUtils;

public abstract class BaseNotesContentImpl extends AbstractWorkbookComponent
        implements INotesContent {

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    public BaseNotesContentImpl(Element implementation,
            WorkbookImpl ownedWorkbook) {
        this.implementation = implementation;
        this.ownedWorkbook = ownedWorkbook;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof BaseNotesContentImpl))
            return false;
        BaseNotesContentImpl that = (BaseNotesContentImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return implementation.toString();
    }

    public Element getImplementation() {
        return implementation;
    }

    protected WorkbookImpl getWorkbook() {
        TopicImpl topic = getTopic();
        if (topic != null)
            return (WorkbookImpl) topic.getPath().getWorkbook();
        return null;
    }

    protected TopicImpl getTopic() {
        INotes notes = getParent();
        if (notes != null) {
            return (TopicImpl) notes.getParent();
        }
        return null;
    }

    public String getFormat() {
        return implementation.getTagName();
    }

    public INotes getParent() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_NOTES)) {
            Node t = p.getParentNode();
            if (DOMUtils.isElementByTag(t, TAG_TOPIC)) {
                ITopic topic = (ITopic) ownedWorkbook.getAdaptableRegistry()
                        .getAdaptable(t);
                if (topic != null)
                    return topic.getNotes();
            }
        }
        return null;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    protected abstract void addNotify(WorkbookImpl workbook);

    protected abstract void removeNotify(WorkbookImpl workbook);

    protected void updateModifiedTime() {
        TopicImpl topic = getTopic();
        if (topic != null) {
            topic.updateModificationInfo();
        }
    }

}
