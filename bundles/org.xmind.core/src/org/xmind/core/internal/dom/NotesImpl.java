/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.TAG_NOTES;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.INotesContent;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.Notes;
import org.xmind.core.util.DOMUtils;

/**
 * @author briansun
 */
public class NotesImpl extends Notes {

    private Element topicElement;

    private TopicImpl ownedTopic;

    /**
     * @param implementation
     */
    public NotesImpl(Element topicElement, TopicImpl ownedTopic) {
        super();
        this.topicElement = topicElement;
        this.ownedTopic = ownedTopic;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof NotesImpl))
            return false;
        NotesImpl that = (NotesImpl) obj;
        return this.topicElement == that.topicElement;
    }

    public int hashCode() {
        return topicElement.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(getNotesElement());
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getNotesElement());
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.core.INotes#getParent()
     */
    public ITopic getParent() {
        return ownedTopic;
    }

    private Element getNotesElement() {
        return DOMUtils.getFirstChildElementByTag(topicElement, TAG_NOTES);
    }

    public INotesContent getContent(String format) {
        Element n = getNotesElement();
        if (n != null) {
            Element c = DOMUtils.getFirstChildElementByTag(n, format);
            if (c != null) {
                return getNotesContent(c);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        Element n = getNotesElement();
        if (n != null) {
            return !n.hasChildNodes();
        }
        return true;
    }

    public void setContent(String format, INotesContent content) {
        if (format == null)
            return;

        Element notesEle = getNotesElement();
        Element oldContentEle = notesEle == null ? null
                : DOMUtils.getFirstChildElementByTag(notesEle, format);
        INotesContent oldContent = oldContentEle == null ? null
                : getNotesContent(oldContentEle);

        WorkbookImpl workbook = null;
        if (oldContent instanceof BaseNotesContentImpl) {
            if (workbook == null)
                workbook = getRealizedWorkbook();
            ((BaseNotesContentImpl) oldContent).removeNotify(workbook);
        }

        if (oldContentEle != null && notesEle != null) {
            notesEle.removeChild(oldContentEle);
        }

        if (content != null) {
            Element newContentEle = (Element) content.getAdapter(Element.class);
            if (newContentEle != null) {
                if (notesEle == null)
                    notesEle = DOMUtils.ensureChildElement(topicElement,
                            TAG_NOTES);
                notesEle.appendChild(newContentEle);
            }
        }

        if (notesEle != null && !notesEle.hasChildNodes()) {
            topicElement.removeChild(notesEle);
        }

        if (content instanceof BaseNotesContentImpl) {
            if (workbook == null)
                workbook = getRealizedWorkbook();
            ((BaseNotesContentImpl) content).addNotify(workbook);
        }

        fireTargetValueChange(format, oldContent, content);
        ownedTopic.updateModificationInfo();
    }

    private INotesContent getNotesContent(Element oldContentEle) {
        return (INotesContent) ((WorkbookImpl) getOwnedWorkbook())
                .getAdaptableRegistry().getAdaptable(oldContentEle);
    }

    protected WorkbookImpl getRealizedWorkbook() {
        ITopic parent = getParent();
        if (parent instanceof TopicImpl)
            return ((TopicImpl) parent).getRealizedWorkbook();
        return null;
    }

    protected void addNotify(WorkbookImpl workbook) {
        Element n = getNotesElement();
        if (n != null) {
            Iterator<Element> it = DOMUtils.childElementIter(n);
            while (it.hasNext()) {
                Element c = it.next();
                INotesContent content = getNotesContent(c);
                if (content instanceof BaseNotesContentImpl) {
                    ((BaseNotesContentImpl) content).addNotify(workbook);
                }
            }
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        Element n = getNotesElement();
        if (n != null) {
            Iterator<Element> it = DOMUtils.childElementIter(n);
            while (it.hasNext()) {
                Element c = it.next();
                INotesContent content = getNotesContent(c);
                if (content instanceof BaseNotesContentImpl) {
                    ((BaseNotesContentImpl) content).removeNotify(workbook);
                }
            }
        }
    }

    private void fireTargetValueChange(Object target, Object oldValue,
            Object newValue) {
        ownedTopic.getCoreEventSupport().dispatchTargetValueChange(ownedTopic,
                Core.TopicNotes, target, oldValue, newValue);
    }

    public ISheet getOwnedSheet() {
        return ownedTopic.getOwnedSheet();
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedTopic.getOwnedWorkbook();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return ownedTopic.isOrphan();
    }

}
