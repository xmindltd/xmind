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

import org.eclipse.core.runtime.IAdaptable;

/**
 * The <code>IPropertyEditingEntry</code> describes the editing model of a
 * property.
 * 
 * @author Frank Shaka
 */
public interface IPropertyEditingEntry extends IAdaptable {

    public static final String PROP_NAMESPACE = "org.xmind.ui.properties.editingEntry"; //$NON-NLS-1$
    public static final String PROP_EDITABLE = "editable"; //$NON-NLS-1$
    public static final String PROP_RESETTABLE = "resettable"; //$NON-NLS-1$
    public static final String PROP_PROPERTY_SET = "isPropertySet"; //$NON-NLS-1$
    public static final String PROP_TEXT_OPERATION = "isTextOperation"; //$NON-NLS-1$

    /**
     * Returns whether this entry is editable. An entry is editable if it has a
     * valid property editor created by the property descriptor.
     * 
     * @return <code>true</code> if this entry is editable, or
     *         <code>false</code> otherwise
     * @see IPropertyDescriptor#createPropertyEditor(org.eclipse.swt.widgets.Composite)
     */
    boolean isEditable();

    /**
     * Returns whether this entry is resettable. An entry is resettable if the
     * property source is resettable on this property.
     * 
     * @return <code>true</code> if this entry is resettable, or
     *         <code>false</code> otherwise
     * @see IPropertySource#isPropertyResettable(String)
     */
    boolean isResettable();

    /**
     * Returns whether the property represented by this entry has been set to a
     * non-default value.
     * 
     * @return <code>true</code> if this entry has non-default value, or
     *         <code>false</code> otherwise
     * @see IPropertySource#isPropertySet(String)
     */
    boolean isPropertySet();

    /**
     * Resets the property to its default value.
     * 
     * @see IPropertySource#resetPropertyValue(String)
     */
    void resetPropertyValue();

    boolean isTextOperation();

}
