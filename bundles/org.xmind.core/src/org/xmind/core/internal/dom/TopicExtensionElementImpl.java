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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.TopicExtensionElement;
import org.xmind.core.util.DOMUtils;

public class TopicExtensionElementImpl extends TopicExtensionElement {

    private Element implementation;

    private TopicImpl topic;

    private TopicExtensionImpl extension;

    public TopicExtensionElementImpl(Element implementation, TopicImpl topic,
            TopicExtensionImpl extension) {
        super();
        this.implementation = implementation;
        this.topic = topic;
        this.extension = extension;
    }

    public Element getImplementation() {
        return implementation;
    }

    public IWorkbook getOwnedWorkbook() {
        return topic.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(getImplementation());
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TopicExtensionElementImpl))
            return false;
        TopicExtensionElementImpl that = (TopicExtensionElementImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "{element:" + getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getName() {
        return implementation.getTagName();
    }

    public ITopicExtensionElement createChild(String elementName) {
        Element childImpl = DOMUtils.createElement(implementation, elementName);
        TopicExtensionElementImpl child = new TopicExtensionElementImpl(
                childImpl, topic, extension);
        registerChild(child);
        topic.updateModificationInfo();
        return child;
    }

    private void registerChild(TopicExtensionElementImpl child) {
        extension.registerElement(child);
    }

    private void unregisterChild(TopicExtensionElementImpl child) {
        extension.unregisterElement(child);
    }

    public void addChild(ITopicExtensionElement child, int index) {
        TopicExtensionElementImpl c = (TopicExtensionElementImpl) child;
        if (c.getExtension() != this.getExtension()
                || c.getTopic() != this.getTopic())
            return;

        ITopicExtensionElement oldParent = c.getParent();
        if (oldParent != null) {
            oldParent.deleteChild(child);
        }
        Element childImpl = c.getImplementation();
        Element[] es = DOMUtils.getChildElements(implementation);
        if (index >= 0 && index < es.length) {
            implementation.insertBefore(childImpl, es[index]);
        } else {
            implementation.appendChild(childImpl);
        }
        registerChild(c);
        topic.updateModificationInfo();
    }

    public void deleteChild(ITopicExtensionElement child) {
        TopicExtensionElementImpl c = (TopicExtensionElementImpl) child;
        Element childImpl = c.getImplementation();
        if (childImpl.getParentNode() == implementation) {
            unregisterChild(c);
            implementation.removeChild(childImpl);
            topic.updateModificationInfo();
        }
    }

    public void deleteChildren(String elementName) {
        Element[] children;
        if (elementName == null)
            children = DOMUtils.getChildElements(implementation);
        else
            children = DOMUtils.getChildElementsByTag(implementation,
                    elementName);
        for (int i = 0; i < children.length; i++) {
            implementation.removeChild(children[i]);
        }
        if (children.length > 0)
            topic.updateModificationInfo();
    }

    public void deleteChildren() {
        deleteChildren(null);
    }

    public Set<String> getAttributeKeys() {
        Set<String> keys = new HashSet<String>();
        NamedNodeMap atts = implementation.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node att = atts.item(i);
            String key = att.getNodeName();
            if (key != null && !"".equals(key)) //$NON-NLS-1$
                keys.add(key);
        }
        return keys;
    }

    public String getAttribute(String attrName) {
        return DOMUtils.getAttribute(implementation, attrName);
    }

    public List<ITopicExtensionElement> getChildren() {
        return DOMUtils.getChildList(implementation, null, extension);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.core.ITopicExtensionElement#getFirstChild(java.lang.String)
     */
    public ITopicExtensionElement getFirstChild(String elementName) {
        Element childImpl = DOMUtils.getFirstChildElementByTag(implementation,
                elementName);
        return childImpl == null ? null : extension.getElement(childImpl);
    }

    public List<ITopicExtensionElement> getChildren(String elementName) {
        return DOMUtils.getChildList(implementation, elementName, extension);
    }

    public ITopicExtension getExtension() {
        return extension;
    }

    public ITopicExtensionElement getParent() {
        Node p = implementation.getParentNode();
        if (p == null || !(p instanceof Element))
            return null;
        return extension.getElement((Element) p);
    }

    public String getTextContent() {
        Node c = implementation.getFirstChild();
        if (c != null && c.getNodeType() == Node.TEXT_NODE)
            return c.getTextContent();
        return null;
    }

    public ITopic getTopic() {
        return topic;
    }

    public void setAttribute(String attrName, String attrValue) {
        DOMUtils.setAttribute(implementation, attrName, attrValue);
        topic.updateModificationInfo();
    }

    public void setTextContent(String text) {
        Node c = implementation.getFirstChild();
        if (text == null) {
            if (c != null) {
                implementation.removeChild(c);
                topic.updateModificationInfo();
            }
        } else {
            if (c != null && c.getNodeType() == Node.TEXT_NODE) {
                c.setTextContent(text);
            } else {
                Node t = implementation.getOwnerDocument().createTextNode(text);
                implementation.insertBefore(t, c);
            }
            topic.updateModificationInfo();
        }
    }

}