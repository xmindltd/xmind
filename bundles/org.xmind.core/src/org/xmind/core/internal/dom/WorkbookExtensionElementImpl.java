package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_OBJECT_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_PATH;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtension;
import org.xmind.core.IWorkbookExtensionElement;
import org.xmind.core.internal.WorkbookExtensionElement;
import org.xmind.core.util.DOMUtils;

/**
 * @author Jason Wong
 */
public class WorkbookExtensionElementImpl extends WorkbookExtensionElement {

    private Element implementation;

    private WorkbookImpl workbook;

    private WorkbookExtensionImpl extension;

    public WorkbookExtensionElementImpl(Element implementation,
            WorkbookImpl workbook, WorkbookExtensionImpl extension) {
        this.implementation = implementation;
        this.workbook = workbook;
        this.extension = extension;
    }

    public Element getImplementation() {
        return implementation;
    }

    public IWorkbook getOwnedWorkbook() {
        return workbook;
    }

    public IWorkbookExtension getExtension() {
        return extension;
    }

    public String getName() {
        return implementation.getTagName();
    }

    public List<IWorkbookExtensionElement> getChildren() {
        return getChildren(null);
    }

    public List<IWorkbookExtensionElement> getChildren(String elementName) {
        return DOMUtils.getChildList(implementation, elementName, extension);
    }

    public IWorkbookExtensionElement getParent() {
        Node p = implementation.getParentNode();
        if (p == null || !(p instanceof Element))
            return null;
        return extension.getElement((Element) p);
    }

    public IWorkbookExtensionElement createChild(String elementName) {
        Element childImpl = DOMUtils.createElement(implementation, elementName);
        WorkbookExtensionElementImpl child = new WorkbookExtensionElementImpl(
                childImpl, workbook, extension);
        registerChild(child);
        workbook.updateModificationInfo();
        return child;
    }

    public IWorkbookExtensionElement getFirstChild(String elementName) {
        Element childImpl = DOMUtils.getFirstChildElementByTag(implementation,
                elementName);
        return childImpl == null ? null : extension.getElement(childImpl);
    }

    public void addChild(IWorkbookExtensionElement child, int index) {
        WorkbookExtensionElementImpl c = (WorkbookExtensionElementImpl) child;
        if (c.getExtension() != this.getExtension()
                || c.getOwnedWorkbook() != this.getOwnedWorkbook())
            return;

        IWorkbookExtensionElement oldParent = c.getParent();
        if (oldParent != null)
            oldParent.deleteChild(child);

        Element childImpl = c.getImplementation();
        Element[] es = DOMUtils.getChildElements(implementation);
        if (index >= 0 && index < es.length)
            implementation.insertBefore(childImpl, es[index]);
        else
            implementation.appendChild(childImpl);
        registerChild(c);
        workbook.updateModificationInfo();
    }

    public void deleteChild(IWorkbookExtensionElement child) {
        WorkbookExtensionElementImpl c = (WorkbookExtensionElementImpl) child;
        Element childImpl = c.getImplementation();
        if (childImpl.getParentNode() == implementation) {
            unregisterChild(c);
            implementation.removeChild(childImpl);
            workbook.updateModificationInfo();
        }
    }

    public void deleteChildren() {
        deleteChildren(null);
    }

    public void deleteChildren(String elementName) {
        Element[] children;
        if (elementName == null)
            children = DOMUtils.getChildElements(implementation);
        else
            children = DOMUtils.getChildElementsByTag(implementation,
                    elementName);

        for (int i = 0; i < children.length; i++)
            implementation.removeChild(children[i]);

        if (children.length > 0)
            workbook.updateModificationInfo();
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

    public void setAttribute(String attrName, String attrValue) {
        DOMUtils.setAttribute(implementation, attrName, attrValue);
        workbook.updateModificationInfo();
    }

    public String getTextContent() {
        Node c = implementation.getFirstChild();
        if (c != null && c.getNodeType() == Node.TEXT_NODE)
            return c.getTextContent();
        return null;
    }

    public void setTextContent(String text) {
        Node c = implementation.getFirstChild();
        if (text == null) {
            if (c != null) {
                implementation.removeChild(c);
                workbook.updateModificationInfo();
            }
        } else {
            if (c != null && c.getNodeType() == Node.TEXT_NODE) {
                c.setTextContent(text);
            } else {
                Node t = implementation.getOwnerDocument().createTextNode(text);
                implementation.insertBefore(t, c);
            }
            workbook.updateModificationInfo();
        }
    }

    public void setResourcePath(String resourcePath) {
        setAttribute(ATTR_RESOURCE_PATH, resourcePath);
    }

    public String getResourcePath() {
        return getAttribute(ATTR_RESOURCE_PATH);
    }

    public void setObjectId(String objectId) {
        setAttribute(ATTR_OBJECT_ID, objectId);
    }

    public String getObjectId() {
        return getAttribute(ATTR_OBJECT_ID);
    }

    private void registerChild(WorkbookExtensionElementImpl child) {
        extension.registerElement(child);
    }

    private void unregisterChild(WorkbookExtensionElementImpl child) {
        extension.unregisterElement(child);
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookExtensionElementImpl))
            return false;
        WorkbookExtensionElementImpl that = (WorkbookExtensionElementImpl) obj;
        return this.implementation == that.implementation;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public String toString() {
        return "{element:}" + getName() + "}"; //$NON-NLS-1$//$NON-NLS-2$
    }

}
