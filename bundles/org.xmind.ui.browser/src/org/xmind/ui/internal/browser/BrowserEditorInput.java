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
package org.xmind.ui.internal.browser;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class BrowserEditorInput
        implements IEditorInput, IPersistableElement, IElementFactory {

    private static final String ELEMENT_FACTORY_ID = "org.xmind.ui.browser.elementFactory"; //$NON-NLS-1$

    private static final String TAG_URL = "url"; //$NON-NLS-1$

    private static final String TAG_NAME = "name"; //$NON-NLS-1$

    private static final String TAG_STYLE = "style"; //$NON-NLS-1$

    private static final String TAG_TOOLTIP = "tooltip"; //$NON-NLS-1$

    private static final String TAG_CLIENT_ID = "clientId"; //$NON-NLS-1$

    private String url;

    private String name;

    private int style;

    private String tooltip;

    private String clientId;

    public BrowserEditorInput() {
    }

    public BrowserEditorInput(String url) {
        this(url, null, 0);
    }

    public BrowserEditorInput(String url, String clientId, int style) {
        this.url = url;
        this.clientId = clientId;
        this.style = style;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getClientId() {
        return clientId;
    }

    public String getURL() {
        return url;
    }

    public int getStyle() {
        return style;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToolTipText(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return BrowserImages.getImageDescriptor(BrowserImages.BROWSER);
    }

    public String getName() {
        if (name != null)
            return name;

        return BrowserMessages.BrowserEditor_title;
    }

    boolean isNameLocked() {
        return (name != null);
    }

    public IPersistableElement getPersistable() {
        return this;
    }

    public String getToolTipText() {
        if (tooltip != null)
            return tooltip;

        if (url != null)
            return url;

        return BrowserMessages.BrowserEditor_title;
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return null;
    }

    public String getFactoryId() {
        return ELEMENT_FACTORY_ID;
    }

    public void saveState(IMemento memento) {
        if (url != null) {
            memento.putString(TAG_URL, url);
        }
        if (name != null) {
            memento.putString(TAG_NAME, name);
        }
        if (style != 0) {
            memento.putInteger(TAG_STYLE, style);
        }
        if (tooltip != null) {
            memento.putString(TAG_TOOLTIP, tooltip);
        }
        if (clientId != null) {
            memento.putString(TAG_CLIENT_ID, clientId);
        }
    }

    public IAdaptable createElement(IMemento memento) {
        String url = memento.getString(TAG_URL);
        String name = memento.getString(TAG_NAME);
        Integer style = memento.getInteger(TAG_STYLE);
        String tooltip = memento.getString(TAG_TOOLTIP);
        String clientId = memento.getString(TAG_CLIENT_ID);
        BrowserEditorInput input = new BrowserEditorInput();
        input.setUrl(url);
        input.setClientId(clientId);
        input.setName(name);
        input.setToolTipText(tooltip);
        if (style != null)
            input.setStyle(style.intValue());
        return input;
    }

    public String toString() {
        return "BrowserEditorInput[" + url + "]"; //$NON-NLS-1$//$NON-NLS-2$
    }

    public boolean canReplaceInput(BrowserEditorInput input) {
        return clientId != null && clientId.equals(input.clientId);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof BrowserEditorInput))
            return false;
        BrowserEditorInput that = (BrowserEditorInput) obj;
        if (this.url != null && !this.url.equals(that.url))
            return false;
        return canReplaceInput(that);
    }

}