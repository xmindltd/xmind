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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_SRC;

import org.w3c.dom.Element;
import org.xmind.core.IImageSpan;
import org.xmind.core.util.DOMUtils;

public class ImageSpanImpl extends SpanImplBase implements IImageSpan {

    public ImageSpanImpl(Element implementation, HtmlNotesContentImpl owner) {
        super(implementation, owner);
    }

    public String getSource() {
        return DOMUtils.getAttribute((Element) getImplementation(), ATTR_SRC);
    }

    public void setSource(String source) {
        WorkbookImpl workbook = getOwner().getRealizedWorkbook();
        InternalHyperlinkUtils.deactivateHyperlink(workbook, getSource(), this);
        DOMUtils.setAttribute((Element) getImplementation(), ATTR_SRC, source);
        InternalHyperlinkUtils.activateHyperlink(workbook, getSource(), this);
        getOwner().updateModifiedTime();
    }

    protected void addNotify(WorkbookImpl workbook) {
        super.addNotify(workbook);
        InternalHyperlinkUtils.activateHyperlink(workbook, getSource(), this);
    }

    protected void removeNotify(WorkbookImpl workbook) {
        InternalHyperlinkUtils.deactivateHyperlink(workbook, getSource(), this);
        super.removeNotify(workbook);
    }
}