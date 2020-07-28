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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IAdaptable;
import org.xmind.core.internal.ElementRegistry;

public class NodeAdaptableProvider implements INodeAdaptableProvider {

    private ElementRegistry registry;

    private INodeAdaptableFactory factory;

    private Document primaryDocument;

    public NodeAdaptableProvider(ElementRegistry registry,
            INodeAdaptableFactory factory, Document primaryDocument) {
        this.registry = registry;
        this.factory = factory;
        this.primaryDocument = primaryDocument;
    }

    private Object getID(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            String id = e.getAttribute(ATTR_ID);
            if (id != null && !"".equals(id)) { //$NON-NLS-1$
                Document doc = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node
                        : node.getOwnerDocument();
                if (doc.equals(primaryDocument)) {
                    return id;
                }
                return new IDKey(doc, id);
            }
        }
        return null;
    }

    /**
     * Get the object adaptable to the DOM node. If no existing one is found, a
     * new proper one will be created and retruned.
     * 
     * @param node
     *            A DOM node
     * @return the adaptable object
     */
    public IAdaptable getAdaptable(Node node) {
        if (node == null)
            return null;

        /*
         * first, if the element has an ID, we look through the registry to see
         * if there's any existing element with that ID
         */
        Object id = getID(node);
        if (id != null) {
            Object e = registry.getElement(id);
            if (e != null && e instanceof IAdaptable)
                return (IAdaptable) e;
        }

        /*
         * second, look through the registry using the element itself as the key
         */
        Object e = registry.getElement(node);
        if (e instanceof IAdaptable)
            return (IAdaptable) e;

        /*
         * if no found, create a new adapter wrapping the DOM element
         */
        IAdaptable adaptable = createAdaptable(node);
        if (adaptable != null) {
            register(adaptable, node);
            return adaptable;
        }

        return null;
    }

    private void register(IAdaptable adapter, Node node) {
        Object id = getID(node);
        if (id != null) {
            registry.registerByKey(id, adapter);
        }
        registry.registerByKey(node, adapter);
    }

    private IAdaptable createAdaptable(Node node) {
        IAdaptable adaptable = factory.createAdaptable(node);
        if (adaptable != null && adaptable.getAdapter(Node.class) == node)
            return adaptable;
        return null;
    }

}