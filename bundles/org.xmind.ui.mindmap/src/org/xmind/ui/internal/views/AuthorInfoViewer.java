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

package org.xmind.ui.internal.views;

import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.xmind.ui.internal.editor.AuthorInfoEditor;

public abstract class AuthorInfoViewer {

    private Composite composite;

    private Composite displayWrap;

    private Label display;

    private Composite editorWrap;

    private StackLayout stack;

    private AuthorInfoEditor editor;

    public AuthorInfoViewer(Composite parent) {
        createControl(parent);
        setEditorVisible(true);
    }

    private void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        stack = new StackLayout();
        composite.setLayout(stack);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createDisplayWrap(composite);
        createEditorWrap(composite);
    }

    private void createDisplayWrap(Composite parent) {
        displayWrap = new Composite(parent, SWT.NONE);
        displayWrap.setBackground(parent.getBackground());
        GridLayout displayWrapLayout = new GridLayout(2, false);
        displayWrapLayout.marginWidth = 5;
        displayWrapLayout.marginHeight = 0;
        displayWrapLayout.verticalSpacing = 0;
        displayWrapLayout.horizontalSpacing = 5;
        displayWrap.setLayout(displayWrapLayout);

        createDisplay(displayWrap);
    }

    private void createDisplay(Composite parent) {
        display = new Label(parent, SWT.NONE);
        display.setBackground(parent.getBackground());
        display.setForeground(parent.getDisplay()
                .getSystemColor(SWT.COLOR_GRAY));
        display.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
    }

    private void createEditorWrap(Composite parent) {
        editorWrap = new Composite(parent, SWT.NONE);
        editorWrap.setBackground(parent.getBackground());
        editorWrap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout durationLayout = new GridLayout(1, false);
        durationLayout.marginWidth = 0;
        durationLayout.marginHeight = 0;
        durationLayout.verticalSpacing = 0;
        durationLayout.horizontalSpacing = 0;
        editorWrap.setLayout(durationLayout);
        createEditor(editorWrap);
    }

    protected void editLinkActivated() {
        editor.startEditing();
    }

    protected void createEditor(Composite parent) {
        editor = new AuthorInfoEditor(parent);
        editor.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        editor.addEditorListener(new ICellEditorListener() {
            public void editorValueChanged(boolean oldValidState,
                    boolean newValidState) {

            }

            public void cancelEditor() {
                refresh();
            }

            public void applyEditorValue() {
                changeContent();
            }
        });
    }

    protected abstract void refresh();

    protected abstract void changeContent();

    protected void setEditorFocus() {
        editor.setFocus();
    }

    public void setEditorVisible(boolean editing) {
        if (!composite.isDisposed()) {
            if (editing) {
                stack.topControl = editorWrap;
                displayWrap.setVisible(false);
                editorWrap.setVisible(true);
            } else {
                stack.topControl = displayWrap;
                displayWrap.setVisible(true);
                editorWrap.setVisible(false);
            }
            composite.layout();
            editorWrap.layout();
            displayWrap.layout();
        }
    }

    public boolean isEditorVisible() {
        return !composite.isDisposed() && stack.topControl == editorWrap;
    }

    /**
     * @return the display
     */
    public Label getDisplay() {
        return display;
    }

    public Control getControl() {
        return composite;
    }

    public AuthorInfoEditor getEditor() {
        return editor;
    }

    public void setEditor(AuthorInfoEditor editor) {
        this.editor = editor;
    }

    public void setEnabled(boolean enabled) {
        editor.setEnabled(enabled);
    }

    public void setFocus() {
        if (isEditorVisible()) {
            setEditorFocus();
        }
    }

}