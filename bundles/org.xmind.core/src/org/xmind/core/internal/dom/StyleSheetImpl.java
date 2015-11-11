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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_AUTOMATIC_STYLES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MASTER_STYLES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_STYLE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_STYLES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_STYLE_SHEET;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.IManifest;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.internal.StyleSheet;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.IPropertiesProvider;
import org.xmind.core.util.Property;

public class StyleSheetImpl extends StyleSheet implements INodeAdaptableFactory,
        ICoreEventSource, IPropertiesProvider {

    private Document implementation;

    private ElementRegistry elementRegistry = null;

    private NodeAdaptableProvider nodeAdaptableProvider = null;

    private CoreEventSupport coreEventSupport = null;

    private Properties properties = null;

    private IManifest manifest = null;

    public StyleSheetImpl(Document implementation) {
        this.implementation = implementation;
        init();
    }

    private void init() {
        Element s = DOMUtils.ensureChildElement(implementation,
                TAG_STYLE_SHEET);
        NS.setNS(NS.Style, s, NS.SVG, NS.Fo);
        InternalDOMUtils.addVersion(implementation);
    }

    public Document getImplementation() {
        return implementation;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ICoreEventSource.class)
            return this;
        if (adapter == ElementRegistry.class)
            return getElementRegistry();
        if (adapter == ICoreEventSupport.class)
            return getCoreEventSupport();
        if (adapter == IPropertiesProvider.class)
            return this;
        if (adapter == Properties.class)
            return getProperties();
        if (adapter == INodeAdaptableFactory.class)
            return this;
        if (adapter == INodeAdaptableProvider.class)
            return getNodeAdaptableProvider();
        if (adapter == Document.class || adapter == Node.class)
            return implementation;
        return super.getAdapter(adapter);
    }

    protected Element getSheetElement() {
        return implementation.getDocumentElement();
    }

    public boolean isEmpty() {
        return !getSheetElement().hasChildNodes();
    }

    @Override
    protected IStyle getLocalStyle(String styleId) {
        Object element = getElementById(styleId);
        return element instanceof IStyle ? (IStyle) element : null;
    }

    public IStyle createStyle(String type) {
        Element s = implementation.createElement(TAG_STYLE);
        s.setAttribute(ATTR_TYPE, type);
        StyleImpl style = new StyleImpl(s, this);
        getElementRegistry().register(style);
        return style;
    }

    public Set<IStyle> getStyles(String groupName) {
        String groupTag = getGroupTag(groupName);
        if (groupTag != null) {
            Element ss = DOMUtils.getFirstChildElementByTag(getSheetElement(),
                    groupTag);
            if (ss != null) {
                return DOMUtils.getChildSet(ss, TAG_STYLE,
                        getNodeAdaptableProvider());
            }
        }
        return NO_STYLES;
    }

    public void addStyle(IStyle style, String groupName) {
        String groupTag = getGroupTag(groupName);
        if (groupTag == null)
            return;

        Element s = ((StyleImpl) style).getImplementation();
        Element as = DOMUtils.ensureChildElement(getSheetElement(), groupTag);
        Node n = as.appendChild(s);
        if (n != null) {
            fireTargetChange(Core.StyleAdd, style);
        }
    }

    public String findOwnedGroup(IStyle style) {
        StyleImpl s = (StyleImpl) style;
        Node p = s.getImplementation().getParentNode();
        if (p instanceof Element) {
            String groupTag = ((Element) p).getTagName();
            return getGroupName(groupTag);
        }
        return null;
    }

    private String getGroupTag(String groupName) {
        if (NORMAL_STYLES.equals(groupName))
            return TAG_STYLES;
        if (MASTER_STYLES.equals(groupName))
            return TAG_MASTER_STYLES;
        if (AUTOMATIC_STYLES.equals(groupName))
            return TAG_AUTOMATIC_STYLES;
        return null;
    }

    private String getGroupName(String groupTag) {
        if (TAG_STYLES.equals(groupTag))
            return NORMAL_STYLES;
        if (TAG_MASTER_STYLES.equals(groupTag))
            return MASTER_STYLES;
        if (TAG_AUTOMATIC_STYLES.equals(groupTag))
            return AUTOMATIC_STYLES;
        return null;
    }

    public void removeStyle(IStyle style) {
        Element s = ((StyleImpl) style).getImplementation();
        Node p = s.getParentNode();
        if (p instanceof Element) {
            Element ss = (Element) p;
            Element sheet = getSheetElement();
            if (ss.getParentNode() == sheet) {
                Node n = ss.removeChild(s);
                if (n != null) {
                    if (!ss.hasChildNodes()) {
                        sheet.removeChild(ss);
                    }
                    fireTargetChange(Core.StyleRemove, style);
                }
            }
        }
    }

    protected Object getElementById(String id) {
        Object element = getElementRegistry().getElement(id);
        if (element == null) {
            Element domElement = implementation.getElementById(id);
            if (domElement != null) {
                element = getNodeAdaptable(domElement);
            }
        }
        return element;
    }

    public ElementRegistry getElementRegistry() {
        if (elementRegistry == null)
            elementRegistry = new ElementRegistry();
        return elementRegistry;
    }

    protected NodeAdaptableProvider getNodeAdaptableProvider() {
        if (nodeAdaptableProvider == null)
            nodeAdaptableProvider = new NodeAdaptableProvider(
                    getElementRegistry(), this, implementation);
        return nodeAdaptableProvider;
    }

    protected IAdaptable getNodeAdaptable(Node node) {
        return getNodeAdaptableProvider().getAdaptable(node);
    }

    public IAdaptable createAdaptable(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            String tagName = e.getTagName();
            if (TAG_STYLE.equals(tagName)) {
                return new StyleImpl(e, this);
            }
        }
        return null;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof StyleSheetImpl))
            return false;
        StyleSheetImpl that = (StyleSheetImpl) obj;
        return this.implementation == that.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(implementation);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public IManifest getManifest() {
        return manifest;
    }

    public void setManifest(IManifest manifest) {
        this.manifest = manifest;
    }

    public void save(OutputStream out) throws IOException, CoreException {
        DOMUtils.save(implementation, out, false);
    }

    public IStyle importStyle(IStyle style) {
        if (style == null)
            return null;
        if (style instanceof StyleImpl) {
            StyleImpl s = (StyleImpl) style;
            return WorkbookUtilsImpl.importStyle(this, s,
                    (StyleSheetImpl) style.getOwnedStyleSheet());
        }
        return importStyleFromOtherImpl(style);
    }

    private IStyle importStyleFromOtherImpl(IStyle style) {
        IStyle target = createStyle(style.getType());
        Iterator<Property> sourcePropIt = style.properties();
        while (sourcePropIt.hasNext()) {
            Property sourceProperty = sourcePropIt.next();
            target.setProperty(sourceProperty.key, sourceProperty.value);
        }
        Iterator<Property> sourceDSIt = style.defaultStyles();
        while (sourceDSIt.hasNext()) {
            Property sourceDS = sourceDSIt.next();
            target.setDefaultStyleId(sourceDS.key, sourceDS.value);
        }
        IStyleSheet sourceSheet = style.getOwnedStyleSheet();
        if (sourceSheet != null) {
            String group = sourceSheet.findOwnedGroup(style);
            if (group != null)
                addStyle(target, group);
        }
        return target;
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    public ICoreEventSupport getCoreEventSupport() {
        if (coreEventSupport != null)
            return coreEventSupport;

        coreEventSupport = new CoreEventSupport();
        return coreEventSupport;
    }

    private void fireTargetChange(String type, Object target) {
        getCoreEventSupport().dispatchTargetChange(this, type, target);
    }

}