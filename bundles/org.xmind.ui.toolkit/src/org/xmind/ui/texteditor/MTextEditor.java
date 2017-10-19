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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @author Frank Shaka
 * 
 */
public class MTextEditor extends MEmbeddedEditor {

    private class EventHandler implements Listener {
        public void handleEvent(Event event) {
            if (event.widget == input) {
                if (event.type == SWT.DefaultSelection) {
                    endEditing();
                } else if (event.type == SWT.Traverse) {
                    if (event.detail == SWT.TRAVERSE_ESCAPE) {
                        cancelEditing();
                    }
                } else if (event.type == SWT.Modify) {
                    if (updating)
                        return;
                    modifying = true;
                    setText(input.getText());
                    modifying = false;
                } else if (event.type == SWT.FocusOut) {
                    endEditingWhenFocusOut();
                }
            } else if (event.widget == getButton().getControl()) {
                if (((Control) event.widget).isEnabled()) {
                    if (event.type == SWT.KeyDown) {
                        handleKeyDownOnButton(event);
                    }
                }
            }
        }
    }

    private Text input;

    private Listener eventHandler;

    private String text = ""; //$NON-NLS-1$

    private boolean deleteAllEnalbed = false;

    private boolean updating = false;

    private boolean modifying = false;

    /**
     * 
     */
    public MTextEditor(Composite parent) {
        super(parent);
        getButton().getControl().addListener(SWT.KeyDown, eventHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.texteditor.MEmbeddedEditor#createContainer(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected Composite createContainer(Composite parent) {
        eventHandler = new EventHandler();
        return super.createContainer(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.texteditor.MEmbeddedEditor#createEditor(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    protected void createEditor(Composite parent) {
        input = new Text(parent, SWT.SINGLE | SWT.BORDER);
        input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        input.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_INFO_BACKGROUND));
        input.addListener(SWT.Traverse, eventHandler);
        input.addListener(SWT.Modify, eventHandler);
        input.addListener(SWT.FocusOut, eventHandler);
        input.addListener(SWT.DefaultSelection, eventHandler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.texteditor.MEmbeddedEditor#setEditorFocus()
     */
    @Override
    protected void setEditorFocus() {
        input.setFocus();
    }

    public Control getInputControl() {
        return input;
    }

    public void setText(String text) {
        String oldText = this.text;
        this.text = text;
        textChanged(text, oldText);
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param newText
     * @param oldText
     */
    protected void textChanged(String newText, String oldText) {
        if (!input.isDisposed() && !modifying) {
            updating = true;
            input.setText(newText);
            updating = false;
        }
        if (!getButton().getControl().isDisposed()) {
            getButton().setText(newText);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        input.setEnabled(enabled);
    }

    /**
     * @param deleteAllEnalbed
     *            the deleteAllEnalbed to set
     */
    public void setDeleteAllEnalbed(boolean deleteAllEnalbed) {
        this.deleteAllEnalbed = deleteAllEnalbed;
    }

    /**
     * @return the deleteAllEnalbed
     */
    public boolean isDeleteAllEnalbed() {
        return deleteAllEnalbed;
    }

    protected void handleKeyDownOnButton(Event event) {
        if (event.stateMask == 0) {
            if (event.character >= '0' && event.character <= '9') {
                setText(String.valueOf(event.character));
                startEditing();
                input.setSelection(input.getCharCount());
            } else if (event.keyCode == SWT.DEL || event.keyCode == SWT.BS) {
                if (isDeleteAllEnalbed()) {
                    setText(""); //$NON-NLS-1$
                    fireApplyEditorValue();
                }
            }
        }
    }

}
