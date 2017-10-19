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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.IAdaptable;

/**
 * @author Frank Shaka
 * 
 */
public class NodeAdaptableRegistry implements INodeAdaptableProvider {

    private Document defaultDocument;

    private INodeAdaptableFactory factory;

    private Map<IDKey, IAdaptable> idMap = new HashMap<IDKey, IAdaptable>();

    private Map<Node, IAdaptable> nodeMap = new HashMap<Node, IAdaptable>();

    /**
     * Used to retrieve adaptable object by ID.
     */
    private IDKey key = new IDKey(null, null);

    /**
     * 
     */
    public NodeAdaptableRegistry(Document defaultDocument,
            INodeAdaptableFactory factory) {
        this.defaultDocument = defaultDocument;
        this.factory = factory;
    }

    public IAdaptable getAdaptable(String id) {
        return getAdaptable(id, defaultDocument);
    }

    public IAdaptable getAdaptable(String id, Document document) {
        IAdaptable a = getAdaptableById(id, document);
        if (a == null) {
            Element element = document.getElementById(id);
            if (element != null) {
                a = getAdaptableByNode(element);
                if (a == null) {
                    a = createAdaptable(element);
                }
                if (a != null) {
                    registerByNode(a, element);
                    registerById(a, id, document);
                }
            }
        }
        return a;
    }

    public IAdaptable getAdaptable(Node node) {
        IAdaptable a = nodeMap.get(node);
        if (a == null) {
            a = createAdaptable(node);
            if (a != null) {
                registerByNode(a, node);
                String id = getId(node);
                if (id != null) {
                    registerById(a, id, node.getOwnerDocument());
                }
            }
        }
        return a;
    }

    public void register(IAdaptable adaptable, String id) {
        register(adaptable, id, defaultDocument);
    }

    public void register(IAdaptable adaptable, String id, Document document) {
        registerById(adaptable, id, document);
        Element element = document.getElementById(id);
        if (element != null) {
            registerByNode(adaptable, element);
        }
    }

    public void register(IAdaptable adaptable, Node node) {
        registerByNode(adaptable, node);
        String id = getId(node);
        if (id != null) {
            registerById(adaptable, id, node.getOwnerDocument());
        }
    }

    public void unregister(IAdaptable adaptable, String id) {
        unregister(adaptable, id, defaultDocument);
    }

    public void unregister(IAdaptable adaptable, String id, Document document) {
        unregisterById(adaptable, id, document);
        Element element = document.getElementById(id);
        if (element != null) {
            unregisterByNode(adaptable, element);
        }
    }

    public void unregister(IAdaptable adaptable, Node node) {
        unregisterByNode(adaptable, node);
        String id = getId(node);
        if (id != null) {
            unregisterById(adaptable, id, node.getOwnerDocument());
        }
    }

    private String getId(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Node v = node.getAttributes().getNamedItem(DOMConstants.ATTR_ID);
            if (v != null) {
                String id = v.getNodeValue();
                if (id != null && !"".equals(id)) { //$NON-NLS-1$
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * @param id
     * @param document
     * @return
     */
    private IDKey getIDKey(String id, Document document) {
        key.id = id;
        key.document = document;
        return key;
    }

    /**
     * @param id
     * @param document
     * @return
     */
    private IDKey createIDKey(String id, Document document) {
        return new IDKey(document, id);
    }

    public IAdaptable getAdaptableById(String id, Document document) {
        return idMap.get(getIDKey(id, document));
    }

    public IAdaptable getAdaptableByNode(Node node) {
        return nodeMap.get(node);
    }

    public void registerById(IAdaptable adaptable, String id, Document document) {
        idMap.put(createIDKey(id, document), adaptable);
    }

    public void registerByNode(IAdaptable adaptable, Node node) {
        nodeMap.put(node, adaptable);
    }

    public void unregisterById(IAdaptable adaptable, String id,
            Document document) {
        IDKey key = getIDKey(id, document);
        IAdaptable a = idMap.get(key);
        if (a == adaptable || (a != null && a.equals(adaptable))) {
            idMap.remove(key);
        }
    }

    public void unregisterByNode(IAdaptable adaptable, Node node) {
        IAdaptable a = nodeMap.get(node);
        if (a == adaptable || (a != null && a.equals(adaptable))) {
            nodeMap.remove(node);
        }
    }

    /**
     * @param element
     * @return
     */
    private IAdaptable createAdaptable(Node element) {
        return factory == null ? null : factory.createAdaptable(element);
    }

}
