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
package org.xmind.core;

import java.util.Iterator;

import org.xmind.core.util.Property;

public interface IProperties {

    /**
     * Gets the value of the property specified by a key.
     * 
     * @param key
     *            a <code>String</code> to identify the property
     * @return the value of the specified property, or <code>null</code> if the
     *         property is not found
     */
    String getProperty(String key);

    /**
     * Gets the value of the property specified by a key. If the property is not
     * found, the default value is returned.
     * 
     * @param key
     *            a <code>String</code> to identify the property
     * @param defaultValue
     *            a default value for non-found properties
     * @return the value of the specified proeprty, or <code>defaultValue</code>
     *         if the property is not found
     */
    String getProperty(String key, String defaultValue);

    /**
     * Sets the value of the property specified by a key, or remove the property
     * if the value is <code>null</code>.
     * 
     * @param key
     *            a <code>String</code> to identify the property
     * @param value
     *            the new value of the specified property, or <code>null</code>
     */
    void setProperty(String key, String value);

    /**
     * Iterates over all properties of this object.
     * 
     * @return an {@link Iterator} that iterates over all properties of this
     *         object
     */
    Iterator<Property> properties();

    /**
     * Gets the number of properties of this object.
     * 
     * @return the number of properties of this object
     */
    int size();

    /**
     * Determines whether this object has any property.
     * 
     * @return <code>true</code> if this object has more than zero properties,
     *         or <code>false</code> if this object has no property
     */
    boolean isEmpty();

}