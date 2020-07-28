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
package org.xmind.ui.properties;

public interface IPropertySource {

    /**
     * Returns the list of property descriptors for this property source. The
     * <code>getPropertyValue</code> and <code>setPropertyValue</code> methods
     * are used to read and write the actual property values by specifying the
     * property ids from these property descriptors.
     * <p>
     * Implementors should cache the descriptors as they will be asked for the
     * descriptors with any edit/update. Since descriptors provide cell editors,
     * returning the same descriptors if possible allows for efficient updating.
     * </p>
     * 
     * @return the property descriptors
     */
    IPropertyDescriptor[] getPropertyDescriptors();

    /**
     * Returns the value of the property with the given id if it has one.
     * Returns <code>null</code> if the property's value is <code>null</code>
     * value or if this source does not have the specified property.
     * 
     * @see #setPropertyValue
     * @param id
     *            the id of the property being set
     * @return the value of the property, or <code>null</code>
     */
    Object getPropertyValue(String id);

    /**
     * Returns whether the value of the property with the given id has changed
     * from its default value. Returns <code>false</code> if this source does
     * not have the specified property.
     * <p>
     * If the notion of default value is not meaningful for the specified
     * property then <code>false</code> is returned.
     * </p>
     * 
     * @param id
     *            the id of the property
     * @return <code>true</code> if the value of the specified property has
     *         changed from its original default value, <code>false</code> if
     *         the specified property does not have a meaningful default value,
     *         and <code>false</code> if this source does not have the specified
     *         property
     * @see #isPropertyResettable(Object)
     * @see #resetPropertyValue(Object)
     */
    boolean isPropertySet(String id);

    /**
     * Sets the property with the given id if possible. Does nothing if the
     * property's value cannot be changed or if this source does not have the
     * specified property.
     * <p>
     * In general, a property source should not directly reference the value
     * parameter unless it is an atomic object that can be shared, such as a
     * string.
     * </p>
     * <p>
     * An important reason for this is that several property sources with
     * compatible descriptors could be appearing in the property sheet at the
     * same time. An editor produces a single edited value which is passed as
     * the value parameter of this message to all the property sources. Thus to
     * avoid a situation where all of the property sources reference the same
     * value they should use the value parameter to create a new instance of the
     * real value for the given property.
     * </p>
     * <p>
     * There is another reason why a level of indirection is useful. The real
     * value of property may be a type that cannot be edited with a standard
     * cell editor. However instead of returning the real value in
     * <code>getPropertyValue</code>, the value could be converted to a
     * <code>String</code> which could be edited with a standard cell editor.
     * The edited value will be passed to this method which can then turn it
     * back into the real property value.
     * </p>
     * <p>
     * Another variation on returning a value other than the real property value
     * in <code>getPropertyValue</code> is to return a value which is an
     * <code>IPropertySource</code> (or for which the property sheet can obtain
     * an <code>IPropertySource</code>). In this case the value to edit is
     * obtained from the child property source using
     * <code>getEditableValue</code>. It is this editable value that will be
     * passed back via this method when it has been editted
     * </p>
     * 
     * @see #getPropertyValue
     * @see #getEditableValue
     * @param id
     *            the id of the property being set
     * @param value
     *            the new value for the property; <code>null</code> is allowed
     */
    void setPropertyValue(String id, Object value);

    /**
     * Resets the property with the given id to its default value if possible.
     * <p>
     * Does nothing if the notion of a default value is not meaningful for the
     * specified property, or if the property's value cannot be changed, or if
     * this source does not have the specified property.
     * </p>
     * <p>
     * This method will only be called if
     * <code>#isPropertyResettable(Object)</code> returns <code>true</code> for
     * the property with the given id.
     * </p>
     * 
     * @param id
     *            the id of the property being reset
     * @see #isPropertySet(Object)
     * @see #isPropertyResettable(Object)
     */
    void resetPropertyValue(String id);

    /**
     * Returns whether the value of the property with the specified id is
     * resettable to a default value.
     * 
     * @param id
     *            the id of the property
     * @return <code>true</code> if the property with the specified id has a
     *         meaningful default value to which it can be resetted, and
     *         <code>false</code> otherwise
     * @see #resetPropertyValue(Object)
     * @see #isPropertySet(Object)
     */
    boolean isPropertyResettable(String id);

}
