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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;

public class PropertyEditingEntryPropertyTester extends PropertyTester {

    public PropertyEditingEntryPropertyTester() {
    }

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (receiver instanceof IPropertyEditingEntry) {
            IPropertyEditingEntry entry = (IPropertyEditingEntry) receiver;
            if (IPropertyEditingEntry.PROP_EDITABLE.equals(property))
                return matchEntryEditable(entry, expectedValue);
            if (IPropertyEditingEntry.PROP_RESETTABLE.equals(property))
                return matchEntryResettable(entry, expectedValue);
            if (IPropertyEditingEntry.PROP_PROPERTY_SET.equals(property))
                return matchIsPropertySet(entry, expectedValue);
            if (IPropertyEditingEntry.PROP_TEXT_OPERATION.equals(property))
                return matchIsTextOperation(entry, expectedValue);
        }
        Assert.isTrue(false);
        return false;
    }

    private boolean matchEntryEditable(IPropertyEditingEntry entry,
            Object value) {
        boolean editable = entry.isEditable();
        if (value == null)
            return editable;
        if (value instanceof String)
            return Boolean.parseBoolean((String) value) == editable;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() == editable;
        return false;
    }

    private boolean matchEntryResettable(IPropertyEditingEntry entry,
            Object value) {
        boolean resettable = entry.isResettable();
        if (value == null)
            return resettable;
        if (value instanceof String)
            return Boolean.parseBoolean((String) value) == resettable;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() == resettable;
        return false;
    }

    private boolean matchIsPropertySet(IPropertyEditingEntry entry,
            Object value) {
        boolean propertySet = entry.isPropertySet();
        if (value == null)
            return propertySet;
        if (value instanceof String)
            return Boolean.parseBoolean((String) value) == propertySet;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() == propertySet;
        return false;
    }

    private boolean matchIsTextOperation(IPropertyEditingEntry entry,
            Object value) {
        boolean isTextOperation = entry.isTextOperation();
        if (value == null)
            return isTextOperation;
        if (value instanceof String)
            return Boolean.parseBoolean((String) value) == isTextOperation;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() == isTextOperation;
        return false;
    }

}
