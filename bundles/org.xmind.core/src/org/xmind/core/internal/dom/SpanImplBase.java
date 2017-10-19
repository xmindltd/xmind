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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.ISpan;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.AbstractWorkbookComponent;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.DOMUtils;

public abstract class SpanImplBase extends AbstractWorkbookComponent
        implements ISpan {

    private Node implementation;

    private HtmlNotesContentImpl owner;

    public SpanImplBase(Node implementation, HtmlNotesContentImpl owner) {
        this.implementation = implementation;
        this.owner = owner;
    }

    public String getStyleType() {
        return IStyle.TEXT;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SpanImplBase))
            return false;
        SpanImplBase that = (SpanImplBase) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return implementation.toString();
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class)
                && implementation instanceof Element)
            return adapter.cast(implementation);
        if (adapter.isAssignableFrom(Node.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public Node getImplementation() {
        return implementation;
    }

    protected void setImplementation(Node implementation) {
//        owner.unregister(this.implementation);
        owner.getAdaptableRegistry().unregister(this, this.implementation);
        this.implementation = implementation;
//        owner.register(this.implementation, this);
        owner.getAdaptableRegistry().register(this, this.implementation);
    }

    public HtmlNotesContentImpl getOwner() {
        return owner;
    }

    public String getStyleId() {
        if (implementation instanceof Element) {
            return DOMUtils.getAttribute((Element) implementation,
                    ATTR_STYLE_ID);
        }
        return null;
    }

    public void setStyleId(String styleId) {
        if (implementation instanceof Element) {
            WorkbookImpl workbook = owner.getRealizedWorkbook();
            WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
            DOMUtils.setAttribute((Element) implementation, ATTR_STYLE_ID,
                    styleId);
            WorkbookUtilsImpl.increaseStyleRef(workbook, this);
            getOwner().updateModifiedTime();
        }
    }

    public IWorkbook getOwnedWorkbook() {
        return owner.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    protected void addNotify(WorkbookImpl workbook) {
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
    }

    protected void removeNotify(WorkbookImpl workbook) {
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
    }

}
