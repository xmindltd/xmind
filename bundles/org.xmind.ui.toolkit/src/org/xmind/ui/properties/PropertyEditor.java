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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class PropertyEditor {

    /**
     * Identifier and enability property name for action 'copy'.
     */
    public static final String COPY = "copy"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'cut'.
     */
    public static final String CUT = "cut"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'paste'.
     */
    public static final String PASTE = "paste"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'selectAll'.
     */
    public static final String SELECT_ALL = "selectAll"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'delete'.
     */
    public static final String DELETE = "delete"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'undo'.
     */
    public static final String UNDO = "undo"; //$NON-NLS-1$

    /**
     * Identifier and enability property name for action 'redo'.
     */
    public static final String REDO = "redo"; //$NON-NLS-1$

    /**
     * Property 'value'.
     * 
     * @see #setValue(Object)
     * @see #getValue()
     */
    public static final String VALUE = "value"; //$NON-NLS-1$

    /**
     * Property 'activated'.
     * 
     * @see #isActivated()
     * @see #activate()
     * @see #deactivate()
     */
    public static final String ACTIVATED = "activated"; //$NON-NLS-1$

    /**
     * Property 'dirty'.
     * 
     * @see #isDirty()
     */
    public static final String DIRTY = "dirty"; //$NON-NLS-1$

    private Control control = null;

    private Object value = null;

    private boolean activated = false;

    private boolean dirty = false;

    private ListenerList editingListeners = new ListenerList();

    private ListenerList propertyChangeListeners = new ListenerList();

    private Map<String, Boolean> actionEnabilities = new HashMap<String, Boolean>(
            7);

    public void create(Composite parent) {
        control = createControl(parent);
        Assert.isNotNull(control);
        deactivateWidget();
    }

    public void dispose() {
        if (control != null && !control.isDisposed()) {
            control.dispose();
        }
        control = null;
    }

    protected abstract Control createControl(Composite parent);

    public Control getControl() {
        return control;
    }

    public void setFocus() {
        if (control != null && !control.isDisposed()) {
            control.setFocus();
        }
    }

    public void setBackground(Color color) {
    }

    public void setForeground(Color color) {
    }

    public void setFont(Font font) {
    }

    protected static void setAllControlsBackground(Control control, Color color) {
        if (control != null && !control.isDisposed()) {
            control.setBackground(color);
            if (control instanceof Composite) {
                for (Control c : ((Composite) control).getChildren()) {
                    setAllControlsBackground(c, color);
                }
            }
        }
    }

    protected static void setAllControlsForeground(Control control, Color color) {
        if (control != null && !control.isDisposed()) {
            control.setForeground(color);
            if (control instanceof Composite) {
                for (Control c : ((Composite) control).getChildren()) {
                    setAllControlsForeground(c, color);
                }
            }
        }
    }

    protected static void setAllControlsFont(Control control, Font font) {
        if (control != null && !control.isDisposed()) {
            control.setFont(font);
            if (control instanceof Composite) {
                for (Control c : ((Composite) control).getChildren()) {
                    setAllControlsFont(c, font);
                }
            }
        }
    }

    public void setValue(Object value) {
        Object oldValue = getValue();
        internalSetValue(value);
        setValueToWidget(value);
        clearDirty();
        Object newValue = getValue();
        if (oldValue != newValue
                && (oldValue == null || !oldValue.equals(newValue))) {
            firePropertyChange(VALUE, oldValue, newValue);
        }
    }

    protected void changeValue(Object newValue) {
        Object oldValue = getValue();
        internalSetValue(newValue);
        markDirty();
        if (oldValue != newValue
                && (oldValue == null || !oldValue.equals(newValue))) {
            firePropertyChange(VALUE, oldValue, newValue);
        }
    }

    protected final void internalSetValue(Object value) {
        this.value = value;
    }

    protected abstract void setValueToWidget(Object value);

    public Object getValue() {
        return this.value;
    }

    public void activate() {
        if (isActivated())
            return;

        this.activated = true;
        activateWidget();
        firePropertyChange(ACTIVATED, Boolean.valueOf(false),
                Boolean.valueOf(true));
    }

    public void deactivate() {
        if (!isActivated())
            return;

        this.activated = false;
        deactivateWidget();
        firePropertyChange(ACTIVATED, Boolean.valueOf(true),
                Boolean.valueOf(false));
    }

    protected void activateWidget() {
        if (control != null && !control.isDisposed()) {
            control.setVisible(true);
        }
    }

    protected void deactivateWidget() {
        if (control != null && !control.isDisposed()) {
            control.setVisible(false);
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void markDirty() {
        boolean oldDirty = this.dirty;
        this.dirty = true;
        if (oldDirty != this.dirty) {
            firePropertyChange(DIRTY, Boolean.valueOf(oldDirty),
                    Boolean.valueOf(this.dirty));
        }
    }

    protected void clearDirty() {
        boolean oldDirty = this.dirty;
        this.dirty = false;
        if (oldDirty != this.dirty) {
            firePropertyChange(DIRTY, Boolean.valueOf(oldDirty),
                    Boolean.valueOf(this.dirty));
        }
    }

    public void addEditingListener(IEditingListener listener) {
        editingListeners.add(listener);
    }

    public void removeEditingListener(IEditingListener listener) {
        editingListeners.remove(listener);
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    public void performAction(String actionId) {
    }

    public boolean isActionEnabled(String actionId) {
        Boolean enabled = actionEnabilities.get(actionId);
        return enabled != null && enabled.booleanValue();
    }

    protected void setActionEnabled(String actionId, boolean enabled) {
        boolean oldEnabled = isActionEnabled(actionId);
        actionEnabilities.put(actionId, Boolean.valueOf(enabled));
        boolean newEnabled = isActionEnabled(actionId);
        if (oldEnabled != newEnabled) {
            firePropertyChange(actionId, Boolean.valueOf(oldEnabled),
                    Boolean.valueOf(newEnabled));
        }
    }

    protected void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        final PropertyChangeEvent event = new PropertyChangeEvent(this,
                propertyName, oldValue, newValue);
        Object[] listeners = propertyChangeListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.propertyChange(event);
                }
            });
        }
    }

    protected void fireApplyEditorValue() {
        Object[] listeners = editingListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IEditingListener listener = (IEditingListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.editingFinished();
                }
            });
        }
    }

    protected void fireCancelEditing() {
        Object[] listeners = editingListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IEditingListener listener = (IEditingListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    listener.editingCanceled();
                }
            });
        }
    }
}
