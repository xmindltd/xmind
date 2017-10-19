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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_END1;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_END2;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIPS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SHEET;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TITLE;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Relationship;
import org.xmind.core.util.DOMUtils;

/**
 * @author briansun
 */
public class RelationshipImpl extends Relationship implements ICoreEventSource {

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    private Map<Integer, ControlPointImpl> controlPoints = null;

    /**
     * @param implementation
     */
    public RelationshipImpl(Element implementation,
            WorkbookImpl ownedWorkbook) {
        super();
        this.implementation = DOMUtils.addIdAttribute(implementation);
        this.ownedWorkbook = ownedWorkbook;
    }

    /**
     * @return the implementation
     */
    public Element getImplementation() {
        return implementation;
    }

    /**
     * @see org.xmind.core.IRelationship#getId()
     */
    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof RelationshipImpl))
            return false;
        RelationshipImpl r = (RelationshipImpl) obj;
        return implementation == r.implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    public String toString() {
        return "REL#" + getId() + "(" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * @see org.xmind.core.IRelationship#getEnd1Id()
     */
    public String getEnd1Id() {
        return DOMUtils.getAttribute(implementation, ATTR_END1);
    }

    /**
     * @see org.xmind.core.IRelationship#setEnd1Id(java.lang.String)
     */
    public void setEnd1Id(String id) {
        String oldId = getEnd1Id();
        DOMUtils.setAttribute(implementation, ATTR_END1, id);
        String newId = getEnd1Id();
        fireValueChange(Core.RelationshipEnd1, oldId, newId);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.IRelationship#getEnd2Id()
     */
    public String getEnd2Id() {
        return DOMUtils.getAttribute(implementation, ATTR_END2);
    }

    /**
     * @see org.xmind.core.IRelationship#setEnd2Id(java.lang.String)
     */
    public void setEnd2Id(String id) {
        String oldId = getEnd2Id();
        DOMUtils.setAttribute(implementation, ATTR_END2, id);
        String newId = getEnd2Id();
        fireValueChange(Core.RelationshipEnd2, oldId, newId);
        updateModificationInfo();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.internal.Relationship#getEnd1()
     */
    @Override
    public IRelationshipEnd getEnd1() {
        return findEnd(getEnd1Id());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.internal.Relationship#getEnd2()
     */
    @Override
    public IRelationshipEnd getEnd2() {
        return findEnd(getEnd2Id());
    }

    private IRelationshipEnd findEnd(String endId) {
        if (endId == null)
            return null;

        IAdaptable obj = ownedWorkbook.getAdaptableRegistry()
                .getAdaptable(endId, implementation.getOwnerDocument());
        if (obj instanceof IRelationshipEnd) {
            return (IRelationshipEnd) obj;
        }
        return null;
    }

    /**
     * @see org.xmind.core.IRelationship#getParent()
     */
    public ISheet getParent() {
        Node p = implementation.getParentNode();
        if (DOMUtils.isElementByTag(p, TAG_RELATIONSHIPS)) {
            p = p.getParentNode();
            if (DOMUtils.isElementByTag(p, TAG_SHEET)) {
                return (ISheet) ownedWorkbook.getAdaptableRegistry()
                        .getAdaptable(p);
            }
        }
        return null;
    }

    /**
     * @see org.xmind.core.ITitled#setTitleText(java.lang.String)
     */
    public void setTitleText(String titleText) {
        String oldText = getLocalTitleText();
        DOMUtils.setText(implementation, TAG_TITLE, titleText);
        String newText = getLocalTitleText();
        fireValueChange(Core.TitleText, oldText, newText);
        updateModificationInfo();
    }

    /**
     * @see org.xmind.core.internal.Relationship#getLocalTitleText()
     */
    @Override
    protected String getLocalTitleText() {
        return DOMUtils.getTextContentByTag(implementation, TAG_TITLE);
    }

    /**
     * @see org.xmind.core.internal.Relationship#getOwnedWorkbook()
     */
    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(implementation);
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.core.IRelationship#checkAvailable()
     */
    public boolean checkAvailable() {
        IRelationshipEnd end1 = getEnd1();
        IRelationshipEnd end2 = getEnd2();
        if (end1 != null && end2 != null) {
            ISheet sheet1 = end1.getOwnedSheet();
            if (sheet1 != null && sheet1.equals(end2.getOwnedSheet()))
                return true;
        }
        return false;
    }

    public String getStyleId() {
        return DOMUtils.getAttribute(implementation, ATTR_STYLE_ID);
    }

    public void setStyleId(String styleId) {
        String oldValue = getStyleId();
        WorkbookImpl workbook = getRealizedWorkbook();
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
        DOMUtils.setAttribute(implementation, ATTR_STYLE_ID, styleId);
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        String newValue = getStyleId();
        fireValueChange(Core.Style, oldValue, newValue);
        updateModificationInfo();
    }

    public IControlPoint getControlPoint(int index) {
        if (controlPoints == null)
            controlPoints = new HashMap<Integer, ControlPointImpl>();
        ControlPointImpl controlPoint = controlPoints
                .get(Integer.valueOf(index));
        if (controlPoint == null) {
            controlPoint = new ControlPointImpl(this, index);
            controlPoints.put(Integer.valueOf(index), controlPoint);
        }
        return controlPoint;
//        Element cp = getControlPointElement(index);
//        if (cp != null) {
//            return new ControlPointImpl(cp);
//        }
//        return null;
    }

//    private Element getControlPointElement(int index) {
//        Element cps = getFirstChildElementByTag(implementation,
//                TAG_CONTROL_POINTS);
//        return cps == null ? null : getControlPointElement(index, cps);
//    }
//
//    private Element getControlPointElement(int index, Element cps) {
//        Iterator<Element> it = childElementIterByTag(cps, TAG_CONTROL_POINT);
//        while (it.hasNext()) {
//            Element cp = it.next();
//            String i = cp.getAttribute(ATTR_INDEX);
//            if (i != null && NumberUtils.safeParseInt(i, -1) == index)
//                return cp;
//        }
//        return null;
//    }
//
//    public void setControlPoint(int index, double angle, double amount) {
//        Element cps = ensureChildElement(implementation, TAG_CONTROL_POINTS);
//        Element cp = getControlPointElement(index, cps);
//        if (cp == null) {
//            cp = createElement(cps, TAG_CONTROL_POINT);
//            cp.setAttribute(ATTR_INDEX, Integer.toString(index));
//        }
//        cp.setAttribute(ATTR_ANGLE, Double.toString(angle));
//        cp.setAttribute(ATTR_AMOUNT, Double.toString(amount));
//        fireIndexedTargetChange(Core.RelationshipControlPoint,
//                getControlPoint(index), index);
//    }
//
//    public void resetControlPoint(int index) {
//        Element cps = getFirstChildElementByTag(implementation,
//                TAG_CONTROL_POINTS);
//        if (cps == null)
//            return;
//        Element cp = getControlPointElement(index, cps);
//        if (cp == null)
//            return;
//        cp.removeAttribute(ATTR_ANGLE);
//        cp.removeAttribute(ATTR_AMOUNT);
//        if (!cp.hasChildNodes()) {
//            cps.removeChild(cp);
//        }
//        fireIndexedTargetChange(Core.RelationshipControlPoint,
//                getControlPoint(index), index);
//    }

    protected WorkbookImpl getRealizedWorkbook() {
        ISheet sheet = getParent();
        if (sheet != null) {
            if (ownedWorkbook == sheet.getParent())
                return ownedWorkbook;
        }
        return null;
    }

    protected void addNotify(WorkbookImpl workbook, SheetImpl parent) {
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, true);
        workbook.getAdaptableRegistry().registerById(this, getId(),
                getImplementation().getOwnerDocument());
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
    }

    protected void removeNotify(WorkbookImpl workbook, SheetImpl parent) {
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
        workbook.getAdaptableRegistry().unregisterById(this, getId(),
                getImplementation().getOwnerDocument());
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, false);
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    public ICoreEventSupport getCoreEventSupport() {
        // Use workbook's core event support directly, so that 
        // orphan components can have events broadcasted, which 
        // will enable transient actions (such as dragging topics 
        // or adjusting relationship control points, etc.) to 
        // perform correctly.
        return ownedWorkbook.getCoreEventSupport();
    }

    private void fireValueChange(String type, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, type, oldValue,
                newValue);
    }

    public long getModifiedTime() {
        return InternalDOMUtils.getModifiedTime(this, implementation);
    }

    public String getModifiedBy() {
        return InternalDOMUtils.getModifiedBy(this, implementation);
    }

    protected void updateModificationInfo() {
        InternalDOMUtils.updateModificationInfo(this);

        ISheet sheet = getParent();
        if (sheet != null) {
            ((SheetImpl) sheet).updateModificationInfo();
        }
    }

}
