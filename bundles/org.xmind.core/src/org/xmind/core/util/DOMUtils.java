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
package org.xmind.core.util;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IAdaptable;
import org.xmind.core.ITopic;
import org.xmind.core.internal.dom.INodeAdaptableProvider;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DOMUtils {

    private static class ElementIterator implements Iterator<Element> {

        private String tagName;

        private Node child;
//        private NodeList children;
//
//        private int index;

        private Element next;

        public ElementIterator(Node parent) {
            this(parent, null);
        }

        public ElementIterator(Node parent, String tagName) {
            this.tagName = tagName;
            this.child = parent.getFirstChild();
//            this.children = parent.getChildNodes();
//            this.index = 0;
            this.next = findNextElement();
        }

        private Element findNextElement() {
            if (child == null) {
                next = null;
            } else {
                while (child != null && !isElementByTag(child, tagName)) {
                    child = child.getNextSibling();
                }
                if (child != null) {
                    next = (Element) child;
                    child = child.getNextSibling();
                } else {
                    next = null;
                }
            }
//            for (int i = index; i < children.getLength(); i++) {
//                Node n = children.item(i);
//                if (isElementByTag(n, tagName)) {
//                    next = (Element) n;
//                    index = i + 1;
//                    return next;
//                }
//            }
            return next;
        }

        public boolean hasNext() {
            return next != null;
        }

        public Element next() {
            Element result = next;
            next = findNextElement();
            return result;
        }

        public void remove() {
        }

    }

    public static class AdaptableIterator<T extends IAdaptable>
            implements Iterator<T> {

        private Node node;

        private String tagName;

        private INodeAdaptableProvider provider;

        private boolean reversed;

        private T next;

        public AdaptableIterator(Node parent, String tagName,
                INodeAdaptableProvider provider, boolean reversed) {
            this.tagName = tagName;
            this.provider = provider;
            this.reversed = reversed;
            this.node = reversed ? parent.getLastChild()
                    : parent.getFirstChild();
            this.next = findNext();
        }

        @SuppressWarnings("unchecked")
        private T findNext() {
            while (node != null) {
                IAdaptable obj;
                if (isElementByTag(node, tagName)) {
                    obj = provider.getAdaptable(node);
                } else {
                    obj = null;
                }
                node = reversed ? node.getPreviousSibling()
                        : node.getNextSibling();
                if (obj != null)
                    return (T) obj;
            }
            return null;
        }

        public boolean hasNext() {
            return next != null;
        }

        public T next() {
            T n = next;
            next = findNext();
            return n;
        }

        public void remove() {
        }

    }

    private static class DelegateSet<T> extends AbstractSet<T> {

        private Collection<T> c;

        public DelegateSet(Collection<T> c) {
            this.c = c;
        }

        public Iterator<T> iterator() {
            return c.iterator();
        }

        public int size() {
            return c.size();
        }

    }

    private static final ErrorHandler NULL_ERROR_HANDLER = new ErrorHandler() {

        public void warning(SAXParseException exception) throws SAXException {
        }

        public void fatalError(SAXParseException exception)
                throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
        }

    };

//    private static Transformer transformer = null;
//    private static DocumentBuilder documentBuilder = null;

    private DOMUtils() {
    }

    public static Transformer getDefaultTransformer() throws CoreException {
        try {
            return TransformerFactory.newInstance().newTransformer();
        } catch (TransformerException e) {
            throw new CoreException(Core.ERROR_FAIL_ACCESS_XML_TRANSFORMER, e);
        }
    }

    public static DocumentBuilder getDefaultDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(
                    "http://apache.org/xml/features/continue-after-fatal-error", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", //$NON-NLS-1$
                    true);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities", //$NON-NLS-1$
                    false);
        } catch (Exception e) {
            // ignore
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(NULL_ERROR_HANDLER);
        return documentBuilder;
    }

    public static String toString(Node node) {
        if (node == null)
            return "null"; //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(node.getNodeName());
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                sb.append(' ');
                Node item = attributes.item(i);
                sb.append(item.getNodeName());
                sb.append('=');
                sb.append('"');
                sb.append(item.getNodeValue());
                sb.append('"');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public static Document doCreateDocument()
            throws ParserConfigurationException {
        return getDefaultDocumentBuilder().newDocument();
    }

    /**
     * @return
     */
    public static Document createDocument() {
        try {
            return getDefaultDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    /**
     * @param docTag
     *            =manifest
     * @return
     */
    public static Document createDocument(String docTag) {
        Document ret = createDocument();
        createElement(ret, docTag);
        return ret;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     */
    public static Document loadDocument(InputStream is) throws IOException {
        if (is == null)
            throw new IllegalArgumentException();
        try {
            return getDefaultDocumentBuilder().parse(is);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static Document loadDocument(byte[] bytes) throws IOException {
        if (bytes == null)
            throw new IllegalArgumentException();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            return loadDocument(in);
        } finally {
            in.close();
        }
    }

    public static void save(Node dom, OutputStream out, boolean closeOnFinish)
            throws IOException, CoreException {
        save(getDefaultTransformer(), dom, out, closeOnFinish);
    }

    //manifest, zob, false
    public static void save(IAdaptable adaptable, OutputStream out,
            boolean closeOnFinish) throws IOException, CoreException {
        save(getDefaultTransformer(), adaptable, out, closeOnFinish);
    }

    public static void save(Transformer t, IAdaptable adaptable,
            OutputStream out, boolean closeOnFinish) throws IOException {
        Node dom = (Node) adaptable.getAdapter(Node.class);

        if (dom != null) {
            save(t, dom, out, closeOnFinish);
        }
    }

    public static void save(Transformer t, Node dom, OutputStream out,
            boolean closeOnFinish) throws IOException {
        try {
            t.transform(new DOMSource(dom), new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (closeOnFinish) {
                out.close();
            }
        }
    }

    /**
     * @param parent
     * @param tag
     *            =manifest
     * @return
     */
    public static Element createElement(Node parent, String tag) {
        Document doc = parent.getNodeType() == Node.DOCUMENT_NODE
                ? (Document) parent : parent.getOwnerDocument();
        Element e = doc.createElement(tag);
        parent.appendChild(e);
        return e;
    }

    public static String getPrefix(String qualifiedName) {
        int index = qualifiedName.indexOf(':');
        if (index >= 0)
            return qualifiedName.substring(0, index);
        return null;
    }

    public static String getLocalName(String qualifiedName) {
        int index = qualifiedName.indexOf(':');
        if (index >= 0)
            return qualifiedName.substring(index + 1);
        return qualifiedName;
    }

    public static String getQualifiedName(String prefix, String localName) {
        return prefix + ":" + localName; //$NON-NLS-1$
    }

    /**
     * @param parent
     * @param tag
     * @param text
     * @return
     */
    public static Element createText(Node parent, String tag, String text) {
        Element e = createElement(parent, tag);
        Node t = parent.getOwnerDocument().createTextNode(text);
        e.appendChild(t);
        return e;
    }

    /**
     * @param parent
     * @param name
     * @param value
     * @return
     */
    public static Attr createAttr(Element parent, String name, Object value) {
        if (value == null)
            return null;
        Attr a = parent.getOwnerDocument().createAttribute(name);
        a.setNodeValue(value.toString());
        parent.getAttributes().setNamedItem(a);
        return a;
    }

    /**
     * @param element
     * @param attrName
     * @param value
     */
    public static void setAttribute(Element element, String attrName,
            Object value) {
        if (value != null) {
            element.setAttribute(attrName, value.toString());
        } else if (element.hasAttribute(attrName)) {
            element.removeAttribute(attrName);
        }
    }

    public static String getAttribute(Element element, String attrName) {
        if (!element.hasAttribute(attrName)) {
            String localName = getLocalName(attrName);
            if (!attrName.equals(localName))
                return getAttribute(element, localName);
            return null;
        }
        return element.getAttribute(attrName);
    }

    public static boolean isElementByTag(Node node, String tagName) {
        if (!(node instanceof Element))
            return false;
        if (tagName == null)
            return true;
        Element element = (Element) node;
        String tag = element.getTagName();
        return tag.equals(tagName)
                || getLocalName(tag).equals(getLocalName(tagName));
    }

    public static Iterator<Element> childElementIter(Node parent) {
        return new ElementIterator(parent);
    }

    public static Iterator<Element> childElementIterByTag(Node parent,
            String tagName) {
        return new ElementIterator(parent, tagName);
    }

    public static boolean hasChildElement(Node parent) {
        return childElementIter(parent).hasNext();
    }

    public static boolean hasChildElementByTag(Node parent, String tagName) {
        return childElementIterByTag(parent, tagName).hasNext();
    }

    public static int getElementIndex(Node parent, String tagName,
            Element child) {
        Iterator<Element> it = childElementIterByTag(parent, tagName);
        for (int i = 0; it.hasNext(); i++) {
            if (it.next() == child)
                return i;
        }
        return -1;
    }

    public static int getNodeIndex(Node parent, Node child) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (child == children.item(i))
                return i;
        }
        return -1;
    }

    public static Element getFirstChildElement(Node parent) {
        return childElementIter(parent).next();
    }

    public static Element getFirstChildElementByTag(Node parent, String tag) {
        return childElementIterByTag(parent, tag).next();
    }

    public static int getNumChildElementsByTag(Node parent, String tag) {
        int num = 0;
        Iterator<Element> it = childElementIterByTag(parent, tag);
        while (it.hasNext()) {
            it.next();
            num++;
        }
        return num;
    }

    /**
     * @param parent
     * @param tag
     * @return
     */
    public static Element[] getChildElementsByTag(Node parent, String tag) {
        List<Element> list = new ArrayList<Element>(
                parent.getChildNodes().getLength());
        Iterator<Element> it = childElementIterByTag(parent, tag);
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list.toArray(new Element[list.size()]);
    }

    public static Element[] getChildElements(Node parent) {
        List<Element> list = new ArrayList<Element>(
                parent.getChildNodes().getLength());
        Iterator<Element> it = childElementIter(parent);
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list.toArray(new Element[list.size()]);
    }

    public static Element ensureChildElement(Node parent, String tagName) {
        Element ele;

        if (parent.getNodeType() == Node.DOCUMENT_NODE) {
            ele = ((Document) parent).getDocumentElement();
        } else {
            ele = getFirstChildElementByTag(parent, tagName);
        }
        if (ele == null) {
            ele = createElement(parent, tagName);
        }
        return ele;
    }

    public static void createCentalTopicElement(Node parent, ITopic topic) {
        if (topic == null)
            createElement(parent, TAG_TOPIC);
        else
            createElement(parent, topic.getTitleText());
    }

    public static <T extends IAdaptable> List<T> getChildList(Element element,
            String childTag, INodeAdaptableProvider finder) {
        List<T> list = getChildren(element, childTag, finder);
        return list;
    }

    public static <T extends IAdaptable> Set<T> getChildSet(Element element,
            String childTag, INodeAdaptableProvider finder) {
        List<T> list = getChildren(element, childTag, finder);
        return unmodifiableSet(list);
    }

    public static <T extends IAdaptable> Iterator<T> getChildIterator(
            Element element, String childTag,
            final INodeAdaptableProvider finder, final Class<T> childClass) {
        final Iterator<Element> it = childElementIterByTag(element, childTag);
        return new Iterator<T>() {

            T next = findNext();

            private T findNext() {
                while (it.hasNext()) {
                    Element e = it.next();
                    IAdaptable a = finder.getAdaptable(e);
                    if (a != null && childClass.isInstance(a)) {
                        return childClass.cast(a);
                    }
                }
                return null;
            }

            public boolean hasNext() {
                return next != null;
            }

            public T next() {
                T n = next;
                next = findNext();
                return n;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> Iterator<T> emptyIter() {
        return new Iterator<T>() {

            public boolean hasNext() {
                return false;
            }

            public T next() {
                return null;
            }

            public void remove() {
            }

        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAdaptable> List<T> getChildren(Element element,
            String childTag, INodeAdaptableProvider finder) {
        ArrayList<T> list = new ArrayList<T>(
                element.getChildNodes().getLength());
        Iterator<Element> it = childElementIterByTag(element, childTag);
        while (it.hasNext()) {
            Element child = it.next();
            IAdaptable a = finder.getAdaptable(child);
            if (a != null) {
                list.add((T) a);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAdaptable> List<T> getChildren(Node parent,
            INodeAdaptableProvider finder) {

        NodeList childNodes = parent.getChildNodes();
        int num = childNodes.getLength();
        ArrayList<T> list = new ArrayList<T>(num);
        for (int i = 0; i < num; i++) {
            Node n = childNodes.item(i);
            IAdaptable a = finder.getAdaptable(n);
            if (a != null) {
                list.add((T) a);
            }
        }
        return list;
    }

    public static <T> Set<T> unmodifiableSet(Collection<T> c) {
        return new DelegateSet<T>(c);
    }

    /**
     * @param parent
     * @param tag
     * @return
     */
    public static String getTextContentByTag(Node parent, String tag) {
        Element ele = getFirstChildElementByTag(parent, tag);
        if (ele == null)
            return null;
        Node firstChild = ele.getFirstChild();
        return firstChild == null ? null : firstChild.getTextContent();
    }

    /**
     * @param titleNode
     * @param textContent
     */
    public static void setText(Node titleNode, String textContent) {
        Node textNode = findTextNode(titleNode);
        if (textNode != null) {
            if (textContent == null) {
                titleNode.removeChild(textNode);
            } else {
                textNode.setTextContent(textContent);
            }
        } else {
            titleNode.setTextContent(textContent);
        }
    }

    public static Node findTextNode(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node c = children.item(i);
            if (c.getNodeType() == Node.TEXT_NODE)
                return c;
        }
        return null;
    }

    /**
     * @param parent
     * @param tag
     * @param text
     */
    public static void setText(Node parent, String tag, String text) {
        Element titleElement = getFirstChildElementByTag(parent, tag);
        if (titleElement == null) {
            if (text != null)
                createText(parent, tag, text);
        } else {
            setText(titleElement, text);
            if (!titleElement.hasChildNodes()
                    && !titleElement.hasAttributes()) {
                parent.removeChild(titleElement);
            }
        }
    }

    /**
     * @param element
     * @return
     */
    public static Element addIdAttribute(Element element) {
        if (!element.hasAttribute(ATTR_ID)) {
            element.setAttribute(ATTR_ID, Core.getIdFactory().createId());
            element.setIdAttribute(ATTR_ID, true);
        }
        return element;
    }

    public static String replaceId(Element element) {
        String newId = Core.getIdFactory().createId();
        replaceId(element, newId);
        return newId;
    }

    public static Element replaceId(Element element, String newId) {
        if (newId == null)
            return element;
        element.setAttribute(ATTR_ID, newId);
        element.setIdAttribute(ATTR_ID, true);
        return element;
    }

    public static boolean isOrphanNode(Node node) {
        if (node == null)
            return true;
        if (node.getNodeType() == Node.DOCUMENT_NODE)
            return false;
        return isOrphanNode(node.getParentNode());
    }

    public static Document getOwnerDocument(Node node) {
        return node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node
                : node.getOwnerDocument();
    }
}