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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALIGN;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_HEIGHT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SRC;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_WIDTH;
import static org.xmind.core.internal.dom.DOMConstants.TAG_IMG;
import static org.xmind.core.internal.dom.NumberUtils.safeParseInt;

import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.Image;
import org.xmind.core.util.DOMUtils;

public class ImageImpl extends Image implements ICoreEventSource {

    private Element topicElement;

    private TopicImpl ownedTopic;

    public ImageImpl(Element topicElement, TopicImpl ownedTopic) {
        this.topicElement = topicElement;
        this.ownedTopic = ownedTopic;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ImageImpl))
            return false;
        ImageImpl that = (ImageImpl) obj;
        return this.topicElement == that.topicElement;
    }

    public int hashCode() {
        return topicElement.hashCode();
    }

    public String toString() {
        return DOMUtils.toString(getImageElement());
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (ICoreEventSource.class.equals(adapter))
            return adapter.cast(this);
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImageElement());
        return super.getAdapter(adapter);
    }

    private Element getImageElement() {
        return DOMUtils.getFirstChildElementByTag(topicElement, TAG_IMG);
    }

    public String getAlignment() {
        Element img = getImageElement();
        return img == null ? null : DOMUtils.getAttribute(img, ATTR_ALIGN);
    }

    public int getHeight() {
        Element img = getImageElement();
        if (img == null)
            return UNSPECIFIED;
        return safeParseInt(DOMUtils.getAttribute(img, ATTR_HEIGHT),
                UNSPECIFIED);
    }

    private Integer getHeightInt() {
        int h = getHeight();
        return h < 0 ? null : Integer.valueOf(h);
    }

    private Integer getWidthInt() {
        int w = getWidth();
        return w < 0 ? null : Integer.valueOf(w);
    }

    public ITopic getParent() {
        return ownedTopic;
    }

    public String getSource() {
        Element img = getImageElement();
        return img == null ? null : DOMUtils.getAttribute(img, ATTR_SRC);
    }

    public int getWidth() {
        Element img = getImageElement();
        if (img == null)
            return UNSPECIFIED;
        return safeParseInt(DOMUtils.getAttribute(img, ATTR_WIDTH),
                UNSPECIFIED);
    }

    private Element ensureImageElement() {
        return DOMUtils.ensureChildElement(topicElement, TAG_IMG);
    }

    public void setAlignment(String alignment) {
        String oldValue = getAlignment();
        Element img = ensureImageElement();
        DOMUtils.setAttribute(img, ATTR_ALIGN, alignment);
        checkImageElement();
        String newValue = getAlignment();
        fireValueChange(Core.ImageAlignment, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setHeight(int height) {
        Integer oldValue = getHeightInt();
        Element img = ensureImageElement();
        String h = height < 0 ? null : Integer.toString(height);
        DOMUtils.setAttribute(img, ATTR_HEIGHT, h);
        checkImageElement();
        Integer newValue = getHeightInt();
        fireValueChange(Core.ImageHeight, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setSize(int width, int height) {
        Integer oldWidth = getWidthInt();
        Integer oldHeight = getHeightInt();
        Element img = ensureImageElement();
        String h = height < 0 ? null : Integer.toString(height);
        String w = width < 0 ? null : Integer.toString(width);
        DOMUtils.setAttribute(img, ATTR_HEIGHT, h);
        DOMUtils.setAttribute(img, ATTR_WIDTH, w);
        checkImageElement();
        Integer newWidth = getWidthInt();
        Integer newHeight = getHeightInt();
        fireValueChange(Core.ImageWidth, oldWidth, newWidth);
        fireValueChange(Core.ImageHeight, oldHeight, newHeight);
        ownedTopic.updateModificationInfo();
    }

    public void setSource(String source) {
        String oldValue = getSource();
        Element img = ensureImageElement();
        IWorkbook workbook = ownedTopic.getPath().getWorkbook();
        deactivateHyperlink(workbook);
        DOMUtils.setAttribute(img, ATTR_SRC, source);
        activateHyperlink(workbook);
        checkImageElement();
        String newValue = getSource();
        fireValueChange(Core.ImageSource, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    public void setWidth(int width) {
        Integer oldValue = getWidthInt();
        Element img = ensureImageElement();
        String w = width < 0 ? null : Integer.toString(width);
        DOMUtils.setAttribute(img, ATTR_WIDTH, w);
        checkImageElement();
        Integer newValue = getWidthInt();
        fireValueChange(Core.ImageWidth, oldValue, newValue);
        ownedTopic.updateModificationInfo();
    }

    private void checkImageElement() {
        Element img = getImageElement();
        if (!img.hasChildNodes() && !img.hasAttributes()) {
            topicElement.removeChild(img);
        }
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
        return ownedTopic.isOrphan() || getImageElement() == null;
    }

    private void fireValueChange(String eventType, Object oldValue,
            Object newValue) {
        getCoreEventSupport().dispatchValueChange(this, eventType, oldValue,
                newValue);
    }

    public ICoreEventSupport getCoreEventSupport() {
        return ownedTopic.getCoreEventSupport();
    }

    public ICoreEventRegistration registerCoreEventListener(String type,
            ICoreEventListener listener) {
        return getCoreEventSupport().registerCoreEventListener(this, type,
                listener);
    }

    protected void activateHyperlink(IWorkbook workbook) {
        InternalHyperlinkUtils.activateHyperlink(workbook, getSource(), this);
    }

    protected void deactivateHyperlink(IWorkbook workbook) {
        InternalHyperlinkUtils.deactivateHyperlink(workbook, getSource(), this);
    }

}
