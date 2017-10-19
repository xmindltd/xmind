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

import static org.xmind.core.internal.dom.DOMConstants.ATTR_CHECKSUM;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_CHECKSUM_TYPE;

import org.w3c.dom.Element;
import org.xmind.core.IFileEntry;
import org.xmind.core.internal.EncryptionData;
import org.xmind.core.util.DOMUtils;

/**
 * @author frankshaka
 */
public class EncryptionDataImpl extends EncryptionData {

    private Element implementation;

    private FileEntryImpl entry;

    /**
     * @param implementation
     * @param manifest
     */
    public EncryptionDataImpl(Element implementation, FileEntryImpl entry) {
        super();
        this.implementation = implementation;
        this.entry = entry;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.internal.EncryptionData#getAdapter(java.lang.Class)
     */
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isAssignableFrom(Element.class))
            return adapter.cast(getImplementation());
        return super.getAdapter(adapter);
    }

    /**
     * @return the implementation
     */
    public Element getImplementation() {
        return implementation;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#getIntAttribute(int,
     * java.lang.String[])
     */
    public int getIntAttribute(int defaultValue, String... keyPath) {
        String value = getAttribute(keyPath);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore) {
            }
        }
        return defaultValue;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#getAttribute(java.lang.String)
     */
    public String getAttribute(String... keys) {
        if (keys.length > 0) {
            Element ele = implementation;
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (i == keys.length - 1) {
                    return DOMUtils.getAttribute(ele, key);
                }
                ele = DOMUtils.getFirstChildElementByTag(ele, key);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#setAttribute(java.lang.String,
     * java.lang.String)
     */
    public void setAttribute(String value, String... keys) {
        if (keys.length > 0) {
            Element ele = implementation;
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (i == keys.length - 1) {
                    DOMUtils.setAttribute(ele, key, value);
                }
                Element ele2 = DOMUtils.getFirstChildElementByTag(ele, key);
                if (ele2 == null) {
                    ele = DOMUtils.createElement(ele, key);
                } else {
                    ele = ele2;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#getChecksum()
     */
    public String getChecksum() {
        return getAttribute(ATTR_CHECKSUM);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#getChecksumType()
     */
    public String getChecksumType() {
        return getAttribute(ATTR_CHECKSUM_TYPE);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#setChecksum(java.lang.String)
     */
    public void setChecksum(String checksum) {
        setAttribute(checksum, ATTR_CHECKSUM);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#setChecksumType(java.lang.String)
     */
    public void setChecksumType(String checksumType) {
        setAttribute(checksumType, ATTR_CHECKSUM_TYPE);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.core.IEncryptionData#getFileEntry()
     */
    public IFileEntry getFileEntry() {
        return entry;
    }

}
