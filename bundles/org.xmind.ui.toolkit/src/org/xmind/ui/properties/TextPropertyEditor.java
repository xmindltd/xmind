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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class TextPropertyEditor extends PropertyEditor {

    private static final int DEFAULT_STYLE = SWT.SINGLE | SWT.BORDER;

    private int style;

    private Text text;

    private boolean modifying = false;

    private boolean regardEmptyStringAsNull;

    /**
     * 
     */
    public TextPropertyEditor() {
        this(DEFAULT_STYLE, false);
    }

    public TextPropertyEditor(boolean regardEmptyStringAsNull) {
        this(DEFAULT_STYLE, regardEmptyStringAsNull);
    }

    public TextPropertyEditor(int style) {
        this(style, false);
    }

    public TextPropertyEditor(int style, boolean regardEmptyStringAsNull) {
        this.style = style;
        this.regardEmptyStringAsNull = regardEmptyStringAsNull;
    }

    protected Control createControl(Composite parent) {
        text = new Text(parent, style);
        text.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                fireApplyEditorValue();
            }

            public void focusGained(FocusEvent e) {
            }
        });
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    fireCancelEditing();
                    e.doit = false;
                } else if (e.detail == SWT.TRAVERSE_RETURN) {
                    fireApplyEditorValue();
                    e.doit = false;
                }
            }
        });
        text.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                if (e.keyCode == 27 && e.stateMask == 0) {
                    fireCancelEditing();
                    e.doit = false;
                } else if (e.keyCode == 13) {
                    fireApplyEditorValue();
                    e.doit = false;
                }
            }
        });
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (modifying)
                    return;
                String value = text.getText();
                if (regardEmptyStringAsNull && "".equals(value)) { //$NON-NLS-1$
                    value = null;
                }
                changeValue(value);
            }
        });
        text.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.character == '\r') {
                    fireApplyEditorValue();
                    e.doit = false;
                }
            }
        });
        return text;
    }

    @Override
    protected void setValueToWidget(Object value) {
        if (text == null || text.isDisposed())
            return;
        modifying = true;
        try {
            String content = value == null ? "" : value.toString(); //$NON-NLS-1$
            text.setText(content);
        } finally {
            modifying = false;
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        text.setFont(font);
    }

}
