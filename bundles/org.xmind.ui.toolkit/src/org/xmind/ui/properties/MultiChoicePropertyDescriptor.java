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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.xmind.ui.viewers.CachedLabelProvider;

/**
 * @author Frank Shaka
 */
public class MultiChoicePropertyDescriptor extends PropertyDescriptor {

    private Object choices = null;

    private IStructuredContentProvider contentProvider = new ArrayContentProvider();

    /**
     * @param id
     * @param displayName
     */
    public MultiChoicePropertyDescriptor(String id, String displayName) {
        super(id, displayName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyDescriptor#createPropertyEditor(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    public PropertyEditor createPropertyEditor(Composite parent) {
        MultiChoicePropertyEditor editor = new MultiChoicePropertyEditor();
        editor.setInput(choices);
        editor.setContentProvider(contentProvider);
        editor.setLabelProvider(new CachedLabelProvider(getLabelDescriptor()));
        editor.create(parent);
        return editor;
    }

    /**
     * @param input
     *            the choices to set
     */
    public MultiChoicePropertyDescriptor setChoices(Object input) {
        this.choices = input;
        return this;
    }

    public MultiChoicePropertyDescriptor setChoiceProvider(
            IStructuredContentProvider contentProvider) {
        this.contentProvider = contentProvider;
        return this;
    }

}
