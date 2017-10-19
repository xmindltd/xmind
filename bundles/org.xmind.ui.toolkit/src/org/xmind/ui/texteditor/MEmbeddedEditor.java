/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/

package org.xmind.ui.texteditor;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.viewers.MButton;

/**
 * @author Frank Shaka
 * 
 */
public abstract class MEmbeddedEditor {

    private Composite composite;

    private MButton button;

    private Composite editorWrap;

    private StackLayout stack;

    private ListenerList editorListeners = new ListenerList();

    /**
     * 
     */
    public MEmbeddedEditor(Composite parent) {
        this(parent, MButton.NORMAL);
        hideEditor();
    }

    /**
     * 
     */
    public MEmbeddedEditor(Composite parent, int buttonStyle) {
        createControl(parent, buttonStyle);
        hideEditor();
    }

    private void createControl(Composite parent, int buttonStyle) {
        parent = createContainer(parent);
        composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        stack = new StackLayout();
        composite.setLayout(stack);

        createButton(composite, buttonStyle);
        createWrap(composite);
    }

    protected Composite createContainer(Composite parent) {
        return parent;
    }

    private void createButton(Composite parent, int buttonStyle) {
        button = new MButton(parent, buttonStyle);
        button.getControl().setBackground(parent.getBackground());
        button.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                startEditing();
            }
        });
    }

    private void createWrap(Composite parent) {
        editorWrap = new Composite(parent, SWT.NONE);
        editorWrap.setBackground(parent.getBackground());
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        editorWrap.setLayout(gridLayout);
        createEditor(editorWrap);
    }

    protected abstract void createEditor(Composite parent);

    public Control getControl() {
        return composite;
    }

    /**
     * @return the button
     */
    public MButton getButton() {
        return button;
    }

    public void startEditing() {
        showEditor();
        setFocus();
    }

    protected void showEditor() {
        stack.topControl = editorWrap;
        button.getControl().setVisible(false);
        editorWrap.setVisible(true);
        composite.layout();
        editorWrap.layout();
    }

    protected void hideEditor() {
        stack.topControl = button.getControl();
        button.getControl().setVisible(true);
        editorWrap.setVisible(false);
        composite.layout();
        editorWrap.layout();
    }

    public void cancelEditing() {
        if (isEditing()) {
            fireCancelEditor();
        }
        hideEditor();
    }

    public void endEditing() {
        if (isEditing()) {
            fireApplyEditorValue();
        }
        hideEditor();
    }

    public boolean isEditing() {
        return stack.topControl == editorWrap;
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }

    public void setFocus() {
        if (isEditing()) {
            setEditorFocus();
        } else {
            button.getControl().setFocus();
        }
    }

    protected abstract void setEditorFocus();

    public void addEditorListener(ICellEditorListener listener) {
        editorListeners.add(listener);
    }

    public void removeEditorListener(ICellEditorListener listener) {
        editorListeners.remove(listener);
    }

    protected void fireApplyEditorValue() {
        Object[] listeners = editorListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final ICellEditorListener l = (ICellEditorListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    l.applyEditorValue();
                }
            });
        }
    }

    protected void fireCancelEditor() {
        Object[] listeners = editorListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final ICellEditorListener l = (ICellEditorListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    l.cancelEditor();
                }
            });
        }
    }

    protected void endEditingWhenFocusOut() {
        if (!isEditing() || editorWrap.isDisposed() || !hasFocus(editorWrap))
            return;
        Display.getCurrent().timerExec(10, new Runnable() {
            public void run() {
                if (editorWrap.isDisposed())
                    return;
                if (!hasFocus(editorWrap)) {
                    endEditing();
                }
            }
        });
    }

    private boolean hasFocus(Control control) {
        if (control.isFocusControl())
            return true;
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (int i = 0; i < children.length; i++) {
                Control child = children[i];
                if (child.isFocusControl())
                    return true;
                if (child instanceof Composite && hasFocus((Composite) child))
                    return true;
            }
        }
        return false;
    }

}
