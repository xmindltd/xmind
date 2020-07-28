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
package org.xmind.ui.internal.wizards;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.style.IStyled;
import org.xmind.ui.wizards.ExportPart;

public abstract class HtmlExportPart extends ExportPart {

    private HtmlExportPart parent;

    private Node node;

    private Node contentNode;

    public HtmlExportPart(HtmlExporter exporter, Object element) {
        super(exporter, element);
    }

    public HtmlExporter getExporter() {
        return (HtmlExporter) super.getExporter();
    }

    public void setParent(HtmlExportPart parent) {
        this.parent = parent;
    }

    public Node getNode() {
        if (node == null)
            node = createNode();
        return node;
    }

    public Node getContentNode(HtmlExportPart child) {
        if (contentNode == null) {
            contentNode = createContentNode();
        }
        return contentNode;
    }

    public void addChildElement(HtmlExportPart child, Node childNode) {
        getContentNode(child).appendChild(childNode);
    }

    public void addToParent(HtmlExportPart parent) {
        parent.addChildElement(this, getNode());
    }

    protected Node createContentNode() {
        return getNode();
    }

    public HtmlExportPart getParent() {
        return parent;
    }

    protected abstract Node createNode();

    public Document getDocument() {
        return getExporter().getDocument();
    }

    protected Element createDOMElement(String tagName) {
        return getDocument().createElement(tagName);
    }

    protected Node createText(String textContent) {
        return getDocument().createTextNode(textContent);
    }

    protected String newPath(String parent, String name, String ext) {
        return getExporter().newPath(parent, name, ext);
    }

    protected String connectPath(String parent, String child) {
        return getExporter().connectPath(parent, child);
    }

    protected void writeStyle(Element element, IStyled styled) {
        String style = getExporter().addStyle(styled.getStyleId());
        if (style != null) {
            element.setAttribute(HtmlConstants.ATT_STYLE, style);
        }
    }

}