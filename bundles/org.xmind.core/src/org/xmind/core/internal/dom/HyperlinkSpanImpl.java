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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_HREF;

import java.util.List;

import org.w3c.dom.Element;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.ISpan;
import org.xmind.core.util.DOMUtils;

/**
 * @author MANGOSOFT
 * 
 */
public class HyperlinkSpanImpl extends SpanImplBase implements IHyperlinkSpan {

    private Element implementation;

    public HyperlinkSpanImpl(Element implementation, HtmlNotesContentImpl owner) {
        super(implementation, owner);
        this.implementation = implementation;
    }

    public String getHref() {
        String href = DOMUtils.getAttribute(implementation, ATTR_HREF);
        if (href != null)
            return href;
        return DOMUtils.getAttribute(implementation, "href"); //$NON-NLS-1$
    }

    public void setHref(String source) {
        DOMUtils.setAttribute(implementation, ATTR_HREF, source);
        getOwner().updateModifiedTime();
    }

    public List<ISpan> getSpans() {
        return DOMUtils.getChildren(implementation, getOwner());
    }

    public void addSpan(ISpan span) {
        SpanImplBase base = (SpanImplBase) span;
        implementation.appendChild(base.getImplementation());
        base.addNotify(getOwner().getRealizedWorkbook());
        getOwner().updateModifiedTime();
    }

    public void removeSpan(ISpan span) {
        SpanImplBase base = (SpanImplBase) span;
        base.removeNotify(getOwner().getRealizedWorkbook());
        implementation.removeChild(base.getImplementation());
        getOwner().updateModifiedTime();
    }

    @Override
    protected void addNotify(WorkbookImpl workbook) {
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        for (ISpan span : getSpans()) {
            ((SpanImplBase) span).addNotify(workbook);
        }
    }

    @Override
    protected void removeNotify(WorkbookImpl workbook) {
        for (ISpan span : getSpans())
            ((SpanImplBase) span).removeNotify(workbook);
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
    }

}
