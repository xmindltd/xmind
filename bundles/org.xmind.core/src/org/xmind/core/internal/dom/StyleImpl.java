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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_NAME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_FAMILY;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_DEFAULT_STYLE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_PROPERTIES;

import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Style;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.Property;

public class StyleImpl extends Style implements ICoreEventSource {

    private final class PropertyIter implements Iterator<Property> {

        Iterator<Element> it = propertiesElementIter();

        Element propEle = it.next();

        NamedNodeMap map = propEle == null ? null : propEle.getAttributes();

        int index = 0;

        Property next = findNextProperty();

        private Property findNextProperty() {
            if (map != null) {
                if (index < map.getLength()) {
                    Attr attr = (Attr) map.item(index);
                    index++;
                    return new Property(attr.getName(), attr.getValue());
                }
                if (it.hasNext()) {
                    propEle = it.next();
                    map = propEle == null ? null : propEle.getAttributes();
                    index = 0;
                    if (map != null) {
                        return findNextProperty();
                    }
                }
            }
            return null;
        }

        public void remove() {
        }

        public Property next() {
            if (next == null)
                return next;

            Property result = next;
            next = findNextProperty();
            return result;
        }

        public boolean hasNext() {
            return next != null;
        }
    }

    private final class DefaultStyleIter implements Iterator<Property> {

        Iterator<Element> propEleIt = propertiesElementIter();

        Element propEle = null;

        Iterator<Element> defaultStyleEleIt = null;

        Property next = findNextDefaultStyle();

        private Property findNextDefaultStyle() {
            if (defaultStyleEleIt != null) {
                while (defaultStyleEleIt.hasNext()) {
                    Element defaultStyleEle = defaultStyleEleIt.next();
                    String family = DOMUtils.getAttribute(defaultStyleEle,
                            ATTR_STYLE_FAMILY);
                    if (family != null) {
                        String styleId = DOMUtils.getAttribute(defaultStyleEle,
                                ATTR_STYLE_ID);
                        return new Property(family, styleId);
                    }
                }
            }
            if (propEleIt.hasNext()) {
                propEle = propEleIt.next();
                defaultStyleEleIt = DOMUtils.childElementIterByTag(propEle,
                        TAG_DEFAULT_STYLE);
                return findNextDefaultStyle();
            }
            return null;
        }

        public boolean hasNext() {
            return next != null;
        }

        public Property next() {
            if (next == null)
                return next;

            Property result = next;
            next = findNextDefaultStyle();
            return result;
        }

        public void remove() {
        }

    }

    private Element implementation;

    private StyleSheetImpl ownedSheet;

    public StyleImpl(Element implementation, StyleSheetImpl ownedSheet) {
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedSheet = ownedSheet;
    }

    public String getId() {
        return implementation.getAttribute(DOMConstants.ATTR_ID);
    }

    public String getType() {
        return implementation.getAttribute(ATTR_TYPE);
    }

    public Element getImplementation() {
        return implementation;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof StyleImpl))
            return false;
        StyleImpl that = (StyleImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "STY#" + getId() + "(" + getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    public IStyleSheet getOwnedStyleSheet() {
        return ownedSheet;
    }

    public String getName() {
        String name = implementation.getAttribute(ATTR_NAME);
        if (name.startsWith("%")) { //$NON-NLS-1$
            Properties properties = ownedSheet.getProperties();
            if (properties != null) {
                String key = name.substring(1);
                name = properties.getProperty(key, name);
            }
        }
        return name;
    }

//    public Properties getProperties() {
//        Properties prop = new Properties();
//        Iterator<Element> it = propertiesElementsIter();
//        while (it.hasNext()) {
//            Element propEle = it.next();
//            NamedNodeMap attrs = propEle.getAttributes();
//            for (int i = 0; i < attrs.getLength(); i++) {
//                Attr attr = (Attr) attrs.item(i);
//                prop.setProperty(attr.getName(), attr.getValue());
//            }
//        }
//        return prop;
//    }

    public String getProperty(String key) {
        Iterator<Element> it = propertiesElementIter();
        while (it.hasNext()) {
            String value = DOMUtils.getAttribute(it.next(), key);
            if (value != null)
                return value;
        }
        return null;
    }

    private Iterator<Element> propertiesElementIter() {
        return DOMUtils.childElementIterByTag(implementation,
                getPropertiesElementName());
    }

    public void setProperty(String key, String value) {
        String oldValue = getProperty(key);
        String propEleName = getPropertiesElementName();
        Element p = DOMUtils.ensureChildElement(implementation, propEleName);
        DOMUtils.setAttribute(p, key, value);
        String newValue = getProperty(key);
        firePropertyChange(key, oldValue, newValue);
    }

    private String getPropertiesElementName() {
        return getType().toLowerCase() + "-" + TAG_PROPERTIES; //$NON-NLS-1$
    }

    public int size() {
        Iterator<Element> it = propertiesElementIter();
        int size = 0;
        while (it.hasNext()) {
            size += it.next().getAttributes().getLength();
        }
        return size;
    }

    public boolean isEmpty() {
        Iterator<Element> it = propertiesElementIter();
        while (it.hasNext()) {
            Element propEle = it.next();
            if (propEle.hasAttributes() || propEle.hasChildNodes())
                return false;
        }
        return true;
    }

    public Iterator<Property> properties() {
        return new PropertyIter();
    }

    public void setName(String name) {
        String oldValue = implementation.hasAttribute(ATTR_NAME) ? getName()
                : null;
        DOMUtils.setAttribute(implementation, ATTR_NAME, name);
        String newValue = implementation.hasAttribute(ATTR_NAME) ? getName()
                : null;
        fireValueChange(Core.Name, oldValue, newValue);
    }

    public Iterator<Property> defaultStyles() {
        return new DefaultStyleIter();
    }

    public String getDefaultStyleId(String styleFamily) {
        if (styleFamily == null || "".equals(styleFamily)) //$NON-NLS-1$
            return null;
        Iterator<Element> it = propertiesElementIter();
        while (it.hasNext()) {
            Iterator<Element> it2 = DOMUtils.childElementIterByTag(it.next(),
                    TAG_DEFAULT_STYLE);
            while (it2.hasNext()) {
                Element ds = it2.next();
                if (styleFamily.equals(ds.getAttribute(ATTR_STYLE_FAMILY))) {
                    return DOMUtils.getAttribute(ds, ATTR_STYLE_ID);
                }
            }
        }
        return null;
    }

    public void setDefaultStyleId(String styleFamily, String styleId) {
        if (styleFamily == null || "".equals(styleFamily)) //$NON-NLS-1$
            return;

        String propEleName = getPropertiesElementName();
        if (styleId != null) {
            Element p = DOMUtils.ensureChildElement(implementation,
                    propEleName);
            Element ds = findDefaultStyleElement(p, styleFamily);
            if (ds == null) {
                ds = DOMUtils.createElement(p, TAG_DEFAULT_STYLE);
                DOMUtils.setAttribute(ds, ATTR_STYLE_FAMILY, styleFamily);
            }
            DOMUtils.setAttribute(ds, ATTR_STYLE_ID, styleId);
        } else {
            Element p = DOMUtils.getFirstChildElementByTag(implementation,
                    propEleName);
            if (p != null) {
                Element ds = findDefaultStyleElement(p, styleFamily);
                if (ds != null) {
                    Node n = p.removeChild(ds);
                    if (n != null) {
                        if (!p.hasChildNodes()) {
                            implementation.removeChild(p);
                        }
                    }
                }
            }
        }
    }

    private Element findDefaultStyleElement(Element propEle,
            String styleFamily) {
        Iterator<Element> it = DOMUtils.childElementIterByTag(propEle,
                TAG_DEFAULT_STYLE);
        while (it.hasNext()) {
            Element ds = it.next();
            if (styleFamily.equals(ds.getAttribute(ATTR_STYLE_FAMILY))) {
                return ds;
            }
        }
        return null;
    }

//    public boolean contentEquals(IStyle style) {
//        StyleImpl s = (StyleImpl) style;
//        if (!getType().equals(s.getType()))
//            return false;
//
//        int propSize = 0;
//        int dsSize = 0;
//
//        Iterator<Element> it = propertiesElementIter();
//        while (it.hasNext()) {
//            Element propEle = it.next();
//            NamedNodeMap attrs = propEle.getAttributes();
//            for (int i = 0; i < attrs.getLength(); i++) {
//                Node attr = attrs.item(i);
//                String key = attr.getNodeName();
//                String value = attr.getNodeValue();
//                if (!value.equals(s.getProperty(key)))
//                    return false;
//            }
//            Iterator<Element> dsIt = DOMUtils.childElementIterByTag(propEle,
//                    TAG_DEFAULT_STYLE);
//            while (dsIt.hasNext()) {
//                
//            }
//        }
//
//        return true;
//    }
//
//    public int getParentGroupId() {
//        Node p = implementation.getParentNode();
//        if (p != null && p instanceof Element) {
//            Element g = (Element) p;
//            String name = g.getTagName();
//            if (DOMConstants.TAG_STYLES.equals(name))
//                return IStyleSheet.GROUP_NORMAL;
//            if (DOMConstants.TAG_MASTER_STYLES.equals(name))
//                return IStyleSheet.GROUP_MASTER;
//            if (DOMConstants.TAG_AUTOMATIC_STYLES.equals(name))
//                return IStyleSheet.GROUP_AUTOMATIC;
//        }
//        return IStyleSheet.GROUP_NONE;
//    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    private void fireValueChange(String eventType, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, eventType, oldValue,
                newValue);
    }

    private void firePropertyChange(String key, String oldValue,
            String newValue) {
        getCoreEventSupport().dispatchTargetValueChange(this, Core.Property,
                key, oldValue, newValue);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedSheet.getCoreEventSupport();
    }

}
