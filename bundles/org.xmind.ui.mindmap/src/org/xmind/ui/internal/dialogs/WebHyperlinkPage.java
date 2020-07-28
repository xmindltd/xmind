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
package org.xmind.ui.internal.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.dialogs.HyperlinkPage;

/**
 * 
 * @author Frank Shaka
 * 
 */
public class WebHyperlinkPage extends HyperlinkPage implements Listener {

    private Composite composite;

    private Text text;

    private boolean isModifyingValue = false;

    public WebHyperlinkPage() {
    }

    public void init(IEditorPart editor, IStructuredSelection selection) {
    }

    /**
     * 
     */
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        createLabel(composite);
        createText(composite);
    }

    private void createLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ((GridData) label.getLayoutData()).widthHint = 380;
        label.setText(DialogMessages.WebHyperlinkPage_label);
    }

    /**
     * @param parent
     */
    private void createText(Composite parent) {
        text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        text.setText("http://"); //$NON-NLS-1$
        text.addListener(SWT.Modify, this);
        text.addListener(SWT.FocusIn, this);
    }

    public void setValue(String value) {
        super.setValue(value);
        if (!isModifyingValue) {
            if (text != null && !text.isDisposed()) {
                if (value != null) {
                    text.setText(value);
                }
            }
        }
    }

    public void dispose() {
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (text != null && !text.isDisposed()) {
            text.setFocus();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
     * Event)
     */
    public void handleEvent(Event event) {
        if (event.widget == text) {
            if (event.type == SWT.Modify) {
                isModifyingValue = true;
                setValue(text.getText());
                isModifyingValue = false;
                boolean isFinish = true;
                String content = text.getText().trim();
                if ("".equals(content)) //$NON-NLS-1$
                    isFinish = false;
                setCanFinish(isFinish);
                setErrorMessage(isFinish ? null
                        : DialogMessages.WebHyperlinkPage_nullHyper_message);
            } else if (event.type == SWT.FocusIn) {
                text.selectAll();
            }
        }
    }
}
