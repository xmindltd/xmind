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
package org.xmind.core.util;

public class Property {

    public String key;

    public String value;

    public Property(String key, String value) {
        if (key == null)
            throw new IllegalArgumentException(
                    "The key should NOT be null in a Property."); //$NON-NLS-1$
        this.key = key;
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof Property))
            return false;
        Property that = (Property) obj;
        return this.key.equals(that.key)
                && (this.value == that.value || (this.value != null && this.value
                        .equals(that.value)));
    }

    public int hashCode() {
        int c = key.hashCode();
        if (value != null)
            c ^= value.hashCode();
        return c;
    }

    public String toString() {
        return "{" + key + "=" + value + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}