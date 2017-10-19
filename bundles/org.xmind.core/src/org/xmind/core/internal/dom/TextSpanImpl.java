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
import org.w3c.dom.Text;
import org.xmind.core.ITextSpan;
import org.xmind.core.util.DOMUtils;

public class TextSpanImpl extends SpanImplBase implements ITextSpan {

    private Text text;

    private Element ele;

    public TextSpanImpl(Node implementation, HtmlNotesContentImpl owner) {
        super(implementation, owner);
        if (implementation instanceof Text) {
            this.text = (Text) implementation;
            this.ele = null;
        } else if (implementation instanceof Element) {
            this.text = null;
            this.ele = (Element) implementation;
        }
    }

    public void setStyleId(String styleId) {
        if (styleId == null) {
            if (getImplementation() == text)
                return;

            if (text == null) {
                text = ((WorkbookImpl) getOwnedWorkbook()).getImplementation()
                        .createTextNode(getTextContent());
            }
            WorkbookUtilsImpl.decreaseStyleRef(
                    getOwner().getRealizedWorkbook(), this);
            Node oldImpl = getImplementation();
            Node p = oldImpl.getParentNode();
            if (p != null) {
                p.replaceChild(oldImpl, text);
            }
            setImplementation(text);
            getOwner().updateModifiedTime();
        } else {
            if (getImplementation() == ele) {
                super.setStyleId(styleId);
            } else {
                if (ele == null) {
                    ele = ((WorkbookImpl) getOwnedWorkbook())
                            .getImplementation().createElement(
                                    DOMConstants.TAG_SPAN);
                    ele.setTextContent(getTextContent());
                }
                Node oldImpl = getImplementation();
                Node p = oldImpl.getParentNode();
                if (p != null) {
                    p.replaceChild(ele, oldImpl);
                }
                setImplementation(ele);
                DOMUtils.setAttribute(ele, ATTR_STYLE_ID, styleId);
                WorkbookUtilsImpl.increaseStyleRef(getOwner()
                        .getRealizedWorkbook(), this);
                getOwner().updateModifiedTime();
            }
        }
    }

    public String getTextContent() {
        return getImplementation().getTextContent();
    }

    public void setTextContent(String textContent) {
        getImplementation().setTextContent(textContent);
        getOwner().updateModifiedTime();
    }

}