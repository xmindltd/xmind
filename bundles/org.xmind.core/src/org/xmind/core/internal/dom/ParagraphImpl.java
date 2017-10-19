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

import java.util.List;

import org.w3c.dom.Element;
import org.xmind.core.IParagraph;
import org.xmind.core.ISpan;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookComponent;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.DOMUtils;

public class ParagraphImpl extends AbstractWorkbookComponent
        implements IParagraph {

    private Element implementation;

    private HtmlNotesContentImpl owner;

    public ParagraphImpl(Element implementation, HtmlNotesContentImpl owner) {
        this.implementation = implementation;
        this.owner = owner;
    }

    public String getStyleType() {
        return IStyle.PARAGRAPH;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ParagraphImpl))
            return false;
        ParagraphImpl that = (ParagraphImpl) obj;
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

    public HtmlNotesContentImpl getOwner() {
        return owner;
    }

    public void addSpan(ISpan span) {
        SpanImplBase s = (SpanImplBase) span;
        implementation.appendChild(s.getImplementation());
        s.addNotify(owner.getRealizedWorkbook());
        getOwner().updateModifiedTime();
    }

    public List<ISpan> getSpans() {
        return DOMUtils.getChildren(implementation, owner);
    }

    public void removeSpan(ISpan span) {
        SpanImplBase s = (SpanImplBase) span;
        s.removeNotify(owner.getRealizedWorkbook());
        implementation.removeChild(s.getImplementation());
        getOwner().updateModifiedTime();
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public String getStyleId() {
        return DOMUtils.getAttribute(implementation,
                DOMConstants.ATTR_STYLE_ID);
    }

    public void setStyleId(String styleId) {
        WorkbookImpl workbook = owner.getRealizedWorkbook();
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
        DOMUtils.setAttribute(implementation, DOMConstants.ATTR_STYLE_ID,
                styleId);
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        getOwner().updateModifiedTime();
    }

    public IWorkbook getOwnedWorkbook() {
        return owner.getOwnedWorkbook();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    protected void addNotify(WorkbookImpl workbook) {
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        for (ISpan span : getSpans()) {
            ((SpanImplBase) span).addNotify(workbook);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        for (ISpan span : getSpans()) {
            ((SpanImplBase) span).removeNotify(workbook);
        }
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
    }

}
