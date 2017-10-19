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

import static org.xmind.core.internal.dom.DOMConstants.TAG_META;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IMetaData;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.internal.Meta;
import org.xmind.core.util.DOMUtils;

@SuppressWarnings("deprecation")
public class MetaImpl extends Meta implements ICoreEventSource {

    private Document implementation;

    private WorkbookImpl ownedWorkbook;

    private ElementRegistry elementRegistry;

    public MetaImpl(Document implementation) {
        super();
        this.implementation = implementation;
        init();
    }

    /**
     * @param ownedWorkbook
     *            the ownedWorkbook to set
     */
    protected void setOwnedWorkbook(WorkbookImpl ownedWorkbook) {
        this.ownedWorkbook = ownedWorkbook;
    }

    private void init() {
        Element m = DOMUtils.ensureChildElement(implementation, TAG_META);
        NS.setNS(NS.Meta, m);
        InternalDOMUtils.addVersion(implementation);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof MetaImpl))
            return false;
        MetaImpl that = (MetaImpl) obj;
        return that.implementation == this.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(implementation);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Document.class))
            return adapter.cast(implementation);
        if (ElementRegistry.class.equals(adapter))
            return adapter.cast(getElementRegistry());
        return super.getAdapter(adapter);
    }

    public Document getImplementation() {
        return implementation;
    }

    protected Element getMetaElement() {
        return implementation.getDocumentElement();
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return ownedWorkbook == null;
    }

    private String[] getKeys(String keyPath) {
        return keyPath.split(SEP);
    }

    private Element findElementByPath(String keyPath, boolean ensure) {
        String[] keys = getKeys(keyPath);
        if (keys.length == 0)
            return null;
        Element e = getMetaElement();
        Element c = null;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (!"".equals(key)) { //$NON-NLS-1$
                if (ensure) {
                    c = DOMUtils.ensureChildElement(e, key);
                } else {
                    c = DOMUtils.getFirstChildElementByTag(e, key);
                }
                if (c == null)
                    return null;
                e = c;
            }
        }
        return c;
    }

    public String getValue(String keyPath) {
        Element d = findElementByPath(keyPath, false);
        return d == null ? null : d.getTextContent();
    }

    public void setValue(String keyPath, String value) {
        String oldValue = null;
        Element d;
        if (value == null) {
            d = findElementByPath(keyPath, false);
            if (d != null && d.getParentNode() != null) {
                oldValue = d.getTextContent();
                d.getParentNode().removeChild(d);
            }
        } else {
            d = findElementByPath(keyPath, true);
            if (d != null) {
                oldValue = d.getTextContent();
                d.setTextContent(value);
            }
        }
        getCoreEventSupport().dispatchTargetValueChange(this, Core.Metadata,
                keyPath, oldValue, value);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IMeta#getKeyPaths()
     */
    public Set<String> getKeyPaths() {
        Set<String> keyPaths = new HashSet<String>();
        collectKeyPaths(getMetaElement(), null, keyPaths);
        return keyPaths;
    }

    private void collectKeyPaths(Element parentEle, String parentKeyPath,
            Set<String> keyPaths) {
        Iterator<Element> it = DOMUtils.childElementIter(parentEle);
        while (it.hasNext()) {
            Element childEle = it.next();
            String childKey = childEle.getTagName();
            String childKeyPath = parentKeyPath == null ? childKey
                    : parentKeyPath + SEP + childKey;
            if (childEle.hasChildNodes()) {
                collectKeyPaths(childEle, childKeyPath, keyPaths);
            } else {
                keyPaths.add(childKeyPath);
            }
        }
    }

    /**
     * @deprecated
     */
    public void addMetaData(IMetaData data) {
        Element mdEle = ((MetaDataImpl) data).getImplementation();
        getMetaElement().appendChild(mdEle);
        String keyPath = ((MetaDataImpl) data).getKeyPath();
        String newValue = data.getValue();
        getCoreEventSupport().dispatchTargetValueChange(this, Core.Metadata,
                keyPath, null, newValue);
    }

    /**
     * @deprecated
     */
    public void removeMetaData(IMetaData data) {
        String keyPath = ((MetaDataImpl) data).getKeyPath();
        String oldValue = data.getValue();
        Element mdEle = ((MetaDataImpl) data).getImplementation();
        getMetaElement().removeChild(mdEle);
        getCoreEventSupport().dispatchTargetValueChange(this, Core.Metadata,
                keyPath, oldValue, null);
    }

    /**
     * @deprecated
     */
    public IMetaData createMetaData(String key) {
        Element mdEle = implementation.createElement(key);
        MetaDataImpl md = new MetaDataImpl(mdEle, this);
        getElementRegistry().registerByKey(mdEle, md);
        return md;
    }

    /**
     * @deprecated
     */
    public IMetaData[] getMetaData(String key) {
        List<IMetaData> list = new ArrayList<IMetaData>();
        Iterator<Element> it = DOMUtils.childElementIterByTag(getMetaElement(),
                key);
        while (it.hasNext()) {
            Element mdEle = it.next();
            list.add(getMetaData(mdEle));
        }
        return list.toArray(new IMetaData[list.size()]);
    }

    /**
     * @deprecated
     */
    protected MetaDataImpl getMetaData(Element mdEle) {
        if (elementRegistry != null) {
            Object md = elementRegistry.getElement(mdEle);
            if (md != null && md instanceof IMetaData)
                return (MetaDataImpl) md;
        }
        MetaDataImpl md = new MetaDataImpl(mdEle, this);
        getElementRegistry().registerByKey(mdEle, md);
        return md;
    }

    public ElementRegistry getElementRegistry() {
        if (elementRegistry == null) {
            elementRegistry = new ElementRegistry();
        }
        return elementRegistry;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.event.ICoreEventSource#getCoreEventSupport()
     */
    public ICoreEventSupport getCoreEventSupport() {
        return ownedWorkbook.getCoreEventSupport();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.core.event.ICoreEventSource#registerCoreEventListener(java.
     * lang.String, org.xmind.core.event.ICoreEventListener)
     */
    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

}
