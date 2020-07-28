/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.properties;

import org.eclipse.swt.widgets.Composite;
import org.xmind.ui.viewers.ILabelDescriptor;

public interface IPropertyDescriptor {

    /**
     * Creates and returns a new in-place editor for editing this property.
     * Returns <code>null</code> if the property is not editable.
     * 
     * @param parent
     *            the parent widget for the cell editor
     * @return the in-place editor for this property, or <code>null</code> if
     *         this property cannot be edited
     */
    PropertyEditor createPropertyEditor(Composite parent);

    /**
     * Returns the name of the category to which this property belongs.
     * Properties belonging to the same category are grouped together visually.
     * This localized string is shown to the user
     * 
     * @return the category name, or <code>null</code> if the default category
     *         is to be used
     */
    String getCategory();

    /**
     * Returns a brief description of this property. This localized string is
     * shown to the user when this property is selected.
     * 
     * @return a brief description, or <code>null</code> if none
     */
    String getDescription();

    /**
     * Returns the display name for this property. This localized string is
     * shown to the user as the name of this property.
     * 
     * @return a displayable name
     */
    String getDisplayName();

    /**
     * Returns a list of filter types to which this property belongs. The user
     * is able to toggle the filters to show/hide properties belonging to a
     * filter type.
     * <p>
     * Valid values for these flags are declared as constants on
     * <code>IPropertySheetEntry</code>
     * </p>
     * 
     * @return a list of filter types to which this property belongs, or
     *         <code>null</code> if none
     */
    String[] getFilterFlags();

    /**
     * Returns the help context id for this property or <code>null</code> if
     * this property has no help context id.
     * 
     * @return the help context id for this entry
     */
    String getHelpContextId();

    /**
     * Returns the id for this property. This object is used internally to
     * distinguish one property descriptor from another.
     * 
     * @return the property id
     */
    String getId();

    /**
     * Returns the label descriptor for this property. A label descriptor is
     * asked for label representations of the property value, such as text,
     * image, foreground color, etc.
     * 
     * @return the label descriptor
     */
    ILabelDescriptor getLabelDescriptor();

    boolean isTextOperation();

}
