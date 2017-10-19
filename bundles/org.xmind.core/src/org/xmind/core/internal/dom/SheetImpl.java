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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_THEME;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIPS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TITLE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;

import java.util.Collections;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmind.core.Core;
import org.xmind.core.ILegend;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheetSetting;
import org.xmind.core.ISheetSettings;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Sheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.ILabelRefCounter;
import org.xmind.core.util.IMarkerRefCounter;

/**
 * @author Brian Sun
 * @author Frank Shaka
 */
public class SheetImpl extends Sheet implements ICoreEventSource {

    private static final Set<IRelationship> NO_RELATIONSHIPS = Collections
            .emptySet();

    private Element implementation;

    private WorkbookImpl ownedWorkbook;

    private SheetMarkerRefCounter markerRefCounter = null;

    private SheetLabelRefCounter labelRefCounter = null;

    private LegendImpl legend = null;

    private ISheetSetting setting;

    private ISheetSettings settings;

    /**
     * @param implementation
     */
    public SheetImpl(Element implementation, WorkbookImpl ownedWorkbook) {
        super();
        this.ownedWorkbook = ownedWorkbook;
        this.implementation = DOMUtils.addIdAttribute(implementation);
        DOMUtils.ensureChildElement(implementation, TAG_TOPIC);
        //((TopicImpl) getRootTopic()).addNotify(ownedWorkbook, this, null);
    }

    /**
     * @return the implementation
     */
    public Element getImplementation() {
        return implementation;
    }

    public int hashCode() {
        return implementation.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof SheetImpl))
            return false;
        SheetImpl t = (SheetImpl) obj;
        return implementation == t.implementation;
    }

    public String toString() {
        return "SHT#" + getId() + "(" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class)) {
            return adapter.cast(implementation);
        } else if (IMarkerRefCounter.class.equals(adapter)) {
            return adapter.cast(getMarkerRefCounter());
        } else if (ILabelRefCounter.class.equals(adapter)) {
            return adapter.cast(getLabelRefCounter());
        } else if (ICoreEventSupport.class.equals(adapter)) {
            return adapter.cast(getCoreEventSupport());
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see org.xmind.core.internal.Sheet#getLocalTitleText()
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
     * @see org.xmind.core.ISheet#getRootTopic()
     */
    public ITopic getRootTopic() {
        Element t = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_TOPIC);
        return (ITopic) ownedWorkbook.getAdaptableRegistry().getAdaptable(t);
    }

    /**
     * @see org.xmind.core.ISheet#getId()
     */
    public String getId() {
        return implementation.getAttribute(ATTR_ID);
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return DOMUtils.isOrphanNode(implementation);
    }

    /**
     * @see org.xmind.core.ISheet#getParent()
     */
    public IWorkbook getParent() {
        Node p = implementation.getParentNode();
        if (p == ownedWorkbook.getWorkbookElement())
            return ownedWorkbook;
        return null;
    }

    /**
     * @see org.xmind.core.ISheet#getIndex()
     */
    public int getIndex() {
        Node p = implementation.getParentNode();
        if (p == ownedWorkbook.getWorkbookElement()) {
            return DOMUtils.getElementIndex(p, DOMConstants.TAG_SHEET,
                    implementation);
        }
        return -1;
    }

    /**
     * @see org.xmind.core.ISheet#getRelationships()
     */
    public Set<IRelationship> getRelationships() {
        Element rs = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_RELATIONSHIPS);
        if (rs != null)
            return DOMUtils.getChildSet(rs, TAG_RELATIONSHIP,
                    ownedWorkbook.getAdaptableRegistry());
        return NO_RELATIONSHIPS;
    }

    /**
     * @see org.xmind.core.ISheet#addRelationship(org.xmind.core.IRelationship)
     */
    public void addRelationship(IRelationship rel) {
        Element rs = DOMUtils.ensureChildElement(implementation,
                TAG_RELATIONSHIPS);
        Element r = ((RelationshipImpl) rel).getImplementation();
        Node n = rs.appendChild(r);
        if (n != null) {
            if (!isOrphan()) {
                ((RelationshipImpl) rel).addNotify(ownedWorkbook, this);
            }
            fireTargetChange(Core.RelationshipAdd, rel);
        }
    }

    /**
     * @see org.xmind.core.ISheet#removeRelationship(org.xmind.core.IRelationship)
     */
    public void removeRelationship(IRelationship rel) {
        Element rs = DOMUtils.getFirstChildElementByTag(implementation,
                TAG_RELATIONSHIPS);
        if (rs != null) {
            if (!isOrphan()) {
                ((RelationshipImpl) rel).removeNotify(ownedWorkbook, this);
            }
            Element r = ((RelationshipImpl) rel).getImplementation();
            Node n = rs.removeChild(r);
            if (!rs.hasChildNodes())
                implementation.removeChild(rs);
            if (n != null) {
                fireTargetChange(Core.RelationshipRemove, rel);
                updateModificationInfo();
            }
        }
    }

    public String getThemeId() {
        return DOMUtils.getAttribute(implementation, ATTR_THEME);
    }

    public void setThemeId(String themeId) {
        String oldValue = getThemeId();
        WorkbookImpl workbook = (WorkbookImpl) getParent();
        decreaseThemeRef(workbook);
        DOMUtils.setAttribute(implementation, ATTR_THEME, themeId);
        increaseThemeRef(workbook);
        String newValue = getThemeId();
        fireValueChange(Core.ThemeId, oldValue, newValue);
        updateModificationInfo();
    }

    public String getStyleId() {
        return DOMUtils.getAttribute(implementation, ATTR_STYLE_ID);
    }

    public void setStyleId(String styleId) {
        String oldValue = getStyleId();
        WorkbookImpl workbook = (WorkbookImpl) getParent();
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
        DOMUtils.setAttribute(implementation, ATTR_STYLE_ID, styleId);
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        String newValue = getStyleId();
        fireValueChange(Core.Style, oldValue, newValue);
        updateModificationInfo();
    }

    public void replaceRootTopic(ITopic newRootTopic) {
        TopicImpl r1 = (TopicImpl) getRootTopic();
        TopicImpl r2 = (TopicImpl) newRootTopic;
        if (!isOrphan()) {
            r1.removeNotify((WorkbookImpl) getParent(), this, null);
        }
        implementation.removeChild(r1.getImplementation());
        implementation.appendChild(r2.getImplementation());
        if (!isOrphan()) {
            r2.addNotify((WorkbookImpl) getParent(), this, null);
        }
        fireValueChange(Core.RootTopic, r1, r2);
        updateModificationInfo();
    }

    public ILegend getLegend() {
        if (legend == null) {
            legend = new LegendImpl(implementation, this);
        }
        return legend;
    }

    public ILabelRefCounter getLabelRefCounter() {
        if (labelRefCounter == null)
            labelRefCounter = new SheetLabelRefCounter(this);
        return labelRefCounter;
    }

    public IMarkerRefCounter getMarkerRefCounter() {
        if (markerRefCounter == null)
            markerRefCounter = new SheetMarkerRefCounter(this);
        return markerRefCounter;
    }

    protected void addNotify(WorkbookImpl workbook) {
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, true);
        workbook.getAdaptableRegistry().registerById(this, getId(),
                getImplementation().getOwnerDocument());
        WorkbookUtilsImpl.increaseStyleRef(workbook, this);
        increaseThemeRef(workbook);
        ((TopicImpl) getRootTopic()).addNotify(workbook, this, null);
        for (IRelationship rel : getRelationships()) {
            ((RelationshipImpl) rel).addNotify(workbook, this);
        }
    }

    protected void removeNotify(WorkbookImpl workbook) {
        for (IRelationship rel : getRelationships()) {
            ((RelationshipImpl) rel).removeNotify(workbook, this);
        }
        ((TopicImpl) getRootTopic()).removeNotify(workbook, this, null);
        decreaseThemeRef(workbook);
        WorkbookUtilsImpl.decreaseStyleRef(workbook, this);
        workbook.getAdaptableRegistry().unregisterById(this, getId(),
                getImplementation().getOwnerDocument());
        getImplementation().setIdAttribute(DOMConstants.ATTR_ID, false);
    }

    private void decreaseThemeRef(WorkbookImpl workbook) {
        if (workbook == null)
            return;

        String themeId = getThemeId();
        if (themeId != null)
            workbook.getStyleRefCounter().decreaseRef(themeId);
    }

    private void increaseThemeRef(WorkbookImpl workbook) {
        if (workbook == null)
            return;

        String themeId = getThemeId();
        if (themeId != null)
            workbook.getStyleRefCounter().increaseRef(themeId);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedWorkbook.getCoreEventSupport();
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    private void fireValueChange(String type, Object oldValue,
            Object newValue) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null) {
            coreEventSupport.dispatchValueChange(this, type, oldValue,
                    newValue);
        }
    }

    private void fireTargetChange(String type, Object target) {
        ICoreEventSupport coreEventSupport = getCoreEventSupport();
        if (coreEventSupport != null) {
            coreEventSupport.dispatchTargetChange(this, type, target);
        }
    }

    public long getModifiedTime() {
        return InternalDOMUtils.getModifiedTime(this, implementation);
    }

    public String getModifiedBy() {
        return InternalDOMUtils.getModifiedBy(this, implementation);
    }

    protected void updateModificationInfo() {
        InternalDOMUtils.updateModificationInfo(this);

        IWorkbook parent = getParent();
        if (parent != null) {
            ((WorkbookImpl) parent).updateModificationInfo();
        }
    }

    public ISheetSetting getSetting() {
        if (setting == null)
            setting = new SheetSetting(implementation, this);
        return setting;
    }

    public ISheetSettings getSettings() {
        if (settings == null)
            settings = new SheetSettingsImpl(implementation, this);
        return settings;
    }

}
