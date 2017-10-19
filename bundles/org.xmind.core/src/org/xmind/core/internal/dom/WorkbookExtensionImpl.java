package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_PROVIDER;
import static org.xmind.core.internal.dom.DOMConstants.TAG_CONTENT;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REFS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IAdaptable;
import org.xmind.core.IResourceRef;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtensionElement;
import org.xmind.core.internal.WorkbookExtension;
import org.xmind.core.util.DOMUtils;

/**
 * @author Jason Wong
 */
public class WorkbookExtensionImpl extends WorkbookExtension
        implements INodeAdaptableProvider {

    private Map<Element, WorkbookExtensionElementImpl> eleMap = new HashMap<Element, WorkbookExtensionElementImpl>();

    private Document implementation;

    private WorkbookImpl workbook;

    private WorkbookExtensionElementImpl content;

    public WorkbookExtensionImpl(Document implementation,
            WorkbookImpl workbook) {
        this.implementation = implementation;
        this.workbook = workbook;
        init();
    }

    private void init() {
        Element e = getExtensionElement();
        for (NS ns : NS.values()) {
            if (ns.getPrefix().equals(e.getTagName()))
                NS.setNS(ns, e);
        }
    }

    private Element getExtensionElement() {
        return implementation.getDocumentElement();
    }

    protected void addNotify(WorkbookImpl workbook) {
        for (IResourceRef ref : getResourceRefs())
            ((ResourceRefImpl) ref).addNotify(workbook);
    }

    protected void removeNotify(WorkbookImpl workbook) {
        for (IResourceRef ref : getResourceRefs())
            ((ResourceRefImpl) ref).removeNotify(workbook);
    }

    public Document getImplementation() {
        return implementation;
    }

    public String getProviderName() {
        return getExtensionElement().getAttribute(ATTR_PROVIDER);
    }

    public IWorkbookExtensionElement getContent() {
        if (content == null) {
            content = new WorkbookExtensionElementImpl(getContentElement(),
                    workbook, this);
            registerElement(content);
        }
        return content;
    }

    private Element getContentElement() {
        return DOMUtils.ensureChildElement(getExtensionElement(), TAG_CONTENT);
    }

    public List<IResourceRef> getResourceRefs() {
        Element refsEle = getRefsElement();
        if (refsEle != null)
            return DOMUtils.getChildList(refsEle, TAG_RESOURCE_REF,
                    workbook.getAdaptableRegistry());
        return EMPTY_REFS;
    }

    public IResourceRef getResourceRef(String resourceId) {
        if (resourceId != null && !"".equals(resourceId)) { //$NON-NLS-1$
            for (IResourceRef ref : getResourceRefs()) {
                if (resourceId.equals(ref.getResourceId()))
                    return ref;
            }
        }
        return null;
    }

    public void addResourceRef(IResourceRef ref) {
        importResourceRef(ref);

        Element refEle = ((ResourceRefImpl) ref).getImplementation();
        Element refsEle = DOMUtils.ensureChildElement(getExtensionElement(),
                TAG_RESOURCE_REFS);
        Node n = refsEle.appendChild(refEle);
        if (n != null) {
            if (!isOrphan())
                ((ResourceRefImpl) ref).addNotify(workbook);
        }
        workbook.updateModificationInfo();
    }

    private void importResourceRef(IResourceRef ref) {
        Element oldValue = ((ResourceRefImpl) ref).getImplementation();

        Element newValue = implementation.createElement(oldValue.getTagName());
        newValue.setAttribute(DOMConstants.ATTR_RESOURCE_ID,
                ref.getResourceId());
        newValue.setAttribute(DOMConstants.ATTR_TYPE, ref.getType());

        ((ResourceRefImpl) ref).setImplementation(newValue);
    }

    public void removeResourceRef(IResourceRef ref) {
        Element refsEle = getRefsElement();
        if (refsEle == null)
            return;

        Element refEle = ((ResourceRefImpl) ref).getImplementation();
        if (refEle.getParentNode() == refsEle) {
            ((ResourceRefImpl) ref).removeNotify(workbook);
            Node n = refsEle.removeChild(refEle);
            if (!refsEle.hasChildNodes())
                getExtensionElement().removeChild(refsEle);
            if (n != null)
                workbook.updateModificationInfo();
        }
    }

    private Element getRefsElement() {
        return DOMUtils.getFirstChildElementByTag(getExtensionElement(),
                TAG_RESOURCE_REFS);
    }

    public IWorkbook getOwnedWorkbook() {
        return workbook;
    }

    public IAdaptable getAdaptable(Node node) {
        if (node instanceof Element)
            return getElement((Element) node);
        return null;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImplementation());
        return super.getAdapter(adapter);
    }

    protected WorkbookExtensionElementImpl getElement(Element impl) {
        if (impl == getExtensionElement())
            return null;

        WorkbookExtensionElementImpl ele = eleMap.get(impl);
        if (ele == null) {
            ele = new WorkbookExtensionElementImpl(impl, workbook, this);
            registerElement(ele);
        }
        return ele;
    }

    protected void registerElement(WorkbookExtensionElementImpl element) {
        eleMap.put(element.getImplementation(), element);
    }

    protected void unregisterElement(WorkbookExtensionElementImpl element) {
        eleMap.remove(element.getImplementation());
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof WorkbookExtensionImpl))
            return false;
        WorkbookExtensionImpl that = (WorkbookExtensionImpl) obj;
        return this.implementation == that.implementation;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    @Override
    public String toString() {
        return "{workbook-extension:" + getProviderName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
