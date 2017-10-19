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

import static org.xmind.core.internal.dom.DOMConstants.TAG_RESOURCE_REFS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IAdaptable;
import org.xmind.core.IResourceRef;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.TopicExtension;
import org.xmind.core.util.DOMUtils;

public class TopicExtensionImpl extends TopicExtension
        implements INodeAdaptableProvider {

    private Element implementation;

    private TopicImpl topic;

    private TopicExtensionElementImpl content;

    private Map<Element, TopicExtensionElementImpl> eleMap = new HashMap<Element, TopicExtensionElementImpl>();

    public TopicExtensionImpl(Element implementation, TopicImpl topic) {
        this.implementation = implementation;
        this.topic = topic;
    }

    public Element getImplementation() {
        return implementation;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TopicExtensionImpl))
            return false;
        TopicExtensionImpl that = (TopicExtensionImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "{topic-extension:" + getProviderName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private Element getContentElement() {
        return DOMUtils.ensureChildElement(implementation,
                DOMConstants.TAG_CONTENT);
    }

    public ITopicExtensionElement getContent() {
        if (content == null) {
            content = new TopicExtensionElementImpl(getContentElement(), topic,
                    this);
            registerElement(content);
        }
        return content;
    }

    public String getProviderName() {
        return implementation.getAttribute(DOMConstants.ATTR_PROVIDER);
    }

    public ITopic getParent() {
        return topic;
    }

    public ISheet getOwnedSheet() {
        return topic.getOwnedSheet();
    }

    public IWorkbook getOwnedWorkbook() {
        return topic.getOwnedWorkbook();
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    private Element getRefsElement() {
        return DOMUtils.getFirstChildElementByTag(implementation,
                TAG_RESOURCE_REFS);
    }

    public void addResourceRef(IResourceRef ref) {
        Element refEle = ((ResourceRefImpl) ref).getImplementation();
        Element refsEle = DOMUtils.ensureChildElement(implementation,
                TAG_RESOURCE_REFS);
        Node n = refsEle.appendChild(refEle);
        if (n != null) {
            if (!isOrphan()) {
                ((ResourceRefImpl) ref).addNotify(topic.getRealizedWorkbook());
            }
            //TODO fire resource ref added
            topic.updateModificationInfo();
        }
    }

    public List<IResourceRef> getResourceRefs() {
        Element refsEle = getRefsElement();
        if (refsEle != null)
            return DOMUtils.getChildList(refsEle, DOMConstants.TAG_RESOURCE_REF,
                    ((WorkbookImpl) topic.getOwnedWorkbook())
                            .getAdaptableRegistry());
        return EMPTY_REFS;
    }

    public void removeResourceRef(IResourceRef ref) {
        Element refsEle = getRefsElement();
        if (refsEle == null)
            return;
        Element refEle = ((ResourceRefImpl) ref).getImplementation();
        if (refEle.getParentNode() == refsEle) {
            ((ResourceRefImpl) ref).removeNotify(topic.getRealizedWorkbook());
            Node n = refsEle.removeChild(refEle);
            if (!refsEle.hasChildNodes())
                implementation.removeChild(refsEle);
            if (n != null) {
                //TODO fire resource ref removed
                topic.updateModificationInfo();
            }
        }
    }

    protected void addNotify(WorkbookImpl workbook) {
        for (IResourceRef ref : getResourceRefs()) {
            ((ResourceRefImpl) ref).addNotify(workbook);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        for (IResourceRef ref : getResourceRefs()) {
            ((ResourceRefImpl) ref).removeNotify(workbook);
        }
    }

    protected void registerElement(TopicExtensionElementImpl element) {
        eleMap.put(element.getImplementation(), element);
    }

    protected void unregisterElement(TopicExtensionElementImpl element) {
        eleMap.remove(element.getImplementation());
    }

    protected TopicExtensionElementImpl getElement(Element impl) {
        if (impl == implementation)
            return null;
        TopicExtensionElementImpl ele = eleMap.get(impl);
        if (ele == null) {
            ele = new TopicExtensionElementImpl(impl, topic, this);
            registerElement(ele);
        }
        return ele;
    }

    public IAdaptable getAdaptable(Node node) {
        if (node instanceof Element)
            return getElement((Element) node);
        return null;
    }

}
