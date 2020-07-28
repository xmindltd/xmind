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
package org.xmind.ui.actions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class DelegatingAction extends Action implements IWorkbenchAction,
        IPropertyChangeListener {

    private IAction delegate;

    private List<String> properties;

    public DelegatingAction(IAction delegate) {
        this(delegate, TEXT, IMAGE, TOOL_TIP_TEXT, DESCRIPTION, CHECKED,
                ENABLED);
    }

    public DelegatingAction(IAction delegate, String... properties) {
        this(delegate, delegate.getStyle(), properties);
    }

    public DelegatingAction(IAction delegate, int style, String... properties) {
        super(delegate.getText(), style);
        this.delegate = delegate;
        this.properties = Arrays.asList(properties);
        setId(delegate.getId());
        setActionDefinitionId(delegate.getActionDefinitionId());
        initProperties(delegate);
        delegate.addPropertyChangeListener(this);
    }

    private void initProperties(IAction delegate) {
        if (properties.contains(TEXT)) {
            setText(delegate.getText());
            setAccelerator(delegate.getAccelerator());
        }
        if (properties.contains(TOOL_TIP_TEXT)) {
            setToolTipText(delegate.getToolTipText());
        }
        if (properties.contains(IMAGE)) {
            setImageDescriptor(delegate.getImageDescriptor());
            setDisabledImageDescriptor(delegate.getDisabledImageDescriptor());
            setHoverImageDescriptor(delegate.getHoverImageDescriptor());
        }
        if (properties.contains(ENABLED)) {
            setEnabled(delegate.isEnabled());
        }
        if (properties.contains(CHECKED)) {
            if (delegate.getStyle() == AS_CHECK_BOX
                    || delegate.getStyle() == AS_RADIO_BUTTON)
                setChecked(delegate.isChecked());
        }
        if (properties.contains(DESCRIPTION)) {
            setDescription(delegate.getDescription());
        }
    }

    public void run() {
        delegate.run();
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (needUpdate(TEXT, event)) {
            setText(delegate.getText());
            setAccelerator(delegate.getAccelerator());
        } else if (needUpdate(TOOL_TIP_TEXT, event)) {
            setToolTipText(delegate.getToolTipText());
        } else if (needUpdate(DESCRIPTION, event)) {
            setDescription(delegate.getDescription());
        } else if (needUpdate(IMAGE, event)) {
            setImageDescriptor(delegate.getImageDescriptor());
            setDisabledImageDescriptor(delegate.getDisabledImageDescriptor());
            setHoverImageDescriptor(delegate.getHoverImageDescriptor());
        } else if (needUpdate(ENABLED, event)) {
            setEnabled(delegate.isEnabled());
        } else if (needUpdate(CHECKED, event)) {
            setChecked(delegate.isChecked());
        }
    }

    private boolean needUpdate(String property, PropertyChangeEvent event) {
        return property.equals(event.getProperty())
                && properties.contains(property);
    }

    public void dispose() {
        delegate.removePropertyChangeListener(this);
    }
}