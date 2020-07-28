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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.xmind.ui.viewers.ILabelDescriptor;

public class PropertyDescriptor implements IPropertyDescriptor {

    private String id;

    private String displayName;

    private String category = null;

    private String description = null;

    private String[] filterFlags = null;

    private String helpContextId = null;

    private ILabelDescriptor labelDescriptor = null;

    public PropertyDescriptor(String id, String displayName) {
        Assert.isNotNull(id);
        Assert.isNotNull(displayName);
        this.id = id;
        this.displayName = displayName;
    }

    public PropertyEditor createPropertyEditor(Composite parent) {
        return null;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getFilterFlags() {
        return filterFlags;
    }

    public String getHelpContextId() {
        return helpContextId;
    }

    public String getId() {
        return id;
    }

    public ILabelDescriptor getLabelDescriptor() {
        return labelDescriptor;
    }

    public PropertyDescriptor setCategory(String category) {
        this.category = category;
        return this;
    }

    public PropertyDescriptor setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PropertyDescriptor setDescription(String description) {
        this.description = description;
        return this;
    }

    public PropertyDescriptor setFilterFlags(String[] filterFlags) {
        this.filterFlags = filterFlags;
        return this;
    }

    public PropertyDescriptor setHelpContextId(String helpContextId) {
        this.helpContextId = helpContextId;
        return this;
    }

    public PropertyDescriptor setLabelDescriptor(
            ILabelDescriptor labelDescriptor) {
        this.labelDescriptor = labelDescriptor;
        return this;
    }

    @Override
    public boolean isTextOperation() {
        return false;
    }

}
