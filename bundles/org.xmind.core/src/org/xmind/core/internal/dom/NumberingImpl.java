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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_NUMBER_DEPTH;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_NUMBER_FORMAT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_NUMBER_SEPARATOR;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_PREPENDING_NUMBERS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_NUMBERING;
import static org.xmind.core.internal.dom.DOMConstants.TAG_PREFIX;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUFFIX;
import static org.xmind.core.internal.dom.DOMConstants.VAL_NONE;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Numbering;
import org.xmind.core.util.DOMUtils;

public class NumberingImpl extends Numbering implements ICoreEventSource {

    private Element topicElement;

    private TopicImpl ownedTopic;

    public NumberingImpl(Element topicElement, TopicImpl ownedTopic) {
        this.topicElement = topicElement;
        this.ownedTopic = ownedTopic;
    }

    public Element getNumberingElement() {
        return DOMUtils.getFirstChildElementByTag(topicElement, TAG_NUMBERING);
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getNumberingElement());
        return super.getAdapter(adapter);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof NumberingImpl))
            return false;
        NumberingImpl that = (NumberingImpl) obj;
        return this.topicElement == that.topicElement;
    }

    public int hashCode() {
        return topicElement.hashCode();
    }

    public String toString() {
        return "Numbering of " + ownedTopic; //$NON-NLS-1$
    }

    public String getNumberFormat() {
        Element e = getNumberingElement();
        if (e == null)
            return null;
        return DOMUtils.getAttribute(e, ATTR_NUMBER_FORMAT);
    }

    public String getSeparator() {
        Element e = getNumberingElement();
        if (e == null)
            return null;
        return DOMUtils.getAttribute(e, ATTR_NUMBER_SEPARATOR);
    }

    public String getPrefix() {
        return getText(TAG_PREFIX);
    }

    public String getSuffix() {
        return getText(TAG_SUFFIX);
    }

    public String getDepth() {
        Element e = getNumberingElement();
        if (e == null)
            return null;
        return DOMUtils.getAttribute(e, ATTR_NUMBER_DEPTH);
    }

    public boolean prependsParentNumbers() {
        Element e = getNumberingElement();
        if (e == null)
            return true;
        String value = DOMUtils.getAttribute(e, ATTR_PREPENDING_NUMBERS);
        return !VAL_NONE.equals(value);
    }

    public ITopic getParent() {
        return ownedTopic;
    }

    public ISheet getOwnedSheet() {
        return ownedTopic.getOwnedSheet();
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedTopic.getOwnedWorkbook();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IWorkbookComponent#isOrphan()
     */
    public boolean isOrphan() {
        return ownedTopic.isOrphan();
    }

    private void setAttribute(String key, String value) {
        if (value == null) {
            Element e = getNumberingElement();
            if (e != null) {
                e.removeAttribute(key);
                if (!e.hasAttributes() && !e.hasChildNodes()) {
                    topicElement.removeChild(e);
                }
            }
        } else {
            Element e = DOMUtils.ensureChildElement(topicElement,
                    TAG_NUMBERING);
            e.setAttribute(key, value);
        }
    }

    private String getText(String key) {
        Element e = getNumberingElement();
        if (e != null) {
            return DOMUtils.getTextContentByTag(e, key);
        }
        return null;
    }

    private void setText(String key, String value) {
        if (value == null) {
            Element e = getNumberingElement();
            if (e != null) {
                Element t = DOMUtils.getFirstChildElementByTag(e, key);
                if (t != null) {
                    e.removeChild(t);
                    if (!e.hasAttributes() && !e.hasChildNodes()) {
                        topicElement.removeChild(e);
                    }
                }
            }
        } else {
            Element e = DOMUtils.ensureChildElement(topicElement,
                    TAG_NUMBERING);
            Element t = DOMUtils.ensureChildElement(e, key);
            t.setTextContent(value);
        }
    }

    public void setFormat(String format) {
        String oldValue = getNumberFormat();
        setAttribute(ATTR_NUMBER_FORMAT, format);
        String newValue = getNumberFormat();
        fireValueChange(Core.NumberFormat, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setPrefix(String prefix) {
        String oldValue = getPrefix();
        setText(TAG_PREFIX, prefix);
        String newValue = getPrefix();
        fireValueChange(Core.NumberingPrefix, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setPrependsParentNumbers(boolean prepend) {
        Boolean oldValue = Boolean.valueOf(prependsParentNumbers());
        setAttribute(ATTR_PREPENDING_NUMBERS, prepend ? null : VAL_NONE);
        Boolean newValue = Boolean.valueOf(prependsParentNumbers());
        fireValueChange(Core.NumberPrepending, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setSuffix(String suffix) {
        String oldValue = getSuffix();
        setText(TAG_SUFFIX, suffix);
        String newValue = getSuffix();
        fireValueChange(Core.NumberingSuffix, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setSeparator(String separator) {
        String oldValue = getSeparator();
        setAttribute(ATTR_NUMBER_SEPARATOR, separator);
        String newValue = getSeparator();
        fireValueChange(Core.NumberingSeparator, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setDepth(String depth) {
        String oldValue = String.valueOf(getComputedDepth());
        setAttribute(ATTR_NUMBER_DEPTH, depth);
        String newValue = String.valueOf(getComputedDepth());
        fireValueChange(Core.NumberingDepth, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    private void fireValueChange(String eventType, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, eventType, oldValue,
                newValue);
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedTopic.getCoreEventSupport();
    }

}
