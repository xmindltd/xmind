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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_BRANCH;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_HREF;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MARKER_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_PROVIDER;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STRUCTURE_CLASS;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_WIDTH;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_X;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_Y;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARIES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_CHILDREN;
import static org.xmind.core.internal.dom.DOMConstants.TAG_EXTENSION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_EXTENSIONS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_LABEL;
import static org.xmind.core.internal.dom.DOMConstants.TAG_LABELS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REF;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER_REFS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_POSITION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUMMARIES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUMMARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TITLE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPICS;
import static org.xmind.core.internal.dom.DOMConstants.VAL_FOLDED;
import static org.xmind.core.internal.dom.NumberUtils.safeParseInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IImage;
import org.xmind.core.INotes;
import org.xmind.core.INumbering;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicPath;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Topic;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.ILabelRefCounter;
import org.xmind.core.util.IMarkerRefCounter;
import org.xmind.core.util.Point;

/**
 * @author briansun
 */
public class TopicImpl extends Topic implements ICoreEventSource {

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    private NotesImpl notes;

    private ImageImpl image;

    private NumberingImpl numbering;

    private Map<String, TopicExtensionImpl> extensions = new HashMap<String, TopicExtensionImpl>();

//    private boolean updatingTimestamp = false;

    /**
     * @param implementation
     */
    public TopicImpl(Element implementation, WorkbookImpl ownedWorkbook) {
        super();
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedWorkbook = ownedWorkbook;
//        installTimestampUpdater(implementation);
    }

    /**
     * @return the implementation
     */
    public Element getImplementation() {
        return implementation;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof TopicImpl))
            return false;
        TopicImpl that = (TopicImpl) obj;
        return this.implementation == that.implementation;
    }

    @Override
    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "TPC#" + getId() + "(" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.core.ITopic#getId()
     */
    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    /**
     * @see org.xmind.core.internal.Topic#getLocalTitleText()
     */
    @Override
    protected String getLocalTitleText() {
        return DOMUtils.getTextContentByTag(implementation, TAG_TITLE);
    }

    /**
     * @see org.xmind.core.ITitled#setTitleText(java.lang.String)
     */
    public void setTitleText(String titleText) {
        String oldValue = getLocalTitleText();
        DOMUtils.setText(implementation, TAG_TITLE, titleText);
        String newValue = getLocalTitleText();
        fireValueChange(Core.TitleText, oldValue, newValue);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.ITopic#isFolded()
     */
    public boolean isFolded() {
        String value = DOMUtils.getAttribute(implementation, ATTR_BRANCH);
        return value != null && value.contains(VAL_FOLDED);
    }

    /**
     * @see org.xmind.core.ITopic#setFolded(boolean)
     */
    public void setFolded(boolean folded) {
        Boolean oldValue = isFolded();
        String value = folded ? VAL_FOLDED : null;
        DOMUtils.setAttribute(implementation, ATTR_BRANCH, value);
        Boolean newValue = isFolded();
        fireValueChange(Core.TopicFolded, oldValue, newValue);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.style.IStyled#getStyleId()
     */
    public String getStyleId() {
        return DOMUtils.getAttribute(implementation, ATTR_STYLE_ID);
    }

    /**
     * @see org.xmind.core.style.IStyled#setStyleId(java.lang.String)
     */
    public void setStyleId(String styleId) {
        String oldValue = getStyleId();
        WorkbookImpl workbook = getRealizedWorkbook();
        decreaseStyleRef(workbook);
        DOMUtils.setAttribute(implementation, ATTR_STYLE_ID, styleId);
        increaseStyleRef(workbook);
        String newValue = getStyleId();
        fireValueChange(Core.Style, oldValue, newValue);
        updateModificationInfo();
    }

    public boolean hasPosition() {
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e == null)
            return false;
        return e.hasAttribute(ATTR_X) && e.hasAttribute(ATTR_Y);
    }

    /**
     * @see org.xmind.core.ITopic#getPosition()
     */
    public Point getPosition() {
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e == null)
            return null;
        String x = DOMUtils.getAttribute(e, ATTR_X);
        String y = DOMUtils.getAttribute(e, ATTR_Y);
        if (x == null && y == null)
            return null;
        return new Point(safeParseInt(x, 0), safeParseInt(y, 0));
    }

    /**
     * @see org.xmind.core.ITopic#setPosition(int, int)
     */
    public void setPosition(int x, int y) {
        Point oldValue = getPosition();
        Element e = DOMUtils.ensureChildElement(implementation, TAG_POSITION);
        DOMUtils.setAttribute(e, ATTR_X, Integer.toString(x));
        DOMUtils.setAttribute(e, ATTR_Y, Integer.toString(y));
        Point newValue = getPosition();
        fireValueChange(Core.Position, oldValue, newValue);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.internal.Topic#removePosition()
     */
    @Override
    protected void removePosition() {
        Point oldValue = getPosition();
        Element e = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_POSITION);
        if (e != null)
            implementation.removeChild(e);
        Point newValue = getPosition();
        fireValueChange(Core.Position, oldValue, newValue);
        updateModificationInfo();
    }

    public String getType() {
        if (isRoot())
            return ITopic.ROOT;
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_TOPICS)) {
            return getType((Element) p);
        }
        return null;
    }

    public List<ITopic> getChildren(String type) {
        if (type == null)
            return NO_CHILDREN;
        Element ts = findSubtopicsElement(implementation, type);
        if (ts != null)
            return getChildren(ts);
        return NO_CHILDREN;
    }

    public boolean hasChildren(String type) {
        if (type == null)
            return false;
        Element ts = findSubtopicsElement(implementation, type);
        if (ts != null)
            return DOMUtils.hasChildElementByTag(ts, TAG_TOPIC);
        return false;
    }

    public Iterator<ITopic> getChildrenIterator(String type) {
        if (type == null)
            return NO_CHILDREN.iterator();
        Element ts = findSubtopicsElement(implementation, type);
        if (ts != null) {
            return DOMUtils.getChildIterator(ts, TAG_TOPIC,
                    ownedWorkbook.getAdaptableRegistry(), ITopic.class);
        }
        return NO_CHILDREN.iterator();
    }

    public Iterator<ITopic> getAllChildrenIterator() {
        Element c = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_CHILDREN);
        if (c == null)
            return NO_CHILDREN.iterator();

        final Iterator<Element> tsIter = DOMUtils.childElementIterByTag(c,
                TAG_TOPICS);
        return new Iterator<ITopic>() {

            Iterator<ITopic> childTopicIter = null;

            ITopic next = findNextChildTopic();

            private ITopic findNextChildTopic() {
                if (childTopicIter == null || !childTopicIter.hasNext()) {
                    childTopicIter = nextChildTopicIter();
                }
                if (childTopicIter == null)
                    return null;

                return childTopicIter.next();
            }

            private Iterator<ITopic> nextChildTopicIter() {
                while (tsIter.hasNext()) {
                    Element ts = tsIter.next();
                    if (ts != null) {
                        return DOMUtils.getChildIterator(ts, TAG_TOPIC,
                                ownedWorkbook.getAdaptableRegistry(),
                                ITopic.class);
                    }
                }
                return null;

            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public ITopic next() {
                ITopic n = next;
                next = findNextChildTopic();
                return n;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

    protected static Element findSubtopicsElement(Element topicElement,
            String type) {
        Element c = DOMUtils.getFirstChildElementByTag(topicElement,
                TAG_CHILDREN);
        if (c != null) {
            return findSubtopicsElementFromChildren(c, type);
        }
        return null;
    }

    private static Element findSubtopicsElementFromChildren(Element c,
            String type) {
        Iterator<Element> it = DOMUtils.childElementIterByTag(c, TAG_TOPICS);
        while (it.hasNext()) {
            Element ts = it.next();
            if (type.equals(getType(ts)))
                return ts;
        }
        return null;
    }

    protected static String getType(Element topicsImpl) {
        return DOMUtils.getAttribute(topicsImpl, ATTR_TYPE);
    }

    private List<ITopic> getChildren(Element ts) {
        return DOMUtils.getChildList(ts, TAG_TOPIC,
                ownedWorkbook.getAdaptableRegistry());
    }

    public Set<String> getChildrenTypes() {
        Element c = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_CHILDREN);
        if (c == null)
            return NO_TYPES;

        List<String> list = new ArrayList<String>(
                c.getChildNodes().getLength());
        Iterator<Element> it = DOMUtils.childElementIterByTag(c, TAG_TOPICS);
        while (it.hasNext()) {
            Element ts = it.next();
            String type = getType(ts);
            if (type != null && !list.contains(type)) {
                list.add(type);
            }
        }
        return DOMUtils.unmodifiableSet(list);
    }

    public void add(ITopic child, int index, String type) {
        Element t = ((TopicImpl) child).getImplementation();
        Element c = DOMUtils.ensureChildElement(implementation, TAG_CHILDREN);
        Element ts = findSubtopicsElementFromChildren(c, type);
        if (ts == null) {
            ts = DOMUtils.createElement(c, TAG_TOPICS);
            DOMUtils.setAttribute(ts, ATTR_TYPE, type);
        }
        Node n = null;
        Element[] es = DOMUtils.getChildElementsByTag(ts, TAG_TOPIC);
        if (index >= 0 && index < es.length) {
            n = ts.insertBefore(t, es[index]);
        } else {
            n = ts.appendChild(t);
        }
        if (n != null) {
            if (!isOrphan()) {
                ((TopicImpl) child).addNotify(getRealizedWorkbook(),
                        getRealizedSheet(), this);
            }
            fireIndexedTargetChange(Core.TopicAdd, child, child.getIndex(),
                    type);
            updateModificationInfo();
        }
    }

    public void add(ITopic child, String type) {
        add(child, -1, type);
    }

    public void add(ITopic child) {
        add(child, -1, ITopic.ATTACHED);
    }

    public void remove(ITopic child) {
        Element t = ((TopicImpl) child).getImplementation();
        Node p = t.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_TOPICS)) {
            Element ts = (Element) p;
            p = p.getParentNode();
            if (DOMUtils.isElementByTag(p, TAG_CHILDREN)) {
                Element c = (Element) p;
                p = p.getParentNode();
                if (p == implementation) {
                    int index = child.getIndex();
                    String type = DOMUtils.getAttribute(ts, ATTR_TYPE);
                    if (!isOrphan()) {
                        ((TopicImpl) child).removeNotify(getRealizedWorkbook(),
                                getRealizedSheet(), null);
                    }
                    Node n = ts.removeChild(t);
                    if (!ts.hasChildNodes()) {
                        c.removeChild(ts);
                        if (!c.hasChildNodes()) {
                            implementation.removeChild(c);
                        }
                    }
                    if (n != null) {
                        fireIndexedTargetChange(Core.TopicRemove, child, index,
                                type);
                        updateModificationInfo();
                    }
                }
            }
        }
    }

    /**
     * @see org.xmind.core.ITopic#getParent()
     */
    public ITopic getParent() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_TOPICS)) {
            p = p.getParentNode();
            if (DOMUtils.isElementByTag(p, TAG_CHILDREN)) {
                p = p.getParentNode();
                if (DOMUtils.isElementByTag(p, TAG_TOPIC)) {
                    return (ITopic) ownedWorkbook.getAdaptableRegistry()
                            .getAdaptable(p);
                }
            }
        }
        return null;
    }

    /**
     * @see org.xmind.core.ITopic#isRoot()
     */
    public boolean isRoot() {
        Node p = implementation.getParentNode();
        return DOMUtils.isElementByTag(p, TAG_SHEET);
    }

    /**
     * @see org.xmind.core.internal.Topic#getOwnedSheet()
     */
    @Override
    public ISheet getOwnedSheet() {
        Node s = implementation.getParentNode();
        if (DOMUtils.isElementByTag(s, TAG_SHEET)) {
            return (ISheet) ownedWorkbook.getAdaptableRegistry()
                    .getAdaptable(s);
        }
        return super.getOwnedSheet();
    }

    /**
     * @see org.xmind.core.internal.Topic#getOwnedWorkbook()
     */
    @Override
    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    /**
     * @see org.xmind.core.ITopic#getPath()
     */
    public ITopicPath getPath() {
        return new TopicPathImpl(this);
    }

    /**
     * @see org.xmind.core.ITopic#getIndex()
     */
    public int getIndex() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_TOPICS)) {
            return DOMUtils.getElementIndex(p, TAG_TOPIC, implementation);
        }
        return -1;
    }

    /**
     * @see org.xmind.core.ITopic#getHyperlink()
     */
    public String getHyperlink() {
        return DOMUtils.getAttribute(implementation, ATTR_HREF);
    }

    /**
     * @see org.xmind.core.ITopic#setHyperlink(java.lang.String)
     */
    public void setHyperlink(String hyperlink) {
        String oldValue = getHyperlink();
        WorkbookImpl workbook = getRealizedWorkbook();
        InternalHyperlinkUtils.deactivateHyperlink(workbook, oldValue, this);
        DOMUtils.setAttribute(implementation, ATTR_HREF, hyperlink);
        InternalHyperlinkUtils.activateHyperlink(workbook, getHyperlink(),
                this);
        String newValue = getHyperlink();
        fireValueChange(Core.TopicHyperlink, oldValue, newValue);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.ITopic#getNotes()
     */
    public INotes getNotes() {
        if (notes == null) {
            notes = new NotesImpl(implementation, this);
        }
        return notes;
    }

    public INumbering getNumbering() {
        if (numbering == null) {
            numbering = new NumberingImpl(implementation, this);
        }
        return numbering;
    }

    public void addMarker(String markerId) {
        if (markerId == null)
            return;

        Element ms = DOMUtils.ensureChildElement(implementation,
                TAG_MARKER_REFS);
        Element m = getMarkerRefElement(markerId, ms);
        if (m != null)
            return;

        m = DOMUtils.createElement(ms, TAG_MARKER_REF);
        m.setAttribute(ATTR_MARKER_ID, markerId);
        WorkbookImpl workbook = getRealizedWorkbook();
        if (workbook != null) {
            workbook.getMarkerRefCounter().increaseRef(markerId);
        }
        fireTargetChange(Core.MarkerRefAdd, markerId);
        SheetImpl sheet = getRealizedSheet();
        if (sheet != null) {
            sheet.getMarkerRefCounter().increaseRef(markerId);
        }
        updateModificationInfo();
    }

    public void removeMarker(String markerId) {
        if (markerId == null)
            return;

        Element ms = getMarkerRefsElement();
        if (ms == null)
            return;

        Element m = getMarkerRefElement(markerId, ms);
        if (m == null)
            return;

        Node n = ms.removeChild(m);
        if (!ms.hasChildNodes()) {
            implementation.removeChild(ms);
        }
        if (n != null) {
            WorkbookImpl workbook = getRealizedWorkbook();
            if (workbook != null) {
                workbook.getMarkerRefCounter().decreaseRef(markerId);
            }
            fireTargetChange(Core.MarkerRefRemove, markerId);
            SheetImpl sheet = getRealizedSheet();
            if (sheet != null) {
                sheet.getMarkerRefCounter().decreaseRef(markerId);
            }
            updateModificationInfo();
        }
    }

    public boolean hasMarker(String markerId) {
        if (markerId == null)
            return false;

        return getMarkerRefElement(markerId) != null;
    }

    public IMarkerRef getMarkerRef(String markerId) {
        if (markerId == null)
            return null;

        Element m = getMarkerRefElement(markerId);
        if (m != null)
            return (IMarkerRef) ownedWorkbook.getAdaptableRegistry()
                    .getAdaptable(m);
        return null;
    }

    public Set<IMarkerRef> getMarkerRefs() {
        Element ms = getMarkerRefsElement();
        if (ms == null)
            return NO_MARKER_REFS;
        return DOMUtils.getChildSet(ms, TAG_MARKER_REF,
                ownedWorkbook.getAdaptableRegistry());
    }

    private Element getMarkerRefsElement() {
        return DOMUtils.getFirstChildElementByTag(implementation,
                TAG_MARKER_REFS);
    }

    private Element getMarkerRefElement(String markerId) {
        Element ms = getMarkerRefsElement();
        if (ms == null)
            return null;
        return getMarkerRefElement(markerId, ms);
    }

    private Element getMarkerRefElement(String markerId, Element ms) {
        Iterator<Element> it = DOMUtils.childElementIterByTag(ms,
                TAG_MARKER_REF);
        while (it.hasNext()) {
            Element m = it.next();
            if (markerId.equals(m.getAttribute(ATTR_MARKER_ID)))
                return m;
        }
        return null;
    }

    public void addBoundary(IBoundary boundary) {
        Element b = ((BoundaryImpl) boundary).getImplementation();
        Element bs = DOMUtils.ensureChildElement(implementation,
                TAG_BOUNDARIES);
        Node n = bs.appendChild(b);
        if (n != null) {
            if (!isOrphan()) {
                ((BoundaryImpl) boundary).addNotify(getRealizedWorkbook(),
                        this);
            }
            fireTargetChange(Core.BoundaryAdd, boundary);
            updateModificationInfo();
        }
    }

    public void removeBoundary(IBoundary boundary) {
        Element bs = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_BOUNDARIES);
        if (bs == null)
            return;

        Element b = ((BoundaryImpl) boundary).getImplementation();
        if (b.getParentNode() == bs) {
            ((BoundaryImpl) boundary).removeNotify(getRealizedWorkbook(), this);
            Node n = bs.removeChild(b);
            if (!bs.hasChildNodes())
                implementation.removeChild(bs);
            if (n != null) {
                fireTargetChange(Core.BoundaryRemove, boundary);
                updateModificationInfo();
            }
        }
    }

    public Set<IBoundary> getBoundaries() {
        Element bs = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_BOUNDARIES);
        if (bs == null)
            return NO_BOUNDARIES;
        return DOMUtils.getChildSet(bs, TAG_BOUNDARY,
                ownedWorkbook.getAdaptableRegistry());
    }

    public void addSummary(ISummary summary) {
        Element s = ((SummaryImpl) summary).getImplementation();
        Element ss = DOMUtils.ensureChildElement(implementation, TAG_SUMMARIES);
        Node n = ss.appendChild(s);
        if (n != null) {
            if (!isOrphan()) {
                ((SummaryImpl) summary).addNotify(getRealizedWorkbook(), this);
            }
            fireTargetChange(Core.SummaryAdd, summary);
            updateModificationInfo();
        }
    }

    public void removeSummary(ISummary summary) {
        Element ss = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_SUMMARIES);
        if (ss == null)
            return;

        Element s = ((SummaryImpl) summary).getImplementation();
        if (s.getParentNode() == ss) {
            ((SummaryImpl) summary).removeNotify(getRealizedWorkbook(), this);
            Node n = ss.removeChild(s);
            if (!ss.hasChildNodes())
                implementation.removeChild(ss);
            if (n != null) {
                fireTargetChange(Core.SummaryRemove, summary);
                updateModificationInfo();
            }
        }
    }

    public Set<ISummary> getSummaries() {
        Element ss = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_SUMMARIES);
        if (ss == null)
            return NO_SUMMARIES;
        return DOMUtils.getChildSet(ss, TAG_SUMMARY,
                ownedWorkbook.getAdaptableRegistry());
    }

    public String getStructureClass() {
        return DOMUtils.getAttribute(implementation, ATTR_STRUCTURE_CLASS);
    }

    public void setStructureClass(String structureClass) {
        String oldValue = getStructureClass();
        DOMUtils.setAttribute(implementation, ATTR_STRUCTURE_CLASS,
                structureClass);
        String newValue = getStructureClass();
        fireValueChange(Core.StructureClass, oldValue, newValue);
        updateModificationInfo();
    }

    public Set<String> getLabels() {
        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls != null) {
            NodeList c = ls.getChildNodes();
            int num = c.getLength();
            if (num > 0) {
                List<String> set = new ArrayList<String>(num);
                for (int i = 0; i < num; i++) {
                    Node n = c.item(i);
                    if (DOMUtils.isElementByTag(n, TAG_LABEL)) {
                        set.add(n.getTextContent());
                    }
                }
                return DOMUtils.unmodifiableSet(set);
            }
        }
        return NO_LABELS;
    }

    private Element findLabelElement(String label, Element ls) {
        NodeList c = ls.getChildNodes();
        for (int i = 0; i < c.getLength(); i++) {
            Node n = c.item(i);
            if (DOMUtils.isElementByTag(n, TAG_LABEL)) {
                String text = n.getTextContent();
                if (label.equals(text))
                    return (Element) n;
            }
        }
        return null;
    }

    public void addLabel(String label) {
        if (label == null)
            return;

        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls != null) {
            Element existing = findLabelElement(label, ls);
            if (existing != null)
                return;
        }

        Set<String> oldValue = getLabels();
        addLabelElement(ls, label);
        Set<String> newValue = getLabels();
        fireValueChange(Core.Labels, oldValue, newValue);
        SheetImpl sheet = getRealizedSheet();
        if (sheet != null) {
            sheet.getLabelRefCounter().increaseRef(label);
        }
        updateModificationInfo();
    }

    public void removeLabel(String label) {
        if (label == null)
            return;

        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls == null)
            return;

        Element l = findLabelElement(label, ls);
        if (l == null)
            return;

        Set<String> oldValue = getLabels();
        Node n = ls.removeChild(l);
        if (n != null) {
            if (!ls.hasChildNodes()) {
                implementation.removeChild(ls);
            }
            Set<String> newValue = getLabels();
            fireValueChange(Core.Labels, oldValue, newValue);
            SheetImpl sheet = getRealizedSheet();
            if (sheet != null) {
                sheet.getLabelRefCounter().decreaseRef(label);
            }
            updateModificationInfo();
        }
    }

    public void setLabels(Collection<String> labels) {
        Set<String> oldValue = getLabels();
        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls != null) {
            implementation.removeChild(ls);
            ls = null;
        }
        if (labels != null && !labels.isEmpty()) {
            for (String label : labels) {
                if (label != null) {
                    ls = addLabelElement(ls, label);
                }
            }
        }
        Set<String> newValue = getLabels();
        fireValueChange(Core.Labels, oldValue, newValue);
        SheetImpl sheet = getRealizedSheet();
        if (sheet != null) {
            ILabelRefCounter counter = sheet.getLabelRefCounter();
            List<String> added = new ArrayList<String>(newValue);
            added.removeAll(oldValue);
            List<String> removed = new ArrayList<String>(oldValue);
            removed.removeAll(newValue);
            for (String increased : added) {
                counter.increaseRef(increased);
            }
            for (String decreased : removed) {
                counter.decreaseRef(decreased);
            }
        }
        updateModificationInfo();
    }

    public void removeAllLabels() {
        setLabels(NO_LABELS);
    }

    private Element addLabelElement(Element ls, String label) {
        if (ls == null)
            ls = DOMUtils.createElement(implementation, TAG_LABELS);
        Element l = DOMUtils.createElement(ls, TAG_LABEL);
        l.setTextContent(label);
        return ls;
    }

    public IImage getImage() {
        if (image == null) {
            image = new ImageImpl(implementation, this);
        }
        return image;
    }

    public int getTitleWidth() {
        Element t = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_TITLE);
        return t == null ? UNSPECIFIED
                : NumberUtils.safeParseInt(DOMUtils.getAttribute(t, ATTR_WIDTH),
                        UNSPECIFIED);
    }

    public void setTitleWidth(int width) {
        Integer oldValue = getTitleWidthValue();
        if (width == UNSPECIFIED) {
            Element t = DOMUtils.getFirstChildElementByTag(implementation,
                    TAG_TITLE);
            if (t != null) {
                t.removeAttribute(ATTR_WIDTH);
                if (!t.hasChildNodes() && !t.hasAttributes()) {
                    implementation.removeChild(t);
                }
            }
        } else {
            Element t = DOMUtils.ensureChildElement(implementation, TAG_TITLE);
            t.setAttribute(ATTR_WIDTH, String.valueOf(width));
        }
        Integer newValue = getTitleWidthValue();
        fireValueChange(Core.TitleWidth, oldValue, newValue);
        updateModificationInfo();
    }

    private Integer getTitleWidthValue() {
        int width = getTitleWidth();
        if (width != UNSPECIFIED)
            return Integer.valueOf(width);
        return null;
    }

    private Iterator<TopicExtensionImpl> iterExtensions() {
        return iterExtensions(!isOrphan());
    }

    private Iterator<TopicExtensionImpl> iterExtensions(
            final boolean realized) {
        Element es = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_EXTENSIONS);
        final Iterator<Element> it = es == null ? null
                : DOMUtils.childElementIterByTag(es, TAG_EXTENSION);
        return new Iterator<TopicExtensionImpl>() {

            TopicExtensionImpl next = findNext();

            public void remove() {
            }

            private TopicExtensionImpl findNext() {
                if (it == null)
                    return null;
                while (it.hasNext()) {
                    Element ele = it.next();
                    String providerName = ele.getAttribute(ATTR_PROVIDER);
                    if (providerName != null && !"".equals(providerName)) { //$NON-NLS-1$
                        TopicExtensionImpl ext = extensions.get(providerName);
                        if (ext == null) {
                            ext = new TopicExtensionImpl(ele, TopicImpl.this);
                            extensions.put(providerName, ext);
                            if (realized) {
                                ext.addNotify(ownedWorkbook);
                            }
                        }
                        return ext;
                    }
                }
                return null;
            }

            public TopicExtensionImpl next() {
                TopicExtensionImpl n = next;
                next = findNext();
                return n;
            }

            public boolean hasNext() {
                return next != null;
            }
        };
    }

//    private TopicExtensionImpl getExtension(String providerName, Element extImpl) {
//        TopicExtensionImpl ext = extensions.get(providerName);
//        if (ext == null) {
//            ext = new TopicExtensionImpl(extImpl, this);
//            extensions.put(providerName, ext);
//            ext.addNotify(getRealizedWorkbook());
//        }
//        return ext;
//    }

    public List<ITopicExtension> getExtensions() {
        List<ITopicExtension> extensions = new ArrayList<ITopicExtension>();
        Iterator<TopicExtensionImpl> it = iterExtensions();
        while (it.hasNext()) {
            extensions.add(it.next());
        }
        return extensions;
    }

    public ITopicExtension getExtension(String providerName) {
        Iterator<TopicExtensionImpl> it = iterExtensions();
        while (it.hasNext()) {
            TopicExtensionImpl ext = it.next();
            if (providerName.equals(ext.getProviderName())) {
                return ext;
            }
        }
        return null;
    }

    public ITopicExtension createExtension(String providerName) {
        ITopicExtension ext = getExtension(providerName);
        if (ext == null) {
            Element es = DOMUtils.ensureChildElement(implementation,
                    TAG_EXTENSIONS);
            Element e = DOMUtils.createElement(es, TAG_EXTENSION);
            e.setAttribute(ATTR_PROVIDER, providerName);
            ext = new TopicExtensionImpl(e, this);
            extensions.put(providerName, (TopicExtensionImpl) ext);
            if (!isOrphan()) {
                ((TopicExtensionImpl) ext).addNotify(ownedWorkbook);
            }
            updateModificationInfo();
        }
        return ext;
//        Element e = findExtensionElement(es, providerName);
//        if (e == null) {
//        }
//        return getExtension(providerName, e);
    }

    private Element findExtensionElement(Element es, String providerName) {
        Iterator<Element> it = DOMUtils.childElementIterByTag(es,
                TAG_EXTENSION);
        while (it.hasNext()) {
            Element e = it.next();
            if (providerName.equals(e.getAttribute(ATTR_PROVIDER))) {
                return e;
            }
        }
        return null;
    }

    public void deleteExtension(String providerName) {
        Element es = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_EXTENSIONS);
        if (es != null) {
            Element e = findExtensionElement(es, providerName);
            if (e != null) {
                es.removeChild(e);
                if (!es.hasChildNodes())
                    implementation.removeChild(es);
                updateModificationInfo();
            }
        }
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    protected void fireValueChange(String type, Object oldValue,
            Object newValue) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null) {
            coreEventSupport.dispatchValueChange(this, type, oldValue,
                    newValue);
        }
    }

    protected void fireIndexedTargetChange(String type, Object target,
            int index, Object data) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null) {
            CoreEvent event = new CoreEvent(this, type, target, index);
            event.setData(data);
            coreEventSupport.dispatch(this, event);
        }
    }

    protected void fireTargetChange(String type, Object target) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null) {
            coreEventSupport.dispatchTargetChange(this, type, target);
        }
    }

    public ICoreEventSupport getCoreEventSupport() {
        // Use workbook's core event support directly, so that 
        // orphan components can have events broadcasted, which 
        // will enable transient actions (such as dragging topics 
        // or adjusting relationship control points, etc.) to 
        // perform correctly.
        return ownedWorkbook.getCoreEventSupport();
    }

    protected WorkbookImpl getRealizedWorkbook() {
        if (getPath().getWorkbook() == ownedWorkbook)
            return ownedWorkbook;
        return null;
    }

    protected SheetImpl getRealizedSheet() {
        ISheet sheet = getOwnedSheet();
        if (sheet != null && sheet instanceof SheetImpl)
            return (SheetImpl) sheet;
        return null;
    }

    protected void addNotify(WorkbookImpl workbook, SheetImpl sheet,
            TopicImpl parent) {
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, true);
        workbook.getAdaptableRegistry().registerById(this, getId(),
                getImplementation().getOwnerDocument());
        increaseLabelRefs(sheet);
        increaseMarkerRefs(workbook, sheet);
        increaseStyleRef(workbook);
        activateHyperlinks(workbook);
        ((NotesImpl) getNotes()).addNotify(workbook);
        for (ITopic t : getAllChildren()) {
            ((TopicImpl) t).addNotify(workbook, sheet, this);
        }
        for (IBoundary b : getBoundaries()) {
            ((BoundaryImpl) b).addNotify(workbook, this);
        }
        for (ISummary s : getSummaries()) {
            ((SummaryImpl) s).addNotify(workbook, this);
        }
        extensionsAddNotify(workbook);

        boolean isRevising = (workbook.getAdaptableRegistry()
                .getAdaptableByNode(sheet.getImplementation()) != sheet);
        if (!isRevising) {
            ((CommentManagerImpl) workbook.getCommentManager())
                    .objectAddNotify(getId(), this);
        }
    }

    protected void removeNotify(WorkbookImpl workbook, SheetImpl sheet,
            TopicImpl parent) {
        boolean isRevising = (workbook.getAdaptableRegistry()
                .getAdaptableByNode(sheet.getImplementation()) != sheet);
        if (!isRevising) {
            ((CommentManagerImpl) workbook.getCommentManager())
                    .objectRemoveNotify(getId(), this);
        }

        extensionsRemoveNotify(workbook);
        for (ISummary s : getSummaries()) {
            ((SummaryImpl) s).removeNotify(workbook, this);
        }
        for (IBoundary b : getBoundaries()) {
            ((BoundaryImpl) b).removeNotify(workbook, this);
        }
        for (ITopic t : getAllChildren()) {
            ((TopicImpl) t).removeNotify(workbook, sheet, this);
        }
        ((NotesImpl) getNotes()).removeNotify(workbook);
        deactivateHyperlinks(workbook);
        decreaseStyleRef(workbook);
        decreaseMarkerRefs(workbook, sheet);
        decreaseLabelRefs(sheet);
        workbook.getAdaptableRegistry().unregisterById(this, getId(),
                getImplementation().getOwnerDocument());
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, false);
    }

    private void extensionsAddNotify(WorkbookImpl workbook) {
        Iterator<TopicExtensionImpl> it = iterExtensions(false);
        while (it.hasNext()) {
            it.next().addNotify(workbook);
        }
//        Element es = getFirstChildElementByTag(implementation, TAG_EXTENSIONS);
//        if (es != null) {
//            Iterator<Element> it = childElementIterByTag(es, TAG_EXTENSION);
//            while (it.hasNext()) {
//                Element e = it.next();
//                String providerName = getAttribute(e, ATTR_PROVIDER);
//                if (providerName != null) {
//                    TopicExtensionImpl ext = extensions.get(providerName);
//                    if (ext == null) {
//                        ext = new TopicExtensionImpl(e, this);
//                        extensions.put(providerName, ext);
//                    }
//                    ext.addNotify(workbook);
//                }
//            }
//        }
    }

    private void extensionsRemoveNotify(WorkbookImpl workbook) {
        Iterator<TopicExtensionImpl> it = iterExtensions(true);
        while (it.hasNext()) {
            it.next().removeNotify(workbook);
        }
//        Element es = DOMUtils.getFirstChildElementByTag(implementation,
//                TAG_EXTENSIONS);
//        if (es != null) {
//            Iterator<Element> it = DOMUtils.childElementIterByTag(es,
//                    TAG_EXTENSION);
//            while (it.hasNext()) {
//                Element e = it.next();
//                String providerName = DOMUtils.getAttribute(e, ATTR_PROVIDER);
//                if (providerName != null) {
//                    TopicExtensionImpl ext = getExtension(providerName, e);
//                    ext.removeNotify(workbook);
//                }
//            }
//        }
    }

    protected void increaseStyleRef(WorkbookImpl workbook) {
        if (workbook == null)
            return;
        String styleId = getStyleId();
        if (styleId != null) {
            workbook.getStyleRefCounter().increaseRef(styleId);
        }
    }

    protected void decreaseStyleRef(WorkbookImpl workbook) {
        if (workbook == null)
            return;
        String styleId = getStyleId();
        if (styleId != null) {
            workbook.getStyleRefCounter().decreaseRef(styleId);
        }
    }

    protected void increaseMarkerRefs(WorkbookImpl workbook, SheetImpl sheet) {
        IMarkerRefCounter counter = sheet == null ? null
                : sheet.getMarkerRefCounter();
        if (workbook == null && counter == null)
            return;

        Element mrs = getMarkerRefsElement();
        if (mrs == null)
            return;

        Iterator<Element> it = DOMUtils.childElementIterByTag(mrs,
                TAG_MARKER_REF);
        while (it.hasNext()) {
            String markerId = DOMUtils.getAttribute(it.next(), ATTR_MARKER_ID);
            if (markerId != null) {
                if (workbook != null)
                    workbook.getMarkerRefCounter().increaseRef(markerId);
                if (counter != null)
                    counter.increaseRef(markerId);
            }
        }
    }

    protected void decreaseMarkerRefs(WorkbookImpl workbook, SheetImpl sheet) {
        IMarkerRefCounter counter = sheet == null ? null
                : sheet.getMarkerRefCounter();
        if (workbook == null && counter == null)
            return;

        Element mrs = getMarkerRefsElement();
        if (mrs == null)
            return;

        Iterator<Element> it = DOMUtils.childElementIterByTag(mrs,
                TAG_MARKER_REF);
        while (it.hasNext()) {
            String markerId = DOMUtils.getAttribute(it.next(), ATTR_MARKER_ID);
            if (markerId != null) {
                if (workbook != null)
                    workbook.getMarkerRefCounter().decreaseRef(markerId);
                if (counter != null) {
                    counter.decreaseRef(markerId);
                }
            }
        }
    }

    private void increaseLabelRefs(SheetImpl sheet) {
        if (sheet == null)
            return;

        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls == null)
            return;

        ILabelRefCounter counter = sheet.getLabelRefCounter();
        Iterator<Element> it = DOMUtils.childElementIterByTag(ls, TAG_LABEL);
        while (it.hasNext()) {
            String label = it.next().getTextContent();
            counter.increaseRef(label);
        }
    }

    private void decreaseLabelRefs(SheetImpl sheet) {
        if (sheet == null)
            return;

        Element ls = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_LABELS);
        if (ls == null)
            return;

        ILabelRefCounter counter = sheet.getLabelRefCounter();
        Iterator<Element> it = DOMUtils.childElementIterByTag(ls, TAG_LABEL);
        while (it.hasNext()) {
            String label = it.next().getTextContent();
            counter.decreaseRef(label);
        }
    }

    protected void activateHyperlinks(WorkbookImpl workbook) {
        InternalHyperlinkUtils.activateHyperlink(workbook, getHyperlink(),
                this);
        ((ImageImpl) getImage()).activateHyperlink(workbook);
    }

    protected void deactivateHyperlinks(WorkbookImpl workbook) {
        ((ImageImpl) getImage()).deactivateHyperlink(workbook);
        InternalHyperlinkUtils.deactivateHyperlink(workbook, getHyperlink(),
                this);
    }

    public long getModifiedTime() {
        return InternalDOMUtils.getModifiedTime(this, implementation);
    }

    public String getModifiedBy() {
        return InternalDOMUtils.getModifiedBy(this, implementation);
    }

    protected void updateModificationInfo() {
        InternalDOMUtils.updateModificationInfo(this);

        ISheet sheet = getOwnedSheet();
        if (sheet != null) {
            ((SheetImpl) sheet).updateModificationInfo();
        }
    }

}
