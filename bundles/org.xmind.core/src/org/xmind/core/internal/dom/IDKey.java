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

import org.w3c.dom.Document;

/**
 * Bind element ID with its owner document, so that same IDs in different
 * documents will be corresponded to different objects.
 * 
 * @author Frank Shaka
 */
public class IDKey {
    public Document document;
    public String id;

    public IDKey(Document document, String id) {
        super();
        this.document = document;
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return document.hashCode() ^ id.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof IDKey))
            return false;
        IDKey that = (IDKey) obj;
        return this.document.equals(that.document)
                && this.id.equals(that.id);
    }
}