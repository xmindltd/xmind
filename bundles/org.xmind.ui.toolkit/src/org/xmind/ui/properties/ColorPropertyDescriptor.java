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
/**
 * 
 */
package org.xmind.ui.properties;

import org.eclipse.swt.widgets.Composite;
import org.xmind.ui.color.ColorPickerConfigurer;

/**
 * @author Frank Shaka
 */
public class ColorPropertyDescriptor extends PropertyDescriptor {

    private ColorPickerConfigurer configurer;

    /**
     * @param id
     * @param displayName
     */
    public ColorPropertyDescriptor(String id, String displayName,
            ColorPickerConfigurer configurer) {
        super(id, displayName);
        this.configurer = configurer;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.properties.PropertyDescriptor#createPropertyEditor(org.
     * eclipse .swt.widgets.Composite)
     */
    @Override
    public PropertyEditor createPropertyEditor(Composite parent) {
        ColorPropertyEditor editor = new ColorPropertyEditor(configurer);
        editor.create(parent);
        return editor;
    }

    @Override
    public boolean isTextOperation() {
        return true;
    }

}
