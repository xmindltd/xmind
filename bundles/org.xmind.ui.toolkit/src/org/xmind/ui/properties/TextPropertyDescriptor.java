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

import org.eclipse.swt.widgets.Composite;

public class TextPropertyDescriptor extends PropertyDescriptor {

    public TextPropertyDescriptor(String id, String displayName) {
        super(id, displayName);
    }

    public PropertyEditor createPropertyEditor(Composite parent) {
        TextPropertyEditor editor = new TextPropertyEditor();
        editor.create(parent);
        return editor;
    }

    @Override
    public boolean isTextOperation() {
        return true;
    }

}
