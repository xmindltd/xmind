/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.net;

import static org.xmind.core.net.internal.EncodingUtils.urlEncode;

/**
 * 
 * @author Frank Shaka
 * @since 3.6.50
 */
public class Field {

    public final String name;

    public final Object value;

    private String encodedName = null;

    private String encodedValue = null;

    public Field(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value == null ? "" : value.toString(); //$NON-NLS-1$
    }

    public String getEncodedName() {
        if (encodedName == null) {
            encodedName = urlEncode(name);
        }
        return encodedName;
    }

    public String getEncodedValue() {
        if (encodedValue == null) {
            encodedValue = urlEncode(value);
        }
        return encodedValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s: %s", name, value); //$NON-NLS-1$
    }

}